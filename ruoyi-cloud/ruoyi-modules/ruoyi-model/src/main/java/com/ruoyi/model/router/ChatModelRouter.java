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
import org.springframework.beans.factory.annotation.Value;
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

    /** 主模型失败后的重试次数（设计 7.4 router.retry-times，默认 1） */
    @Value("${ai.model.router.retry-times:1}")
    private int retryTimes;

    @Override
    public ChatResponse chat(ChatRequest request)
    {
        int attempts = 1 + Math.max(0, retryTimes);
        ChatResponse response = null;
        // 主模型：失败（超时/5xx/429/空内容/异常/格式不符）则重试，重试耗尽再兜底
        for (int i = 1; i <= attempts; i++)
        {
            response = primaryClient.chat(request);
            logModelCall(request, response);
            if (isUsable(response))
            {
                return response;
            }
            log.warn("主模型第 {}/{} 次调用失败: {}", i, attempts, reasonOf(response));
        }

        log.warn("主模型重试耗尽({} 次)，触发兜底模型。原因: {}", attempts, reasonOf(response));
        ChatResponse fallbackResp = fallbackClient.chat(request);
        if (fallbackResp != null)
        {
            fallbackResp.setFallback(true);
        }
        logModelCall(request, fallbackResp);
        return fallbackResp;
    }

    /**
     * 判断响应是否可用（成功且非空内容）。
     */
    private boolean isUsable(ChatResponse response)
    {
        return response != null && response.isSuccess() && StringUtils.isNotEmpty(response.getContent());
    }

    /**
     * 提取失败原因。
     */
    private String reasonOf(ChatResponse response)
    {
        if (response == null)
        {
            return "空响应";
        }
        if (!response.isSuccess())
        {
            return StringUtils.isEmpty(response.getErrorMessage()) ? "调用失败" : response.getErrorMessage();
        }
        return "返回空内容";
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
