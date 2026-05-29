package com.ruoyi.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 知识库切片向量对象 kb_chunk_vector
 *
 * @author ruoyi
 */
@TableName("kb_chunk_vector")
public class KbChunkVector
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chunkId;

    private String embeddingVector;

    private String embeddingModel;

    private Integer embeddingDimension;

    private String vectorStatus;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getChunkId()
    {
        return chunkId;
    }

    public void setChunkId(Long chunkId)
    {
        this.chunkId = chunkId;
    }

    public String getEmbeddingVector()
    {
        return embeddingVector;
    }

    public void setEmbeddingVector(String embeddingVector)
    {
        this.embeddingVector = embeddingVector;
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

    public String getVectorStatus()
    {
        return vectorStatus;
    }

    public void setVectorStatus(String vectorStatus)
    {
        this.vectorStatus = vectorStatus;
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
}
