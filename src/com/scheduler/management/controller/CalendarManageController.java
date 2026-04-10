package com.scheduler.management.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.CalendarManageDao;
import com.scheduler.management.vo.CalendarManageVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

import net.sf.json.JSONObject;

/**
 * 캘린더
 *
 */

@Controller
public class CalendarManageController {

	public CalendarManageDao calendarManageDao;
	
	public CalendarManageDao getcalendarManageDao() {
		return calendarManageDao;
	}

	public void setcalendarManageDao(CalendarManageDao calendarManageDao) {
		this.calendarManageDao = calendarManageDao;
	}

	/**
	 * CALENDAR PAGE
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/calendar/calendar.do")
	public void c_selectAgentList(HttpServletRequest req, HttpServletResponse res) throws IOException{

		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);

		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		map.put("in_userId",		userSession.user_id);
		map.put("in_Role",			userSession.role);
		map.put("in_userDesk",		userSession.desk_div);
		
		try {
			List<CalendarManageVo> list = null;
			list = (List<CalendarManageVo>) calendarManageDao.selectCalendarList(map);
			
			if(list.size() > 0 ){
				for(int i=0; i<list.size(); i++){
					list.get(i).setStart(list.get(i).getStart_time());
					list.get(i).setEnd(list.get(i).getEnd_time());
				}
			}
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setData(list);
			resultVo.setRecordsFiltered(list.size());
			resultVo.setRecordsTotal(list.isEmpty() ? 0 : list.get(0).getTotal_count());

			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}
	
	/**
	 * GRID PAGE
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/calendar/calendarGridData.do")
	public void c_calendarGridData(HttpServletRequest req, HttpServletResponse res) throws IOException{
		
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		try {
			List<CalendarManageVo> list = null;
			list = (List<CalendarManageVo>) calendarManageDao.selectCalendarGridList(map);
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setData(list);
        	resultVo.setDraw(map.get("draw"));
        	resultVo.setStart_no(Integer.parseInt(map.get("start")));
        	resultVo.setPage_length(Integer.parseInt(map.get("length")));
        	resultVo.setRecordsFiltered(list.size());
        	resultVo.setRecordsTotal(list.isEmpty() ? 0 : list.get(0).getTotal_count());
	
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}
	
	/**
	 *  캘린더 등록
	 * @throws IOException 
	 */
	@RequestMapping("/calendar/eventCalendar.do")
    public void c_saveCalendar(HttpServletRequest req, HttpServletResponse res) throws IOException{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		String[] dates = map.get("reportrange").split(" - ");
		map.put("in_startDate",		dates[0]);
		map.put("in_endDate",		dates[1]);
		map.put("in_userId",		userSession.user_id);
		map.put("in_Role",			userSession.role);

		try {
			calendarManageDao.eventCalendar(map);
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);

			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}
}