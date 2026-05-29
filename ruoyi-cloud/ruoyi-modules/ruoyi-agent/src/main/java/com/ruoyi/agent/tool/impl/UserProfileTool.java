package com.ruoyi.agent.tool.impl;

import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.agent.tool.AgentTool;
import com.ruoyi.agent.tool.AgentToolContext;
import com.ruoyi.agent.tool.ToolPermissionResult;
import com.ruoyi.agent.tool.ToolValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户画像工具：读取学生画像，供动态 Prompt 注入个性化信息。
 *
 * @author ruoyi
 */
@Component
public class UserProfileTool implements AgentTool<UserProfileTool.Input, UserProfile>
{
    @Autowired
    private IUserProfileService userProfileService;

    @Override
    public String name()
    {
        return "UserProfileTool";
    }

    @Override
    public String description()
    {
        return "读取学生用户画像（专业、年级、目标岗位、技能标签等）";
    }

    @Override
    public Class<Input> inputType()
    {
        return Input.class;
    }

    @Override
    public ToolValidationResult validateInput(Input input, AgentToolContext context)
    {
        if (input == null || input.getUserId() == null)
        {
            return ToolValidationResult.fail("userId 不能为空");
        }
        return ToolValidationResult.ok();
    }

    @Override
    public ToolPermissionResult checkPermission(Input input, AgentToolContext context)
    {
        // 对象级归属校验：只能读取自己的画像
        if (context == null || context.getUserId() == null
                || !context.getUserId().equals(input.getUserId()))
        {
            return ToolPermissionResult.deny("只能读取本人画像");
        }
        return ToolPermissionResult.allow();
    }

    @Override
    public UserProfile execute(Input input, AgentToolContext context)
    {
        return userProfileService.getByUserId(input.getUserId());
    }

    /**
     * 输入：用户 ID。
     */
    public static class Input
    {
        private Long userId;

        public Input()
        {
        }

        public Input(Long userId)
        {
            this.userId = userId;
        }

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }
    }
}
