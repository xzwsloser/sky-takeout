package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 9:42
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;   // 注意 Mapper层的作用就是接受 Service 层的调用
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Transactional  // 注意需要在启动类上配置开启注解管理
    @Override
    public void saveWithFlavour(DishDTO dishDTO) {
        // 向菜品表中加入一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        // 获取主键值
        Long dishId = dish.getId();
        // 向口味表中插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            // 插入数据
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult getDishByInfo(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品的批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否可以删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE) {
                // 表示起售
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否被套餐关联
        List<Long> seatmeals = setmealDishMapper.getSetmealIdsByDishId(ids);
        if(seatmeals != null && !seatmeals.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 可以删除

        // 删除菜品数据
//        for(Long id : ids) {
//            dishMapper.deleteById(id);
//            // 删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }
        // 根据ids,批量删除数据
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据 id 查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 根据id查询口味
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorByDishId(dish.getId());
        // 封装的对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改菜品的口味信息和菜品信息
        // 统一删除原来的数据,同时重新插入数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        // 删除原来的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        // 重新导入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // id和口味数据进行绑定
        if(flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
        }
        dishFlavorMapper.insertBatch(flavors);
    }
}
