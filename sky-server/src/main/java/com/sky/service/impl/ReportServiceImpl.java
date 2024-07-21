package com.sky.service.impl;

import com.aliyuncs.http.HttpResponse;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.truncate.Truncate;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.DataLine;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {
        // 首先封装 dateList对象
        List<LocalDate> dateLists = new ArrayList<>();
        dateLists.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateLists.add(begin);
        }
        // 转化为字符串
        String dateList = StringUtils.join(dateLists, ",");
        // 获取到用户
        List<Integer> userCount = new ArrayList<>();
        for (LocalDate date : dateLists) {
            // 构建 LocalDateTime 对象
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            // 查询数据库
            Map<String,Object> map = new HashMap<>();
            map.put("end",endTime);
            Integer userSum = userMapper.getUserCount(map);
            userCount.add(userSum);
        }
        // 把数字编程字符串
        // 最后还需要获取到第一天前面一天的数据
        Map<String,Object> map = new HashMap<>();
        map.put("end",LocalDateTime.of(dateLists.get(0).plusDays(-1),LocalTime.MAX));
        Integer initCount = userMapper.getUserCount(map);
        // 开始获取到之后的所有元素
        List<Integer> newUserCount = new ArrayList<>();
        newUserCount.add(userCount.get(0) - initCount);
        for(int i = 1 ; i < userCount.size() ; i ++){
            newUserCount.add(userCount.get(i) - userCount.get(i - 1));
        }
        // 转化为字符串
        String totalUserList = StringUtils.join(userCount, ",");
        String newUserList = StringUtils.join(newUserCount,",");
        return UserReportVO.builder()
                .dateList(dateList)
                .totalUserList(totalUserList)
                .newUserList(newUserList)
                .build();
    }

    @Override
    public OrderReportVO orderReportResult(LocalDate begin, LocalDate end) {
        // 首先获取日期列表
        List<LocalDate> dateLists = new ArrayList<>();
        dateLists.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateLists.add(begin);
        }
        // 转化为字符串
        String dateList = StringUtils.join(dateLists, ",");
        // 获取每一天的有效订单数量和总订单数量
        List<Integer> totalOrder = new ArrayList<>();
        List<Integer> validOrder = new ArrayList<>();
        Integer totalSum = 0;
        Integer validSum = 0;
        for (LocalDate date : dateLists) {
            // 确定开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String,Object> map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            Integer totalCount = orderMapper.getTotalOrders(map); // 查询总订单数量
            Integer validCount = orderMapper.getValidOrders(map);  // 查询有效订单数量
            totalOrder.add(totalCount);
            validOrder.add(validCount);
            totalSum += totalCount;
            validSum += validCount;
        }
        // 变成字符串
        String totalOrders = StringUtils.join(totalOrder, ",");
        String validOrders = StringUtils.join(validOrder, ",");
        return OrderReportVO.builder()
                .dateList(dateList)
                .orderCountList(totalOrders)
                .validOrderCountList(validOrders)
                .validOrderCount(validSum)
                .totalOrderCount(totalSum)
                .orderCompletionRate((double)validSum / (double)totalSum).build();
    }

    @Override
    public SalesTop10ReportVO saleTop10Result(LocalDate begin, LocalDate end) {
        // 查询日期列表
        // 查询所有 order_id,之后根据 order_id 查询菜品排名
        Map<String,LocalDateTime> map = new HashMap<>() ;
        map.put("begin",LocalDateTime.of(begin,LocalTime.MIN));
        map.put("end",LocalDateTime.of(end,LocalTime.MAX));
        List<Integer> orderIds = orderMapper.getOrderIds(map);  // 获取到订单的id列表
        // 根据订单列表查询
        List<GoodsSalesDTO> res = orderDetailMapper.getTop10(orderIds);
        // 开始获取列表
        List<String> nameList = res.stream().map(goods -> goods.getName()).collect(Collectors.toList());
        List<Integer> numberList = res.stream().map(goods -> goods.getNumber()).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,",")).build();
    }

    /**
     *  把 excel文件下载到客户端
     * @param httpResponse
     */
    @Override
    public void exportBusinessData(HttpServletResponse httpResponse) {
        // 1. 查询数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        // 截止到昨天
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd,LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end); // 注意对象的封装
        // 2. 通过 POI把数据写入到 Excel文件中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(is);
            // 填充数据,时间
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 获取行
            XSSFRow row = sheet.getRow(1);
            // 获取单元格
            row.getCell(1).setCellValue("时间:" + dateBegin + "至" + dateEnd);
            // 获取营业额
            XSSFRow row1 = sheet.getRow(3);
            row1.getCell(2).setCellValue(businessData.getTurnover());
            // 获取订单完成率
            row1.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            // 新增用户数量
            row1.getCell(6).setCellValue(businessData.getNewUsers());
            // 获取第五行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());  // 平均单价
            // 填充明细数据
            for(int i = 0 ; i < 30 ; i ++){
                LocalDate date = dateBegin.plusDays(1);
                BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                // 进行填充
                // 获取行
                XSSFRow row2 = sheet.getRow(i + 7);
                row2.getCell(1).setCellValue(date.toString());
                row2.getCell(2).setCellValue(businessDataVO.getTurnover());
                row2.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row2.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row2.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                row2.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            ServletOutputStream out = httpResponse.getOutputStream();
            excel.write(out);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 3. 通过输出流把 Excel文件下载到客户端浏览器

    }
}
