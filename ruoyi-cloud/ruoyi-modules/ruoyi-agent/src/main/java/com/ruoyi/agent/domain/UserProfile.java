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
 * 用户画像对象 user_profile
 *
 * @author ruoyi
 */
@TableName("user_profile")
public class UserProfile implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 学校 */
    private String school;

    /** 专业 */
    private String major;

    /** 年级 */
    private String grade;

    /** 目标岗位 */
    private String targetPosition;

    /** 目标城市 */
    private String targetCity;

    /** 技能标签 */
    private String skillTags;

    /** 项目标签 */
    private String projectTags;

    /** 求职阶段 */
    private String jobStage;

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

    public String getSchool()
    {
        return school;
    }

    public void setSchool(String school)
    {
        this.school = school;
    }

    public String getMajor()
    {
        return major;
    }

    public void setMajor(String major)
    {
        this.major = major;
    }

    public String getGrade()
    {
        return grade;
    }

    public void setGrade(String grade)
    {
        this.grade = grade;
    }

    public String getTargetPosition()
    {
        return targetPosition;
    }

    public void setTargetPosition(String targetPosition)
    {
        this.targetPosition = targetPosition;
    }

    public String getTargetCity()
    {
        return targetCity;
    }

    public void setTargetCity(String targetCity)
    {
        this.targetCity = targetCity;
    }

    public String getSkillTags()
    {
        return skillTags;
    }

    public void setSkillTags(String skillTags)
    {
        this.skillTags = skillTags;
    }

    public String getProjectTags()
    {
        return projectTags;
    }

    public void setProjectTags(String projectTags)
    {
        this.projectTags = projectTags;
    }

    public String getJobStage()
    {
        return jobStage;
    }

    public void setJobStage(String jobStage)
    {
        this.jobStage = jobStage;
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
                .append("school", getSchool())
                .append("major", getMajor())
                .append("grade", getGrade())
                .append("targetPosition", getTargetPosition())
                .append("targetCity", getTargetCity())
                .append("skillTags", getSkillTags())
                .append("projectTags", getProjectTags())
                .append("jobStage", getJobStage())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("deleted", getDeleted())
                .toString();
    }
}
