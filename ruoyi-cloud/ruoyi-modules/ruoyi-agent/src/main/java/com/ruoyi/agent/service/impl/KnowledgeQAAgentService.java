package com.ruoyi.agent.service.impl;

import java.util.List;
import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
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
    private ChatModelRouter chatModelRouter;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private IKbChunkService kbChunkService;

    public ChatResponse ask(Long userId, String question)
    {
        if (StringUtils.isEmpty(question))
        {
            return ChatResponse.fail("问题不能为空");
        }

        UserProfile profile = userProfileService.getByUserId(userId);
        List<KbChunk> chunks = kbChunkService.searchByKeyword(question, DEFAULT_TOP_K);
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE) + buildKnowledgeContext(chunks);

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent(question);
        request.setHistory(buildHistory(systemPrompt));
        return chatModelRouter.chat(request);
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
