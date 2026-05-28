package com.ruoyi.model.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础对话接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/model/chat")
public class ChatController
{
    @Autowired
    private ChatModelRouter chatModelRouter;

    /**
     * 基础对话接口（非流式）
     */
    @PostMapping("/send")
    public R<ChatResponse> send(@RequestBody ChatRequest request)
    {
        ChatResponse response = chatModelRouter.chat(request);
        return R.ok(response);
    }
}
