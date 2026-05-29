package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.AgentStepLog;

public interface IAgentStepLogService
{
    void log(Long taskId, Long userId, Long sessionId, String agentType, Integer stepIndex, String stepType,
            String stepName, String inputSummary, String outputSummary, String status, String errorMessage);

    List<AgentStepLog> listByTaskId(Long taskId);
}
