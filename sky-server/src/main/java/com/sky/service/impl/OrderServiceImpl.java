package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sky.entity.Orders.*;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/18 20:48
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBook;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;
    /**
     * 用户下单
     *
     * @param ordersSubmitDTOs
     * @return
     */
    @Override
    @Transactional   // 注意设计多表操作一定需要利用事务注解
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTOs) {
        // 处理各种异常
        // 判断地址簿是否为空
        AddressBook address = addressBook.getById(ordersSubmitDTOs.getAddressBookId());
        if(address == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        // 表示购物车为空就无法提交订单
        if(shoppingCarts == null || shoppingCarts.isEmpty()){
            throw  new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 订单表中插入一条数据
        // 构造订单对象
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTOs,orders);
        // 自己设置属性
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);  // 表示没有支付
        orders.setStatus(Orders.PENDING_PAYMENT);  // 表示待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis()));  // 表示订单号
        // 手机号
        orders.setPhone(address.getPhone());
        // 设置用户Id
        orders.setUserId(userId);
        orderMapper.insert(orders);
        // 订单明细表中插入多条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            // 向订单明细表中插入数据
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        // 清空购物车中的数据
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        // 封装 VO 对象
        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        // 推动消息
        Map<String,Object> map = new HashMap<>();
        map.put("type", 1);  // 表示消息类型: 1. 来单提醒 2. 表示用户接单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:" + outTradeNo);
        String jsonStr = JSON.toJSONString(map);
        // 推动到页面
        webSocketServer.sendToAllClient(jsonStr);  // 推送消息
    }


    /**
     *  查看历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult queryHistoryOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> orders = orderMapper.queryByUserId(BaseContext.getCurrentId());
        // 开始查询细节对象
        // 就是把每一个订单分别进行细节对象的查询
        for (OrderVO order : orders.getResult()) {
            order.setOrderDetailList(orderDetailMapper.getByOrderId(order.getId()));
        }
        // 开始封装对象返回
        PageResult pageResult = new PageResult();
        pageResult.setTotal(orders.getTotal());
        pageResult.setRecords(orders.getResult());
        return pageResult;
    }

    @Override
    public OrderVO getOrders(Long orderId) {
        OrderVO orderVO = orderMapper.queryOrderId(orderId);
        // 封装订单细节对象
        orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orderId));
        return orderVO;
    }

    @Override
    @Transactional
    public void cancelById(Long orderId) {
        // 取消订单
        orderMapper.deleteOrder(orderId);
        // 删除细节信息
        orderDetailMapper.deleteOrderDetail(orderId);
    }

    @Override
    @Transactional
    public void getSameOrder(Long orderId) {
        // 直接在order表中查出数据并且插入
        Orders orders = orderDetailMapper.getOrderById(orderId);
        orders.setId(null);
        // 插入数据
        orderMapper.insert(orders);
        // 查询细节表中的数据
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrderId(orderId);
            orderDetail.setId(null);  // 表示重新分配id
        }
        // 插入数据
        orderDetailMapper.insertBatch(orderDetails);
    }

    @Override
    public PageResult searchOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 根据条件查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersPageQueryDTO,orders);
        Page<Orders> page = orderMapper.searchOrder(orders);
        List<Orders> result = page.getResult();
        // 是否需要根据时间过滤
        LocalDateTime beginTime = ordersPageQueryDTO.getBeginTime();
        LocalDateTime endTime = ordersPageQueryDTO.getEndTime();
        if(beginTime != null && endTime != null){
            result = result.stream().filter(order -> order.getOrderTime().isBefore(endTime) && order.getOrderTime().isAfter(beginTime)).collect(Collectors.toList());
        } else if(beginTime == null && endTime != null){
            result = result.stream().filter(order -> order.getOrderTime().isAfter(beginTime)).collect(Collectors.toList());
        } else if(beginTime != null && endTime == null){
            result = result.stream().filter(order -> order.getOrderTime().isBefore(endTime)).collect(Collectors.toList());
        }
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        // 拒绝订单
        orderMapper.updateRejectedOrder(ordersRejectionDTO.getId(),ordersRejectionDTO.getRejectionReason());
    }

    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        // 确认订单
        orderMapper.updateOrderStatus(ordersConfirmDTO.getId(),CONFIRMED);
    }

    @Override
    public OrderStatisticsVO countStatus() {
        // 查询各种状态的商品
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.getConfirmed());
        orderStatisticsVO.setToBeConfirmed(orderMapper.getBeConfirmed());
        orderStatisticsVO.setDeliveryInProgress(orderMapper.getDelivery());
        return orderStatisticsVO;
    }
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        // 就是填写状态就可以了
        orderMapper.cancelOrder(ordersCancelDTO.getId(),ordersCancelDTO.getCancelReason());
    }

    @Override
    public void deliveryOrder(Long id) {
        // 首先查询订单
        Orders orders = orderMapper.getOrderById(id);
        // 设置相关信息
        orders.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        // 更新
        orders.setStatus(DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void completeOrder(Long id) {
        orderMapper.updateOrderStatus(id,COMPLETED);
    }

    /**
     * 可会催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 首先查询
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getOrderById(id);
        if(ordersDB == null){
            throw  new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 推动消息
        Map<String,Object> map = new HashMap<>();
        map.put("type", 2);  // 表示消息类型: 1. 来单提醒 2. 表示用户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:" + ordersDB.getNumber());
        String jsonStr = JSON.toJSONString(map);
        // 推动到页面
        webSocketServer.sendToAllClient(jsonStr);  // 推送消息
    }
}
