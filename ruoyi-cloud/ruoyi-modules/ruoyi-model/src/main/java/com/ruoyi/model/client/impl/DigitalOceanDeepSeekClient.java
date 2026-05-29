package com.ruoyi.model.client.impl;

import com.ruoyi.model.domain.ModelConfig;
import com.ruoyi.model.service.IModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DigitalOcean DeepSeek V4 Pro 客户端
 *
 * @author ruoyi
 */
@Component("deepSeekClient")
public class DigitalOceanDeepSeekClient extends AbstractHttpChatClient
{
    private static final String DEFAULT_MODEL_NAME = "deepseek-v4-pro";

    private static final String DEFAULT_PROVIDER = "digitalocean";

    private static final long CACHE_TTL_MILLIS = 30_000L;

    private volatile ModelConfig primaryConfig;

    private volatile long lastLoadTime = 0L;

    @Autowired
    private IModelConfigService modelConfigService;

    @Override
    protected String getModelName()
    {
        ModelConfig cfg = getPrimaryConfig();
        return cfg != null && cfg.getModelName() != null ? cfg.getModelName() : DEFAULT_MODEL_NAME;
    }

    @Override
    protected String getBaseUrl()
    {
        ModelConfig cfg = getPrimaryConfig();
        return cfg != null ? cfg.getBaseUrl() : null;
    }

    @Override
    protected String getApiKey()
    {
        ModelConfig cfg = getPrimaryConfig();
        return cfg != null ? cfg.getApiKey() : null;
    }

    @Override
    protected String getProvider()
    {
        ModelConfig cfg = getPrimaryConfig();
        return cfg != null && cfg.getProvider() != null ? cfg.getProvider() : DEFAULT_PROVIDER;
    }

    @Override
    protected Integer getDefaultMaxTokens()
    {
        ModelConfig cfg = getPrimaryConfig();
        return cfg != null ? cfg.getMaxTokens() : null;
    }

    /**
     * 获取主模型配置
     *
     * @return 主模型配置
     */
    protected ModelConfig getPrimaryConfig()
    {
        long now = System.currentTimeMillis();
        if (primaryConfig == null || now - lastLoadTime > CACHE_TTL_MILLIS)
        {
            synchronized (this)
            {
                if (primaryConfig == null || now - lastLoadTime > CACHE_TTL_MILLIS)
                {
                    primaryConfig = modelConfigService.getEnabledPrimary();
                    lastLoadTime = System.currentTimeMillis();
                }
            }
        }
        return primaryConfig;
    }
}
