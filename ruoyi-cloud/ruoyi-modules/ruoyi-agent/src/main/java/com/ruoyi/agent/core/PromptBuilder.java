package com.ruoyi.agent.core;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.model.api.dto.ChatRequest.ChatMessageVo;
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

    /** 简历优化时必须遵循的针对应届生/初级程序员的核心准则 */
    private static final String RESUME_GUIDELINES =
            "【简历优化准则（务必严格遵循，并据此审查与改写简历）】\n"
            + "一、技能描述用词要克制。\n"
            + "  - 应届生或刚毕业的程序员切忌写\"精通\"；学得好也建议写\"熟悉\"。\n"
            + "  - 词语强烈程度排序：精通 > 熟悉（推荐）> 掌握（推荐）> 了解（推荐）。\n"
            + "  - 不要把只用过或只懂语法的语言并列写成\"熟悉C++、Java、Go、Python\"。"
            + "应区分掌握程度，例如\"熟悉C++，了解Java、Go、Python\"。\n"
            + "  - 提醒学生：写\"熟悉某语言\"就意味着它会成为面试重点。例如写熟悉C++，"
            + "继承、多态、封装、虚函数、C++11特性、STL 几乎必问；要重点准备所写的那门语言。\n"
            + "二、拿不准的绝对不要写。\n"
            + "  - 不要为了显得丰富而堆砌内容，内容越多面试考点越多。突出几个技能亮点，而非面面俱到。\n"
            + "  - 仅部署过 Nginx 就写\"熟悉Nginx\"是大忌，面试官会就底层实现深问导致尴尬。\n"
            + "  - 不要写\"代码行数10万+\"这类无从考证又抬高面试官期望、放大提问范围的表述。\n";

    /** 简历优化准则补充：项目经验与博客 */
    private static final String RESUME_GUIDELINES_PROJECT =
            "三、项目经验要突出个人贡献与难点。\n"
            + "  - 不要只复述项目，要说清自己添加了哪些功能、优化了哪些性能指标，以及收益"
            + "（如被多少人使用、性能提升多少倍）。\n"
            + "  - 主动提炼并写出 1-2 个可控的项目难点（如分布式数据一致性），引导面试官往自己最熟悉的领域提问，"
            + "变被动为主动；并提示学生提前精心准备这些难点的原理与解决方案。\n"
            + "  - 若学生自称项目没难点，应引导其在某个技术点上深挖（如 Java 内存管理、减小 JVM 内存压力）找到可讲的难点。\n"
            + "四、鼓励补充博客/GitHub 等技术沉淀链接（CSDN、博客园均可），"
            + "高质量博客和 GitHub 能让面试官快速判断技术热情与基础，现场发挥不佳时也能加分。\n";

    /** 面向国企/体制内（SOE）目标时的简历改写策略：核心逻辑与用词、视角 */
    private static final String RESUME_GUIDELINES_SOE =
            "【国企/体制内（SOE）简历改写策略（仅当学生目标单位为国企、事业单位、央企、政府或体制内时启用，"
            + "此时优先于上面的互联网技术逻辑）】\n"
            + "核心逻辑：从\"技术实现\"转向\"业务价值\"。互联网逻辑讲\"用了什么技术、解决了多难的问题、提升了多少性能\"；"
            + "SOE 逻辑讲\"参与了什么级别的项目（高度）、保障了什么业务稳定性（安全）、为集体做出了什么支撑（贡献）\"。\n"
            + "一、动词行政化升级（用宏大、正式、体现管理思维的词）：\n"
            + "  - 写了/开发了 → 构建、建设、实施、推进（从\"干活的\"变\"建设者\"）。\n"
            + "  - 使用了/用了 → 引入、采用、依托（强调技术选型的决策过程）。\n"
            + "  - 修好了/解决了 → 保障、支撑、完善、优化（强调长期维护价值）。\n"
            + "  - 对接/联调 → 统筹、协调、规划（强调沟通组织能力）。\n"
            + "  - 查Bug/监控 → 监测、研判、防控（强调风险意识）。\n"
            + "二、视角宏观化（盯系统和业务，而非代码）：\n"
            + "  - 例：\"用 RabbitMQ 死信队列处理流量峰值\" → \"搭建资源调度系统，保障重大活动期间业务连续性与资源分配公平性\"。\n"
            + "  - SOE 看重\"公平\"\"稳定\"\"连续性\"。\n";

    /** 面向国企/体制内（SOE）目标时的简历改写策略：合规关键词、软实力与综合素质 */
    private static final String RESUME_GUIDELINES_SOE_DETAIL =
            "三、关键词正确与安全合规（主动靠拢行业重点方向）：\n"
            + "  - 安全可控：强调\"自主可控\"\"国产化替代\"\"内网安全\"\"物理隔离\"。"
            + "例：把 Docker 隔离描述为\"确保系统运行自主可控\"。\n"
            + "  - 风险防控：强调\"应急预案\"\"熔断机制\"\"预警平台\"。例：把\"异常处理\"包装为\"风险监测预警平台\"。\n"
            + "  - 常用拔高词：\"统一\"（如统一身份认证体系）、\"分级分类管理\"、\"辅助决策\"、\"公共安全专项\"、\"标准化处置流程\"。\n"
            + "四、软实力组织化呈现（SOE 要\"听指挥、能写文、会来事\"的人）：\n"
            + "  - 学生工作别只写头衔，要写\"统筹\"\"组织\"\"联防联控\"\"思想教育\"等组织管理动作。\n"
            + "  - 有公文写作、新闻稿撰写经验一定写上。\n"
            + "五、综合素质与门槛：奖学金、三好学生、优秀干部等荣誉务必列出；英语四六级是很多 SOE 硬门槛，一定要写。\n"
            + "六、面试人设提醒：保持\"大局观\"，被问技术细节时先讲技术原理，紧接着补充\"如何保障系统稳定性\"或\"如何降低运维成本\"。\n";


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
            sb.append(RESUME_GUIDELINES);
            sb.append(RESUME_GUIDELINES_PROJECT);
            sb.append(RESUME_GUIDELINES_SOE);
            sb.append(RESUME_GUIDELINES_SOE_DETAIL);
            sb.append("请先根据学生的目标岗位/目标单位判断属于\"互联网/技术公司\"还是\"国企/体制内（SOE）\"方向：");
            sb.append("技术公司方向按前面互联网逻辑（突出技术深度、贡献、性能数据）优化；");
            sb.append("国企/体制内方向按 SOE 策略（业务价值、行政化用词、宏观视角、安全合规）改写。无法判断时默认按技术公司方向，并提示学生补充目标单位。\n");
            sb.append("请以 JSON 格式输出，字段包括：score、summary、problems、suggestions、keywords。\n");
            sb.append("其中 problems 要逐条列出违反上述原则的地方（如滥用\"精通\"、技能堆砌、夸大表述、项目只描述未突出贡献等）；");
            sb.append("suggestions 要给出可直接替换的改写示例，并对每个声称\"熟悉\"的技能补充对应的面试高频考点提醒。\n");
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
