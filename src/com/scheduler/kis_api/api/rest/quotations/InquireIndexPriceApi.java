package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

/**
 * 국내 업종/지수 현재가
 * path: /uapi/domestic-stock/v1/quotations/inquire-index-price
 */
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/quotations/inquire-index-price")
public class InquireIndexPriceApi extends CommonRestApi<InquireIndexPriceResult> {

    @Header
    private String trId = "FHPUP02100000";

    // KIS OpenAPI 파라미터명은 대문자 스네이크 케이스를 그대로 사용한다.
    // (kis_client에서 자동 매핑이 보장되지 않으므로 명시)
    @Parameter("FID_INPUT_ISCD")
    private String fidInputIscd; // 코스피(0001), 코스닥(1001), 코스피200(2001)

    @Parameter("FID_COND_MRKT_DIV_CODE")
    private String fidCondMrktDivCode = "U";

    public InquireIndexPriceApi() {
    }

    public String getTrId() {
        return trId;
    }

    public void setTrId(String trId) {
        this.trId = trId;
    }

    public String getFidInputIscd() {
        return fidInputIscd;
    }

    public void setFidInputIscd(String fidInputIscd) {
        this.fidInputIscd = fidInputIscd;
    }

    public String getFidCondMrktDivCode() {
        return fidCondMrktDivCode;
    }

    public void setFidCondMrktDivCode(String fidCondMrktDivCode) {
        this.fidCondMrktDivCode = fidCondMrktDivCode;
    }
}
