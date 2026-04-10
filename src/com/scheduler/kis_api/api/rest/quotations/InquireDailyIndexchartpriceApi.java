package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 국내주식업종기간별시세(일/주/월/년)
 * path: /uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice
 */
@NoArgsConstructor
@RequiredArgsConstructor
@Setter
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice")
public class InquireDailyIndexchartpriceApi extends CommonRestApi<InquireDailyIndexchartpriceResult> {

    @Header
    private String trId = "FHKUP03500100";

    @Parameter("FID_COND_MRKT_DIV_CODE")
    private String fidCondMrktDivCode;

    @Parameter("FID_INPUT_ISCD")
    @NonNull
    private String fidInputIscd;

    @Parameter("FID_INPUT_DATE_1")
    @NonNull
    private String fidInputDate1;

    @Parameter("FID_INPUT_DATE_2")
    @NonNull
    private String fidInputDate2;

    @Parameter("FID_PERIOD_DIV_CODE")
    private String fidPeriodDivCode;

    public String getTrId() {
        return trId;
    }

    public void setTrId(String trId) {
        this.trId = trId;
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

    public String getFidInputDate1() {
        return fidInputDate1;
    }

    public void setFidInputDate1(String fidInputDate1) {
        this.fidInputDate1 = fidInputDate1;
    }

    public String getFidInputDate2() {
        return fidInputDate2;
    }

    public void setFidInputDate2(String fidInputDate2) {
        this.fidInputDate2 = fidInputDate2;
    }

    public String getFidPeriodDivCode() {
        return fidPeriodDivCode;
    }

    public void setFidPeriodDivCode(String fidPeriodDivCode) {
        this.fidPeriodDivCode = fidPeriodDivCode;
    }
}
