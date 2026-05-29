package com.ruoyi.model.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.client.ChatModelClient;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 模型客户端基类
 *
 * @author ruoyi
 */
public abstract class AbstractHttpChatClient implements ChatModelClient
{
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final Double DEFAULT_TEMPERATURE = 0.7D;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    protected abstract String getModelName();

    /**
     * 获取 API 地址
     *
     * @return API 地址
     */
    protected abstract String getBaseUrl();

    /**
     * 获取 API Key
     *
     * @return API Key
     */
    protected abstract String getApiKey();

    /**
     * 获取模型提供商
     *
     * @return 模型提供商
     */
    protected abstract String getProvider();

    /**
     * 获取默认最大输出 Token
     *
     * @return 最大输出 Token
     */
    protected Integer getDefaultMaxTokens()
    {
        return null;
    }

    @Override
    public ChatResponse chat(ChatRequest request)
    {
        long start = System.currentTimeMillis();
        ChatResponse response = null;
        try
        {
            validateConfig();
            Map<String, Object> requestBody = buildRequestBody(request);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(requestBody, buildHeaders());
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(buildChatCompletionsUrl(), entity, Map.class);
            response = parseResponse(responseEntity.getBody());
        }
        catch (org.springframework.web.client.HttpServerErrorException e)
        {
            // 5xx 服务端错误（设计 7.4 兜底条件）
            response = ChatResponse.fail("模型服务端错误(" + e.getStatusCode() + ")");
        }
        catch (org.springframework.web.client.HttpClientErrorException e)
        {
            // 429 限流 / 其它 4xx（设计 7.4 兜底条件）
            boolean rateLimited = e.getStatusCode().value() == 429;
            response = ChatResponse.fail(rateLimited ? "模型限流(429)" : "模型客户端错误(" + e.getStatusCode() + ")");
        }
        catch (org.springframework.web.client.ResourceAccessException e)
        {
            // 连接/读取超时（设计 7.4 兜底条件）
            response = ChatResponse.fail("模型调用超时或网络异常: " + e.getMessage());
        }
        catch (Exception e)
        {
            response = ChatResponse.fail("模型调用异常: " + e.getMessage());
        }
        finally
        {
            if (response != null)
            {
                response.setCostTime(System.currentTimeMillis() - start);
            }
        }
        return response;
    }

    /**
     * 构建 messages 数组
     *
     * @param request 对话请求
     * @return OpenAI 兼容消息数组
     */
    protected List<Map<String, String>> buildMessages(ChatRequest request)
    {
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        if (request != null && StringUtils.isNotEmpty(request.getHistory()))
        {
            for (ChatMessageVo item : request.getHistory())
            {
                if (item != null && StringUtils.isNotEmpty(item.getRole()) && StringUtils.isNotEmpty(item.getContent()))
                {
                    messages.add(buildMessage(item.getRole(), item.getContent()));
                }
            }
        }
        if (request != null && StringUtils.isNotEmpty(request.getContent()))
        {
            messages.add(buildMessage("user", request.getContent()));
        }
        return messages;
    }

    /**
     * 构建请求体
     *
     * @param request 对话请求
     * @return 请求体
     */
    protected Map<String, Object> buildRequestBody(ChatRequest request)
    {
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("model", getModelName());
        requestBody.put("messages", buildMessages(request));
        requestBody.put("temperature", request != null && request.getTemperature() != null
                ? request.getTemperature() : DEFAULT_TEMPERATURE);
        Integer maxTokens = request != null && request.getMaxTokens() != null ? request.getMaxTokens() : getDefaultMaxTokens();
        if (maxTokens != null)
        {
            requestBody.put("max_tokens", maxTokens);
        }
        return requestBody;
    }

    /**
     * 构建请求头
     *
     * @return 请求头
     */
    protected HttpHeaders buildHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getApiKey());
        return headers;
    }

    /**
     * 构建单条消息
     *
     * @param role 角色
     * @param content 内容
     * @return 消息
     */
    protected Map<String, String> buildMessage(String role, String content)
    {
        Map<String, String> message = new HashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 解析 OpenAI 兼容响应
     *
     * @param responseBody 响应体
     * @return 对话响应
     */
    @SuppressWarnings("unchecked")
    protected ChatResponse parseResponse(Map responseBody)
    {
        if (responseBody == null)
        {
            return ChatResponse.fail("模型返回空响应");
        }
        Object choicesObj = responseBody.get("choices");
        if (!(choicesObj instanceof List) || ((List<?>) choicesObj).isEmpty())
        {
            return ChatResponse.fail("模型响应缺少 choices");
        }
        Object choiceObj = ((List<?>) choicesObj).get(0);
        if (!(choiceObj instanceof Map))
        {
            return ChatResponse.fail("模型响应 choices 格式错误");
        }
        Object messageObj = ((Map<?, ?>) choiceObj).get("message");
        if (!(messageObj instanceof Map))
        {
            return ChatResponse.fail("模型响应缺少 message");
        }
        Object contentObj = ((Map<?, ?>) messageObj).get("content");
        String content = contentObj == null ? null : String.valueOf(contentObj);
        if (StringUtils.isEmpty(content))
        {
            return ChatResponse.fail("模型返回空内容");
        }

        Map<String, Object> usage = responseBody.get("usage") instanceof Map
                ? (Map<String, Object>) responseBody.get("usage") : null;
        return ChatResponse.ok(content, getModelName(), getProvider(), getIntegerValue(usage, "prompt_tokens"),
                getIntegerValue(usage, "completion_tokens"), getIntegerValue(usage, "total_tokens"));
    }

    /**
     * 获取整型值
     *
     * @param map Map 数据
     * @param key 键
     * @return 整型值
     */
    protected Integer getIntegerValue(Map<String, Object> map, String key)
    {
        if (map == null || map.get(key) == null)
        {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * 构建 chat/completions 地址
     *
     * @return 请求地址
     */
    protected String buildChatCompletionsUrl()
    {
        String baseUrl = getBaseUrl();
        if (baseUrl.endsWith(CHAT_COMPLETIONS_PATH))
        {
            return baseUrl;
        }
        while (baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + CHAT_COMPLETIONS_PATH;
    }

    /**
     * 校验模型配置
     */
    protected void validateConfig()
    {
        if (StringUtils.isEmpty(getModelName()))
        {
            throw new IllegalStateException("模型名称不能为空");
        }
        if (StringUtils.isEmpty(getBaseUrl()))
        {
            throw new IllegalStateException("模型 API 地址不能为空");
        }
        if (StringUtils.isEmpty(getApiKey()))
        {
            throw new IllegalStateException("模型 API Key 不能为空");
        }
    }
}
