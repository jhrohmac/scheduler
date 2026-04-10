package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 해외주식 상품기본정보 [v1_해외주식-034]
 *
 * - HTTP Method : GET
 * - URL        : /uapi/overseas-price/v1/quotations/search-info
 * - 실전 TR_ID : CTPF1702R
 */
@NoArgsConstructor
@Setter
@RestApi(method = RestApi.Method.GET, path = "/uapi/overseas-price/v1/quotations/search-info")
public class SearchOverseasStockInfoApi extends CommonRestApi<SearchOverseasStockInfoResult> {

    /**
     * 거래 ID (TR_ID)
     * - CTPF1702R : 해외주식 상품기본정보
     */
    @Header("tr_id")
    private String trId = "CTPF1702R";

    /**
     * 상품유형코드 (필수)
     *  - 512 미국 나스닥 / 513 미국 뉴욕 / 529 미국 아멕스
     *  - 515 일본
     *  - 501 홍콩 / 543 홍콩CNY / 558 홍콩USD
     *  - 507 베트남 하노이 / 508 베트남 호치민
     *  - 551 중국 상해A / 552 중국 심천A
     */
    @Parameter
    private String prdtTypeCd;

    /**
     * 상품번호 (필수)
     *  - 예: AAPL, TSLA 등
     */
    @Parameter
    private String pdno;

    public String getTrId() {
        return trId;
    }

    public void setTrId(String trId) {
        this.trId = trId;
    }

    public String getPrdtTypeCd() {
        return prdtTypeCd;
    }

    public void setPrdtTypeCd(String prdtTypeCd) {
        this.prdtTypeCd = prdtTypeCd;
    }

    public String getPdno() {
        return pdno;
    }

    public void setPdno(String pdno) {
        this.pdno = pdno;
    }
}
