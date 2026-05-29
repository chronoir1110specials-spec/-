package com.ruoyi.agent.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import com.ruoyi.agent.core.DocumentParserService;
import com.ruoyi.agent.domain.ResumeInfo;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.IResumeInfoService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/resume")
public class ResumeController
{
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private static final String[] ALLOWED_EXTENSIONS = { "txt", "pdf", "docx" };

    private static final String UPLOAD_DIR = "uploads/resume";

    @Autowired
    private IResumeInfoService resumeInfoService;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private DocumentParserService documentParserService;

    @PostMapping("/upload")
    public R<ResumeInfo> upload(@RequestParam("file") MultipartFile file)
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
            return R.fail("文件格式不正确，请上传 txt、pdf、docx 格式");
        }

        Long userId = requireCurrentUserId();
        try
        {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
            Files.createDirectories(uploadPath);
            String safeFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension.toLowerCase();
            Path targetPath = uploadPath.resolve(safeFileName);
            String contentHash = saveAndHash(file, targetPath);
            String content = documentParserService.parseDocument(targetPath.toAbsolutePath().toString(), extension);

            ResumeInfo resumeInfo = new ResumeInfo();
            resumeInfo.setUserId(userId);
            resumeInfo.setResumeName(FilenameUtils.getBaseName(originalFilename));
            resumeInfo.setOriginalFileName(originalFilename);
            resumeInfo.setFileType(extension.toLowerCase());
            resumeInfo.setFileUrl(UPLOAD_DIR + "/" + safeFileName);
            resumeInfo.setContentHash(contentHash);
            UserProfile profile = userProfileService.getByUserId(userId);
            resumeInfo.setTargetPosition(profile == null ? null : profile.getTargetPosition());
            if (StringUtils.isEmpty(content))
            {
                resumeInfo.setParseStatus("failed");
                resumeInfo.setParseError("文件无法提取文本，请粘贴简历文本后重试");
            }
            else
            {
                resumeInfo.setContent(content);
                resumeInfo.setParseStatus("success");
            }
            resumeInfoService.save(resumeInfo);
            return R.ok(resumeInfo);
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

    @GetMapping("/list")
    public R<List<ResumeInfo>> list()
    {
        return R.ok(resumeInfoService.listByUserId(requireCurrentUserId()));
    }

    @GetMapping("/latest")
    public R<ResumeInfo> latest()
    {
        return R.ok(resumeInfoService.getByUserId(requireCurrentUserId()));
    }

    @GetMapping("/{id}")
    public R<ResumeInfo> detail(@PathVariable Long id)
    {
        ResumeInfo resumeInfo = ensureOwner(id);
        return R.ok(resumeInfo);
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public R<Boolean> delete(@PathVariable Long id)
    {
        ensureOwner(id);
        return R.ok(resumeInfoService.delete(id));
    }

    private ResumeInfo ensureOwner(Long id)
    {
        ResumeInfo resumeInfo = resumeInfoService.getById(id);
        if (resumeInfo == null)
        {
            throw new ServiceException("简历不存在", HttpStatus.NOT_FOUND);
        }
        if (resumeInfo.getUserId() == null || !resumeInfo.getUserId().equals(requireCurrentUserId()))
        {
            throw new ServiceException("无权访问该简历", HttpStatus.FORBIDDEN);
        }
        return resumeInfo;
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
}
