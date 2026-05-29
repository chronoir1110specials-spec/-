package com.ruoyi.model.api.dto;

import java.io.Serializable;

/**
 * 模型连通性测试结果（Feign 传输用）
 *
 * @author ruoyi
 */
public class ModelTestResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 模型角色：primary / fallback */
    private String modelRole;

    /** 是否连通成功 */
    private boolean reachable;

    /** 配置是否就绪 */
    private boolean configured;

    /** 提供商 */
    private String provider;

    /** 模型名称 */
    private String modelName;

    /** 探活回复内容 */
    private String reply;

    /** 耗时 ms */
    private Long costTime;

    /** 总 Token */
    private Integer totalTokens;

    /** 失败原因 */
    private String errorMessage;

    public String getModelRole()
    {
        return modelRole;
    }

    public void setModelRole(String modelRole)
    {
        this.modelRole = modelRole;
    }

    public boolean isReachable()
    {
        return reachable;
    }

    public void setReachable(boolean reachable)
    {
        this.reachable = reachable;
    }

    public boolean isConfigured()
    {
        return configured;
    }

    public void setConfigured(boolean configured)
    {
        this.configured = configured;
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

    public String getReply()
    {
        return reply;
    }

    public void setReply(String reply)
    {
        this.reply = reply;
    }

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }

    public Integer getTotalTokens()
    {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens)
    {
        this.totalTokens = totalTokens;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
