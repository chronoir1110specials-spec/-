package com.ruoyi.model.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 对话请求对象
 *
 * @author ruoyi
 */
public class ChatRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 会话 ID */
    private String sessionId;

    /** 用户 ID */
    private Long userId;

    /** 用户消息 */
    private String content;

    /** 对话历史 */
    private List<ChatMessageVo> history;

    /** 温度 */
    private Double temperature;

    /** 最大输出 Token */
    private Integer maxTokens;

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public List<ChatMessageVo> getHistory()
    {
        return history;
    }

    public void setHistory(List<ChatMessageVo> history)
    {
        this.history = history;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Double temperature)
    {
        this.temperature = temperature;
    }

    public Integer getMaxTokens()
    {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens)
    {
        this.maxTokens = maxTokens;
    }

    /**
     * 对话消息
     */
    public static class ChatMessageVo implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /** 角色：user / assistant / system */
        private String role;

        /** 消息内容 */
        private String content;

        public String getRole()
        {
            return role;
        }

        public void setRole(String role)
        {
            this.role = role;
        }

        public String getContent()
        {
            return content;
        }

        public void setContent(String content)
        {
            this.content = content;
        }
    }
}
