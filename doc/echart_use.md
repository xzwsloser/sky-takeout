# Apache EChart
## Apache EChart介绍
- 一种基于 js 的数据可视化图形库 https://echarts.apache.org/zh/index.html  
## 入门案例
- 一般是后端进行数据统计把数据返回给前端,前端进行数据的展示
# 营业额统计
## 需求分析和设计
- 基于折线图展现,主要展示每一天的营业额,可以通过时间选择器进行时间的选择
- 业务规则:
  - 营业额表示订单状态为已经完成的订单金额合计
  - X轴表示时间,Y轴表示营业额,X轴展示的日期由时间选择器决定
- 接口设计：
  - Method: GET
  - 传递的数据: 开始日期和结束日期
  - Path: /admin/report/turnoverStatistics
  - 返回数据: 日期列表和营业额列表
## 代码实现
- 最主要的就是如何写 sql,如何获取一天的开始和结束的时间
```java
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
```
```sql
    <select id="sumByMap" resultType="java.lang.Double">
        select sum(amount) from orders
        <where>
            <if test="begin != null">
<!--                  注意从map中取出元素的方式-->
                and order_time &gt; #{begin}
            </if>
            <if test="end != null">
                and order_time &lt; #{end}
            </if>
            <if test="status != null">
                and status = #{status}
            </if>
        </where>
    </select>
```
