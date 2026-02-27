package com.scheduler.finance.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.scheduler.finance.vo.PositionPlanVo;
import com.scheduler.finance.vo.PositionSignalVo;
import com.scheduler.finance.vo.StockDataVo;

/**
 * 포지션 플랜 평가 엔진 (MVP)
 *
 * 현재 범위:
 * - 포지션이 없는 상태에서 (ENTRY_READY 여부)와 진입/손절/TP1(+1R) 제안값을 계산
 * - 일봉으로 큰 추세 필터(예: close > MA60)
 * - 30분봉으로 눌림 반등 간단 조건(예: MA20 상향 재진입)
 */
public class PositionRuleEngine {
    // WF-2-2: 차트 분석 모듈(포지션 플랜/시그널 평가)

    public static class Result {
        private final PositionPlanVo plan;
        private final List<PositionSignalVo> signals;

        public Result(PositionPlanVo plan, List<PositionSignalVo> signals) {
            this.plan = plan;
            this.signals = signals;
        }

        public PositionPlanVo getPlan() {
            return plan;
        }

        public List<PositionSignalVo> getSignals() {
            return signals;
        }
    }

    public static Result evaluatePreEntry(List<StockDataVo> dailyCandles, List<StockDataVo> m30Candles) {
        PositionPlanVo plan = new PositionPlanVo();
        List<PositionSignalVo> signals = new ArrayList<PositionSignalVo>();

        // defaults
        plan.setState("WATCH");
        plan.setPartialTakeProfitRatio(0.5);

        if (dailyCandles == null || dailyCandles.size() < 60) {
            signals.add(new PositionSignalVo("INFO", now(dailyCandles), 0, "일봉 데이터가 부족합니다(최소 60개 권장)."));
            return new Result(plan, signals);
        }

        StockDataVo dailyLast = dailyCandles.get(dailyCandles.size() - 1);
        double dailyClose = dailyLast.getClose();
        double dailyMa60 = smaClose(dailyCandles, 60, dailyCandles.size() - 1);

        boolean dailyTrendOk = dailyClose > dailyMa60;
        if (!dailyTrendOk) {
            signals.add(new PositionSignalVo("INFO", dailyLast.getDate(), dailyClose, "일봉 추세 필터 미충족: close <= MA60"));
            return new Result(plan, signals);
        }

        // 일봉 추세 OK → 관심→진입 준비 후보
        plan.setState("ENTRY_READY");
        signals.add(new PositionSignalVo("DAILY_TREND_OK", dailyLast.getDate(), dailyClose, "일봉 close > MA60 (진입 검토 가능)"));

        // 30분봉 타이밍(2안): 돌파형 / 눌림형
        if (m30Candles == null || m30Candles.size() < 30) {
            signals.add(new PositionSignalVo("INFO", dailyLast.getDate(), dailyClose, "30분봉 데이터가 부족합니다(최소 30개 권장)."));
            plan.setSuggestedEntryPrice(roundPrice(dailyClose));
            return new Result(plan, signals);
        }

        int lastIdx = m30Candles.size() - 1;
        StockDataVo m30Last = m30Candles.get(lastIdx);
        double m30Close = m30Last.getClose();
        double m30Ma20 = smaClose(m30Candles, 20, lastIdx);
        double m30Ma60 = smaClose(m30Candles, 60, lastIdx);
        double prevClose = m30Candles.get(Math.max(0, lastIdx - 1)).getClose();
        double prevMa20 = smaClose(m30Candles, 20, Math.max(0, lastIdx - 1));

        // A안) 눌림형: MA20 부근(±0.6%) + MA60 상단 유지
        double dist20Pct = (m30Ma20 == 0 ? 999 : ((m30Close - m30Ma20) / m30Ma20 * 100.0));
        boolean pullbackZone = Math.abs(dist20Pct) <= 0.6;
        boolean pullbackTrendOk = (m30Close >= m30Ma60);
        boolean pullbackReady = pullbackZone && pullbackTrendOk;
        double pullbackEntry = m30Ma20;

        // B안) 돌파형: 최근 20봉 고점 상향 돌파
        double breakoutPivot = recentHigh(m30Candles, 20);
        boolean breakoutReady = (breakoutPivot > 0) && (m30Close >= breakoutPivot);
        double breakoutEntry = breakoutPivot > 0 ? breakoutPivot : m30Close;

        if (pullbackReady) {
            signals.add(new PositionSignalVo("TRIGGER_PULLBACK_READY", m30Last.getDate(), m30Close,
                    "진입 트리거(눌림형) READY: MA20 근접(" + roundPrice(dist20Pct) + "%) + MA60 상단 유지"));
        } else {
            signals.add(new PositionSignalVo("TRIGGER_PULLBACK_WAIT", m30Last.getDate(), m30Close,
                    "진입 트리거(눌림형) WAIT: MA20 이격 " + roundPrice(dist20Pct) + "%"));
        }

        if (breakoutReady) {
            signals.add(new PositionSignalVo("TRIGGER_BREAKOUT_READY", m30Last.getDate(), m30Close,
                    "진입 트리거(돌파형) READY: 최근 20봉 고점 " + roundPrice(breakoutPivot) + " 돌파"));
        } else {
            signals.add(new PositionSignalVo("TRIGGER_BREAKOUT_WAIT", m30Last.getDate(), m30Close,
                    "진입 트리거(돌파형) WAIT: 최근 20봉 고점 " + roundPrice(breakoutPivot) + " 미돌파"));
        }

        // 기존 눌림반등 시그널은 유지(호환)
        boolean pullbackRebound = (prevClose < prevMa20) && (m30Close >= m30Ma20);
        if (pullbackRebound) {
            signals.add(new PositionSignalVo("PULLBACK_REBOUND_30M", m30Last.getDate(), m30Close, "30분봉 MA20 하단→상단 재진입(눌림 반등)"));
        } else {
            signals.add(new PositionSignalVo("INFO", m30Last.getDate(), m30Close, "30분봉 타이밍은 아직 보수적으로 대기(눌림 반등 미확인)"));
        }

        // 기본 제안가: 눌림 READY면 눌림형, 아니면 돌파형 기준
        double entry = pullbackReady ? pullbackEntry : breakoutEntry;

        double stop = suggestSwingLowStop(m30Candles, 10, 0.001); // 최근 10봉 저점 -0.1%
        if (stop <= 0 || stop >= entry) {
            // fallback: 최근 20봉 최저가
            stop = suggestSwingLowStop(m30Candles, 20, 0.001);
        }

        plan.setSuggestedEntryPrice(roundPrice(entry));
        plan.setInitialStopPrice(roundPrice(stop));

        double r = Math.max(0, entry - stop);
        plan.setRiskR(roundPrice(r));
        plan.setTakeProfit1Price(roundPrice(entry + r));

        if (r <= 0) {
            signals.add(new PositionSignalVo("WARNING", m30Last.getDate(), entry, "R(초기 위험폭) 계산 실패: stop >= entry. 데이터/룰을 확인하세요."));
        } else {
            signals.add(new PositionSignalVo("PLAN", m30Last.getDate(), entry, "제안: +1R 도달 시 50% 익절 + 스탑을 진입가로 이동"));
        }

        return new Result(plan, signals);
    }

    private static long now(List<StockDataVo> candles) {
        if (candles == null || candles.isEmpty()) return System.currentTimeMillis();
        return candles.get(candles.size() - 1).getDate();
    }

    private static double smaClose(List<StockDataVo> candles, int period, int idx) {
        if (candles == null || candles.isEmpty()) return 0;
        if (idx < 0) return 0;
        int start = Math.max(0, idx - period + 1);
        int count = idx - start + 1;
        if (count <= 0) return 0;
        double sum = 0;
        for (int i = start; i <= idx; i++) {
            sum += candles.get(i).getClose();
        }
        return sum / count;
    }

    private static double suggestSwingLowStop(List<StockDataVo> candles, int lookback, double bufferRatio) {
        if (candles == null || candles.isEmpty()) return 0;
        int n = candles.size();
        int start = Math.max(0, n - lookback);
        List<Double> lows = new ArrayList<Double>();
        for (int i = start; i < n; i++) {
            lows.add(candles.get(i).getLow());
        }
        if (lows.isEmpty()) return 0;
        double minLow = Collections.min(lows);
        return minLow * (1.0 - bufferRatio);
    }

    private static double recentHigh(List<StockDataVo> candles, int lookback) {
        if (candles == null || candles.isEmpty()) return 0;
        int n = candles.size();
        int start = Math.max(0, n - lookback);
        double max = Double.MIN_VALUE;
        for (int i = start; i < n; i++) {
            double h = candles.get(i).getHigh();
            if (h > max) max = h;
        }
        return (max == Double.MIN_VALUE ? 0 : max);
    }

    private static double roundPrice(double v) {
        // MVP: 소수점 2자리
        return Math.round(v * 100.0) / 100.0;
    }
}
