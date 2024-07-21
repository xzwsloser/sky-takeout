# 苍穹外卖项目总结
- 这里总结苍穹外卖项目中用到的新技术
## Swagger
- Swagger可以帮助开发者使用注解式的配置方法生成接口文档,并且可以通过访问本地静态资源访问接口文档并且进行接口文档的测试
- 使用 Swagger技术的步骤如下:
  - 首先需要导入 Knife4j 的依赖
  - 配置相关的 docket 
  - 配置静态资源映射(MVCConfiguration)中配置
- 细节参见:  [swagger_use.md](swagger_use.md)
## 利用 Aop 进行公共字段的填充
- Aop的底层起始就是利用了动态代理的思想,使用动态代理的方法使得代码的功能得以增强
- Aop的使用步骤:
  - 使用@Aspect 标注一个类表示这一个类就是一个切片
  - 定义一个切点表达式,最好使用一个方法来表示切点表达式,使用@PointCut注解
  - 之后利用@Before等注解指定通知类型就可以了,注意可以结合 JointPoint参数,这一个参数就表示方法
- Aop使用的过程中,可以通过自定义注解的方式来标记方法,之后利用切点表达式定位注解
- 细节参见: [dish_manager.md](dish_manager.md)
## 阿里云对象存储服务(oss)
- 参考语雀笔记和官方文档,一定要学会通过阅读官方文档的方式调用第三方接口
## PageHelper插件的使用
- PageHelper分页插件的使用,参见: [dish_manager.md](dish_manager.md)
## 利用 Redis做缓存
- 重点注意 StringRedisTemplate 和 RedisTemplate之间的区别
- StringRedisTemplate只可以操作字符串类型的变量,并且 set,get操作取出的都是字符串类型的变量
- RedisTemplate操作的数据类型使用了泛型,无论何种数据类型都可以操作,但是取出数据类型之后需要强制类型转换
- 参考: [shop_status_manager.md](shop_status_manager.md)
## HttpClient
- HttpClient的作用就是利用 java程序发送请求,一般封装成工具类,细节参考: 
[we_chat_development.md](we_chat_development.md)
## 微信登录
- 注意微信登录的时序图: [we_chat_development.md](we_chat_development.md)
## SpringCache
- 作用利用注解式开发实现缓存
- 使用步骤:
  - 导入依赖(一般不需要导入,Spring-content依赖中就含有Spring Cache的依赖)
  - 在启动类上配置相关的注解 @EnableCache
- 细节参见: [shop_car_cache.md](shop_car_cache.md)
## 用户下单和支付功能:
- 注意微信支付的时序图如何画,微信支付的流程,可以参考微信开发者文档
注意每一步需要传输的数据就可以了
- 参考: [wx_pay.md](wx_pay.md)
## SpringTask
- Spring提供的一个关于定时任务的简单框架
- 使用方式:
  - 引入依赖(一般不需要)
  - 在启动类上面配置 @EnableScheduling注解开启定时任务
  - 利用@Schedule在方法上配置 cron表达式用于指示方法执行的时机
## WebSocket
- 一种全双工通信协议,主要用于客户端和服务器端的长时间相互通信,长连接,不同于 http 协议
- 只用一次握手就可以达全连接的目的
- 细节: [order_cancel_task.md](order_cancel_task.md)
## Apache ECharts
- 一种前端技术,用于数据可视化,可以支持多种图的形式,比如折线图,直方图等形式
- 后端只用提供相应的技术就可以了
## Apache POI
- 一种读取 Office 文件的库,可以用于读取和创建 excel文件从而到达读取文件的目的
- 参考: [excel_use.md](excel_use.md)

