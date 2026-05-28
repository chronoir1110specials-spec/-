package com.ruoyi.agent.core;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import org.springframework.stereotype.Component;

/**
 * 动态 Prompt 构建器
 * 根据用户画像和 Agent 类型构建 System Prompt
 *
 * @author ruoyi
 */
@Component
public class PromptBuilder
{
    /** System Prompt 基础模板 */
    private static final String SYSTEM_PROMPT = "你是一位专业的大学生就业辅导顾问。请根据学生的个人情况和需求，提供有针对性的建议。";

    /**
     * 构建 System Prompt
     *
     * @param profile 用户画像（可为 null）
     * @param agentType Agent 类型
     * @return 完整 System Prompt
     */
    public String buildSystemPrompt(UserProfile profile, String agentType)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_PROMPT).append("\n\n");

        if (profile != null)
        {
            sb.append("【学生信息】\n");
            appendIfNotEmpty(sb, "学校", profile.getSchool());
            appendIfNotEmpty(sb, "专业", profile.getMajor());
            appendIfNotEmpty(sb, "年级", profile.getGrade());
            appendIfNotEmpty(sb, "目标岗位", profile.getTargetPosition());
            appendIfNotEmpty(sb, "意向城市", profile.getTargetCity());
            appendIfNotEmpty(sb, "技能标签", profile.getSkillTags());
            appendIfNotEmpty(sb, "求职阶段", profile.getJobStage());
            sb.append("\n");
        }

        if ("resume".equals(agentType))
        {
            sb.append("【当前任务】帮助学生优化简历。请分析简历内容，指出问题，给出评分和修改建议。\n");
            sb.append("请以 JSON 格式输出，字段包括：score、summary、problems、suggestions、keywords。\n");
        }
        else if ("interview".equals(agentType))
        {
            sb.append("【当前任务】进行模拟面试。根据学生目标岗位提出面试问题，对回答点评打分。\n");
        }
        else if ("job_analysis".equals(agentType))
        {
            sb.append("【当前任务】分析目标岗位 JD，提取技能要求，评估匹配度。\n");
            sb.append("请以 JSON 格式输出，字段包括：requiredSkills、bonusSkills、matchScore、resumeAdvice、interviewTopics。\n");
        }
        else if ("career_plan".equals(agentType))
        {
            sb.append("【当前任务】根据学生情况制定职业规划建议和学习路线。\n");
        }
        else if ("material".equals(agentType))
        {
            sb.append("【当前任务】生成适合学生画像和目标岗位的求职材料。\n");
        }
        else if ("knowledge_qa".equals(agentType))
        {
            sb.append("【当前任务】基于就业知识库回答政策和流程问题，注明信息来源。\n");
        }
        else
        {
            sb.append("【当前任务】回答学生的就业相关问题，提供专业建议。\n");
        }

        return sb.toString();
    }

    /**
     * 构建带 System Prompt 的消息列表
     *
     * @param profile 用户画像
     * @param agentType Agent 类型
     * @param currentMessage 当前用户消息
     * @return 消息列表
     */
    public List<ChatMessageVo> buildPrompt(UserProfile profile, String agentType, String currentMessage)
    {
        List<ChatMessageVo> messages = new ArrayList<ChatMessageVo>();
        messages.add(buildVo("system", buildSystemPrompt(profile, agentType)));
        if (currentMessage != null && !currentMessage.isEmpty())
        {
            messages.add(buildVo("user", currentMessage));
        }
        return messages;
    }

    private ChatMessageVo buildVo(String role, String content)
    {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setRole(role);
        vo.setContent(content);
        return vo;
    }

    private void appendIfNotEmpty(StringBuilder sb, String label, String value)
    {
        if (value != null && !value.trim().isEmpty())
        {
            sb.append("- ").append(label).append("：").append(value).append("\n");
        }
    }
}
