package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.stereotype.Service;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/18 20:45
 */
@Service
public interface OrderService {
        /**
         *  提交订单数据
         * @param ordersSubmitDTOs
         * @return
         */
        OrderSubmitVO  submitOrder(OrdersSubmitDTO ordersSubmitDTOs);



        /**
         * 订单支付
         * @param ordersPaymentDTO
         * @return
         */
        OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

        /**
         * 支付成功，修改订单状态
         * @param outTradeNo
         */
        void paySuccess(String outTradeNo);

        /**
         * 查询历史订单
         * @param ordersPageQueryDTO
         * @return
         */
        PageResult queryHistoryOrder(OrdersPageQueryDTO ordersPageQueryDTO);

        /**
         * 查看订单详细信息
         * @param orderId
         * @return
         */
        OrderVO getOrders(Long orderId);

        /**
         * 取消订单
         * @param orderId
         */
        void cancelById(Long orderId);

        /**
         * 再来一单
         * @param userId
         */
        void getSameOrder(Long userId);

        PageResult searchOrder(OrdersPageQueryDTO ordersPageQueryDTO);

        OrderStatisticsVO countStatus();

        void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

        void rejectOrder(OrdersRejectionDTO ordersRejectionDTO);

        void cancelOrder(OrdersCancelDTO ordersCancelDTO);

        void deliveryOrder(Long id);

        void completeOrder(Long id);
}
