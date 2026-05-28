package com.ruoyi.agent.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 会话对象 chat_session
 *
 * @author ruoyi
 */
@TableName("chat_session")
public class ChatSession implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 会话标题 */
    private String title;

    /** Agent 类型 */
    private String agentType;

    /** 会话历史摘要，用于上下文压缩 */
    private String contextSummary;

    /** 上次摘要覆盖到的消息 ID */
    private Long lastSummaryMessageId;

    /** 摘要版本 */
    private Integer summaryVersion;

    /** 最近消息保留数量 */
    private Integer recentMessageLimit;

    /** 会话上下文最大 Token 预算 */
    private Integer maxContextTokens;

    /** 模拟面试状态 */
    private String interviewStatus;

    /** 当前面试题序号 */
    private Integer currentQuestionIndex;

    /** 面试总题数 */
    private Integer totalQuestions;

    /** 面试岗位方向 */
    private String interviewPosition;

    /** 面试难度 */
    private String interviewDifficulty;

    /** 面试综合评分 */
    private Integer interviewScore;

    /** 面试总结 */
    private String interviewSummary;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getAgentType()
    {
        return agentType;
    }

    public void setAgentType(String agentType)
    {
        this.agentType = agentType;
    }

    public String getContextSummary()
    {
        return contextSummary;
    }

    public void setContextSummary(String contextSummary)
    {
        this.contextSummary = contextSummary;
    }

    public Long getLastSummaryMessageId()
    {
        return lastSummaryMessageId;
    }

    public void setLastSummaryMessageId(Long lastSummaryMessageId)
    {
        this.lastSummaryMessageId = lastSummaryMessageId;
    }

    public Integer getSummaryVersion()
    {
        return summaryVersion;
    }

    public void setSummaryVersion(Integer summaryVersion)
    {
        this.summaryVersion = summaryVersion;
    }

    public Integer getRecentMessageLimit()
    {
        return recentMessageLimit;
    }

    public void setRecentMessageLimit(Integer recentMessageLimit)
    {
        this.recentMessageLimit = recentMessageLimit;
    }

    public Integer getMaxContextTokens()
    {
        return maxContextTokens;
    }

    public void setMaxContextTokens(Integer maxContextTokens)
    {
        this.maxContextTokens = maxContextTokens;
    }

    public String getInterviewStatus()
    {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus)
    {
        this.interviewStatus = interviewStatus;
    }

    public Integer getCurrentQuestionIndex()
    {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(Integer currentQuestionIndex)
    {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public Integer getTotalQuestions()
    {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions)
    {
        this.totalQuestions = totalQuestions;
    }

    public String getInterviewPosition()
    {
        return interviewPosition;
    }

    public void setInterviewPosition(String interviewPosition)
    {
        this.interviewPosition = interviewPosition;
    }

    public String getInterviewDifficulty()
    {
        return interviewDifficulty;
    }

    public void setInterviewDifficulty(String interviewDifficulty)
    {
        this.interviewDifficulty = interviewDifficulty;
    }

    public Integer getInterviewScore()
    {
        return interviewScore;
    }

    public void setInterviewScore(Integer interviewScore)
    {
        this.interviewScore = interviewScore;
    }

    public String getInterviewSummary()
    {
        return interviewSummary;
    }

    public void setInterviewSummary(String interviewSummary)
    {
        this.interviewSummary = interviewSummary;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
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
                .append("userId", getUserId())
                .append("title", getTitle())
                .append("agentType", getAgentType())
                .append("contextSummary", getContextSummary())
                .append("lastSummaryMessageId", getLastSummaryMessageId())
                .append("summaryVersion", getSummaryVersion())
                .append("recentMessageLimit", getRecentMessageLimit())
                .append("maxContextTokens", getMaxContextTokens())
                .append("interviewStatus", getInterviewStatus())
                .append("currentQuestionIndex", getCurrentQuestionIndex())
                .append("totalQuestions", getTotalQuestions())
                .append("interviewPosition", getInterviewPosition())
                .append("interviewDifficulty", getInterviewDifficulty())
                .append("interviewScore", getInterviewScore())
                .append("interviewSummary", getInterviewSummary())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("deleted", getDeleted())
                .toString();
    }
}
