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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.exp.SchedulerException;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.util.UserSessionCheck;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.finance.dao.CodeManageDao;
import com.scheduler.finance.vo.CodeVo;
import com.scheduler.login.service.UserSession;

import net.sf.json.JSONObject;

@Controller
public class CodeManageController
{
    public CodeManageDao codeManageDao;
    
    public CodeManageDao getCodeManageDao() {
        return this.codeManageDao;
    }
    
    public void setCodeManageDao( CodeManageDao codeManageDao) {
        this.codeManageDao = codeManageDao;
    }
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/codeGroupCode.do")
    public ModelAndView c_getcodeGroupCode(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		
		
    	ModelAndView mav = new ModelAndView();
    	List<CodeVo> list;
    	
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("flag", req.getParameter("flag"));
    	
		try {			
			list = (List<CodeVo>) codeManageDao.selectCodeGroup(map);
		} catch (Exception e) {
			throw new SchedulerException(this.getClass().getName()+".c_getItemCode : " + e.getLocalizedMessage());
		}
				
		mav.addObject("codeList", list);
		mav.setViewName("/finance/stockCode/item_code");
		
		return mav;
	}	

    @SuppressWarnings("unchecked")
	@RequestMapping({ "/finance/selectCodeList.do" })
    public void c_selectCodeList(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		String sel_codename = StringUtil.nvl(req.getParameter("sel_codename"));
		String sel_codediv = StringUtil.nvl(req.getParameter("sel_codediv"));
		String sel_useyn = StringUtil.nvl(req.getParameter("sel_useyn"));
		String sel_parentid = StringUtil.nvl(req.getParameter("sel_parentid"));
		String draw = StringUtil.nvl(req.getParameter("draw"));
		String start = StringUtil.nvl(req.getParameter("start"));
		String length = StringUtil.nvl(req.getParameter("length"));
		
		int totalPagesize = Integer.parseInt(start) + Integer.parseInt(length);
		int currentPageSize = Integer.parseInt(start);
		int size = 0;
		int total_cnt = 0;
		List<CodeVo> list = null;
		DataTableSettingVo resultVo = new DataTableSettingVo();
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("sel_codename", sel_codename);
		map.put("sel_codediv", sel_codediv);
		map.put("sel_useyn", sel_useyn);
		map.put("sel_parentid", sel_parentid);
		map.put("totalPagesize", String.valueOf(totalPagesize));
		map.put("currentPageSize", String.valueOf(currentPageSize));
		try {
			UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
			if (userSession == null) {
				resultVo.setResult_code("-1");
				resultVo.setResult_msg("Session이 종료되었습니다.");
			}
			list = (List<CodeVo>) codeManageDao.selectCodeList(map);
			size = list.size();
			if (size > 0) {
				total_cnt = Integer.parseInt(list.get(0).getTotalcnt());
			}
		} catch (Exception e) {
			resultVo.setResult_code("-1");
			resultVo.setResult_msg(e.getMessage());
		}
		 
		resultVo.setData(list);
		resultVo.setDraw(draw);
		resultVo.setStart_no(Integer.parseInt(start));
		resultVo.setPage_length(Integer.parseInt(length));
		resultVo.setRecordsFiltered(total_cnt);
		resultVo.setRecordsTotal(total_cnt);
		JSONObject obj = JSONObject.fromObject((Object) resultVo);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
    }
    
    @RequestMapping({ "/finance/saveCode.do" })
    public ModelAndView c_saveCode(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
    	 ModelAndView mav = new ModelAndView();
         UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
         if (userSession == null) {
         	mav.addObject(ResultMsg.RESULT_CODE, "error");
         	mav.addObject(ResultMsg.RESULT_MSG, "Session이 종료되었습니다.");
 			mav.setViewName("comm/result/sessionError");
             return mav;
         }
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
        String in_codeparent = req.getParameter("in_codeparent");
        if (in_codeparent.equals("null")) {
            in_codeparent = "";
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("in_codeseq", req.getParameter("in_codeseq"));
        map.put("in_sortorder", req.getParameter("in_sortorder"));
        map.put("in_codeparent", in_codeparent);
        map.put("in_codeid", req.getParameter("in_codeid"));
        map.put("in_codeicon", req.getParameter("in_codeicon"));
        map.put("in_codename", req.getParameter("in_codename"));
        map.put("in_codevalue", req.getParameter("in_codevalue"));
        map.put("in_codeaccess", req.getParameter("in_codeaccess"));
        map.put("in_useyn", use_yn);
        map.put("in_default", defaultyn);
        map.put("in_codedesc", req.getParameter("in_codedesc"));
        map.put("in_eventdiv", req.getParameter("in_eventdiv"));
        map.put("in_user_id", userSession.user_id);
        try {
            this.codeManageDao.save(map);
        }
        catch (Exception e) {
            throw new SchedulerException(String.valueOf(this.getClass().getName()) + ".c_insertCode : " + e.getLocalizedMessage());
        }
        mav.addObject(ResultMsg.RESULT_CODE, "success");
        mav.addObject(ResultMsg.RESULT_MSG, "정상 등록 되었습니다.");
        mav.setViewName("comm/result/result_msg_script");
        return mav;
    }
    
    @RequestMapping({ "/finance/deleteCode.do" })
    public ModelAndView deleteCode(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		ModelAndView mav = new ModelAndView();
		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
		if (userSession == null) {
			mav.addObject(ResultMsg.RESULT_CODE, "error");
			mav.addObject(ResultMsg.RESULT_MSG, "Session이 종료되었습니다.");
			mav.setViewName("comm/result/sessionError");
			return mav;
		}

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("in_codeseq", req.getParameter("in_codeseq"));
        map.put("in_user_id", userSession.user_id);
        try {
            codeManageDao.delete(map);
        }
        catch (Exception e) {
            throw new SchedulerException(String.valueOf(this.getClass().getName()) + ".delete : " + e.getLocalizedMessage());
        }
        mav.addObject(ResultMsg.RESULT_CODE, "success");
        mav.addObject(ResultMsg.RESULT_MSG, "정상 삭제 되었습니다.");
        mav.setViewName("comm/result/result_msg_script");
        return mav;
    }
    
    @RequestMapping({ "/finance/selectCodeCheck.do" })
    public void c_selectCodeCheck( HttpServletRequest req,  HttpServletResponse res) throws SchedulerException, IOException {
        String resultMsg = "";
        int cnt = 0;
         HashMap<String, String> map = new HashMap<String, String>();
        map.put("code_id", req.getParameter("code_id"));
        try {
            cnt = this.codeManageDao.selectOneCnt(map);
            if (cnt > 0) {
            	resultMsg = "사용중인 메뉴 코드 입니다. 코드명을 변경해주세요.";
            }
            else {
            	resultMsg = "사용 가능한 메뉴 코드 입니다.";
            }
        }
        catch (Exception e) {
            throw new SchedulerException(String.valueOf(this.getClass().getName()) + ".c_selectCodeCheck : " + e.getLocalizedMessage());
        }
         HashMap<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("result", resultMsg);
         JSONObject obj = JSONObject.fromObject((Object)resultMap);
        res.setContentType("text/javascript;charset=UTF-8");
         PrintWriter pw = res.getWriter();
        pw.println(obj.toString());
        pw.flush();
        pw.close();
    }

    @RequestMapping({ "/finance/selectCodeSeq.do" })
    public void c_selectCodeSeq( HttpServletRequest req,  HttpServletResponse res) throws SchedulerException, IOException {
        System.out.println("===="+req.getParameter("parent_id"));
        CodeVo vo = new CodeVo();
         HashMap<String, String> map = new HashMap<String, String>();
        map.put("grid_div", req.getParameter("grid_div"));
        map.put("parent_id", req.getParameter("parent_id"));
        try {
        	vo = codeManageDao.selectCodeSeq(map);
        }
        catch (Exception e) {
            throw new SchedulerException(String.valueOf(this.getClass().getName()) + ".selectCodeSeq : " + e.getLocalizedMessage());
        }
         HashMap<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("code_seq", vo.getCode_seq());
         JSONObject obj = JSONObject.fromObject((Object)resultMap);
        res.setContentType("text/javascript;charset=UTF-8");
         PrintWriter pw = res.getWriter();
        pw.println(obj.toString());
        pw.flush();
        pw.close();
    }
}
