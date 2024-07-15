package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import nonapi.io.github.classgraph.utils.Join;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author xzw
 * @version 1.0
 * @Description 自定义切面类,注意标记为切面,并且无脑纳入到 spring容器管理
 * @Date 2024/7/15 15:34
 */
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
