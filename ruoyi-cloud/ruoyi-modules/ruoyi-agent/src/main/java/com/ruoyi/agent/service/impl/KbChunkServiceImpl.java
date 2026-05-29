package com.ruoyi.agent.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.core.EmbeddingClient;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.domain.KbChunkVector;
import com.ruoyi.agent.mapper.KbChunkMapper;
import com.ruoyi.agent.mapper.KbChunkVectorMapper;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 知识片段 业务层处理
 *
 * @author ruoyi
 */
@Service
public class KbChunkServiceImpl implements IKbChunkService
{
    private static final Logger log = LoggerFactory.getLogger(KbChunkServiceImpl.class);

    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    /** 余弦距离阈值（pgvector &lt;=&gt;，0=完全相同~2=相反）。超过则视为低相关被过滤。可按模型调优 */
    private static final double MAX_COSINE_DISTANCE = 0.8D;

    @Autowired
    private KbChunkMapper kbChunkMapper;

    @Autowired
    private KbChunkVectorMapper kbChunkVectorMapper;

    @Autowired
    private EmbeddingClient embeddingClient;

    @Override
    public KbChunk save(KbChunk chunk)
    {
        if (chunk == null)
        {
            return null;
        }
        Date now = new Date();
        if (chunk.getId() == null)
        {
            if (chunk.getChunkVersion() == null)
            {
                chunk.setChunkVersion(1);
            }
            chunk.setCreateTime(now);
            chunk.setUpdateTime(now);
            chunk.setDeleted(NOT_DELETED);
            kbChunkMapper.insert(chunk);
        }
        else
        {
            chunk.setUpdateTime(now);
            kbChunkMapper.updateById(chunk);
        }
        return chunk;
    }

    @Override
    public List<KbChunk> listByDocId(Long documentId)
    {
        LambdaQueryWrapper<KbChunk> queryWrapper = new LambdaQueryWrapper<KbChunk>()
                .eq(KbChunk::getDocumentId, documentId)
                .eq(KbChunk::getDeleted, NOT_DELETED)
                .orderByAsc(KbChunk::getChunkIndex);
        return kbChunkMapper.selectList(queryWrapper);
    }

    @Override
    public List<KbChunk> searchByKeyword(String keyword, int limit)
    {
        if (StringUtils.isEmpty(keyword))
        {
            return Collections.emptyList();
        }

        float[] queryVector = embeddingClient.embed(keyword);
        int topK = Math.max(1, limit);
        if (queryVector == null || queryVector.length == 0)
        {
            log.warn("Embedding query failed, fallback to LIKE search keyword={}", keyword);
            return fallbackLikeSearch(keyword, topK);
        }

        try
        {
            List<KbChunkVector> vectorResults = kbChunkVectorMapper.searchByVector(formatVector(queryVector), topK,
                    MAX_COSINE_DISTANCE);
            if (vectorResults == null || vectorResults.isEmpty())
            {
                log.info("向量检索无满足阈值({})的片段，回退 LIKE，keyword={}", MAX_COSINE_DISTANCE, keyword);
                return fallbackLikeSearch(keyword, topK);
            }

            List<Long> chunkIds = vectorResults.stream()
                    .map(KbChunkVector::getChunkId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<KbChunk> queryWrapper = new LambdaQueryWrapper<KbChunk>()
                    .in(KbChunk::getId, chunkIds)
                    .eq(KbChunk::getDeleted, NOT_DELETED);
            List<KbChunk> chunks = kbChunkMapper.selectList(queryWrapper);

            List<KbChunk> orderedResult = new ArrayList<KbChunk>(chunkIds.size());
            for (Long chunkId : chunkIds)
            {
                for (KbChunk chunk : chunks)
                {
                    if (chunk.getId().equals(chunkId))
                    {
                        orderedResult.add(chunk);
                        break;
                    }
                }
            }
            return orderedResult;
        }
        catch (Exception e)
        {
            log.error("pgvector search failed, fallback to LIKE search", e);
            return fallbackLikeSearch(keyword, topK);
        }
    }

    @Override
    public boolean deleteByDocId(Long documentId)
    {
        if (documentId == null)
        {
            return false;
        }
        List<KbChunk> chunks = kbChunkMapper.selectList(new LambdaQueryWrapper<KbChunk>()
                .eq(KbChunk::getDocumentId, documentId)
                .eq(KbChunk::getDeleted, NOT_DELETED));
        LambdaUpdateWrapper<KbChunk> updateWrapper = new LambdaUpdateWrapper<KbChunk>()
                .set(KbChunk::getDeleted, DELETED)
                .set(KbChunk::getUpdateTime, new Date())
                .eq(KbChunk::getDocumentId, documentId)
                .eq(KbChunk::getDeleted, NOT_DELETED);
        boolean result = kbChunkMapper.update(null, updateWrapper) >= 0;
        if (chunks != null && !chunks.isEmpty())
        {
            List<Long> chunkIds = chunks.stream().map(KbChunk::getId).collect(Collectors.toList());
            try
            {
                kbChunkVectorMapper.markDeletedByChunkIds(chunkIds);
            }
            catch (Exception e)
            {
                log.warn("Mark pgvector deleted failed, documentId={}", documentId, e);
            }
        }
        return result;
    }

    @Override
    public KbChunk getByDocId(Long documentId, Integer chunkIndex)
    {
        if (documentId == null || chunkIndex == null)
        {
            return null;
        }
        LambdaQueryWrapper<KbChunk> queryWrapper = new LambdaQueryWrapper<KbChunk>()
                .eq(KbChunk::getDocumentId, documentId)
                .eq(KbChunk::getChunkIndex, chunkIndex)
                .eq(KbChunk::getDeleted, NOT_DELETED)
                .last("limit 1");
        return kbChunkMapper.selectOne(queryWrapper);
    }

    private List<KbChunk> fallbackLikeSearch(String keyword, int limit)
    {
        LambdaQueryWrapper<KbChunk> queryWrapper = new LambdaQueryWrapper<KbChunk>()
                .like(KbChunk::getContent, keyword)
                .eq(KbChunk::getDeleted, NOT_DELETED)
                .orderByDesc(KbChunk::getCreateTime)
                .last("limit " + Math.max(1, limit));
        return kbChunkMapper.selectList(queryWrapper);
    }

    private String formatVector(float[] vector)
    {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
