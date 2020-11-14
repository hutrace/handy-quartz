package org.hutrace.handy.quartz.impl.dispatch;

import java.lang.reflect.InvocationTargetException;

import org.apache.ibatis.session.SqlSession;
import org.hutrace.handy.mybatis.MyBatisConfig;
import org.hutrace.handy.mybatis.PondSqlFactory;
import org.hutrace.handy.mybatis.exception.RollbackException;
import org.hutrace.handy.quartz.impl.DispatchSupervisor;

/**
 * 在调度时创建与关闭MyBatis的SqlSession
 * @author hu trace
 *
 */
public class MyBatisSqlSessionSupervisor implements DispatchSupervisor {
	
	private String sqlFactoryId = MyBatisConfig.DEFAULT_ID;
	
	/**
	 * 设置定时器使用的sql工厂id，默认是{@link MyBatisConfig.DEFAULT_ID}
	 * @param sqlFactoryId
	 */
	public void setSqlFactoryId(String sqlFactoryId) {
		this.sqlFactoryId = sqlFactoryId;
	}

	@Override
	public void before() {
		PondSqlFactory.addSqlSessions(sqlFactoryId);
	}

	@Override
	public void after(Throwable e) {
		SqlSession sqlSession = PondSqlFactory.getSqlSessions();
		boolean rollback = false;
		if(e != null) {
			if(e instanceof InvocationTargetException) {
				e = ((InvocationTargetException) e).getTargetException();
			}
			if(e instanceof RollbackException) {
				rollback = true;
				sqlSession.rollback();
			}
		}
		if(!rollback) {
			sqlSession.commit();
		}
		sqlSession.close();
		PondSqlFactory.removeSqlSessions();
	}

}
