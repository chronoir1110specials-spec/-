package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.AgentTask;

public interface IAgentTaskService
{
    AgentTask start(Long userId, Long sessionId, String agentType, String taskType, String executionMode, String inputSummary);

    void success(Long taskId, String currentStep, String outputSummary, Integer totalTokens, Integer costTime);

    void fail(Long taskId, String currentStep, String errorCode, String errorMessage, Integer costTime);

    List<AgentTask> listRecent(Integer limit);
}
