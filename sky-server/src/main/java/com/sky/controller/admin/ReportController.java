package com.sky.controller.admin;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/20 16:04
 */
@RestController
@RequestMapping("/admin/report")
@Api("数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverReportVOResult(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                           @DateTimeFormat(pattern ="yyyy-MM-dd") LocalDate end){
        TurnoverReportVO turnoverReportVO = reportService.turnoverReport(begin,end);
        log.info("营业额数据统计,{}",turnoverReportVO);
        return Result.success(turnoverReportVO);
    }

    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userReportVOResult(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate end
    ){
        log.info("用户数据统计,{}",begin,end);
        UserReportVO userReportVO = reportService.userReport(begin, end);
        log.info("用户统计,{}",userReportVO);
        return Result.success(userReportVO);
    }


    @GetMapping("/ordersStatistics")
    @ApiOperation("订单图表相关功能")
    public Result<OrderReportVO> orderReportVOResult(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                     @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("订单图表相关功能,{},{}",begin,end);
        OrderReportVO orderReportVO = reportService.orderReportResult(begin,end);
        log.info("订单相关信息,{}",orderReportVO);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("查询销量top10")
    public Result<SalesTop10ReportVO> salesTop10ReportVOResult(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("查询销量top10,{},{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.saleTop10Result(begin, end);
        log.info("销量排名top10,{}",salesTop10ReportVO);
        return Result.success(salesTop10ReportVO);
    }
}
