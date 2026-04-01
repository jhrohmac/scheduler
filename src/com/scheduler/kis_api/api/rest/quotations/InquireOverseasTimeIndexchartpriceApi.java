package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

/**
 * Overseas index intraday chart price.
 * path: /uapi/overseas-price/v1/quotations/inquire-time-indexchartprice
 */
@RestApi(method = RestApi.Method.GET, path = "/uapi/overseas-price/v1/quotations/inquire-time-indexchartprice")
public class InquireOverseasTimeIndexchartpriceApi extends CommonRestApi<InquireOverseasTimeIndexchartpriceResult> {

    @Header
    private String trId = "FHKST03030200";

    @Header
    private String trCont;

    @Parameter("FID_COND_MRKT_DIV_CODE")
    private String fidCondMrktDivCode = "N";

    @Parameter("FID_INPUT_ISCD")
    private String fidInputIscd;

    @Parameter("FID_HOUR_CLS_CODE")
    private String fidHourClsCode = "0";

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

    public String getFidInputIscd() {
        return fidInputIscd;
    }

    public void setFidInputIscd(String fidInputIscd) {
        this.fidInputIscd = fidInputIscd;
    }

    public String getFidHourClsCode() {
        return fidHourClsCode;
    }

    public void setFidHourClsCode(String fidHourClsCode) {
        this.fidHourClsCode = fidHourClsCode;
    }

    public String getFidPwDataIncuYn() {
        return fidPwDataIncuYn;
    }

    public void setFidPwDataIncuYn(String fidPwDataIncuYn) {
        this.fidPwDataIncuYn = fidPwDataIncuYn;
    }
}
