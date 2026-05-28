package com.ruoyi.model.service;

import java.util.List;
import com.ruoyi.model.domain.ModelConfig;

/**
 * 模型配置 业务层
 *
 * @author ruoyi
 */
public interface IModelConfigService
{
    /**
     * 根据模型角色查询模型配置
     *
     * @param modelRole 模型角色
     * @return 模型配置
     */
    public ModelConfig getByModelRole(String modelRole);

    /**
     * 查询启用的主模型配置
     *
     * @return 模型配置
     */
    public ModelConfig getEnabledPrimary();

    /**
     * 查询启用的兜底模型配置
     *
     * @return 模型配置
     */
    public ModelConfig getEnabledFallback();

    /**
     * 查询启用的向量模型配置
     *
     * @return 模型配置
     */
    public ModelConfig getEnabledEmbedding();

    /**
     * 查询所有启用的模型配置
     *
     * @return 模型配置集合
     */
    public List<ModelConfig> listEnabled();

    /**
     * 查询所有模型配置
     *
     * @return 模型配置集合
     */
    public List<ModelConfig> listAll();

    /**
     * 保存模型配置
     *
     * @param config 模型配置
     * @return 模型配置
     */
    public ModelConfig save(ModelConfig config);

    /**
     * 根据模型角色更新模型配置
     *
     * @param config 模型配置
     * @return 结果
     */
    public boolean updateByModelRole(ModelConfig config);
}
