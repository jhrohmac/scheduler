package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주식기본조회 [v1_국내주식-067]
 *
 * - HTTP Method : GET
 * - URL        : /uapi/domestic-stock/v1/quotations/search-stock-info
 * - 실전 TR_ID : CTPF1002R
 */
@NoArgsConstructor
@Setter
@RestApi(method = RestApi.Method.GET, path = "/uapi/domestic-stock/v1/quotations/search-stock-info")
public class SearchStockInfoApi extends CommonRestApi<SearchStockInfoResult> {

    /**
     * 거래 ID (TR_ID)
     * - CTPF1002R : 주식기본조회
     */
    @Header("tr_id")
    private String trId = "CTPF1002R";

    /**
     * 상품유형코드 (필수)
     *  - 국내 주식: 300
     */
    @Parameter
    private String prdtTypeCd;

    /**
     * 상품번호 (필수)
     *  - 예: 005930, 000660 등
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
