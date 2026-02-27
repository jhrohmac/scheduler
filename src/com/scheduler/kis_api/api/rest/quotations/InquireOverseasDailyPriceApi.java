package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

/**
 * Overseas stock daily/weekly/monthly prices.
 * path: /uapi/overseas-price/v1/quotations/dailyprice
 */
@RestApi(method = RestApi.Method.GET, path = "/uapi/overseas-price/v1/quotations/dailyprice")
public class InquireOverseasDailyPriceApi extends CommonRestApi<InquireOverseasDailyPriceResult> {

    @Header
    private String trId = "HHDFS76240000";

    @Parameter("AUTH")
    private String auth = "";

    @Parameter("EXCD")
    private String excd;

    @Parameter("SYMB")
    private String symb;

    @Parameter("GUBN")
    private String gubn = "0";

    @Parameter("BYMD")
    private String bymd;

    @Parameter("MODP")
    private String modp = "0";

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

    public String getGubn() {
        return gubn;
    }

    public void setGubn(String gubn) {
        this.gubn = gubn;
    }

    public String getBymd() {
        return bymd;
    }

    public void setBymd(String bymd) {
        this.bymd = bymd;
    }

    public String getModp() {
        return modp;
    }

    public void setModp(String modp) {
        this.modp = modp;
    }

    public String getKeyb() {
        return keyb;
    }

    public void setKeyb(String keyb) {
        this.keyb = keyb;
    }
}
