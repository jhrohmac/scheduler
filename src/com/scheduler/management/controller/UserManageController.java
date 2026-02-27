package com.scheduler.management.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.Base64Util;
import com.scheduler.comm.util.SeedUtil;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.UserManagementDao;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

@Controller
public class UserManageController {
	
	private UserManagementDao userManagementDao;

	
	public UserManagementDao getUserManagementDao() {
		return userManagementDao;
	}

	public void setUserManagementDao(UserManagementDao userManagementDao) {
		this.userManagementDao = userManagementDao;
	}
	
	/** 
	 *  사용자 목록을 조회한다.
	 *  @param  조건  : 아이디, 이름, 부서, 권한, 메일/SMS 수신여부
	 *  @return 사용자 목록
	 * @throws IOException  
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/user/selectUserAll.do")
	public void c_selectUserAll(HttpServletRequest req, HttpServletResponse res){

		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
        	List<UserVo> list = (List<UserVo>) userManagementDao.selectUserAll(map);
            for (UserVo user : list) {
            	if (user.getHp_no() != null) {
            		user.setHp_no(SeedUtil.invokeDecrypt("tlsfk!2016", user.getHp_no(), "UTF-8"));
                }
            }
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);
			resultVo.setDraw(map.get("draw"));
			resultVo.setStart_no(Integer.parseInt(map.get("start")));
			resultVo.setPage_length(Integer.parseInt(map.get("length")));
			resultVo.setRecordsFiltered(list.isEmpty() ? 0 : list.get(0).getTotal_count());
			resultVo.setRecordsTotal(list.isEmpty() ? 0 : list.get(0).getTotal_count());
             
            ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
         } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
         }
	}
	
	/**
	 *  사용자 중복 체크
	 *  @param  조건  : 아이디
	 *  @return 사용자 등록 여부
	 * @throws IOException 
	 */
	@RequestMapping("/user/selectUserInfo.do")
	public void c_selectUserInfo(HttpServletRequest req, HttpServletResponse res){

		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		try {
			UserVo userVo = null;
			/* 조회 */
			userVo = (UserVo)userManagementDao.selectUserInfo(map);
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
			
			if(userVo == null) {
				resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
				resultVo.setResult_msg("사용 가능한 아이디 입니다.");
			}else {
				resultVo.setResult_code(ResultMsg.ERROR_CODE);
		    	resultVo.setResult_msg("사용중인 아이디 입니다.");	

				String user_pwd = Base64Util.decode(userVo.getPw());
				String hpno = SeedUtil.invokeDecrypt("tlsfk!2016", userVo.getHp_no(), "UTF-8");
				userVo.setPw(user_pwd);
				userVo.setHp_no(hpno);
				resultVo.setSingleData(userVo);
			}
		    ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}

	/*
	 * Main Code Group List
	*/  
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/user/userStartPageList.do" })
    public ModelAndView c_getcodeGroupList(HttpServletRequest req, HttpServletResponse res)  {

		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);

	    //PARAMETER KEY VALUE Setting
	    HashMap<String, String> map = RequestHandler.extractParameters(req);
	    map.put("in_loginId", userSession.user_id);

		try {
			List<ItemCodeVo> list;
			list = (List<ItemCodeVo>) userManagementDao.userStartPageList(map);
			
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("itemList", list);
			resParams.put("view_Name", "comm/code/item_code");
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	/**
	 *  사용자 패스워드 변경
	 *  @param  조건  : 아이디
	 *  @return 사용자 등록 여부
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping("/user/updatePw.do")
	public void updatePw(HttpServletRequest req, HttpServletResponse res) throws NoSuchAlgorithmException{
	
	    //PARAMETER KEY VALUE Setting
	    HashMap<String, String> map = RequestHandler.extractParameters(req);
	
		String up_pwd = Sha256Util.sha256(map.get("in_userNewPwd"));
		map.put("in_userPwd", up_pwd);
	
		try {
			/* 조회 */
			userManagementDao.updatePw(map);
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_PSW_CHG);
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}
	
	/**
	 *  사용자를 등록한다.
	 *  @return 성공유무
	 * @throws NoSuchAlgorithmException 
	 *  @throws IOException 
	 */
	@RequestMapping("/user/userEvent.do")
	public ModelAndView c_userEvent(HttpServletRequest req, HttpServletResponse res){
        UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);

	    //PARAMETER KEY VALUE Setting
	    HashMap<String, String> map = RequestHandler.extractParameters(req);
	    map.put("in_loginId", userSession.user_id);
	    
	    //패스워드 암호화
	    String user_pwd = Base64Util.encode(map.get("in_userPwd").getBytes());
	    map.put("in_userPwd", user_pwd);
		//핸드폰 암호화 
		String phone = SeedUtil.invokeEncrypt("tlsfk!2016",(map.get("in_userPhone")),"UTF-8");
		map.put("in_userPhone", phone);
		

        if (map.get("in_Role").equals("")) {
        	map.put("in_Role", "9"); 
        }
		try {
			userManagementDao.userEvent(map);
			if(map.get("in_userId").equals(userSession.user_id)) {
				userSession.defaultPage = map.get("in_userStartPage");
				userSession.user_color = map.get("in_userColor");
				userSession.use_end_date = map.get("in_userEndDate");
				userSession.user_avatar = map.get("in_userAvatar");
			}
		    return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, null); // 성공 처리
		} catch (Exception e) {
	        return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	/**
	 * 사용자 정보 삭제.
	 *  @param  조건 : usr_id
	 *  @return 성공유무
	 *  @throws IOException 
	 */
	@RequestMapping("/user/deleteUser.do")
	public ModelAndView c_deleteUser(HttpServletRequest req, HttpServletResponse res) {
        UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        
		/* 삭제조건 변수 */
		String del_usr_id = StringUtil.nvl(req.getParameter("deleteList"));
		String[] list_user  = del_usr_id.split(",");
		List<String> list = new ArrayList<String>();
		for(String user : list_user){
			list.add(user);
		}
		try {
			/* 삭제 */
			userManagementDao.delete(list, userSession.user_id);
		   return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, null); // 성공 처리
		} catch (Exception e) {
	       return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	/**
	 *  사용자 정보 수정 
	 *  @param  조건 : usr_id
	 *  @return 성공유무
	 *  @throws IOException 
	 */
	@RequestMapping("/user/updateExtensionDate.do")
	public ModelAndView c_updateExtensionDate(HttpServletRequest req, HttpServletResponse res) {

		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		try {
			/* 수정 */
			userManagementDao.updateExtensionDate(map);
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, null); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
}