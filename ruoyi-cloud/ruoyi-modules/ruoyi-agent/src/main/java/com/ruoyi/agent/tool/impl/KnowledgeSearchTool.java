package com.ruoyi.agent.tool.impl;

import java.util.List;
import com.ruoyi.agent.domain.KbChunk;
import com.ruoyi.agent.service.IKbChunkService;
import com.ruoyi.agent.tool.AgentTool;
import com.ruoyi.agent.tool.AgentToolContext;
import com.ruoyi.agent.tool.ToolPermissionResult;
import com.ruoyi.agent.tool.ToolValidationResult;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 知识库检索工具：对问题做向量检索，返回 TopK 就业知识片段。
 *
 * @author ruoyi
 */
@Component
public class KnowledgeSearchTool implements AgentTool<KnowledgeSearchTool.Input, List<KbChunk>>
{
    private static final int DEFAULT_TOP_K = 5;

    private static final int MAX_TOP_K = 10;

    @Autowired
    private IKbChunkService kbChunkService;

    @Override
    public String name()
    {
        return "KnowledgeSearchTool";
    }

    @Override
    public String description()
    {
        return "检索就业知识库相关片段（pgvector 余弦向量检索，失败回退 LIKE）";
    }

    @Override
    public Class<Input> inputType()
    {
        return Input.class;
    }

    @Override
    public ToolValidationResult validateInput(Input input, AgentToolContext context)
    {
        if (input == null || StringUtils.isEmpty(input.getQuery()))
        {
            return ToolValidationResult.fail("query 不能为空");
        }
        return ToolValidationResult.ok();
    }

    @Override
    public ToolPermissionResult checkPermission(Input input, AgentToolContext context)
    {
        // 知识库为公共资料，登录用户可检索
        return ToolPermissionResult.allow();
    }

    @Override
    public List<KbChunk> execute(Input input, AgentToolContext context)
    {
        int topK = input.getTopK() == null || input.getTopK() <= 0
                ? DEFAULT_TOP_K : Math.min(input.getTopK(), MAX_TOP_K);
        return kbChunkService.searchByKeyword(input.getQuery(), topK);
    }

    /**
     * 输入：检索词 + TopK。
     */
    public static class Input
    {
        private String query;

        private Integer topK;

        public Input()
        {
        }

        public Input(String query, Integer topK)
        {
            this.query = query;
            this.topK = topK;
        }

        public String getQuery()
        {
            return query;
        }

        public void setQuery(String query)
        {
            this.query = query;
        }

        public Integer getTopK()
        {
            return topK;
        }

        public void setTopK(Integer topK)
        {
            this.topK = topK;
        }
    }
}
