package com.ruoyi.agent.controller;

import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.service.IUserProfileService;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户画像接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/profile")
public class UserProfileController
{
    @Autowired
    private IUserProfileService userProfileService;

    @GetMapping({"/get", ""})
    public R<UserProfile> get()
    {
        return R.ok(userProfileService.getByUserId(requireCurrentUserId()));
    }

    @PostMapping({"/save", ""})
    public R<Boolean> save(@RequestBody UserProfile profile)
    {
        return R.ok(saveCurrentUserProfile(profile));
    }

    @PutMapping({"/update", ""})
    public R<Boolean> update(@RequestBody UserProfile profile)
    {
        return R.ok(saveCurrentUserProfile(profile));
    }

    private boolean saveCurrentUserProfile(UserProfile profile)
    {
        if (profile == null)
        {
            profile = new UserProfile();
        }
        UserProfile existing = userProfileService.getByUserId(requireCurrentUserId());
        profile.setUserId(requireCurrentUserId());
        profile.setId(existing == null ? null : existing.getId());
        return userProfileService.saveOrUpdate(profile);
    }

    private Long requireCurrentUserId()
    {
        Long userId;
        try
        {
            userId = SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            throw new ServiceException("当前用户未登录", HttpStatus.UNAUTHORIZED);
        }
        if (userId == null)
        {
            throw new ServiceException("当前用户未登录", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }
}
