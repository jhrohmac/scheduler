package com.scheduler.management.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.scheduler.management.dao.CalendarManageDao;
import com.scheduler.management.vo.CalendarManageVo;

/**
 * 캘린더
 *
 */
//@Transactional(readOnly = false, timeout=30)

public class CalendarManageDaoImpl extends SqlSessionDaoSupport implements CalendarManageDao {
	
	private static final Logger logger = LoggerFactory.getLogger(CalendarManageDaoImpl.class);

	@Override
	public List<?> selectCalendarList(HashMap<String, String> param) throws Exception {
		
		List<CalendarManageVo> calList = null;
		try{
			calList = getSqlSession().selectList("sql.CalendarManage.selectCalendarList",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return calList;
	}
	
	//@Override
	public List<?> selectAll2(HashMap<String, Object> param) throws Exception {
	    List<CalendarManageVo> calList = null;
	    try {
	        // Record the start time
	        long startTime = System.currentTimeMillis();

	        // Use try-with-resources to ensure resources are closed
	        //try (SqlSession sqlSession = getSqlSession()) {
	            // Execute the query
	           // calList = getSqlSession.selectList("sql.CalendarManage.selectCalendarList", param);

	            // Check elapsed time
//	            long elapsedTime = System.currentTimeMillis() - startTime;
//	            if (elapsedTime > 60000) {  // 1 minute (60,000 milliseconds)
//	                logger.warn("Database query took more than 1 minute. Closing the session.");
//	                sqlSession.rollback();  // Rollback any changes (if any)
//	            }
	        //}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
	    return calList;
	}

	@Override
	public List<?> selectCalendarGridList(HashMap<String, String> param) throws Exception {
		
		List<CalendarManageVo> calList = null;
		try{
			calList = getSqlSession().selectList("sql.CalendarManage.selectCalendarGridList",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return calList;
	}
	
	@Override
	public List<?> eventCalendar(HashMap<String, String> param) throws Exception {
		
		List<CalendarManageVo> calList = null;
		try{
			if(param.get("in_eventDiv").equals("C")){
				getSqlSession().insert("sql.CalendarManage.insertCalendar",param);
			} else if(param.get("in_eventDiv").equals("M")){
				getSqlSession().insert("sql.CalendarManage.updateCalendar",param);
			} else if(param.get("in_eventDiv").equals("D")){
				getSqlSession().insert("sql.CalendarManage.deleteCalendar",param);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		
		return calList;
	}
	
	
	@Override
	public void directCalendarEvent(HashMap<String, String> param) throws Exception {
		
		System.out.println("===========param================");
		System.out.println(param.get("in_eventDiv"));
		System.out.println(param);
		try{
			getSqlSession().insert("sql.CalendarManage.insertCalendar",param);
/*			if(param.get("in_eventDiv").equals("insert")){
				getSqlSession().insert("sql.CalendarManage.insertCalendar",param);
			} else if(param.get("in_eventDiv").equals("update")){
				getSqlSession().insert("sql.CalendarManage.updateCalendar",param);
			} else if(param.get("in_eventDiv").equals("delete")){
				getSqlSession().insert("sql.CalendarManage.deleteCalendar",param);
			}
*/			
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
	}
}