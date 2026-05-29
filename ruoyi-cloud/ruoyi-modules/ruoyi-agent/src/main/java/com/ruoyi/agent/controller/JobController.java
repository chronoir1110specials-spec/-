package com.ruoyi.agent.controller;

import java.util.List;
import com.ruoyi.agent.domain.JobInfo;
import com.ruoyi.agent.service.IJobInfoService;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位分析记录接口
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agent/job")
public class JobController
{
    @Autowired
    private IJobInfoService jobInfoService;

    @GetMapping("/list")
    public R<List<JobInfo>> list()
    {
        return R.ok(jobInfoService.listByUserId(requireCurrentUserId()));
    }

    @GetMapping("/{id}")
    public R<JobInfo> detail(@PathVariable Long id)
    {
        return R.ok(ensureOwner(id));
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public R<Boolean> delete(@PathVariable Long id)
    {
        ensureOwner(id);
        return R.ok(jobInfoService.delete(id));
    }

    private JobInfo ensureOwner(Long id)
    {
        JobInfo jobInfo = jobInfoService.getById(id);
        if (jobInfo == null)
        {
            throw new ServiceException("岗位分析记录不存在", HttpStatus.NOT_FOUND);
        }
        if (jobInfo.getUserId() == null || !jobInfo.getUserId().equals(requireCurrentUserId()))
        {
            throw new ServiceException("无权访问该岗位分析记录", HttpStatus.FORBIDDEN);
        }
        return jobInfo;
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
