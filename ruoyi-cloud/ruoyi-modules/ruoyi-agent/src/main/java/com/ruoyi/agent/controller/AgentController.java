package com.ruoyi.agent.controller;

import com.ruoyi.agent.service.impl.JobAnalysisAgentService;
import com.ruoyi.agent.service.impl.ResumeAgentService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 统一接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agent")
public class AgentController
{
    @Autowired
    private ResumeAgentService resumeAgentService;

    @Autowired
    private JobAnalysisAgentService jobAnalysisAgentService;

    @Autowired
    private ChatModelRouter chatModelRouter;

    /**
     * 简历优化
     */
    @PostMapping("/resume/optimize")
    public R<ChatResponse> optimizeResume(@RequestBody ResumeOptimizeRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getResumeContent()))
        {
            return R.fail("简历内容不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        return R.ok(resumeAgentService.analyzeResume(userId, request.getResumeContent()));
    }

    /**
     * 岗位分析
     */
    @PostMapping("/job/analyze")
    public R<ChatResponse> analyzeJob(@RequestBody JobAnalyzeRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getJobDescription()))
        {
            return R.fail("岗位描述不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        return R.ok(jobAnalysisAgentService.analyzeJob(userId, request.getJobDescription(), request.getJobName(),
                request.getCompanyName()));
    }

    /**
     * 统一 Agent 问答
     */
    @PostMapping("/ask")
    public R<ChatResponse> ask(@RequestBody AskRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getMessage()))
        {
            return R.fail("消息内容不能为空");
        }
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setUserId(SecurityUtils.getUserId());
        chatRequest.setContent(request.getMessage());
        return R.ok(chatModelRouter.chat(chatRequest));
    }

    /**
     * 简历优化请求
     */
    public static class ResumeOptimizeRequest
    {
        private String resumeContent;

        public String getResumeContent()
        {
            return resumeContent;
        }

        public void setResumeContent(String resumeContent)
        {
            this.resumeContent = resumeContent;
        }
    }

    /**
     * 岗位分析请求
     */
    public static class JobAnalyzeRequest
    {
        private String jobName;

        private String companyName;

        private String jobDescription;

        public String getJobName()
        {
            return jobName;
        }

        public void setJobName(String jobName)
        {
            this.jobName = jobName;
        }

        public String getCompanyName()
        {
            return companyName;
        }

        public void setCompanyName(String companyName)
        {
            this.companyName = companyName;
        }

        public String getJobDescription()
        {
            return jobDescription;
        }

        public void setJobDescription(String jobDescription)
        {
            this.jobDescription = jobDescription;
        }
    }

    /**
     * 通用问答请求
     */
    public static class AskRequest
    {
        private String message;

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }
    }
}
