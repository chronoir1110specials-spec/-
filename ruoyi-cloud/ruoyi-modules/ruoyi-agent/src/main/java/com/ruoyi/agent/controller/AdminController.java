package com.ruoyi.agent.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.domain.KbDocument;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.mapper.ChatMessageMapper;
import com.ruoyi.agent.mapper.ChatSessionMapper;
import com.ruoyi.agent.mapper.KbDocumentMapper;
import com.ruoyi.agent.mapper.UserProfileMapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.model.domain.ModelCallLog;
import com.ruoyi.model.domain.ModelConfig;
import com.ruoyi.model.service.IModelCallLogService;
import com.ruoyi.model.service.IModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
public class AdminController
{
    private static final Integer NOT_DELETED = 0;

    @Autowired
    private IModelConfigService modelConfigService;

    @Autowired
    private IModelCallLogService modelCallLogService;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @GetMapping("/model/list")
    public R<List<ModelConfig>> listModels()
    {
        return R.ok(modelConfigService.listAll());
    }

    @PostMapping("/model/save")
    public R<ModelConfig> saveModel(@RequestBody ModelConfig config)
    {
        return R.ok(modelConfigService.save(config));
    }

    @GetMapping("/log/list")
    public R<List<ModelCallLog>> listLogs(@RequestParam(value = "limit", required = false) Integer limit)
    {
        return R.ok(modelCallLogService.listRecent(limit == null ? 50 : limit));
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
