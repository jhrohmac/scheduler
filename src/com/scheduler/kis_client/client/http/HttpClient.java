package com.scheduler.kis_client.client.http;

import com.scheduler.kis_client.api.annotation.RestApi;
import com.scheduler.kis_client.client.NetworkClient;
import com.scheduler.kis_client.context.ApiData;

public abstract class HttpClient implements NetworkClient {

    public boolean isSupport(ApiData apiData) {
        return apiData.hasAnnotation(RestApi.class);
    }

}
