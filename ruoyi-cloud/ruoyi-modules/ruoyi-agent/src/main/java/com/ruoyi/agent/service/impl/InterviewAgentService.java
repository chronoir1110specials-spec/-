package com.ruoyi.agent.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.core.TypeConverter;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IChatMessageService;
import com.ruoyi.agent.service.IChatSessionService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 模拟面试 Agent 服务
 *
 * @author ruoyi
 */
@Service
public class InterviewAgentService extends BaseAgentService
{
    private static final String AGENT_TYPE = "interview";

    private static final String ROLE_USER = "user";

    private static final String ROLE_ASSISTANT = "assistant";

    private static final String STATUS_ANSWERING = "ANSWERING";

    private static final String STATUS_NEXT_QUESTION = "NEXT_QUESTION";

    private static final String STATUS_SUMMARIZING = "SUMMARIZING";

    private static final String STATUS_COMPLETED = "COMPLETED";

    @Autowired
    private ChatModelRouter chatModelRouter;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private OutputParser outputParser;

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private IUserProfileService userProfileService;

    public ChatResponse startInterview(Long userId, String position, String difficulty, int totalQuestions)
    {
        UserProfile profile = userProfileService.getByUserId(userId);
        String systemPrompt = buildInterviewSystemPrompt(profile, position, difficulty);

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent("请生成第 1 题，只输出面试问题本身。");
        request.setHistory(buildSystemHistory(systemPrompt));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && response.isSuccess() && StringUtils.isNotEmpty(response.getContent()))
        {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setTitle("模拟面试-" + position);
            session.setAgentType(AGENT_TYPE);
            session.setInterviewStatus(STATUS_ANSWERING);
            session.setCurrentQuestionIndex(1);
            session.setTotalQuestions(totalQuestions);
            session.setInterviewPosition(position);
            session.setInterviewDifficulty(difficulty);
            chatSessionService.createSession(session);

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setSessionId(session.getId());
            assistantMessage.setUserId(userId);
            assistantMessage.setRole(ROLE_ASSISTANT);
            assistantMessage.setContent(response.getContent());
            assistantMessage.setModelName(response.getModelName());
            chatMessageService.saveMessage(assistantMessage);
            response.setContent(wrapContentWithSessionId(session.getId(), "question", response.getContent()));
        }
        return response;
    }

    public ChatResponse answerQuestion(Long sessionId, String answer)
    {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null)
        {
            return ChatResponse.fail("会话不存在");
        }
        List<ChatMessage> messages = chatMessageService.listBySessionId(sessionId);
        String systemPrompt = buildInterviewSystemPrompt(userProfileService.getByUserId(session.getUserId()),
                session.getInterviewPosition(), session.getInterviewDifficulty());

        ChatRequest request = new ChatRequest();
        request.setSessionId(String.valueOf(sessionId));
        request.setUserId(session.getUserId());
        request.setContent(buildEvaluationPrompt(answer));
        request.setHistory(buildHistory(systemPrompt, messages));

        ChatResponse response = chatModelRouter.chat(request);
        saveMessage(session, ROLE_USER, answer, null);
        if (response != null && StringUtils.isNotEmpty(response.getContent()))
        {
            saveMessage(session, ROLE_ASSISTANT, response.getContent(), response.getModelName());
        }

        Integer questionIndex = session.getCurrentQuestionIndex() == null ? 1 : session.getCurrentQuestionIndex();
        Integer totalQuestions = session.getTotalQuestions() == null ? questionIndex : session.getTotalQuestions();
        String nextStatus = questionIndex >= totalQuestions ? STATUS_SUMMARIZING : STATUS_NEXT_QUESTION;
        chatSessionService.updateInterviewStatus(sessionId, nextStatus, questionIndex);
        return response;
    }

    public ChatResponse nextQuestion(Long sessionId)
    {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null)
        {
            return ChatResponse.fail("会话不存在");
        }
        int currentIndex = session.getCurrentQuestionIndex() == null ? 0 : session.getCurrentQuestionIndex();
        int totalQuestions = session.getTotalQuestions() == null ? 0 : session.getTotalQuestions();
        int nextIndex = currentIndex + 1;
        if (totalQuestions > 0 && nextIndex > totalQuestions)
        {
            chatSessionService.updateInterviewStatus(sessionId, STATUS_SUMMARIZING, currentIndex);
            return ChatResponse.fail("面试题已全部完成，请生成总结");
        }

        List<ChatMessage> messages = chatMessageService.listBySessionId(sessionId);
        String systemPrompt = buildInterviewSystemPrompt(userProfileService.getByUserId(session.getUserId()),
                session.getInterviewPosition(), session.getInterviewDifficulty());

        ChatRequest request = new ChatRequest();
        request.setSessionId(String.valueOf(sessionId));
        request.setUserId(session.getUserId());
        request.setContent("请基于历史表现生成第 " + nextIndex + " 题，只输出面试问题本身。");
        request.setHistory(buildHistory(systemPrompt, messages));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && response.isSuccess() && StringUtils.isNotEmpty(response.getContent()))
        {
            saveMessage(session, ROLE_ASSISTANT, response.getContent(), response.getModelName());
            chatSessionService.updateInterviewStatus(sessionId, STATUS_ANSWERING, nextIndex);
            response.setContent(wrapContentWithSessionId(sessionId, "question", response.getContent()));
        }
        return response;
    }

    public ChatResponse summarize(Long sessionId)
    {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null)
        {
            return ChatResponse.fail("会话不存在");
        }
        List<ChatMessage> messages = chatMessageService.listBySessionId(sessionId);
        String systemPrompt = buildInterviewSystemPrompt(userProfileService.getByUserId(session.getUserId()),
                session.getInterviewPosition(), session.getInterviewDifficulty());

        ChatRequest request = new ChatRequest();
        request.setSessionId(String.valueOf(sessionId));
        request.setUserId(session.getUserId());
        request.setContent("请根据完整面试记录生成总结。请以 JSON 格式输出，字段包括：score、summary、weakPoints、suggestions。");
        request.setHistory(buildHistory(systemPrompt, messages));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && response.isSuccess())
        {
            Map<String, Object> result = outputParser.parseToMap(response.getContent());
            Integer score = TypeConverter.toInteger(result.get("score"));
            String summary = result.get("summary") == null ? response.getContent() : String.valueOf(result.get("summary"));
            chatSessionService.updateInterviewSummary(sessionId, STATUS_COMPLETED, score, response.getContent());
            if (StringUtils.isNotEmpty(response.getContent()))
            {
                saveMessage(session, ROLE_ASSISTANT, response.getContent(), response.getModelName());
            }
            response.setContent(wrapContentWithSessionId(sessionId, "summary", summary));
        }
        return response;
    }

    private String buildInterviewSystemPrompt(UserProfile profile, String position, String difficulty)
    {
        StringBuilder prompt = new StringBuilder(promptBuilder.buildSystemPrompt(profile, AGENT_TYPE));
        prompt.append("\n【面试设置】\n");
        prompt.append("- 岗位方向：").append(position).append("\n");
        prompt.append("- 难度：").append(difficulty).append("\n");
        prompt.append("点评回答时请以 JSON 格式输出，字段包括：score、comment、advantages、problems、referenceAnswer。\n");
        return prompt.toString();
    }

    private List<ChatMessageVo> buildHistory(String systemPrompt, List<ChatMessage> messages)
    {
        List<ChatMessageVo> history = buildSystemHistory(systemPrompt);
        history.addAll(toHistory(messages));
        return history;
    }

    private String buildEvaluationPrompt(String answer)
    {
        return "这是学生对当前题目的回答，请点评并打分，输出 JSON：\n" + answer;
    }

    private void saveMessage(ChatSession session, String role, String content, String modelName)
    {
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setUserId(session.getUserId());
        message.setRole(role);
        message.setContent(content);
        message.setModelName(modelName);
        chatMessageService.saveMessage(message);
    }

    private String wrapContentWithSessionId(Long sessionId, String fieldName, String content)
    {
        return "{\"sessionId\":" + sessionId + ",\"" + fieldName + "\":\"" + escapeJson(content) + "\"}";
    }

    private String escapeJson(String content)
    {
        if (content == null)
        {
            return "";
        }
        return content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }
}
