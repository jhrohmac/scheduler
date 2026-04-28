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
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.StkSearchDao;
import com.scheduler.login.service.UserSession;
import com.scheduler.stock.dto.StkMasterDto;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

import net.sf.json.JSONObject;

@Controller
public class StkSearchController {

    public StkSearchDao stkSearchDao;

    public StkSearchDao getStkSearchDao() {
        return stkSearchDao;
    }

    public void setStkSearchDao(StkSearchDao stkSearchDao) {
        this.stkSearchDao = stkSearchDao;
    }

    @RequestMapping({ "/stkSearch/view.do" })
    public ModelAndView view(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("finance/stkSearch/stkSearch");
        return mav;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping({ "/stkSearch/selectList.do" })
    public void selectList(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        String draw   = req.getParameter("draw")   != null ? req.getParameter("draw")   : "0";
        String start  = req.getParameter("start")  != null ? req.getParameter("start")  : "0";
        String length = req.getParameter("length") != null ? req.getParameter("length") : "50";

        int size = 0;
        int total_cnt = 0;
        List<StkMasterDto> list = null;
    	try {
	        DataTableSettingVo resultVo = new DataTableSettingVo();
	        try {
	            UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
	            if (userSession == null) {
	                resultVo.setResult_code("-1");
	                resultVo.setResult_msg("Session이 종료되었습니다.");
	            } else {
	                list = (List<StkMasterDto>) stkSearchDao.selectStkSearchList(map);
	                size = list.size();
	                if (size > 0) {
	                    // totalCount 필드는 StkMasterDto에 추가 필요 → TOTAL_COUNT 컬럼 직접 map 파싱
	                }
	            }
	        } catch (Exception e) {
	            resultVo.setResult_code("-1");
	            resultVo.setResult_msg(e.getMessage());
	        }
	
	        if (list != null && size > 0) {
	            total_cnt = list.get(0).getTotalCount();
	        }
	
	        resultVo.setData(list);
	        resultVo.setDraw(draw);
	        resultVo.setStart_no(Integer.parseInt(start));
	        resultVo.setPage_length(Integer.parseInt(length));
	        resultVo.setRecordsFiltered(total_cnt);
	        resultVo.setRecordsTotal(total_cnt);

	        ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
    }
}
