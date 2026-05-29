package com.ruoyi.agent.tool;

/**
 * Agent 统一工具协议（设计 8.13）。
 *
 * <p>工具不直接暴露给用户，由 Agent Runtime 按任务类型调用。每个工具必须声明
 * 输入结构，执行前做输入校验与权限校验；高风险工具显式标记 {@code isDestructive}。</p>
 *
 * @param <I> 输入类型
 * @param <O> 输出类型
 * @author ruoyi
 */
public interface AgentTool<I, O>
{
    /**
     * 工具唯一名称。
     */
    String name();

    /**
     * 工具用途说明（用于前端展示与调度）。
     */
    String description();

    /**
     * 输入类型。
     */
    Class<I> inputType();

    /**
     * 输入校验。
     */
    ToolValidationResult validateInput(I input, AgentToolContext context);

    /**
     * 权限校验。
     */
    ToolPermissionResult checkPermission(I input, AgentToolContext context);

    /**
     * 执行工具。
     */
    O execute(I input, AgentToolContext context);

    /**
     * 是否只读工具。
     */
    default boolean isReadOnly()
    {
        return true;
    }

    /**
     * 是否高风险（破坏性）工具，默认不允许普通 Agent 调用。
     */
    default boolean isDestructive()
    {
        return false;
    }

    /**
     * 结果摘要最大字符数，超过则截断。
     */
    default int maxResultChars()
    {
        return 20000;
    }
}
