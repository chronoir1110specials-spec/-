package com.ruoyi.agent.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.domain.ModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Embedding 模型客户端。
 *
 * @author ruoyi
 */
@Service
public class EmbeddingClient
{
    private static final Logger log = LoggerFactory.getLogger(EmbeddingClient.class);

    private static final String MODEL_ROLE_EMBEDDING = "embedding";

    private static final String EMBEDDINGS_PATH = "/embeddings";

    @Autowired
    private RemoteModelService remoteModelService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 返回当前配置的 embedding 模型名（用于记录到 kb_chunk.embedding_model，
     * 供判断是否需要重新向量化）。未配置返回 null。
     */
    public String getModelName()
    {
        R<ModelConfig> r = remoteModelService.getConfigByRole(MODEL_ROLE_EMBEDDING, SecurityConstants.INNER);
        ModelConfig config = r == null ? null : r.getData();
        return config == null ? null : config.getModelName();
    }

    public float[] embed(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        R<ModelConfig> r = remoteModelService.getConfigByRole(MODEL_ROLE_EMBEDDING, SecurityConstants.INNER);
        ModelConfig config = r == null ? null : r.getData();
        if (config == null || StringUtils.isEmpty(config.getBaseUrl()) || StringUtils.isEmpty(config.getModelName())
                || StringUtils.isEmpty(config.getApiKey()))
        {
            return null;
        }

        try
        {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("model", config.getModelName());
            requestBody.put("input", text);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(requestBody, buildHeaders(config));
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(buildEmbeddingsUrl(config.getBaseUrl()), entity,
                    Map.class);
            return parseEmbedding(responseEntity.getBody());
        }
        catch (Exception e)
        {
            log.warn("Embedding request failed", e);
            return null;
        }
    }

    private HttpHeaders buildHeaders(ModelConfig config)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        return headers;
    }

    private String buildEmbeddingsUrl(String baseUrl)
    {
        if (baseUrl.endsWith(EMBEDDINGS_PATH))
        {
            return baseUrl;
        }
        while (baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + EMBEDDINGS_PATH;
    }

    @SuppressWarnings("unchecked")
    private float[] parseEmbedding(Map responseBody)
    {
        if (responseBody == null || !(responseBody.get("data") instanceof List)
                || ((List<?>) responseBody.get("data")).isEmpty())
        {
            return null;
        }
        Object first = ((List<?>) responseBody.get("data")).get(0);
        if (!(first instanceof Map) || !(((Map<?, ?>) first).get("embedding") instanceof List))
        {
            return null;
        }
        List<Object> values = (List<Object>) ((Map<?, ?>) first).get("embedding");
        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++)
        {
            Object value = values.get(i);
            if (value instanceof Number)
            {
                embedding[i] = ((Number) value).floatValue();
            }
            else
            {
                embedding[i] = Float.parseFloat(String.valueOf(value));
            }
        }
        return embedding;
    }
}
