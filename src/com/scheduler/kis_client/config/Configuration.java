package com.scheduler.kis_client.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scheduler.kis_client.middleware.Middleware;

import lombok.Getter;
import lombok.Setter;

/**
 * API 설정
 */
public class Configuration {

    /**
     * HTTP API 호스트 주소
     */
    @Getter
    @Setter
    private String httpHost = "https://openapi.koreainvestment.com:9443";

    /**
     * HTTP Timeout
     */
    @Getter
    @Setter
    private Duration httpTimeout = Duration.ofSeconds(10);

    /**
     * HTTP Timeout 재시도 횟수
     */
    @Getter
    @Setter
    private int httpTimeoutMaxRetries = 5;

    /**
     * Socket API 호스트 주소
     */
    @Getter
    @Setter
    private String socketHost = "ws://ops.koreainvestment.com:21000";

    /**
     * Socket Timeout
     */
    @Getter
    @Setter
    private Duration socketTimeout = Duration.ofSeconds(10);
    
    /**
     * 로깅 시 credential 정보를 마스킹할지 여부
     */
    @Getter
    @Setter
    private boolean maskCredentials = true;

    private Map<String, Credentials> credentials = new HashMap<>();
    private CredentialsSelectionStrategy credentialsSelector = new WeightedRoundRobinCredentialsSelector();

    private List<Middleware> middlewares = new ArrayList<>();
    
    private int restLimitPerSecond = 0;


    public void addCredentials(Credentials credentials) {
        String uuid = java.util.UUID.randomUUID().toString();

        this.addCredentials(uuid, credentials);
    }

    public void addCredentials(String name, Credentials credentials) {
        this.credentials.put(name, credentials);
        this.credentialsSelector.setCredentials(this.credentials);
    }

    public int removeCredentials(String name) {
        int result = this.credentials.remove(name) != null ? 1 : 0;

        if (result == 1) {
            this.credentialsSelector.setCredentials(this.credentials);
        }

        return result;
    }

    public int removeCredentials(Credentials credentials) {
        int before = this.credentials.size();

        this.credentials.values().removeIf(c -> c.equals(credentials));

        int removed = before - this.credentials.size();

        if (removed > 0) {
            this.credentialsSelector.setCredentials(this.credentials);
        }

        return removed;
    }

    public void setCredentialsSelector(CredentialsSelectionStrategy credentialsSelector) {
        this.credentialsSelector = credentialsSelector;
    }

    public Map<String, Credentials> getAllCredentials() {
        return credentials;
    }

    public Credentials getCredentials() {
        return credentialsSelector.getCredentials();
    }

    public Credentials getCredentials(String name) {
        return credentialsSelector.getCredentials(name);
    }

    public void addMiddleWare(Middleware middleware) {
        middlewares.add(middleware);
    }

    public List<Middleware> getAllMiddlewares() {
        return middlewares;
    }

	public String getHttpHost() {
		return httpHost;
	}

	public void setHttpHost(String httpHost) {
		this.httpHost = httpHost;
	}

	public Duration getHttpTimeout() {
		return httpTimeout;
	}

	public void setHttpTimeout(Duration httpTimeout) {
		this.httpTimeout = httpTimeout;
	}

	public int getHttpTimeoutMaxRetries() {
		return httpTimeoutMaxRetries;
	}

	public void setHttpTimeoutMaxRetries(int httpTimeoutMaxRetries) {
		this.httpTimeoutMaxRetries = httpTimeoutMaxRetries;
	}

	public String getSocketHost() {
		return socketHost;
	}

	public void setSocketHost(String socketHost) {
		this.socketHost = socketHost;
	}

	public Duration getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Duration socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public boolean isMaskCredentials() {
		return maskCredentials;
	}

	public void setMaskCredentials(boolean maskCredentials) {
		this.maskCredentials = maskCredentials;
	}

	public List<Middleware> getMiddlewares() {
		return middlewares;
	}

	public void setMiddlewares(List<Middleware> middlewares) {
		this.middlewares = middlewares;
	}

	public CredentialsSelectionStrategy getCredentialsSelector() {
		return credentialsSelector;
	}

	public void setCredentials(Map<String, Credentials> credentials) {
		this.credentials = credentials;
	}

	public int getRestLimitPerSecond() {
		return restLimitPerSecond;
	}

	public void setRestLimitPerSecond(int restLimitPerSecond) {
		this.restLimitPerSecond = restLimitPerSecond;
	}

}
