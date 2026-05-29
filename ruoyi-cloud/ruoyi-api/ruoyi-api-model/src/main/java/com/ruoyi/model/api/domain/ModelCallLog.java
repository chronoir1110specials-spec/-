package com.ruoyi.model.api.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 模型调用日志对象（API 层纯 POJO，无 MyBatis-Plus 注解）
 *
 * @author ruoyi
 */
public class ModelCallLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 会话 ID */
    private Long sessionId;

    /** 模型提供商 */
    private String provider;

    /** 模型名称 */
    private String modelName;

    /** 是否兜底调用 */
    private Integer isFallback;

    /** 输入 Token */
    private Integer promptTokens;

    /** 输出 Token */
    private Integer completionTokens;

    /** 总 Token */
    private Integer totalTokens;

    /** 耗时，毫秒 */
    private Integer costTime;

    /** 调用状态 */
    private String status;

    /** 错误信息 */
    private String errorMessage;

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

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
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

    public Integer getIsFallback()
    {
        return isFallback;
    }

    public void setIsFallback(Integer isFallback)
    {
        this.isFallback = isFallback;
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

    public Integer getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Integer costTime)
    {
        this.costTime = costTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
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
}
