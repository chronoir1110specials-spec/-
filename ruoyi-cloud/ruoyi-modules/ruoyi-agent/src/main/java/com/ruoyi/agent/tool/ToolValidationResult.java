package com.ruoyi.agent.tool;

/**
 * 工具输入校验结果。
 *
 * @author ruoyi
 */
public class ToolValidationResult
{
    private final boolean valid;

    private final String message;

    private ToolValidationResult(boolean valid, String message)
    {
        this.valid = valid;
        this.message = message;
    }

    public static ToolValidationResult ok()
    {
        return new ToolValidationResult(true, null);
    }

    public static ToolValidationResult fail(String message)
    {
        return new ToolValidationResult(false, message);
    }

    public boolean isValid()
    {
        return valid;
    }

    public String getMessage()
    {
        return message;
    }
}
