package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 16:07
 */
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper seatmealDishMapper;
    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        // 首先把 Setmeal 插入表中,但是注意需要带回主键
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 插入数据
        setmealMapper.insert(setmeal);
        // 获取id
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish dish : setmealDishes) {
            dish.setSetmealId(setmealId);
        }
        // 批量插入数据库中
        seatmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void deleteByIds(Long[] ids) {
        // 批量删除套餐,同时还需要删除 setmealDish中的菜品
        for (Long id : ids) {
            int status = setmealMapper.queryStatus(id);
            if (status == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        setmealMapper.deleteByIds(ids);
        // 批量删除关联的菜品 id
        seatmealDishMapper.deleteByIds(ids);
    }

    @Override
    public SetmealVO getSetmealVOById(Long id) {
        // 根据id查询套餐
        SetmealVO setmealVO = setmealMapper.querySetmealById(id);
        // 还需要查询菜品
        List<SetmealDish> setmealDishes = seatmealDishMapper.getDishedBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        // 首先更新 setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateSetmeal(setmeal);
        // 删除 setmeal_dish 表中相应的元素
        seatmealDishMapper.deleteById(setmeal.getId());
        // 最后更新 setmeal_dish中的元素
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDTO.getId());
        }
        seatmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void modifyStatus(Integer status, Long id) {
        // 修改套餐状态
        setmealMapper.modifyStatus(status,id);

    }
}
