package com.scheduler.kis_client.client.nothing;

import java.io.IOException;

import com.scheduler.kis_client.api.annotation.VirtualApi;
import com.scheduler.kis_client.client.NetworkClient;
import com.scheduler.kis_client.client.NetworkRequest;
import com.scheduler.kis_client.context.ApiContext;
import com.scheduler.kis_client.context.ApiData;

public class NothingClient implements NetworkClient {

    @Override
    public boolean isSupport(ApiData apiData) {
        return apiData.hasAnnotation(VirtualApi.class);
    }

    @Override
    public NetworkRequest makeRequest(ApiData apiData) {
        return null;
    }

    @Override
    public void execute(ApiContext context) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

}
