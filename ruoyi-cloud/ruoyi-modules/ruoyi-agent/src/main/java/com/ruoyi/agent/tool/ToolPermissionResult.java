package com.ruoyi.agent.tool;

/**
 * 工具权限校验结果。
 *
 * @author ruoyi
 */
public class ToolPermissionResult
{
    private final boolean allowed;

    private final String message;

    private ToolPermissionResult(boolean allowed, String message)
    {
        this.allowed = allowed;
        this.message = message;
    }

    public static ToolPermissionResult allow()
    {
        return new ToolPermissionResult(true, null);
    }

    public static ToolPermissionResult deny(String message)
    {
        return new ToolPermissionResult(false, message);
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public String getMessage()
    {
        return message;
    }
}
