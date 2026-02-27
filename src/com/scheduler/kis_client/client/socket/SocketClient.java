package com.scheduler.kis_client.client.socket;


import com.scheduler.kis_client.api.annotation.RealTimeApi;
import com.scheduler.kis_client.client.NetworkClient;
import com.scheduler.kis_client.context.ApiData;

public abstract class SocketClient implements NetworkClient {

    public boolean isSupport(ApiData data) {
        return data.hasAnnotation(RealTimeApi.class);
    }

}
