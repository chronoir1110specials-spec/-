package com.ruoyi.agent.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.domain.ResumeInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.IResumeInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 简历优化 Agent 服务
 *
 * @author ruoyi
 */
@Service
public class ResumeAgentService
{
    private static final String ROLE_SYSTEM = "system";

    private static final String AGENT_TYPE = "resume";

    @Autowired
    private ChatModelRouter chatModelRouter;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IResumeInfoService resumeInfoService;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private OutputParser outputParser;

    /**
     * 分析并优化简历
     *
     * @param userId 用户 ID
     * @param resumeContent 简历内容
     * @return 模型响应
     */
    public ChatResponse analyzeResume(Long userId, String resumeContent)
    {
        UserProfile profile = userProfileService.getByUserId(userId);
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE)
                + "\n请以 JSON 格式输出，字段包括：score、summary、problems、suggestions、keywords。";

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent(resumeContent);
        request.setHistory(buildSystemHistory(systemPrompt));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && response.isSuccess())
        {
            Map<String, Object> result = outputParser.parseToMap(response.getContent());
            Integer score = toInteger(result.get("score"));

            ResumeInfo resumeInfo = new ResumeInfo();
            resumeInfo.setUserId(userId);
            resumeInfo.setResumeName("文本简历");
            resumeInfo.setFileType("text");
            resumeInfo.setContent(resumeContent);
            resumeInfo.setParseStatus("success");
            resumeInfo.setTargetPosition(profile == null ? null : profile.getTargetPosition());
            resumeInfo.setAnalysisResult(response.getContent());
            resumeInfo.setScore(score);
            resumeInfoService.save(resumeInfo);
        }
        return response;
    }

    private List<ChatMessageVo> buildSystemHistory(String systemPrompt)
    {
        List<ChatMessageVo> history = new ArrayList<ChatMessageVo>();
        ChatMessageVo system = new ChatMessageVo();
        system.setRole(ROLE_SYSTEM);
        system.setContent(systemPrompt);
        history.add(system);
        return history;
    }

    private Integer toInteger(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Double.valueOf(String.valueOf(value).trim()).intValue();
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
