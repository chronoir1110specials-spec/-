package com.ruoyi.model.api;

import java.util.Collections;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.model.api.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 模型服务 Feign 降级处理
 *
 * @author ruoyi
 */
@Component
public class RemoteModelFallbackFactory implements FallbackFactory<RemoteModelService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteModelFallbackFactory.class);

    @Override
    public RemoteModelService create(Throwable cause)
    {
        log.error("模型服务调用失败", cause);
        return new RemoteModelService()
        {
            @Override
            public R<ChatResponse> chat(com.ruoyi.model.api.dto.ChatRequest request, String source)
            {
                return R.ok(ChatResponse.fail("模型服务不可用: " + cause.getMessage()));
            }

            @Override
            public R<com.ruoyi.model.api.domain.ModelConfig> getConfigByRole(String modelRole, String source)
            {
                return R.fail("模型服务不可用");
            }

            @Override
            public R<java.util.List<com.ruoyi.model.api.domain.ModelConfig>> listModels(String source)
            {
                return R.ok(Collections.emptyList());
            }

            @Override
            public R<com.ruoyi.model.api.domain.ModelConfig> saveModel(com.ruoyi.model.api.domain.ModelConfig config, String source)
            {
                return R.fail("模型服务不可用");
            }

            @Override
            public R<java.util.List<com.ruoyi.model.api.domain.ModelCallLog>> listLogs(Integer limit, String source)
            {
                return R.ok(Collections.emptyList());
            }

            @Override
            public R<com.ruoyi.model.api.dto.ModelTestResult> testPrimary(String source)
            {
                return R.fail("模型服务不可用");
            }

            @Override
            public R<com.ruoyi.model.api.dto.ModelTestResult> testFallback(String source)
            {
                return R.fail("模型服务不可用");
            }

            @Override
            public R<Boolean> recordLog(com.ruoyi.model.api.domain.ModelCallLog log, String source)
            {
                return R.fail("模型服务不可用");
            }
        };
    }
}
