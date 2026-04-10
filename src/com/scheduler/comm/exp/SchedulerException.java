package com.scheduler.comm.exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.dao.impl.ItemCodeDaoImpl;

public class SchedulerException extends Exception {
	
	private static final long serialVersionUID = 5224374179334683663L;
	
	public static String XLS_UPLOAD_UNMATCH_ID 	 			= "인터페이스 ID가 맞지 않습니다.";
	public static String XLS_UPLOAD_NOT_APPROVED 			= "아직 등록완료 되지 않은 인터페이스 입니다. 삭제 후 다시 등록신청하세요 .";
	public static String XLS_UPLOAD_WRONG_SHEET  			= "인터페이스 정의서에 Mapping Sheet를 찾을 수 없습니다.";
	public static String XLS_UPLOAD_WRONG_FORMAT 			= "인터페이스정의서의 양식이 표준양식과 다릅니다.";
	public static String XLS_UPLOAD_IFID_NOT_FOUND 			= "인터페이스 ID가 없습니다.";
	public static String XLS_UPLOAD_IFINFO_NOT_FOUND 		= "인터페이스 기준정보가 없습니다.";
	public static String XLS_UPLOAD_SRCSYS_CD_NOT_FOUND 	= "소스시스템 코드가 없습니다.";
	public static String XLS_UPLOAD_TARSYS_CD_NOT_FOUND 	= "타켓시스템 코드가 없습니다.";
	public static String XLS_UPLOAD_SRCTAR_CD_SAME	 		= "소스시스템 코드와 타켓시스템 코드가 같습니다.";
	
	public static String XLS_UPLOAD_SRCSYS_NOT_EMPTY 		= "요청부 시스템명은 필수항목입니다.";
	public static String XLS_UPLOAD_TARSYS_NOT_EMPTY 		= "제공부 시스템명은 필수항목입니다.";
	public static String XLS_UPLOAD_PROCTYPE_NOT_EMPTY 		= "처리유형은 필수항목입니다.";
	public static String XLS_UPLOAD_COMTYPE_NOT_EMPTY 		= "통신유형은 필수항목입니다.";
	public static String XLS_UPLOAD_PROCTYPE_CD_NOT_FOUND 	= "처리유형 코드가 없습니다.";
	public static String XLS_UPLOAD_COMTYPE_CD_NOT_FOUND 	= "처리유형 코드가 없습니다.";
	public static String XLS_UPLOAD_REQ_TYPE_NOT_EMPTY 		= "요청부 Type 은 필수항목입니다.";
	public static String XLS_UPLOAD_RES_TYPE_NOT_EMPTY 		= "제공부 Type 은 필수항목입니다.";
	public static String XLS_UPLOAD_REQ_TYPE_WRONG	 		= "요청부 Type이 잘못되었습니다.";
	public static String XLS_UPLOAD_RES_TYPE_WRONG	 		= "제공부 Type이 잘못되었습니다.";
	public static String XLS_UPLOAD_IFID_REQSYS_UNMATCH	    = "인터페이스ID의 생성규칙과 다른 제공부 시스템 코드입니다. 수정등록 할 수 없습니다.";
	
	public static String APPV_HAS_SUPERIOR_DECISION         = "상위 결재자의 결재내용이 존재합니다. 처리할 수 없습니다.";
	public static String APPV_INFO_NOT_FOUND       			= "결재현황을 찾을 수 없습니다.";
	
	private static final Logger logger = LoggerFactory.getLogger(ItemCodeDaoImpl.class);
	
	public String code;
	public String message;
	public SchedulerException(Exception e){
		super(e.getLocalizedMessage());
	}
	public SchedulerException(String message){
		super(message);
		this.message = message;
		logger.error(message);
	}
	public SchedulerException(String code, String message){
		this(message);
		this.code = code;
		
	}
}
