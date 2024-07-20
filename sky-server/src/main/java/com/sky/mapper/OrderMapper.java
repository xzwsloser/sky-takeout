package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/18 20:48
 */
@Mapper
public interface OrderMapper {

    void insert(Orders orders);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from orders where user_id = #{userId}")
    Page<OrderVO> queryByUserId(Long userId);

    @Select("select * from orders where id = #{orderId}")
    OrderVO queryOrderId(Long orderId);

    @Delete("delete from orders where id = #{orderId}")
    void deleteOrder(Long orderId);

    Page<Orders> searchOrder(Orders orders);

    @Select("select count(*) from orders where status = 3")
    Integer getConfirmed();

    @Select("select count(*) from orders where status = 2")
    Integer getBeConfirmed();

    @Select("select count(*) from orders where status = 4")
    Integer getDelivery();


    @Update("update orders set status = #{status} where id = #{id}")
    void updateOrderStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("update orders set rejection_reason = #{rejectionReason} where id = #{id}")
    void updateRejectedOrder(@Param("id") Long id, @Param("rejectionReason") String rejectionReason);

    @Update("update orders set cancel_reason = #{cancelReason} ,status = 6 where id = #{id}")
    void cancelOrder(@Param("id")Long id, @Param("cancelReason") String cancelReason);

    @Select("select * from orders where id = #{id}")
    Orders getOrderById(Long id);

    // 直接比较时间就可以了,不用使用 @Param注解吗
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTypeLT(Integer status, LocalDateTime orderTime);

    Double sumByMap(Map<String, Object> map);

    Integer getTotalOrders(Map<String, Object> map);

    Integer getValidOrders(Map<String, Object> map);

    List<Integer> getOrderIds(Map<String, LocalDateTime> map);

}
