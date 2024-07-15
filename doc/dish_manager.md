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