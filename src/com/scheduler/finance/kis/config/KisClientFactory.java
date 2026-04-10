package com.scheduler.finance.kis.config;

import java.time.Duration;

import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.config.Configuration;
import com.scheduler.kis_client.config.Credentials;

/**
 * KIS 공통 클라이언트 생성 팩토리.
 *
 * - KisConfigLoader → KisProperties 로 설정을 읽고
 * - Configuration, Credentials 에 매핑한 후 KisClient 를 생성한다.
 *
 * 토큰 발급/캐싱은 kis_client 내부 AuthMiddleware 가 처리하므로
 * 여기서는 설정과 클라이언트 생성만 담당한다.
 */
public class KisClientFactory {

    private static final KisClient KIS_CLIENT;

    static {
        // kis.properties → KisProperties 로딩
        KisProperties props = KisConfigLoader.getKisProperties();

        // 1) Configuration 설정
        Configuration config = new Configuration();
        config.setHttpHost(props.getHttpHost());
        config.setSocketHost(props.getWebsocketHost());

        // 타임아웃, 재시도 등 필요에 따라 조정
        config.setHttpTimeout(Duration.ofSeconds(10));
        config.setHttpTimeoutMaxRetries(3);

        // 2) Credentials 설정
        // Credentials 에는 기본 생성자가 없고, 아래 두 생성자만 정의되어 있다.
        //  - Credentials(String apiKey, String apiSecret)
        //  - Credentials(String apiKey, String apiSecret, String accountNo, String accountProductCode)
        // 계좌 정보를 같이 쓰는 쪽이므로 네 개짜리 생성자를 사용한다.
        Credentials credentials = new Credentials(
                props.getAppKey(),
                props.getAppSecret(),
                props.getAccountNo(),
                props.getAccountProductCode()
        );
        credentials.setRestLimitPerSecond(props.getRestLimitPerSecond());

        // 3) Configuration 에 Credentials 등록
        config.addCredentials(credentials);

        // 4) KisClient 생성
        KIS_CLIENT = new KisClient(config);
        System.out.println(KIS_CLIENT);
    }

    private KisClientFactory() {
    }

    public static KisClient getClient() {
        return KIS_CLIENT;
    }

    public static void shutdown() {
        try {
            if (KIS_CLIENT != null) {
                KIS_CLIENT.close();
            }
        } catch (Exception ignore) {
        }
    }
}
