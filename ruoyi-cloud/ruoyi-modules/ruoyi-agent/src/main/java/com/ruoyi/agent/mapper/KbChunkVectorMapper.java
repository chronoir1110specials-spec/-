package com.ruoyi.agent.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.agent.domain.KbChunkVector;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 知识库切片向量Mapper接口（PostgreSQL + pgvector）
 *
 * @author ruoyi
 */
@DS("vector")
public interface KbChunkVectorMapper extends BaseMapper<KbChunkVector>
{
    /**
     * 向量相似度检索（带余弦距离阈值过滤）
     *
     * @param queryVector 查询向量（pgvector 文本字面量，形如 [0.1,0.2,...]）
     * @param topK 返回数量
     * @param maxDistance 余弦距离上限（&lt;=&gt; 越小越相似，超过该值的低相关片段被过滤）
     * @return 向量记录列表
     */
    List<KbChunkVector> searchByVector(@Param("queryVector") String queryVector, @Param("topK") int topK,
            @Param("maxDistance") double maxDistance);

    /**
     * 写入向量记录（embedding_vector 以文本字面量 CAST 为 pgvector 类型）
     *
     * @param record 向量记录
     * @return 影响行数
     */
    int insertVector(KbChunkVector record);

    /**
     * 按 chunkId 逻辑删除向量记录
     *
     * @param chunkIds chunkId 列表
     * @return 影响行数
     */
    int markDeletedByChunkIds(@Param("chunkIds") List<Long> chunkIds);
}
