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
 * 해외주식 현재체결가[v1_해외주식-009]
 * path: /uapi/overseas-price/v1/quotations/price
 */
@NoArgsConstructor
@RequiredArgsConstructor
@Setter
@RestApi(method = RestApi.Method.GET, path = "/uapi/overseas-price/v1/quotations/price")
public class PriceApi extends CommonRestApi<PriceResult> {

    @Header
    private String trId = "HHDFS00000300";

    @Parameter("AUTH")
    private String auth = "";

    @Parameter("EXCD")
    @NonNull
    private String excd;

    @Parameter("SYMB")
    @NonNull
    private String symb;

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
}
