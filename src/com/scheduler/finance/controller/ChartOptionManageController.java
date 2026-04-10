// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.exp.SchedulerException;
import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.ChartOptionManageDao;
import com.scheduler.finance.vo.ChartOptionVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

import net.sf.json.JSONObject;

@Controller
public class ChartOptionManageController
{
    public ChartOptionManageDao chartOptionManageDao;
    
    public ChartOptionManageDao getChartOptionManageDao() {
        return this.chartOptionManageDao;
    }
    
    public void setChartOptionManageDao( ChartOptionManageDao chartOptionManageDao) {
        this.chartOptionManageDao = chartOptionManageDao;
    }
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectOptionGroup.do")
    public ModelAndView selectOptionGroup(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		
		
    	ModelAndView mav = new ModelAndView();
    	List<ChartOptionVo> list;
    	
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("flag", req.getParameter("flag"));
    	
		try {			
			list = (List<ChartOptionVo>) chartOptionManageDao.selectOptionGroup(map);
		} catch (Exception e) {
			throw new SchedulerException(this.getClass().getName()+".selectOptionGroup : " + e.getLocalizedMessage());
		}
				
		mav.addObject("optionList", list);
		mav.setViewName("/finance/chartOption/item_code");
		
		return mav;
	}	

    @SuppressWarnings("unchecked")
	@RequestMapping({ "/finance/selectOptionList.do" })
    public void c_selectOptionList(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
    	
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		List<ChartOptionVo> list = null;
		
		try {
			
			list = (List<ChartOptionVo>) chartOptionManageDao.selectOptionList(map);
			System.out.println("selectOptionList====="+list.size());

            DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setData(list);
        	resultVo.setDraw(map.get("draw"));
            resultVo.setStart_no(Integer.parseInt(map.get("start")));
        	resultVo.setPage_length(Integer.parseInt(map.get("length")));
        	resultVo.setRecordsFiltered(list.size());
        	resultVo.setRecordsTotal(list.isEmpty() ? 0 : Integer.parseInt(list.get(0).getTotalcnt()));

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
    }
    
    @RequestMapping({ "/finance/saveOption.do" })
    public ModelAndView c_saveOption(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
        UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
        String use_yn = req.getParameter("useyn");
        if (use_yn.equals("true")) {
            use_yn = "Y";
        }else{
            use_yn = "N";
        }
        String defaultyn = req.getParameter("defaultyn");
        if (defaultyn.equals("true")) {
        	defaultyn = "Y";
        }
        else {
        	defaultyn = "N";
        }
        String in_optionparent = req.getParameter("in_optionparent");
        if (in_optionparent.equals("null")) {
            in_optionparent = "";
        }
        
        //PARAMETER KEY VALUE Setting
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        map.put("in_optionparent", in_optionparent);
        map.put("in_useyn", use_yn);
        map.put("in_default", defaultyn);
        map.put("in_user_id", userSession.user_id);
        try {
            this.chartOptionManageDao.save(map);
            HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("view_Name", "comm/result/result_msg_script");
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
    }
    
    @RequestMapping({ "/finance/deleteOption.do" })
    public ModelAndView deleteOption(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("in_optionseq", req.getParameter("in_optionseq"));
        map.put("in_user_id", userSession.user_id);
        try {
            chartOptionManageDao.delete(map);
            HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("view_Name", "comm/result/result_msg_script");
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
    }
    
    @RequestMapping({ "/finance/selectOptionCheck.do" })
    public void c_selectOptionCheck( HttpServletRequest req,  HttpServletResponse res) throws SchedulerException, IOException {
        String resultMsg = "";
        int cnt = 0;
         HashMap<String, String> map = new HashMap<String, String>();
        map.put("option_id", req.getParameter("option_id"));
        try {
            cnt = this.chartOptionManageDao.selectOneCnt(map);
            if (cnt > 0) {
            	resultMsg = "사용중인 메뉴 코드 입니다. 코드명을 변경해주세요.";
            }
            else {
            	resultMsg = "사용 가능한 메뉴 코드 입니다.";
            }
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(resultMsg);

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/finance/selectOptionSeq.do" })
    public void c_selectOptionSeq( HttpServletRequest req,  HttpServletResponse res) throws SchedulerException, IOException {
        ChartOptionVo vo = new ChartOptionVo();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("grid_div", req.getParameter("grid_div"));
        map.put("parent_id", req.getParameter("parent_id"));
        try {
        	vo = chartOptionManageDao.selectOptionSeq(map);
        	
            HashMap<String, String> resultMap = new HashMap<String, String>();
            resultMap.put("option_seq", vo.getOption_seq());
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(resultMap);
            resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
            resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
    }
}
