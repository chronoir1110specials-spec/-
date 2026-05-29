package com.ruoyi.model.service.impl;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.client.ChatModelClient;
import com.ruoyi.model.domain.ModelConfig;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.dto.ModelTestResult;
import com.ruoyi.model.service.IModelConfigService;
import com.ruoyi.model.service.IModelTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 模型连通性测试 业务层实现
 *
 * @author ruoyi
 */
@Service
public class ModelTestServiceImpl implements IModelTestService
{
    /** 探活提示词 */
    private static final String PROBE_CONTENT = "请只回复两个字：正常";

    /** 探活最大输出 Token。需给推理型模型(deepseek-v4-flash 等)留出 reasoning 预算，
     *  否则 token 全被 reasoning_content 消耗、content 为空，连通性会误判为失败 */
    private static final Integer PROBE_MAX_TOKENS = 512;

    /** 回复内容展示截断长度 */
    private static final int REPLY_MAX_LENGTH = 100;

    @Autowired
    @Qualifier("deepSeekClient")
    private ChatModelClient primaryClient;

    @Autowired
    @Qualifier("glmClient")
    private ChatModelClient fallbackClient;

    @Autowired
    private IModelConfigService modelConfigService;

    @Override
    public ModelTestResult testPrimary()
    {
        return doTest("primary", primaryClient, modelConfigService.getEnabledPrimary());
    }

    @Override
    public ModelTestResult testFallback()
    {
        return doTest("fallback", fallbackClient, modelConfigService.getEnabledFallback());
    }

    /**
     * 执行单个模型探活
     *
     * @param modelRole 模型角色
     * @param client 模型客户端
     * @param config 模型配置
     * @return 测试结果
     */
    private ModelTestResult doTest(String modelRole, ChatModelClient client, ModelConfig config)
    {
        ModelTestResult result = new ModelTestResult();
        result.setModelRole(modelRole);
        result.setConfigured(isConfigured(config));
        if (config != null)
        {
            result.setProvider(config.getProvider());
            result.setModelName(config.getModelName());
        }

        if (!result.isConfigured())
        {
            result.setReachable(false);
            result.setErrorMessage("模型未配置或未启用：模型名/地址/Key 不完整");
            return result;
        }

        ChatRequest request = new ChatRequest();
        request.setContent(PROBE_CONTENT);
        request.setMaxTokens(PROBE_MAX_TOKENS);

        ChatResponse response = client.chat(request);
        if (response == null)
        {
            result.setReachable(false);
            result.setErrorMessage("模型返回空响应");
            return result;
        }

        result.setCostTime(response.getCostTime());
        result.setReachable(response.isSuccess() && StringUtils.isNotEmpty(response.getContent()));
        if (result.isReachable())
        {
            // 探活成功时以真实响应中的 provider/modelName 为准
            if (StringUtils.isNotEmpty(response.getProvider()))
            {
                result.setProvider(response.getProvider());
            }
            if (StringUtils.isNotEmpty(response.getModelName()))
            {
                result.setModelName(response.getModelName());
            }
            result.setReply(truncate(response.getContent()));
            result.setTotalTokens(response.getTotalTokens());
        }
        else
        {
            result.setErrorMessage(StringUtils.isNotEmpty(response.getErrorMessage())
                    ? response.getErrorMessage() : "模型返回空内容");
        }
        return result;
    }

    /**
     * 判断模型配置是否就绪
     *
     * @param config 模型配置
     * @return 是否就绪
     */
    private boolean isConfigured(ModelConfig config)
    {
        return config != null
                && StringUtils.isNotEmpty(config.getModelName())
                && StringUtils.isNotEmpty(config.getBaseUrl())
                && StringUtils.isNotEmpty(config.getApiKey());
    }

    /**
     * 截断回复内容
     *
     * @param content 回复内容
     * @return 截断后的内容
     */
    private String truncate(String content)
    {
        if (content == null)
        {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.length() > REPLY_MAX_LENGTH ? trimmed.substring(0, REPLY_MAX_LENGTH) + "..." : trimmed;
    }
}
