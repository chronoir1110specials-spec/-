package com.ruoyi.model.service;

import com.ruoyi.model.dto.ModelTestResult;

/**
 * 模型连通性测试 业务层
 *
 * @author ruoyi
 */
public interface IModelTestService
{
    /**
     * 测试主模型连通性
     *
     * @return 测试结果
     */
    public ModelTestResult testPrimary();

    /**
     * 测试兜底模型连通性
     *
     * @return 测试结果
     */
    public ModelTestResult testFallback();
}
