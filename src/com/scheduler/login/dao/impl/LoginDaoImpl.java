package com.scheduler.login.dao.impl;

import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.login.dao.LoginDao;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.vo.UserVo;

public class LoginDaoImpl extends SqlSessionDaoSupport implements LoginDao {
		
	private static final Logger logger = LoggerFactory.getLogger(LoginDaoImpl.class);
	
	@Override
	public UserSession login(Map<String, String> map) throws Exception{
		String err_message = "";
    	String usr_pwd 	= map.get("in_userPwd");
    	
    	UserSession user = new UserSession();
    	
    	UserVo getInfo = new UserVo();
		try{
			getInfo = getSqlSession().selectOne("sql.UserManage.selectUserInfo",map);
			if (getInfo == null) {
				err_message = "등록된 사용자가 없습니다.";
	            user = setErrorMessage(user, err_message);
	            return user; 								
	        }
			
			//userId check
			if(getInfo.getUser_id() == null){
				err_message = "사용자 정보가 없습니다.";
				user = setErrorMessage(user, err_message);
		        return user;
	    	}
			//password check
			String pw = getInfo.getPw();
			if(!usr_pwd.equals(pw)){
	    		err_message = "비밀번호가 맞지 않습니다.";
				user = setErrorMessage(user, err_message);
		        return user;
	    	}
			getInfo.setInfo_message("로그인이 정상적으로 처리되었습니다.");
			user = setSession(user, getInfo);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		
		return user;
	}
	
	@Override
	public UserVo selectUserCheck(Map<String, String> map) throws Exception{		
		UserVo userVo = null;
		try{
			userVo = getSqlSession().selectOne("sql.UserManage.selectUserCheck",map);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}		
		return userVo;
	}
	
	@Override
	public void resetPassword(Map<String, String> map) throws Exception{		
		try{
			getSqlSession().insert("sql.UserManage.updatePw",map);
		}catch(Exception e){
			e.printStackTrace();
			getSqlSession().rollback();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}		
	}
	
	@Override
	public void registerUser(Map<String, String> map) throws Exception{		
		try{
			getSqlSession().insert("sql.UserManage.registerUser",map);
			getSqlSession().insert("sql.UserManage.registerPage",map);
		}catch(Exception e){
			e.printStackTrace();
			getSqlSession().rollback();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}		
	}
	
	// Helper method to set the error message in the session and return the user object
	private UserSession setErrorMessage(UserSession user, String err_message) {
	    user.err_message = err_message;
	    return user;
	}
	
	@Override
	public void setExtensionDate(Map<String, String> map) throws Exception{		
		
		try{
			getSqlSession().update("sql.UserManage.updateUserExtension",map);
				
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}		
	}
	
	@Override
	public void updateUserSessionTime(Map<String, String> map) throws Exception{		
		
		try{
			
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}		
	}
	
	/**
	 * Login success - Session Setting
	 * @param user
	 * @param userInfo
	 * @return
	 */
	private UserSession setSession(UserSession user, UserVo userInfo){
		
		
		user.user_id = userInfo.getUser_id();
		user.user_nm = userInfo.getUser_nm();
		user.desk_div = userInfo.getDesk_div();
		user.email = userInfo.getEmail();
		user.hp_no = userInfo.getHp_no();
		user.role = userInfo.getRole();
		user.mail_yn = userInfo.getMail_yn();
		user.sms_yn = userInfo.getSms_yn();
		user.use_yn = userInfo.getUse_yn();
		user.user_color = userInfo.getUser_color();
		user.user_avatar = userInfo.getUser_avatar();
		user.lang = userInfo.getLang();
		user.defaultPage = userInfo.getDefaultpage();
		user.layout_level = userInfo.getLayout_level();
		user.timer_ifst = userInfo.getTimer_ifst();
		user.timer_host = userInfo.getTimer_host();
		user.timer_dashboard = userInfo.getTimer_dashboard();
		user.timer_sysrsc = userInfo.getTimer_sysrsc();
		user.timer_kpi = userInfo.getTimer_kpi();
		user.timer_process = userInfo.getTimer_process();
		user.timer_present = userInfo.getTimer_present();
		user.timer_alert = userInfo.getTimer_alert();
		user.session_login_time = userInfo.getSession_login_time();
		user.use_sta_date = userInfo.getUse_sta_date();
		user.use_end_date = userInfo.getUse_end_date();
		user.employment_date = userInfo.getEmployment_date();
		user.over_count = userInfo.getOver_count();
		user.info_message = userInfo.getInfo_message();
		user.err_message = userInfo.getErr_message();
		user.timer_ems = userInfo.getTimer_ems();
		user.listup_process = userInfo.getListup_process();
		return user;
	}
}