package com.ruoyi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.model.domain.ModelConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 模型配置 数据层
 *
 * @author ruoyi
 */
public interface ModelConfigMapper extends BaseMapper<ModelConfig>
{
    /**
     * 根据模型角色查询模型配置
     *
     * @param modelRole 模型角色
     * @return 模型配置
     */
    public ModelConfig selectByModelRole(@Param("modelRole") String modelRole);
}
