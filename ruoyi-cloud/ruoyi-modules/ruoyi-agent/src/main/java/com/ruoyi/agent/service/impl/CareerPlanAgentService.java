package com.ruoyi.agent.service.impl;

import com.ruoyi.agent.core.PromptBuilder;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.BaseAgentService;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.model.dto.ChatRequest;
import com.ruoyi.model.dto.ChatResponse;
import com.ruoyi.model.router.ChatModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 职业规划 Agent 服务
 *
 * @author ruoyi
 */
@Service
public class CareerPlanAgentService extends BaseAgentService
{
    private static final String AGENT_TYPE = "career_plan";

    @Autowired
    private ChatModelRouter chatModelRouter;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IUserProfileService userProfileService;

    public ChatResponse generatePlan(Long userId)
    {
        UserProfile profile = userProfileService.getByUserId(userId);
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE)
                + "\n请以 JSON 格式输出，字段包括：career_path、stages、timeline、learning_resources。\n";

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent("请根据我的画像生成职业规划方案。");
        request.setHistory(buildSystemHistory(systemPrompt));
        return chatModelRouter.chat(request);
    }
}
