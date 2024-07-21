package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);


    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    void deleteByIds(List<Long> ids);

    void update(Dish dish);
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> getDishByCategoryId(Long categoryId);


    @Update("update dish set status = #{status} where id = #{id}")
    void setStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
