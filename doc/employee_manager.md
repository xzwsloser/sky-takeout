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
# 员工分页查询
- 需求分析：
  - 需要向后端响应页码,每页的记录数还有被查询人的姓名等信息,可以使用键值对的方式传参(Query类型,不是动态参数Param)
## 分页查询实现
- 利用 PageHelper 插件传入当前页码和每一页的记录条数就进行查询了,sql语句只用查询所有的满足要求的记录记录就可以了,分页插件自动把查询到的记录封装成 Page 对象并且返回
```java
   @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 底层使用 limit 进行查询
        // 一般使用 PageHelper 插件
        // 开始分页查询,底层使用 动态sql进行拼接
        // 表示页码和
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();  // 表示总条数
        List<Employee> result = page.getResult(); // 表示这一页中的所有记录
        return new PageResult(total,result);

    }
```
```sql
   <select id="pageQuery" resultType="Employee">
        SELECT * FROM employee
                <where>
                    <if test="name != null and name != ''">
                        and name like concat('%',#{name},'%')
                    </if> 
                </where>
                ORDER BY create_time DESC 
    </select>
```
- 注意返回给前端的数据一律使用 Result 进行封装
## 处理日期格式
- 两种解决方法：
  - 方式1: 在属性上加入注解,对日期进行格式化 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss"),对于属性所加的注解,表示 json 序列化之后就会成为这样的格式
  - 方式2: 在 WebMvcConfiguration中扩展SpringMVC 的消息转换器,对于统一的日期类型进行格式化处理
- 第一种方式注解用于修饰属性,但是缺点就是如果属性比较多的话,那么就会导致对于所有注解进行格式化就会十分复杂
- 第二种方式可以设置全局的消息转换器,使得所有的日期都可以转换为指定的格式,配置方法就是在 WebMvcConfiguration中进行相应的配置
```java
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        // 表示一个 json 对象转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 为消息转换器设置一个对象转换器，对象转换器可以把java对象序列化成json数据,注意其中的写法
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, messageConverter);
        // 设置优先级

    }
```
## 状态的启用和禁用操作
- 按下启用或者禁用按钮时就会向后端发送请求携带相应的状态,之后就可以根据相应的状态对于数据库进行操作
- 其实就是简单的修改员工信息的sql操作,代码演示如下(注意如何利用 builder 方法创建对象):
```java
    @Override
    public void startOrStop(Integer status, Long id) {
        // 注意传入参数一定需要想到封装实体类
        // 链式编程的风格
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }
```
- sql语句中注意条件,不然只能删库跑路了
```sql
    <update id="update" parameterType="Employee">
update employee
  <set>
        <if test="name!=null"> name = #{name}, </if>
        <if test="username!=null"> username = #{username}, </if>
        <if test="password!=null"> password = #{password}, </if>
        <if test="phone!=null"> phone = #{phone}, </if>
        <if test="sex!=null"> sex = #{sex}, </if>
        <if test="idNumber!=null"> id_Number = #{idNumber} ,</if>
        <if test="updateTime!=null"> update_Time = #{updateTime} ,</if>
        <if test="updateUser!=null"> update_User = #{updateUser}, </if>
        <if test="status!=null"> status = #{status} </if>
        </set>
where id = #{id}
    </update>
```
## 编辑员工
- 根据 id 查询员工信息
- 编辑员工信息(前面已经写好了)
- 返回数据就有 code,data,msg等
- 代码演示,其实就是复用了上面的更新员工的方法
```java
    @Override
    public void update(EmployeeDTO employeeDTO) {
        // 更新员工信息
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employeeMapper.update(employee);
    }
```
# 导入分类模块功能代码
- 基本和上面的员工管理类似,所以直接导入模块代码
- 业务规则:
  - 分类名称是唯一的
  - 分类按照类型可以分为菜品分类和套裁分类
  - 新添加的分类状态默认为禁用
- 就是简单的 CURD 
- 数据库表设计(category),详情查看: [数据库设计文档.md](%CA%FD%BE%DD%BF%E2%C9%E8%BC%C6%CE%C4%B5%B5.md)
- 导入模块之后注意自动编译一下,形成 target 目录



