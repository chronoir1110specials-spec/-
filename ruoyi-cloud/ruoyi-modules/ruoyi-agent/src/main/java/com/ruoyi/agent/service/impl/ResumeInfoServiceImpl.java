package com.ruoyi.agent.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.ResumeInfo;
import com.ruoyi.agent.mapper.ResumeInfoMapper;
import com.ruoyi.agent.service.IResumeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 简历 业务层处理
 *
 * @author ruoyi
 */
@Service
public class ResumeInfoServiceImpl implements IResumeInfoService
{
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    @Autowired
    private ResumeInfoMapper resumeInfoMapper;

    @Override
    public boolean save(ResumeInfo resumeInfo)
    {
        if (resumeInfo == null)
        {
            return false;
        }
        Date now = new Date();
        if (resumeInfo.getCreateTime() == null)
        {
            resumeInfo.setCreateTime(now);
        }
        resumeInfo.setUpdateTime(now);
        resumeInfo.setDeleted(NOT_DELETED);
        return resumeInfoMapper.insert(resumeInfo) > 0;
    }

    @Override
    public ResumeInfo getByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        LambdaQueryWrapper<ResumeInfo> queryWrapper = new LambdaQueryWrapper<ResumeInfo>()
                .eq(ResumeInfo::getUserId, userId)
                .eq(ResumeInfo::getDeleted, NOT_DELETED)
                .orderByDesc(ResumeInfo::getUpdateTime)
                .orderByDesc(ResumeInfo::getCreateTime)
                .last("limit 1");
        return resumeInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ResumeInfo> listByUserId(Long userId)
    {
        if (userId == null)
        {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ResumeInfo> queryWrapper = new LambdaQueryWrapper<ResumeInfo>()
                .eq(ResumeInfo::getUserId, userId)
                .eq(ResumeInfo::getDeleted, NOT_DELETED)
                .orderByDesc(ResumeInfo::getUpdateTime)
                .orderByDesc(ResumeInfo::getCreateTime);
        return resumeInfoMapper.selectList(queryWrapper);
    }

    @Override
    public ResumeInfo getById(Long id)
    {
        if (id == null)
        {
            return null;
        }
        LambdaQueryWrapper<ResumeInfo> queryWrapper = new LambdaQueryWrapper<ResumeInfo>()
                .eq(ResumeInfo::getId, id)
                .eq(ResumeInfo::getDeleted, NOT_DELETED)
                .last("limit 1");
        return resumeInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean delete(Long id)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ResumeInfo> updateWrapper = new LambdaUpdateWrapper<ResumeInfo>()
                .set(ResumeInfo::getDeleted, DELETED)
                .set(ResumeInfo::getUpdateTime, new Date())
                .eq(ResumeInfo::getId, id)
                .eq(ResumeInfo::getDeleted, NOT_DELETED);
        return resumeInfoMapper.update(null, updateWrapper) > 0;
    }
}
