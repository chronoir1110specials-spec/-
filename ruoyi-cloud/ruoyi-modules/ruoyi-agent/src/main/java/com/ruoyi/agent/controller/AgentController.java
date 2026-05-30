package com.ruoyi.agent.controller;

import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.service.IChatSessionService;
import com.ruoyi.agent.service.impl.CareerPlanAgentService;
import com.ruoyi.agent.service.impl.InterviewAgentService;
import com.ruoyi.agent.service.impl.JobAnalysisAgentService;
import com.ruoyi.agent.service.impl.MaterialAgentService;
import com.ruoyi.agent.service.impl.ResumeAgentService;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private RemoteModelService remoteModelService;

    @Autowired
    private InterviewAgentService interviewAgentService;

    @Autowired
    private CareerPlanAgentService careerPlanAgentService;

    @Autowired
    private MaterialAgentService materialAgentService;

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private com.ruoyi.agent.tool.ToolRegistry toolRegistry;

    @Autowired
    private com.ruoyi.agent.runtime.AgentDefinitionRegistry agentDefinitionRegistry;

    @Autowired
    private com.ruoyi.agent.core.IntentRouter intentRouter;

    /**
     * 意图识别路由（设计 8.4）：对用户输入分类到具体 Agent 类型，返回意图与置信度，
     * 供前端统一入口自动调度。
     */
    @PostMapping("/route")
    public R<java.util.Map<String, Object>> route(@RequestBody AskRequest request)
    {
        String text = request == null ? null : request.getMessage();
        com.ruoyi.agent.core.IntentRouter.IntentResult result = intentRouter.detect(text);
        com.ruoyi.agent.runtime.AgentDefinition def = agentDefinitionRegistry.get(result.getAgentType());
        java.util.Map<String, Object> data = new java.util.HashMap<String, Object>();
        data.put("agentType", result.getAgentType());
        data.put("displayName", def == null ? result.getAgentType() : def.getDisplayName());
        data.put("confidence", result.getConfidence());
        data.put("reason", result.getReason());
        return R.ok(data);
    }

    /**
     * 列出已注册工具与 Agent 定义（用于后台展示 Agent 能力）
     */
    @GetMapping("/tools")
    public R<java.util.Map<String, Object>> tools()
    {
        java.util.List<java.util.Map<String, Object>> toolList = new java.util.ArrayList<java.util.Map<String, Object>>();
        for (com.ruoyi.agent.tool.AgentTool<?, ?> tool : toolRegistry.listTools())
        {
            java.util.Map<String, Object> t = new java.util.HashMap<String, Object>();
            t.put("name", tool.name());
            t.put("description", tool.description());
            t.put("readOnly", tool.isReadOnly());
            t.put("destructive", tool.isDestructive());
            toolList.add(t);
        }
        java.util.List<java.util.Map<String, Object>> defList = new java.util.ArrayList<java.util.Map<String, Object>>();
        for (com.ruoyi.agent.runtime.AgentDefinition def : agentDefinitionRegistry.list())
        {
            java.util.Map<String, Object> d = new java.util.HashMap<String, Object>();
            d.put("agentType", def.getAgentType());
            d.put("displayName", def.getDisplayName());
            d.put("allowedTools", def.getAllowedTools());
            d.put("ragPolicy", def.getRagPolicy());
            d.put("memoryPolicy", def.getMemoryPolicy());
            d.put("modelPolicy", def.getModelPolicy());
            d.put("maxToolCalls", def.getMaxToolCalls());
            defList.add(d);
        }
        java.util.Map<String, Object> result = new java.util.HashMap<String, Object>();
        result.put("tools", toolList);
        result.put("agents", defList);
        return R.ok(result);
    }

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
        Long userId = requireCurrentUserId();
        return R.ok(resumeAgentService.analyzeResume(userId, request.getResumeContent(), request.getResumeName()));
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
        Long userId = requireCurrentUserId();
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
        chatRequest.setUserId(requireCurrentUserId());
        chatRequest.setContent(request.getMessage());
        com.ruoyi.common.core.domain.R<ChatResponse> r = remoteModelService.chat(chatRequest, SecurityConstants.INNER);
        return R.ok(r == null ? null : r.getData());
    }

    /**
     * 开始模拟面试
     */
    @PostMapping("/interview/start")
    public R<ChatResponse> startInterview(@RequestBody InterviewStartRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getPosition()))
        {
            return R.fail("面试岗位不能为空");
        }
        int totalQuestions = request.getTotalQuestions() == null ? 5 : request.getTotalQuestions();
        if (totalQuestions <= 0)
        {
            return R.fail("面试题数必须大于 0");
        }
        String difficulty = StringUtils.isEmpty(request.getDifficulty()) ? "normal" : request.getDifficulty();
        return R.ok(interviewAgentService.startInterview(requireCurrentUserId(), request.getPosition(), difficulty,
                totalQuestions));
    }

    /**
     * 提交面试回答
     */
    @PostMapping("/interview/answer")
    public R<ChatResponse> answerInterviewQuestion(@RequestBody InterviewAnswerRequest request)
    {
        if (request == null || request.getSessionId() == null)
        {
            return R.fail("会话 ID 不能为空");
        }
        if (StringUtils.isEmpty(request.getAnswer()))
        {
            return R.fail("回答内容不能为空");
        }
        ensureInterviewSessionOwner(request.getSessionId());
        return R.ok(interviewAgentService.answerQuestion(request.getSessionId(), request.getAnswer()));
    }

    /**
     * 进入下一题
     */
    @PostMapping("/interview/next")
    public R<ChatResponse> nextInterviewQuestion(@RequestBody InterviewNextRequest request)
    {
        if (request == null || request.getSessionId() == null)
        {
            return R.fail("会话 ID 不能为空");
        }
        ensureInterviewSessionOwner(request.getSessionId());
        return R.ok(interviewAgentService.nextQuestion(request.getSessionId()));
    }

    /**
     * 查询模拟面试进度
     */
    @GetMapping("/interview/status/{sessionId}")
    public R<ChatSession> interviewStatus(@PathVariable Long sessionId)
    {
        ChatSession session = ensureInterviewSessionOwner(sessionId);
        return R.ok(session);
    }

    /**
     * 获取面试总结
     */
    @GetMapping("/interview/summary/{sessionId}")
    public R<ChatResponse> interviewSummary(@PathVariable Long sessionId)
    {
        ensureInterviewSessionOwner(sessionId);
        return R.ok(interviewAgentService.summarize(sessionId));
    }

    /**
     * 职业规划
     */
    @PostMapping("/career/plan")
    public R<ChatResponse> careerPlan()
    {
        return R.ok(careerPlanAgentService.generatePlan(requireCurrentUserId()));
    }

    /**
     * 求职材料生成
     */
    @PostMapping("/material/generate")
    public R<ChatResponse> generateMaterial(@RequestBody MaterialGenerateRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getMaterialType()))
        {
            return R.fail("材料类型不能为空");
        }
        return R.ok(materialAgentService.generateMaterial(requireCurrentUserId(), request.getMaterialType()));
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

    private ChatSession ensureInterviewSessionOwner(Long sessionId)
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

    /**
     * 简历优化请求
     */
    public static class ResumeOptimizeRequest
    {
        private String resumeName;

        private String resumeContent;

        public String getResumeName()
        {
            return resumeName;
        }

        public void setResumeName(String resumeName)
        {
            this.resumeName = resumeName;
        }

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

    /**
     * 开始面试请求
     */
    public static class InterviewStartRequest
    {
        private String position;

        private String difficulty;

        private Integer totalQuestions;

        public String getPosition()
        {
            return position;
        }

        public void setPosition(String position)
        {
            this.position = position;
        }

        public String getDifficulty()
        {
            return difficulty;
        }

        public void setDifficulty(String difficulty)
        {
            this.difficulty = difficulty;
        }

        public Integer getTotalQuestions()
        {
            return totalQuestions;
        }

        public void setTotalQuestions(Integer totalQuestions)
        {
            this.totalQuestions = totalQuestions;
        }
    }

    /**
     * 面试回答请求
     */
    public static class InterviewAnswerRequest
    {
        private Long sessionId;

        private String answer;

        public Long getSessionId()
        {
            return sessionId;
        }

        public void setSessionId(Long sessionId)
        {
            this.sessionId = sessionId;
        }

        public String getAnswer()
        {
            return answer;
        }

        public void setAnswer(String answer)
        {
            this.answer = answer;
        }
    }

    /**
     * 下一题请求
     */
    public static class InterviewNextRequest
    {
        private Long sessionId;

        public Long getSessionId()
        {
            return sessionId;
        }

        public void setSessionId(Long sessionId)
        {
            this.sessionId = sessionId;
        }
    }

    /**
     * 求职材料生成请求
     */
    public static class MaterialGenerateRequest
    {
        private String materialType;

        public String getMaterialType()
        {
            return materialType;
        }

        public void setMaterialType(String materialType)
        {
            this.materialType = materialType;
        }
    }
}
