package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.truncate.Truncate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.sampled.DataLine;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/20 16:07
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 查询总营业额
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end) {
        // 统计营业额
        StringBuilder dateListBuilder = new StringBuilder();
        // 存放日期到集合中
        List<LocalDate> dateLists = new ArrayList<>();
        dateLists.add(begin);
        // 计算指定日期的后一天对应的日期
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateLists.add(begin);
        }
        String dateList = StringUtils.join(dateLists, ",");  // 进行字符串的拼接
        // 查询营业额统计
        // sql语句
        // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
        // 时间确定,确定起始时间
        List<Double> doubles = new ArrayList<>();
        for (LocalDate date : dateLists) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 开始传入时间
            Map<String,Object> map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            // 进行转换
            turnover = turnover == null ? 0.0 : turnover;  // 防止空数据的出现
            doubles.add(turnover);
        }

        String turnOverList = StringUtils.join(doubles, ",");

        return TurnoverReportVO.builder().dateList(dateList).turnoverList(turnOverList).build();
    }
}
