package com.ruoyi.agent.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 文档解析服务。
 *
 * @author ruoyi
 */
@Service
public class DocumentParserService
{
    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    public String parseDocument(String filePath, String fileType)
    {
        if (StringUtils.isBlank(filePath) || StringUtils.isBlank(fileType))
        {
            return null;
        }

        String normalizedType = StringUtils.lowerCase(StringUtils.removeStart(fileType.trim(), "."));
        try
        {
            if ("txt".equals(normalizedType))
            {
                return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            }
            if ("pdf".equals(normalizedType))
            {
                return parsePdf(filePath);
            }
            if ("docx".equals(normalizedType))
            {
                return parseDocx(filePath);
            }
            log.warn("Unsupported document type: {}", fileType);
            return null;
        }
        catch (IOException e)
        {
            log.warn("Parse document failed, filePath={}, fileType={}", filePath, fileType, e);
            return null;
        }
    }

    private String parsePdf(String filePath) throws IOException
    {
        try (PDDocument document = Loader.loadPDF(new File(filePath)))
        {
            return new PDFTextStripper().getText(document);
        }
    }

    private String parseDocx(String filePath) throws IOException
    {
        try (XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(filePath)));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document))
        {
            return extractor.getText();
        }
    }
}
