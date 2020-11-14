package org.hutrace.handy.quartz.exception;

import org.hutrace.handy.exception.ScanningApplicationException;

/**
 * <p>Quartz的其它异常
 * @author hu trace
 * @see ScanApplicationException
 * @since 1.8
 * @version 1.0
 */
public class QuartzSchedulerException extends ScanningApplicationException {

	private static final long serialVersionUID = 1L;

	public QuartzSchedulerException(String msg) {
		super(msg);
	}
	
	public QuartzSchedulerException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public QuartzSchedulerException(Throwable e) {
		super(e);
	}

}
