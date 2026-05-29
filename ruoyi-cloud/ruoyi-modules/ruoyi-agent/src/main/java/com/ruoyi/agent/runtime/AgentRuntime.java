package com.ruoyi.agent.runtime;

import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.tool.AgentToolContext;
import com.ruoyi.agent.tool.ToolRegistry;
import com.ruoyi.agent.tool.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Agent Runtime 骨架（设计 8.10 / 8.12 Workflow 模式）。
 *
 * <p>统一承载工具治理与审计：按 {@link AgentDefinition} 校验工具白名单与
 * {@code maxToolCalls} 预算，经 {@link ToolRegistry} 执行，并写 agent_step_log。
 * 复杂 AgenticLoop 作为扩展，不在毕设核心范围。</p>
 *
 * @author ruoyi
 */
@Component
public class AgentRuntime
{
    private static final Logger log = LoggerFactory.getLogger(AgentRuntime.class);

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private AgentDefinitionRegistry definitionRegistry;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    /**
     * 在 Agent 上下文中执行一个工具（带白名单/预算校验 + 审计）。
     *
     * @param context   工具上下文（含 agentType、userId、调用计数）
     * @param toolName  工具名
     * @param input     工具输入
     * @param taskId    所属 Agent 任务 ID（用于审计，可空）
     * @param stepIndex 步骤序号
     * @param <I>       输入类型
     * @param <O>       输出类型
     * @return 结构化工具结果
     */
    public <I, O> ToolResult<O> runTool(AgentToolContext context, String toolName, I input, Long taskId,
            int stepIndex)
    {
        String agentType = context == null ? null : context.getAgentType();
        AgentDefinition definition = agentType == null ? null : definitionRegistry.get(agentType);

        // 工具白名单校验
        if (definition != null && !definition.allowsTool(toolName))
        {
            ToolResult<O> denied = ToolResult.fail("TOOL_NOT_ALLOWED",
                    agentType + " 不允许调用工具 " + toolName);
            audit(taskId, context, stepIndex, toolName, input, denied);
            return denied;
        }

        // maxToolCalls 预算校验
        if (definition != null && definition.getMaxToolCalls() > 0)
        {
            int used = context.incrementToolCalls();
            if (used > definition.getMaxToolCalls())
            {
                ToolResult<O> exceeded = ToolResult.fail("MAX_TOOL_CALLS_EXCEEDED",
                        "超过最大工具调用次数 " + definition.getMaxToolCalls());
                audit(taskId, context, stepIndex, toolName, input, exceeded);
                return exceeded;
            }
        }

        ToolResult<O> result = toolRegistry.invoke(toolName, input, context);
        audit(taskId, context, stepIndex, toolName, input, result);
        return result;
    }

    private void audit(Long taskId, AgentToolContext context, int stepIndex, String toolName, Object input,
            ToolResult<?> result)
    {
        if (taskId == null)
        {
            return;
        }
        try
        {
            String status = result != null && result.isSuccess() ? "succeeded" : "failed";
            String outputSummary = result == null ? null
                    : (result.isSuccess() ? result.getSummary() : result.getErrorMessage());
            String error = result == null || result.isSuccess() ? null : result.getErrorMessage();
            agentStepLogService.log(taskId, context == null ? null : context.getUserId(),
                    context == null ? null : context.getSessionId(),
                    context == null ? null : context.getAgentType(),
                    stepIndex, "tool_call", toolName, truncate(String.valueOf(input)), truncate(outputSummary),
                    status, error);
        }
        catch (Exception e)
        {
            log.warn("工具调用审计写入失败 tool={}", toolName, e);
        }
    }

    private String truncate(String text)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) + "..." : text;
    }
}
