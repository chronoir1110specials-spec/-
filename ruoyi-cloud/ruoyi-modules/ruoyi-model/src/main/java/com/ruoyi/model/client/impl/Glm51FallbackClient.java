package com.ruoyi.model.client.impl;

import com.ruoyi.model.domain.ModelConfig;
import com.ruoyi.model.service.IModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * GLM 5.1 兜底客户端
 *
 * @author ruoyi
 */
@Component("glmClient")
public class Glm51FallbackClient extends AbstractHttpChatClient
{
    private static final String DEFAULT_MODEL_NAME = "glm-5.1";

    private static final String DEFAULT_PROVIDER = "glm";

    private static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas/v4";

    private volatile ModelConfig fallbackConfig;

    @Autowired
    private IModelConfigService modelConfigService;

    @Override
    protected String getModelName()
    {
        ModelConfig cfg = getFallbackConfig();
        return cfg != null && cfg.getModelName() != null ? cfg.getModelName() : DEFAULT_MODEL_NAME;
    }

    @Override
    protected String getBaseUrl()
    {
        ModelConfig cfg = getFallbackConfig();
        return cfg != null && cfg.getBaseUrl() != null ? cfg.getBaseUrl() : DEFAULT_BASE_URL;
    }

    @Override
    protected String getApiKey()
    {
        ModelConfig cfg = getFallbackConfig();
        return cfg != null ? cfg.getApiKey() : null;
    }

    @Override
    protected String getProvider()
    {
        ModelConfig cfg = getFallbackConfig();
        return cfg != null && cfg.getProvider() != null ? cfg.getProvider() : DEFAULT_PROVIDER;
    }

    @Override
    protected Integer getDefaultMaxTokens()
    {
        ModelConfig cfg = getFallbackConfig();
        return cfg != null ? cfg.getMaxTokens() : null;
    }

    /**
     * 获取兜底模型配置
     *
     * @return 兜底模型配置
     */
    protected ModelConfig getFallbackConfig()
    {
        if (fallbackConfig == null)
        {
            fallbackConfig = modelConfigService.getEnabledFallback();
        }
        return fallbackConfig;
    }
}
