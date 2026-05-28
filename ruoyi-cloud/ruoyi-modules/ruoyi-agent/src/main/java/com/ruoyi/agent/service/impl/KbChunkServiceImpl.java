package com.ruoyi.agent.service.impl;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.mapper.KbChunkMapper;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.common.core.utils.StringUtils;
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
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    @Autowired
    private KbChunkMapper kbChunkMapper;

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
        LambdaQueryWrapper<KbChunk> queryWrapper = new LambdaQueryWrapper<KbChunk>()
                .like(StringUtils.isNotEmpty(keyword), KbChunk::getContent, keyword)
                .eq(KbChunk::getDeleted, NOT_DELETED)
                .orderByDesc(KbChunk::getCreateTime)
                .last("limit " + Math.max(1, limit));
        return kbChunkMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteByDocId(Long documentId)
    {
        if (documentId == null)
        {
            return false;
        }
        LambdaUpdateWrapper<KbChunk> updateWrapper = new LambdaUpdateWrapper<KbChunk>()
                .set(KbChunk::getDeleted, DELETED)
                .set(KbChunk::getUpdateTime, new Date())
                .eq(KbChunk::getDocumentId, documentId)
                .eq(KbChunk::getDeleted, NOT_DELETED);
        return kbChunkMapper.update(null, updateWrapper) >= 0;
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
}
