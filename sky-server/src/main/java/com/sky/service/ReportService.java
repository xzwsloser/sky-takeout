package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/20 16:07
 */
@Service
public interface ReportService {
    TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end);

    UserReportVO userReport(LocalDate begin, LocalDate date);

    OrderReportVO orderReportResult(LocalDate begin, LocalDate end);
}
