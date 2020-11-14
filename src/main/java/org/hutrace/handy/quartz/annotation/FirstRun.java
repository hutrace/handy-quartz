package org.hutrace.handy.quartz.annotation;

/**
 * <p>表示是否首次执行值枚举
 * @author hu trace
 * @since 1.8
 * @version 1.0
 */
public enum FirstRun {
	
	/**
	 * 默认，cron表达式默认首次不执行，fixed毫秒单位默认首次执行
	 */
	DEFAULT,
	/**
	 * 首次需要执行
	 */
	TRUE,
	/**
	 * 首次不需要执行
	 */
	FALSE
	
}
