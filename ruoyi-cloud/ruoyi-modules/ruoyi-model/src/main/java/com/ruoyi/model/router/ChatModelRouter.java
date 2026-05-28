package com.ruoyi.model.router;

import java.util.Date;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.client.ChatModelClient;
import com.ruoyi.model.domain.ModelCallLog;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.service.IModelCallLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 模型路由器
 *
 * @author ruoyi
 */
@Component
public class ChatModelRouter implements ChatModelClient
{
    private static final Logger log = LoggerFactory.getLogger(ChatModelRouter.class);

    @Autowired
    @Qualifier("deepSeekClient")
    private ChatModelClient primaryClient;

    @Autowired
    @Qualifier("glmClient")
    private ChatModelClient fallbackClient;

    @Autowired
    private IModelCallLogService modelCallLogService;

    @Override
    public ChatResponse chat(ChatRequest request)
    {
        ChatResponse response = primaryClient.chat(request);
        if (response != null && response.isSuccess() && StringUtils.isNotEmpty(response.getContent()))
        {
            logModelCall(request, response);
            return response;
        }

        String errorMessage = response == null ? "主模型返回空响应" : response.getErrorMessage();
        logModelCall(request, response);
        log.warn("Primary model failed: {}, falling back to GLM", errorMessage);
        ChatResponse fallbackResp = fallbackClient.chat(request);
        if (fallbackResp != null)
        {
            fallbackResp.setFallback(true);
        }
        logModelCall(request, fallbackResp);
        return fallbackResp;
    }

    /**
     * 记录模型调用日志
     *
     * @param request 对话请求
     * @param response 对话响应
     */
    private void logModelCall(ChatRequest request, ChatResponse response)
    {
        try
        {
            ModelCallLog modelCallLog = buildModelCallLog(request, response);
            modelCallLogService.logCall(modelCallLog);
        }
        catch (Exception e)
        {
            log.warn("Trigger model call log failed", e);
        }
    }

    /**
     * 构建模型调用日志
     *
     * @param request 对话请求
     * @param response 对话响应
     * @return 模型调用日志
     */
    private ModelCallLog buildModelCallLog(ChatRequest request, ChatResponse response)
    {
        ModelCallLog modelCallLog = new ModelCallLog();
        if (request != null)
        {
            modelCallLog.setUserId(request.getUserId());
            modelCallLog.setSessionId(parseSessionId(request.getSessionId()));
        }
        if (response != null)
        {
            modelCallLog.setProvider(response.getProvider());
            modelCallLog.setModelName(response.getModelName());
            modelCallLog.setIsFallback(response.isFallback() ? 1 : 0);
            modelCallLog.setPromptTokens(response.getPromptTokens());
            modelCallLog.setCompletionTokens(response.getCompletionTokens());
            modelCallLog.setTotalTokens(response.getTotalTokens());
            modelCallLog.setCostTime(toInteger(response.getCostTime()));
            modelCallLog.setStatus(response.isSuccess() ? "success" : "failed");
            modelCallLog.setErrorMessage(response.getErrorMessage());
        }
        else
        {
            modelCallLog.setIsFallback(0);
            modelCallLog.setStatus("failed");
            modelCallLog.setErrorMessage("模型返回空响应");
        }
        modelCallLog.setCreateTime(new Date());
        modelCallLog.setDeleted(0);
        return modelCallLog;
    }

    /**
     * 转换会话 ID
     *
     * @param sessionId 会话 ID
     * @return Long 会话 ID
     */
    private Long parseSessionId(String sessionId)
    {
        if (StringUtils.isEmpty(sessionId))
        {
            return null;
        }
        try
        {
            return Long.valueOf(sessionId);
        }
        catch (NumberFormatException e)
        {
            log.debug("Ignore non-numeric sessionId: {}", sessionId);
            return null;
        }
    }

    /**
     * 转换耗时
     *
     * @param value 耗时
     * @return Integer 耗时
     */
    private Integer toInteger(Long value)
    {
        if (value == null)
        {
            return null;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value.intValue();
    }
}
