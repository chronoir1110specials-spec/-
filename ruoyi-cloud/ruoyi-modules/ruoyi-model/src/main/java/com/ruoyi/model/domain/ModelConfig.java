package com.ruoyi.model.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 模型配置对象 model_config
 *
 * @author ruoyi
 */
@TableName("model_config")
public class ModelConfig implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** primary / fallback / embedding */
    private String modelRole;

    /** digitalocean / glm */
    private String provider;

    /** 模型名称 */
    private String modelName;

    /** API 地址 */
    private String baseUrl;

    /** API Key，加密存储 */
    private String apiKey;

    /** 是否启用 */
    private Integer enabled;

    /** 最大输出长度 */
    private Integer maxTokens;

    /** Embedding 模型向量维度，仅向量模型使用 */
    private Integer embeddingDimension;

    /** 超时时间 */
    private Integer timeout;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 是否删除 */
    private Integer deleted;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getModelRole()
    {
        return modelRole;
    }

    public void setModelRole(String modelRole)
    {
        this.modelRole = modelRole;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
    }

    public Integer getMaxTokens()
    {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens)
    {
        this.maxTokens = maxTokens;
    }

    public Integer getEmbeddingDimension()
    {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(Integer embeddingDimension)
    {
        this.embeddingDimension = embeddingDimension;
    }

    public Integer getTimeout()
    {
        return timeout;
    }

    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Integer getDeleted()
    {
        return deleted;
    }

    public void setDeleted(Integer deleted)
    {
        this.deleted = deleted;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("modelRole", getModelRole())
                .append("provider", getProvider())
                .append("modelName", getModelName())
                .append("baseUrl", getBaseUrl())
                .append("apiKey", getApiKey())
                .append("enabled", getEnabled())
                .append("maxTokens", getMaxTokens())
                .append("embeddingDimension", getEmbeddingDimension())
                .append("timeout", getTimeout())
                .append("createTime", getCreateTime())
                .append("deleted", getDeleted())
                .toString();
    }
}
