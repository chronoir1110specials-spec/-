package com.ruoyi.agent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.agent.core.ContextWindowManager;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.core.StreamingChatClient;
import com.ruoyi.agent.core.StreamingChatClient.StreamResult;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.service.IAgentTaskService;
import com.ruoyi.agent.service.IChatMessageService;
import com.ruoyi.agent.service.IChatSessionService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.domain.ModelCallLog;
import com.ruoyi.model.api.domain.ModelConfig;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatRequest.ChatMessageVo;
import com.ruoyi.model.api.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * 会话管理接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping
public class ChatSessionController
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatSessionController.class);

    private static final String ROLE_USER = "user";

    private static final String ROLE_ASSISTANT = "assistant";

    private static final String AGENT_TYPE = "chat";

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private RemoteModelService remoteModelService;

    @Autowired
    private ContextWindowManager contextWindowManager;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private IAgentTaskService agentTaskService;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    @Autowired
    private StreamingChatClient streamingChatClient;

    @Autowired
    private com.ruoyi.agent.core.RateLimitService rateLimitService;

    /**
     * 创建会话
     */
    @PostMapping({"/agent/session/create", "/chat/session/create"})
    public R<ChatSession> create(@RequestBody(required = false) ChatSession session)
    {
        if (session == null)
        {
            session = new ChatSession();
        }
        Long userId = requireCurrentUserId();
        session.setUserId(userId);
        return R.ok(chatSessionService.createSession(session));
    }

    /**
     * 用户会话列表
     */
    @GetMapping({"/agent/session/list", "/chat/session/list"})
    public R<List<ChatSession>> list()
    {
        return R.ok(chatSessionService.listByUserId(requireCurrentUserId()));
    }

    /**
     * 会话详情（含消息列表）
     */
    @GetMapping({"/agent/session/{id}", "/chat/session/{id}"})
    public R<Map<String, Object>> detail(@PathVariable Long id)
    {
        ChatSession session = ensureSessionOwner(id);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("session", session);
        result.put("messages", chatMessageService.listBySessionId(id));
        return R.ok(result);
    }

    /**
     * 修改标题
     */
    @PutMapping({"/agent/session/{id}/title", "/chat/session/{id}/title"})
    public R<Boolean> updateTitle(@PathVariable Long id, @RequestBody ChatSession session)
    {
        ensureSessionOwner(id);
        return R.ok(chatSessionService.updateTitle(id, session == null ? null : session.getTitle()));
    }

    /**
     * 删除会话
     */
    @DeleteMapping({"/agent/session/{id}", "/chat/session/{id}", "/agent/session/delete/{id}",
            "/chat/session/delete/{id}"})
    public R<Boolean> delete(@PathVariable Long id)
    {
        ensureSessionOwner(id);
        boolean result = chatSessionService.deleteSession(id);
        chatMessageService.deleteBySessionId(id);
        return R.ok(result);
    }

    /**
     * 查询会话消息
     */
    @GetMapping({"/agent/session/{id}/messages", "/chat/session/{id}/messages"})
    public R<List<ChatMessage>> messages(@PathVariable Long id)
    {
        ensureSessionOwner(id);
        return R.ok(chatMessageService.listBySessionId(id));
    }

    /**
     * 发消息并返回 AI 回复
     */
    @PostMapping({"/agent/session/{id}/messages", "/chat/session/{id}/messages", "/chat/message/send"})
    public R<ChatResponse> sendMessage(@PathVariable(value = "id", required = false) Long id,
            @RequestBody ChatRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getContent()))
        {
            return R.fail("消息内容不能为空");
        }
        Long sessionId = id;
        if (sessionId == null)
        {
            if (StringUtils.isEmpty(request.getSessionId()))
            {
                return R.fail("会话 ID 不能为空");
            }
            try
            {
                sessionId = Long.valueOf(request.getSessionId());
            }
            catch (NumberFormatException e)
            {
                return R.fail("会话 ID 格式不正确");
            }
        }
        ChatSession session = ensureSessionOwner(sessionId);
        Long userId = session.getUserId();
        if (!rateLimitService.tryAcquire(userId))
        {
            return R.fail("已达今日调用上限，请明日再试");
        }
        long startTime = System.currentTimeMillis();
        AgentTask task = agentTaskService.start(userId, sessionId, AGENT_TYPE, "chat_message", "sync", request.getContent());
        try
        {
            ChatMessage userMessage = new ChatMessage();
            userMessage.setSessionId(sessionId);
            userMessage.setUserId(userId);
            userMessage.setRole(ROLE_USER);
            userMessage.setContent(request.getContent());
            chatMessageService.saveMessage(userMessage);

            List<ChatMessage> historyMessages = chatMessageService.listBySessionId(sessionId);
            List<ChatMessage> filtered = new ArrayList<ChatMessage>();
            for (ChatMessage message : historyMessages)
            {
                if (message != null && (userMessage.getId() == null || !userMessage.getId().equals(message.getId())))
                {
                    filtered.add(message);
                }
            }
            List<ChatMessageVo> context = contextWindowManager.buildContext(filtered, session);
            UserProfile profile = userProfileService.getByUserId(userId);
            context.add(0, buildSystemMessage(promptBuilder.buildSystemPrompt(profile, AGENT_TYPE)));
            agentStepLogService.log(task.getId(), userId, sessionId, AGENT_TYPE, 1, "context", "构建对话上下文",
                    request.getContent(), "历史消息数：" + filtered.size(), "succeeded", null);

            request.setSessionId(String.valueOf(sessionId));
            request.setUserId(userId);
            request.setHistory(context);

            R<ChatResponse> r = remoteModelService.chat(request, SecurityConstants.INNER);
            ChatResponse response = r == null ? null : r.getData();
            if (response != null && StringUtils.isNotEmpty(response.getContent()))
            {
                ChatMessage assistantMessage = new ChatMessage();
                assistantMessage.setSessionId(sessionId);
                assistantMessage.setUserId(userId);
                assistantMessage.setRole(ROLE_ASSISTANT);
                assistantMessage.setContent(response.getContent());
                assistantMessage.setModelName(response.getModelName());
                chatMessageService.saveMessage(assistantMessage);

                agentStepLogService.log(task.getId(), userId, sessionId, AGENT_TYPE, 2, "model", "调用模型生成回复",
                        request.getContent(), response.getContent(), "succeeded", null);
                agentTaskService.success(task.getId(), "finish", response.getContent(), response.getTotalTokens(),
                        response.getCostTime() == null ? (int) (System.currentTimeMillis() - startTime)
                                : response.getCostTime().intValue());
            }
            else
            {
                String errorMessage = response == null ? "模型服务无响应" : response.getErrorMessage();
                agentStepLogService.log(task.getId(), userId, sessionId, AGENT_TYPE, 2, "model", "调用模型生成回复",
                        request.getContent(), null, "failed", errorMessage);
                agentTaskService.fail(task.getId(), "model", "MODEL_ERROR", errorMessage,
                        (int) (System.currentTimeMillis() - startTime));
            }
            return R.ok(response);
        }
        catch (RuntimeException e)
        {
            agentTaskService.fail(task.getId(), "exception", "SYSTEM_ERROR", e.getMessage(),
                    (int) (System.currentTimeMillis() - startTime));
            throw e;
        }
    }

    private ChatMessageVo buildSystemMessage(String content)
    {
        ChatMessageVo message = new ChatMessageVo();
        message.setRole("system");
        message.setContent(content);
        return message;
    }

    /**
     * 流式发消息（SSE）。事件：start / delta / done / error。
     */
    @PostMapping({"/agent/session/{id}/messages/stream", "/chat/session/{id}/messages/stream",
            "/chat/message/stream"})
    public SseEmitter streamMessage(@PathVariable(value = "id", required = false) Long id,
            @RequestBody ChatRequest request)
    {
        SseEmitter emitter = new SseEmitter(180_000L);
        if (request == null || StringUtils.isEmpty(request.getContent()))
        {
            completeWithError(emitter, "消息内容不能为空");
            return emitter;
        }
        Long sessionId = resolveSessionId(id, request);
        if (sessionId == null)
        {
            completeWithError(emitter, "会话 ID 不正确");
            return emitter;
        }

        // 以下在请求线程内完成（依赖登录态 / 请求上下文）
        ChatSession session = ensureSessionOwner(sessionId);
        Long userId = session.getUserId();
        if (!rateLimitService.tryAcquire(userId))
        {
            completeWithError(emitter, "已达今日调用上限，请明日再试");
            return emitter;
        }
        long startTime = System.currentTimeMillis();
        AgentTask task = agentTaskService.start(userId, sessionId, AGENT_TYPE, "chat_message", "stream",
                request.getContent());

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setRole(ROLE_USER);
        userMessage.setContent(request.getContent());
        chatMessageService.saveMessage(userMessage);

        List<ChatMessage> historyMessages = chatMessageService.listBySessionId(sessionId);
        List<ChatMessage> filtered = new ArrayList<ChatMessage>();
        for (ChatMessage message : historyMessages)
        {
            if (message != null && (userMessage.getId() == null || !userMessage.getId().equals(message.getId())))
            {
                filtered.add(message);
            }
        }
        List<ChatMessageVo> context = contextWindowManager.buildContext(filtered, session);
        UserProfile profile = userProfileService.getByUserId(userId);
        context.add(0, buildSystemMessage(promptBuilder.buildSystemPrompt(profile, AGENT_TYPE)));
        agentStepLogService.log(task.getId(), userId, sessionId, AGENT_TYPE, 1, "context", "构建对话上下文",
                request.getContent(), "历史消息数：" + filtered.size(), "succeeded", null);

        request.setSessionId(String.valueOf(sessionId));
        request.setUserId(userId);
        request.setHistory(context);

        ModelConfig primary = streamingChatClient.getPrimaryConfig();
        boolean canStream = primary != null && StringUtils.isNotEmpty(primary.getBaseUrl())
                && StringUtils.isNotEmpty(primary.getModelName()) && StringUtils.isNotEmpty(primary.getApiKey());

        CompletableFuture.runAsync(() -> runStream(emitter, request, session, task, primary, canStream, startTime));
        return emitter;
    }

    private ChatSession ensureSessionOwner(Long sessionId)
    {
        Long currentUserId = requireCurrentUserId();
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null)
        {
            throw new ServiceException("会话不存在", HttpStatus.NOT_FOUND);
        }
        if (session.getUserId() == null || !session.getUserId().equals(currentUserId))
        {
            throw new ServiceException("无权访问该会话", HttpStatus.FORBIDDEN);
        }
        return session;
    }

    private Long requireCurrentUserId()
    {
        Long userId;
        try
        {
            userId = SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            throw new ServiceException("当前用户未登录", HttpStatus.UNAUTHORIZED);
        }
        if (userId == null)
        {
            throw new ServiceException("当前用户未登录", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }

    private Long resolveSessionId(Long id, ChatRequest request)
    {
        if (id != null)
        {
            return id;
        }
        if (StringUtils.isEmpty(request.getSessionId()))
        {
            return null;
        }
        try
        {
            return Long.valueOf(request.getSessionId());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private void completeWithError(SseEmitter emitter, String message)
    {
        try
        {
            emitter.send(SseEmitter.event().name("error").data(message));
        }
        catch (IOException ignored)
        {
        }
        emitter.complete();
    }

    /**
     * 异步执行流式调用，逐块下发 SSE，并在结束后持久化消息与日志。
     */
    private void runStream(SseEmitter emitter, ChatRequest request, ChatSession session, AgentTask task,
            ModelConfig primary, boolean canStream, long startTime)
    {
        Long sessionId = session.getId();
        Long userId = session.getUserId();
        try
        {
            emitter.send(SseEmitter.event().name("start").data("{\"sessionId\":" + sessionId + "}"));

            String content;
            String modelName;
            Integer totalTokens;
            Integer promptTokens;
            Integer completionTokens;
            boolean fallback = false;
            String errorMessage = null;

            if (canStream)
            {
                StreamResult sr = streamingChatClient.streamChat(primary, request, piece -> trySend(emitter, piece));
                if (sr.isSuccess())
                {
                    content = sr.getContent().toString();
                    modelName = primary.getModelName();
                    totalTokens = sr.getTotalTokens();
                    promptTokens = sr.getPromptTokens();
                    completionTokens = sr.getCompletionTokens();
                    recordStreamLog(userId, sessionId, primary.getProvider(), modelName, 0, promptTokens,
                            completionTokens, totalTokens, (int) sr.getCostTime(), "success", null);
                    finishStream(emitter, request, session, task, content, modelName, totalTokens, startTime, null);
                    return;
                }
                errorMessage = sr.getErrorMessage();
            }

            // 流式不可用或主模型失败 → 回退到非流式 Feign 链路（保留 primary→fallback 兜底）
            R<ChatResponse> r = remoteModelService.chat(request, SecurityConstants.INNER);
            ChatResponse response = r == null ? null : r.getData();
            if (response != null && StringUtils.isNotEmpty(response.getContent()))
            {
                content = response.getContent();
                modelName = response.getModelName();
                totalTokens = response.getTotalTokens();
                fallback = true;
                trySend(emitter, content);
                finishStream(emitter, request, session, task, content, modelName, totalTokens, startTime,
                        fallback ? "fallback" : null);
            }
            else
            {
                String msg = response == null ? (errorMessage == null ? "模型服务无响应" : errorMessage)
                        : response.getErrorMessage();
                agentStepLogService.log(task.getId(), userId, sessionId, AGENT_TYPE, 2, "model", "流式生成回复",
                        request.getContent(), null, "failed", msg);
                agentTaskService.fail(task.getId(), "model", "MODEL_ERROR", msg,
                        (int) (System.currentTimeMillis() - startTime));
                emitter.send(SseEmitter.event().name("error").data(msg == null ? "生成失败" : msg));
                emitter.complete();
            }
        }
        catch (Exception e)
        {
            log.warn("Stream run failed", e);
            agentTaskService.fail(task.getId(), "exception", "SYSTEM_ERROR", e.getMessage(),
                    (int) (System.currentTimeMillis() - startTime));
            emitter.completeWithError(e);
        }
    }

    private void finishStream(SseEmitter emitter, ChatRequest request, ChatSession session, AgentTask task,
            String content, String modelName, Integer totalTokens, long startTime, String note) throws IOException
    {
        Long sessionId = session.getId();
        Long userId = session.getUserId();
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole(ROLE_ASSISTANT);
        assistantMessage.setContent(content);
        assistantMessage.setModelName(modelName);
        chatMessageService.saveMessage(assistantMessage);

        int costTime = (int) (System.currentTimeMillis() - startTime);
        agentStepLogService.log(task.getId(), userId, sessionId, AGENT_TYPE, 2, "model",
                note == null ? "流式生成回复" : "流式生成回复(" + note + ")",
                request.getContent(), content, "succeeded", null);
        agentTaskService.success(task.getId(), "finish", content, totalTokens, costTime);

        emitter.send(SseEmitter.event().name("done")
                .data("{\"modelName\":\"" + (modelName == null ? "" : modelName) + "\",\"totalTokens\":"
                        + (totalTokens == null ? "null" : totalTokens) + "}"));
        emitter.complete();
    }

    private void trySend(SseEmitter emitter, String piece)
    {
        try
        {
            emitter.send(SseEmitter.event().name("delta").data(piece));
        }
        catch (IOException e)
        {
            throw new RuntimeException("SSE 连接已断开", e);
        }
    }

    private void recordStreamLog(Long userId, Long sessionId, String provider, String modelName, int isFallback,
            Integer promptTokens, Integer completionTokens, Integer totalTokens, int costTime, String status,
            String errorMessage)
    {
        try
        {
            ModelCallLog logEntry = new ModelCallLog();
            logEntry.setUserId(userId);
            logEntry.setSessionId(sessionId);
            logEntry.setProvider(provider);
            logEntry.setModelName(modelName);
            logEntry.setIsFallback(isFallback);
            logEntry.setPromptTokens(promptTokens);
            logEntry.setCompletionTokens(completionTokens);
            logEntry.setTotalTokens(totalTokens);
            logEntry.setCostTime(costTime);
            logEntry.setStatus(status);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setCreateTime(new Date());
            remoteModelService.recordLog(logEntry, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            log.warn("Record stream model_call_log failed", e);
        }
    }
}
