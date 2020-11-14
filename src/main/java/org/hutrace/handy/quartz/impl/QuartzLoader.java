package org.hutrace.handy.quartz.impl;

import java.util.List;

import org.hutrace.handy.exception.AppLoaderException;
import org.hutrace.handy.loader.Loader;
import org.hutrace.handy.utils.scan.ScanningAnnotation;

/**
 * <p>Quartz的加载器
 * <p>你需要在你的配置中使用此加载器
 * <p>通过它可以设置一些你需要使用的参数
 * @author hu trace
 * @see Loader
 * @since 1.8
 * @version 1.0
 */
public class QuartzLoader implements Loader {
	
	private int threadCount = 5;
	
	static List<DispatchSupervisor> dispatchSupervisors;
	
	/**
	 * 设置定时器最大线程数量
	 * @param threadCount
	 */
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	
	/**
	 * 设置定时器调度管理器
	 * @param dispatchSupervisors
	 */
	public void setDispatchSupervisors(List<DispatchSupervisor> dispatchSupervisors) {
		QuartzLoader.dispatchSupervisors = dispatchSupervisors;
	}

	@Override
	public void execute() throws AppLoaderException {
		QuartzManager.instance.init(threadCount);
		QuartzScaning timerScan = new QuartzScaning();
		ScanningAnnotation.build(timerScan);
		QuartzManager.instance.start();
	}

}
