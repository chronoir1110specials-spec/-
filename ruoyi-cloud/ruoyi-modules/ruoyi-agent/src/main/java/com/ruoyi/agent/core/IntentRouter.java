package com.ruoyi.agent.core;

import java.util.LinkedHashMap;
import java.util.Map;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 意图识别路由（设计 8.4）。
 *
 * <p>基于关键词规则把用户输入分类到具体 Agent 类型，供统一入口自动调度。
 * 采用确定性规则实现（可解释、可演示、零额外模型成本），低置信回退通用对话；
 * 后续可平滑替换为模型分类。</p>
 *
 * @author ruoyi
 */
@Component
public class IntentRouter
{
    /** 各 agentType 的关键词命中表（按优先级顺序） */
    private static final Map<String, String[]> INTENT_KEYWORDS = new LinkedHashMap<String, String[]>();

    static
    {
        INTENT_KEYWORDS.put("resume_optimize", new String[] {
            "简历", "履历", "resume", "cv", "自我介绍怎么写", "项目经历", "优化简历", "简历怎么" });
        INTENT_KEYWORDS.put("job_analyze", new String[] {
            "岗位", "职位", "jd", "招聘要求", "任职要求", "这个工作", "岗位要求", "匹配度", "适合这个" });
        INTENT_KEYWORDS.put("interview", new String[] {
            "面试", "面经", "模拟面试", "面试官", "八股", "手撕", "面试题", "怎么回答" });
        INTENT_KEYWORDS.put("knowledge_qa", new String[] {
            "三方协议", "报到证", "户口", "档案", "派遣", "政策", "应届生身份", "违约金", "落户", "社保", "公积金" });
    }

    /** 默认兜底意图 */
    private static final String DEFAULT_INTENT = "chat";

    /**
     * 识别用户输入的意图。
     *
     * @param text 用户输入
     * @return 命中的 agentType，未命中返回 chat
     */
    public IntentResult detect(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return new IntentResult(DEFAULT_INTENT, 0, "空输入，默认通用对话");
        }
        String lower = text.toLowerCase();
        String bestIntent = DEFAULT_INTENT;
        int bestHits = 0;
        String matchedKeyword = null;
        for (Map.Entry<String, String[]> entry : INTENT_KEYWORDS.entrySet())
        {
            int hits = 0;
            String firstHit = null;
            for (String kw : entry.getValue())
            {
                if (lower.contains(kw.toLowerCase()))
                {
                    hits++;
                    if (firstHit == null)
                    {
                        firstHit = kw;
                    }
                }
            }
            if (hits > bestHits)
            {
                bestHits = hits;
                bestIntent = entry.getKey();
                matchedKeyword = firstHit;
            }
        }
        if (bestHits == 0)
        {
            return new IntentResult(DEFAULT_INTENT, 0, "未命中关键词，回退通用对话");
        }
        // 简易置信度：命中关键词数封顶到 1.0（1 个=0.6，2 个=0.8，3+=0.95）
        double confidence = bestHits >= 3 ? 0.95 : (bestHits == 2 ? 0.8 : 0.6);
        return new IntentResult(bestIntent, confidence, "命中关键词「" + matchedKeyword + "」等 " + bestHits + " 项");
    }

    /**
     * 意图识别结果。
     */
    public static class IntentResult
    {
        private final String agentType;

        private final double confidence;

        private final String reason;

        public IntentResult(String agentType, double confidence, String reason)
        {
            this.agentType = agentType;
            this.confidence = confidence;
            this.reason = reason;
        }

        public String getAgentType()
        {
            return agentType;
        }

        public double getConfidence()
        {
            return confidence;
        }

        public String getReason()
        {
            return reason;
        }
    }
}
