package com.scheduler.comm.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.dao.MenuDao;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.MenuVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;
import com.scheduler.util.handler.SessionValidator;

@Controller
public class MenuController  {
	
	private MenuDao menuDao;

	
	public MenuDao getMenuDao() {
		return menuDao;
	}

	public void setMenuDao(MenuDao menuDao) {
		this.menuDao = menuDao;
	}
	
	/**
	 * Main 화면의 좌측 Sub 메뉴를 가져온다.
	 * @param  usr_id, tot_menu
	 * @return 좌측 메뉴
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/menu/setMenu.do" })
	public void c_getSubMenuList(HttpServletRequest req, HttpServletResponse res) throws IOException{
			
		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);

    	List<MenuVo> list = null;

		String in_userId = userSession.user_id;
		String defaultPage = userSession.defaultPage;
		
    	HashMap<String, String> map = new HashMap<String, String>();
		map.put("in_userId", in_userId);
		try {
			
			list = (List<MenuVo>) menuDao.selectSetMenu(map);
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setData(list);
			resultVo.setSingleData(defaultPage);
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
		}
	}
	
	/**
	 * Main View 상단 메뉴를 가져온다.
	 * @param  usr_id
	 * @return 상단 메뉴
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/menu/selPageMove.do" })
	public ModelAndView c_selPageMove(HttpServletRequest req, HttpServletResponse res){
		
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
		String in_Role = userSession.role;
		String lang = userSession.lang;
		map.put("in_userId", userSession.user_id);
		map.put("in_Role", in_Role);
		map.put("lang", lang);
		
		List<MenuVo> list = null;
		
        String page_url = "";
        String popup_flag = "";
		
		try {
			list = (List<MenuVo>) menuDao.selectPageMove(map);
			
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			if (list.size() > 0) {
                page_url = list.get(0).getUrl();
                popup_flag = list.get(0).getPopup_flag();
                //POPUP Page Set
                if (popup_flag.equals("Y")) {
                	resParams.put("pop_url", list.get(0).getUrl());
                    page_url = "comm/result/result_popup";
                }
			}else {
				// Example map with additional parameters
		        resParams.put("error_code", "404");
		        resParams.put("error_msg", "");
		        resParams.put("error_dtl", "페이지가 존재하지 않습니다.");
		        resParams.put("default_page", (userSession == null ? "" : userSession.defaultPage) );
		        page_url = "comm/result/error_view";
			}
			
			resParams.put("user_color", userSession.user_color);
			resParams.put("view_Name", page_url);
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	
	/**
	 * Main View 상단 메뉴를 가져온다.
	 * @param  usr_id
	 * @return 상단 메뉴
	 * @throws IOException 
	 */
	@RequestMapping({ "/menu/selectPageInfo.do" })
	public void c_selPageTitle(HttpServletRequest req, HttpServletResponse res) throws IOException{
		
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		String in_menuId = req.getParameter("menu_id");
		String in_userId = userSession.user_id;
		String lang = userSession.lang;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("in_userId", in_userId);
		map.put("in_menuId", in_menuId);
		map.put("lang", lang);
		
		try {
			MenuVo menuVo = null;
			menuVo = (MenuVo) menuDao.selectPageInfo(map);
			
			DataTableSettingVo resultVo = new DataTableSettingVo();
			if (menuVo != null) {
				resultVo.setSingleData(menuVo);
				resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
				resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			}else {
				resultVo.setResult_code(ResultMsg.ERROR_CODE);
				resultVo.setResult_msg("권한이 없거나 페이지가 없습니다.");
			}
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
         } catch (Exception e) {
            ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
         }
	}
	
	@RequestMapping({ "/menu/contents.do" })
	public ModelAndView c_getContents(HttpServletRequest req, HttpServletResponse res){
		String contents_url = req.getParameter("contents_url");
		try {
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("view_Name", contents_url);
		
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	/**
	 * Page not found
	 * @param req
	 * @param res
	 * @return
	 * @
	 */
	@RequestMapping({ "/comm/result/error_view.do" })
	public ModelAndView c_error_pageView(HttpServletRequest req, HttpServletResponse res){				
		//Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        System.err.println(map);
		String error_code = req.getParameter("error_code");
		String error_msg = req.getParameter("error_msg");
	    String rawDtl = req.getParameter("error_dtl");
	    
	    String error_dtl = "";
	    if (rawDtl != null && !rawDtl.isEmpty()) {
	        // 줄바꿈(\n) 또는 세미콜론(;) 기준으로 split
	        String[] parts = rawDtl.split("\\r?\\n|;");
	        error_dtl = parts[0].trim();
	        int start = rawDtl.toLowerCase().indexOf("<title>");
	        int end   = rawDtl.toLowerCase().indexOf("</title>");
	        if (start != -1 && end > start) {
	            error_dtl = rawDtl.substring(start + 7, end).trim();
	        } else {
	            // title 태그가 없으면 첫 번째 줄만
	            int nl = rawDtl.indexOf("\n");
	            if (nl > 0) {
	                error_dtl = rawDtl.substring(0, nl).trim();
	            }
	        } 
	    }
	    
		System.out.println("===================error_view====================");
		System.out.println("error_code : "+error_code);
		System.out.println("error_msg : "+error_msg);
		System.out.println("error_dtl : "+error_dtl);
		System.out.println("===================error_view====================");
		try {
			UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
			// Example map with additional parameters
	        HashMap<String, Object> resParams = new HashMap<String, Object>();
	        resParams.put("error_code", error_code);
	        resParams.put("error_msg", error_msg);
	        resParams.put("error_dtl", error_dtl);
	        resParams.put("default_page", (userSession == null ? "" : userSession.defaultPage) );
	        resParams.put("view_Name", "comm/result/error_view");
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
}