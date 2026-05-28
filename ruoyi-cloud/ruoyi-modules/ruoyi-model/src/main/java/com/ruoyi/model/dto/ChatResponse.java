package com.ruoyi.model.dto;

import java.io.Serializable;

/**
 * 对话响应对象
 *
 * @author ruoyi
 */
public class ChatResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    private boolean success;

    /** 回复内容 */
    private String content;

    /** 使用的模型 */
    private String modelName;

    /** 提供商 */
    private String provider;

    /** 是否兜底 */
    private boolean fallback;

    /** 输入 Token */
    private Integer promptTokens;

    /** 输出 Token */
    private Integer completionTokens;

    /** 总 Token */
    private Integer totalTokens;

    /** 耗时 ms */
    private Long costTime;

    /** 错误信息 */
    private String errorMessage;

    public static ChatResponse ok(String content, String modelName, String provider, Integer promptTokens,
            Integer completionTokens, Integer totalTokens)
    {
        ChatResponse response = new ChatResponse();
        response.setSuccess(true);
        response.setContent(content);
        response.setModelName(modelName);
        response.setProvider(provider);
        response.setPromptTokens(promptTokens);
        response.setCompletionTokens(completionTokens);
        response.setTotalTokens(totalTokens);
        return response;
    }

    public static ChatResponse fail(String errorMessage)
    {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public static ChatResponse fallback(String content, String modelName, String provider, Integer promptTokens,
            Integer completionTokens, Integer totalTokens)
    {
        ChatResponse response = ok(content, modelName, provider, promptTokens, completionTokens, totalTokens);
        response.setFallback(true);
        return response;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public boolean isFallback()
    {
        return fallback;
    }

    public void setFallback(boolean fallback)
    {
        this.fallback = fallback;
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

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
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
