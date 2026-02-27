package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestApi;
import com.scheduler.kis_client.api.annotation.Header;
import com.scheduler.kis_client.api.annotation.Parameter;
import com.scheduler.kis_client.api.annotation.RestApi;

// NOTE
// - 프로젝트 일부 환경에서 Lombok 애노테이션 프로세싱이 비활성화되어
//   @RequiredArgsConstructor/@Setter/@NoArgsConstructor가 동작하지 않는 사례가 있어
//   컴파일/실행 안정성을 위해 명시 생성자/Getter/Setter를 직접 구현한다.

/**
 * ETF 구성종목시세[국내주식-073]
 */
@RestApi(method = RestApi.Method.GET, path = "/uapi/etfetn/v1/quotations/inquire-component-stock-price")
public class EtfetnInquireComponentStockPriceApi extends CommonRestApi<EtfetnInquireComponentStockPriceResult> {

    @Header
    private String trId = "FHKST121600C0";

    /**
     * 조건시장분류코드
     *
     * 시장구분코드 (J)
     */
    @Parameter
    private String fidCondMrktDivCode = "J";

    /**
     * 입력종목코드
     *
     * 종목코드
     */
    @Parameter
    private String fidInputIscd;

    /**
     * 조건화면분류코드
     *
     * Unique key( 11216 )
     */
    @Parameter
    private String fidCondScrDivCode = "11216";

    public EtfetnInquireComponentStockPriceApi() {
    }

    public EtfetnInquireComponentStockPriceApi(String fidInputIscd) {
        this.fidInputIscd = fidInputIscd;
    }

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

    public String getFidCondScrDivCode() {
        return fidCondScrDivCode;
    }

    public void setFidCondScrDivCode(String fidCondScrDivCode) {
        this.fidCondScrDivCode = fidCondScrDivCode;
    }

}
