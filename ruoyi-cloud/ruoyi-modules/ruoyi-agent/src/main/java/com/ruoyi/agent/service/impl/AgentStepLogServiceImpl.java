package com.ruoyi.agent.service.impl;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.agent.domain.AgentStepLog;
import com.ruoyi.agent.mapper.AgentStepLogMapper;
import com.ruoyi.agent.service.IAgentStepLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentStepLogServiceImpl implements IAgentStepLogService
{
    private static final Integer NOT_DELETED = 0;

    @Autowired
    private AgentStepLogMapper agentStepLogMapper;

    @Override
    public void log(Long taskId, Long userId, Long sessionId, String agentType, Integer stepIndex, String stepType,
            String stepName, String inputSummary, String outputSummary, String status, String errorMessage)
    {
        AgentStepLog stepLog = new AgentStepLog();
        stepLog.setTaskId(taskId);
        stepLog.setUserId(userId);
        stepLog.setSessionId(sessionId);
        stepLog.setAgentType(agentType);
        stepLog.setStepIndex(stepIndex);
        stepLog.setStepType(stepType);
        stepLog.setStepName(stepName);
        stepLog.setInputSummary(summary(inputSummary));
        stepLog.setOutputSummary(summary(outputSummary));
        stepLog.setStatus(status);
        stepLog.setErrorMessage(summary(errorMessage));
        stepLog.setCreateTime(new Date());
        stepLog.setDeleted(NOT_DELETED);
        agentStepLogMapper.insert(stepLog);
    }

    @Override
    public List<AgentStepLog> listByTaskId(Long taskId)
    {
        LambdaQueryWrapper<AgentStepLog> queryWrapper = new LambdaQueryWrapper<AgentStepLog>()
                .eq(AgentStepLog::getTaskId, taskId)
                .eq(AgentStepLog::getDeleted, NOT_DELETED)
                .orderByAsc(AgentStepLog::getStepIndex)
                .orderByAsc(AgentStepLog::getCreateTime);
        return agentStepLogMapper.selectList(queryWrapper);
    }

    private String summary(String text)
    {
        if (text == null)
        {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 1000 ? normalized.substring(0, 1000) : normalized;
    }
}
