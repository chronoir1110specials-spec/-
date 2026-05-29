package com.ruoyi.agent.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.domain.ModelConfig;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatRequest.ChatMessageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 流式对话客户端。
 *
 * <p>复用 {@link EmbeddingClient} 的既有模式：从模型服务拉取主模型配置（含真实 Key），
 * 直接以 stream=true 调用 OpenAI 兼容的 /chat/completions，逐块回调 token。</p>
 *
 * @author ruoyi
 */
@Service
public class StreamingChatClient
{
    private static final Logger log = LoggerFactory.getLogger(StreamingChatClient.class);

    private static final String MODEL_ROLE_PRIMARY = "primary";

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final String DATA_PREFIX = "data:";

    private static final String DONE_MARKER = "[DONE]";

    private static final Double DEFAULT_TEMPERATURE = 0.7D;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RemoteModelService remoteModelService;

    /**
     * 拉取主模型配置。
     *
     * @return 主模型配置，未配置返回 null
     */
    public ModelConfig getPrimaryConfig()
    {
        R<ModelConfig> r = remoteModelService.getConfigByRole(MODEL_ROLE_PRIMARY, SecurityConstants.INNER);
        return r == null ? null : r.getData();
    }

    /**
     * 以流式方式调用主模型。
     *
     * @param config 主模型配置
     * @param request 对话请求（含 history 与 content）
     * @param onDelta 收到增量内容时的回调
     * @return 流式结果（累计内容、用量、是否成功）
     */
    public StreamResult streamChat(ModelConfig config, ChatRequest request, Consumer<String> onDelta)
    {
        StreamResult result = new StreamResult();
        long start = System.currentTimeMillis();
        try
        {
            byte[] body = objectMapper.writeValueAsBytes(buildRequestBody(config, request));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(buildUrl(config.getBaseUrl())))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                result.setErrorMessage("模型返回状态码 " + response.statusCode());
                return result;
            }
            consumeStream(response.body(), result, onDelta);
            result.setSuccess(StringUtils.isNotEmpty(result.getContent().toString()));
        }
        catch (Exception e)
        {
            log.warn("Streaming chat failed", e);
            result.setErrorMessage(e.getMessage());
        }
        finally
        {
            result.setCostTime(System.currentTimeMillis() - start);
        }
        return result;
    }

    private void consumeStream(java.io.InputStream in, StreamResult result, Consumer<String> onDelta)
            throws java.io.IOException
    {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(in, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (StringUtils.isEmpty(line) || !line.startsWith(DATA_PREFIX))
                {
                    continue;
                }
                String payload = line.substring(DATA_PREFIX.length()).trim();
                if (DONE_MARKER.equals(payload))
                {
                    break;
                }
                parseChunk(payload, result, onDelta);
            }
        }
    }

    private void parseChunk(String payload, StreamResult result, Consumer<String> onDelta)
    {
        try
        {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode choices = node.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0)
            {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.hasNonNull("content"))
                {
                    String piece = delta.get("content").asText();
                    if (StringUtils.isNotEmpty(piece))
                    {
                        result.getContent().append(piece);
                        onDelta.accept(piece);
                    }
                }
            }
            JsonNode usage = node.get("usage");
            if (usage != null && !usage.isNull())
            {
                if (usage.hasNonNull("prompt_tokens"))
                {
                    result.setPromptTokens(usage.get("prompt_tokens").asInt());
                }
                if (usage.hasNonNull("completion_tokens"))
                {
                    result.setCompletionTokens(usage.get("completion_tokens").asInt());
                }
                if (usage.hasNonNull("total_tokens"))
                {
                    result.setTotalTokens(usage.get("total_tokens").asInt());
                }
            }
        }
        catch (Exception e)
        {
            log.debug("Skip non-JSON SSE chunk: {}", payload);
        }
    }

    private Map<String, Object> buildRequestBody(ModelConfig config, ChatRequest request)
    {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("model", config.getModelName());
        body.put("messages", buildMessages(request));
        body.put("temperature", request != null && request.getTemperature() != null
                ? request.getTemperature() : DEFAULT_TEMPERATURE);
        body.put("stream", Boolean.TRUE);
        body.put("stream_options", java.util.Collections.singletonMap("include_usage", Boolean.TRUE));
        if (request != null && request.getMaxTokens() != null)
        {
            body.put("max_tokens", request.getMaxTokens());
        }
        return body;
    }

    private List<Map<String, String>> buildMessages(ChatRequest request)
    {
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        if (request != null && request.getHistory() != null)
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

    private Map<String, String> buildMessage(String role, String content)
    {
        Map<String, String> message = new HashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildUrl(String baseUrl)
    {
        if (baseUrl.endsWith(CHAT_COMPLETIONS_PATH))
        {
            return baseUrl;
        }
        String trimmed = baseUrl;
        while (trimmed.endsWith("/"))
        {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + CHAT_COMPLETIONS_PATH;
    }

    /**
     * 流式调用结果。
     */
    public static class StreamResult
    {
        private final StringBuilder content = new StringBuilder();

        private boolean success;

        private String errorMessage;

        private Integer promptTokens;

        private Integer completionTokens;

        private Integer totalTokens;

        private long costTime;

        public StringBuilder getContent()
        {
            return content;
        }

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage)
        {
            this.errorMessage = errorMessage;
        }

        public Integer getPromptTokens()
        {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens)
        {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens()
        {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens)
        {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens()
        {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens)
        {
            this.totalTokens = totalTokens;
        }

        public long getCostTime()
        {
            return costTime;
        }

        public void setCostTime(long costTime)
        {
            this.costTime = costTime;
        }
    }
}
