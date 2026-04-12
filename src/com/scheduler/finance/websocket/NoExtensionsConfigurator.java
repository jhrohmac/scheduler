package com.scheduler.finance.websocket;

import java.util.Collections;
import java.util.List;

import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Native browser WebSocket clients advertise permessage-deflate by default.
 * In the current Tomcat + app classpath combination, extension negotiation can
 * route through Tyrus and fail the handshake with HTTP 500. Returning no
 * negotiated extensions keeps the handshake stable without affecting payloads.
 */
public class NoExtensionsConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
        return Collections.emptyList();
    }
}
