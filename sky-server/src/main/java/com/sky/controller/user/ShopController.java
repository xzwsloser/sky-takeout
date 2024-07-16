package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/16 19:56
 */
@RestController("userShopController")  // 防止Bean的名称冲突
@Api("店铺相关操作")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String KEY = "SHOP_STATUS";
    @GetMapping("/status")
    @ApiOperation("查询用户营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
