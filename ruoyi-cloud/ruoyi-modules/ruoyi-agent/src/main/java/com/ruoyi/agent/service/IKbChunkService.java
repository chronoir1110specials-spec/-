package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.KbChunk;

/**
 * 知识片段 服务层
 *
 * @author ruoyi
 */
public interface IKbChunkService
{
    KbChunk save(KbChunk chunk);

    List<KbChunk> listByDocId(Long documentId);

    boolean deleteByDocId(Long documentId);

    KbChunk getByDocId(Long documentId, Integer chunkIndex);
}
