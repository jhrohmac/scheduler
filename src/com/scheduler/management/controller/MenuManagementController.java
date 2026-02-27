// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.management.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.comm.vo.MenuVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.MenuManagementDao;
import com.scheduler.management.vo.UserVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

@Controller
public class MenuManagementController
{
    public MenuManagementDao menuManagementDao;
    
    public MenuManagementDao getMenuManagementDao() {
        return this.menuManagementDao;
    }
    
    public void setMenuManagementDao(MenuManagementDao menuManagementDao) {
        this.menuManagementDao = menuManagementDao;
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping({ "/menu/selectMenuList.do" })
    public void c_selectMenuList(HttpServletRequest req, HttpServletResponse res){

        //Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
        	List<MenuVo> list = null;
            list = (List<MenuVo>)menuManagementDao.selectMenuList(map);
        
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

	/*
	 * Main Code Group List
	*/  
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/menu/selectMenuGroupList.do" })
    public ModelAndView c_getcodeGroupList(HttpServletRequest req, HttpServletResponse res)  {

        //Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
		try {
			List<ItemCodeVo> list;
			list = (List<ItemCodeVo>) menuManagementDao.selectMenuGroupList(map);
			
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("itemList", list);
			resParams.put("view_Name", "comm/code/item_code");
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	/*
	 * Main Code Group List
	 */  
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/menu/selectMenuAccessUsers.do" })
	public void c_selectMenuAccessUsers(HttpServletRequest req, HttpServletResponse res)  {
		
		//Request 공통 함수 호출  PARAMETER KEY VALUE Set
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		try {
			List<UserVo> list;
			list = (List<UserVo>) menuManagementDao.selectMenuAccessUsers(map);
			

        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setData(list);
        	resultVo.setRecordsFiltered(list.size());

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}

	/*
	 *메뉴 수정, 저장
	*/
    @RequestMapping({ "/menu/saveMenu.do" })
    public ModelAndView c_saveMenu(HttpServletRequest req, HttpServletResponse res)  {

        //Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
    	UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        map.put("in_userId", userSession.user_id);
        try {
        	this.menuManagementDao.save(map);
        	HashMap<String, Object> resParams = new HashMap<String, Object>();
        	resParams.put(ResultMsg.RESULT_MSG, ResultMsg.SUCCESS_COMPLITE);
        	
        	return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
        } catch (Exception e) {
        	return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); 	// 예외 처리
        }
    }

	/*
	 * Main Code Group List
	 */  
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/menu/checkMenuAccessUsers.do" })
	public void c_checkMenuAccessUsers(HttpServletRequest req, HttpServletResponse res)  {
		
		//Request 공통 함수 호출  PARAMETER KEY VALUE Set
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		try {
			List<UserVo> list;
			list = (List<UserVo>) menuManagementDao.checkMenuAccessUsers(map);
			

        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setData(list);
        	resultVo.setRecordsFiltered(list.size());

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}
    
    @RequestMapping({ "/menu/saveMenuAccessUsers.do" })
    public ModelAndView c_saveMenuAccessUsers(HttpServletRequest req, HttpServletResponse res)  {
    	
    	//Request 공통 함수 호출  PARAMETER KEY VALUE Set
    	HashMap<String, String> map = RequestHandler.extractParameters(req);
    	
    	UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
    	map.put("in_loginId", userSession.user_id);
    	
    	try {
    		this.menuManagementDao.saveMenuAccessUsers(map);
    		HashMap<String, Object> resParams = new HashMap<String, Object>();
    		resParams.put(ResultMsg.RESULT_MSG, ResultMsg.SUCCESS_COMPLITE);
    		
    		return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
    	} catch (Exception e) {
    		return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); 	// 예외 처리
    	}
    }

    @RequestMapping({ "/menu/deleteMenu.do" })
    public ModelAndView c_deleteMenu(HttpServletRequest req, HttpServletResponse res)  {

        //Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
        	this.menuManagementDao.delete(map);
        	HashMap<String, Object> resParams = new HashMap<String, Object>();
        	resParams.put(ResultMsg.RESULT_MSG, "정상 삭제 되었습니다.");
        	
        	return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
        } catch (Exception e) {
        	return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
        }
    }
    
    @RequestMapping({ "/menu/selectMenuCheck.do" })
    public void c_selectMenuCheck(HttpServletRequest req, HttpServletResponse res){
    	//Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        int cnt = 0;
        try {
            cnt = this.menuManagementDao.selectOneCnt(map);
            String resultCode = "";
            String resultMsg = "";
            if (cnt > 0) {
            	resultCode ="N";
            	resultMsg = "사용중인 메뉴 코드 입니다. 코드명을 변경해주세요.";
            }
            else {
            	resultCode = "사용 가능한 메뉴 코드 입니다.";
            	resultCode ="Y";
            }
            
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setResult_code(resultCode);
        	resultVo.setResult_msg(resultMsg);

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/menu/selectMenuSeq.do" })
    public void c_selectCodeSeq( HttpServletRequest req,  HttpServletResponse res){
    	//Request 공통 함수 호출  PARAMETER KEY VALUE Set
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
        	MenuVo menuVo = new MenuVo();
        	menuVo = menuManagementDao.selectMenuSeq(map);
        	
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(menuVo.getMenu_seq());

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
    }
    
    @RequestMapping({ "/menu/saveMenuOrder.do" })
    public void c_saveMenuOrder( HttpServletRequest req,  HttpServletResponse res){
    	String orderObj = StringUtil.nvl(req.getParameter("orderObj"));
    	try {
        	UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
            // JSON 데이터를 파싱
            JSONArray jsonArray = new JSONArray(orderObj);
            // 공통 함수 호출
            List<HashMap<String, String>> jsonList = RequestHandler.parseJSONArray(jsonArray);

            // 추가 값 설정 및 처리
            for (HashMap<String, String> map : jsonList) {
                map.put("in_userId", userSession.user_id);
            	menuManagementDao.saveMenuOrder(map);
            }
            
    		ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, null);
    	} catch (Exception e) {
    		e.printStackTrace();
    		ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getMessage(), null);
    	}
    }

    // ===== [TREE VIEW INTEGRATION] =====
    @SuppressWarnings("unchecked")
    @RequestMapping({ "/menu/selectTreeViewUsers.do" })
    public void c_selectTreeViewUsers(HttpServletRequest req, HttpServletResponse res){
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            List<UserVo> list = (List<UserVo>) menuManagementDao.selectTreeViewUsers(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping({ "/menu/selectMenuTree.do" })
    public void c_selectMenuTree(HttpServletRequest req, HttpServletResponse res){
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            List<MenuVo> list = (List<MenuVo>) menuManagementDao.selectAllMenus(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping({ "/menu/selectUserMenuAuth.do" })
    public void c_selectUserMenuAuth(HttpServletRequest req, HttpServletResponse res){
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            List<MenuVo> list = (List<MenuVo>) menuManagementDao.selectUserMenuAuth(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/menu/saveUserMenuAuth.do" })
    public ModelAndView c_saveUserMenuAuth(HttpServletRequest req, HttpServletResponse res){
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            final String userId = map.get("in_userId");
            final String menuIdsCsv = map.get("menuIds"); // CSV of MENU_SEQ
            menuManagementDao.deleteUserMenuAuth(map);   // delete all existing
            if (menuIdsCsv != null && menuIdsCsv.trim().length() > 0) {
                for (String mseq : menuIdsCsv.split(",")) {
                    HashMap<String, String> p = new HashMap<String, String>();
                    p.put("in_userId", userId);
                    p.put("in_menuSeq", mseq.trim());
                    menuManagementDao.insertUserMenuAuth(p);
                }
            }
            HashMap<String, Object> resParams = new HashMap<String, Object>();
            resParams.put(ResultMsg.RESULT_MSG, ResultMsg.SUCCESS_COMPLITE);
            return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams);
        } catch (Exception e) {
            return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null);
        }
    }
}
