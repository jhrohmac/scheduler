package com.scheduler.stock.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import com.scheduler.stock.dao.TradeDateDao;

public class TradeDateService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private TradeDateDao tradeDateDao;

    public TradeDateDao getTradeDateDao() {
        return tradeDateDao;
    }

    public void setTradeDateDao(TradeDateDao tradeDateDao) {
        this.tradeDateDao = tradeDateDao;
    }

    public String resolveToday() {
        return LocalDate.now(KOREA_ZONE).format(ISO_DATE);
    }

    public String normalizeInputBaseDate(String inputBaseDate) {
        if (inputBaseDate == null || inputBaseDate.trim().isEmpty()) {
            return resolveToday();
        }

        String value = inputBaseDate.trim();
        try {
            if (value.contains("-")) {
                return LocalDate.parse(value, ISO_DATE).format(ISO_DATE);
            }
            return LocalDate.parse(value, BASIC_DATE).format(ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("baseDt 형식이 올바르지 않습니다. yyyy-MM-dd 또는 yyyyMMdd 를 사용하세요.");
        }
    }

    public String resolveEffectiveBaseDate(String inputBaseDate) throws Exception {
        return resolveEffectiveBaseDate(inputBaseDate, "KR");
    }

    public String resolveEffectiveBaseDate(String inputBaseDate, String marketGroup) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("inputBaseDt", normalizeInputBaseDate(inputBaseDate));
        map.put("mktCd", normalizeMarketGroup(marketGroup));
        String effectiveBaseDt = tradeDateDao.selectEffectiveBaseDate(map);
        if (effectiveBaseDt == null || effectiveBaseDt.trim().isEmpty()) {
            throw new IllegalStateException("유효 거래일(EFFECTIVE_BASE_DATE)을 찾을 수 없습니다.");
        }
        return effectiveBaseDt;
    }

    public String resolveReadBaseDate(String inputBaseDate, String latestStoredBaseDt) throws Exception {
        return resolveReadBaseDate(inputBaseDate, latestStoredBaseDt, "KR");
    }

    public String resolveReadBaseDate(String inputBaseDate, String latestStoredBaseDt, String marketGroup) throws Exception {
        if (inputBaseDate != null && inputBaseDate.trim().length() > 0) {
            return resolveEffectiveBaseDate(inputBaseDate, marketGroup);
        }
        if (latestStoredBaseDt != null && latestStoredBaseDt.trim().length() > 0) {
            return latestStoredBaseDt;
        }
        return resolveEffectiveBaseDate(resolveToday(), marketGroup);
    }

    public String toYearMonth(String baseDate) {
        return LocalDate.parse(baseDate, ISO_DATE).format(YYYYMM);
    }

    public String addTradingDays(String baseDate, String marketGroup, int offset) throws Exception {
        String normalizedBaseDate = normalizeInputBaseDate(baseDate);
        if (offset <= 0) {
            return normalizedBaseDate;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("baseDt", normalizedBaseDate);
        map.put("mktCd", normalizeMarketGroup(marketGroup));
        map.put("offset", String.valueOf(offset));
        String tradeDt = tradeDateDao.selectTradingDateByOffset(map);
        return tradeDt == null ? normalizedBaseDate : tradeDt;
    }

    public int diffTradingDays(String fromDt, String toDt, String marketGroup) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("fromDt", normalizeInputBaseDate(fromDt));
        map.put("toDt", normalizeInputBaseDate(toDt));
        map.put("mktCd", normalizeMarketGroup(marketGroup));
        Integer count = tradeDateDao.selectTradingDaysBetween(map);
        return count == null ? 0 : Math.max(count.intValue() - 1, 0);
    }

    public List<String> selectTradingDatesBetween(String fromDt, String toDt, String marketGroup) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("fromDt", normalizeInputBaseDate(fromDt));
        map.put("toDt", normalizeInputBaseDate(toDt));
        map.put("mktCd", normalizeMarketGroup(marketGroup));
        List<String> list = tradeDateDao.selectTradingDateListBetween(map);
        return list == null ? new ArrayList<String>() : list;
    }

    private String normalizeMarketGroup(String marketGroup) {
        if (marketGroup == null || marketGroup.trim().length() == 0) {
            return "KR";
        }
        return "US".equalsIgnoreCase(marketGroup.trim()) ? "US" : "KR";
    }
}
