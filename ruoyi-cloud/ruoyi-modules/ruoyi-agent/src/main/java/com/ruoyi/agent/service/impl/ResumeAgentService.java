package com.ruoyi.agent.service.impl;

import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.core.TypeConverter;
import com.ruoyi.agent.domain.ResumeInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IResumeInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.model.dto.ChatRequest;
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
public class ResumeAgentService extends BaseAgentService
{
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
    public ChatResponse analyzeResume(Long userId, String resumeContent, String resumeName)
    {
        UserProfile profile = userProfileService.getByUserId(userId);
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE);

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent(resumeContent);
        request.setHistory(buildSystemHistory(systemPrompt));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && response.isSuccess())
        {
            Map<String, Object> result = outputParser.parseToMap(response.getContent());
            Integer score = TypeConverter.toInteger(result.get("score"));

            ResumeInfo resumeInfo = new ResumeInfo();
            resumeInfo.setUserId(userId);
            resumeInfo.setResumeName(resumeName == null || resumeName.trim().isEmpty() ? "文本简历" : resumeName);
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
}
