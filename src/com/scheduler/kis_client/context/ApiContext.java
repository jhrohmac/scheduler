package com.scheduler.kis_client.context;

import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.client.NetworkClient;
import com.scheduler.kis_client.client.NetworkRequest;
import com.scheduler.kis_client.client.NetworkResponse;
import com.scheduler.kis_client.config.Credentials;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * API 요청에 대한 정보를 담은 객체
 */
@Data
@NoArgsConstructor
public class ApiContext {

    public ApiContext(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * 인증 정보
     */
    private @NonNull Credentials credentials;

    /**
     * Api Class에서 추출한 데이터를 담은 객체
     */
    private @NonNull ApiData apiData;

    private @NonNull NetworkClient networkClient;

    /**
     * 서버에 요청 보낼 데이터를 담은 객체
     */
    private NetworkRequest request;

    /**
     * 서버로부터 응답 받은 데이터를 담은 객체
     */
    private NetworkResponse response;

    /**
     * 최종적으로 반환될 데이터를 담은 객체
     */
    private ApiResult apiResult;

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public ApiData getApiData() {
		return apiData;
	}

	public void setApiData(ApiData apiData) {
		this.apiData = apiData;
	}

	public NetworkClient getNetworkClient() {
		return networkClient;
	}

	public void setNetworkClient(NetworkClient networkClient) {
		this.networkClient = networkClient;
	}

	public NetworkRequest getRequest() {
		return request;
	}

	public void setRequest(NetworkRequest request) {
		this.request = request;
	}

	public NetworkResponse getResponse() {
		return response;
	}

	public void setResponse(NetworkResponse response) {
		this.response = response;
	}

	public ApiResult getApiResult() {
		return apiResult;
	}

	public void setApiResult(ApiResult apiResult) {
		this.apiResult = apiResult;
	}

}
