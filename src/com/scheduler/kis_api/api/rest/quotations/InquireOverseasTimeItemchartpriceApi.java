package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

/**
 * Overseas stock time chart price.
 * path: /uapi/overseas-price/v1/quotations/inquire-time-itemchartprice
 */
@RestApi(method = RestApi.Method.GET, path = "/uapi/overseas-price/v1/quotations/inquire-time-itemchartprice")
public class InquireOverseasTimeItemchartpriceApi extends CommonRestApi<InquireOverseasTimeItemchartpriceResult> {

    @Header
    private String trId = "HHDFS76950200";

    @Parameter("AUTH")
    private String auth = "";

    @Parameter("EXCD")
    private String excd;

    @Parameter("SYMB")
    private String symb;

    @Parameter("NMIN")
    private String nmin = "30";

    @Parameter("PINC")
    private String pinc = "1";

    @Parameter("NEXT")
    private String next = "";

    @Parameter("NREC")
    private String nrec = "120";

    @Parameter("FILL")
    private String fill = "";

    @Parameter("KEYB")
    private String keyb = "";

    public String getTrId() {
        return trId;
    }

    public void setTrId(String trId) {
        this.trId = trId;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getExcd() {
        return excd;
    }

    public void setExcd(String excd) {
        this.excd = excd;
    }

    public String getSymb() {
        return symb;
    }

    public void setSymb(String symb) {
        this.symb = symb;
    }

    public String getNmin() {
        return nmin;
    }

    public void setNmin(String nmin) {
        this.nmin = nmin;
    }

    public String getPinc() {
        return pinc;
    }

    public void setPinc(String pinc) {
        this.pinc = pinc;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getNrec() {
        return nrec;
    }

    public void setNrec(String nrec) {
        this.nrec = nrec;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }

    public String getKeyb() {
        return keyb;
    }

    public void setKeyb(String keyb) {
        this.keyb = keyb;
    }
}
