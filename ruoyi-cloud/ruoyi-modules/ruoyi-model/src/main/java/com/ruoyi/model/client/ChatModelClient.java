package com.ruoyi.model.client;

import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;

/**
 * 统一模型客户端接口
 *
 * @author ruoyi
 */
public interface ChatModelClient
{
    /**
     * 发起对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    public ChatResponse chat(ChatRequest request);
}
