package com.ruoyi.model.service.impl;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.model.domain.ModelCallLog;
import com.ruoyi.model.mapper.ModelCallLogMapper;
import com.ruoyi.model.service.IModelCallLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 模型调用日志 业务层处理
 *
 * @author ruoyi
 */
@Service
public class ModelCallLogServiceImpl implements IModelCallLogService
{
    private static final Logger log = LoggerFactory.getLogger(ModelCallLogServiceImpl.class);

    private static final Integer NOT_DELETED = 0;

    @Autowired
    private ModelCallLogMapper modelCallLogMapper;

    /**
     * 记录模型调用日志
     *
     * @param modelCallLog 模型调用日志
     */
    @Async("modelLogExecutor")
    @Override
    public void logCall(ModelCallLog modelCallLog)
    {
        try
        {
            if (modelCallLog == null)
            {
                return;
            }
            if (modelCallLog.getCreateTime() == null)
            {
                modelCallLog.setCreateTime(new Date());
            }
            if (modelCallLog.getDeleted() == null)
            {
                modelCallLog.setDeleted(NOT_DELETED);
            }
            modelCallLogMapper.insert(modelCallLog);
        }
        catch (Exception e)
        {
            log.warn("Save model call log failed", e);
        }
    }

    /**
     * 查询最近模型调用日志
     *
     * @param limit 查询数量
     * @return 模型调用日志集合
     */
    @Override
    public List<ModelCallLog> listRecent(int limit)
    {
        LambdaQueryWrapper<ModelCallLog> queryWrapper = new LambdaQueryWrapper<ModelCallLog>()
                .eq(ModelCallLog::getDeleted, NOT_DELETED)
                .orderByDesc(ModelCallLog::getCreateTime)
                .last("limit " + Math.max(1, limit));
        return modelCallLogMapper.selectList(queryWrapper);
    }

    /**
     * 按用户查询
     *
     * @param userId 用户 ID
     * @return 模型调用日志集合
     */
    @Override
    public List<ModelCallLog> listByUserId(Long userId)
    {
        LambdaQueryWrapper<ModelCallLog> queryWrapper = new LambdaQueryWrapper<ModelCallLog>()
                .eq(ModelCallLog::getUserId, userId)
                .eq(ModelCallLog::getDeleted, NOT_DELETED)
                .orderByDesc(ModelCallLog::getCreateTime);
        return modelCallLogMapper.selectList(queryWrapper);
    }

    /**
     * 按会话查询
     *
     * @param sessionId 会话 ID
     * @return 模型调用日志集合
     */
    @Override
    public List<ModelCallLog> listBySessionId(Long sessionId)
    {
        LambdaQueryWrapper<ModelCallLog> queryWrapper = new LambdaQueryWrapper<ModelCallLog>()
                .eq(ModelCallLog::getSessionId, sessionId)
                .eq(ModelCallLog::getDeleted, NOT_DELETED)
                .orderByDesc(ModelCallLog::getCreateTime);
        return modelCallLogMapper.selectList(queryWrapper);
    }
}
