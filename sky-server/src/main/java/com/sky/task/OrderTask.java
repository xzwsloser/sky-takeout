package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/19 22:15
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ")  // 表示每分钟出发一次
    public void processTimeOutOrder(){
        log.info("定时触发超时订单,{}", LocalDateTime.now());
        // 查询订单状态
        // 下单时间超过 15 min,并且没有支付
        List<Orders> orders = orderMapper.getByStatusAndOrderTypeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(orders != null && !orders.isEmpty()){
            // 遍历集合进行处理
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时,自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    // 处理这一种在派送中的订单,定时处理处于派送中的订单,注意表达式如何处理,就可以直接使用
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        log.info("定时处理定时中的订单,{}",LocalDateTime.now());
        // 上一个工作日的订单
        List<Orders> orders = orderMapper.getByStatusAndOrderTypeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusHours(-1));
        for (Orders order : orders) {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        }
    }
}
