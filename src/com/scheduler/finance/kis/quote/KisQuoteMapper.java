package com.scheduler.finance.kis.quote;

import com.scheduler.kis_api.api.realtime.H0STCNT0Data;
import com.scheduler.kis_client.api.RealTimeApiData;
import com.scheduler.kis_api.api.rest.quotations.InquireIndexPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.PriceResult;

public final class KisQuoteMapper {
    private KisQuoteMapper() {
    }

    public static KisQuoteDto fromDomesticRest(String token, String code, InquirePriceResult result) {
        return fromDomesticRest(token, "KRX", code, result);
    }

    public static KisQuoteDto fromDomesticRest(String token, String market, String code, InquirePriceResult result) {
        String effectiveMarket = KisMarketCode.normalizeDomesticMarket(market);
        KisQuoteDto dto = base("WL", token, "KR", effectiveMarket, code, "REST_CURRENT");
        if (result == null || result.getOutput() == null) {
            dto.setStale(true);
            return dto;
        }

        InquirePriceResult.Output o = result.getOutput();
        String sign = normalizeSign(o.getPrdyVrssSign(), o.getPrdyVrss());
        dto.setPrice(safe(o.getStckPrpr()));
        dto.setDiff(applySign(o.getPrdyVrss(), sign));
        dto.setRate(applySign(o.getPrdyCtrt(), sign));
        dto.setSign(sign);
        dto.setBasePrice(firstNonEmpty(o.getStckSdpr(), calcBase(dto.getPrice(), dto.getDiff())));
        dto.setOpen(safe(o.getStckOprc()));
        dto.setHigh(safe(o.getStckHgpr()));
        dto.setLow(safe(o.getStckLwpr()));
        dto.setTotalVolume(safe(o.getAcmlVol()));
        return dto;
    }

    public static KisQuoteDto fromDomesticTick(String token, String code, H0STCNT0Data data) {
        return fromDomesticTick(token, "KRX", code, data);
    }

    public static KisQuoteDto fromDomesticTick(String token, String market, String code, RealTimeApiData data) {
        String effectiveMarket = KisMarketCode.normalizeDomesticMarket(market);
        KisQuoteDto dto = base("WL", token, "KR", effectiveMarket, code, "WS_TICK");
        if (data == null) {
            dto.setStale(true);
            return dto;
        }

        String sign = normalizeSign(read(data, "getPrdyVrssSign"), read(data, "getPrdyVrss"));
        dto.setPrice(safe(read(data, "getStckPrpr")));
        dto.setDiff(applySign(read(data, "getPrdyVrss"), sign));
        dto.setRate(applySign(read(data, "getPrdyCtrt"), sign));
        dto.setSign(sign);
        dto.setBasePrice(calcBase(dto.getPrice(), dto.getDiff()));
        dto.setOpen(safe(read(data, "getStckOprc")));
        dto.setHigh(safe(read(data, "getStckHgpr")));
        dto.setLow(safe(read(data, "getStckLwpr")));
        dto.setVolume(safe(read(data, "getCntgVol")));
        dto.setTotalVolume(safe(read(data, "getAcmlVol")));
        dto.setAskPrice(safe(read(data, "getAskp1")));
        dto.setBidPrice(safe(read(data, "getBidp1")));
        dto.setTradeTime(safe(read(data, "getStckCntgHour")));
        return dto;
    }

    public static KisQuoteDto fromOverseasRest(String token, String country, String market, String code,
            PriceResult result) {
        KisQuoteDto dto = base("WL", token, country, market, code, "REST_OVERSEAS");
        if (result == null || result.getOutput() == null) {
            dto.setStale(true);
            return dto;
        }

        PriceResult.Output o = result.getOutput();
        String sign = normalizeSign(o.getSign(), o.getDiff());
        dto.setPrice(safe(o.getLast()));
        dto.setDiff(applySign(o.getDiff(), sign));
        dto.setRate(applySign(o.getRate(), sign));
        dto.setSign(sign);
        dto.setBasePrice(firstNonEmpty(o.getBase(), calcBase(dto.getPrice(), dto.getDiff())));
        dto.setTotalVolume(safe(o.getTvol()));
        return dto;
    }

    public static KisQuoteDto fromDomesticIndex(String token, String code, InquireIndexPriceResult result) {
        KisQuoteDto dto = base("SUMMARY", token, "KR", "KRX", code, "REST_INDEX");
        if (result == null || result.getOutput() == null) {
            dto.setStale(true);
            return dto;
        }

        InquireIndexPriceResult.Output o = result.getOutput();
        String sign = normalizeSign(o.getPrdyVrssSign(), o.getBstpNmixPrdyVrss());
        dto.setPrice(safe(o.getBstpNmixPrpr()));
        dto.setDiff(applySign(o.getBstpNmixPrdyVrss(), sign));
        dto.setRate(applySign(o.getBstpNmixPrdyCtrt(), sign));
        dto.setSign(sign);
        dto.setBasePrice(calcBase(dto.getPrice(), dto.getDiff()));
        return dto;
    }

    public static KisQuoteDto fromOverseasIndex(String token, String country, String market, String code,
            InquireOverseasDailyChartPriceResult result) {
        KisQuoteDto dto = base("SUMMARY", token, country, market, code, "REST_OVERSEAS_INDEX");
        if (result == null || result.getOutput1() == null) {
            dto.setStale(true);
            return dto;
        }

        InquireOverseasDailyChartPriceResult.Output1 o = result.getOutput1();
        String sign = normalizeSign(o.getPrdyVrssSign(), o.getOvrsNmixPrdyVrss());
        dto.setPrice(safe(o.getOvrsNmixPrpr()));
        dto.setDiff(applySign(o.getOvrsNmixPrdyVrss(), sign));
        dto.setRate(applySign(o.getPrdyCtrt(), sign));
        dto.setSign(sign);
        dto.setBasePrice(calcBase(dto.getPrice(), dto.getDiff()));
        return dto;
    }

    private static KisQuoteDto base(String type, String token, String country, String market, String code,
            String source) {
        KisQuoteDto dto = new KisQuoteDto();
        dto.setType(type);
        dto.setToken(safe(token));
        dto.setCountry(safe(country));
        dto.setMarket(safe(market));
        dto.setCode(safe(code));
        dto.setSource(source);
        dto.setFetchedAt(System.currentTimeMillis());
        dto.setStale(false);
        return dto;
    }

    private static String normalizeSign(Object sign, Object diff) {
        String s = safe(sign);
        if ("1".equals(s) || "2".equals(s) || "+".equals(s)) {
            return "+";
        }
        if ("4".equals(s) || "5".equals(s) || "-".equals(s)) {
            return "-";
        }
        if ("0".equals(s) || "3".equals(s) || " ".equals(s)) {
            return "0";
        }
        double d = parseDouble(diff);
        if (d > 0) {
            return "+";
        }
        if (d < 0) {
            return "-";
        }
        return s.isEmpty() ? "0" : s;
    }

    private static String applySign(Object value, String sign) {
        String raw = safe(value);
        if (raw.isEmpty()) {
            return "";
        }

        double n = parseDouble(raw);
        if (Double.isNaN(n)) {
            return raw;
        }

        String abs = stripLeadingSign(raw);
        if (n == 0d || "0".equals(sign)) {
            return abs;
        }
        if ("-".equals(sign)) {
            return "-" + abs;
        }
        if ("+".equals(sign)) {
            return "+" + abs;
        }
        return raw;
    }

    private static String stripLeadingSign(String value) {
        String s = safe(value).replace(",", "");
        while (s.startsWith("+") || s.startsWith("-")) {
            s = s.substring(1);
        }
        return s;
    }

    private static String calcBase(String price, String diff) {
        double p = parseDouble(price);
        double d = parseDouble(diff);
        if (Double.isNaN(p) || Double.isNaN(d)) {
            return "";
        }
        double base = p - d;
        if (Math.abs(base - Math.rint(base)) < 0.0000001d) {
            return String.valueOf((long) Math.rint(base));
        }
        return String.valueOf(base);
    }

    private static String firstNonEmpty(Object first, Object second) {
        String a = safe(first);
        return a.isEmpty() ? safe(second) : a;
    }

    private static double parseDouble(Object v) {
        String s = safe(v).replace(",", "");
        if (s.isEmpty()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static String safe(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static Object read(Object bean, String methodName) {
        if (bean == null || methodName == null) {
            return null;
        }
        try {
            return bean.getClass().getMethod(methodName).invoke(bean);
        } catch (Exception e) {
            return null;
        }
    }
}
