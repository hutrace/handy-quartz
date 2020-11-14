package org.hutrace.handy.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>使用quartz定时器的必须注解
 * <p>将此注解加入在方法上面，可将方法加入定时器管理，并且通过设置<code>cron</code>属性或<code>fixed</code>属性
 * 即可完成基本使用
 * <p>你还可以设置程序启动时是否执行一次任务，通过设置<code>firstRun</code>属性即可，它的结果是{@link FirstRun}枚举类
 * @author hu trace
 * @see FirstRun
 * @since 1.8
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduled {
	
	/**
	 * <p>定时器表达式
	 * <p>此值与{@link #fixed()}值只能同时设置一个
	 * @return
	 */
	String cron() default "";
	
	/**
	 * <p>定时器固定时间
	 * <p>此值与{@link #cron()}值只能同时设置一个
	 * <p>fixed值不能小于1
	 * @return
	 */
	long fixed() default 0;
	
	/**
	 * <p>添加任务(定时器)时(程序启动时)是否执行
	 * <p>默认使用fixed时执行true,使用cron时不执行false
	 * @return 
	 */
	FirstRun firstRun() default FirstRun.DEFAULT;
	
}
