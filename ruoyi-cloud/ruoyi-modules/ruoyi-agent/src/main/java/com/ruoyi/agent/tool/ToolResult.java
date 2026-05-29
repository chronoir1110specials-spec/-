package com.ruoyi.agent.tool;

/**
 * 工具执行结果（结构化）。
 *
 * <p>设计 8.13：工具执行失败时返回结构化错误，允许 Agent 据此决定重试或降级；
 * 结果过长时只回传摘要。</p>
 *
 * @param <O> 输出类型
 * @author ruoyi
 */
public class ToolResult<O>
{
    private final boolean success;

    private final O data;

    /** 结果摘要（截断后，用于审计与回传 Agent） */
    private final String summary;

    private final String errorCode;

    private final String errorMessage;

    private ToolResult(boolean success, O data, String summary, String errorCode, String errorMessage)
    {
        this.success = success;
        this.data = data;
        this.summary = summary;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static <O> ToolResult<O> ok(O data, String summary)
    {
        return new ToolResult<O>(true, data, summary, null, null);
    }

    public static <O> ToolResult<O> fail(String errorCode, String errorMessage)
    {
        return new ToolResult<O>(false, null, null, errorCode, errorMessage);
    }

    public boolean isSuccess()
    {
        return success;
    }

    public O getData()
    {
        return data;
    }

    public String getSummary()
    {
        return summary;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
}
