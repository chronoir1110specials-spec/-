package com.ruoyi.agent.service;

import com.ruoyi.agent.domain.UserProfile;

/**
 * 用户画像 服务层
 *
 * @author ruoyi
 */
public interface IUserProfileService
{
    /**
     * 根据用户 ID 查询画像
     *
     * @param userId 用户 ID
     * @return 用户画像
     */
    UserProfile getByUserId(Long userId);

    /**
     * 新增或更新画像
     *
     * @param profile 用户画像
     * @return 结果
     */
    boolean saveOrUpdate(UserProfile profile);

    /**
     * 根据用户 ID 逻辑删除画像
     *
     * @param userId 用户 ID
     * @return 结果
     */
    boolean deleteByUserId(Long userId);
}
