package com.ruoyi.agent.service;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.model.api.dto.ChatRequest.ChatMessageVo;

/**
 * Agent 服务基础能力
 *
 * @author ruoyi
 */
public abstract class BaseAgentService
{
    private static final String ROLE_SYSTEM = "system";

    protected List<ChatMessageVo> buildSystemHistory(String systemPrompt)
    {
        List<ChatMessageVo> history = new ArrayList<ChatMessageVo>();
        history.add(buildMessageVo(ROLE_SYSTEM, systemPrompt));
        return history;
    }

    protected ChatMessageVo buildMessageVo(String role, String content)
    {
        ChatMessageVo message = new ChatMessageVo();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    protected List<ChatMessageVo> toHistory(List<ChatMessage> messages)
    {
        List<ChatMessageVo> history = new ArrayList<ChatMessageVo>();
        if (messages == null)
        {
            return history;
        }
        for (ChatMessage message : messages)
        {
            if (message == null || message.getRole() == null || message.getContent() == null)
            {
                continue;
            }
            history.add(buildMessageVo(message.getRole(), message.getContent()));
        }
        return history;
    }
}
