package org.hutrace.handy.quartz.impl;

/**
 * 调度管理器
 * <p>在调用执行方法时的管理器
 * @author hu trace
 *
 */
public interface DispatchSupervisor {
	
	/**
	 * 在调度之前执行的方法
	 */
	void before();
	
	/**
	 * 在调度之后执行的方法
	 */
	void after(Throwable e);
	
}
