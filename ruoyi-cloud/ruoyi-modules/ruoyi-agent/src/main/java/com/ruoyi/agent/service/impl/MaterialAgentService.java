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
 * 求职材料 Agent 服务
 *
 * @author ruoyi
 */
@Service
public class MaterialAgentService extends BaseAgentService
{
    private static final String AGENT_TYPE = "material";

    @Autowired
    private ChatModelRouter chatModelRouter;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private IUserProfileService userProfileService;

    public ChatResponse generateMaterial(Long userId, String materialType)
    {
        UserProfile profile = userProfileService.getByUserId(userId);
        String systemPrompt = promptBuilder.buildSystemPrompt(profile, AGENT_TYPE) + "\n" + buildMaterialPrompt(materialType);

        ChatRequest request = new ChatRequest();
        request.setUserId(userId);
        request.setContent("请生成求职材料，材料类型：" + materialType);
        request.setHistory(buildSystemHistory(systemPrompt));
        return chatModelRouter.chat(request);
    }

    private String buildMaterialPrompt(String materialType)
    {
        if ("cover_letter".equals(materialType))
        {
            return "【当前任务】生成一封求职信，突出岗位匹配度、项目经历和求职动机。";
        }
        if ("self_intro".equals(materialType))
        {
            return "【当前任务】生成一份 1 分钟面试自我介绍，结构包括背景、能力亮点、项目成果和岗位意愿。";
        }
        return "【当前任务】生成一份简历内容草稿，结构包括教育背景、技能、项目经历、实习经历和自我评价。";
    }
}
