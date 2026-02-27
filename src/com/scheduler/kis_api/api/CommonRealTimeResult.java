package com.scheduler.kis_api.api;

import com.scheduler.kis_client.api.RealTimeApiData;
import com.scheduler.kis_client.client.socket.SubscribableApiResult;

public abstract class CommonRealTimeResult<T extends RealTimeApiData> extends SubscribableApiResult<T> {

}
