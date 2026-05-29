package com.ruoyi.agent.service.impl;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.mapper.AgentTaskMapper;
import com.ruoyi.agent.service.IAgentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentTaskServiceImpl implements IAgentTaskService
{
    private static final Integer NOT_DELETED = 0;

    @Autowired
    private AgentTaskMapper agentTaskMapper;

    @Override
    public AgentTask start(Long userId, Long sessionId, String agentType, String taskType, String executionMode, String inputSummary)
    {
        AgentTask task = new AgentTask();
        task.setUserId(userId);
        task.setSessionId(sessionId);
        task.setAgentType(agentType);
        task.setTaskType(taskType);
        task.setExecutionMode(executionMode);
        task.setInputSummary(summary(inputSummary));
        task.setStatus("running");
        task.setCurrentStep("start");
        task.setTotalToolCalls(0);
        task.setTotalTokens(0);
        task.setTotalCostTime(0);
        task.setCreateTime(new Date());
        task.setUpdateTime(task.getCreateTime());
        task.setDeleted(NOT_DELETED);
        agentTaskMapper.insert(task);
        return task;
    }

    @Override
    public void success(Long taskId, String currentStep, String outputSummary, Integer totalTokens, Integer costTime)
    {
        AgentTask task = agentTaskMapper.selectById(taskId);
        if (task == null)
        {
            return;
        }
        task.setStatus("succeeded");
        task.setCurrentStep(currentStep);
        task.setOutputSummary(summary(outputSummary));
        task.setTotalTokens(totalTokens == null ? 0 : totalTokens);
        task.setTotalCostTime(costTime == null ? 0 : costTime);
        task.setUpdateTime(new Date());
        task.setFinishTime(task.getUpdateTime());
        agentTaskMapper.updateById(task);
    }

    @Override
    public void fail(Long taskId, String currentStep, String errorCode, String errorMessage, Integer costTime)
    {
        AgentTask task = agentTaskMapper.selectById(taskId);
        if (task == null)
        {
            return;
        }
        task.setStatus("failed");
        task.setCurrentStep(currentStep);
        task.setErrorCode(errorCode);
        task.setErrorMessage(summary(errorMessage));
        task.setTotalCostTime(costTime == null ? 0 : costTime);
        task.setUpdateTime(new Date());
        task.setFinishTime(task.getUpdateTime());
        agentTaskMapper.updateById(task);
    }

    @Override
    public List<AgentTask> listRecent(Integer limit)
    {
        int rowLimit = limit == null || limit <= 0 ? 50 : Math.min(limit, 200);
        LambdaQueryWrapper<AgentTask> queryWrapper = new LambdaQueryWrapper<AgentTask>()
                .eq(AgentTask::getDeleted, NOT_DELETED)
                .orderByDesc(AgentTask::getCreateTime)
                .last("limit " + rowLimit);
        return agentTaskMapper.selectList(queryWrapper);
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
