package edu.fdu.se.lang.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD}) //注解应用类型(应用到方法的注解,还有类的可以自己试试)
@Retention(RetentionPolicy.RUNTIME) // 注解的类型
public @interface MethodLookupTbl {
    int key() default -100;
}
