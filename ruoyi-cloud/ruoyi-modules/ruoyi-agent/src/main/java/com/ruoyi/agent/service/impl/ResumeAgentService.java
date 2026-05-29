package com.ruoyi.agent.service.impl;

import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.core.TypeConverter;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.domain.ResumeInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.service.IAgentTaskService;
import com.ruoyi.agent.service.IResumeInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatResponse;
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
    private RemoteModelService remoteModelService;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IResumeInfoService resumeInfoService;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private OutputParser outputParser;

    @Autowired
    private IAgentTaskService agentTaskService;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    public ChatResponse analyzeResume(Long userId, String resumeContent, String resumeName)
    {
        long startTime = System.currentTimeMillis();
        AgentTask task = agentTaskService.start(userId, null, AGENT_TYPE, "resume_optimize", "sync",
                resumeName == null || resumeName.trim().isEmpty() ? "文本简历" : resumeName);
        try
        {
            UserProfile profile = userProfileService.getByUserId(userId);
            String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE);
            agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 1, "prompt", "构建简历优化提示词",
                    resumeName, systemPrompt, "succeeded", null);

            ChatRequest request = new ChatRequest();
            request.setUserId(userId);
            request.setContent(resumeContent);
            request.setHistory(buildSystemHistory(systemPrompt));

            R<ChatResponse> r = remoteModelService.chat(request, SecurityConstants.INNER);
            ChatResponse response = r == null ? null : r.getData();
            if (response != null && response.isSuccess())
            {
                // 结构化解析（带降级）：坏 JSON 时降级为文本，不让前端解析崩溃
                OutputParser.ParseResult parsed = outputParser.parseWithFallback(response.getContent(), "score");
                Integer score = parsed.isStructured()
                        ? TypeConverter.toInteger(parsed.getData().get("score")) : null;
                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 3, "output_parse", "结构化输出解析",
                        null, parsed.isStructured() ? "structured=true" : "structured=false(降级文本展示)",
                        parsed.isStructured() ? "succeeded" : "failed", parsed.getErrorMessage());

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

                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 2, "model", "调用模型优化简历",
                        resumeName, response.getContent(), "succeeded", null);
                agentTaskService.success(task.getId(), "finish", response.getContent(), response.getTotalTokens(),
                        response.getCostTime() == null ? (int) (System.currentTimeMillis() - startTime)
                                : response.getCostTime().intValue());
            }
            else
            {
                String errorMessage = response == null ? "模型服务无响应" : response.getErrorMessage();
                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 2, "model", "调用模型优化简历",
                        resumeName, null, "failed", errorMessage);
                agentTaskService.fail(task.getId(), "model", "MODEL_ERROR", errorMessage,
                        (int) (System.currentTimeMillis() - startTime));
            }
            return response;
        }
        catch (RuntimeException e)
        {
            agentTaskService.fail(task.getId(), "exception", "SYSTEM_ERROR", e.getMessage(),
                    (int) (System.currentTimeMillis() - startTime));
            throw e;
        }
    }
}
