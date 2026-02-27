package com.scheduler.finance.module;

import java.util.ArrayList;
import java.util.List;

import com.scheduler.finance.vo.PositionSignalVo;
import com.scheduler.finance.vo.StockDataVo;

/**
 * WF-2-2c-BASE: 지수 전용 추세/이벤트 판정 모듈
 * - 기준 문서: _workflow/notes/31_MARKET_TREND_RULES.md
 * - 용어 표준: _workflow/notes/DD_DATA_DICTIONARY.md
 */
public class IndexTrendRuleModule {

    public static class Result {
        private String stateCode;      // UPTREND | SIDEWAYS | DOWNTREND | NO_BUY_ZONE
        private String stateLabelKo;   // 상승추세 | 횡보 | 하락추세 | 매수금지구간
        private String stateLabelEn;   // Uptrend | Sideways | Downtrend | No-Buy Zone
        private List<PositionSignalVo> signals;

        public Result() {
            this.signals = new ArrayList<PositionSignalVo>();
        }

        public String getStateCode() {
            return stateCode;
        }

        public void setStateCode(String stateCode) {
            this.stateCode = stateCode;
        }

        public String getStateLabelKo() {
            return stateLabelKo;
        }

        public void setStateLabelKo(String stateLabelKo) {
            this.stateLabelKo = stateLabelKo;
        }

        public String getStateLabelEn() {
            return stateLabelEn;
        }

        public void setStateLabelEn(String stateLabelEn) {
            this.stateLabelEn = stateLabelEn;
        }

        public List<PositionSignalVo> getSignals() {
            return signals;
        }

        public void setSignals(List<PositionSignalVo> signals) {
            this.signals = signals;
        }
    }

    /**
     * 일봉 지수 데이터 기준 이벤트 판정
     */
    public static Result evaluateDailyIndexTrend(List<StockDataVo> candles) {
        Result out = new Result();
        out.setStateCode("SIDEWAYS");
        out.setStateLabelKo("횡보");
        out.setStateLabelEn("Sideways");

        if (candles == null || candles.size() < 240) {
            out.getSignals().add(sig("IDX_INFO", now(candles), 0,
                "지수 추세 판정 데이터 부족(최소 240봉 필요)"));
            return out;
        }

        int i = candles.size() - 1;
        int p = Math.max(0, i - 1);

        double close = candles.get(i).getClose();
        double prevClose = candles.get(p).getClose();

        double ma5 = sma(candles, 5, i);
        double ma20 = sma(candles, 20, i);
        double ma60 = sma(candles, 60, i);
        double ma120 = sma(candles, 120, i);
        double ma240 = sma(candles, 240, i);

        double pMa5 = sma(candles, 5, p);
        double pMa20 = sma(candles, 20, p);
        double pMa60 = sma(candles, 60, p);
        double pMa120 = sma(candles, 120, p);
        double pMa240 = sma(candles, 240, p);

        boolean ordered = ma5 > ma20 && ma20 > ma60 && ma60 > ma120 && ma120 > ma240;
        boolean zigzagUp = isHigherHighHigherLow(candles, 20);
        boolean gapWiden = Math.abs(ma120 - ma240) > Math.abs(pMa120 - pMa240);

        // NBZ_240: 매수금지
        if (close < ma240 || (ma5 < ma240 && ma20 < ma240 && ma60 < ma240)) {
            out.setStateCode("NO_BUY_ZONE");
            out.setStateLabelKo("매수금지구간");
            out.setStateLabelEn("No-Buy Zone");
            out.getSignals().add(sig("IDX_NBZ_240", candles.get(i).getDate(), close,
                "NBZ_240 | 매수금지 | No-Buy Zone (<MA240)"));
        }

        // 데드크로스: 60 -> 240 하향 이탈
        if (pMa60 >= pMa240 && ma60 < ma240) {
            out.setStateCode("DOWNTREND");
            out.setStateLabelKo("하락추세");
            out.setStateLabelEn("Downtrend");
            out.getSignals().add(sig("IDX_DC_60_240", candles.get(i).getDate(), close,
                "DC_60_240 | 데드크로스 | Dead Cross 60/240"));
        }

        // 기본 우상향 판정
        if (ordered && zigzagUp && gapWiden) {
            if (!"NO_BUY_ZONE".equals(out.getStateCode())) {
                out.setStateCode("UPTREND");
                out.setStateLabelKo("상승추세");
                out.setStateLabelEn("Uptrend");
            }
            out.getSignals().add(sig("IDX_UPTREND_BASE", candles.get(i).getDate(), close,
                "정배열+지그재그 우상향+120/240 간격 확대"));
        }

        // 5/20 이벤트
        if (pMa5 >= pMa20 && ma5 < ma20) {
            out.getSignals().add(sig("IDX_M520_DW_W", candles.get(i).getDate(), close,
                "M520_DW_W | 5-20 하락관망 | MA5→20 Down Watch"));
        }
        if (isReverseN(candles, 5, 20, 8, 0.8)) {
            out.getSignals().add(sig("IDX_M5_RN_WARN", candles.get(i).getDate(), close,
                "M5_RN_WARN | 5일 역N 경고 | MA5 Reverse-N Warn"));
        }
        if (ma5 >= ma20 && pctDist(ma5, ma20) <= 1.0) {
            out.getSignals().add(sig("IDX_M520_GW", candles.get(i).getDate(), close,
                "M520_GW | 5-20 완만관망 | MA5-20 Gentle Watch"));
            if (prevClose > 0 && close > prevClose) {
                out.getSignals().add(sig("IDX_M520_GU", candles.get(i).getDate(), close,
                    "M520_GU | 5-20 완만상승 | MA5-20 Gentle Up"));
            }
        }
        if (ma5 < ma20 && ma20 < ma60) {
            out.getSignals().add(sig("IDX_M5260_DW", candles.get(i).getDate(), close,
                "M5260_DW | 5-20-60 하락 | MA5→20→60 Down"));
        }

        // 20/60 이벤트
        if (pMa20 >= pMa60 && ma20 < ma60) {
            out.getSignals().add(sig("IDX_M2060_WARN", candles.get(i).getDate(), close,
                "M2060_WARN | 20-60 경고 | MA20→60 Warn"));
        }
        if (isReverseN(candles, 20, 60, 12, 1.0)) {
            out.getSignals().add(sig("IDX_M20_RN_WARN", candles.get(i).getDate(), close,
                "M20_RN_WARN | 20일 역N 경고 | MA20 Reverse-N Warn"));
        }
        if (ma20 >= ma60 && pctDist(ma20, ma60) <= 1.0) {
            out.getSignals().add(sig("IDX_M2060_GW", candles.get(i).getDate(), close,
                "M2060_GW | 20-60 완만관망 | MA20-60 Gentle Watch"));
        }

        return out;
    }

    private static PositionSignalVo sig(String type, long time, double price, String msg) {
        return new PositionSignalVo(type, time, price, msg);
    }

    private static long now(List<StockDataVo> candles) {
        if (candles == null || candles.isEmpty()) return System.currentTimeMillis();
        return candles.get(candles.size() - 1).getDate();
    }

    private static double sma(List<StockDataVo> candles, int period, int idx) {
        if (candles == null || candles.isEmpty() || idx < 0) return 0;
        int start = Math.max(0, idx - period + 1);
        int cnt = idx - start + 1;
        if (cnt <= 0) return 0;
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += candles.get(i).getClose();
        return sum / cnt;
    }

    // 최근 n봉에서 고점/저점이 높아지는 단순 우상향 확인
    private static boolean isHigherHighHigherLow(List<StockDataVo> candles, int lookback) {
        if (candles == null || candles.size() < lookback + 2) return false;
        int end = candles.size() - 1;
        int start = Math.max(0, end - lookback + 1);

        double prevHigh = candles.get(start).getHigh();
        double prevLow = candles.get(start).getLow();
        int upCnt = 0;

        for (int i = start + 1; i <= end; i++) {
            double h = candles.get(i).getHigh();
            double l = candles.get(i).getLow();
            if (h >= prevHigh && l >= prevLow) upCnt++;
            prevHigh = h;
            prevLow = l;
        }
        return upCnt >= (lookback / 3);
    }

    // 단순 역N 판정: 하향이탈 후 재접근(근접) + 마지막 하락봉
    private static boolean isReverseN(List<StockDataVo> candles, int shortMa, int baseMa, int lookback, double nearPct) {
        if (candles == null || candles.size() < Math.max(baseMa + 2, lookback + 2)) return false;
        int end = candles.size() - 1;
        int start = Math.max(1, end - lookback + 1);

        boolean hadCrossDown = false;
        boolean cameNear = false;

        for (int i = start; i <= end; i++) {
            double s = sma(candles, shortMa, i);
            double b = sma(candles, baseMa, i);
            double ps = sma(candles, shortMa, i - 1);
            double pb = sma(candles, baseMa, i - 1);

            if (ps >= pb && s < b) hadCrossDown = true;
            if (hadCrossDown && pctDist(s, b) <= nearPct) cameNear = true;
        }

        double c = candles.get(end).getClose();
        double pc = candles.get(end - 1).getClose();
        boolean turnDown = c < pc;

        return hadCrossDown && cameNear && turnDown;
    }

    private static double pctDist(double a, double b) {
        if (b == 0) return 999;
        return Math.abs((a - b) / b * 100.0);
    }
}
