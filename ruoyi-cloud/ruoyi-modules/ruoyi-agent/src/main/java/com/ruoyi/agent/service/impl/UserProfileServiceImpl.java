package com.ruoyi.agent.service.impl;

import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.agent.domain.UserProfile;
import com.ruoyi.agent.mapper.UserProfileMapper;
import com.ruoyi.agent.service.IUserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户画像 业务层处理
 *
 * @author ruoyi
 */
@Service
public class UserProfileServiceImpl implements IUserProfileService
{
    private static final Integer NOT_DELETED = 0;

    private static final Integer DELETED = 1;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .eq(UserProfile::getDeleted, NOT_DELETED)
                .last("limit 1");
        return userProfileMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean saveOrUpdate(UserProfile profile)
    {
        if (profile == null || profile.getUserId() == null)
        {
            return false;
        }
        Date now = new Date();
        profile.setUpdateTime(now);
        profile.setDeleted(NOT_DELETED);
        if (profile.getId() != null)
        {
            return userProfileMapper.updateById(profile) > 0;
        }
        UserProfile existing = getByUserId(profile.getUserId());
        if (existing != null)
        {
            profile.setId(existing.getId());
            return userProfileMapper.updateById(profile) > 0;
        }
        profile.setCreateTime(now);
        return userProfileMapper.insert(profile) > 0;
    }

    @Override
    public boolean deleteByUserId(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        LambdaUpdateWrapper<UserProfile> updateWrapper = new LambdaUpdateWrapper<UserProfile>()
                .set(UserProfile::getDeleted, DELETED)
                .set(UserProfile::getUpdateTime, new Date())
                .eq(UserProfile::getUserId, userId)
                .eq(UserProfile::getDeleted, NOT_DELETED);
        return userProfileMapper.update(null, updateWrapper) > 0;
    }
}
