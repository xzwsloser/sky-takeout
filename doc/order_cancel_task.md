# Spring Task 
- Spring Task是Spring框架提供的任务调度工具,可以按照约定的时间自动执行某一个代码逻辑
- 作用: 定时执行某一段 java 代码
- 应用场景:
  - 信用卡每月还款提醒
  - 银行贷款每月还款提醒
  - 火车票处理没有支付的订单
  - 入职纪念日给用户发送通知
- cron表达式:
  - 本质上就是一个字符串，通过 cron 表达式可以定义任务出发时间
  - 构成分析: 分为 6个或者7个域,又空格分隔开,每一个域代表一个含义
  - 每一个域含义分别为: 秒,分钟,小时,日,月,周,年(可选)等
比如 2024年7月19日20点整: 0 0 20 19 7 ? 2024
  - cron表达式在线生成器:   https://cron.qqe2.com/
- Spring Task的依赖存在于 Spring-context中
- 在启动类中加入开启注解@EnableScheduling开启任务调度
- 自定义定时任务类
# 订单状态定时处理
- 下单没有支付,订单一致处于待支付状态
- 用户收货之后管理端没有点击完成按钮,订单一直处于派送状态
- 定时任务可以一分钟检查一次是否处于待支付状态
- 通过定时任务每天凌晨1点检查一次是否存在派送中的订单,如果存在就可以修改订单状态为 "已完成"
- 代码演示如下:
```java
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? *")  // 表示每分钟出发一次
    public void processTimeOutOrder(){
        log.info("定时触发超时订单,{}", LocalDateTime.now());
        // 查询订单状态
        // 下单时间超过 15 min,并且没有支付
        List<Orders> orders = orderMapper.getByStatusAndOrderTypeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(orders != null && !orders.isEmpty()){
            // 遍历集合进行处理
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时,自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    // 处理这一种在派送中的订单,定时处理处于派送中的订单,注意表达式如何处理,就可以直接使用
    @Scheduled(cron = "0 0 1 * * ? *")
    public void processDeliveryOrder(){
        log.info("定时处理定时中的订单,{}",LocalDateTime.now());
        // 上一个工作日的订单
        List<Orders> orders = orderMapper.getByStatusAndOrderTypeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusHours(-1));
        for (Orders order : orders) {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        }
    }
}
```
# WebSocket
- WebSocket是基于 TCP 的一种新的网络协议,实现了浏览器和服务器全双工通信,浏览器和服务器之间只用完成一次握手就可以创建持久性的连接,并且进行双向数据传递
- WebSocket 和 Http 协议的时序图对比
![Screenshot_20240720_095640_tv.danmaku.bilibilihd.jpg](img%2FScreenshot_20240720_095640_tv.danmaku.bilibilihd.jpg)
- Http协议和WebSocket协议对比:
  - Http是短连接
  - WebSocket是长连接
  - Http通信是单向的,基于请求响应模式
  - WebSocket支持双向通信
  - Http和WebSocket底层都是TCP连接
- WebSocket的应用场景:
  - 弹幕
  - 网页聊天
  - 实况更新(页面没有刷新(没有发送请求),但是数据可以发生变化)
- 浏览器和服务器双向的发送消息
## WebSocket的使用
- 一般浏览器就是客户端,浏览器请求服务器就是握手的过程,这一个过程可以建立客户端和服务器端的连接
- 注意需要导入组件WebSocketServer,还需要配置相关的配置类
- 可以导入定时任务类WebSocketTask,定时向客户端推送数据
- 代码可以参考:    [WebSocketServer.java](..%2Fsky-server%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsky%2Fwebsocket%2FWebSocketServer.java)
## 来单提醒
- 需求分析和设计:
  - 通知的形式: 语音播报
- 设计:
  - 通过WebSocket实现管理端页面和服务端页面保持长连接状态
  - 当客户支付之后,调用WebSocket的相关API实现服务端向客户都安推送消息
  - 客户都安浏览器解析服务端推送的消息之后,判断是来单提醒还是客户催单,进行相应的消息提示和语音播报
  - 约定服务端发送客户端浏览器的数据格式为 JSON,字段包括: type,orderId,content
    - type 为消息类型
    - orderId 订单id
    - content 消息内容
- 谷歌浏览器没有声音,是否是没有打开声音权限
# 客户催单
## 需求设计
- 需求设计和上面一致,就是消息的类型不同
- 接口设计:
  - Path: /user/order/reminder/{id}
  - Method: GET
