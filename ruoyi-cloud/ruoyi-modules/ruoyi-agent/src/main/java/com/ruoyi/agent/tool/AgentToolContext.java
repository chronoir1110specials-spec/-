package com.ruoyi.agent.tool;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工具执行上下文。
 *
 * <p>携带调用方身份与本次 Agent 执行的工具调用计数，用于权限校验与
 * {@code maxToolCalls} 预算控制。</p>
 *
 * @author ruoyi
 */
public class AgentToolContext
{
    /** 调用用户 ID */
    private final Long userId;

    /** 会话 ID（可空） */
    private final Long sessionId;

    /** 触发工具的 Agent 类型 */
    private final String agentType;

    /** 本次 Agent 执行已发生的工具调用次数 */
    private final AtomicInteger toolCallCount = new AtomicInteger(0);

    public AgentToolContext(Long userId, Long sessionId, String agentType)
    {
        this.userId = userId;
        this.sessionId = sessionId;
        this.agentType = agentType;
    }

    public Long getUserId()
    {
        return userId;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public String getAgentType()
    {
        return agentType;
    }

    /**
     * 递增工具调用计数并返回最新值。
     *
     * @return 递增后的调用次数
     */
    public int incrementToolCalls()
    {
        return toolCallCount.incrementAndGet();
    }

    public int getToolCallCount()
    {
        return toolCallCount.get();
    }
}
