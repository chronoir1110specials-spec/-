package com.ruoyi.agent.service;

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

    ResumeInfo getById(Long id);

    boolean delete(Long id);
}
