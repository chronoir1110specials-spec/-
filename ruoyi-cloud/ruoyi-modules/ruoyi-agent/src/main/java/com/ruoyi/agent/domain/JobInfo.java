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
 * 岗位信息对象 job_info
 *
 * @author ruoyi
 */
@TableName("job_info")
public class JobInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 岗位名称 */
    private String jobName;

    /** 公司名称 */
    private String companyName;

    /** 岗位 JD */
    private String jobDescription;

    /** 分析结果 */
    private String analysisResult;

    /** 匹配评分 */
    private Integer matchScore;

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

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getJobDescription()
    {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription)
    {
        this.jobDescription = jobDescription;
    }

    public String getAnalysisResult()
    {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult)
    {
        this.analysisResult = analysisResult;
    }

    public Integer getMatchScore()
    {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore)
    {
        this.matchScore = matchScore;
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
                .append("userId", getUserId())
                .append("jobName", getJobName())
                .append("companyName", getCompanyName())
                .append("jobDescription", getJobDescription())
                .append("analysisResult", getAnalysisResult())
                .append("matchScore", getMatchScore())
                .append("createTime", getCreateTime())
                .append("deleted", getDeleted())
                .toString();
    }
}
