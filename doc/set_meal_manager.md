# 套餐管理
## 新增套餐
- 根据id查询菜品：
  - 路径: /admin/dish/list
  - 方式 GET
  - 接口参数 categoryId
  - 返回数据
## 新增套餐
- 接口设计:
  - path: /admin/setmeal
  - 请求方式 Post
  - 参数
- 使用的表结构就是 setmeal 和 setmeal_dish 两张表,但是注意setmeal_dish中的setmealId如果获取
## 分页查询
- 接口设计:
  - path: /admin/setmeal/page
  - 请求参数 SetmealPageQueryDTO对象
  - 返回参数 PageResult,并且 record中承装的对象就是 SetmealVo 对象
- 还是一样见招拆招(需要什么就可以直接进行查询)
## 删除套餐
- 接口设计：
  - path: /admin/setmeal
- 好像比删除菜品简单一些,直接删除就可以了
- 注意还要删除关联的菜品
## 修改套餐
### 根据id查询店铺
- 接口设计:
  - path: /admin/setmeal/{id}
### 修改菜品
- 接口设计
  - path: /admin/setmeal
  - 请求方式 PUT
  - 传输数据: XxxDTO
### 修改菜品状态
- 接口设计
  - path: /admin/setmeal/status/{status}
  - 请求方式PUT
- 注意没有起售就不可以删除
