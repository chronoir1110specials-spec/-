package com.ruoyi.agent.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * 结构化输出解析器
 * 从模型回复中提取 JSON，支持代码块和裸 JSON
 *
 * @author ruoyi
 */
@Component
public class OutputParser
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```json\\s*([\\s\\S]*?)```", Pattern.DOTALL);

    /**
     * 从模型回复中提取 JSON 字符串
     * 支持 ```json ... ``` 和裸 JSON 两种格式
     *
     * @param content 模型回复内容
     * @return JSON 字符串
     */
    public Optional<String> extractJson(String content)
    {
        if (content == null || content.isEmpty())
        {
            return Optional.empty();
        }

        // 尝试 ```json ... ```
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(content);
        if (matcher.find())
        {
            return Optional.of(matcher.group(1).trim());
        }

        // 尝试裸 JSON { ... }
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start >= 0 && end > start)
        {
            return Optional.of(content.substring(start, end + 1).trim());
        }

        return Optional.empty();
    }

    /**
     * 解析为 Map
     *
     * @param content 模型回复内容
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseToMap(String content)
    {
        return extractJson(content).map(json -> {
            try
            {
                return (Map<String, Object>) OBJECT_MAPPER.readValue(json, Map.class);
            }
            catch (Exception e)
            {
                return new HashMap<String, Object>();
            }
        }).orElse(new HashMap<String, Object>());
    }

    /**
     * 解析为指定类型
     *
     * @param content 模型回复内容
     * @param clazz 目标类型
     * @param <T> 类型参数
     * @return 解析结果，失败返回 null
     */
    public <T> T parseToObject(String content, Class<T> clazz)
    {
        return extractJson(content).map(json -> {
            try
            {
                return OBJECT_MAPPER.readValue(json, clazz);
            }
            catch (Exception e)
            {
                return null;
            }
        }).orElse(null);
    }
}
