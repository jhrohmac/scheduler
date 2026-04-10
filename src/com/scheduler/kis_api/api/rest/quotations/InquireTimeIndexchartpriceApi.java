package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

/**
 * Domestic index intraday chart price.
 * path: /uapi/domestic-stock/v1/quotations/inquire-time-indexchartprice
 */
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/quotations/inquire-time-indexchartprice")
public class InquireTimeIndexchartpriceApi extends CommonRestApi<InquireTimeIndexchartpriceResult> {

    @Header
    private String trId = "FHKUP03500200";

    @Header
    private String trCont;

    @Parameter("FID_COND_MRKT_DIV_CODE")
    private String fidCondMrktDivCode = "U";

    @Parameter("FID_ETC_CLS_CODE")
    private String fidEtcClsCode = "0";

    @Parameter("FID_INPUT_ISCD")
    private String fidInputIscd;

    @Parameter("FID_INPUT_HOUR_1")
    private String fidInputHour1 = "60";

    @Parameter("FID_PW_DATA_INCU_YN")
    private String fidPwDataIncuYn = "Y";

    public String getTrId() {
        return trId;
    }

    public void setTrId(String trId) {
        this.trId = trId;
    }

    public String getTrCont() {
        return trCont;
    }

    public void setTrCont(String trCont) {
        this.trCont = trCont;
    }

    public String getFidCondMrktDivCode() {
        return fidCondMrktDivCode;
    }

    public void setFidCondMrktDivCode(String fidCondMrktDivCode) {
        this.fidCondMrktDivCode = fidCondMrktDivCode;
    }

    public String getFidEtcClsCode() {
        return fidEtcClsCode;
    }

    public void setFidEtcClsCode(String fidEtcClsCode) {
        this.fidEtcClsCode = fidEtcClsCode;
    }

    public String getFidInputIscd() {
        return fidInputIscd;
    }

    public void setFidInputIscd(String fidInputIscd) {
        this.fidInputIscd = fidInputIscd;
    }

    public String getFidInputHour1() {
        return fidInputHour1;
    }

    public void setFidInputHour1(String fidInputHour1) {
        this.fidInputHour1 = fidInputHour1;
    }

    public String getFidPwDataIncuYn() {
        return fidPwDataIncuYn;
    }

    public void setFidPwDataIncuYn(String fidPwDataIncuYn) {
        this.fidPwDataIncuYn = fidPwDataIncuYn;
    }
}
