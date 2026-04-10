package com.scheduler.management.dao;

import java.util.HashMap;
import java.util.List;

public interface CalendarManageDao {
	   
    /********************************************************************************
     *  BW Engine List Search
     ********************************************************************************/    
    public List<?> selectCalendarList(HashMap<String, String> param) throws Exception;
    
    public List<?> selectCalendarGridList(HashMap<String, String> param) throws Exception;
    
    public List<?> eventCalendar(HashMap<String, String> param) throws Exception;
    
    public void directCalendarEvent(HashMap<String, String> param) throws Exception;
}