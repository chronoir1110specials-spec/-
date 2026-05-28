package com.ruoyi.agent.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.JobInfo;
import com.ruoyi.agent.mapper.JobInfoMapper;
import com.ruoyi.agent.service.IJobInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 岗位信息 业务层处理
 *
 * @author ruoyi
 */
@Service
public class JobInfoServiceImpl implements IJobInfoService
{
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    @Autowired
    private JobInfoMapper jobInfoMapper;

    @Override
    public boolean save(JobInfo jobInfo)
    {
        if (jobInfo == null)
        {
            return false;
        }
        if (jobInfo.getCreateTime() == null)
        {
            jobInfo.setCreateTime(new Date());
        }
        jobInfo.setDeleted(NOT_DELETED);
        return jobInfoMapper.insert(jobInfo) > 0;
    }

    @Override
    public JobInfo getByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        LambdaQueryWrapper<JobInfo> queryWrapper = new LambdaQueryWrapper<JobInfo>()
                .eq(JobInfo::getUserId, userId)
                .eq(JobInfo::getDeleted, NOT_DELETED)
                .orderByDesc(JobInfo::getCreateTime)
                .last("limit 1");
        return jobInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public JobInfo getById(Long id)
    {
        if (id == null)
        {
            return null;
        }
        LambdaQueryWrapper<JobInfo> queryWrapper = new LambdaQueryWrapper<JobInfo>()
                .eq(JobInfo::getId, id)
                .eq(JobInfo::getDeleted, NOT_DELETED)
                .last("limit 1");
        return jobInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public List<JobInfo> listByUserId(Long userId)
    {
        if (userId == null)
        {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<JobInfo> queryWrapper = new LambdaQueryWrapper<JobInfo>()
                .eq(JobInfo::getUserId, userId)
                .eq(JobInfo::getDeleted, NOT_DELETED)
                .orderByDesc(JobInfo::getCreateTime);
        return jobInfoMapper.selectList(queryWrapper);
    }

    @Override
    public boolean delete(Long id)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<JobInfo> updateWrapper = new LambdaUpdateWrapper<JobInfo>()
                .set(JobInfo::getDeleted, DELETED)
                .eq(JobInfo::getId, id)
                .eq(JobInfo::getDeleted, NOT_DELETED);
        return jobInfoMapper.update(null, updateWrapper) > 0;
    }
}
