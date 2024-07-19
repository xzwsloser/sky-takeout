# 用户端历史订单模块
- 查询历史订单
- 查询订单详情
- 取消订单
- 再来一单


- 历史订单查询:
  - Path:  /user/order/historyOrders
  - Method: GET
  - 传输对象: OrdersPageQueryDTO
  - 返回对象： PageResult(recoreds = OrderVO)

- 查询订单详情:
  - Path:  /user/order/orderDetail/{id}
  - Method: GET
  - 传输数据: 订单 id
  - 返回数据: OrdersVO对象

- 取消订单
  - Path:  /user/order/cancel/{id}
  - Method: PUT 
  - 传输对象: id
  - 返回数据: 无

- 再来一单:
  - Path:  /user/order/repetition/{id}
  - 方式:  POST
  - 对象: orderId
  - 返回对象： 无 
  - 注意插入数据之前设置id为null

# 商家端订单管理模块

商家端订单管理模块：

- 订单搜索
- 各个状态的订单数量统计
- 查询订单详情
- 接单
- 拒单
- 取消订单
- 派送订单
- 完成订单

- 订单搜索:
  - Path:  /admin/order/conditionSearch
  - 方式: GET
  - 传输对象: OrderPageQueryDTO
  - 返回对象: PageResult(records = Orders)
- 各个状态订单数量统计:
  - Path: /admin/order/statistics
  - Method: GET
  - 传输对象： 无
  - 返回对象: OrderStatisticsVO
- 查询订单详情
  - Path: /admin/order/details/{id}
  - Method: GET
  - 直接调用之前的接口
- 接单:
  - Path:  /admin/order/confirm
  - Method: PUT
  - 传输对象:  OrdersConfirmDTO
- 拒单:
  - Path: /admin/order/rejection
  - Method: PUT 
  - 传输对象: OrdersRejectionDTO
  - 返回结果: 无
- 取消订单
  - Path:  /admin/order/cancel
  - Method: PUT
  - 传输对象 OrderCancelDTO
  - 返回结果: 无
- 派送订单: 
  - Path: /admin/order/delivery/{id}
  - Method: PUT
- 完成订单：
  - Path:  /admin/order/complete/{id}
  - Method: PUT
  
