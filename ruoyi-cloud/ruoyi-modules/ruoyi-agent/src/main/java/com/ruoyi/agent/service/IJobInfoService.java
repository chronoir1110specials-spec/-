package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.JobInfo;

/**
 * 岗位信息 服务层
 *
 * @author ruoyi
 */
public interface IJobInfoService
{
    boolean save(JobInfo jobInfo);

    JobInfo getByUserId(Long userId);

    JobInfo getById(Long id);

    List<JobInfo> listByUserId(Long userId);

    boolean delete(Long id);
}
