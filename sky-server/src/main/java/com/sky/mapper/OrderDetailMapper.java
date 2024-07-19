package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/18 20:49
 */
@Mapper
public interface OrderDetailMapper {

    void insertBatch(List<OrderDetail> orderDetails);

}
