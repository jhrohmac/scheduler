package com.scheduler.kis_client.client.socket;


@FunctionalInterface
public interface UnsubscribeCallback {
    void onUnsubscribe(SubscribableApiResult<?> source);
}