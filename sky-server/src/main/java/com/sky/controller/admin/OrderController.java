package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/19 16:26
 */
@RestController("adminOrderController")
@Api("管理端订单模块")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索模块")
    public Result<PageResult> searchOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.searchOrder(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("统计各种数量的商品")
    public Result<OrderStatisticsVO> countStatus() {
        OrderStatisticsVO orderStatisticsVO = orderService.countStatus();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详细信息
     *
     * @param orderId
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详细信息")
    public Result<OrderVO> queryDetail(@PathVariable("id") Long orderId) {
        OrderVO orderVO = orderService.getOrders(orderId);
        return Result.success(orderVO);
    }


    @PutMapping("/confirm")
    @ApiOperation("商铺接单")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("商家拒单接口")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单接口")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("配送订单")
    public Result deliveryOrder(@PathVariable("id") Long id){
        orderService.deliveryOrder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result completeOrder(@PathVariable("id")Long id){
        orderService.completeOrder(id);
        return Result.success();
    }
}
