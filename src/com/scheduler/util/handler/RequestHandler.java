package com.scheduler.util.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.scheduler.comm.util.StringUtil;

public class RequestHandler {

    /**
     * HttpServletRequest에서 파라미터를 추출하여 HashMap으로 반환하는 공통 함수
     * 
     * @param req HttpServletRequest 객체
     * @return 요청 파라미터가 담긴 HashMap
     */
    public static HashMap<String, String> extractParameters(HttpServletRequest req) {
    	
    	HashMap<String, String> map = new HashMap<>();
    	try {
	        Map<String, String[]> reqParameter = req.getParameterMap();
	        
	        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
	        System.out.println(" ** extractParameters ======="+stackTrace[2].getMethodName()+"=======");
	        
	        for (Map.Entry<String, String[]> entry : reqParameter.entrySet()) {
	            String key = entry.getKey();
	            String[] values = entry.getValue();
	            
	            if (values.length > 0) {
	            	if(!key.contains("columns")) {
	            		System.out.println("key:'"+key+"'  value:'"+values[0]+"'");
	            	}
	            	//Null Check
	            	String value = (values[0].equals("null"))? "" : values[0];
	                map.put(key, value); // 첫 번째 값을 저장
	            }
	        }
	        
	    	if(map.containsKey("start")) {
	    		if(!map.get("start").equals("") && !map.get("length").equals("")) {
		        	int totalPagesize = Integer.parseInt(map.get("start")) + Integer.parseInt(map.get("length"));
		            int currentPageSize = Integer.parseInt(map.get("start"));
		        	map.put("totalPagesize", String.valueOf(totalPagesize));
		            map.put("currentPageSize", String.valueOf(currentPageSize));
		            System.out.println("key:totalPagesize  value:'"+String.valueOf(totalPagesize)+"'");
		            System.out.println("key:currentPageSize  value:'"+String.valueOf(totalPagesize)+"'");
		        }	
	    	}
    	} catch (Exception e) {
    		throw new RuntimeException(e.getMessage(), e);
		}
        return map;
    }
    
    /**
     * JSONArray를 파싱하여 각 JSONObject를 HashMap<String, String>으로 변환하는 함수
     * 
     * @param jsonArray JSON 데이터 배열 1차배열
     * @return HashMap<String, String> 리스트
     */
    public static HashMap<String, String> parseJSONObject(HttpServletRequest req) {
    	 Map<String, String[]> reqParameter = req.getParameterMap();
    	 StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    	 System.out.println("============="+stackTrace[2].getMethodName()+"   Parameters===================");
    	 HashMap<String, String> map = new HashMap<>();
	      
	     for (Entry<String, String[]> entry : reqParameter.entrySet()) {
	    	 String objKey = entry.getKey();
	    	 String[] values = entry.getValue();
	         JSONObject jsonObject = new JSONObject(values[0]);
	         
            if (values.length > 0) {
            	if(!objKey.contains("columns")) {
            		System.out.println("key:'"+objKey+"'  value:'"+values[0]+"'");
            	}
            	//Null Check
            	String value = (values[0].equals("null"))? "" : values[0];
                map.put(objKey, value); // 첫 번째 값을 저장
            }
            
             for (String key : jsonObject.keySet()) {
            	 System.out.println("key:'"+key+"'  value:'"+String.valueOf(jsonObject.opt(key))+"'");
                 map.put(key, String.valueOf(jsonObject.opt(key))); // null 값도 안전하게 처리
             }
	     }
             System.out.println("============="+stackTrace[2].getMethodName()+"   Parameters===================");
        return map;
    }
    
    /**
     * JSONArray를 파싱하여 각 JSONObject를 HashMap<String, String>으로 변환하는 함수
     * 
     * @param jsonArray JSON 데이터 배열 2차배열
     * @return HashMap<String, String> 리스트
     */
    public static List<HashMap<String, String>> parseJSONArray(JSONArray jsonArray) {
    	
    	List<HashMap<String, String>> list = new ArrayList<>();
    	System.out.println("=================parseJSONArray Parameters===================");
    	for (int i = 0; i < jsonArray.length(); i++) {
    		JSONObject jsonObj = jsonArray.getJSONObject(i);
    		HashMap<String, String> map = new HashMap<>();
    		for (String key : jsonObj.keySet()) {
    			map.put(key, String.valueOf(jsonObj.opt(key))); // null 값도 안전하게 처리
    			System.out.println("key:'"+key+"'  value:'"+String.valueOf(jsonObj.opt(key))+"'");
    		}
    		list.add(map);
    	}
    	System.out.println("=================parseJSONArray Parameters===================");
    	return list;
    }
}
