package com.ruoyi.agent.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Agent 定义注册表（设计 8.11，毕设阶段 Java 配置实现）。
 *
 * @author ruoyi
 */
@Component
public class AgentDefinitionRegistry
{
    private final Map<String, AgentDefinition> definitions = new LinkedHashMap<String, AgentDefinition>();

    public AgentDefinitionRegistry()
    {
        register(new AgentDefinition(
                "knowledge_qa", "知识库问答 Agent", "基于就业知识库回答政策与流程问题",
                Arrays.asList("KnowledgeSearchTool", "UserProfileTool"),
                "profile", "required", "primary_with_fallback", 3, 60000));

        register(new AgentDefinition(
                "job_analyze", "岗位分析 Agent", "分析岗位 JD，提取技能要求",
                Arrays.asList("JobKeywordExtractTool", "UserProfileTool"),
                "profile", "optional", "primary_with_fallback", 3, 60000));

        register(new AgentDefinition(
                "resume_optimize", "简历优化 Agent", "分析学生简历并给出结构化优化建议",
                Arrays.asList("UserProfileTool", "JobKeywordExtractTool"),
                "profile", "optional", "primary_with_fallback", 5, 60000));

        register(new AgentDefinition(
                "chat", "智能对话 Agent", "面向学生提供基础求职咨询和多轮对话",
                Collections.<String>emptyList(),
                "session", "none", "primary_with_fallback", 0, 60000));
    }

    private void register(AgentDefinition definition)
    {
        definitions.put(definition.getAgentType(), definition);
    }

    public AgentDefinition get(String agentType)
    {
        return definitions.get(agentType);
    }

    public List<AgentDefinition> list()
    {
        return new ArrayList<AgentDefinition>(definitions.values());
    }
}
