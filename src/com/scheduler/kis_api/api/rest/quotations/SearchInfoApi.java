package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품기본조회 [v1_국내주식-029]
 *
 * - HTTP Method : GET
 * - URL        : /uapi/domestic-stock/v1/quotations/search-info
 * - 실전 TR_ID : CTPF1604R
 */
@NoArgsConstructor
@Setter
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/quotations/search-info")
public class SearchInfoApi extends CommonRestApi<SearchInfoResult> {

    /**
     * 거래 ID (TR_ID)
     * - CTPF1604R : 상품기본조회
     */
    @Header("tr_id")
    private String trId = "CTPF1604R";

    /**
     * 상품번호 (필수)
     *
     * 예)
     *  - 국내주식(하이닉스) : 000660 (코드 : 300)
     *  - 선물(101S12)     : KR4101SC0009 (코드 : 301)
     *  - 미국(AAPL)       : AAPL (코드 : 512)
     */
    @Parameter
    private String pdno;

    /**
     * 상품유형코드 (필수)
     *
     * 예)
     *  - 300 : 주식
     *  - 301 : 선물옵션
     *  - 302 : 채권
     *  - 512 : 미국 나스닥
     *  - ...
     */
    @Parameter
    private String prdtTypeCd;

	public String getTrId() {
		return trId;
	}

	public void setTrId(String trId) {
		this.trId = trId;
	}

	public String getPdno() {
		return pdno;
	}

	public void setPdno(String pdno) {
		this.pdno = pdno;
	}

	public String getPrdtTypeCd() {
		return prdtTypeCd;
	}

	public void setPrdtTypeCd(String prdtTypeCd) {
		this.prdtTypeCd = prdtTypeCd;
	}

}
