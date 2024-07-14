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
