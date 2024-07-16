# 菜品管理
## 公共字段自动填充
- 公共字段:
  - 每一个表中的公共字段比如create_time,create_user,update_time,update_user等字段如果都进行填充,那么就会导致重复代码比较多
- 实现思路:
  - create_time,create_user需要在 insert 操作时赋值,但是 update_time,update_user需要在update和insert中处理
- 可以使用面向切片编程的方式进行拦截
- 具体的实现思路(AOP的用途!!!):
  - 自定义注解(AutoFill),用于标识需要进行公共字段填充的方法
  - 自定义切面类(AutoFillAspect),统一拦截(切点表达式)加入AutoFill注解的方法,通过反射为公共字段赋值
  - 在 Mapper 的方法上加入 AutoFill 注解
- 第一次遇见利用 AOP 进行代码片段的增强
- 注意避免使用硬编码最好定义为常量类,使用常量进行操作,注意反射方法的运用(通常在转换为 MethodSignature从而获取到方法的各种属性和参数)
- 切点其实就是指的切下去的方法,可以使用切点获取方法的参数和注解等信息,一定要学会利用注解标记方法这一种策略
- 注意方法签名的含义和作用,可以查看帖子: https://blog.csdn.net/weixin_43895362/article/details/135872245
```java
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
  /**
   * 切入点 权限修饰符 返回值 包名.类名.方法名(形参列表)异常类型
   * 前面一个锁定方法,后面一个锁定注解
   */
  @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
  public void autoFillPointCut(){}

  /**
   * 前置通知,作用就是为公共字段赋值
   */
  @Before("autoFillPointCut()")
  public void autoFill(JoinPoint joinPoint){
    log.info("开始进行公共字段的自动填充");
    // 1. 获取注解中的操作类型
    MethodSignature signature = (MethodSignature)joinPoint.getSignature();
    AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
    OperationType value = autoFill.value();  // 获取操作类型
    // 2. 获取到当前被拦截到的方法的参数
    Object[] args = joinPoint.getArgs();
    if(args == null || args.length == 0) {
      return ;
    }
    Object arg = args[0];
    // 3. 准备赋值的数据,获取当前用户使用 ThreadLocal 
    // 调用set方法赋值
    LocalDateTime now = LocalDateTime.now();
    Long id = BaseContext.getCurrentId();
    Class<?> aClass = arg.getClass();
    // 4. 为实体的对象赋值
    if(value == OperationType.INSERT) {
      // 为四个公共字段赋值
      try {
        Method createTime = aClass.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
        Method createUser = aClass.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
        Method updateTime = aClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
        Method updateUser = aClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
        // 调用方法赋值
        createTime.invoke(arg,now);
        updateTime.invoke(arg,now);
        createUser.invoke(arg,id);
        updateUser.invoke(arg,id);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if(value == OperationType.UPDATE){
      // 为两个公共字段赋值

      try {
        Method updateTime = aClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
        Method updateUser = aClass.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
        updateUser.invoke(arg,id);
        updateTime.invoke(arg,now);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
```
## 菜品管理相关功能
- 需求设计:
  - 重点就是如何上传图片
- 业务需求:
  - 菜品名称必须是唯一的
  - 菜品必须属于某一个分类以下,不可以单独存在
  - 新增菜品时可以根据情况选择菜品的口味
  - 每一个菜品需要上传一张照片
- 接口设计:
  - 根据类型查询分类
  - 文件上传
  - 新增菜品
- 文件上传接口的设计:
  - 需要传入一个File对象作为文件对象,需要返回一个data也就是文件上传路径表示文件的路径使得前端可以找到图片完成回显
- 数据库表主要就是口味表和菜品表(dish , dish_flavor)
- 这里使用阿里云OSS 对象存储服务来存储信息,查看之前的笔记
- 注意这里体现的一个 SpringBoot开发的思想:
  - 对于工具类的服务,首先在SpringBoot配置文件中配置相关的信息,最好在不同的环境的配置文件中配置信息,同时在 application.yml中激活相应的环境
  - 同时提供一个类 XxxPropeties 类专门用于接受配置文件中的属性
  - 提供一个配置类创建相关的工具类 Bean,通常使用 @Bean注解和XxxProperties类进行创建
- 还是参考之前的笔记
- 阿里云OSS服务的使用方式:
```java
@RestController
@RequestMapping("/admin/common")
@Api("通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传,上传到 阿里 OSS 服务
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件到阿里云OSS上{}",file);
        try {
            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            // 开始取得文件名后缀 .png 
            String fileName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID() + fileName;
            // 获取文件的访问路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败: {}",e);
        }
        return null;
    }
}
```
- 配置方式(注意参数的顺序):
```java
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("开始创建阿里云文件上传工具类对象：{}",aliOssProperties);
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
    }
}
```
### 接受表单数据
- 这里有几个注意点,首先在一对多的关系中,可以不设置具体的外键而设置逻辑外键,就是在程序中建立二者之间的关系,插入一方时带回主键(userGeneralKey , keyProperty)
,之后把主键赋值给对应字段的外键就可以了
- 另外一个注意点就是多表的操作必须使用事务管理,同时一定记得在启动类上加上@@EnableTransactionManagement 开启事务管理
- 代码演示如下:
[DishServiceImpl.java](..%2Fsky-server%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsky%2Fservice%2Fimpl%2FDishServiceImpl.java)
## 分页查询
- 业务规则:
  - 根据页码查询菜品信息
  - 每一页最多展示十条数据
  - 分页查询时根据需要输入菜品的名称分类和状态就可以了
- 接口:
  - /admin/dish/page
  - 利用query携带参数(使用@RequestParam注解就可以获取url中的信息了)
- 这里总结一下分页查询的一般实现方式:
  - 首先明确最终返回给前端的数据都是 Result<PageResult> 格式的数据,前端只需要拿到数据就可以了
  - PageResult中封装着前端所需要的各种数据,包含总记录条数和么一条记录的相关信息组成的集合
  - 明确好前端和后端交换数据的协议,如果前端需要的数据是使用实体类就可以满足那么就可以使用实体类,如果前端需要的数据使用实体类不可以满足那么就不可以使用实体类
  而需要定义一个 VO 对象用于满足后端的需求
  - 前端和后端之间传递数据一般使用DTO对象,如果使用 Get方式的请求那么参数就是 Query类型，此时就可以使用自动封装获取 DTO对象
- 注意 sql查询中最重要的就是数据,mybatis底层可以把数据根据映射关系封装到对象中
- 分页查询一般的代码演示:
- controller
```java
    @GetMapping("/page")
    @ApiOperation("根据菜品名称,分类id,菜品售卖状态查询菜品")
    public Result<PageResult> getDishByInfo(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.getDishByInfo(dishPageQueryDTO);
        return Result.success(pageResult);
    }
```
- service
```java
    @Override
    public PageResult getDishByInfo(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
```
- 注意多表联查的sql语句的写法
```sql
    <select id="pageQuery" resultType="com.sky.vo.DishVO">
        select d.* , c.name as categoryName from dish d left outer join category c on d.category_id = c.id
        <where>
            <if test="name != null and name != ''">
                d.name like concat('%',#{name},'%')
            </if>
            <if test="categoryId != null and categoryId != ''">
                and d.category_id = #{categoryId}
            </if>
            <if test="status != null and status != ''">
                and  d.status = #{status}
            </if>
        </where>
        order by d.create_time desc
    </select>
```
## 删除菜品
- 业务规则:
  - 可以一次删除一个菜品,也可以批量删除菜品
  - 起售中的菜品不可以删除
  - 被套餐关联的菜品不可以删除
  - 删除菜品之后,关联的口味数据也需要被删除
- 请求路径: /admin/dish DELETE 使用 query传递参数,参数为 ids (int[])
- 关联三张表: dish表,dish_flavor表,setmeal_dish表(用于判断菜品是否被关联)
- 总结一下实现删除功能的步骤(CRUD的步骤):
  - 首先明确需求时,需要设计好前后端用于交换数据的类,定义好业务规则,参数传递方式等信息
  - 之后编写对应的 Controller, 在dao层定义号相应的方法,根据业务逻辑编写对于数据库中操作的方法,如果没有相应的方法就可以进行扩展
  - 对于多张表的操作可以拆分为每次操作一张表,明确需要返回的数据和需要传递的数据,并且注意操作多张表需要使用事务管理@Transctional注解标注
  - 最后注意 动态 sql的运用
## 修改菜品
- 需求和分析:
  - 需要数据的回显和数据的修改
- 注意已经实现的接口之后可以复用
- 涉及到的接口:
  - 根据id查询菜品接口(需要返回口味等信息)
  - 修改菜品接口
  - 文件上传(已实现)
  - 根据类型查询分类(已实现)
- 注意口味更新的策略就是首先删除掉原有的口味,之后把更新之后的口味插入到口味表中,注意 sql语句的写法
