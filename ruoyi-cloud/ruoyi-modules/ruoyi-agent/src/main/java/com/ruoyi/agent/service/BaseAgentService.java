package com.ruoyi.agent.service;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;

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
        ChatMessageVo system = new ChatMessageVo();
        system.setRole(ROLE_SYSTEM);
        system.setContent(systemPrompt);
        history.add(system);
        return history;
    }
}
