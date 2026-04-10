package com.scheduler.util.handler;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.scheduler.comm.vo.DataTableSettingVo;

import net.sf.json.JSONObject;


public class ResponseHandler {
    /**
     * 예외 처리 메서드
     * 
     * @param e       발생한 예외
     * @param methodName 호출된 메서드 이름
     * @return ModelAndView 예외 정보를 담은 응답 객체
     */
    public static ModelAndView handleException(String code, Exception e, String methodName, Map<String, Object> additionalParams) {
    	ModelAndView mav = new ModelAndView();
    	try {
	        
	        mav.addObject(ResultMsg.RESULT_CODE, code);
	        mav.addObject(ResultMsg.RESULT_MSG, e.getLocalizedMessage());
	        System.out.println("============handleException===========");
	        System.out.println("RESULT_CODE : "+code);
	        System.out.println("RESULT_MSG : "+e.getLocalizedMessage());
	        e.printStackTrace();
	        System.out.println("============handleException===========");
	        // Add the additional parameters to the ModelAndView
	        if (additionalParams != null) {
	            for (Map.Entry<String, Object> entry : additionalParams.entrySet()) {
	                mav.addObject(entry.getKey(), entry.getValue());
	            }
	        }
	        mav.setViewName("comm/result/result_view");
	    }catch(Exception exception){
	    	exception.printStackTrace();
			throw new RuntimeException(methodName + " : " + exception.getLocalizedMessage(), e); // 예외 래핑 후 재던짐
		}
		return mav;
    }
    
    /**
     * 성공 처리 메서드
     * 
     * @return ModelAndView 성공 정보를 담은 응답 객체
     */
    public static ModelAndView handleSuccess(String code, String message, HashMap<String, Object> params) {
        ModelAndView mav = new ModelAndView();
        System.out.println("===============handleSuccess==================");
        mav.addObject(ResultMsg.RESULT_CODE, code);
        mav.addObject(ResultMsg.RESULT_MSG, message);
        
        boolean orderview = false;
        // Add the additional parameters to the ModelAndView
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                mav.addObject(entry.getKey(), entry.getValue());
                if(entry.getKey().equals("view_Name")) {
                	mav.setViewName((String)entry.getValue());
                	orderview = true;
                }
            }
        }
        if(!orderview) {
        	mav.setViewName("comm/result/result_view");
        }
        return mav;
    }
    
    /**
     * 공통 JSON 응답 처리 메서드
     * 
     * @param response HTTP 응답 객체
     * @param code     프로세스 결과 코드
     * @param message  프로세스 메시지
     * @param data     응답 데이터
     */
    public static void sendResponse(HttpServletResponse response, String code, String message, DataTableSettingVo data) {
        try {
        	
        	//System.out.println("================================sendResponse====================================");
//        	StackTraceElement[] ste = new Throwable().getStackTrace();
//        	for(StackTraceElement element : ste) {
//      			System.out.println(element.getClassName() + "["+ element.getMethodName() +"]" + "("+ element.getLineNumber()+ ")");
//      		}
        	
        	// Null 방지 처리
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	if(data == null) {
        		resultVo.setSystem_code(code);
                resultVo.setSystem_msg(message);
        	}else {
        		BeanUtils.copyProperties(data,resultVo);
        		resultVo.setSystem_code(code);
        		resultVo.setSystem_msg(message);
        	}
        	
//        	System.out.println("getResult_code========="+resultVo.getResult_code());
//        	System.out.println("getResult_msg========="+resultVo.getResult_msg());
//        	System.out.println("getSystem_code========="+resultVo.getSystem_code());
//        	System.out.println("getSystem_msg========="+resultVo.getSystem_msg());

            JSONObject obj = JSONObject.fromObject(resultVo);
            //2025-09-16
            //response.setContentType("text/javascript;charset=UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter pw = response.getWriter();
            pw.println(obj.toString());
            pw.flush();
            pw.close();
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("Failed to send JSON response: " + e.getMessage(), e);
        }
    }
}
