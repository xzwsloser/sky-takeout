package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 11:38
 */
@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishId(List<Long> ids);
}
