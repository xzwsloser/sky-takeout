package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/18 20:43
 */
@RestController("userOrderController")
@Api("用户端订单相关接口")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单接口")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单接口,{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = OrderPaymentVO.builder()
                .nonceStr("支付成功")
                .paySign("支付成功")
                .timeStamp("2024-07-20")
                .signType("rsa")
                .packageStr("110").build();
        log.info("生成预支付交易单：{}", orderPaymentVO);
        // 根据订单号查询订单
        log.info("订单号:,{}",ordersPaymentDTO.getOrderNumber());
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 查看历史订单
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查看历史订单")
    public Result<PageResult> queryHistoryOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.queryHistoryOrder(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 查询订单详细信息
     * @param orderId
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详细信息")
    public Result<OrderVO> queryDetail(@PathVariable("id")Long orderId){
        OrderVO orderVO = orderService.getOrders(orderId);
        return Result.success(orderVO);
    }


    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrders(@PathVariable("id") Long orderId){
        orderService.cancelById(orderId);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result getSameOrder(@PathVariable("id")Long userId){
        orderService.getSameOrder(userId);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单功能")
    public Result reminder(@PathVariable("id")Long id){
        orderService.reminder(id);
        return Result.success();
    }

}
