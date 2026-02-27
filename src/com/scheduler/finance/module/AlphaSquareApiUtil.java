package com.scheduler.finance.module;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.sf.json.JSONObject;

import com.scheduler.comm.util.GetHttpsURLConnection;
import com.scheduler.comm.util.ImageDownload;
import com.scheduler.finance.vo.StockInfoVo;

/**
 * AlphaSquare REST API 호출/파싱 공통 유틸
 *
 * 기존 Finance 모듈에서 흩어져 있던 AlphaSquare 호출 코드를 모아
 * Controller / Service 쪽에서는 이 유틸만 사용하도록 정리한다.
 *
 * 사용 중인 주요 엔드포인트
 *  - /data/v2/price/current-price
 *  - /data/v2/stock/stocks
 *  - /data/v2/stock/details
 *  - /data/v3/prices/candles/{id}
 */
public class AlphaSquareApiUtil {

    private static final String BASE_URL    = "https://api.alphasquare.co.kr";
    private static final String BASE_URL_V2 = BASE_URL + "/data/v2";
    private static final String BASE_URL_V3 = BASE_URL + "/data/v3";

    private AlphaSquareApiUtil() {
    }

    // ==========================
    // 공통 HTTP GET 래퍼
    // ==========================

    /**
     * 공통 GET 호출 래퍼
     */
    private static String callGet(String url) throws IOException {
        return GetHttpsURLConnection.getHttpsGet(url);
    }

    /**
     * URL 파라미터 인코딩
     */
    private static String encode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    // ==========================
    // 1. 현재가 (/data/v2/price/current-price)
    // ==========================

    /**
     * 단일 종목 현재가 조회
     *
     * 예) https://api.alphasquare.co.kr/data/v2/price/current-price?code=005930
     */
    public static String callCurrentPriceSingle(String stockCode) throws IOException {
        String encodedCode = encode(stockCode);
        String url = BASE_URL_V2 + "/price/current-price?code=" + encodedCode;
        return callGet(url);
    }

    /**
     * /data/v2/price/current-price 응답(JSON)에서
     * 특정 종목 코드의 데이터만 꺼내서 반환
     *
     *  응답 예시:
     *  {
     *      "005930": {
     *          "dt": 1663148400000,
     *          "open": ...,
     *          "high": ...,
     *          "low": ...,
     *          "close": ...
     *      }
     *  }
     */
    public static JSONObject getCurrentPriceData(String stockCode, String responseJson) {
        JSONObject result = new JSONObject();
        if (responseJson == null || responseJson.trim().length() == 0) {
            return result;
        }

        try {
            JSONObject root = JSONObject.fromObject(responseJson);
            if (root.has(stockCode)) {
                result = root.getJSONObject(stockCode);
            }
        } catch (Exception e) {
            // 파싱 실패 시 빈 객체 리턴
        }

        return result;
    }

    // ==========================
    // 2. 종목 검색 (/data/v2/stock/stocks)
    // ==========================

    /**
     * 키워드 기반 종목 검색
     *
     * 예) https://api.alphasquare.co.kr/data/v2/stock/stocks?keyword=삼성전자
     */
    public static String callKeywordSingle(String keyword) throws IOException {
        String encodedKeyword = encode(keyword);
        String url = BASE_URL_V2 + "/stock/stocks?keyword=" + encodedKeyword;
        return callGet(url);
    }

    // ==========================
    // 3. 종목 상세 (/data/v2/stock/details)
    // ==========================

    /**
     * 종목 상세 조회 (/stock/details)
     *
     * 예) https://api.alphasquare.co.kr/data/v2/stock/details?code=005930
     */
    public static String callStockDetails(String stockCode) throws IOException {
        String encodedCode = encode(stockCode);
        String url = BASE_URL_V2 + "/stock/details?code=" + encodedCode;
        return callGet(url);
    }

    /**
     * /stock/details 응답(JSON 문자열)에서 StockInfoVo 생성
     *
     *  AlphaSquare details 응답 구조(예시):
     *  {
     *      "005930": {
     *          "id": 12345,
     *          "code": "005930",
     *          "name_kr": "...",
     *          "name_en": "...",
     *          "sector": "...",
     *          "market": "...",
     *          "market_section": "...",
     *          "country": "KR",
     *          "timezone": "Asia/Seoul",
     *          "currency": "KRW",
     *          "aliases": "...",
     *          "logo": "https://...",
     *          "description": "...",
     *          "url": "https://..."
     *      }
     *  }
     */
    public static StockInfoVo parseStockDetailsToVo(String stockCode, String responseJson) {
        if (responseJson == null || responseJson.trim().length() == 0) {
            return null;
        }

        try {
            JSONObject root = JSONObject.fromObject(responseJson);
            JSONObject data;

            if (root.has(stockCode)) {
                data = root.getJSONObject(stockCode);
            } else if (!root.isEmpty()) {
                Object firstKey = root.keys().hasNext() ? root.keys().next() : null;
                if (firstKey == null) {
                    return null;
                }
                data = root.getJSONObject(firstKey.toString());
            } else {
                return null;
            }

            StockInfoVo stockInfoVo = new StockInfoVo();
            stockInfoVo.setStock_id(data.optString("id", null));
            stockInfoVo.setStock_code(data.optString("code", stockCode));
            stockInfoVo.setStock_ko_name(data.optString("name_kr", null));
            stockInfoVo.setStock_en_name(data.optString("name_en", null));
            stockInfoVo.setStock_sector(data.optString("sector", null));
            stockInfoVo.setStock_market(data.optString("market", null));
            stockInfoVo.setMarket_section(data.optString("market_section", null));
            stockInfoVo.setStock_country_code(data.optString("country", null));
            stockInfoVo.setStock_timezone(data.optString("timezone", null));
            stockInfoVo.setStock_currency(data.optString("currency", null));
            stockInfoVo.setStock_aliases(data.optString("aliases", null));
            stockInfoVo.setStock_desc(data.optString("description", null));
            stockInfoVo.setStock_url(data.optString("url", null));

            String logoUrl = data.optString("logo", null);
            if (logoUrl != null && logoUrl.trim().length() > 0) {
                stockInfoVo.setStock_logo(logoUrl);
            }

            return stockInfoVo;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * /stock/details 전체 호출 + StockInfoVo 생성까지 한 번에 처리하는 헬퍼
     */
    public static StockInfoVo getStockInfoByCode(String stockCode) throws IOException {
        String result = callStockDetails(stockCode);
        StockInfoVo stockInfoVo = parseStockDetailsToVo(stockCode, result);

        if (stockInfoVo != null && stockInfoVo.getStock_url() != null && stockInfoVo.getStock_url().length() > 0) {
            ImageDownload.imageDownload(stockInfoVo);
        }

        return stockInfoVo;
    }

    // ==========================
    // 4. 캔들 데이터 (/data/v3/prices/candles)
    // ==========================

    /**
     * StockCodeInfoController.getChartData() 에서 직접 작성하던
     *
     *   String candlesUrl =
     *       "https://api.alphasquare.co.kr/data/v3/prices/candles/" + id
     *       + "?freq=" + period
     *       + "&limit=" + limit
     *       + "&include_current_candle=" + current_candle_div;
     *
     * 부분을 공통 유틸로 분리.
     *
     * Controller 쪽에서는 이 메서드만 호출해서 URL 을 얻도록 변경하면 된다.
     */
    public static String buildCandlesUrl(String id, String period, String limit, String includeCurrentCandle) {
        if (id == null) {
            id = "";
        }
        if (period == null) {
            period = "";
        }
        if (limit == null) {
            limit = "";
        }
        if (includeCurrentCandle == null || includeCurrentCandle.trim().length() == 0) {
            includeCurrentCandle = "false";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(BASE_URL_V3)
          .append("/prices/candles/")
          .append(id)
          .append("?freq=").append(period);

        if (limit.length() > 0) {
            sb.append("&limit=").append(limit);
        }

        sb.append("&include_current_candle=").append(includeCurrentCandle);

        return sb.toString();
    }

    /**
     * 캔들 데이터 직접 호출을 위한 헬퍼 메서드
     *
     * 예)
     *  String result =
     *      AlphaSquareApiUtil.callCandles(id, period, limit, current_candle_div);
     */
    public static String callCandles(String id, String period, String limit, String includeCurrentCandle) throws IOException {
        String url = buildCandlesUrl(id, period, limit, includeCurrentCandle);
        return callGet(url);
    }
}
