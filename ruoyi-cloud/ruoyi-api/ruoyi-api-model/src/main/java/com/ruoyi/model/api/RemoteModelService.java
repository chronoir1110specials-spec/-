package com.ruoyi.model.api;

import java.util.List;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.model.api.domain.ModelCallLog;
import com.ruoyi.model.api.domain.ModelConfig;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatResponse;
import com.ruoyi.model.api.dto.ModelTestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 模型服务 Feign 接口
 *
 * @author ruoyi
 */
@FeignClient(contextId = "remoteModelService", value = ServiceNameConstants.MODEL_SERVICE, fallbackFactory = RemoteModelFallbackFactory.class)
public interface RemoteModelService
{
    @PostMapping("/model/chat/send")
    R<ChatResponse> chat(@RequestBody ChatRequest request, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/model/config/byRole/{modelRole}")
    R<ModelConfig> getConfigByRole(@PathVariable("modelRole") String modelRole, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/admin/model/list")
    R<List<ModelConfig>> listModels(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/admin/model/save")
    R<ModelConfig> saveModel(@RequestBody ModelConfig config, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/admin/log/list")
    R<List<ModelCallLog>> listLogs(@RequestParam("limit") Integer limit, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/model/test/primary")
    R<ModelTestResult> testPrimary(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/model/test/fallback")
    R<ModelTestResult> testFallback(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/model/log/record")
    R<Boolean> recordLog(@RequestBody ModelCallLog log, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
