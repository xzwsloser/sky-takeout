# 微信支付相关代码
## 地址簿
- 类似于淘宝的设置地址的功能,需要实现查询收货地址,设置默认地址,查询默认地址等功能
- 业务功能:
  - 查询地址列表
  - 新增地址
  - 修改地址
  - 删除地址
  - 设置默认地址
  - 查询默认地址(生成订单就会使用到)
- 相关表: address_book 表
## 用户下单
### 业务说明
- 电商系统中,用户通过下单的方式通知商家,需要商家进行备货和发货
- 接口设计:
  - 方式: POST 
  - 路径： /user/order/submit
  - 参数: 地址簿,配送状态,打包费,备注和参数数量
  - 返回结果: 下单时间,订单总金额,订单号,订单id等信息
- 数据库设计:
  - 订单表: orders
  - 订单明细表: order_tail
- 注意冗余字段的作用,为了更加直接的展示数据,还是注意设计原则(不同的业务场景下需要什么样子的功能就可以进行什么样子的开发):
  - 前端传递过来的数据封装成 DTO 对象
  - 后端返回给前端的数据可以封装成 VO 对象
  - 和数据库相关的字段可以封装成 entity 对象
- 数据传输对象设计:
  - DTO:  [OrdersSubmitDTO.java](..%2Fsky-pojo%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsky%2Fdto%2FOrdersSubmitDTO.java)
# 订单支付
## 微信支付介绍
- 微信支付方式: 扫码支付,刷脸支付,APP支付,native支付等 ...
- 微信支付官方文档:   https://pay.weixin.qq.com/index.php/core/home/login?return_url=https%3A%2F%2Fpay.weixin.qq.com%2Fproduct%2Fproduct_index.html
- 微信支付流程:  提交资料,签署协议,绑定场景
## 微信支付实现
- 微信小程序支付的时序图
![Screenshot_20240719_142443_tv.danmaku.bilibilihd.jpg](img%2FScreenshot_20240719_142443_tv.danmaku.bilibilihd.jpg)
- 相关接口介绍
- JSAPI下单:
  - 商户系统调用该接口在微信支付后天生成预支付交易单
- 微信小程序调用接口
## 准备工作
- 首先明确两个问题：
  - 调用过程如何保证数据安全?   进行加密解密签名等操作
  - 微信后台如何调用商户系统?   需要利用内网穿透获得一个临时的公网IP(如果是本地主机微信后台无法访问到)
- 获得临时域名: 使用软件 cpolar  , 获取的域名： https://65ac61a6.r7.cpolar.cn 
- 配置相关信息就可以了(注意之后工作可以查看官方文档)
- 代码可以根据流程图阅读
