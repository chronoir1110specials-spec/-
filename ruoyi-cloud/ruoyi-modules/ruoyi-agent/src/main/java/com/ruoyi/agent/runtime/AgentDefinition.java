package com.ruoyi.agent.runtime;

import java.util.List;

/**
 * Agent 定义（设计 8.11）。
 *
 * <p>把 Agent 的能力配置从业务代码中抽离。毕设阶段以 Java 配置形式声明，
 * 后续可放入 agent_definition 表由管理员维护。</p>
 *
 * @author ruoyi
 */
public class AgentDefinition
{
    /** Agent 类型标识，例如 resume_optimize、job_analyze */
    private final String agentType;

    /** 前端展示名称 */
    private final String displayName;

    /** 适用场景说明 */
    private final String description;

    /** 工具白名单 */
    private final List<String> allowedTools;

    /** 记忆策略：none / session / profile / history */
    private final String memoryPolicy;

    /** RAG 策略：none / optional / required */
    private final String ragPolicy;

    /** 模型策略：primary_only / primary_with_fallback */
    private final String modelPolicy;

    /** 最大工具调用次数 */
    private final int maxToolCalls;

    /** 最大执行时间（毫秒） */
    private final int timeoutMs;

    public AgentDefinition(String agentType, String displayName, String description, List<String> allowedTools,
            String memoryPolicy, String ragPolicy, String modelPolicy, int maxToolCalls, int timeoutMs)
    {
        this.agentType = agentType;
        this.displayName = displayName;
        this.description = description;
        this.allowedTools = allowedTools;
        this.memoryPolicy = memoryPolicy;
        this.ragPolicy = ragPolicy;
        this.modelPolicy = modelPolicy;
        this.maxToolCalls = maxToolCalls;
        this.timeoutMs = timeoutMs;
    }

    public String getAgentType()
    {
        return agentType;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getDescription()
    {
        return description;
    }

    public List<String> getAllowedTools()
    {
        return allowedTools;
    }

    public String getMemoryPolicy()
    {
        return memoryPolicy;
    }

    public String getRagPolicy()
    {
        return ragPolicy;
    }

    public String getModelPolicy()
    {
        return modelPolicy;
    }

    public int getMaxToolCalls()
    {
        return maxToolCalls;
    }

    public int getTimeoutMs()
    {
        return timeoutMs;
    }

    public boolean allowsTool(String toolName)
    {
        return allowedTools != null && allowedTools.contains(toolName);
    }
}
