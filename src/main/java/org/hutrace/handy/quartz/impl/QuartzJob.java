package org.hutrace.handy.quartz.impl;

import org.hutrace.handy.utils.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>Quartz的任务主类
 * <p>实现Quartz的{@link Job}类，实现其{@link #execute(JobExecutionContext)}方法
 * <p>根据JobDetail的名称去执行对应的任务
 * @author hu trace
 * @see Logger
 * @see Job
 * @since 1.8
 * @version 1.0
 */
public class QuartzJob extends Logger implements Job {
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String key = context.getJobDetail().getKey().getName();
		log.debug("Execute the method [" + key + "()]");
		QuartzManager.instance.quertzExecuteMap(key).invoking();
	}
	
}
