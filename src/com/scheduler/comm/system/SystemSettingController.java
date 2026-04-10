// src/com/scheduler/system/controller/SystemSettingController.java
package com.scheduler.comm.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;

@Controller
public class SystemSettingController {

    @RequestMapping({"/system/setSessionTimeout.do"})
    public void setSessionTimeout(HttpServletRequest req, HttpServletResponse res) {
        String minutesStr = req.getParameter("minutes");
        try {
			DataTableSettingVo resultVo = new DataTableSettingVo();
			
            int minutes = Integer.parseInt(minutesStr);
            if (minutes < 1 || minutes > 24 * 60) { // 1분 ~ 24시간 가드
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE,"invalid range 시간을 다시 설정해주세요.", resultVo);
                return;
            }
            resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_COMPLITE);
            // 런타임 적용(리스너가 sessionCreated에서 참조)
            req.getServletContext().setAttribute("SESSION_TIMEOUT_MINUTES", minutes);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getMessage(), null);
        }
    }
}
