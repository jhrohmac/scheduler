package com.scheduler.kis_client.middleware;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.finance.kis.config.KisConfigLoader;
import com.scheduler.finance.kis.config.KisProperties;
import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.api.annotation.auth.AccountRequired;
import com.scheduler.kis_client.api.annotation.auth.AppKeyRequired;
import com.scheduler.kis_client.api.annotation.auth.AppSecretRequired;
import com.scheduler.kis_client.api.annotation.auth.AppTokenRequired;
import com.scheduler.kis_client.api.annotation.auth.ApprovalKeyRequired;
import com.scheduler.kis_client.api.rest.auth.GetSocketApprovalKeyApi;
import com.scheduler.kis_client.api.rest.auth.GetSocketApprovalKeyResult;
import com.scheduler.kis_client.api.rest.auth.GetTokenApi;
import com.scheduler.kis_client.api.rest.auth.GetTokenResult;
import com.scheduler.kis_client.api.special.PeekIssuedAppTokensResult;
import com.scheduler.kis_client.api.special.PeekIssuedAppTokensResult.AppToken;
import com.scheduler.kis_client.client.NetworkClient;
import com.scheduler.kis_client.client.NetworkRequest;
import com.scheduler.kis_client.config.Credentials;
import com.scheduler.kis_client.context.ApiContext;
import com.scheduler.kis_client.context.ApiData;
import com.scheduler.kis_client.exception.InvalidApiRequestException;
import com.scheduler.kis_client.util.AnnotationUtil;
import com.scheduler.kis_client.util.CredentialsUtil;
import com.scheduler.kis_client.util.DateUtil;
import com.scheduler.kis_client.util.Pair;

public class AuthMiddleware implements Middleware {

    private static final ConcurrentMap<String, Pair<String, LocalDateTime>> appTokens = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Pair<NetworkClient, String>> approvalKeys = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);

    @Override
    public void afterInit(KisClient client, ApiContext context) {
        if (context.getApiData().getResponseClass() == PeekIssuedAppTokensResult.class) {
            Map<String, AppToken> data = new HashMap<>();

            PeekIssuedAppTokensResult result = new PeekIssuedAppTokensResult();
            result.setAppTokens(data);

            for (Entry<String, Pair<String, LocalDateTime>> entry : appTokens.entrySet()) {
                data.put(
                    entry.getKey(),
                    new PeekIssuedAppTokensResult.AppToken(
                        entry.getValue().getA(),
                        entry.getValue().getB()
                    )
                );
            }

            context.setApiResult(result);

            return;
        }

        injectAccountInfo(context);
    }

    @Override
    public void before(KisClient client, ApiContext context) {
        ApiData apiData = context.getApiData();
        NetworkRequest request = context.getRequest();

        if (request == null) {
            return;
        }

        Credentials credentials = context.getCredentials();
        boolean maskCredentials = client.getConfig().isMaskCredentials();

        Map<String, Object> body = request.getBody();
        Map<String, Object> headers = request.getHeaders();

        Set<String> bodyKeys = new HashSet<>();
        Set<String> headerKeys = new HashSet<>();

        BiConsumer<Boolean, Pair<String, Object>> assign = (isBody, pair) -> {
            if (isBody) {
                bodyKeys.add(pair.getFirst());
                body.put(pair.getFirst(), pair.getSecond());
            } else {
                headerKeys.add(pair.getFirst());
                headers.put(pair.getFirst(), pair.getSecond());
            }
        };

        // AppKeyRequired
        for (AppKeyRequired anno : AnnotationUtil.getAnnotations(apiData.getAnnotations(), AppKeyRequired.class)) {
            boolean isBody = (anno.location() == AppKeyRequired.Location.BODY);
            assign.accept(isBody, new Pair<>(anno.key(), credentials.getAppKey()));
        }

        // AppSecretRequired
        for (AppSecretRequired anno : AnnotationUtil.getAnnotations(apiData.getAnnotations(), AppSecretRequired.class)) {
            boolean isBody = (anno.location() == AppSecretRequired.Location.BODY);
            assign.accept(isBody, new Pair<>(anno.key(), credentials.getAppSecret()));
        }

        // AppTokenRequired
        for (AppTokenRequired anno : AnnotationUtil.getAnnotations(apiData.getAnnotations(), AppTokenRequired.class)) {
            boolean isBody = (anno.location() == AppTokenRequired.Location.BODY);
            String token = anno.prefix() + getAppToken(client, context);
            assign.accept(isBody, new Pair<>(anno.key(), token));
        }

        // ApprovalKeyRequired
        for (ApprovalKeyRequired anno : AnnotationUtil.getAnnotations(apiData.getAnnotations(), ApprovalKeyRequired.class)) {
            boolean isBody = (anno.location() == ApprovalKeyRequired.Location.BODY);
            String approvalKey = getApprovalKey(client, context);
            assign.accept(isBody, new Pair<>(anno.key(), approvalKey));
        }

        if (maskCredentials) {
            request.setBody(CredentialsUtil.maskMap(body, bodyKeys.toArray(new String[0])));
            request.setHeaders(CredentialsUtil.maskMap(headers, headerKeys.toArray(new String[0])));
        }
    }

    @Override
    public void after(KisClient client, ApiContext context) {

    }

    private void injectAccountInfo(ApiContext context) {
        ApiData apiData = context.getApiData();
        Annotation[] annotations = apiData.getAnnotations();
        Credentials credentials = context.getCredentials();

        for (AccountRequired anno : AnnotationUtil.getAnnotations(annotations, AccountRequired.class)) {
            Map<String, Object> data = null;
            switch (anno.location()) {
                case HEADER:
                    data = apiData.getHeaders();
                    break;
                case BODY:
                    data = apiData.getBody();
                    break;
                case PARAMETER:
                    data = apiData.getParameters();
                    break;
            }

            if (data != null) {
                if (credentials.getAccountNo() == null || credentials.getAccountProductCode() == null) {
                    throw new InvalidApiRequestException("Account information is required");
                }

                data.put(anno.key1(), credentials.getAccountNo());
                data.put(anno.key2(), credentials.getAccountProductCode());
            }
        }
    }

    private String getApprovalKey(KisClient client, ApiContext context) {
        Credentials credentials = context.getCredentials();

        if (credentials.getApprovalKey() != null) {
            return credentials.getApprovalKey();
        }

        NetworkClient currentClient = context.getNetworkClient();

        Pair<NetworkClient, String> pair = approvalKeys.compute(credentials.getAppKey(), (creds, existingPair) -> {
            if (existingPair != null && existingPair.getFirst().equals(currentClient)) {
                return existingPair;
            }

            GetSocketApprovalKeyApi req = new GetSocketApprovalKeyApi();
            GetSocketApprovalKeyResult resp = client.execute(req, credentials);

            return Pair.of(currentClient, resp.getApprovalKey());
        });

        return pair.getSecond();
    }

    private boolean isExpired(LocalDateTime expiredAt) {
        return expiredAt.isBefore(LocalDateTime.now().plusMinutes(10));
    }

    // -----------------------------
    // AppToken 메인 로직
    // -----------------------------
    private String getAppToken(KisClient client, ApiContext context) {
        Credentials credentials = context.getCredentials();

        // 1) 캐시에서 먼저 조회
        String token = getAppTokenFromCache(credentials);
        if (token != null) {
            return token;
        }

        // 2) Credentials 에 미리 세팅되어 있으면 그 값 사용
        token = getAppTokenFromCredentials(credentials);
        if (token != null) {
            return token;
        }

        // 3) kis.properties 에 저장된 토큰 재사용
        try {
            KisProperties props = KisConfigLoader.getKisProperties();
            if (props != null) {
                String tokenFromProps = props.getAppTokenKey();
                String expireFromProps = props.getAppTokenExpiredAt();

                if (tokenFromProps != null && expireFromProps != null) {
                    try {
                        LocalDateTime expiredAt = DateUtil.toLocalDateTime(expireFromProps);
                        if (!isExpired(expiredAt)) {
                            Pair<String, LocalDateTime> tokenInfo = new Pair<>(tokenFromProps, expiredAt);
                            appTokens.put(credentials.getAppKey(), tokenInfo);

                            credentials.setAppToken(tokenFromProps);
                            credentials.setAppTokenExpiredAt(expireFromProps);

                            return tokenFromProps;
                        } else {
                            logger.info("kis.properties 의 AppToken 이 만료되어 신규 발급이 필요합니다.");
                        }
                    } catch (Exception e) {
                        logger.warn("kis.properties 의 kis.app.tokenExpiredAt 값을 LocalDateTime 으로 파싱하지 못했습니다.", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("KisConfigLoader.getKisProperties() 호출 중 예외가 발생했습니다. 파일 기반 토큰 재사용은 건너뜁니다.", e);
        }

        // 4) 둘 다 없으면 서버에서 신규 발급
        return fetchAppTokenFromServer(client, credentials);
    }

    private String getAppTokenFromCache(Credentials credentials) {
        Pair<String, LocalDateTime> tokenInfo = appTokens.get(credentials.getAppKey());

        if (tokenInfo == null) {
            return null;
        }

        LocalDateTime accessTokenExpired = tokenInfo.getB();

        if (!isExpired(accessTokenExpired)) {
            return tokenInfo.getA();
        }

        return null;
    }

    private String getAppTokenFromCredentials(Credentials credentials) {
        if (credentials.getAppToken() != null) {
            if (credentials.getAppTokenExpiredAt() == null) {
                logger.warn("Credentials의 appTokenExpiredAt이 null입니다. appToken 자동 재발급을 하지 않습니다.");

                return credentials.getAppToken();
            }

            LocalDateTime expiredAt = DateUtil.toLocalDateTime(credentials.getAppTokenExpiredAt());

            if (!isExpired(expiredAt)) {
                Pair<String, LocalDateTime> tokenInfo = new Pair<>(credentials.getAppToken(), expiredAt);
                appTokens.put(credentials.getAppKey(), tokenInfo);

                return credentials.getAppToken();
            } else {
                logger.warn("Credentials의 appToken이 만료되어(appTokenExpiredAt이 지났습니다.), 재발급을 시도합니다.");
            }
        }

        return null;
    }

    private String fetchAppTokenFromServer(KisClient client, Credentials credentials) {
        ReentrantLock lock = locks.computeIfAbsent(credentials.getAppKey(), k -> new ReentrantLock());
        lock.lock();
        try {
            while (true) {
                String cachedToken = getAppTokenFromCache(credentials);

                if (cachedToken != null) {
                    return cachedToken;
                }

                try {
                    GetTokenApi req = new GetTokenApi();
                    GetTokenResult resp = client.execute(req, credentials);

                    LocalDateTime expiredAt = DateUtil.toLocalDateTime(resp.getAccessTokenTokenExpired());
                    Pair<String, LocalDateTime> tokenInfo = new Pair<>(resp.getAccessToken(), expiredAt);
                    appTokens.put(credentials.getAppKey(), tokenInfo);

                    System.out.println("======================= GET AccessToken =====================");
                    System.out.println(resp.getAccessToken());
                    System.out.println("======================= GET AccessToken =====================");

                    // kis.properties 에도 저장
                    KisConfigLoader.updateAppToken(
                            resp.getAccessToken(),
                            resp.getAccessTokenTokenExpired()
                    );

                    // Credentials 에도 세팅
                    credentials.setAppToken(resp.getAccessToken());
                    credentials.setAppTokenExpiredAt(resp.getAccessTokenTokenExpired());

                    return resp.getAccessToken();
                } catch (InvalidApiRequestException e) {
                    if (e.getMessage() != null && e.getMessage().contains("\"error_code\":\"EGW00133\"")) {
                        logger.info("AppToken 발급 주기가 짧아 발급이 거절되었습니다. 10초 후 재시도합니다.");
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10000));
                        continue;
                    }
                    throw e;
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
