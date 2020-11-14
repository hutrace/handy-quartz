package org.hutrace.handy.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>使用quartz定时器的必须注解
 * <p>将此注解注入对应的Java类，即可将此类加入定时器管理
 * @author hu trace
 * @since 1.8
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QuartzTimer {}
