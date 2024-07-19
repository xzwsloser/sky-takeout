# 项目中各个模块的作用
- 项目中的各种文件如下:
![img.png](img%2Fimg.png)
- sky-common中存放的就是一些常量类,可以供其他模块利用,比如常量和工具等包
- sky-pojo定义的就是一些 entity,DTO,VO:
  - Entity: 实体,通常和数据库中的表对应
  - DTO: 数据传输对象,通常用于程序中各层之间传递数据(比如接受前端的对象)
  - VO: 视图对象,为前端展示数据提供的对象(比如前端的列表或者表单数据)
  - POJO: 普通的Java类,只有属性和对应的 getter 和 setter 方法(具体体现就是上面三种类)
- sky-server子模块中存放的就是配置文件,配置类,拦截器,controller,service,mapper,启动类等
## 数据库环境搭建
- 数据库中使用的表如下:

序号|表名|作用
---|---|---
1|employee|员工表
2|category|分类表
3|dish|菜品表
4|dish_flavor|菜品口味表
5|setmeal|套餐表
6|setmeal_dish|套餐菜品关系表
7|user|用户表
8|address_book|地址表
9|shopping_cart|购物车表
10|orders|订单表
11|order_detail|订单明细表

- 数据库具体介绍: [数据库设计文档.md](doc%2F%CA%FD%BE%DD%BF%E2%C9%E8%BC%C6%CE%C4%B5%B5.md)
# 苍穹外卖各个部分介绍
## Swagger的基本使用
[swagger_use.md](doc%2Fswagger_use.md)
## 员工管理功能
[employee_manager.md](doc%2Femployee_manager.md)
## 菜品管理
[dish_manager.md](doc%2Fdish_manager.md)
## 套餐查询(自主任务)
[set_meal_manager.md](doc%2Fset_meal_manager.md)
## 店铺营业状态设置
[shop_status_manager.md](doc%2Fshop_status_manager.md)
## 微信登录和商品浏览功能开发
[we_chat_development.md](doc%2Fwe_chat_development.md)
## 缓存商品和购物车
[shop_car_cache.md](doc%2Fshop_car_cache.md)
## 用户下单和支付功能
[wx_pay.md](doc%2Fwx_pay.md)
## 用户管理模块(自主任务)
[order_manager.md](doc%2Forder_manager.md)