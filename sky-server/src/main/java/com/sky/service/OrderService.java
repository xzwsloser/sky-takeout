package com.sky.service;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
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

}
