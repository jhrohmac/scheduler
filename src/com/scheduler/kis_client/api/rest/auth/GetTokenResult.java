package com.scheduler.kis_client.api.rest.auth;


import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.api.annotation.Header;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class GetTokenResult implements ApiResult {

    @Header("content-type")
    private String contentType;

    @Header
    private String trId;

    @Header
    private String trCont;

    @Header
    private String gtUid;

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String accessTokenTokenExpired;
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
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public long getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getAccessTokenTokenExpired() {
		return accessTokenTokenExpired;
	}
	public void setAccessTokenTokenExpired(String accessTokenTokenExpired) {
		this.accessTokenTokenExpired = accessTokenTokenExpired;
	}

    @Override
    public String toString() {
        return "GetTokenResult{" +
            "contentType='" + contentType + '\'' +
            ", trId='" + trId + '\'' +
            ", trCont='" + trCont + '\'' +
            ", gtUid='" + gtUid + '\'' +
            ", accessToken='" + accessToken + '\'' +
            ", tokenType='" + tokenType + '\'' +
            ", expiresIn=" + expiresIn +
            ", accessTokenTokenExpired='" + accessTokenTokenExpired + '\'' +
            '}';
    }
}