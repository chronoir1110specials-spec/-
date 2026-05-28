package com.ruoyi.model.service;

import java.util.List;
import com.ruoyi.model.domain.ModelCallLog;

/**
 * 模型调用日志 服务层
 *
 * @author ruoyi
 */
public interface IModelCallLogService
{
    /**
     * 记录模型调用日志
     *
     * @param log 模型调用日志
     */
    public void logCall(ModelCallLog log);

    /**
     * 按用户查询
     *
     * @param userId 用户 ID
     * @return 模型调用日志集合
     */
    public List<ModelCallLog> listByUserId(Long userId);

    /**
     * 按会话查询
     *
     * @param sessionId 会话 ID
     * @return 模型调用日志集合
     */
    public List<ModelCallLog> listBySessionId(Long sessionId);
}
