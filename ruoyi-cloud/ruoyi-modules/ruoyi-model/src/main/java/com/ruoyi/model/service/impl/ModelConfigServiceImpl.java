package com.ruoyi.model.service.impl;

import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.model.domain.ModelConfig;
import com.ruoyi.model.mapper.ModelConfigMapper;
import com.ruoyi.model.service.IModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 模型配置 业务层处理
 *
 * @author ruoyi
 */
@Service
public class ModelConfigServiceImpl implements IModelConfigService
{
    private static final Integer ENABLED = 1;

    private static final Integer NOT_DELETED = 0;

    private static final String MODEL_ROLE_PRIMARY = "primary";

    private static final String MODEL_ROLE_FALLBACK = "fallback";

    private static final String MODEL_ROLE_EMBEDDING = "embedding";

    @Autowired
    private ModelConfigMapper modelConfigMapper;

    /**
     * 根据模型角色查询模型配置
     *
     * @param modelRole 模型角色
     * @return 模型配置
     */
    @Override
    public ModelConfig getByModelRole(String modelRole)
    {
        return modelConfigMapper.selectByModelRole(modelRole);
    }

    /**
     * 查询启用的主模型配置
     *
     * @return 模型配置
     */
    @Override
    public ModelConfig getEnabledPrimary()
    {
        return getEnabledByModelRole(MODEL_ROLE_PRIMARY);
    }

    /**
     * 查询启用的兜底模型配置
     *
     * @return 模型配置
     */
    @Override
    public ModelConfig getEnabledFallback()
    {
        return getEnabledByModelRole(MODEL_ROLE_FALLBACK);
    }

    /**
     * 查询启用的向量模型配置
     *
     * @return 模型配置
     */
    @Override
    public ModelConfig getEnabledEmbedding()
    {
        return getEnabledByModelRole(MODEL_ROLE_EMBEDDING);
    }

    /**
     * 查询所有启用的模型配置
     *
     * @return 模型配置集合
     */
    @Override
    public List<ModelConfig> listEnabled()
    {
        LambdaQueryWrapper<ModelConfig> queryWrapper = new LambdaQueryWrapper<ModelConfig>()
                .eq(ModelConfig::getEnabled, ENABLED)
                .eq(ModelConfig::getDeleted, NOT_DELETED)
                .orderByAsc(ModelConfig::getId);
        return modelConfigMapper.selectList(queryWrapper);
    }

    /**
     * 根据模型角色更新模型配置
     *
     * @param config 模型配置
     * @return 结果
     */
    @Override
    public boolean updateByModelRole(ModelConfig config)
    {
        if (config == null || config.getModelRole() == null)
        {
            return false;
        }
        LambdaUpdateWrapper<ModelConfig> updateWrapper = new LambdaUpdateWrapper<ModelConfig>()
                .eq(ModelConfig::getModelRole, config.getModelRole())
                .eq(ModelConfig::getDeleted, NOT_DELETED);
        return modelConfigMapper.update(config, updateWrapper) > 0;
    }

    /**
     * 根据模型角色查询启用的模型配置
     *
     * @param modelRole 模型角色
     * @return 模型配置
     */
    private ModelConfig getEnabledByModelRole(String modelRole)
    {
        LambdaQueryWrapper<ModelConfig> queryWrapper = new LambdaQueryWrapper<ModelConfig>()
                .eq(ModelConfig::getModelRole, modelRole)
                .eq(ModelConfig::getEnabled, ENABLED)
                .eq(ModelConfig::getDeleted, NOT_DELETED)
                .last("limit 1");
        return modelConfigMapper.selectOne(queryWrapper);
    }
}
