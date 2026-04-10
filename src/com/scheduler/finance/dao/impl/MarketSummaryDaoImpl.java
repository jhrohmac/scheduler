package com.scheduler.finance.dao.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.finance.dao.MarketSummaryDao;
import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.rest.quotations.InquireIndexPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireIndexPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.PriceApi;
import com.scheduler.kis_api.api.rest.quotations.PriceResult;
import com.scheduler.kis_client.KisClient;

public class MarketSummaryDaoImpl extends SqlSessionDaoSupport implements MarketSummaryDao {

    private static final String NS = "sql.MarketSummary.";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public List<?> selectWatchlistGroups(HashMap<String, String> map) throws Exception {
        try {
            return getSqlSession().selectList(NS + "selectWatchlistGroups", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".selectWatchlistGroups() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public List<?> selectWatchlistItems(HashMap<String, String> map) throws Exception {
        try {
            return getSqlSession().selectList(NS + "selectWatchlistItems", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".selectWatchlistItems() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public int insertWatchlistItem(HashMap<String, String> map) throws Exception {
        try {
            return getSqlSession().insert(NS + "insertWatchlistItem", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".insertWatchlistItem() : ", e.getLocalizedMessage());
        }
    }

    
    @Override
    public int deleteWatchlistItem(HashMap<String, String> map) throws Exception {
        try {
            return getSqlSession().delete(NS + "deleteWatchlistItem", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".deleteWatchlistItem() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public int selectWatchlistItemCount(HashMap<String, String> map) throws Exception {
        try {
            Integer cnt = getSqlSession().selectOne(NS + "selectWatchlistItemCount", map);
            return cnt == null ? 0 : cnt.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".selectWatchlistItemCount() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public List<?> selectWatchlistItemsRealtime(HashMap<String, String> map) throws Exception {
        try {
            List<?> list = getSqlSession().selectList(NS + "selectWatchlistItems", map);
            if (list == null || list.isEmpty()) {
                return list;
            }

            KisClient client = KisClientFactory.getClient();
            String asOf = java.time.LocalDateTime.now(KOREA_ZONE).toString().replace("T", " ").substring(0, 19);

            for (Object obj : list) {
                if (!(obj instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> row = (Map<String, Object>) obj;

                String code = nvl(str(row.get("STOCK_CODE")), "");
                String country = nvl(str(row.get("STOCK_COUNTRY_CODE")), "");
                String stockMarket = nvl(str(row.get("STOCK_MARKET")), "");
                String stockType = nvl(str(row.get("STOCK_TYPE")), "");
                String stockId = nvl(str(row.get("STOCK_ID")), "");

                Quote q = null;

                if ("KR".equalsIgnoreCase(country)) {
                    String indexCode = mapDomesticIndexCode(code, stockId, stockType, stockMarket);
                    if (indexCode != null) {
                        q = fetchDomesticIndexQuote(client, indexCode);
                    } else {
                        if (code.length() > 0) {
                            q = fetchDomesticStockQuote(client, code);
                        }
                    }
                } else {
                    if (code.length() > 0 && stockMarket.length() > 0) {
                        q = fetchOverseasStockQuote(client, stockMarket, code);
                    }
                }

                if (q != null) {
                    row.put("CUR_PRICE", q.price);
                    row.put("DIFF_PRICE", q.diff);
                    row.put("DIFF_RATE", q.rate);
                    row.put("DIR", q.dir);
                    row.put("AS_OF", asOf);
                } else {
                    row.put("CUR_PRICE", row.get("STOCK_CLOSE"));
                    row.put("DIFF_PRICE", "");
                    row.put("DIFF_RATE", "");
                    row.put("DIR", "FLAT");
                    row.put("AS_OF", asOf);
                }
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".selectWatchlistItemsRealtime() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, Object> selectMarketSummary(HashMap<String, String> map) throws Exception {
        try {
            Map<String, Object> out = new LinkedHashMap<String, Object>();
            String asOf = java.time.LocalDateTime.now(KOREA_ZONE).toString().replace("T", " ").substring(0, 19);
            out.put("AS_OF", asOf);

            KisClient client = KisClientFactory.getClient();

            out.put("KOSPI", fetchDomesticIndexSummaryString(client, "0001"));
            out.put("KOSDAQ", fetchDomesticIndexSummaryString(client, "1001"));

            out.put("USDKRW", fetchOverseasLike(client,
                    new String[] { "X" },
                    new String[] { "USDKRW", "USD/KRW", "USD" }));

            out.put("DJI", fetchOverseasLike(client,
                    new String[] { "N" },
                    new String[] { ".DJI", "DJI", "DOW" }));

            out.put("IXIC", fetchOverseasLike(client,
                    new String[] { "N" },
                    new String[] { "COMP", ".COMP", ".IXIC", "IXIC", "NASDAQ" }));

            out.put("SPX", fetchOverseasLike(client,
                    new String[] { "N" },
                    new String[] { ".INX", "SPX", "S&P500" }));

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".selectMarketSummary() : ", e.getLocalizedMessage());
        }
    }

    private Quote fetchDomesticStockQuote(KisClient client, String stockCode) {
        try {
            InquirePriceApi api = new InquirePriceApi();
            api.setFidInputIscd(stockCode);
            api.setFidCondMrktDivCode("UN");

            InquirePriceResult result = client.execute(api);

            String rtCd = safeGet(result, "rtCd", "getRtCd");
            if (!"0".equals(rtCd)) {
                return null;
            }

            Object output = safeGetObj(result, "output", "getOutput");
            if (output == null) {
                return null;
            }

            String price = safeGet(output, "stckPrpr", "getStckPrpr");
            String diff = safeGet(output, "prdyVrss", "getPrdyVrss");
            String rate = safeGet(output, "prdyCtrt", "getPrdyCtrt");
            String sign = safeGet(output, "prdyVrssSign", "getPrdyVrssSign");

            Quote q = new Quote();
            q.price = nvl(price, "");
            q.diff = nvl(diff, "");
            q.rate = nvl(rate, "");
            q.dir = toDir(diff, sign);

            return q;
        } catch (Exception e) {
            return null;
        }
    }

    private Quote fetchDomesticIndexQuote(KisClient client, String fidInputIscd) {
        try {
            InquireIndexPriceApi api = new InquireIndexPriceApi();
            api.setFidInputIscd(fidInputIscd);
            api.setFidCondMrktDivCode("U");

            InquireIndexPriceResult result = client.execute(api);

            String rtCd = safeGet(result, "rtCd", "getRtCd");
            if (!"0".equals(rtCd)) {
                return null;
            }

            Object output = safeGetObj(result, "output", "getOutput");
            if (output == null) {
                return null;
            }

            String price = safeGet(output, "bstpNmixPrpr", "getBstpNmixPrpr");
            String diff = safeGet(output, "bstpNmixPrdyVrss", "getBstpNmixPrdyVrss");
            String rate = safeGet(output, "bstpNmixPrdyCtrt", "getBstpNmixPrdyCtrt");

            Quote q = new Quote();
            q.price = nvl(price, "");
            q.diff = nvl(diff, "");
            q.rate = nvl(rate, "");
            q.dir = toDir(diff, "");

            return q;
        } catch (Exception e) {
            return null;
        }
    }

    private Quote fetchOverseasStockQuote(KisClient client, String excd, String symb) {
        try {
            PriceApi api = new PriceApi();
            api.setExcd(excd);
            api.setSymb(symb);

            PriceResult result = client.execute(api);

            String rtCd = safeGet(result, "rtCd", "getRtCd");
            if (!"0".equals(rtCd)) {
                return null;
            }

            Object output = safeGetObj(result, "output", "getOutput");
            if (output == null) {
                return null;
            }

            String price = safeGet(output, "last", "getLast");
            String diff = safeGet(output, "diff", "getDiff");
            String rate = safeGet(output, "rate", "getRate");
            String sign = safeGet(output, "sign", "getSign");

            Quote q = new Quote();
            q.price = nvl(price, "");
            q.diff = nvl(diff, "");
            q.rate = nvl(rate, "");
            q.dir = toDir(diff, sign);

            return q;
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchOverseasLike(KisClient client, String[] condCodes, String[] iscdCandidates) {
        try {
            if (client == null) {
                return "-";
            }
            if (condCodes == null || condCodes.length == 0) {
                return "-";
            }
            if (iscdCandidates == null || iscdCandidates.length == 0) {
                return "-";
            }

            LocalDate today = LocalDate.now(KOREA_ZONE);
            String to = today.format(DATE_YYYYMMDD);
            String from = today.minusDays(7).format(DATE_YYYYMMDD);

            for (String cond : condCodes) {
                String c = nvl(cond, "").trim();
                if (c.length() == 0) {
                    continue;
                }

                for (String iscd : iscdCandidates) {
                    String code = nvl(iscd, "").trim();
                    if (code.length() == 0) {
                        continue;
                    }

                    try {
                        InquireOverseasDailyChartPriceApi api = new InquireOverseasDailyChartPriceApi();
                        api.setFidCondMrktDivCode(c);
                        api.setFidInputIscd(code);
                        api.setFidInputDate1(from);
                        api.setFidInputDate2(to);
                        api.setFidPeriodDivCode("D");

                        InquireOverseasDailyChartPriceResult result = client.execute(api);
                        String rtCd = safeGet(result, "rtCd", "getRtCd");
                        if (!"0".equals(rtCd)) {
                            continue;
                        }

                        Object output1 = safeGetObj(result, "output1", "getOutput1");
                        if (output1 != null) {
                            String prpr = safeGet(output1, "ovrsNmixPrpr", "getOvrsNmixPrpr");
                            String diff = safeGet(output1, "ovrsNmixPrdyVrss", "getOvrsNmixPrdyVrss");
                            String rate = safeGet(output1, "prdyCtrt", "getPrdyCtrt");
                            String sign = safeGet(output1, "prdyVrssSign", "getPrdyVrssSign");

                            if (prpr != null && prpr.trim().length() > 0) {
                                if (isMissingOverseasValue(prpr, diff, rate)) {
                                    continue;
                                }
                                String p = nvl(prpr, "-");
                                String d = nvl(diff, "");
                                String r = applyRateSign(rate, sign, diff);

                                if (d.length() > 0 || r.length() > 0) {
                                    return p + " (" + d + " / " + r + "%)";
                                }
                                return p;
                            }
                        }

                        Object output2Obj = safeGetObj(result, "output2", "getOutput2");
                        if (output2Obj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> output2 = (List<Object>) output2Obj;
                            if (output2 != null && !output2.isEmpty()) {
                                Object last = output2.get(0);
                                String clos = safeGet(last, "clos", "getClos");
                                String diff = safeGet(last, "diff", "getDiff");
                                String rate = safeGet(last, "rate", "getRate");

                                if (clos != null && clos.trim().length() > 0) {
                                    if (isMissingOverseasValue(clos, diff, rate)) {
                                        continue;
                                    }
                                    String p = nvl(clos, "-");
                                    String d = nvl(diff, "");
                                    String r = applyRateSign(rate, "", diff);
                                    if (d.length() > 0 || r.length() > 0) {
                                        return p + " (" + d + " / " + r + "%)";
                                    }
                                    return p;
                                }
                            }
                        }
                    } catch (Exception ignoreOne) {
                    }
                }
            }

            return "-";
        } catch (Exception e) {
            return "-";
        }
    }

    private String mapDomesticIndexCode(String code, String stockId, String stockType, String stockMarket) {
        String c = nvl(code, "").toUpperCase();
        String id = nvl(stockId, "").toUpperCase();
        String t = nvl(stockType, "").toUpperCase();
        String m = nvl(stockMarket, "").toUpperCase();

        if ("INDEX".equals(t)) {
            if ("KOSPI".equals(c) || "KOSPI".equals(id) || "KOSPI".equals(m)) return "0001";
            if ("KOSDAQ".equals(c) || "KOSDAQ".equals(id) || "KOSDAQ".equals(m)) return "1001";
        }

        if ("KOSPI".equals(c)) return "0001";
        if ("KOSDAQ".equals(c)) return "1001";
        if ("KOSPI".equals(id)) return "0001";
        if ("KOSDAQ".equals(id)) return "1001";

        return null;
    }

    private String fetchDomesticIndexSummaryString(KisClient client, String fidInputIscd) {
        try {
            InquireIndexPriceApi api = new InquireIndexPriceApi();
            api.setFidInputIscd(fidInputIscd);
            api.setFidCondMrktDivCode("U");

            InquireIndexPriceResult result = client.execute(api);

            String rtCd = safeGet(result, "rtCd", "getRtCd");
            if (!"0".equals(rtCd)) {
                return "-";
            }

            Object output = safeGetObj(result, "output", "getOutput");
            if (output == null) {
                return "-";
            }

            String prpr = safeGet(output, "bstpNmixPrpr", "getBstpNmixPrpr");
            String diff = safeGet(output, "bstpNmixPrdyVrss", "getBstpNmixPrdyVrss");
            String rate = safeGet(output, "bstpNmixPrdyCtrt", "getBstpNmixPrdyCtrt");
            String sign = safeGet(output, "prdyVrssSign", "getPrdyVrssSign");

            if ((diff != null && diff.length() > 0) || (rate != null && rate.length() > 0)) {
                return nvl(prpr, "-") + " (" + nvl(diff, "") + " / " + applyRateSign(rate, sign, diff) + "%)";
            }
            return nvl(prpr, "-");
        } catch (Exception e) {
            return "-";
        }
    }

    private String safeGet(Object target, String fieldName, String getterName) {
        Object v = safeGetObj(target, fieldName, getterName);
        return v == null ? "" : String.valueOf(v);
    }

    private Object safeGetObj(Object target, String fieldName, String getterName) {
        if (target == null) {
            return null;
        }
        try {
            return target.getClass().getMethod(getterName).invoke(target);
        } catch (Exception ignore) {
        }
        try {
            java.lang.reflect.Field f = findField(target.getClass(), fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception ignore) {
        }
        return null;
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) throws Exception {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private String nvl(String v, String def) {
        if (v == null || v.trim().isEmpty()) {
            return def;
        }
        return v.trim();
    }

    private boolean isMissingOverseasValue(String price, String diff, String rate) {
        String p = nvl(price, "");
        if (p.isEmpty()) return true;
        if (!isZeroLike(p)) return false;

        String d = nvl(diff, "");
        String r = nvl(rate, "");
        return isZeroLike(d) && isZeroLike(r);
    }

    private boolean isZeroLike(String v) {
        if (v == null) return true;
        String s = v.trim();
        if (s.isEmpty()) return true;
        s = s.replace(",", "");
        if (s.startsWith("+")) s = s.substring(1);
        try {
            return Double.parseDouble(s) == 0.0;
        } catch (Exception e) {
            return false;
        }
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String normalizeKisSign(String sign) {
        String s = nvl(sign, "").trim();
        // KIS 부호코드: 1=상한, 2=상승, 3=보합, 4=하한, 5=하락
        if ("+".equals(s) || "1".equals(s) || "2".equals(s)) return "+";
        if ("-".equals(s) || "4".equals(s) || "5".equals(s)) return "-";
        if ("0".equals(s) || "3".equals(s)) return "0";
        return "";
    }

    /**
     * KIS API 등락률(절댓값)에 부호를 적용한다.
     * sign(prdyVrssSign) 우선, 없으면 diff의 음수 여부로 판단.
     */
    private String applyRateSign(String rate, String sign, String diff) {
        String r = nvl(rate, "");
        if (r.length() == 0 || r.startsWith("-") || r.startsWith("+")) {
            return r;
        }
        String normalized = normalizeKisSign(sign);
        if ("-".equals(normalized)) {
            return "-" + r;
        }
        if ("+".equals(normalized)) {
            return r;
        }
        // sign이 없으면 diff 부호로 판단
        try {
            String d = nvl(diff, "").replace(",", "").trim();
            if (d.length() > 0 && Double.parseDouble(d) < 0) {
                return "-" + r;
            }
        } catch (Exception ignore) {
        }
        return r;
    }

    private String toDir(String diff, String sign) {
        String normalized = normalizeKisSign(sign);
        if ("+".equals(normalized)) return "UP";
        if ("-".equals(normalized)) return "DOWN";
        if ("0".equals(normalized)) return "FLAT";

        try {
            String d = nvl(diff, "").replace(",", "").trim();
            if (d.length() == 0) return "FLAT";
            double dv = Double.parseDouble(d);
            if (dv > 0) return "UP";
            if (dv < 0) return "DOWN";
            return "FLAT";
        } catch (Exception e) {
            return "FLAT";
        }
    }

    private static class Quote {
        String price;
        String diff;
        String rate;
        String dir;
    }
}
