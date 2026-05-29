package com.ruoyi.agent.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 工具注册表。
 *
 * <p>Spring 自动注入全部 {@link AgentTool} 实现，统一负责输入校验、权限校验、
 * 执行与结果摘要截断。</p>
 *
 * @author ruoyi
 */
@Component
public class ToolRegistry
{
    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, AgentTool<?, ?>> tools = new LinkedHashMap<String, AgentTool<?, ?>>();

    public ToolRegistry(List<AgentTool<?, ?>> toolBeans)
    {
        if (toolBeans != null)
        {
            for (AgentTool<?, ?> tool : toolBeans)
            {
                tools.put(tool.name(), tool);
            }
        }
        log.info("ToolRegistry 已注册 {} 个工具: {}", tools.size(), tools.keySet());
    }

    /**
     * 是否存在指定工具。
     */
    public boolean contains(String name)
    {
        return tools.containsKey(name);
    }

    /**
     * 列出全部工具（用于展示）。
     */
    public List<AgentTool<?, ?>> listTools()
    {
        return new ArrayList<AgentTool<?, ?>>(tools.values());
    }

    public Map<String, AgentTool<?, ?>> getTools()
    {
        return Collections.unmodifiableMap(tools);
    }

    /**
     * 调用工具：校验输入 → 校验权限 → 执行 → 截断摘要。
     *
     * @param name    工具名
     * @param input   输入
     * @param context 上下文
     * @param <I>     输入类型
     * @param <O>     输出类型
     * @return 结构化结果
     */
    @SuppressWarnings("unchecked")
    public <I, O> ToolResult<O> invoke(String name, I input, AgentToolContext context)
    {
        AgentTool<I, O> tool = (AgentTool<I, O>) tools.get(name);
        if (tool == null)
        {
            return ToolResult.fail("TOOL_NOT_FOUND", "工具不存在: " + name);
        }
        try
        {
            ToolValidationResult validation = tool.validateInput(input, context);
            if (validation == null || !validation.isValid())
            {
                return ToolResult.fail("INVALID_INPUT",
                        validation == null ? "输入校验失败" : validation.getMessage());
            }
            ToolPermissionResult permission = tool.checkPermission(input, context);
            if (permission == null || !permission.isAllowed())
            {
                return ToolResult.fail("PERMISSION_DENIED",
                        permission == null ? "无权限调用该工具" : permission.getMessage());
            }
            O output = tool.execute(input, context);
            return ToolResult.ok(output, summarize(output, tool.maxResultChars()));
        }
        catch (Exception e)
        {
            log.warn("工具执行异常: {}", name, e);
            return ToolResult.fail("TOOL_EXECUTION_ERROR", e.getMessage());
        }
    }

    private String summarize(Object output, int maxChars)
    {
        if (output == null)
        {
            return "";
        }
        String text = String.valueOf(output);
        if (text.length() <= maxChars)
        {
            return text;
        }
        return text.substring(0, maxChars) + "...(truncated)";
    }
}
