package com.scheduler.stock.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult.Output2;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.PriceApi;
import com.scheduler.kis_api.api.rest.quotations.PriceResult;
import com.scheduler.kis_client.KisClient;
import com.scheduler.stock.dao.StkMasterDao;
import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.RecSignalDto;

public class KisDlyPriceSyncService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int DEFAULT_FETCH_DAYS = 400;
    private static final int DEFAULT_REQUEST_INTERVAL_MS = 1000;
    private static final int MAX_LOOP_COUNT = 60;

    private static final class RequestThrottle {
        private final long intervalMs;
        private boolean firstRequest = true;

        private RequestThrottle(long intervalMs) {
            this.intervalMs = intervalMs;
        }

        private void beforeRequest() throws Exception {
            if (firstRequest) {
                firstRequest = false;
                return;
            }
            if (intervalMs <= 0L) {
                return;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("KIS 요청 대기 중 인터럽트가 발생했습니다.");
            }
        }
    }

    private StkMasterDao stkMasterDao;

    public void setStkMasterDao(StkMasterDao stkMasterDao) {
        this.stkMasterDao = stkMasterDao;
    }

    public HashMap<String, Object> sync(HashMap<String, String> map) throws Exception {
        HashMap<String, String> requestMap = new HashMap<String, String>();
        if (map != null) {
            requestMap.putAll(map);
        }

        String marketGroup = normalizeMarketGroup(requestMap.get("marketGroup"));
        String targetStkCd = trim(requestMap.get("stkCd"));
        LocalDate endDate = resolveDate(requestMap.get("endDt"), LocalDate.now(KOREA_ZONE));
        int days = parsePositiveInt(requestMap.get("days"), DEFAULT_FETCH_DAYS);
        int limit = parsePositiveInt(requestMap.get("limit"), 0);
        long requestIntervalMs = parsePositiveLong(requestMap.get("requestIntervalMs"), DEFAULT_REQUEST_INTERVAL_MS);

        List<RecSignalDto> universe = stkMasterDao.selectStkMasterList(new HashMap<String, String>());
        if (universe == null) {
            universe = new ArrayList<RecSignalDto>();
        }

        int targetStockCnt = 0;
        int fetchedStockCnt = 0;
        int skippedStockCnt = 0;
        int failedStockCnt = 0;
        long fetchedPriceCnt = 0L;
        String lastError = null;

        for (int i = 0; i < universe.size(); i++) {
            RecSignalDto stock = universe.get(i);
            if (stock == null || isBlank(stock.getStkCd())) {
                continue;
            }
            if (!matchesMarketGroup(stock.getMktCd(), marketGroup)) {
                continue;
            }
            if (!isBlank(targetStkCd) && !targetStkCd.equals(stock.getStkCd())) {
                continue;
            }
            if (limit > 0 && targetStockCnt >= limit) {
                break;
            }

            targetStockCnt++;

            try {
                List<DlyPriceDto> priceList = fetchAdjustedDailyPrices(
                    stock.getStkCd(),
                    stock.getMktCd(),
                    marketGroup,
                    endDate.format(ISO_DATE),
                    days,
                    requestIntervalMs
                );
                if (priceList == null || priceList.isEmpty()) {
                    skippedStockCnt++;
                    continue;
                }
                fetchedStockCnt++;
                fetchedPriceCnt += priceList.size();
            } catch (Exception e) {
                failedStockCnt++;
                lastError = stock.getStkCd() + " : " + e.getLocalizedMessage();
            }
        }

        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("marketGroup", marketGroup);
        resultMap.put("targetStockCnt", Integer.valueOf(targetStockCnt));
        resultMap.put("fetchedStockCnt", Integer.valueOf(fetchedStockCnt));
        resultMap.put("skippedStockCnt", Integer.valueOf(skippedStockCnt));
        resultMap.put("failedStockCnt", Integer.valueOf(failedStockCnt));
        resultMap.put("fetchedPriceCnt", Long.valueOf(fetchedPriceCnt));
        resultMap.put("days", Integer.valueOf(days));
        resultMap.put("endDt", endDate.format(ISO_DATE));
        resultMap.put("requestIntervalMs", Long.valueOf(requestIntervalMs));
        resultMap.put("lastError", lastError);
        return resultMap;
    }

    public List<DlyPriceDto> fetchAdjustedDailyPrices(String stkCd, String mktCd, String marketGroup, String endDt,
            int days, long requestIntervalMs) throws Exception {
        if (isBlank(stkCd)) {
            return new ArrayList<DlyPriceDto>();
        }

        LocalDate endDate = resolveDate(endDt, LocalDate.now(KOREA_ZONE));
        int safeDays = parsePositiveInt(String.valueOf(days), DEFAULT_FETCH_DAYS);
        LocalDate startDate = endDate.minusDays(Math.max(safeDays - 1, 0));
        RequestThrottle throttle = new RequestThrottle(parsePositiveLong(String.valueOf(requestIntervalMs),
                DEFAULT_REQUEST_INTERVAL_MS));

        List<DlyPriceDto> priceList;
        if ("KR".equals(normalizeMarketGroup(marketGroup))) {
            priceList = fetchDomesticAdjustedDailyPrices(stkCd, startDate, endDate, throttle);
        } else {
            priceList = fetchOverseasAdjustedDailyPrices(stkCd, mktCd, startDate, endDate, throttle);
        }

        Collections.sort(priceList, new Comparator<DlyPriceDto>() {
            @Override
            public int compare(DlyPriceDto left, DlyPriceDto right) {
                return safeString(left == null ? null : left.getTradeDt())
                    .compareTo(safeString(right == null ? null : right.getTradeDt()));
            }
        });
        return priceList;
    }

    private List<DlyPriceDto> fetchDomesticAdjustedDailyPrices(String stkCd, LocalDate startDate, LocalDate endDate,
            RequestThrottle throttle) throws Exception {
        KisClient client = KisClientFactory.getClient();
        Map<String, DlyPriceDto> mergedMap = new LinkedHashMap<String, DlyPriceDto>();
        LocalDate currentEnd = endDate;
        int loopCount = 0;

        while (!currentEnd.isBefore(startDate) && loopCount < MAX_LOOP_COUNT) {
            throttle.beforeRequest();

            InquireDailyItemchartpriceApi api = new InquireDailyItemchartpriceApi();
            api.setFidInputIscd(stkCd);
            api.setFidInputDate1(startDate.format(BASIC_DATE));
            api.setFidInputDate2(currentEnd.format(BASIC_DATE));
            api.setFidPeriodDivCode("D");
            api.setFidOrgAdjPrc("0");

            InquireDailyItemchartpriceResult result = client.execute(api);
            if (result == null) {
                throw new IllegalStateException("KIS 기간별시세 응답이 null 입니다.");
            }
            if (!"0".equals(result.getRtCd())) {
                throw new IllegalStateException("KIS 기간별시세 API 오류 rtCd=" + result.getRtCd()
                        + ", msgCd=" + result.getMsgCd()
                        + ", msg1=" + result.getMsg1());
            }

            Output2[] outputs = result.getOutput2();
            if (outputs == null || outputs.length == 0) {
                break;
            }

            LocalDate oldestDate = null;
            for (int i = 0; i < outputs.length; i++) {
                Output2 output = outputs[i];
                if (output == null || isBlank(output.getStckBsopDate())) {
                    continue;
                }

                LocalDate tradeDate = LocalDate.parse(output.getStckBsopDate(), BASIC_DATE);
                if (tradeDate.isBefore(startDate) || tradeDate.isAfter(endDate)) {
                    continue;
                }

                DlyPriceDto dto = toDomesticDlyPriceDto(stkCd, tradeDate, output);
                mergedMap.put(dto.getTradeDt(), dto);

                if (oldestDate == null || tradeDate.isBefore(oldestDate)) {
                    oldestDate = tradeDate;
                }
            }

            if (oldestDate == null || !oldestDate.isAfter(startDate)) {
                break;
            }

            currentEnd = oldestDate.minusDays(1);
            loopCount++;
        }

        return new ArrayList<DlyPriceDto>(mergedMap.values());
    }

    private List<DlyPriceDto> fetchOverseasAdjustedDailyPrices(String stkCd, String mktCd, LocalDate startDate,
            LocalDate endDate, RequestThrottle throttle) throws Exception {
        KisClient client = KisClientFactory.getClient();
        Map<String, DlyPriceDto> mergedMap = new LinkedHashMap<String, DlyPriceDto>();
        LocalDate currentEnd = endDate;
        String keyb = "";
        int loopCount = 0;
        String excd = normalizeOverseasExcd(mktCd, "US");

        while (!currentEnd.isBefore(startDate) && loopCount < MAX_LOOP_COUNT) {
            throttle.beforeRequest();

            InquireOverseasDailyPriceApi api = new InquireOverseasDailyPriceApi();
            api.setExcd(excd);
            api.setSymb(stkCd);
            api.setGubn("0");
            api.setBymd(currentEnd.format(BASIC_DATE));
            api.setModp("0");
            api.setAuth("");
            api.setKeyb(keyb);

            InquireOverseasDailyPriceResult result = client.execute(api);
            if (result == null) {
                throw new IllegalStateException("KIS 해외 일봉 응답이 null 입니다.");
            }
            if (!"0".equals(result.getRtCd())) {
                throw new IllegalStateException("KIS 해외 일봉 API 오류 rtCd=" + result.getRtCd()
                        + ", msgCd=" + result.getMsgCd()
                        + ", msg1=" + result.getMsg1());
            }

            InquireOverseasDailyPriceResult.Output2[] outputs = result.getOutput2();
            if (outputs == null || outputs.length == 0) {
                break;
            }

            LocalDate oldestDate = null;
            for (int i = 0; i < outputs.length; i++) {
                InquireOverseasDailyPriceResult.Output2 output = outputs[i];
                if (output == null || isBlank(output.getXymd())) {
                    continue;
                }

                LocalDate tradeDate = LocalDate.parse(output.getXymd(), BASIC_DATE);
                if (tradeDate.isBefore(startDate) || tradeDate.isAfter(endDate)) {
                    continue;
                }

                DlyPriceDto dto = toOverseasDlyPriceDto(stkCd, tradeDate, output);
                mergedMap.put(dto.getTradeDt(), dto);

                if (oldestDate == null || tradeDate.isBefore(oldestDate)) {
                    oldestDate = tradeDate;
                }
            }

            if (oldestDate == null || !oldestDate.isAfter(startDate)) {
                break;
            }

            String nextKeyb = "";
            if (result.getOutput1() != null) {
                nextKeyb = safeString(result.getOutput1().getKeyb());
            }

            if (!isBlank(nextKeyb) && !nextKeyb.equals(keyb)) {
                keyb = nextKeyb;
            } else {
                keyb = "";
                currentEnd = oldestDate.minusDays(1);
            }
            loopCount++;
        }

        return new ArrayList<DlyPriceDto>(mergedMap.values());
    }

    private DlyPriceDto toDomesticDlyPriceDto(String stkCd, LocalDate tradeDate, Output2 output) {
        DlyPriceDto dto = new DlyPriceDto();
        dto.setStkCd(stkCd);
        dto.setTradeDt(tradeDate.format(ISO_DATE));
        dto.setAdjOpenPrice(parseDouble(output.getStckOprc()));
        dto.setAdjHighPrice(parseDouble(output.getStckHgpr()));
        dto.setAdjLowPrice(parseDouble(output.getStckLwpr()));
        dto.setAdjClosePrice(parseDouble(output.getStckClpr()));
        dto.setVolume(parseLong(output.getAcmlVol()));
        dto.setTradeValue(parseLong(output.getAcmlTrPbmn()));
        dto.setAdjFactor(Double.valueOf(1d));
        return dto;
    }

    private DlyPriceDto toOverseasDlyPriceDto(String stkCd, LocalDate tradeDate,
            InquireOverseasDailyPriceResult.Output2 output) {
        DlyPriceDto dto = new DlyPriceDto();
        dto.setStkCd(stkCd);
        dto.setTradeDt(tradeDate.format(ISO_DATE));
        dto.setAdjOpenPrice(parseDouble(output.getOpen()));
        dto.setAdjHighPrice(parseDouble(output.getHigh()));
        dto.setAdjLowPrice(parseDouble(output.getLow()));
        dto.setAdjClosePrice(parseDouble(output.getClos()));
        dto.setVolume(parseLong(output.getTvol()));
        dto.setTradeValue(parseLong(output.getTamt()));
        dto.setAdjFactor(Double.valueOf(1d));
        return dto;
    }

    /**
     * priceList에 effectiveBaseDt 데이터가 없으면 KIS 현재가 API를 호출해 당일 row를 합성해 반환.
     * 이미 데이터가 있거나 API 실패 시 원본 priceList를 반환한다.
     */
    public List<DlyPriceDto> appendCurrentPriceIfNeeded(List<DlyPriceDto> priceList, String stkCd, String mktCd,
            String marketGroup, String effectiveBaseDt, long requestIntervalMs) {
        if (hasTradeDt(priceList, effectiveBaseDt)) {
            return priceList;
        }
        try {
            if (requestIntervalMs > 0L) {
                Thread.sleep(requestIntervalMs);
            }
            DlyPriceDto currentDto = fetchCurrentPriceDto(stkCd, mktCd, marketGroup, effectiveBaseDt);
            if (currentDto == null
                    || currentDto.getAdjClosePrice() == null
                    || currentDto.getAdjClosePrice().doubleValue() <= 0d) {
                return priceList;
            }
            List<DlyPriceDto> augmented = new ArrayList<DlyPriceDto>(priceList);
            augmented.add(currentDto);
            return augmented;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return priceList;
        } catch (Exception e) {
            return priceList;
        }
    }

    private DlyPriceDto fetchCurrentPriceDto(String stkCd, String mktCd, String marketGroup,
            String today) throws Exception {
        if ("KR".equals(normalizeMarketGroup(marketGroup))) {
            return fetchDomesticCurrentPriceDto(stkCd, today);
        }
        return fetchOverseasCurrentPriceDto(stkCd, mktCd, today);
    }

    private DlyPriceDto fetchDomesticCurrentPriceDto(String stkCd, String today) throws Exception {
        KisClient client = KisClientFactory.getClient();
        InquirePriceApi api = new InquirePriceApi();
        api.setFidInputIscd(stkCd);

        InquirePriceResult result = client.execute(api);
        if (result == null) {
            throw new IllegalStateException("KIS 주식현재가 응답이 null 입니다.");
        }
        if (!"0".equals(result.getRtCd())) {
            throw new IllegalStateException("KIS 주식현재가 API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd() + ", msg1=" + result.getMsg1());
        }
        InquirePriceResult.Output output = result.getOutput();
        if (output == null) {
            return null;
        }
        DlyPriceDto dto = new DlyPriceDto();
        dto.setStkCd(stkCd);
        dto.setTradeDt(today);
        dto.setAdjOpenPrice(parseDouble(output.getStckOprc()));
        dto.setAdjHighPrice(parseDouble(output.getStckHgpr()));
        dto.setAdjLowPrice(parseDouble(output.getStckLwpr()));
        dto.setAdjClosePrice(parseDouble(output.getStckPrpr()));
        dto.setVolume(parseLong(output.getAcmlVol()));
        dto.setTradeValue(parseLong(output.getAcmlTrPbmn()));
        dto.setAdjFactor(Double.valueOf(1d));
        return dto;
    }

    private DlyPriceDto fetchOverseasCurrentPriceDto(String stkCd, String mktCd, String today) throws Exception {
        KisClient client = KisClientFactory.getClient();
        PriceApi api = new PriceApi();
        api.setExcd(normalizeOverseasExcd(mktCd, "US"));
        api.setSymb(stkCd);

        PriceResult result = client.execute(api);
        if (result == null) {
            throw new IllegalStateException("KIS 해외주식 현재가 응답이 null 입니다.");
        }
        if (!"0".equals(result.getRtCd())) {
            throw new IllegalStateException("KIS 해외주식 현재가 API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd() + ", msg1=" + result.getMsg1());
        }
        PriceResult.Output output = result.getOutput();
        if (output == null) {
            return null;
        }
        // US 현재가 API는 시가/고가/저가를 제공하지 않으므로 현재가로 대체
        Double currentPrice = parseDouble(output.getLast());
        if (currentPrice == null || currentPrice.doubleValue() <= 0d) {
            return null;
        }
        DlyPriceDto dto = new DlyPriceDto();
        dto.setStkCd(stkCd);
        dto.setTradeDt(today);
        dto.setAdjOpenPrice(currentPrice);
        dto.setAdjHighPrice(currentPrice);
        dto.setAdjLowPrice(currentPrice);
        dto.setAdjClosePrice(currentPrice);
        dto.setVolume(parseLong(output.getTvol()));
        dto.setTradeValue(parseLong(output.getTamt()));
        dto.setAdjFactor(Double.valueOf(1d));
        return dto;
    }

    private boolean hasTradeDt(List<DlyPriceDto> priceList, String tradeDt) {
        if (priceList == null || isBlank(tradeDt)) {
            return false;
        }
        for (int i = 0; i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto != null && tradeDt.equals(dto.getTradeDt())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeMarketGroup(String marketGroup) {
        String value = safeString(marketGroup).toUpperCase();
        if ("US".equals(value)) {
            return "US";
        }
        return "KR";
    }

    private boolean matchesMarketGroup(String mktCd, String marketGroup) {
        String group = normalizeMarketGroup(marketGroup);
        if ("US".equals(group)) {
            return !"KR".equals(resolveMarketGroup(mktCd));
        }
        return "KR".equals(resolveMarketGroup(mktCd));
    }

    private String resolveMarketGroup(String mktCd) {
        String value = safeString(mktCd).toUpperCase();
        if (value.length() == 0) {
            return "KR";
        }
        if ("KR".equals(value) || value.startsWith("KOS") || value.startsWith("KQ")) {
            return "KR";
        }
        return "US";
    }

    private String normalizeOverseasExcd(String market, String country) {
        String c = safeString(country).toUpperCase();
        String m = safeString(market).trim();
        if (m.length() == 0) {
            return "US".equals(c) ? "NAS" : m;
        }

        String u = m.toUpperCase();
        if ("NAS".equals(u) || "NYS".equals(u) || "AMS".equals(u) || "HKS".equals(u)
                || "SHS".equals(u) || "SZS".equals(u) || "TSE".equals(u) || "TYO".equals(u)
                || "HSX".equals(u) || "HNX".equals(u)) {
            return u;
        }

        if ("US".equals(c)) {
            if (u.indexOf("NASDAQ") >= 0 || u.indexOf("NAS") >= 0) {
                return "NAS";
            }
            if (u.indexOf("NYSE") >= 0 || u.indexOf("NYS") >= 0) {
                return "NYS";
            }
            if (u.indexOf("AMEX") >= 0 || u.indexOf("AMS") >= 0) {
                return "AMS";
            }
            return "NAS";
        }
        return u;
    }

    private LocalDate resolveDate(String value, LocalDate defaultDate) {
        if (isBlank(value)) {
            return defaultDate;
        }
        String trimmed = value.trim();
        if (trimmed.indexOf('-') >= 0) {
            return LocalDate.parse(trimmed, ISO_DATE);
        }
        return LocalDate.parse(trimmed, BASIC_DATE);
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long parsePositiveLong(String value, long defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0L ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Double parseDouble(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Double.valueOf(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
