package com.scheduler.management.vo;

public class CalendarManageVo {
	
	public String rnum;				//ROWNUM
	public String host_nm;			//호스트 명
	public String calendar_div;		//캘린더 구분
	public int	  id;				//ID
	public String title;			//제목
	public String content;			//설명
	public String desk_div;			//부서
	public String start;			//시작날짜
	public String start_time;		//시작날짜
	public String end;				//종료날짜
	public String end_time;			//종료날짜
	public String allDay;			//종일 여부
	public String backgroundColor;	//백그라운드 컬러
	public String borderColor;		//보드 컬러
	public String status_cd;		//상태코드
	public String status_dtl;		//상태 디테일
	public String check_date;		//체크 시간
	public String check_yn;			//체크 유무
	public String event_user;		//작업자
	public String create_date;		//생성일자
	public int	  total_count;		//총건수
	
	public String getRnum() {
		return rnum;
	}
	public void setRnum(String rnum) {
		this.rnum = rnum;
	}
	public String getHost_nm() {
		return host_nm;
	}
	public void setHost_nm(String host_nm) {
		this.host_nm = host_nm;
	}
	public String getCalendar_div() {
		return calendar_div;
	}
	public void setCalendar_div(String calendar_div) {
		this.calendar_div = calendar_div;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDesk_div() {
		return desk_div;
	}
	public void setDesk_div(String desk_div) {
		this.desk_div = desk_div;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getStart_time() {
		return start_time;
	} 
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public String getAllDay() {
		return allDay;
	}
	public void setAllDay(String allDay) {
		this.allDay = allDay;
	}
	public String getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public String getBorderColor() {
		return borderColor;
	}
	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}
	
	public String getStatus_cd() {
		return status_cd;
	}
	public void setStatus_cd(String status_cd) {
		this.status_cd = status_cd;
	}
	public String getStatus_dtl() {
		return status_dtl;
	}
	public void setStatus_dtl(String status_dtl) {
		this.status_dtl = status_dtl;
	}
	public String getCheck_date() {
		return check_date;
	}
	public void setCheck_date(String check_date) {
		this.check_date = check_date;
	}
	public String getCheck_yn() {
		return check_yn;
	}
	public void setCheck_yn(String check_yn) {
		this.check_yn = check_yn;
	}
	public String getEvent_user() {
		return event_user;
	}
	public void setEvent_user(String event_user) {
		this.event_user = event_user;
	}
	
	public String getCreate_date() {
		return create_date;
	}
	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}
	public int getTotal_count() {
		return total_count;
	}
	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}
}