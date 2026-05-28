package com.ruoyi.agent.core;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import org.springframework.stereotype.Component;

/**
 * 上下文窗口管理器
 * 负责裁剪对话历史，控制 Token 预算
 *
 * @author ruoyi
 */
@Component
public class ContextWindowManager
{
    /** 默认 Token 预算 */
    private static final int DEFAULT_TOKEN_BUDGET = 4000;

    /** 最近消息默认保留数 */
    private static final int DEFAULT_RECENT_MESSAGE_LIMIT = 12;

    /**
     * 构建对话上下文列表
     *
     * @param messages 会话全部消息
     * @param session 会话信息（含摘要、Token预算、消息限制）
     * @return 裁剪后的历史消息列表
     */
    public List<ChatMessageVo> buildContext(List<ChatMessage> messages, ChatSession session)
    {
        int budget = DEFAULT_TOKEN_BUDGET;
        int limit = DEFAULT_RECENT_MESSAGE_LIMIT;
        if (session != null)
        {
            if (session.getMaxContextTokens() != null)
            {
                budget = session.getMaxContextTokens();
            }
            if (session.getRecentMessageLimit() != null)
            {
                limit = session.getRecentMessageLimit();
            }
        }

        List<ChatMessageVo> context = new ArrayList<ChatMessageVo>();

        // 注入历史摘要
        if (session != null && StringUtils.isNotEmpty(session.getContextSummary()))
        {
            ChatMessageVo summaryVo = new ChatMessageVo();
            summaryVo.setRole("system");
            summaryVo.setContent("【历史对话摘要】" + session.getContextSummary());
            context.add(summaryVo);
        }

        if (messages == null || messages.isEmpty())
        {
            return context;
        }

        // 取最近 N 条，从后往前裁剪，不超过 Token 预算
        int startIndex = Math.max(0, messages.size() - limit);
        List<ChatMessage> recent = new ArrayList<ChatMessage>(messages.subList(startIndex, messages.size()));
        int usedTokens = estimateTokensForList(context);

        for (int i = recent.size() - 1; i >= 0; i--)
        {
            ChatMessage msg = recent.get(i);
            String role = msg.getRole() != null ? msg.getRole() : "user";
            String content = msg.getContent() != null ? msg.getContent() : "";
            ChatMessageVo vo = new ChatMessageVo();
            vo.setRole(role);
            vo.setContent(content);
            int msgTokens = estimateTokens(content);
            if (usedTokens + msgTokens > budget)
            {
                break;
            }
            context.add(vo);
            usedTokens += msgTokens;
        }

        return context;
    }

    /**
     * 估算文本 Token 数（简单按字符数/2）
     *
     * @param text 文本
     * @return Token 数
     */
    public int estimateTokens(String text)
    {
        if (text == null || text.isEmpty())
        {
            return 0;
        }
        return Math.max(1, text.length() / 2);
    }

    /**
     * 估算消息列表 Token 数
     *
     * @param messages 消息列表
     * @return Token 总数
     */
    public int estimateTokensForList(List<ChatMessageVo> messages)
    {
        if (messages == null || messages.isEmpty())
        {
            return 0;
        }
        int total = 0;
        for (ChatMessageVo vo : messages)
        {
            total += estimateTokens(vo.getContent());
        }
        return total;
    }
}
