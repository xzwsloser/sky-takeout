# 员工管理功能的实现
## 新增员工
- 需求分析和设计:
  - 首先需要对照着产品原型进行传输数据的设计和请求方式的确定(增删改查)
  - 每一次写一个功能时确定返回的数据,请求的参数和请求头请求体等信息
- 项目约定:
  - 管理端使用 /admin作为前缀
  - 用户端使用 /user 作为前缀
- 使用的数据库表就是 employee ,可以查看数据库设计文档
### 新增员工代码实现
- 当前端提交的数据和实体类中的数据相差比较大的时候,建议使用 DTO 来封装数据
- EmployeeDTO
```java
@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
```
- 就是简单的新增功能,演示如下:
```java
    @Override
    public Result saveEmployee(EmployeeDTO employeeDTO) {
        // 转换为实体类
        Employee employee = new Employee();
        // 进行属性转换
        // 使用对象属性的拷贝
        BeanUtils.copyProperties(employeeDTO,employee);  // Spring提供的工具,用于对象属性的拷贝
        // 设置状态的状态
        employee.setStatus(StatusConstant.ENABLE);  // 设置状态,定义为常量,方式硬编码

        // 设置密码,使用默认密码,还是注意通过常量的方式进行应用
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        // 设置当前时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 设置修改人
        // TODO 这里需要完善代码,可以使用 ThreadLocal 或者 JWT
        employee.setCreateUser(10L);
        employee.setUpdateUser(10L);
        employeeMapper.save(employee);
        return Result.success();
    }
```
- 一个小的插曲:
  - idea端口占用问题的解决:
    - 利用 netstat -ano | findstr :端口号  查看哪些进行使用了本地端口，注意本地端口和远程端口的区别
    - 利用 taskkill /pid 进程号 /t 杀死进程就可以了
### 存在的两个问题
- 第一个问题: 用户名存在时会报错,利用全局的异常处理器解决,注意异常处理器的写法:
```java

    /**
     * 利用异常处理器捕获异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")) {
            String username = message.split(" ")[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
```
- 第二个问题如何获取当前登录的用户,之前使用过 ThreadLocal对象获取登录的对象
- 解析出登录用户的 id 之后,如果传递给Service的save方法? 使用 ThreadLocal对象
- 最好为 ThreadLocal 封装一个工具类,用于取出和移除 ThreadLocal中存储的对象
- 如何判断两个函数是否是同一个线程执行的,可以使用 Thread.getCurrentThread().getId() 就可以了
- 这里封装了 BaseContext工具类,可以用于获取 ThreadLocal对象中的各种属性
