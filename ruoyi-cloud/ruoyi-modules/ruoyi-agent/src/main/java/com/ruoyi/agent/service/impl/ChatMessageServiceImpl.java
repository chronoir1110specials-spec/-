package com.ruoyi.agent.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.mapper.ChatMessageMapper;
import com.ruoyi.agent.service.IChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息 业务层处理
 *
 * @author ruoyi
 */
@Service
public class ChatMessageServiceImpl implements IChatMessageService
{
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage saveMessage(ChatMessage msg)
    {
        if (msg == null)
        {
            return null;
        }
        msg.setCreateTime(new Date());
        msg.setDeleted(NOT_DELETED);
        chatMessageMapper.insert(msg);
        return msg;
    }

    @Override
    public List<ChatMessage> listBySessionId(Long sessionId)
    {
        if (sessionId == null)
        {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, NOT_DELETED)
                .orderByAsc(ChatMessage::getCreateTime)
                .orderByAsc(ChatMessage::getId);
        return chatMessageMapper.selectList(queryWrapper);
    }

    @Override
    public List<ChatMessage> getRecentMessages(Long sessionId, int limit)
    {
        if (sessionId == null || limit <= 0)
        {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, NOT_DELETED)
                .orderByDesc(ChatMessage::getCreateTime)
                .orderByDesc(ChatMessage::getId)
                .last("limit " + limit);
        List<ChatMessage> messages = chatMessageMapper.selectList(queryWrapper);
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public List<ChatMessage> getMessagesSince(Long sessionId, Long sinceMessageId)
    {
        if (sessionId == null)
        {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .gt(sinceMessageId != null, ChatMessage::getId, sinceMessageId)
                .eq(ChatMessage::getDeleted, NOT_DELETED)
                .orderByAsc(ChatMessage::getCreateTime)
                .orderByAsc(ChatMessage::getId);
        return chatMessageMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteBySessionId(Long sessionId)
    {
        if (sessionId == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<ChatMessage>()
                .set(ChatMessage::getDeleted, DELETED)
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, NOT_DELETED);
        return chatMessageMapper.update(null, updateWrapper) > 0;
    }
}
