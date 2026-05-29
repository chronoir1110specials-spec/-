package com.ruoyi.agent.tool.impl;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.agent.tool.AgentTool;
import com.ruoyi.agent.tool.AgentToolContext;
import com.ruoyi.agent.tool.ToolPermissionResult;
import com.ruoyi.agent.tool.ToolValidationResult;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 岗位关键词提取工具：基于技能词典从岗位 JD 中抽取技能关键词。
 *
 * <p>采用确定性的词典匹配（不依赖模型），便于稳定演示与单元验证；
 * 复杂语义抽取可作为模型增强扩展。</p>
 *
 * @author ruoyi
 */
@Component
public class JobKeywordExtractTool implements AgentTool<JobKeywordExtractTool.Input, List<String>>
{
    /** 常见技能词典（大小写不敏感匹配） */
    private static final String[] SKILL_DICT = {
        "Java", "Spring Boot", "Spring Cloud", "Spring", "MyBatis", "MySQL", "Redis", "PostgreSQL",
        "Oracle", "Docker", "Kubernetes", "K8s", "Nacos", "Kafka", "RabbitMQ", "RocketMQ",
        "Elasticsearch", "Vue", "React", "TypeScript", "JavaScript", "Python", "Go", "Golang",
        "C++", "Linux", "Git", "Maven", "JVM", "微服务", "分布式", "高并发", "多线程", "设计模式",
        "数据结构", "算法", "网络", "操作系统", "消息队列", "缓存", "数据库", "前端", "后端",
        "机器学习", "深度学习", "数据分析", "测试", "运维", "产品", "项目管理"
    };

    @Override
    public String name()
    {
        return "JobKeywordExtractTool";
    }

    @Override
    public String description()
    {
        return "从岗位 JD 中提取技能关键词（词典匹配）";
    }

    @Override
    public Class<Input> inputType()
    {
        return Input.class;
    }

    @Override
    public ToolValidationResult validateInput(Input input, AgentToolContext context)
    {
        if (input == null || StringUtils.isEmpty(input.getJobDescription()))
        {
            return ToolValidationResult.fail("jobDescription 不能为空");
        }
        return ToolValidationResult.ok();
    }

    @Override
    public ToolPermissionResult checkPermission(Input input, AgentToolContext context)
    {
        return ToolPermissionResult.allow();
    }

    @Override
    public List<String> execute(Input input, AgentToolContext context)
    {
        String jd = input.getJobDescription().toLowerCase();
        List<String> hits = new ArrayList<String>();
        for (String skill : SKILL_DICT)
        {
            if (jd.contains(skill.toLowerCase()) && !hits.contains(skill))
            {
                hits.add(skill);
            }
        }
        return hits;
    }

    /**
     * 输入：岗位描述。
     */
    public static class Input
    {
        private String jobDescription;

        public Input()
        {
        }

        public Input(String jobDescription)
        {
            this.jobDescription = jobDescription;
        }

        public String getJobDescription()
        {
            return jobDescription;
        }

        public void setJobDescription(String jobDescription)
        {
            this.jobDescription = jobDescription;
        }
    }
}
