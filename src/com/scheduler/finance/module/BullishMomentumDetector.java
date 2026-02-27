package com.scheduler.finance.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.finance.vo.BullishMomentumSignal;
import com.scheduler.finance.vo.StockDataVo;

/**
 * 이동평균선 정배열 + 장대양봉/연속양봉 패턴 감지 모듈
 *
 * 패턴 설명:
 * - 완전 정배열: close > MA5 > MA20 > MA60 > MA120 > MA240
 * - 장대양봉: 실체(close-open)가 최근 평균 실체의 2배 이상
 * - 연속양봉: 3일 이상 연속 양봉(close > open)
 *
 * 이 패턴은 상승 추세 초입(Golden Cross) + 강한 매수 추진력을 나타낸다.
 */
public class BullishMomentumDetector {
    // WF-2-2: 차트 분석 모듈(모멘텀 신호)

    private static final int[] DEFAULT_PERIODS = {5, 20, 60, 120, 240};
    private static final int LOOKBACK_PERIOD = 20;  // 평균 실체 계산 기간

    /**
     * 완전 정배열 + 장대양봉/연속양봉 패턴 감지
     *
     * @param candles 캔들 데이터 리스트 (시간순 정렬)
     * @param stockCode 종목 코드
     * @param stockName 종목명 (선택)
     * @param consecutiveBullDays 연속양봉 일수 기준 (기본 3일)
     * @param longCandleRatio 장대양봉 배율 기준 (기본 2.0배)
     * @return 감지된 신호 리스트
     */
    public static List<BullishMomentumSignal> detectBullishMomentum(
            List<StockDataVo> candles,
            String stockCode,
            String stockName,
            int consecutiveBullDays,
            double longCandleRatio) {

        List<BullishMomentumSignal> signals = new ArrayList<>();

        if (candles == null || candles.isEmpty()) {
            return signals;
        }

        int size = candles.size();

        // MA 계산
        double[] ma5 = calculateSMA(candles, 5);
        double[] ma20 = calculateSMA(candles, 20);
        double[] ma60 = calculateSMA(candles, 60);
        double[] ma120 = calculateSMA(candles, 120);
        double[] ma240 = calculateSMA(candles, 240);

        // 완전 정배열 지점부터 시작
        int startIdx = 240;  // ma240을 위해 최소 240개 필요

        for (int i = startIdx; i < size; i++) {
            StockDataVo current = candles.get(i);
            double closePrice = current.getClose();

            // 1. 완전 정배열 체크: close > ma5 > ma20 > ma60 > ma120 > ma240
            if (!isCompleteAlignment(closePrice, ma5[i], ma20[i], ma60[i], ma120[i], ma240[i])) {
                continue;
            }

            // 2. 장대양봉 체크
            double avgBody = calculateAverageBody(candles, i);
            double currentBody = Math.abs(current.getClose() - current.getOpen());
            double bodyRatio = avgBody > 0 ? currentBody / avgBody : 0;

            if (bodyRatio >= longCandleRatio && current.getClose() > current.getOpen()) {
                BullishMomentumSignal signal = createSignal(
                        current, stockCode, stockName, "LONG_BULL_BODY",
                        ma5[i], ma20[i], ma60[i], ma120[i], ma240[i],
                        bodyRatio, 1
                );
                signals.add(signal);
            }

            // 3. 연속양봉 체크
            int consecutiveDays = countConsecutiveBullish(candles, i);
            if (consecutiveDays >= consecutiveBullDays) {
                BullishMomentumSignal signal = createSignal(
                        current, stockCode, stockName, "CONSECUTIVE_BULL",
                        ma5[i], ma20[i], ma60[i], ma120[i], ma240[i],
                        0, consecutiveDays
                );
                signals.add(signal);
            }
        }

        return signals;
    }

    /**
     * Chart List 형식(Highcharts 호환)에서 신호 감지
     *
     * @param chartList [time, open, high, low, close, volume] 구조의 리스트
     * @param stockCode 종목 코드
     * @param stockName 종목명
     * @param consecutiveBullDays 연속양봉 일수
     * @param longCandleRatio 장대양봉 배율
     * @return 감지된 신호 리스트
     */
    @SuppressWarnings("rawtypes")
    public static List<BullishMomentumSignal> detectBullishMomentumFromChartList(
            List<?> chartList,
            String stockCode,
            String stockName,
            int consecutiveBullDays,
            double longCandleRatio) {

        if (chartList == null || chartList.isEmpty()) {
            return new ArrayList<>();
        }

        List<StockDataVo> candles = new ArrayList<>();
        for (Object item : chartList) {
            if (!(item instanceof List)) {
                continue;
            }
            List row = (List) item;
            if (row.size() < 5) {
                continue;
            }

            try {
                long time = ((Number) row.get(0)).longValue();
                double open = ((Number) row.get(1)).doubleValue();
                double high = ((Number) row.get(2)).doubleValue();
                double low = ((Number) row.get(3)).doubleValue();
                double close = ((Number) row.get(4)).doubleValue();
                double volume = row.size() > 5 ? ((Number) row.get(5)).doubleValue() : 0;

                StockDataVo vo = new StockDataVo(time, open, high, low, close, volume);
                candles.add(vo);
            } catch (Exception e) {
                // Skip invalid row
                continue;
            }
        }

        return detectBullishMomentum(candles, stockCode, stockName, consecutiveBullDays, longCandleRatio);
    }

    /**
     * 완전 정배열 확인: close > ma5 > ma20 > ma60 > ma120 > ma240
     */
    private static boolean isCompleteAlignment(double close, double ma5, double ma20,
                                                double ma60, double ma120, double ma240) {
        if (Double.isNaN(ma5) || Double.isNaN(ma20) || Double.isNaN(ma60) ||
                Double.isNaN(ma120) || Double.isNaN(ma240)) {
            return false;
        }
        return close > ma5 && ma5 > ma20 && ma20 > ma60 && ma60 > ma120 && ma120 > ma240;
    }

    /**
     * 평균 실체 크기 계산 (최근 LOOKBACK_PERIOD일)
     */
    private static double calculateAverageBody(List<StockDataVo> candles, int endIdx) {
        int lookback = Math.min(LOOKBACK_PERIOD, endIdx + 1);
        if (lookback <= 0) {
            return 0;
        }

        double sum = 0;
        for (int i = endIdx - lookback + 1; i <= endIdx; i++) {
            if (i >= 0 && i < candles.size()) {
                StockDataVo candle = candles.get(i);
                sum += Math.abs(candle.getClose() - candle.getOpen());
            }
        }
        return sum / lookback;
    }

    /**
     * 현재 지점부터 거슬러 올라가며 연속 양봉 일수 카운트
     */
    private static int countConsecutiveBullish(List<StockDataVo> candles, int endIdx) {
        int count = 0;
        for (int i = endIdx; i >= 0; i--) {
            StockDataVo candle = candles.get(i);
            if (candle.getClose() > candle.getOpen()) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 신호 객체 생성
     */
    private static BullishMomentumSignal createSignal(
            StockDataVo current,
            String stockCode,
            String stockName,
            String signalType,
            double ma5,
            double ma20,
            double ma60,
            double ma120,
            double ma240,
            double bodyRatio,
            int consecutiveDays) {

        BullishMomentumSignal signal = new BullishMomentumSignal();
        signal.setSignalTime(current.getDate());
        signal.setStockCode(stockCode);
        signal.setStockName(stockName);
        signal.setClosePrice(current.getClose());
        signal.setSignalType(signalType);
        signal.setOpen(current.getOpen());
        signal.setHigh(current.getHigh());
        signal.setLow(current.getLow());
        signal.setVolume(current.getVolume());
        signal.setMa5(ma5);
        signal.setMa20(ma20);
        signal.setMa60(ma60);
        signal.setMa120(ma120);
        signal.setMa240(ma240);
        signal.setBodyRatio(bodyRatio);
        signal.setConsecutiveDays(consecutiveDays);

        return signal;
    }

    /**
     * 단순 이동평균(SMA) 계산
     */
    private static double[] calculateSMA(List<StockDataVo> candles, int period) {
        int n = candles.size();
        double[] sma = new double[n];
        Arrays.fill(sma, Double.NaN);

        if (period <= 0 || n < period) {
            return sma;
        }

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += candles.get(i).getClose();
            if (i >= period) {
                sum -= candles.get(i - period).getClose();
            }
            if (i >= period - 1) {
                sma[i] = sum / period;
            }
        }
        return sma;
    }

    /**
     * 여러 종목 차트 데이터에서 일괄 신호 감지
     */
    public static List<BullishMomentumSignal> detectBullishMomentumBatch(
            Map<String, List<StockDataVo>> candlesByStock,
            Map<String, String> stockNames,
            int consecutiveBullDays,
            double longCandleRatio) {

        List<BullishMomentumSignal> allSignals = new ArrayList<>();

        for (Map.Entry<String, List<StockDataVo>> entry : candlesByStock.entrySet()) {
            String stockCode = entry.getKey();
            List<StockDataVo> candles = entry.getValue();
            String stockName = stockNames != null ? stockNames.get(stockCode) : null;

            List<BullishMomentumSignal> signals = detectBullishMomentum(
                    candles, stockCode, stockName, consecutiveBullDays, longCandleRatio
            );
            allSignals.addAll(signals);
        }

        return allSignals;
    }
}
