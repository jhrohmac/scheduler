package com.scheduler.stock.indicator.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * RSI(14) 과매수 필터
 *  사용자 가변 파라미터(period) 때문에 사전계산이 어렵다 → 실시간 Java 계산
 *
 *  파라미터:
 *   - period: 계산 기간 (5~30, 기본 14)
 *   - maxRsi: 최대 RSI (0~100, 기본 70)
 *
 *  알고리즘 (Wilder's smoothing):
 *   - 일별 변동: gain = max(close - prevClose, 0), loss = max(prevClose - close, 0)
 *   - 평균 gain/loss = (이전평균 × (period-1) + 당일값) / period
 *   - RS = avgGain / avgLoss
 *   - RSI = 100 - (100 / (1 + RS))
 */
public class RsiIndicator implements Indicator {

    public String getId() { return "RSI"; }
    public String getDisplayName() { return "RSI 과매수 필터"; }
    public String getCategory() { return "모멘텀"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p1 = new ParameterDef("period", "기간", "number", 14);
        p1.setMinValue(5).setMaxValue(30).setStep(1).setUnit("일");
        ParameterDef p2 = new ParameterDef("maxRsi", "최대 RSI", "range", 70);
        p2.setMinValue(0).setMaxValue(100).setStep(5);
        return Arrays.asList(p1, p2);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        return null; // 실시간 계산 → Java 단계에서 evaluate
    }

    public boolean evaluate(IndicatorContext ctx, Map<String, Object> params) {
        int period = intParam(params, "period", 14);
        double maxRsi = numParam(params, "maxRsi", 70.0);

        Double rsi = calculateRsi(ctx.getPriceList(), period);
        if (rsi == null) {
            return false; // 계산 불가 → 탈락
        }
        return rsi.doubleValue() <= maxRsi;
    }

    public boolean requiresPriceList() { return true; }

    /** Wilder RSI 계산 — 외부에서도 사용 가능하도록 public */
    public static Double calculateRsi(List<DlyPriceDto> priceList, int period) {
        if (priceList == null || priceList.size() < period + 1) {
            return null;
        }
        double avgGain = 0d;
        double avgLoss = 0d;

        // 초기 평균
        for (int i = 1; i <= period; i++) {
            Double cur = priceList.get(i).getAdjClosePrice();
            Double prev = priceList.get(i - 1).getAdjClosePrice();
            if (cur == null || prev == null) return null;
            double diff = cur.doubleValue() - prev.doubleValue();
            if (diff > 0) avgGain += diff; else avgLoss += -diff;
        }
        avgGain /= period;
        avgLoss /= period;

        // 이후 Wilder smoothing
        for (int i = period + 1; i < priceList.size(); i++) {
            Double cur = priceList.get(i).getAdjClosePrice();
            Double prev = priceList.get(i - 1).getAdjClosePrice();
            if (cur == null || prev == null) continue;
            double diff = cur.doubleValue() - prev.doubleValue();
            double gain = diff > 0 ? diff : 0d;
            double loss = diff < 0 ? -diff : 0d;
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;
        }

        if (avgLoss == 0d) return Double.valueOf(100d);
        double rs = avgGain / avgLoss;
        return Double.valueOf(100d - (100d / (1d + rs)));
    }

    private static int intParam(Map<String, Object> params, String key, int def) {
        Object v = params == null ? null : params.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    private static double numParam(Map<String, Object> params, String key, double def) {
        Object v = params == null ? null : params.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
}
