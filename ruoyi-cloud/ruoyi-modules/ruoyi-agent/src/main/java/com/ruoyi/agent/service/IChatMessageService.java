package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.ChatMessage;

/**
 * 消息 服务层
 *
 * @author ruoyi
 */
public interface IChatMessageService
{
    ChatMessage saveMessage(ChatMessage msg);

    List<ChatMessage> listBySessionId(Long sessionId);

    List<ChatMessage> getRecentMessages(Long sessionId, int limit);

    List<ChatMessage> getMessagesSince(Long sessionId, Long sinceMessageId);

    boolean deleteBySessionId(Long sessionId);
}
