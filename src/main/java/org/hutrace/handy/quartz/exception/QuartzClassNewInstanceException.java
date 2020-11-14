package org.hutrace.handy.quartz.exception;

import org.hutrace.handy.exception.ScanningApplicationException;
import org.hutrace.handy.quartz.annotation.QuartzTimer;

/**
 * <p>{@link QuartzTimer}的注解类构造实列出现错误时抛出的异常
 * @author hu trace
 * @see ScanApplicationException
 * @since 1.8
 * @version 1.0
 */
public class QuartzClassNewInstanceException extends ScanningApplicationException {

	private static final long serialVersionUID = 1L;

	public QuartzClassNewInstanceException(String msg) {
		super(msg);
	}
	
	public QuartzClassNewInstanceException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public QuartzClassNewInstanceException(Throwable e) {
		super(e);
	}

}
