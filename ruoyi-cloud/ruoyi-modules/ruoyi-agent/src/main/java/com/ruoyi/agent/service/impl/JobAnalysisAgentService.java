package com.ruoyi.agent.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ruoyi.agent.core.OutputParser;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.domain.JobInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.IJobInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
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
public class JobAnalysisAgentService
{
    private static final String ROLE_SYSTEM = "system";

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
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE)
                + "\n请以 JSON 格式输出，字段包括：requiredSkills、bonusSkills、matchScore、resumeAdvice、interviewTopics。";

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent(jobDesc);
        request.setHistory(buildSystemHistory(systemPrompt));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && response.isSuccess())
        {
            Map<String, Object> result = outputParser.parseToMap(response.getContent());
            Integer matchScore = toInteger(result.get("matchScore"));

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
