package com.ruoyi.agent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.agent.domain.ChatMessage;
import com.ruoyi.agent.domain.ChatSession;
import com.ruoyi.agent.service.IChatMessageService;
import com.ruoyi.agent.service.IChatSessionService;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatRequest.ChatMessageVo;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会话管理接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agent/session")
public class ChatSessionController
{
    private static final String ROLE_USER = "user";

    private static final String ROLE_ASSISTANT = "assistant";

    @Autowired
    private IChatSessionService chatSessionService;

    @Autowired
    private IChatMessageService chatMessageService;

    @Autowired
    private ChatModelRouter chatModelRouter;

    /**
     * 创建会话
     */
    @PostMapping("/create")
    public R<ChatSession> create(@RequestBody(required = false) ChatSession session)
    {
        if (session == null)
        {
            session = new ChatSession();
        }
        Long userId = getCurrentUserId(session.getUserId());
        if (userId != null)
        {
            session.setUserId(userId);
        }
        if (session.getUserId() == null)
        {
            return R.fail("用户 ID 不能为空");
        }
        return R.ok(chatSessionService.createSession(session));
    }

    /**
     * 用户会话列表
     */
    @GetMapping("/list")
    public R<List<ChatSession>> list(@RequestParam(value = "userId", required = false) Long userId)
    {
        return R.ok(chatSessionService.listByUserId(getCurrentUserId(userId)));
    }

    /**
     * 会话详情（含消息列表）
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id)
    {
        ChatSession session = chatSessionService.getById(id);
        if (session == null)
        {
            return R.fail("会话不存在");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("session", session);
        result.put("messages", chatMessageService.listBySessionId(id));
        return R.ok(result);
    }

    /**
     * 修改标题
     */
    @PutMapping("/{id}/title")
    public R<Boolean> updateTitle(@PathVariable Long id, @RequestBody ChatSession session)
    {
        return R.ok(chatSessionService.updateTitle(id, session == null ? null : session.getTitle()));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id)
    {
        boolean result = chatSessionService.deleteSession(id);
        chatMessageService.deleteBySessionId(id);
        return R.ok(result);
    }

    /**
     * 查询会话消息
     */
    @GetMapping("/{id}/messages")
    public R<List<ChatMessage>> messages(@PathVariable Long id)
    {
        return R.ok(chatMessageService.listBySessionId(id));
    }

    /**
     * 发消息并返回 AI 回复
     */
    @PostMapping("/{id}/messages")
    public R<ChatResponse> sendMessage(@PathVariable Long id, @RequestBody ChatRequest request)
    {
        ChatSession session = chatSessionService.getById(id);
        if (session == null)
        {
            return R.fail("会话不存在");
        }
        if (request == null || StringUtils.isEmpty(request.getContent()))
        {
            return R.fail("消息内容不能为空");
        }

        Long userId = getCurrentUserId(request.getUserId());
        if (userId == null)
        {
            userId = session.getUserId();
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(id);
        userMessage.setUserId(userId);
        userMessage.setRole(ROLE_USER);
        userMessage.setContent(request.getContent());
        chatMessageService.saveMessage(userMessage);

        int limit = session.getRecentMessageLimit() == null ? 12 : session.getRecentMessageLimit();
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(id, limit);
        request.setSessionId(String.valueOf(id));
        request.setUserId(userId);
        request.setHistory(toHistory(recentMessages, userMessage.getId()));

        ChatResponse response = chatModelRouter.chat(request);
        if (response != null && StringUtils.isNotEmpty(response.getContent()))
        {
            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setSessionId(id);
            assistantMessage.setUserId(userId);
            assistantMessage.setRole(ROLE_ASSISTANT);
            assistantMessage.setContent(response.getContent());
            assistantMessage.setModelName(response.getModelName());
            chatMessageService.saveMessage(assistantMessage);
        }
        return R.ok(response);
    }

    /**
     * 转换历史消息，排除当前请求消息，避免重复拼接。
     */
    private List<ChatMessageVo> toHistory(List<ChatMessage> messages, Long currentMessageId)
    {
        List<ChatMessageVo> history = new ArrayList<ChatMessageVo>();
        if (messages == null)
        {
            return history;
        }
        for (ChatMessage message : messages)
        {
            if (message == null || (currentMessageId != null && currentMessageId.equals(message.getId())))
            {
                continue;
            }
            if (StringUtils.isEmpty(message.getRole()) || StringUtils.isEmpty(message.getContent()))
            {
                continue;
            }
            ChatMessageVo item = new ChatMessageVo();
            item.setRole(message.getRole());
            item.setContent(message.getContent());
            history.add(item);
        }
        return history;
    }

    /**
     * 优先使用登录用户，未登录调用时使用请求中的用户 ID。
     */
    private Long getCurrentUserId(Long fallbackUserId)
    {
        try
        {
            Long userId = SecurityUtils.getUserId();
            return userId == null ? fallbackUserId : userId;
        }
        catch (Exception e)
        {
            return fallbackUserId;
        }
    }
}
