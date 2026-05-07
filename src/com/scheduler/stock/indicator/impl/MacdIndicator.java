package com.scheduler.stock.indicator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * MACD 골든크로스 필터
 *  조건: 최근 N일 내 MACD 라인이 Signal 라인을 상향 돌파(GOLDEN CROSS)
 *  → 일봉 EMA 계산 필요 → 실시간 Java 계산
 *
 *  파라미터:
 *   - fast: 단기 EMA 기간 (기본 12)
 *   - slow: 장기 EMA 기간 (기본 26)
 *   - signal: Signal EMA 기간 (기본 9)
 *   - lookbackDays: 골든크로스 탐지 구간 (기본 5일)
 */
public class MacdIndicator implements Indicator {

    public String getId() { return "MACD"; }
    public String getDisplayName() { return "MACD 골든크로스"; }
    public String getCategory() { return "모멘텀"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p1 = new ParameterDef("fast", "Fast EMA", "number", 12);
        p1.setMinValue(2).setMaxValue(50).setStep(1).setUnit("일");
        ParameterDef p2 = new ParameterDef("slow", "Slow EMA", "number", 26);
        p2.setMinValue(5).setMaxValue(100).setStep(1).setUnit("일");
        ParameterDef p3 = new ParameterDef("signal", "Signal", "number", 9);
        p3.setMinValue(2).setMaxValue(30).setStep(1).setUnit("일");
        ParameterDef p4 = new ParameterDef("lookbackDays", "탐지 구간", "number", 5);
        p4.setMinValue(1).setMaxValue(20).setStep(1).setUnit("일");
        return Arrays.asList(p1, p2, p3, p4);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        return null;
    }

    public boolean evaluate(IndicatorContext ctx, Map<String, Object> params) {
        int fast = intParam(params, "fast", 12);
        int slow = intParam(params, "slow", 26);
        int signal = intParam(params, "signal", 9);
        int lookback = intParam(params, "lookbackDays", 5);

        return hasRecentGoldenCross(ctx.getPriceList(), fast, slow, signal, lookback);
    }

    public boolean requiresPriceList() { return true; }

    public static boolean hasRecentGoldenCross(List<DlyPriceDto> priceList, int fast, int slow, int signal, int lookback) {
        if (priceList == null || priceList.size() < slow + signal + lookback + 1) {
            return false;
        }
        List<Double> closeList = new ArrayList<Double>();
        for (DlyPriceDto p : priceList) {
            if (p == null || p.getAdjClosePrice() == null) return false;
            closeList.add(p.getAdjClosePrice());
        }
        List<Double> emaFast = ema(closeList, fast);
        List<Double> emaSlow = ema(closeList, slow);
        List<Double> macd = new ArrayList<Double>();
        for (int i = 0; i < closeList.size(); i++) {
            Double f = emaFast.get(i);
            Double s = emaSlow.get(i);
            macd.add(f != null && s != null ? Double.valueOf(f - s) : null);
        }
        // signal EMA on macd values (skip leading nulls)
        List<Double> macdNonNull = new ArrayList<Double>();
        int firstValidIdx = -1;
        for (int i = 0; i < macd.size(); i++) {
            if (macd.get(i) != null) {
                if (firstValidIdx < 0) firstValidIdx = i;
                macdNonNull.add(macd.get(i));
            }
        }
        if (macdNonNull.size() < signal + lookback) return false;
        List<Double> signalEma = ema(macdNonNull, signal);

        // 최근 lookback 구간에서 macd > signal AND 직전엔 macd <= signal 이면 골든크로스
        int n = macdNonNull.size();
        for (int i = Math.max(signal, n - lookback); i < n; i++) {
            if (i < 1) continue;
            Double cur = macdNonNull.get(i);
            Double sig = signalEma.get(i);
            Double prevCur = macdNonNull.get(i - 1);
            Double prevSig = signalEma.get(i - 1);
            if (cur == null || sig == null || prevCur == null || prevSig == null) continue;
            if (prevCur <= prevSig && cur > sig) {
                return true;
            }
        }
        return false;
    }

    /** 단순 EMA */
    private static List<Double> ema(List<Double> values, int period) {
        List<Double> result = new ArrayList<Double>();
        if (values == null || values.size() < period) {
            for (int i = 0; i < (values == null ? 0 : values.size()); i++) result.add(null);
            return result;
        }
        double multiplier = 2.0 / (period + 1);
        // SMA 시작
        double sma = 0d;
        for (int i = 0; i < period; i++) sma += values.get(i);
        sma /= period;
        for (int i = 0; i < period - 1; i++) result.add(null);
        result.add(Double.valueOf(sma));
        double prev = sma;
        for (int i = period; i < values.size(); i++) {
            double v = values.get(i);
            double cur = (v - prev) * multiplier + prev;
            result.add(Double.valueOf(cur));
            prev = cur;
        }
        return result;
    }

    private static int intParam(Map<String, Object> params, String key, int def) {
        Object v = params == null ? null : params.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }
}
