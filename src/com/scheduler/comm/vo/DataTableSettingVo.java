package com.scheduler.comm.vo;

import java.util.List;


public class DataTableSettingVo {
	public String draw;
	public int start_no;
	public int page_length;
	public int recordsFiltered;
	public int recordsTotal;
	public List<?> data;
	private Object singleData;
	
	public String result_code;
	public String result_msg;
	
	public String system_code;
	public String system_msg;
	
	public String getDraw() {
		return draw;
	}
	public void setDraw(String draw) {
		this.draw = draw;
	}
	public int getStart_no() {
		return start_no;
	}
	public void setStart_no(int start_no) {
		this.start_no = start_no;
	}
	public int getPage_length() {
		return page_length;
	}
	public void setPage_length(int page_length) {
		this.page_length = page_length;
	}
	public int getRecordsFiltered() {
		return recordsFiltered;
	}
	public void setRecordsFiltered(int recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}
	public int getRecordsTotal() {
		return recordsTotal;
	}
	public void setRecordsTotal(int recordsTotal) {
		this.recordsTotal = recordsTotal;
	}
	public List<?> getData() {
		return data;
	}
	public void setData(List<?> data) {
		this.data = data;
	}
	public Object getSingleData() {
		return singleData;
	}
	public void setSingleData(Object singleData) {
		this.singleData = singleData;
	}
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	public String getResult_msg() {
		return result_msg;
	}
	public void setResult_msg(String result_msg) {
		this.result_msg = result_msg;
	}
	public String getSystem_code() {
		return system_code;
	}
	public void setSystem_code(String system_code) {
		this.system_code = system_code;
	}
	public String getSystem_msg() {
		return system_msg;
	}
	public void setSystem_msg(String system_msg) {
		this.system_msg = system_msg;
	}
}