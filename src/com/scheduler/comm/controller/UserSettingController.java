package com.scheduler.comm.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.dao.UserSettingDao;
import com.scheduler.comm.util.SeedUtil;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.UserSettingVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

import net.sf.json.JSONObject;

@Controller
public class UserSettingController {

	public UserSettingDao userSettingDao;

	public UserSettingDao getUserSettingDao() {
		return userSettingDao;
	}

	public void setUserSettingDao(UserSettingDao userSettingDao) {
		this.userSettingDao = userSettingDao;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/userSetting/selectDuplicateUser.do")
	public void selectDuplicateUser(HttpServletRequest req, HttpServletResponse res) throws  IOException{
	    //PARAMETER KEY VALUE Setting
	    HashMap<String, String> map = RequestHandler.extractParameters(req);
	    
		List<UserSettingVo> list = new ArrayList<UserSettingVo>();
		
		try {
			list =  (List<UserSettingVo>) userSettingDao.selectDuplicateUser(map);
		
			DataTableSettingVo resultVo = new DataTableSettingVo();
			if(list == null || list.size() == 0 ) {
				resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
				resultVo.setResult_msg("사용 가능한 아이디 입니다.");
			}else {
				resultVo.setResult_code(ResultMsg.ERROR_CODE);
		    	resultVo.setResult_msg("사용중인 아이디 입니다.");	
			}
            ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}
		
		
		
	@RequestMapping("/userSetting/selectUserInfo.do")
	public void c_selectUserInfo(HttpServletRequest req, HttpServletResponse res) throws  IOException{
    			
		String parameter = StringUtil.nvl(req.getParameter("parameter"));
		org.json.JSONObject jsonObject = new org.json.JSONObject(parameter);
		/* Request Parameter Set */
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("user_id",			StringUtil.nvl((String) jsonObject.get("usr_id")));
		map.put("usr_nm",			StringUtil.nvl((String) jsonObject.get("usr_nm")));
		map.put("usr_role",			StringUtil.nvl((String) jsonObject.get("usr_role")));
		
		UserSettingVo userSettingVo = null;
		
		try {
			userSettingVo = (UserSettingVo) userSettingDao.selectUserInfo(map);
			if(userSettingVo != null){
				userSettingVo.setHp_no(SeedUtil.invokeDecrypt("tlsfk!2016",userSettingVo.getHp_no(),"UTF-8"));
			}
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
        //JSON Object 변환
		JSONObject obj = JSONObject.fromObject(userSettingVo);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}

	/**
	 *  사용자 등록 시  중복 체크
	 *  @param  조건  : 아이디
	 *  @return 사용자 등록 여부
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping("/userSetting/selectInsPwConfirm.do")
	public void c_selectInsPwConfirm(HttpServletRequest req, HttpServletResponse res) throws  IOException, NoSuchAlgorithmException{
		/* 검색조건 변수 */
		String usr_id 		= StringUtil.nvl(req.getParameter("usr_id"));
		String oldpwd 		= StringUtil.nvl(req.getParameter("txt_Upd_oldpwd"));
		String newpwd 		= StringUtil.nvl(req.getParameter("txt_Upd_newpwd"));
		String old_pwd = Sha256Util.sha256(oldpwd);
		String new_pwd = Sha256Util.sha256(newpwd);
		/* 리턴 할 값들을 담는 변수 */
		String resultCD = "";
		String resultMsg = "";
		int tot_cnt = 0;
		/* 마이바티스에서 사용할 파라메터 설정 */
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("usr_id", usr_id);
		map.put("new_pwd", new_pwd);
		map.put("old_pwd", old_pwd);
		
		try {
			/* 조회 */
			tot_cnt = userSettingDao.selectInsPwConfirm(map);
			if(tot_cnt !=0){
				resultCD = "Success";
				resultMsg = "비밀번호가 변경되었습니다.";
			}else{
				resultCD = "Error";
				resultMsg = "비밀번호를 다시 확인해주세요.";
			}
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
		/* 결과 리턴 */
		HashMap<String,String> json = new HashMap<String,String>();
		json.put("resultCD", resultCD);
		json.put("resultMsg", resultMsg);
		JSONObject obj = JSONObject.fromObject(json);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}
	
	
	/**
	 *  사용자 등록 시  중복 체크
	 *  @param  조건  : 아이디
	 *  @return 사용자 등록 여부
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping("/userSetting/updPwChange.do")
	public void c_updPwChange(HttpServletRequest req, HttpServletResponse res) throws  IOException, NoSuchAlgorithmException{
		
		/* 리턴 할 값들을 담는 변수 */
		String resultCD = "";
		String resultMsg = "";
		int tot_cnt = 0;
		
		try {
			/* 조회 */
			tot_cnt = userSettingDao.updUserList();
			if(tot_cnt !=0){
				resultCD = "Success";
				resultMsg = "비밀번호가 변경되었습니다.";
			}else{
				resultCD = "Error";
				resultMsg = "비밀번호를 다시 확인해주세요.";
			}
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
		/* 결과 리턴 */
		HashMap<String,String> json = new HashMap<String,String>();
		json.put("resultCD", resultCD);
		json.put("resultMsg", resultMsg);
		JSONObject obj = JSONObject.fromObject(json);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}
	
	@RequestMapping("/userSetting/updateUserInfo.do")
	public void c_updateUserInfo(HttpServletRequest req, HttpServletResponse res) throws  IOException, NoSuchAlgorithmException{

		String usr_id = req.getParameter("usr_id");
		String usr_nm = StringUtil.nvl(req.getParameter("user_info_nm"));
		String usr_dept = StringUtil.nvl(req.getParameter("user_info_dp"));
		String usr_email = StringUtil.nvl(req.getParameter("user_info_email"));
		String usr_hp = StringUtil.nvl(req.getParameter("user_info_mb"));
			   usr_hp = SeedUtil.invokeEncrypt("tlsfk!2016",usr_hp,"UTF-8");
		String usr_mailyn = StringUtil.nvl(req.getParameter("user_info_emailfg"));
		String usr_smsyn = StringUtil.nvl(req.getParameter("user_info_mbfg"));
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("usr_id", usr_id);
		map.put("usr_nm", usr_nm);
		map.put("usr_dept", usr_dept);
		map.put("usr_email", usr_email);
		map.put("usr_hp", usr_hp);
		map.put("usr_mailyn", usr_mailyn);
		map.put("usr_smsyn", usr_smsyn);
		
		HashMap<String, String> resultMap = new HashMap<String, String>();		
		String resultMsg = "";
		
		try {			
			userSettingDao.updateUserInfo(map);
			resultMsg = "수정되었습니다.";
		} catch (Exception e) {
			resultMsg = e.getMessage();
			e.getLocalizedMessage();
		} finally{
			resultMap.put("result", resultMsg);
			
			//JSON Object 변환
			JSONObject obj = JSONObject.fromObject(resultMap);
			res.setContentType("text/javascript;charset=UTF-8");
			PrintWriter pw = res.getWriter();
			pw.println(obj.toString());
			pw.flush();
			pw.close();
		}
	}

	/**
	 * 해당 유저의 DefaultPage에 해당하는 탑메뉴 아이디와 서브메뉴 아이디를 가져온다.
	 * @param  usr_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/userSetting/selectUserSystemCodeInfo.do")
	public void c_getUserSystemCodeInfo(HttpServletRequest req, HttpServletResponse res) throws  IOException{

		String parameter = StringUtil.nvl(req.getParameter("parameter"));
		org.json.JSONObject jsonObject = new org.json.JSONObject(parameter);
		
		/* Request Parameter Set */
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("usr_id",			StringUtil.nvl((String) jsonObject.get("usr_id")));
		map.put("usr_nm",			StringUtil.nvl((String) jsonObject.get("usr_nm")));
		map.put("usr_role",			StringUtil.nvl((String) jsonObject.get("usr_role")));
		
		List<UserSettingVo> list = null;   	
    	
		try {			
			list = (List<UserSettingVo>) userSettingDao.selectUserSystemCodeInfo(map);
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		@SuppressWarnings("rawtypes")
		HashMap<String,List> json = new HashMap<String,List>();
		json.put("list", list);
		
		JSONObject obj = JSONObject.fromObject(json);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}
	
	
	/**
	 * 해당 유저의 권한에 맞는 Top Menu Item Code를 가져온다.
	 * @param  usr_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/userSetting/selectUserTopMenu.do")
	public ModelAndView c_getUserTopMenuList(HttpServletRequest req, HttpServletResponse res){
    			
		String usr_id = req.getParameter("usr_id");
		String lang = req.getParameter("lang");
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("usr_id", usr_id);
		map.put("lang", lang);
		
		List<UserSettingVo> list = null;
    	
		try {			
			list = (List<UserSettingVo>) userSettingDao.selectUserTopMenu(map);			
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("itemList", list);
		mav.setViewName("comm/item_code");
		
		return mav;
	}
	
	/**
	 * Top 메뉴 중 해당 유저의 권한에 맞는 Sub Menu Item Code를 가져온다.
	 * @param  usr_id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/userSetting/selectUserSubMenu.do")
	public ModelAndView c_getUserSubMenuList(HttpServletRequest req, HttpServletResponse res){
    			
		String top_menu = req.getParameter("top_menu");
		String usr_id = req.getParameter("usr_id");
		String lang = req.getParameter("lang");
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("top_menu", top_menu);
		map.put("usr_id", usr_id);
		map.put("lang", lang);
		
		List<UserSettingVo> list = null;   	
    	
		try {			
			list = (List<UserSettingVo>) userSettingDao.selectUserSubMenu(map);			
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("itemList", list);
		mav.setViewName("comm/item_code");
		
		return mav;
	}
	
	/**
	 * 해당 유저의 권한에 맞는 Timer List를 가져온다.
	 * @param  usr_id
	 * @return 상단 메뉴
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/userSetting/selectUserTimerList.do")
	public ModelAndView c_getUserTimerList(HttpServletRequest req, HttpServletResponse res){
		String usr_id = req.getParameter("usr_id");
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("usr_id", usr_id);
		
		List<UserSettingVo> list = null;   	
    	
		try {			
			list = (List<UserSettingVo>) userSettingDao.selectUserTimerList(map);			
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
    	// 리턴할 값을 ModelAndView에 담는다.
		ModelAndView mav = new ModelAndView();
		mav.addObject("userTimerList",	 list);
		
		// 정해진 페이지로 값 리턴.
		mav.setViewName("comm/userSetting/userSettingList");
		
		return mav;
	}
	 
	/**
	 * 해당 유저의 로그인 페이지를 변경한다.
	 * @param  usr_id, subMenu
	 * @return 
	 * @throws IOException 
	 */
    @RequestMapping({ "/userSetting/updateUserLoginPage.do" })
    public ModelAndView c_saveMenuAccessUsers(HttpServletRequest req, HttpServletResponse res)  {
    	
    	//Request 공통 함수 호출  PARAMETER KEY VALUE Set
    	HashMap<String, String> map = RequestHandler.extractParameters(req);
		String usr_id  = StringUtil.nvl(req.getParameter("usr_id"));
		String menu_id = StringUtil.nvl(req.getParameter("sel_usrSubMenu"));
		
		map.put("usr_id", usr_id);
		map.put("menu_id", menu_id);
		
    	UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
    	map.put("in_loginId", userSession.user_id);
    	
    	try {
    		userSettingDao.updateUserLoginPage(map);		
    		
    		HashMap<String, Object> resParams = new HashMap<String, Object>();
    		resParams.put(ResultMsg.RESULT_MSG, ResultMsg.SUCCESS_COMPLITE);
    		
    		return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
    	} catch (Exception e) {
    		return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); 	// 예외 처리
    	}
    }
	/**
	 * 해당 화면갱신주기를 변경한다.
	 * @param  각 화면별 갱신주기
	 * @return 상단 메뉴
	 * @throws IOException 
	 */
	@RequestMapping("/userSetting/updateUserTimer.do")
	public ModelAndView c_updateUserTimer(HttpServletRequest req, HttpServletResponse res) throws  IOException{
		String dashboardTimer = StringUtil.nvl(req.getParameter("dashboardTimer"));
		String interfaceStatusTimer = StringUtil.nvl(req.getParameter("interfaceStatusTimer"));
		String alertTrayTimer = StringUtil.nvl(req.getParameter("alertTrayTimer"));
		String presentConditionTimer = StringUtil.nvl(req.getParameter("presentConditionTimer"));
		String processStatusTimer = StringUtil.nvl(req.getParameter("processStatusTimer"));
		String kpiInterfaceTimer = StringUtil.nvl(req.getParameter("kpiInterfaceTimer"));
		String systemResrouceTimer = StringUtil.nvl(req.getParameter("systemResrouceTimer"));
		
		ArrayList<String> key = new ArrayList<String>();
		key.add("dashboardTimer");
		key.add("interfaceStatusTimer");
		key.add("alertTrayTimer");
		key.add("presentConditionTimer");
		key.add("processStatusTimer");
		key.add("kpiInterfaceTimer");
		key.add("systemResrouceTimer");
		
		ArrayList<String> value = new ArrayList<String>();
		value.add(dashboardTimer);
		value.add(interfaceStatusTimer);
		value.add(alertTrayTimer);
		value.add(presentConditionTimer);
		value.add(processStatusTimer);
		value.add(kpiInterfaceTimer);
		value.add(systemResrouceTimer);
		
		/* 리턴할 값을 ModelAndView에 담는다. */
		ModelAndView mav = new ModelAndView();
		mav.addObject("key", key);
		mav.addObject("value", value);
		
		/* 정해진 페이지로 값 리턴 */
		mav.setViewName("comm/userSetting/UpdateUserSetting");
		return mav;
	}
	
	/**
	 * 해당 세션의 디폴트 페이지를 변경한다.(새로고침 시 해당 화면 유지를 위해)
	 * @param  usr_defaultPage
	 * @return 
	 * @throws IOException 
	 */
	@RequestMapping("/userSetting/updateSessionDefaultPage.do")
	public ModelAndView c_updateSessionDefaultPage(HttpServletRequest req, HttpServletResponse res) throws  IOException{
		String defaultPage = StringUtil.nvl(req.getParameter("usr_defaultPage"));
		
		ArrayList<String> key = new ArrayList<String>();
		key.add("loginPage");
		
		ArrayList<String> value = new ArrayList<String>();
		value.add(defaultPage);
		
		/* 리턴할 값을 ModelAndView에 담는다. */
		ModelAndView mav = new ModelAndView();
		mav.addObject("key", key);
		mav.addObject("value", value);
		
		/* 정해진 페이지로 값 리턴 */
		mav.setViewName("comm/userSetting/UpdateUserSetting");
		return mav;
	}
}