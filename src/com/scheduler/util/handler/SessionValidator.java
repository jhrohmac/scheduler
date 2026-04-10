package com.scheduler.util.handler;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.login.service.UserSession;

import net.sf.json.JSONObject;

public class SessionValidator {

    /**
     * 세션 유효성 검사
     * 
     * @param req HttpServletRequest
     * @return ModelAndView 세션이 없을 경우 에러 응답을 담은 ModelAndView 반환, 세션이 유효하면 null 반환
     */
    public static ModelAndView validateUserSession(HttpServletRequest req) {
    	System.out.println("=======================sessionException=====ModelAndView==========================");
        UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        if (userSession == null) {
            ModelAndView mav = new ModelAndView();
            mav.addObject(ResultMsg.RESULT_CODE, ResultMsg.SESSION_FAILE_CODE);
            mav.addObject(ResultMsg.RESULT_MSG, ResultMsg.SESSION_FAILE_MSG);
            mav.setViewName("comm/result/result_view");
            return mav;
        }
        return null; // 세션이 유효하면 null 반환
    }
    
    /**
     * 공통 JSON 응답 처리 메서드
     * 
     * @param response HTTP 응답 객체
     * @param code     프로세스 결과 코드
     * @param message  프로세스 메시지
     * @param data     응답 데이터
     */
    public static void sessionException(HttpServletRequest req, HttpServletResponse response) {
    	 StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		try {
			System.out.println(" ** Session Check ======= "+stackTrace[2].getMethodName()+" ==================");
			
			UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
	    	if (userSession == null) {
	    		System.out.println("======= Failed to session Exception=====================");
	    		
	    		DataTableSettingVo resultVo = new DataTableSettingVo();
	        	StackTraceElement[] ste = new Throwable().getStackTrace();
	        	for(StackTraceElement element : ste) {
	      			System.out.println(element.getClassName() + "["+ element.getMethodName() +"]" + "("+ element.getLineNumber()+ ")");
	      		}
	        	System.out.println("======= Failed to session Exception=====================");
	            
	    		// 기본적으로 Result Code와 메시지를 설정
	            resultVo.setSystem_code(ResultMsg.SESSION_FAILE_CODE);
	        	resultVo.setSystem_msg(ResultMsg.SESSION_FAILE_MSG);	
	            JSONObject obj = JSONObject.fromObject(resultVo);
	            response.setContentType("text/javascript;charset=UTF-8");
	            PrintWriter pw = response.getWriter();
	            pw.println(obj.toString());
	            pw.flush();
	            pw.close();
	    	}
		  } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("Failed to session Exception: " + e.getMessage(), e);
        }
    }
}
