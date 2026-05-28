package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.KbDocument;

/**
 * 知识库文档 服务层
 *
 * @author ruoyi
 */
public interface IKbDocumentService
{
    KbDocument save(KbDocument document);

    KbDocument getById(Long id);

    List<KbDocument> listAll();

    boolean delete(Long id);
}
