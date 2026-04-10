package com.scheduler.management.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.management.dao.CodeManagementDao;
import com.scheduler.management.vo.CodeManagementVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

@Controller
public class CodeManagementController {
	
	private CodeManagementDao codeManagementDao;

	public CodeManagementDao getCodeManagementDao() {
		return codeManagementDao;
	}

	public void setCodeManagementDao(CodeManagementDao codeManagementDao) {
		this.codeManagementDao = codeManagementDao;
	}

	/*
	 * 코드 리스트
	*/
    @SuppressWarnings("unchecked")
	@RequestMapping({ "/code/selectCodeList.do" })
    public void c_selectCodeList(HttpServletRequest req, HttpServletResponse res){
    	//PARAMETER KEY VALUE Setting
    	HashMap<String, String> map = RequestHandler.extractParameters(req);
		
        try {
        	List<CodeManagementVo> list = null;
            list = (List<CodeManagementVo>)codeManagementDao.selectCodeList(map);
            
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

	/*
	 * 코드 중복 체크
	*/  
    @RequestMapping({ "/code/selectCodeCheck.do" })
    public void c_selectCodeCheck(HttpServletRequest req, HttpServletResponse res) {
    	String resultCode = "";
        String resultMsg = "";
        int cnt = 0;
      //PARAMETER KEY VALUE Setting
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
			cnt = this.codeManagementDao.selectOneCnt(map);
			if (cnt > 0) {
            	resultMsg = "사용중인 코드 입니다. 코드명을 변경해주세요.";
            	resultCode ="N";
            }else {
            	resultMsg = "사용 가능한 코드 입니다.";
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

	/*
	 * 코드 Sort Order 순번
	*/  
    @RequestMapping({ "/code/selectSortOrder.do" })
    public void c_selectSortOrder( HttpServletRequest req,  HttpServletResponse res) {
        //PARAMETER KEY VALUE Setting
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
        	CodeManagementVo codeVo = new CodeManagementVo();
        	codeVo = codeManagementDao.selectSortOrder(map);
        	
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(codeVo.getSort_order());
        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
    }
    
	/*
	 * Main Code Group List
	*/  
	@SuppressWarnings("unchecked")
	@RequestMapping({ "/code/selectCodeGroupList.do" })
    public ModelAndView c_getcodeGroupList(HttpServletRequest req, HttpServletResponse res)  {
	      //PARAMETER KEY VALUE Setting
        HashMap<String, String> map = RequestHandler.extractParameters(req);
    	
		try {
			List<ItemCodeVo> list;
			list = (List<ItemCodeVo>) codeManagementDao.selectCodeGroupList(map);
			
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("itemList", list);
			resParams.put("view_Name", "comm/code/item_code");
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
	
	/*
	 * 등록
	*/   
    @RequestMapping({ "/code/saveCode.do" })
    public ModelAndView c_saveCode(HttpServletRequest req, HttpServletResponse res)  {
    	UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
       
        //PARAMETER KEY VALUE Setting
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        map.put("in_userId", userSession.user_id);
        try {
        	this.codeManagementDao.save(map);
        	HashMap<String, Object> resParams = new HashMap<String, Object>();
        	resParams.put(ResultMsg.RESULT_MSG, ResultMsg.SUCCESS_COMPLITE);
        	
        	return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
        } catch (Exception e) {
        	return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); 	// 예외 처리
        }
    }

	/*
	 * 삭제
	*/    
    @RequestMapping({ "/code/deleteCode.do" })
    public ModelAndView c_deleteCode(HttpServletRequest req, HttpServletResponse res)  {
        //PARAMETER KEY VALUE Setting
        HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		try {
			this.codeManagementDao.delete(map);
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put(ResultMsg.RESULT_MSG, "정상 삭제 되었습니다.");
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
            
    }
    
}