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
 * 简历对象 resume_info
 *
 * @author ruoyi
 */
@TableName("resume_info")
public class ResumeInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 简历名称 */
    private String resumeName;

    /** 原始文件名 */
    private String originalFileName;

    /** 文件类型：pdf / docx / doc / text */
    private String fileType;

    /** 原始文件保存路径 */
    private String fileUrl;

    /** 简历文本 hash */
    private String contentHash;

    /** 简历文本内容 */
    private String content;

    /** 解析状态：pending / success / failed */
    private String parseStatus;

    /** 解析失败原因 */
    private String parseError;

    /** 目标岗位 */
    private String targetPosition;

    /** 分析结果 */
    private String analysisResult;

    /** 简历评分 */
    private Integer score;

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

    public String getResumeName()
    {
        return resumeName;
    }

    public void setResumeName(String resumeName)
    {
        this.resumeName = resumeName;
    }

    public String getOriginalFileName()
    {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName)
    {
        this.originalFileName = originalFileName;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getContentHash()
    {
        return contentHash;
    }

    public void setContentHash(String contentHash)
    {
        this.contentHash = contentHash;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getParseStatus()
    {
        return parseStatus;
    }

    public void setParseStatus(String parseStatus)
    {
        this.parseStatus = parseStatus;
    }

    public String getParseError()
    {
        return parseError;
    }

    public void setParseError(String parseError)
    {
        this.parseError = parseError;
    }

    public String getTargetPosition()
    {
        return targetPosition;
    }

    public void setTargetPosition(String targetPosition)
    {
        this.targetPosition = targetPosition;
    }

    public String getAnalysisResult()
    {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult)
    {
        this.analysisResult = analysisResult;
    }

    public Integer getScore()
    {
        return score;
    }

    public void setScore(Integer score)
    {
        this.score = score;
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
                .append("resumeName", getResumeName())
                .append("originalFileName", getOriginalFileName())
                .append("fileType", getFileType())
                .append("fileUrl", getFileUrl())
                .append("contentHash", getContentHash())
                .append("content", getContent())
                .append("parseStatus", getParseStatus())
                .append("parseError", getParseError())
                .append("targetPosition", getTargetPosition())
                .append("analysisResult", getAnalysisResult())
                .append("score", getScore())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("deleted", getDeleted())
                .toString();
    }
}
