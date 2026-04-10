package com.scheduler.comm.util;

import java.util.HashMap;

public class DataSet extends HashMap<String, Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2599098929417206096L;
	
	public static final String ERROR_CODE 	= 	"ERROR_CODE";
	public static final String ERROR_MSG  	= 	"ERROR_MSG";
	public static final String MSG  		= 	"MSG";
	public static final String NOTICE  		= 	"NOTICE";
	public static final String WINDOW_ALERT = 	"WINDOW_ALERT";
	public static final String EAI_RESDATA	=	"EAI_RESDATA";
	
	public DataSet(){
		super();
	}
	public void setParam(String s, Object v){
		super.put(s, v);
	}
	public String getString(String s){
		Object obj = get(s);
		return obj == null ? "" : (String)obj;
	}
	public int getInt(String s){
		Object obj = get(s);
		if(obj instanceof String){
			return obj == null ? 0 : Integer.parseInt((String)obj);
		}else{
			return obj == null ? 0 : ((Integer)obj).intValue();
		}
	}

	public String getErrCode(){
		return getString(ERROR_CODE);
	}
	public String getErrMsg(){
		return getString(ERROR_MSG);
	}
	public String getMsg(){
		return getString(MSG);
	}
	public String getNotice(){
		return getString(NOTICE);
	}
	public boolean hasError(){
		if(!"".equals(getErrCode())){
			return true;
		}
		return false;
	}
}    
