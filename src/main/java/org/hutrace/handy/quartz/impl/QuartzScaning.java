package org.hutrace.handy.quartz.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.hutrace.handy.annotation.DAO;
import org.hutrace.handy.config.Configuration;
import org.hutrace.handy.exception.ScanningApplicationException;
import org.hutrace.handy.quartz.annotation.QuartzTimer;
import org.hutrace.handy.quartz.annotation.Scheduled;
import org.hutrace.handy.utils.scan.ScanningAnnotationConduct;

/**
 * <p>Quartz的注解类扫描器
 * <p>扫描包含{@link QuartzTimer}注解的类以及类下包含{@link Scheduled}注解的方法
 * <p>并将类与方法加入Quartz管理
 * @author hu trace
 * @see AnnotationScanConduct
 * @since 1.8
 * @version 1.0
 */
public class QuartzScaning implements ScanningAnnotationConduct {
	
	@Override
	public void addClass(Annotation arg0, Class<?> arg1) throws ScanningApplicationException {
		
	}

	@Override
	public void addField(Annotation arg0, Class<?> arg1, Field arg2) throws ScanningApplicationException {
		
	}

	@Override
	public void addMethod(Annotation arg0, Class<?> arg1, Method arg2) throws ScanningApplicationException {
		Scheduled scheduled = (Scheduled) arg0;
		if(scheduled.fixed() < 0) {
			throw new ScanningApplicationException("The 'fixed' cannot be less than 1 '" + scheduled.fixed() + "'");
		}
		QuartzManager.instance.add(scheduled, arg1, arg2);
	}

	@Override
	public Class<? extends Annotation> getFieldAnnotation() {
		return DAO.class;
	}

	@Override
	public Class<? extends Annotation> getMethodAnnotation() {
		return Scheduled.class;
	}

	@Override
	public String[] getPackages() {
		return Configuration.scan();
	}

	@Override
	public Class<? extends Annotation> getTypeAnnotation() {
		return QuartzTimer.class;
	}

}
