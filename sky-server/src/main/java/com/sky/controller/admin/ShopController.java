package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 19:49
 */
@RestController("adminShopController")
@Api("设置商铺营业状态")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;  // 注意可以操作任意类型
    private static final String KEY = "SHOP_STATUS";
    @PutMapping("/{status}")
    @ApiOperation("修改商铺营业状态")
    public Result modifyStatus(@PathVariable Integer status){
       redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
