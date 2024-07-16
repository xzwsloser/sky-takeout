package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 9:40
 */
@Service
public interface DishService {
    /**
     * 新增菜品和口味信息
     * @param dishDTO
     */
    public void saveWithFlavour(DishDTO dishDTO);

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @param categoryId
     * @param status
     */
    PageResult getDishByInfo(DishPageQueryDTO dishPageQueryDTO);
}
