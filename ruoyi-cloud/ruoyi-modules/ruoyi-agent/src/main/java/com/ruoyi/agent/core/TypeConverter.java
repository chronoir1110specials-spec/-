package com.ruoyi.agent.core;

/**
 * 类型转换工具
 *
 * @author ruoyi
 */
public final class TypeConverter
{
    private TypeConverter()
    {
    }

    public static Integer toInteger(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Double.valueOf(String.valueOf(value).trim()).intValue();
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
