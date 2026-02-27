
package com.scheduler.login.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.SeedUtil;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.util.UserMap;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.login.dao.LoginDao;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;
import com.scheduler.util.handler.SessionValidator;

@Controller
public class LoginController {

	private LoginDao loginDao;

	public LoginDao getLoginDao() {
		return this.loginDao;
	}

	public void setLoginDao(final LoginDao loginDao) {
		this.loginDao = loginDao;
	}

	@RequestMapping({ "/login.do" })
	public ModelAndView openLoginPage() {
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("login/login");
		return mav;
	}
	@RequestMapping({ "/register.do" })
	public ModelAndView registerPage() {
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("login/register");
		return mav;
	}
	@RequestMapping({ "/forgotPassword.do" })
	public ModelAndView forgotPasswordPage() {
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("login/forgotPassword");
		return mav;
	}

	@RequestMapping({ "/main.do" })
	public ModelAndView openMainPage(HttpServletRequest req, HttpServletResponse res) {
		try {
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			   // 기존 세션만 가져옴(없으면 null) - 새로 생성하지 않음
	        HttpSession session = req.getSession(false);
	        if (session == null) {
	        	final ModelAndView mav = new ModelAndView();
	        	mav.setViewName("login/login");
	    		return mav;
	        }
	        Object attr = session.getAttribute(UserSession.KEY);
	        if (!(attr instanceof UserSession)) {
	            // res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        	final ModelAndView mav = new ModelAndView();
	        	mav.setViewName("login/login");
	    		return mav;
	        }
	        UserSession userSession = (UserSession) attr;
			if(userSession != null) {
				resParams.put("user_id", 			userSession.user_id);
				resParams.put("user_nm", 			userSession.user_nm);
				resParams.put("user_avatar", 		userSession.user_avatar);
				resParams.put("email", 				userSession.email);
				//resParams.put("hp_no", 				SeedUtil.invokeDecrypt("tlsfk!2016", userSession.hp_no, "UTF-8"));
				resParams.put("desk_div", 			userSession.desk_div);
				resParams.put("employment_date", 	userSession.employment_date);
				resParams.put("default_page", 		userSession.defaultPage);
				resParams.put("user_color", 		userSession.user_color);
			}
			resParams.put("view_Name", "main/scheduler");
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}

	/**
	 * 사용자 정보의 등록 여부를 체크한다.
	 *
	 * @name_ko 사용자 등록 여부 조회 LoginController.c_userCheck(input);
	 * @param input 로그인 신규등록에서 중복체크 조회(사용자ID)
	 * @return 사용자 정보 결과
	 */
	@RequestMapping({ "/login/chkUser.do" })
	public void chkUser(HttpServletRequest req, HttpServletResponse res){
		String usr_id = StringUtil.nvl(req.getParameter("in_userId"));
		String result = "N";
		try {
			if (UserMap.getUserMap(usr_id) != null) {
				result = "Y";
			}
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(result);
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);

			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}
	
	/**
	 * 
	 * @name_ko LoginController.c_userCheck(input);
	 * @param input 세션 체크
	 * @return
	 */
	@RequestMapping({ "/session/sessionchk.do" })
	public void sessionchk(HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		SessionValidator.sessionException(req,res);
		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
		try {
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setSingleData(userSession.user_id);
			
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}


	/**
	 * 통합 모니터링 로그인.
	 *
	 * @name_ko 로그인 LoginController.c_login(input);
	 * @param input 로그인 신규등록에서 중복체크 조회(사용자ID)
	 * @return 사용자 정보 결과
	 * @throws NoSuchAlgorithmException
	 */

	@RequestMapping({ "/login/login.do" })
	public void getLogin(HttpServletRequest req, HttpServletResponse res) throws NoSuchAlgorithmException{

		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);

		String up_pwd = Sha256Util.sha256(map.get("in_userPwd"));
		map.put("in_userPwd", up_pwd);
		System.out.println(up_pwd);
		/* Response Parameter Set */
		String resultCode = "S";
		String resultMsg = null;
		
		try {
			UserSession userSession = null;
			userSession = loginDao.login(map);

			if (userSession == null || !StringUtil.nvl(userSession.user_id).equals(StringUtil.nvl(map.get("in_userId")))) {
				resultCode = "E";
				resultMsg = "로그인 정보가 올바르지 않습니다.";
			} else if (!"Y".equalsIgnoreCase(StringUtil.nvl(userSession.use_yn, "Y"))) {
				resultCode = "E";
				resultMsg = "사용이 중지된 계정입니다. 관리자에게 문의하세요.";
			} else if (userSession.err_message != null) {
				resultCode = "E";
				resultMsg = userSession.err_message;
			} else {
				// 정상 로그인: 세션에 UserSession 저장
				HttpSession session = req.getSession(true);
				session.setAttribute(UserSession.KEY, userSession);

				// 세션 타임아웃 설정 - DB(UserSession.session_login_time) 값으로 적용
				String sessStr = userSession.session_login_time;
				int minutes = 60; // 기본값(분)
				try {
					if (sessStr != null && sessStr.trim().length() > 0) {
						minutes = Integer.parseInt(sessStr.trim());
					}
				} catch (NumberFormatException ignore) { /* 기본값 유지 */ }
				if (minutes < 1) minutes = 1;
				if (minutes > 24 * 60) minutes = 24 * 60; // 최대 24시간 방어

				session.setMaxInactiveInterval(minutes * 60);
				// 운영단/필터 등에서 재사용할 수 있도록 세션에도 보관
				session.setAttribute("SESSION_TIMEOUT_MINUTES", minutes);

				resultMsg = "로그인 되었습니다.";
			}
			
			// 2013.07.15 동일 ID 로그인 체크(host controller 수정)
//			UserMap.setUserMap(map.get("in_userId"), req.getSession());
//
//			if (req.getSession().getAttribute(UserSession.KEY) == null) {
//				req.getSession().setAttribute(UserSession.KEY, userSession);
//			}else {
//				req.getSession().setAttribute(UserSession.KEY, userSession);
//				req.setAttribute(ResultMsg.RESULT_MSG, "로그인이 정상적으로 처리되었습니다.");
//				req.setAttribute(ResultMsg.WINDOW_ALERT, "NO");
//				
//				//세션 타임아웃 설정
//				int minutes = 30; // 계정/권한에 따라 계산
//				req.getSession().setMaxInactiveInterval(minutes * 60);
//			}

			System.out.println("resultMsg================"+resultMsg);
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(resultCode);
			resultVo.setResult_msg(resultMsg);
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}

	/**
	 * 사용기간 만료 연장
	 *
	 * @name_ko 사용기간 만료 연장 LoginController.c_login(input);
	 * @param input (사용자ID)(사용기간 만료)
	 * @return 사용자 정보 결과
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping({ "/login/sessionKeepAlive.do" })
	public void sessionKeepAlive(HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		SessionValidator.sessionException(req,res);
		/* Response Parameter Set */
		String resultCode = null;
		String resultMsg = null;
		try {
			HttpSession session = req.getSession(false);
			if (session != null) {
				Object ext = session.getAttribute("SESSION_TIMEOUT_MINUTES");
				int minutes = 60;
				if (ext instanceof Integer) {
					minutes = (Integer) ext;
				} else if (ext instanceof String) {
					try { minutes = Integer.parseInt(((String) ext).trim()); } catch (Exception ignore) {}
				}
				if (minutes < 1) minutes = 1;
				if (minutes > 24 * 60) minutes = 24 * 60;
				session.setMaxInactiveInterval(minutes * 60);
			}
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(resultCode);
			resultVo.setResult_msg(resultMsg);
			
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}
	/**
	 * 사용기간 만료 연장
	 *
	 * @name_ko 사용기간 만료 연장 LoginController.c_login(input);
	 * @param input (사용자ID)(사용기간 만료)
	 * @return 사용자 정보 결과
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping({ "/login/extensionDate.do" })
	public void getExtensionDate(HttpServletRequest req, HttpServletResponse res) throws IOException {
		/*세션 검증 */
		SessionValidator.sessionException(req,res);
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		String extension_date = map.get("extension_date");
		
		/* Response Parameter Set */
		String resultCode = null;
		String resultMsg = null;

		try {
			UserVo userVo = null;
			userVo = (UserVo) loginDao.selectUserCheck(map);

			// if(userVo.getExtension_date()==null ||
			// userVo.getExtension_date().equals("")){
			loginDao.setExtensionDate(map);
			resultCode = "S";
			resultMsg = extension_date + "일로 연장요청 되었습니다.";
			
			
			HttpSession session = req.getSession(false);
			if (session != null) {
				Object ext = session.getAttribute("SESSION_TIMEOUT_MINUTES");
				int minutes = 60;
				if (ext instanceof Integer) {
					minutes = (Integer) ext;
				} else if (ext instanceof String) {
					try { minutes = Integer.parseInt(((String) ext).trim()); } catch (Exception ignore) {}
				}
				if (minutes < 1) minutes = 1;
				if (minutes > 24 * 60) minutes = 24 * 60;
				session.setMaxInactiveInterval(minutes * 60);
			}
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setSingleData(userVo);
			resultVo.setResult_code(resultCode);
			resultVo.setResult_msg(resultMsg);

			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}

	/**
	 * 사용자 중복 체크
	 *
	 * @name_ko 사용자 중복 체크
	 * @param input (사용자ID)
	 * @return 사용자 정보 결과
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping({ "/register/registerUserCheck.do" })
	public void c_registerUserCheck(HttpServletRequest req, HttpServletResponse res) throws IOException {
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		/* Response Parameter Set */
		String resultCode = ResultMsg.SUCCESS_CODE;
		String resultMsg = "";

		try {
			UserVo userVo = null;
			userVo = (UserVo) loginDao.selectUserCheck(map);
			if(userVo == null) {
				resultCode = "9999";
				resultMsg = "등록되지 않은 USER ID 이거나 E-mail 입니다.";
			}else {
				resultMsg = "USER 정보가 확인되었습니다.";
			}
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setSingleData(userVo);
			resultVo.setResult_code(resultCode);
			resultVo.setResult_msg(resultMsg);

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
	@RequestMapping("/register/registerUser.do")
	public void c_registerUser(HttpServletRequest req, HttpServletResponse res){
	    //PARAMETER KEY VALUE Setting
	    HashMap<String, String> map = RequestHandler.extractParameters(req);
		try {
			//패스워드 암호화
			String up_pwd = Sha256Util.sha256(map.get("in_userPwd"));
			map.put("in_userPwd", up_pwd);
			
			loginDao.registerUser(map);
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
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
	@RequestMapping("/register/resetPassword.do")
	public void c_resetPassword(HttpServletRequest req, HttpServletResponse res){
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		try {
			//패스워드 암호화
			String up_pwd = Sha256Util.sha256("Welcome!2");
			map.put("in_userPwd", up_pwd);
			loginDao.resetPassword(map);
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_COMPLITE);
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}
	
	@RequestMapping({ "/logout.do" })
	public String doLogout(HttpServletRequest req, HttpServletResponse res) {
	    try {
	        // 매핑 제거(현재 세션 기준)
	        UserMap.delUserMapBySession(req.getSession(false));
	        // 세션 무효화(고정 세션 공격 방지)
	        HttpSession s = req.getSession(false);
	        if (s != null) s.invalidate();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "login/login";
	}

}