package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

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
     *
     */
    PageResult getDishByInfo(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDTO dishDTO);
}
