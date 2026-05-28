package com.ruoyi.agent.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.agent.domain.KbChunk;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 文本切片服务。
 *
 * @author ruoyi
 */
@Service
public class TextChunkerService
{
    private static final int DEFAULT_CHUNK_SIZE = 500;

    private static final int DEFAULT_OVERLAP = 50;

    private static final String STATUS_PENDING = "pending";

    public List<KbChunk> chunkText(String text)
    {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<KbChunk> chunkText(String text, int chunkSize, int overlap)
    {
        List<KbChunk> chunks = new ArrayList<KbChunk>();
        if (StringUtils.isBlank(text))
        {
            return chunks;
        }

        int normalizedChunkSize = chunkSize > 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
        int normalizedOverlap = Math.max(0, Math.min(overlap, normalizedChunkSize - 1));
        int start = 0;
        int index = 0;
        while (start < text.length())
        {
            int end = Math.min(start + normalizedChunkSize, text.length());
            String content = text.substring(start, end);

            KbChunk chunk = new KbChunk();
            chunk.setChunkIndex(index);
            chunk.setContent(content);
            chunk.setContentHash(sha256(content));
            chunk.setVectorStatus(STATUS_PENDING);
            chunk.setChunkVersion(1);
            chunk.setTokenCount(content.length());
            chunks.add(chunk);

            if (end >= text.length())
            {
                break;
            }
            start = end - normalizedOverlap;
            index++;
        }
        return chunks;
    }

    private String sha256(String content)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
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
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
