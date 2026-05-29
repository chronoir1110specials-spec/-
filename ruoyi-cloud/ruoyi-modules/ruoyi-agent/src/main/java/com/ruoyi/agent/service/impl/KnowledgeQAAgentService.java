package com.ruoyi.agent.service.impl;

import java.util.List;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.domain.AgentTask;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IAgentStepLogService;
import com.ruoyi.agent.service.IAgentTaskService;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.agent.runtime.AgentRuntime;
import com.ruoyi.agent.tool.AgentToolContext;
import com.ruoyi.agent.tool.ToolResult;
import com.ruoyi.agent.tool.impl.KnowledgeSearchTool;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.api.RemoteModelService;
import com.ruoyi.model.api.dto.ChatRequest;
import com.ruoyi.model.api.dto.ChatRequest.ChatMessageVo;
import com.ruoyi.model.api.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 知识库问答 Agent 服务。
 *
 * @author ruoyi
 */
@Service
public class KnowledgeQAAgentService extends BaseAgentService
{
    private static final String AGENT_TYPE = "knowledge_qa";

    private static final int DEFAULT_TOP_K = 5;

    @Autowired
    private RemoteModelService remoteModelService;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private IKbChunkService kbChunkService;

    @Autowired
    private IAgentTaskService agentTaskService;

    @Autowired
    private IAgentStepLogService agentStepLogService;

    @Autowired
    private AgentRuntime agentRuntime;

    public ChatResponse ask(Long userId, String question)
    {
        if (StringUtils.isEmpty(question))
        {
            return ChatResponse.fail("问题不能为空");
        }

        long startTime = System.currentTimeMillis();
        AgentTask task = agentTaskService.start(userId, null, AGENT_TYPE, "knowledge_ask", "sync", question);
        try
        {
            UserProfile profile = userProfileService.getByUserId(userId);
            // 通过 Agent Runtime + ToolRegistry 调用知识库检索工具（统一工具治理与审计）
            AgentToolContext toolContext = new AgentToolContext(userId, null, AGENT_TYPE);
            ToolResult<List<KbChunk>> toolResult = agentRuntime.runTool(toolContext, "KnowledgeSearchTool",
                    new KnowledgeSearchTool.Input(question, DEFAULT_TOP_K), task.getId(), 1);
            List<KbChunk> chunks = toolResult != null && toolResult.isSuccess() ? toolResult.getData() : null;
            String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE) + buildKnowledgeContext(chunks);

            ChatRequest request = new ChatRequest();
            request.setUserId(userId);
            request.setContent(question);
            request.setHistory(buildHistory(systemPrompt));
            R<ChatResponse> r = remoteModelService.chat(request, SecurityConstants.INNER);
            ChatResponse response = r == null ? null : r.getData();
            if (response != null && response.isSuccess())
            {
                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 2, "model", "调用模型回答知识库问题",
                        question, response.getContent(), "succeeded", null);
                agentTaskService.success(task.getId(), "finish", response.getContent(), response.getTotalTokens(),
                        response.getCostTime() == null ? (int) (System.currentTimeMillis() - startTime)
                                : response.getCostTime().intValue());
            }
            else
            {
                String errorMessage = response == null ? "模型服务无响应" : response.getErrorMessage();
                agentStepLogService.log(task.getId(), userId, null, AGENT_TYPE, 2, "model", "调用模型回答知识库问题",
                        question, null, "failed", errorMessage);
                agentTaskService.fail(task.getId(), "model", "MODEL_ERROR", errorMessage,
                        (int) (System.currentTimeMillis() - startTime));
            }
            return response;
        }
        catch (RuntimeException e)
        {
            agentTaskService.fail(task.getId(), "exception", "SYSTEM_ERROR", e.getMessage(),
                    (int) (System.currentTimeMillis() - startTime));
            throw e;
        }
    }

    private List<ChatMessageVo> buildHistory(String systemPrompt)
    {
        return buildSystemHistory(systemPrompt);
    }

    private String buildKnowledgeContext(List<KbChunk> chunks)
    {
        StringBuilder context = new StringBuilder();
        context.append("\n【知识库参考资料】\n");
        if (chunks == null || chunks.isEmpty())
        {
            context.append("未检索到直接相关资料。请说明信息不足，并基于通用就业知识谨慎回答。\n");
            return context.toString();
        }
        for (KbChunk chunk : chunks)
        {
            if (chunk == null || StringUtils.isEmpty(chunk.getContent()))
            {
                continue;
            }
            context.append("- 来源：文档 ").append(chunk.getDocumentId())
                    .append("，片段 ").append(chunk.getChunkIndex()).append("\n");
            context.append(chunk.getContent()).append("\n");
        }
        context.append("回答时优先依据上述资料，并在相关结论后注明文档和片段来源。\n");
        return context.toString();
    }
}
