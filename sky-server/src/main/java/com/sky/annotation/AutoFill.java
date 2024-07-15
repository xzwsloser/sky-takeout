package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xzw
 * @version 1.0
 * @Description  自定义注解,用于标记方法用于指定哪些方法需要自动填充
 * @Date 2024/7/15 15:31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)  // 表示运行时注解,运行时还可以看得到
public @interface AutoFill {
    OperationType value();  // 数据库操作类型
}
