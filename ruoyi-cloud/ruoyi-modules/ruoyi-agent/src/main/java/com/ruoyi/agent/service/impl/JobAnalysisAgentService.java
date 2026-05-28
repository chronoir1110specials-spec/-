package com.ruoyi.agent.service.impl;

import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.core.TypeConverter;
import com.ruoyi.agent.domain.JobInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IJobInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
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
    private ChatModelRouter chatModelRouter;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private OutputParser outputParser;

    /**
     * 分析岗位 JD
     *
     * @param userId 用户 ID
     * @param jobDesc 岗位 JD
     * @param jobName 岗位名称
     * @param company 公司名称
     * @return 模型响应
     */
    public ChatResponse analyzeJob(Long userId, String jobDesc, String jobName, String company)
    {
        UserProfile profile = userProfileService.getByUserId(userId);
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE);

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent(jobDesc);
        request.setHistory(buildSystemHistory(systemPrompt));

        ChatResponse response = chatModelRouter.chat(request);
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
        }
        return response;
    }
}
