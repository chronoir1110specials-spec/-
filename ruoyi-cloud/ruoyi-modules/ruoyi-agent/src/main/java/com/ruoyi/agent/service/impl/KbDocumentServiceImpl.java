package com.ruoyi.agent.service.impl;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.KbDocument;
import com.ruoyi.agent.mapper.KbDocumentMapper;
import com.ruoyi.agent.service.IKbDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 知识库文档 业务层处理
 *
 * @author ruoyi
 */
@Service
public class KbDocumentServiceImpl implements IKbDocumentService
{
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    private static final String STATUS_PENDING = "pending";

    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @Override
    public KbDocument save(KbDocument document)
    {
        if (document == null)
        {
            return null;
        }
        Date now = new Date();
        if (document.getId() == null)
        {
            if (document.getParseStatus() == null)
            {
                document.setParseStatus(STATUS_PENDING);
            }
            if (document.getEmbeddingStatus() == null)
            {
                document.setEmbeddingStatus(STATUS_PENDING);
            }
            if (document.getChunkCount() == null)
            {
                document.setChunkCount(0);
            }
            document.setCreateTime(now);
            document.setDeleted(NOT_DELETED);
            document.setUpdateTime(now);
            kbDocumentMapper.insert(document);
        }
        else
        {
            document.setUpdateTime(now);
            kbDocumentMapper.updateById(document);
        }
        return document;
    }

    @Override
    public KbDocument getById(Long id)
    {
        if (id == null)
        {
            return null;
        }
        LambdaQueryWrapper<KbDocument> queryWrapper = new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getId, id)
                .eq(KbDocument::getDeleted, NOT_DELETED)
                .last("limit 1");
        return kbDocumentMapper.selectOne(queryWrapper);
    }

    @Override
    public List<KbDocument> listAll()
    {
        LambdaQueryWrapper<KbDocument> queryWrapper = new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getDeleted, NOT_DELETED)
                .orderByDesc(KbDocument::getCreateTime);
        return kbDocumentMapper.selectList(queryWrapper);
    }

    @Override
    public boolean delete(Long id)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<KbDocument> updateWrapper = new LambdaUpdateWrapper<KbDocument>()
                .set(KbDocument::getDeleted, DELETED)
                .set(KbDocument::getUpdateTime, new Date())
                .eq(KbDocument::getId, id)
                .eq(KbDocument::getDeleted, NOT_DELETED);
        return kbDocumentMapper.update(null, updateWrapper) > 0;
    }
}
