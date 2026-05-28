package com.ruoyi.model.router;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.client.ChatModelClient;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
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

    @Override
    public ChatResponse chat(ChatRequest request)
    {
        ChatResponse response = primaryClient.chat(request);
        if (response != null && response.isSuccess() && StringUtils.isNotEmpty(response.getContent()))
        {
            return response;
        }

        String errorMessage = response == null ? "主模型返回空响应" : response.getErrorMessage();
        log.warn("Primary model failed: {}, falling back to GLM", errorMessage);
        ChatResponse fallbackResp = fallbackClient.chat(request);
        if (fallbackResp != null)
        {
            fallbackResp.setFallback(true);
        }
        return fallbackResp;
    }
}
