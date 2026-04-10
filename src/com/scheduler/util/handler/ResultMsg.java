package com.scheduler.util.handler;

public class ResultMsg {
	
	public static String NOTICE  			= 	"NOTICE";
	public static String WINDOW_ALERT 		= 	"WINDOW_ALERT";

	public static String RESULT_CODE  		= 	"RESULT_CODE";
	public static String RESULT_MSG  		= 	"RESULT_MSG";

	/**
	 * SUCCESS STATUS
	 */
	public static String SUCCESS_CODE		=	"0000";
	public static String SUCCESS_MSG		=	"";
	
	public static String SUCCESS_COMPLITE	=	"정상 반영되었습니다.";
	public static String SUCCESS_PSW_CHG	=	"패스워드가 변경되었습니다.";
	

	/**
	 * ERROR STATUS
	 */
	public static String ERROR_CODE 		= 	"9999";
	public static String ERROR_MSG  		= 	"";
	

	/**
	 * SESSION STATUS
	 */
	//521: A session conflict error. 
	//522: A session timeout error. 
	//523: A session closed error. 
	public static String SESSION_FAILE_CODE	=	"0523";
	public static String SESSION_FAILE_MSG	=	"세션이 종료되었습니다.";
	
	
	
 	
}
