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
 * 知识片段对象 kb_chunk
 *
 * @author ruoyi
 */
@TableName("kb_chunk")
public class KbChunk implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档 ID */
    private Long documentId;

    /** 片段序号 */
    private Integer chunkIndex;

    /** 文本片段 */
    private String content;

    /** 片段内容 hash */
    private String contentHash;

    /** 向量 ID */
    private String vectorId;

    /** 使用的 Embedding 模型 */
    private String embeddingModel;

    /** 向量维度 */
    private Integer embeddingDimension;

    /** 切片版本 */
    private Integer chunkVersion;

    /** 片段 Token 数 */
    private Integer tokenCount;

    /** 向量化状态 */
    private String vectorStatus;

    /** 向量值（JSON 数组字符串） */
    private String embeddingVector;

    /** 元数据 */
    private String metadata;

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

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public Integer getChunkIndex()
    {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex)
    {
        this.chunkIndex = chunkIndex;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContentHash()
    {
        return contentHash;
    }

    public void setContentHash(String contentHash)
    {
        this.contentHash = contentHash;
    }

    public String getVectorId()
    {
        return vectorId;
    }

    public void setVectorId(String vectorId)
    {
        this.vectorId = vectorId;
    }

    public String getEmbeddingModel()
    {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    public Integer getEmbeddingDimension()
    {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(Integer embeddingDimension)
    {
        this.embeddingDimension = embeddingDimension;
    }

    public Integer getChunkVersion()
    {
        return chunkVersion;
    }

    public void setChunkVersion(Integer chunkVersion)
    {
        this.chunkVersion = chunkVersion;
    }

    public Integer getTokenCount()
    {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount)
    {
        this.tokenCount = tokenCount;
    }

    public String getVectorStatus()
    {
        return vectorStatus;
    }

    public void setVectorStatus(String vectorStatus)
    {
        this.vectorStatus = vectorStatus;
    }

    public String getEmbeddingVector()
    {
        return embeddingVector;
    }

    public void setEmbeddingVector(String embeddingVector)
    {
        this.embeddingVector = embeddingVector;
    }

    public String getMetadata()
    {
        return metadata;
    }

    public void setMetadata(String metadata)
    {
        this.metadata = metadata;
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
                .append("documentId", getDocumentId())
                .append("chunkIndex", getChunkIndex())
                .append("content", getContent())
                .append("contentHash", getContentHash())
                .append("vectorId", getVectorId())
                .append("embeddingModel", getEmbeddingModel())
                .append("embeddingDimension", getEmbeddingDimension())
                .append("chunkVersion", getChunkVersion())
                .append("tokenCount", getTokenCount())
                .append("vectorStatus", getVectorStatus())
                .append("metadata", getMetadata())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("deleted", getDeleted())
                .toString();
    }
}
