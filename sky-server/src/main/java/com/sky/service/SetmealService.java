package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 16:06
 */
@Service
public interface SetmealService {
    void addSetmeal(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteByIds(Long[] ids);

    SetmealVO getSetmealVOById(Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    void modifyStatus(Integer status, Long id);
}
