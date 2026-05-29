package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.ResumeInfo;

/**
 * 简历 服务层
 *
 * @author ruoyi
 */
public interface IResumeInfoService
{
    boolean save(ResumeInfo resumeInfo);

    ResumeInfo getByUserId(Long userId);

    List<ResumeInfo> listByUserId(Long userId);

    ResumeInfo getById(Long id);

    boolean delete(Long id);
}
