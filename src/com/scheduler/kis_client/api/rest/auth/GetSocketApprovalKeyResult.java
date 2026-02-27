package com.scheduler.kis_client.api.rest.auth;


import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.api.annotation.Header;

import lombok.Getter;

@Getter
public class GetSocketApprovalKeyResult implements ApiResult {

    @Header("content-type")
    private String contentType;

    @Header
    private String trId;

    @Header
    private String trCont;

    @Header
    private String gtUid;

    private String approvalKey;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getTrId() {
		return trId;
	}

	public void setTrId(String trId) {
		this.trId = trId;
	}

	public String getTrCont() {
		return trCont;
	}

	public void setTrCont(String trCont) {
		this.trCont = trCont;
	}

	public String getGtUid() {
		return gtUid;
	}

	public void setGtUid(String gtUid) {
		this.gtUid = gtUid;
	}

	public String getApprovalKey() {
		return approvalKey;
	}

	public void setApprovalKey(String approvalKey) {
		this.approvalKey = approvalKey;
	}

}