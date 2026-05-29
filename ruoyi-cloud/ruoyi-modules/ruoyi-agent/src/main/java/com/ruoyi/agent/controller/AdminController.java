package com.ruoyi.agent.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.agent.domain.AgentStepLog;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.domain.KbDocument;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.mapper.ChatMessageMapper;
import com.ruoyi.agent.mapper.ChatSessionMapper;
import com.ruoyi.agent.mapper.KbDocumentMapper;
import com.ruoyi.agent.mapper.UserProfileMapper;
import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.service.IAgentTaskService;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.RequiresRoles;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.domain.ModelCallLog;
import com.ruoyi.model.api.domain.ModelConfig;
import com.ruoyi.model.api.dto.ModelTestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端接口。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/admin")
@RequiresRoles("admin")
public class AdminController
{
    private static final Integer NOT_DELETED = 0;

    @Autowired
    private RemoteModelService remoteModelService;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @Autowired
    private IAgentTaskService agentTaskService;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    @Autowired
    private com.ruoyi.agent.core.RateLimitService rateLimitService;

    @GetMapping("/model/list")
    public R<List<ModelConfig>> listModels()
    {
        R<List<ModelConfig>> r = remoteModelService.listModels(SecurityConstants.INNER);
        return r == null ? R.ok(null) : r;
    }

    @PostMapping("/model/save")
    public R<ModelConfig> saveModel(@RequestBody ModelConfig config)
    {
        R<ModelConfig> r = remoteModelService.saveModel(config, SecurityConstants.INNER);
        return r == null ? R.ok(null) : r;
    }

    @GetMapping("/log/list")
    public R<List<ModelCallLog>> listLogs(@RequestParam(value = "limit", required = false) Integer limit)
    {
        R<List<ModelCallLog>> r = remoteModelService.listLogs(limit == null ? 50 : limit, SecurityConstants.INNER);
        return r == null ? R.ok(null) : r;
    }

    @GetMapping("/model/test/primary")
    public R<ModelTestResult> testPrimaryModel()
    {
        R<ModelTestResult> r = remoteModelService.testPrimary(SecurityConstants.INNER);
        return r == null ? R.ok(null) : r;
    }

    @GetMapping("/model/test/fallback")
    public R<ModelTestResult> testFallbackModel()
    {
        R<ModelTestResult> r = remoteModelService.testFallback(SecurityConstants.INNER);
        return r == null ? R.ok(null) : r;
    }

    @GetMapping("/log/agent")
    public R<List<AgentTask>> listAgentLogs(@RequestParam(value = "limit", required = false) Integer limit)
    {
        return R.ok(agentTaskService.listRecent(limit));
    }

    @GetMapping("/log/agent/steps/{taskId}")
    public R<List<AgentStepLog>> listAgentSteps(@PathVariable("taskId") Long taskId)
    {
        return R.ok(agentStepLogService.listByTaskId(taskId));
    }

    /**
     * 查询当前每日调用限额
     */
    @GetMapping("/config/rate-limit")
    public R<Map<String, Object>> getRateLimit()
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("dailyLimit", rateLimitService.getDailyLimit());
        return R.ok(result);
    }

    /**
     * 修改每日调用限额（<=0 表示不限流）
     */
    @PostMapping("/config/rate-limit")
    public R<Boolean> setRateLimit(@RequestBody Map<String, Object> body)
    {
        Object value = body == null ? null : body.get("dailyLimit");
        if (value == null)
        {
            return R.fail("dailyLimit 不能为空");
        }
        int limit;
        try
        {
            limit = Integer.parseInt(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return R.fail("dailyLimit 必须为整数");
        }
        rateLimitService.setDailyLimit(limit);
        return R.ok(Boolean.TRUE);
    }

    @GetMapping("/stats")
    public R<Map<String, Long>> stats()
    {
        Map<String, Long> stats = new HashMap<String, Long>();
        stats.put("totalUsers", userProfileMapper.selectCount(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getDeleted, NOT_DELETED)));
        stats.put("totalSessions", chatSessionMapper.selectCount(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getDeleted, NOT_DELETED)));
        stats.put("totalMessages", chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getDeleted, NOT_DELETED)));
        stats.put("totalDocuments", kbDocumentMapper.selectCount(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getDeleted, NOT_DELETED)));
        return R.ok(stats);
    }
}
