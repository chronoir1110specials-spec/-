package com.ruoyi.agent.core;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件头（magic number）校验（设计 15.6）。
 *
 * <p>在扩展名/MIME 之外，再核对文件真实头部字节，防止伪造扩展名上传。
 * 文本类（txt/md）无固定 magic，改用"无 NUL 字节"的文本嗅探。</p>
 *
 * @author ruoyi
 */
public final class FileSignatureValidator
{
    private static final int HEADER_LEN = 8;

    /** PDF: 25 50 44 46 (%PDF) */
    private static final byte[] PDF_MAGIC = { 0x25, 0x50, 0x44, 0x46 };

    /** ZIP/OOXML(docx): 50 4B 03 04 (PK..) */
    private static final byte[] ZIP_MAGIC = { 0x50, 0x4B, 0x03, 0x04 };

    private FileSignatureValidator()
    {
    }

    /**
     * 校验文件头是否与声明的扩展名一致。
     *
     * @param file      上传文件
     * @param extension 小写扩展名（pdf/docx/txt/md）
     * @return 一致返回 true
     */
    public static boolean matches(MultipartFile file, String extension)
    {
        if (file == null || extension == null)
        {
            return false;
        }
        byte[] header = readHeader(file);
        if (header == null || header.length == 0)
        {
            return false;
        }
        switch (extension.toLowerCase())
        {
            case "pdf":
                return startsWith(header, PDF_MAGIC);
            case "docx":
                // docx 是 OOXML(zip 容器)，头部为 PK..
                return startsWith(header, ZIP_MAGIC);
            case "txt":
            case "md":
                // 文本文件：头部不应包含 NUL 字节（二进制特征）
                return isLikelyText(header);
            default:
                return false;
        }
    }

    private static byte[] readHeader(MultipartFile file)
    {
        try (InputStream in = file.getInputStream())
        {
            byte[] buf = new byte[HEADER_LEN];
            int read = in.read(buf);
            if (read <= 0)
            {
                return new byte[0];
            }
            if (read == HEADER_LEN)
            {
                return buf;
            }
            byte[] trimmed = new byte[read];
            System.arraycopy(buf, 0, trimmed, 0, read);
            return trimmed;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private static boolean startsWith(byte[] header, byte[] magic)
    {
        if (header.length < magic.length)
        {
            return false;
        }
        for (int i = 0; i < magic.length; i++)
        {
            if (header[i] != magic[i])
            {
                return false;
            }
        }
        return true;
    }

    private static boolean isLikelyText(byte[] header)
    {
        for (byte b : header)
        {
            if (b == 0x00)
            {
                return false;
            }
        }
        return true;
    }
}
