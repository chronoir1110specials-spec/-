package com.ruoyi.agent.core;

import java.util.Date;
import java.util.List;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.domain.KbChunkVector;
import com.ruoyi.agent.domain.KbDocument;
import com.ruoyi.agent.mapper.KbChunkVectorMapper;
import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.service.IAgentTaskService;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.agent.service.IKbDocumentService;
import com.ruoyi.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 知识库文档异步入库服务（解析 -> 切片 -> 向量化）。
 *
 * @author ruoyi
 */
@Service
public class KbIngestionService
{
    private static final Logger log = LoggerFactory.getLogger(KbIngestionService.class);

    private static final String STATUS_PROCESSING = "processing";

    private static final String STATUS_SUCCESS = "success";

    private static final String STATUS_FAILED = "failed";

    @Autowired
    private IKbDocumentService kbDocumentService;

    @Autowired
    private IKbChunkService kbChunkService;

    @Autowired
    private KbChunkVectorMapper kbChunkVectorMapper;

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private TextChunkerService textChunkerService;

    @Autowired
    private EmbeddingClient embeddingClient;

    @Autowired
    private IAgentTaskService agentTaskService;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    @Async("kbIngestionExecutor")
    public void ingest(Long documentId, String absolutePath, String fileType)
    {
        if (documentId == null)
        {
            return;
        }
        KbDocument doc = kbDocumentService.getById(documentId);
        if (doc == null)
        {
            return;
        }
        long startTime = System.currentTimeMillis();
        AgentTask task = agentTaskService.start(doc.getCreateUser(), null, "knowledge_ingestion", "kb_ingest", "async",
                doc.getTitle());
        try
        {
            doc.setParseStatus(STATUS_PROCESSING);
            kbDocumentService.save(doc);

            String text = documentParserService.parseDocument(absolutePath, fileType);
            if (StringUtils.isEmpty(text))
            {
                markFailed(doc);
                agentStepLogService.log(task.getId(), doc.getCreateUser(), null, "knowledge_ingestion", 1, "parse",
                        "解析知识库文档", doc.getTitle(), null, STATUS_FAILED, "文档内容为空");
                agentTaskService.fail(task.getId(), "parse", "PARSE_EMPTY", "文档内容为空",
                        (int) (System.currentTimeMillis() - startTime));
                log.warn("KB ingestion parse empty, documentId={}", documentId);
                return;
            }
            doc.setParseStatus(STATUS_SUCCESS);
            doc.setEmbeddingStatus(STATUS_PROCESSING);
            kbDocumentService.save(doc);
            agentStepLogService.log(task.getId(), doc.getCreateUser(), null, "knowledge_ingestion", 1, "parse",
                    "解析知识库文档", doc.getTitle(), "字符数：" + text.length(), STATUS_SUCCESS, null);

            List<KbChunk> chunks = textChunkerService.chunkText(text);
            int saved = 0;
            String embeddingModelName = embeddingClient.getModelName();
            for (KbChunk chunk : chunks)
            {
                chunk.setDocumentId(documentId);
                // 切片元数据（设计 9.5：文档标题、片段序号）
                chunk.setMetadata(buildMetadata(doc.getTitle(), chunk.getChunkIndex()));
                embedAndStore(chunk, embeddingModelName);
                saved++;
            }
            doc.setChunkCount(saved);
            doc.setEmbeddingStatus(saved > 0 ? STATUS_SUCCESS : STATUS_FAILED);
            kbDocumentService.save(doc);
            agentStepLogService.log(task.getId(), doc.getCreateUser(), null, "knowledge_ingestion", 2, "embedding",
                    "切片并向量化知识库文档", doc.getTitle(), "切片数：" + saved, doc.getEmbeddingStatus(), null);
            if (saved > 0)
            {
                agentTaskService.success(task.getId(), "finish", "切片数：" + saved, null,
                        (int) (System.currentTimeMillis() - startTime));
            }
            else
            {
                agentTaskService.fail(task.getId(), "embedding", "EMBEDDING_FAILED", "未生成有效切片",
                        (int) (System.currentTimeMillis() - startTime));
            }
        }
        catch (Exception e)
        {
            log.warn("KB ingestion failed, documentId={}", documentId, e);
            markFailed(doc);
            agentTaskService.fail(task.getId(), "exception", "SYSTEM_ERROR", e.getMessage(),
                    (int) (System.currentTimeMillis() - startTime));
        }
    }

    private void markFailed(KbDocument doc)
    {
        try
        {
            doc.setParseStatus(STATUS_FAILED);
            doc.setEmbeddingStatus(STATUS_FAILED);
            kbDocumentService.save(doc);
        }
        catch (Exception ignore)
        {
            log.debug("Update KB document status to failed swallowed", ignore);
        }
    }

    /**
     * 对单个切片做向量化并双写（MySQL kb_chunk + PG kb_chunk_vector）。
     *
     * @param chunk              切片
     * @param embeddingModelName 当前 embedding 模型名（记录到 kb_chunk，用于判断是否需重建）
     * @return 是否向量化成功
     */
    private boolean embedAndStore(KbChunk chunk, String embeddingModelName)
    {
        float[] embedding = embeddingClient.embed(chunk.getContent());
        String vectorLiteral = null;
        if (embedding != null && embedding.length > 0)
        {
            vectorLiteral = formatEmbedding(embedding);
            chunk.setEmbeddingDimension(embedding.length);
            chunk.setVectorStatus(STATUS_SUCCESS);
            chunk.setEmbeddingVector(vectorLiteral);
            chunk.setEmbeddingModel(embeddingModelName);
        }
        else
        {
            chunk.setVectorStatus(STATUS_FAILED);
        }
        kbChunkService.save(chunk);
        if (vectorLiteral != null)
        {
            saveVector(chunk, vectorLiteral, embedding.length);
            return true;
        }
        return false;
    }

    /**
     * 重新向量化已入库文档（设计 9.7.5）：对已存在的切片重新调用 Embedding 并 upsert 向量，
     * 不重新解析原文件。用于更换 Embedding 模型 / 维度后重建向量。
     *
     * @param documentId 文档 ID
     */
    @Async("kbIngestionExecutor")
    public void reembedDocument(Long documentId)
    {
        if (documentId == null)
        {
            return;
        }
        KbDocument doc = kbDocumentService.getById(documentId);
        if (doc == null)
        {
            return;
        }
        long startTime = System.currentTimeMillis();
        AgentTask task = agentTaskService.start(doc.getCreateUser(), null, "knowledge_reembed", "kb_reembed", "async",
                doc.getTitle());
        try
        {
            List<KbChunk> chunks = kbChunkService.listByDocId(documentId);
            if (chunks == null || chunks.isEmpty())
            {
                agentTaskService.fail(task.getId(), "reembed", "NO_CHUNKS", "无可重建切片",
                        (int) (System.currentTimeMillis() - startTime));
                return;
            }
            doc.setEmbeddingStatus(STATUS_PROCESSING);
            kbDocumentService.save(doc);

            String embeddingModelName = embeddingClient.getModelName();
            int success = 0;
            for (KbChunk chunk : chunks)
            {
                if (embedAndStore(chunk, embeddingModelName))
                {
                    success++;
                }
            }
            doc.setEmbeddingStatus(success > 0 ? STATUS_SUCCESS : STATUS_FAILED);
            kbDocumentService.save(doc);
            agentStepLogService.log(task.getId(), doc.getCreateUser(), null, "knowledge_reembed", 1, "embedding",
                    "重新向量化", doc.getTitle(), "成功切片：" + success + "/" + chunks.size(),
                    success > 0 ? STATUS_SUCCESS : STATUS_FAILED, null);
            agentTaskService.success(task.getId(), "finish", "重建切片：" + success + "/" + chunks.size(), null,
                    (int) (System.currentTimeMillis() - startTime));
        }
        catch (Exception e)
        {
            log.warn("KB reembed failed, documentId={}", documentId, e);
            agentTaskService.fail(task.getId(), "exception", "SYSTEM_ERROR", e.getMessage(),
                    (int) (System.currentTimeMillis() - startTime));
        }
    }

    private String buildMetadata(String docTitle, Integer chunkIndex)
    {
        String safeTitle = docTitle == null ? "" : docTitle.replace("\"", "'");
        return "{\"title\":\"" + safeTitle + "\",\"chunkIndex\":" + chunkIndex + "}";
    }

    private void saveVector(KbChunk chunk, String vectorLiteral, int dimension)
    {
        try
        {
            Date now = new Date();
            KbChunkVector vector = new KbChunkVector();
            vector.setChunkId(chunk.getId());
            vector.setEmbeddingVector(vectorLiteral);
            vector.setEmbeddingModel(chunk.getEmbeddingModel());
            vector.setEmbeddingDimension(dimension);
            vector.setVectorStatus(STATUS_SUCCESS);
            vector.setCreateTime(now);
            vector.setUpdateTime(now);
            vector.setDeleted(0);
            kbChunkVectorMapper.insertVector(vector);
        }
        catch (Exception e)
        {
            log.warn("Save vector to pgvector failed, chunkId={}", chunk.getId(), e);
        }
    }

    private String formatEmbedding(float[] embedding)
    {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
