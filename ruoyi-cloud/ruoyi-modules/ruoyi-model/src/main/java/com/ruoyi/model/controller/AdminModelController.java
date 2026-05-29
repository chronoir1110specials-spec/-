package com.ruoyi.model.controller;

import java.util.List;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.model.domain.ModelCallLog;
import com.ruoyi.model.domain.ModelConfig;
import com.ruoyi.model.dto.ModelTestResult;
import com.ruoyi.model.service.IModelCallLogService;
import com.ruoyi.model.service.IModelConfigService;
import com.ruoyi.model.service.IModelTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型管理接口（供 ruoyi-agent 通过 Feign 调用）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping
public class AdminModelController
{
    @Autowired
    private IModelConfigService modelConfigService;

    @Autowired
    private IModelCallLogService modelCallLogService;

    @Autowired
    private IModelTestService modelTestService;

    @InnerAuth
    @GetMapping({"/admin/model/list", "/model/config/list"})
    public R<List<ModelConfig>> listModels()
    {
        return R.ok(modelConfigService.listAll());
    }

    @InnerAuth
    @PostMapping({"/admin/model/save", "/model/config/save"})
    public R<ModelConfig> saveModel(@RequestBody ModelConfig config)
    {
        return R.ok(modelConfigService.save(config));
    }

    @InnerAuth
    @GetMapping({"/admin/log/list", "/model/log/list", "/admin/log/model"})
    public R<List<ModelCallLog>> listLogs(@RequestParam(value = "limit", required = false) Integer limit)
    {
        return R.ok(modelCallLogService.listRecent(limit == null ? 50 : limit));
    }

    @InnerAuth
    @GetMapping("/model/config/byRole/{modelRole}")
    public R<ModelConfig> getByRole(@PathVariable String modelRole)
    {
        return R.ok(modelConfigService.getByModelRole(modelRole));
    }

    @InnerAuth
    @GetMapping("/model/test/primary")
    public R<ModelTestResult> testPrimary()
    {
        return R.ok(modelTestService.testPrimary());
    }

    @InnerAuth
    @GetMapping("/model/test/fallback")
    public R<ModelTestResult> testFallback()
    {
        return R.ok(modelTestService.testFallback());
    }

    /**
     * 记录一次模型调用日志（供 agent 流式对话回写）
     */
    @InnerAuth
    @PostMapping("/model/log/record")
    public R<Boolean> recordLog(@RequestBody ModelCallLog log)
    {
        modelCallLogService.logCall(log);
        return R.ok(Boolean.TRUE);
    }
}
