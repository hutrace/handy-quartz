package org.hutrace.handy.quartz.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hutrace.handy.config.Configuration;
import org.hutrace.handy.exception.ScanningApplicationException;
import org.hutrace.handy.quartz.annotation.FirstRun;
import org.hutrace.handy.quartz.annotation.QuartzTimer;
import org.hutrace.handy.quartz.annotation.Scheduled;
import org.hutrace.handy.quartz.exception.QuartzClassNewInstanceException;
import org.hutrace.handy.quartz.exception.QuartzSchedulerException;
import org.hutrace.handy.utils.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * <p>Quartz的管理类
 * <p>它对于整个框架的写法起到了核心的作用
 * <p>它会对扫描到的类与方法进行封装，并根据类名与方法名建立起键值对对应，对应的值为封装子类{@link QuertzCollection}，
 * 并将键赋予{@link JobDetail}中的key，可直接根据{@link JobDetail}调用对应的方法
 * <p>你需要在扫描类之前初始化，需要调用{@link QuartzManager#init(int)}方法，指定线程数量，此方法已在{@link QuartzLoader}方法中调用。
 * @author hu trace
 * @see Logger
 * @since 1.8
 * @version 1.0
 */
public class QuartzManager extends Logger {
	
	/**
	 * 此类的静态实列，通过此实列操作此类
	 */
	public static final QuartzManager instance = new QuartzManager();
	
	/**
	 * 任务工厂
	 */
	private SchedulerFactory schedulerFactory;
	
	/**
	 * 任务的分组名称
	 */
	private String JOB_GROUP_NAME = "FASTSERVER_QUARTZ_JOB";
	
	/**
	 * 任务触发器的分组名称
	 */
	private String TRIGGER_GROUP_NAME = "FASTSERVER_QUARTZ_TRIGGER";
	
	/**
	 * {@link QuartzTimer}主键类实列的缓存Map，它在处理完成之后定时器启动之前是会被清除的
	 */
	private Map<String, Object> classInstanceMap = new HashMap<>();
	
	/**
	 * Quartz的管理Map，它储存了类与方法的关系，并可以通过{@link QuertzCollection}直接执行对应的方法
	 */
	private final Map<String, QuertzCollection> quertzExecuteMap = new HashMap<>();
	
	/**
	 * 允许在{@link Scheduled}主键方法的参数中添加的参数类型与参数实列
	 */
	private final Map<Class<?>, Object> invokeAllowableParams = new HashMap<>();
	
	/**
	 * <p>根据key获取{@link quertzExecuteMap}的值{@link QuertzCollection}
	 * @param key
	 * @return {@link QuertzCollection}，可直接调用对应的执行方法
	 */
	QuertzCollection quertzExecuteMap(String key) {
		return quertzExecuteMap.get(key);
	}
	
	/**
	 * <p>私有构造
	 * <p>初始化{@link #invokeAllowableParams}的值
	 */
	private QuartzManager() {
		Class<?> clazs;
		try {
			clazs = Class.forName(Configuration.commonDao());
			invokeAllowableParams.put(clazs, clazs.newInstance());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>初始化方法
	 * <p>此方法需要最先调用
	 * <pre>
	 *  初始化{@link #schedulerFactory}，设置线程数量
	 * </pre>
	 * @param threadCount 线程数量
	 * @throws QuartzSchedulerException
	 */
	public void init(int threadCount) throws QuartzSchedulerException {
		Properties properties = new Properties();
		properties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
		properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		try {
			this.schedulerFactory = new StdSchedulerFactory(properties);
		}catch (SchedulerException e) {
			throw new QuartzSchedulerException(e);
		}
	}
	
	/**
	 * <p>私有方法，获取{@link Scheduler}
	 * <p>如果你没有调用{@link #init(int)}方法，将会抛出异常提醒你
	 * @return {@link Scheduler}
	 * @throws QuartzSchedulerException
	 */
	private Scheduler getScheduler() throws QuartzSchedulerException {
		try {
			return schedulerFactory.getScheduler();
		}catch (SchedulerException e) {
			throw new QuartzSchedulerException(e);
		}catch (NullPointerException e) {
			throw new QuartzSchedulerException("You need to call the 'init()' method first");
		}
	}
	
	/**
	 * <p>添加任务
	 * <p>向Quartz中添加任务，此处和{@link QuartzScaning}结合使用的，将方法与方法所属的类加入任务管理
	 * @param qs {@link Scheduled}
	 * @param clazs 扫描到的{@link QuartzTimer}注解类
	 * @param method 扫描到的{@link QuartzTimer}注解类下的{@link Scheduled}注解方法
	 * @throws ScanApplicationException
	 */
	public void add(Scheduled qs, Class<?> clazs, Method method) throws ScanningApplicationException {
		Scheduler scheduler = getScheduler();
		String classInstanceMapKey = clazs.getSimpleName();
		String quertzExecuteMapKey = classInstanceMapKey + "#" + method.getName();
		Object ins = getClassInstance(clazs, classInstanceMapKey);
		JobDetail detail = createJobDetail(clazs, method, ins, quertzExecuteMapKey);
		Trigger trigger = createJobTrigger(quertzExecuteMapKey, qs, clazs, method);
		try {
			scheduler.scheduleJob(detail, trigger);
		}catch (SchedulerException e) {
			throw new QuartzSchedulerException(e);
		}
	}
	
	/**
	 * <p>创建任务的触发器(Trigger)
	 * <p>根据{@link Scheduled}参数的{@link Scheduled#cron()}、{@link Scheduled#fixed()}执行不同的创建
	 * <p>根据{@link Scheduled#firstRun()}判断是否加入首次执行
	 * @param quertzExecuteMapKey
	 * @param qs {@link Scheduled}
	 * @param clazs 扫描到的{@link QuartzTimer}注解类
	 * @param method 扫描到的{@link QuartzTimer}注解类下的{@link Scheduled}注解方法
	 * @return 创建好的{@link Trigger}触发器
	 * @throws ScanApplicationException
	 */
	private Trigger createJobTrigger(String quertzExecuteMapKey, Scheduled qs, Class<?> clazs,
			Method method) throws ScanningApplicationException {
		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
				.withIdentity(quertzExecuteMapKey + "@Trigger", TRIGGER_GROUP_NAME);
		if(qs.cron() != null && qs.cron().length() > 0) {
			// cron
			return triggerCron(triggerBuilder, qs);
		}else if(qs.fixed() > 0) {
			// fixed
			return triggerFixed(triggerBuilder, qs);
		}else {
			// not timer
			throw new ScanningApplicationException("Missing expression : " + clazs.getName() + "#" + method.getName());
		}
	}
	
	/**
	 * <p>创建任务详情(Detail)
	 * @param clazs 扫描到的{@link QuartzTimer}注解类
	 * @param method 扫描到的{@link QuartzTimer}注解类下的{@link Scheduled}注解方法
	 * @param ins {@link QuartzTimer}注解类的实列
	 * @param key {@link #quertzExecuteMap}的key
	 * @return 任务详情(Detail)
	 */
	private JobDetail createJobDetail(Class<?> clazs, Method method, Object ins, String key) {
		quertzExecuteMap.put(key, new QuertzCollection(ins, method));
		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
				.withIdentity(key, JOB_GROUP_NAME)
				.build();
		return jobDetail;
	}
	
	/**
	 * <p>获取{@link QuartzTimer}注解类的实列
	 * @param clazs 扫描到的{@link QuartzTimer}注解类
	 * @param key {@link #classInstanceMap}缓存Map的key
	 * @return {@link QuartzTimer}注解类的实列
	 * @throws QuartzClassNewInstanceException
	 */
	private Object getClassInstance(Class<?> clazs, String key) throws QuartzClassNewInstanceException {
		Object ins = classInstanceMap.get(key);
		if(ins == null) {
			try {
				ins = clazs.newInstance();
				classInstanceMap.put(key, ins);
			}catch (Exception e) {
				throw new QuartzClassNewInstanceException(e);
			}
		}
		return ins;
	}
	
	/**
	 * <p>创建Cron表达式的任务触发器
	 * @param triggerBuilder
	 * @param qs {@link Scheduled}
	 * @return 任务触发器(Trigger)
	 */
	private Trigger triggerCron(TriggerBuilder<Trigger> triggerBuilder, Scheduled qs) {
		boolean start = (qs.firstRun() == FirstRun.DEFAULT || qs.firstRun() == FirstRun.FALSE) ? false : true;
		if(start) {
			triggerBuilder.startNow();
		}
		return triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(qs.cron())).build();
	}
	
	/**
	 * <p>创建Fixed毫秒任务触发器
	 * @param triggerBuilder
	 * @param qs {@link Scheduled}
	 * @return 任务触发器(Trigger)
	 */
	private Trigger triggerFixed(TriggerBuilder<Trigger> triggerBuilder, Scheduled qs) {
		boolean start = (qs.firstRun() == FirstRun.DEFAULT || qs.firstRun() == FirstRun.TRUE) ? true : false;
		if(start) {
			triggerBuilder.startNow();
		}else {
			Calendar now = Calendar.getInstance();
			now.add(Calendar.MILLISECOND, (int) qs.fixed());
			triggerBuilder.startAt(now.getTime());
		}
		return triggerBuilder.withSchedule(SimpleScheduleBuilder
				.simpleSchedule()
				.withIntervalInMilliseconds(qs.fixed())
				.repeatForever()).build();
	}
	
	/**
	 * <p>启动任务
	 * @throws QuartzSchedulerException
	 */
	public void start() throws QuartzSchedulerException {
		try {
			classInstanceMap = null;
			getScheduler().start();
		}catch (SchedulerException e) {
			throw new QuartzSchedulerException(e);
		}
	}
	
	private void invokBefore() {
		List<DispatchSupervisor> lists = QuartzLoader.dispatchSupervisors;
		if(lists != null && lists.size() > 0) {
			for(int i = 0; i < lists.size(); i++) {
				lists.get(i).before();
			}
		}
	}
	
	private void invokAfter(Throwable t) {
		List<DispatchSupervisor> lists = QuartzLoader.dispatchSupervisors;
		if(lists != null && lists.size() > 0) {
			for(int i = 0; i < lists.size(); i++) {
				lists.get(i).after(t);
			}
		}
	}
	
	/**
	 * <p>Quartz的任务集合
	 * <p>通过它可以直接调用对应的方法
	 * @author hu trace
	 * @since 1.8
	 * @version 1.0
	 */
	class QuertzCollection {
		
		Object classInstance;
		Method classMethod;
		
		QuertzCollection(Object classInstance, Method classMethod) {
			this.classInstance = classInstance;
			this.classMethod = classMethod;
		}
		
		/**
		 * <p>调用注解方法
		 */
		void invoking() {
			invokBefore();
			Throwable t = null;
			try {
				classMethod.invoke(classInstance, createParameters());
			}catch (Throwable e) {
				t = e;
			}
			invokAfter(t);
		}
		/**
		 * <p>创建调用方法需要的参数
		 * @return 调用方法需要的参数
		 */
		private Object[] createParameters() {
			Parameter[] parameters = classMethod.getParameters();
			Object[] objs = new Object[parameters.length];
			for(int i = 0; i < objs.length; i++) {
				objs[i] = invokeAllowableParams.get(parameters[i].getType());
			}
			return objs;
		}
	}
	
}
