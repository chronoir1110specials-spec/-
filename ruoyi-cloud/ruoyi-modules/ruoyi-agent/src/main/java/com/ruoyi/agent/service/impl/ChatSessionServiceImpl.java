package com.ruoyi.agent.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Collections;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.mapper.ChatSessionMapper;
import com.ruoyi.agent.service.IChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 会话 业务层处理
 *
 * @author ruoyi
 */
@Service
public class ChatSessionServiceImpl implements IChatSessionService
{
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    private static final Integer DEFAULT_RECENT_MESSAGE_LIMIT = 12;

    private static final Integer DEFAULT_MAX_CONTEXT_TOKENS = 6000;

    private static final String DEFAULT_TITLE = "新会话";

    private static final String DEFAULT_AGENT_TYPE = "chat";

    private static final String DEFAULT_INTERVIEW_STATUS = "none";

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Override
    public ChatSession createSession(ChatSession session)
    {
        if (session == null)
        {
            session = new ChatSession();
        }
        Date now = new Date();
        if (session.getTitle() == null)
        {
            session.setTitle(DEFAULT_TITLE);
        }
        if (session.getAgentType() == null)
        {
            session.setAgentType(DEFAULT_AGENT_TYPE);
        }
        if (session.getSummaryVersion() == null)
        {
            session.setSummaryVersion(0);
        }
        if (session.getRecentMessageLimit() == null)
        {
            session.setRecentMessageLimit(DEFAULT_RECENT_MESSAGE_LIMIT);
        }
        if (session.getMaxContextTokens() == null)
        {
            session.setMaxContextTokens(DEFAULT_MAX_CONTEXT_TOKENS);
        }
        if (session.getInterviewStatus() == null)
        {
            session.setInterviewStatus(DEFAULT_INTERVIEW_STATUS);
        }
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setDeleted(NOT_DELETED);
        chatSessionMapper.insert(session);
        return session;
    }

    @Override
    public ChatSession getById(Long id)
    {
        if (id == null)
        {
            return null;
        }
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, id)
                .eq(ChatSession::getDeleted, NOT_DELETED)
                .last("limit 1");
        return chatSessionMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ChatSession> listByUserId(Long userId)
    {
        if (userId == null)
        {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getDeleted, NOT_DELETED)
                .orderByDesc(ChatSession::getUpdateTime)
                .orderByDesc(ChatSession::getCreateTime);
        return chatSessionMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateTitle(Long id, String title)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<ChatSession>()
                .set(ChatSession::getTitle, title)
                .set(ChatSession::getUpdateTime, new Date())
                .eq(ChatSession::getId, id)
                .eq(ChatSession::getDeleted, NOT_DELETED);
        return chatSessionMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean deleteSession(Long id)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<ChatSession>()
                .set(ChatSession::getDeleted, DELETED)
                .set(ChatSession::getUpdateTime, new Date())
                .eq(ChatSession::getId, id)
                .eq(ChatSession::getDeleted, NOT_DELETED);
        return chatSessionMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean updateSummary(Long id, String summary, Long lastMsgId, Integer version)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<ChatSession>()
                .set(ChatSession::getContextSummary, summary)
                .set(ChatSession::getLastSummaryMessageId, lastMsgId)
                .set(ChatSession::getSummaryVersion, version)
                .set(ChatSession::getUpdateTime, new Date())
                .eq(ChatSession::getId, id)
                .eq(ChatSession::getDeleted, NOT_DELETED);
        return chatSessionMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean updateInterviewStatus(Long id, String status, Integer questionIndex)
    {
        if (id == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<ChatSession>()
                .set(ChatSession::getInterviewStatus, status)
                .set(ChatSession::getCurrentQuestionIndex, questionIndex)
                .set(ChatSession::getUpdateTime, new Date())
                .eq(ChatSession::getId, id)
                .eq(ChatSession::getDeleted, NOT_DELETED);
        return chatSessionMapper.update(null, updateWrapper) > 0;
    }
}
