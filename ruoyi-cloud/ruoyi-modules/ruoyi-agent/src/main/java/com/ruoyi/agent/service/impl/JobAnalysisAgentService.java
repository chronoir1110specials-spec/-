package com.ruoyi.agent.service.impl;

import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.core.TypeConverter;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.domain.JobInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.service.IAgentTaskService;
import com.ruoyi.agent.service.IJobInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 岗位分析 Agent 服务
 *
 * @author ruoyi
 */
@Service
public class JobAnalysisAgentService extends BaseAgentService
{
    private static final String AGENT_TYPE = "job_analysis";

    @Autowired
    private RemoteModelService remoteModelService;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private OutputParser outputParser;

    @Autowired
    private IAgentTaskService agentTaskService;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    public ChatResponse analyzeJob(Long userId, String jobDesc, String jobName, String company)
    {
        long startTime = System.currentTimeMillis();
        String taskInput = (company == null ? "" : company + " ") + (jobName == null ? "岗位分析" : jobName);
        AgentTask task = agentTaskService.start(userId, null, AGENT_TYPE, "job_analyze", "sync", taskInput);
        try
        {
            UserProfile profile = userProfileService.getByUserId(userId);
            String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE);
            agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 1, "prompt", "构建岗位分析提示词",
                    taskInput, systemPrompt, "succeeded", null);

            ChatRequest request = new ChatRequest();
            request.setUserId(userId);
            request.setContent(jobDesc);
            request.setHistory(buildSystemHistory(systemPrompt));

            R<ChatResponse> r = remoteModelService.chat(request, SecurityConstants.INNER);
            ChatResponse response = r == null ? null : r.getData();
            if (response != null && response.isSuccess())
            {
                Map<String, Object> result = outputParser.parseToMap(response.getContent());
                Integer matchScore = TypeConverter.toInteger(result.get("matchScore"));

                JobInfo jobInfo = new JobInfo();
                jobInfo.setUserId(userId);
                jobInfo.setJobName(jobName);
                jobInfo.setCompanyName(company);
                jobInfo.setJobDescription(jobDesc);
                jobInfo.setAnalysisResult(response.getContent());
                jobInfo.setMatchScore(matchScore);
                jobInfoService.save(jobInfo);

                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 2, "model", "调用模型分析岗位",
                        taskInput, response.getContent(), "succeeded", null);
                agentTaskService.success(task.getId(), "finish", response.getContent(), response.getTotalTokens(),
                        response.getCostTime() == null ? (int) (System.currentTimeMillis() - startTime)
                                : response.getCostTime().intValue());
            }
            else
            {
                String errorMessage = response == null ? "模型服务无响应" : response.getErrorMessage();
                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 2, "model", "调用模型分析岗位",
                        taskInput, null, "failed", errorMessage);
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
