package com.ruoyi.agent.service;

import java.util.List;
import com.ruoyi.agent.domain.ChatSession;

/**
 * 会话 服务层
 *
 * @author ruoyi
 */
public interface IChatSessionService
{
    ChatSession createSession(ChatSession session);

    ChatSession getById(Long id);

    List<ChatSession> listByUserId(Long userId);

    boolean updateTitle(Long id, String title);

    boolean deleteSession(Long id);

    boolean updateSummary(Long id, String summary, Long lastMsgId, Integer version);

    boolean updateInterviewStatus(Long id, String status, Integer questionIndex);

    boolean updateInterviewSummary(Long id, String status, Integer score, String summary);
}
