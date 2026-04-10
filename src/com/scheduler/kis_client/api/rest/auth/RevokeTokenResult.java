package com.scheduler.kis_client.api.rest.auth;

import com.scheduler.kis_client.api.ApiResult;

import lombok.Getter;

@Getter
public class RevokeTokenResult implements ApiResult {

    private String code;
    private String message;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
