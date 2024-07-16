package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
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

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteByIds(Long[] ids);

    List<SetmealDish> getDishedBySetmealId(Long id);
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteById(Long id);
}
