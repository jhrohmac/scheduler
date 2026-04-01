package com.scheduler.kis_client.test.sample;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.scheduler.kis_api.api.rest.quotations.InquirePriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.config.Configuration;
import com.scheduler.kis_client.config.Credentials;

public class KisInquirePriceTest {

    /**
     * kis/kis.properties 를 읽어서 Properties 로 반환
     * (이미 KisConfigLoader 가 있으면 이 메서드 대신 그걸 사용해도 됨)
     */
    private static Properties loadKisProperties() {
        Properties props = new Properties();

        try (InputStream is = KisInquirePriceTest.class
                .getClassLoader()
                .getResourceAsStream("kis/kis.properties")) {

            if (is == null) {
                throw new IllegalStateException("클래스패스에서 kis/kis.properties 를 찾을 수 없습니다.");
            }

            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("kis.properties 로딩 중 에러", e);
        }

        return props;
    }

    /**
     * kis.properties 기반으로 Configuration 생성
     */
    private static Configuration buildConfiguration(Properties p) {
        Configuration config = new Configuration();

        // 실전/모의 환경에 따라 host 선택 (원하는 값으로 조정)
        // - 실전: https://openapi.koreainvestment.com:9443
        // - 모의: https://openapivts.koreainvestment.com:29443
        String httpHost = p.getProperty(
                "kis.http.host",
                "https://openapi.koreainvestment.com:9443"
        );

        config.setHttpHost(httpHost);

        // 필요하면 socketHost 도 설정
        // config.setSocketHost("ws://ops.koreainvestment.com:21000");

        // Rate Limit (properties 에 있으면 가져오고, 없으면 기본값 사용)
        String restLimitStr = p.getProperty("kis.rest.limit.per.second");
        if (restLimitStr != null && !restLimitStr.isEmpty()) {
            try {
                int restLimit = Integer.parseInt(restLimitStr.trim());
                config.setRestLimitPerSecond(restLimit);
            } catch (NumberFormatException ignore) {
                // 잘못된 값이면 기본값 유지
            }
        }

        return config;
    }

    /**
     * kis.properties 기반 Credentials 생성
     */
    private static Credentials buildCredentials(Properties p) {
        String appKey = p.getProperty("kis.app.key");
        String appSecret = p.getProperty("kis.app.secret");
        String accountNo = p.getProperty("kis.account.no");
        String accountProductCode = p.getProperty("kis.account.product.code");

        if (appKey == null || appSecret == null) {
            throw new IllegalStateException("kis.app.key / kis.app.secret 가 설정되어 있지 않습니다.");
        }

        // 계좌가 필요 없는 API(시세 조회 등)라도 account 정보는 있어도 무방
        return new Credentials(appKey, appSecret, accountNo, accountProductCode);
    }

    public static void main(String[] args) {
        // 1) kis.properties 로딩
        Properties kisProps = loadKisProperties();

        // 2) Configuration / Credentials 생성
        Configuration config = buildConfiguration(kisProps);
        Credentials credentials = buildCredentials(kisProps);
        config.addCredentials("default", credentials);

        // 3) KisClient 생성
        KisClient kisClient = new KisClient(config);

        // 4) 005930 현재가 조회용 API 객체 생성
        InquirePriceApi api = new InquirePriceApi();

        // 시장 분류 코드 (통합 = UN, KRX만 = J)
        api.setFidCondMrktDivCode("UN");  // 또는 "UN"

        // 005930 (삼성전자)
        api.setFidInputIscd("005930");

        // 5) API 호출
        try {
            InquirePriceResult result = kisClient.execute(api, credentials);

            // 6) 결과 출력
            //System.out.println("=== InquirePriceResult ===");
            //System.out.println("rt_cd  = " + result.getRtCd());
            //System.out.println("msg_cd = " + result.getMsgCd());
            //System.out.println("msg1   = " + result.getMsg1());

            if ("0".equals(result.getRtCd())) {
                InquirePriceResult.Output out = result.getOutput();
                if (out == null) {
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    System.out.println("======="+stackTrace[2].getMethodName()+"=======output 이 null 입니다.");
                    // System.out.println("종목코드    : " + out.getStckShrnIscd());
                    // System.out.println("종목명      : " + out.getHtSdtNm());
                    // System.out.println("현재가      : " + out.getStckPrpr());
                    // System.out.println("전일대비     : " + out.getPrdyVrss());
                    // System.out.println("전일대비율  : " + out.getPrdyCtrt());
                    // System.out.println("거래량      : " + out.getAcmlVol());
                }// else {
                   // System.out.println("output 이 null 입니다.");
               // }
            } else {
                System.out.println("요청 실패. 코드/메시지를 확인하세요.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("API 호출 중 예외 발생: " + e.getMessage());
        }
    }

}
