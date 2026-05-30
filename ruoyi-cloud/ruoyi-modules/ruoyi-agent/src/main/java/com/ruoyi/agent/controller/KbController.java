package com.ruoyi.agent.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.ruoyi.agent.core.KbIngestionService;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.domain.KbDocument;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.agent.service.IKbDocumentService;
import com.ruoyi.agent.service.impl.KnowledgeQAAgentService;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.security.annotation.RequiresRoles;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.model.api.dto.ChatResponse;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/kb")
public class KbController
{
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private static final String[] ALLOWED_EXTENSIONS = { "pdf", "docx", "txt", "md" };

    private static final String STATUS_PENDING = "pending";

    private static final String UPLOAD_DIR = "uploads/kb";

    private static final int DEFAULT_TOP_K = 5;

    @Autowired
    private IKbDocumentService kbDocumentService;

    @Autowired
    private IKbChunkService kbChunkService;

    @Autowired
    private KnowledgeQAAgentService knowledgeQAAgentService;

    @Autowired
    private KbIngestionService kbIngestionService;

    /**
     * 上传知识库文档
     */
    @RequiresRoles("admin")
    @PostMapping("/document/upload")
    public R<KbDocument> upload(@RequestParam("file") MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            return R.fail("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            return R.fail("文件大小不能超过 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileTypeUtils.getExtension(file);
        if (StringUtils.isEmpty(originalFilename) || StringUtils.isEmpty(extension) || !isAllowedExtension(extension))
        {
            return R.fail("文件格式不正确，请上传 pdf、docx、txt、md 格式");
        }
        // 文件头(magic number)校验，防伪造扩展名（设计 15.6）
        if (!com.ruoyi.agent.core.FileSignatureValidator.matches(file, extension))
        {
            return R.fail("文件内容与扩展名不符，疑似伪造文件");
        }

        Long currentUserId = requireCurrentUserId();
        try
        {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String baseName = FilenameUtils.getBaseName(originalFilename);
            String safeFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension.toLowerCase();
            Path targetPath = uploadPath.resolve(safeFileName);
            String contentHash = saveAndHash(file, targetPath);

            KbDocument document = new KbDocument();
            document.setTitle(baseName);
            document.setFileName(originalFilename);
            document.setFileType(extension.toLowerCase());
            document.setFileUrl(UPLOAD_DIR + "/" + safeFileName);
            document.setContentHash(contentHash);
            document.setParseStatus(STATUS_PENDING);
            document.setEmbeddingStatus(STATUS_PENDING);
            document.setChunkCount(0);
            document.setCreateUser(currentUserId);
            KbDocument saved = kbDocumentService.save(document);

            kbIngestionService.ingest(saved.getId(), targetPath.toAbsolutePath().toString(), document.getFileType());
            return R.ok(saved);
        }
        catch (IOException e)
        {
            return R.fail("文件保存失败：" + e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            return R.fail("文件 hash 计算失败");
        }
    }

    /**
     * 查询文档列表
     */
    @GetMapping("/document/list")
    public R<List<KbDocument>> list()
    {
        return R.ok(kbDocumentService.listAll());
    }

    /**
     * 删除文档
     */
    @RequiresRoles("admin")
    @DeleteMapping("/document/{id}")
    public R<Boolean> delete(@PathVariable Long id)
    {
        boolean result = kbDocumentService.delete(id);
        kbChunkService.deleteByDocId(id);
        return R.ok(result);
    }

    /**
     * 删除文档，兼容设计文档中的接口路径。
     */
    @RequiresRoles("admin")
    @DeleteMapping("/document/delete/{id}")
    public R<Boolean> deleteAlias(@PathVariable Long id)
    {
        return delete(id);
    }

    /**
     * 重新向量化文档（设计 9.7.5）：对已入库切片重新生成向量并 upsert，
     * 用于更换 Embedding 模型 / 维度后重建。异步执行。
     */
    @RequiresRoles("admin")
    @PostMapping({"/document/{id}/reembed", "/reembed/{id}"})
    public R<Boolean> reembed(@PathVariable Long id)
    {
        KbDocument document = kbDocumentService.getById(id);
        if (document == null)
        {
            return R.fail("文档不存在");
        }
        kbIngestionService.reembedDocument(id);
        return R.ok(Boolean.TRUE, "已触发重新向量化，请稍后查看状态");
    }

    /**
     * 语义检索
     */
    @PostMapping("/search")
    public R<List<Map<String, Object>>> search(@RequestBody SearchRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getQuery()))
        {
            return R.fail("查询内容不能为空");
        }
        int topK = request.getTopK() == null || request.getTopK() <= 0 ? DEFAULT_TOP_K : request.getTopK();
        List<KbChunk> chunks = kbChunkService.searchByKeyword(request.getQuery(), topK);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (chunks != null)
        {
            for (KbChunk chunk : chunks)
            {
                if (chunk == null)
                {
                    continue;
                }
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("id", chunk.getId());
                item.put("documentId", chunk.getDocumentId());
                item.put("chunkIndex", chunk.getChunkIndex());
                item.put("content", chunk.getContent());
                result.add(item);
            }
        }
        return R.ok(result);
    }

    /**
     * 知识库问答
     */
    @PostMapping("/ask")
    public R<ChatResponse> ask(@RequestBody AskRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getQuestion()))
        {
            return R.fail("问题不能为空");
        }
        return R.ok(knowledgeQAAgentService.ask(requireCurrentUserId(), request.getQuestion()));
    }

    private boolean isAllowedExtension(String extension)
    {
        for (String allowedExtension : ALLOWED_EXTENSIONS)
        {
            if (allowedExtension.equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    private String saveAndHash(MultipartFile file, Path targetPath) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = file.getInputStream();
             DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest))
        {
            Files.copy(digestInputStream, targetPath);
        }
        return toHex(messageDigest.digest());
    }

    private String toHex(byte[] bytes)
    {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte item : bytes)
        {
            String hex = Integer.toHexString(item & 0xff);
            if (hex.length() == 1)
            {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }

    private Long requireCurrentUserId()
    {
        Long userId;
        try
        {
            userId = SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            throw new ServiceException("当前用户未登录", HttpStatus.UNAUTHORIZED);
        }
        if (userId == null)
        {
            throw new ServiceException("当前用户未登录", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 知识库检索请求
     */
    public static class SearchRequest
    {
        private String query;

        private Integer topK;

        public String getQuery()
        {
            return query;
        }

        public void setQuery(String query)
        {
            this.query = query;
        }

        public Integer getTopK()
        {
            return topK;
        }

        public void setTopK(Integer topK)
        {
            this.topK = topK;
        }
    }

    /**
     * 知识库问答请求
     */
    public static class AskRequest
    {
        private String question;

        public String getQuestion()
        {
            return question;
        }

        public void setQuestion(String question)
        {
            this.question = question;
        }
    }
}
