package com.scheduler.kis_client.client.http;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.config.Configuration;
import com.scheduler.kis_client.config.Credentials;
import com.scheduler.kis_client.context.ApiContext;
import com.scheduler.kis_client.context.ApiData;
import com.scheduler.kis_client.exception.InvalidApiRequestException;
import com.scheduler.kis_client.util.CredentialsUtil;
import com.scheduler.kis_client.util.JsonUtil;
import com.scheduler.kis_client.util.ReflectionUtil;
import com.scheduler.kis_client.util.StringUtil;
import com.scheduler.kis_client.client.http.KisRateLimiter;

public class JavaHttpClient extends com.scheduler.kis_client.client.http.HttpClient {

    private static final AtomicInteger EXEC_ID = new AtomicInteger(0);
    private static final ClassLoader SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();

    HttpClient client;
    Configuration config;
    private final ExecutorService executor;

    private static final Logger logger = LoggerFactory.getLogger(JavaHttpClient.class);

    public JavaHttpClient(Configuration config) {
        Duration timeout = config.getHttpTimeout();

        this.config = config;
        this.executor = Executors.newCachedThreadPool(new HttpClientThreadFactory());

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            if (SYSTEM_CLASS_LOADER != null) {
                Thread.currentThread().setContextClassLoader(SYSTEM_CLASS_LOADER);
            }
            this.client = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(timeout)
                .executor(executor)
                .build();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public HttpClientRequest makeRequest(ApiData apiData) {
        String apiHost = config.getHttpHost();
        String urlPath = apiData.getUrlPath();
        String url = StringUtil.concatUrl(apiHost, urlPath);

        return HttpClientRequest.builder()
            .method(apiData.getMethod())
            .url(url)
            .parameters(apiData.getParameters())
            .headers(apiData.getHeaders())
            .body(apiData.getBody())
            .build();
    }

    public HttpResponse<String> requestWithRetry(HttpClientRequest request) throws IOException, InterruptedException {
        int attempt = 0;
        int maxAttempt = config.getHttpTimeoutMaxRetries();

//        System.out.println("API Request data:=========================");
//        System.out.println("getMethod >"+request.getMethod());
//        System.out.println("getUrl >"+request.getUrl());
//        System.out.println("getBody >"+request.getBody());
//        System.out.println("getHeaders >"+request.getHeaders());
//        System.out.println("getParameters >"+request.getParameters());
//        System.out.println("API Request data:=========================");

        while (true) {
            try {
                if (request.getMethod().equals("POST")) {
                    return POST(request);
                } else {
                    return GET(request);
                }
            } catch (HttpTimeoutException e) {
                if (++attempt >= maxAttempt) {
                    throw e;
                }

                System.out.println("Request timeout. Retry. [{} / {}]" + e.getMessage());
                LockSupport.parkNanos(1_000_000);
            } catch (IOException e) {
                if (++attempt >= maxAttempt) {
                    throw e;
                }

                if (e.getMessage() != null && e.getMessage().contains("HTTP/1.1 header parser received no bytes")) {
                    logger.debug("Server-Side Network Error. Retry. [{} / {}]", attempt, maxAttempt, e.getMessage());
                    System.out.println("HTTP/1.1 header parser received no bytes===============");
                    System.out.println(e.getMessage());
                    LockSupport.parkNanos(1_000_000);
                    continue;
                }

                logger.debug("Unknown network error. Retry. [{} / {}]", attempt, maxAttempt, e);
                LockSupport.parkNanos(1_000_000);
                System.out.println("Unknown network error. Retry. ==============");
            }
        }
    }

    private HttpResponse<String> requestWithRateLimitRetry(HttpClientRequest request, ApiData api) throws IOException, InterruptedException {
        int max = KisRateLimiter.maxRetries();
        HttpResponse<String> last = null;
        for (int attempt = 0; attempt <= max; attempt++) {
            KisRateLimiter.throttle();
            last = requestWithRetry(request);
            if (last == null) {
                return null;
            }
            int sc = last.statusCode();
            if (sc == 200) {
                return last;
            }
            String body = last.body();
            if (KisRateLimiter.isRateLimitResponse(sc, body) && attempt < max) {
                logger.warn("KIS rate limit detected. Backoff and retry. [{} / {}] {} -> {}", (attempt + 1), max, request, body);
                KisRateLimiter.backoffSleep(attempt);
                continue;
            }
            return last;
        }
        return last;
    }


    @Override
    public void execute(ApiContext context) throws IOException {
        HttpClientRequest request = (HttpClientRequest)context.getRequest();
        ApiData api = context.getApiData();

        HttpResponse<String> response = null;

        try {
            response = requestWithRateLimitRetry(request, api);
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            throw new IOException("Failed to get response from server. (InterruptedException)");
        }

        if (response == null) {
            throw new IOException("Failed to get response from server.");
        }

        String responseString = response.body();
//        System.out.println("================= responseString ==================");
//        System.out.println(responseString);
//        System.out.println("================= responseString ==================");
        int statusCode = response.statusCode();
        Map<String, List<String>> headers = response.headers().map();

        if (logger.isTraceEnabled()) {
            String maskedResponseString = config.isMaskCredentials() ? CredentialsUtil.maskAccessToken(responseString) : responseString;
            logger.trace("API Request ends. [{}] {} -> ({}) {}", api.getClass().getSimpleName(), request, statusCode, maskedResponseString);
        }

        if (responseString == null) {
            throw new InvalidApiRequestException("Failed to get response from server. (no response)", statusCode);
        }

        if (statusCode != 200) {
            logger.error("API Response Error [{}] : Unexpected status code {}. {} -> {}", api.getClass().getSimpleName(), statusCode, request, responseString);
            throw new InvalidApiRequestException(responseString, statusCode);
        }

        HttpClientResponse clinetResponse = HttpClientResponse.builder()
            .statusCode(statusCode)
            .headers(headers)
            .body(responseString)
            .build();

        ApiResult apiResult = makeApiResult(context.getCredentials(), context.getApiData(), clinetResponse);
        context.setResponse(clinetResponse);
        context.setApiResult(apiResult);
    }

    private ApiResult makeApiResult(Credentials credentials, ApiData apiData, HttpClientResponse clinetResponse) {
        String responseString = clinetResponse.getBody();

        ApiResult result = JsonUtil.fromJson(responseString, apiData.getResponseClass());
        injectHeader(clinetResponse, result);
        return result;
    }

    private void injectHeader(HttpClientResponse clientResponse, ApiResult result) {
        Map<String, List<String>> allHeaders = clientResponse.getHeaders();
        Map<String, String> headers = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : allHeaders.entrySet()) {
            if (entry.getValue().size() == 1) {
                headers.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        Field[] fields = ReflectionUtil.getAllFields(result.getClass());

        for (Field field : fields) {
            Header annotation = field.getAnnotation(Header.class);

            if (annotation == null) {
                continue;
            }

            String key = annotation.value().isEmpty() ? StringUtil.toSnakeCase(field.getName()) : annotation.value();
            if (headers.containsKey(key)) {
                try {
                    field.setAccessible(true);
                    field.set(result, headers.get(key));
                } catch (IllegalAccessException e) {
                    continue;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        shutdownExecutor();
        // Java 8 + java.net.http.HttpClient 또는 기타 HttpClient 사용 시
        // 명시적으로 close 할 필요가 없거나 close() 메서드가 없음.
        // 따라서 여기서는 아무 작업도 하지 않는다.
        // 필요하면 향후 커넥션 풀 라이브러리를 쓰면서 정리 가능.
    }


    public void shutdownExecutor() {
        if (executor == null) {
            return;
        }
        try {
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class HttpClientThreadFactory implements ThreadFactory {
        private final int poolId = EXEC_ID.incrementAndGet();
        private final AtomicInteger threadId = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("kis-http-" + poolId + "-" + threadId.incrementAndGet());
            t.setDaemon(true);
            if (SYSTEM_CLASS_LOADER != null) {
                t.setContextClassLoader(SYSTEM_CLASS_LOADER);
            }
            return t;
        }
    }

    public HttpRequest.Builder getRequestBuilder(HttpClientRequest request) {
        String parameter = StringUtil.makeUrlParamString(request.getParameters());
        StringBuilder urlBuilder = new StringBuilder(request.getUrl());

        if (!parameter.isEmpty()) {
            urlBuilder.append('?').append(parameter);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(urlBuilder.toString()));

        for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            requestBuilder = requestBuilder.header(entry.getKey(), entry.getValue().toString());
        }

        return requestBuilder;
    }

    public HttpResponse<String> GET(HttpClientRequest request) throws IOException, InterruptedException {
        HttpRequest req = getRequestBuilder(request).GET().build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public HttpResponse<String> POST(HttpClientRequest request) throws IOException, InterruptedException {
        String jsonBody = JsonUtil.toJson(request.getBody());

        HttpRequest req = getRequestBuilder(request)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                            .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        return response;
    }
}
