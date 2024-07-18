package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品,{}",dishDTO);
        dishService.saveWithFlavour(dishDTO);
        // 新增菜品之后分类中的菜品就会改变所以需要删除key
        String key = "dish:" + dishDTO.getCategoryId();
        clearCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("根据菜品名称,分类id,菜品售卖状态查询菜品")
    public Result<PageResult> getDishByInfo(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.getDishByInfo(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品的批量删除")    // 注意这里使用 @RequestParam注解就可以把字符串自动封装到 ids 中
    public Result delete(@RequestParam("ids") List<Long> ids) {
        log.info("菜品的批量删除,{}",ids);
        dishService.deleteBatch(ids);
        clearCache("dish:*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("根据id查询菜品: {}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品: {}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 删除所有缓存数据
        clearCache("dish:*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据id查询菜品")
    public Result<List<Dish>> getDishByCategoryId(@RequestParam("categoryId") Long categoryId){
        log.info("根据id查询菜品,{}",categoryId);
        List<Dish> dishes = dishService.getDishByCategoryId(categoryId);
        return Result.success(dishes);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜皮的起售和停售状态")  // id 就是 query参数
    public Result setStatus(@PathVariable("status")Integer status , Long id){
        dishService.setStatus(id,status);
        clearCache("dish:*");
        return Result.success();
    }

    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
