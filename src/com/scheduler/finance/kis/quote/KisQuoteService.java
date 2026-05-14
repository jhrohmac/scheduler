package com.scheduler.finance.kis.quote;

import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.PriceApi;
import com.scheduler.kis_api.api.rest.quotations.PriceResult;
import com.scheduler.kis_client.KisClient;

/**
 * KIS 현재가 호출의 얇은 공통 진입점.
 *
 * 1차 리팩토링에서는 기존 RateLimitingMiddleware/JavaHttpClient 안정장치를 그대로 사용하고,
 * 호출 생성과 화면 DTO 변환만 한 곳으로 모은다.
 */
public class KisQuoteService {
    private final KisClient client;

    public KisQuoteService() {
        this(KisClientFactory.getClient());
    }

    public KisQuoteService(KisClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client is required");
        }
        this.client = client;
    }

    public KisQuoteDto getDomesticCurrentQuote(String token, String code) throws Exception {
        return getDomesticCurrentQuote(token, "UN", code);
    }

    public KisQuoteDto getDomesticCurrentQuote(String token, String market, String code) throws Exception {
        String effectiveCode = require(code, "code");
        String effectiveMarket = KisMarketCode.normalizeDomesticMarket(market);
        InquirePriceResult result = getDomesticCurrentRaw(effectiveMarket, effectiveCode);
        return KisQuoteMapper.fromDomesticRest(token, effectiveMarket, effectiveCode, result);
    }

    public InquirePriceResult getDomesticCurrentRaw(String code) throws Exception {
        return getDomesticCurrentRaw("UN", code);
    }

    public InquirePriceResult getDomesticCurrentRaw(String market, String code) throws Exception {
        String effectiveCode = require(code, "code");
        InquirePriceApi api = new InquirePriceApi();
        api.setFidInputIscd(effectiveCode);
        api.setFidCondMrktDivCode(KisMarketCode.toDomesticRestCode(market));

        InquirePriceResult result = client.execute(api);
        validateRestResult(result == null ? null : result.getRtCd(), result == null ? null : result.getMsgCd(),
                result == null ? null : result.getMsg1(), "KIS current price");
        return result;
    }

    public KisQuoteDto getOverseasCurrentQuote(String token, String country, String market, String code)
            throws Exception {
        String effectiveCode = require(code, "code");
        PriceApi api = new PriceApi();
        api.setExcd(require(market, "market"));
        api.setSymb(effectiveCode);

        PriceResult result = client.execute(api);
        validateRestResult(result == null ? null : result.getRtCd(), result == null ? null : result.getMsgCd(),
                result == null ? null : result.getMsg1(), "KIS overseas price");
        return KisQuoteMapper.fromOverseasRest(token, country, market, effectiveCode, result);
    }

    private void validateRestResult(String rtCd, String msgCd, String msg1, String label) {
        if (rtCd == null) {
            throw new IllegalStateException(label + " response is null");
        }
        // KIS 정상 응답은 항상 "0". 빈 문자열도 비정상으로 본다.
        if (!"0".equals(rtCd)) {
            throw new IllegalStateException(label + " error rtCd=" + rtCd + ", msgCd=" + safe(msgCd)
                    + ", msg1=" + safe(msg1));
        }
    }

    private String require(String value, String name) {
        String v = safe(value);
        if (v.isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return v;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
