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

    /**
     * 带降级的结构化解析（设计 8.8.4）。
     *
     * <p>提取 JSON → 解析 → 校验必填字段；解析失败时尝试一次本地修复
     * （去除代码块标记、尾随逗号、智能引号）再解析；仍失败则降级为
     * {@code structured=false} 并回传原始文本，避免前端解析崩溃。</p>
     *
     * @param content        模型原始回复
     * @param requiredFields 必填字段（缺失则视为结构不完整，降级）
     * @return 解析结果
     */
    public ParseResult parseWithFallback(String content, String... requiredFields)
    {
        if (content == null || content.isEmpty())
        {
            return ParseResult.degraded("", "模型返回空内容");
        }
        Optional<String> jsonOpt = extractJson(content);
        if (jsonOpt.isPresent())
        {
            Map<String, Object> map = tryParse(jsonOpt.get());
            if (map == null)
            {
                // 本地修复一次
                map = tryParse(repair(jsonOpt.get()));
            }
            if (map != null && hasRequiredFields(map, requiredFields))
            {
                return ParseResult.structured(map);
            }
        }
        return ParseResult.degraded(content, "结构化解析失败，已降级为文本展示");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> tryParse(String json)
    {
        if (json == null)
        {
            return null;
        }
        try
        {
            return OBJECT_MAPPER.readValue(json, Map.class);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 本地修复常见 JSON 错误：去除代码块标记、尾随逗号、中文/智能引号。
     */
    private String repair(String json)
    {
        if (json == null)
        {
            return null;
        }
        String fixed = json.trim();
        fixed = fixed.replaceAll("^```(json)?", "").replaceAll("```$", "");
        // 智能引号 → 标准引号
        fixed = fixed.replace('“', '"').replace('”', '"')
                .replace('‘', '\'').replace('’', '\'');
        // 去除对象/数组结尾的尾随逗号
        fixed = fixed.replaceAll(",\\s*([}\\]])", "$1");
        return fixed.trim();
    }

    private boolean hasRequiredFields(Map<String, Object> map, String... requiredFields)
    {
        if (requiredFields == null || requiredFields.length == 0)
        {
            return true;
        }
        for (String field : requiredFields)
        {
            if (!map.containsKey(field))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 结构化解析结果。
     */
    public static class ParseResult
    {
        private final boolean structured;

        private final Map<String, Object> data;

        private final String rawText;

        private final String errorMessage;

        private ParseResult(boolean structured, Map<String, Object> data, String rawText, String errorMessage)
        {
            this.structured = structured;
            this.data = data;
            this.rawText = rawText;
            this.errorMessage = errorMessage;
        }

        public static ParseResult structured(Map<String, Object> data)
        {
            return new ParseResult(true, data, null, null);
        }

        public static ParseResult degraded(String rawText, String errorMessage)
        {
            return new ParseResult(false, null, rawText, errorMessage);
        }

        public boolean isStructured()
        {
            return structured;
        }

        public Map<String, Object> getData()
        {
            return data;
        }

        public String getRawText()
        {
            return rawText;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }
    }
}
