package com.scheduler.comm.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

public class Parameter extends HashMap<String, Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4374017991428761528L;

	public Parameter(){
		super();
	}
	public void setParam(String s, String v){
		super.put(s, v);
	}
	public void setParam(String s, int v){
		super.put(s, String.valueOf(v));
	}
	public void setObject(String s, Object obj){
		super.put(s, obj);
	}
	public String getString(String s){
		Object obj = get(s);
		return obj == null ? "" : (String)obj;
	}
	public int getInt(String s){
		Object obj = get(s);
		if(obj instanceof String){
			int i_retval = 0;
			try{
				i_retval = Integer.parseInt((String)obj);
			}catch(Exception e){
				i_retval = 0;
			}
			return i_retval;
		}else{
			return obj == null ? 0 : ((Integer)obj).intValue();
		}
	}
	
	public void print(){
		 Enumeration<String> en = Collections.enumeration(keySet());   
		 while(en.hasMoreElements()) {      
			 String key 	= en.nextElement();
			 Object	obj		= get(key);
			 if(obj instanceof String){
				 String value 	= getString(key);
				 System.out.println("param." + key + " = " + value);
			 }else{
				 System.out.println("param." + key + " = " + String.valueOf(obj));
			 }
		 } 		
	}
}    
