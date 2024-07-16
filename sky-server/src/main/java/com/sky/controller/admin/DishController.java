package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xzw
 * @version 1.0
 * @Description 菜品相关结果
 * @Date 2024/7/16 9:38
 */
@RestController
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品,{}",dishDTO);
        dishService.saveWithFlavour(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("根据菜品名称,分类id,菜品售卖状态查询菜品")
    public Result<PageResult> getDishByInfo(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.getDishByInfo(dishPageQueryDTO);
        return Result.success(pageResult);
    }
}
