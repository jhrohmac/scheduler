package com.scheduler.comm.util;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.login.service.UserSession;
import com.scheduler.util.handler.ResultMsg;

public class UserSessionCheck {
	
	private static HashMap<String, HttpSession> userMap = new HashMap<String, HttpSession>();
	
	private UserSessionCheck(){}
	
	public static HttpSession getUserMap(String userId){
		return userMap.get(userId);
	}
	
	public static void setUserMap(String userId, HttpSession sess){
		userMap.put(userId, sess);
	}
	
	public static void delUserMap(String userId){
		userMap.remove(userId);
	}
	
	public static boolean checkSession(HttpServletRequest req, String usrId){
 		boolean bool = false;
		if(userMap.get(usrId) != null){
			if(req.getSession().getId().equals(userMap.get(usrId).getId())){
				bool = true;
			}
		}
		return bool;
	}
}