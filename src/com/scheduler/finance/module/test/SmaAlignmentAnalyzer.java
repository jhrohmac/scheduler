package com.scheduler.finance.module.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 이동평균 정배열 및 이후 N자 5일선 + 양봉 패턴 탐지 유틸리티.
 *
 * - 입력: OHLC 데이터 (time, open, high, low, close)
 *   * time 은 Highcharts 기준 millisecond(UTC) long 값으로 가정.
 *
 * 기능
 *  1) findSmaAlignmentStartsFromOhlc : 5/20/60/120/240 정배열 "시작 시점" 탐지
 *  2) findNPatternSignalsFromOhlc    : 정배열 이후, 5일선 N자 + 양봉 3일 또는 장대양봉 시그널 탐지
 */
public class SmaAlignmentAnalyzer {

    /**
     * OHLC 한 캔들을 표현하는 VO.
     */
    public static class Candle {
        private final long time;
        private final double open;
        private final double high;
        private final double low;
        private final double close;

        public Candle(long time, double open, double high, double low, double close) {
            this.time = time;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        public long getTime() {
            return time;
        }

        public double getOpen() {
            return open;
        }

        public double getHigh() {
            return high;
        }

        public double getLow() {
            return low;
        }

        public double getClose() {
            return close;
        }
    }

    /**
     * 정배열 시작 지점을 표현하는 VO.
     */
    public static class AlignmentPoint {
        private final int index;
        private final long time;
        private final double close;

        public AlignmentPoint(int index, long time, double close) {
            this.index = index;
            this.time = time;
            this.close = close;
        }

        public int getIndex() {
            return index;
        }

        public long getTime() {
            return time;
        }

        public double getClose() {
            return close;
        }

        @Override
        public String toString() {
            return "AlignmentPoint{" +
                    "index=" + index +
                    ", time=" + time +
                    ", close=" + close +
                    '}';
        }
    }

    /**
     * 정배열 이후 N자 5일선 + 양봉 패턴 시그널.
     *
     * type:
     *   THREE_BULLS : 양봉 3일 연속
     *   LONG_BULL   : 장대 양봉
     */
    public static class NPatternSignal {
        public enum SignalType {
            THREE_BULLS,
            LONG_BULL
        }

        private final int index;
        private final long time;
        private final double close;
        private final SignalType type;

        public NPatternSignal(int index, long time, double close, SignalType type) {
            this.index = index;
            this.time = time;
            this.close = close;
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public long getTime() {
            return time;
        }

        public double getClose() {
            return close;
        }

        public SignalType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "NPatternSignal{" +
                    "index=" + index +
                    ", time=" + time +
                    ", close=" + close +
                    ", type=" + type +
                    '}';
        }
    }

    /**
     * 외부에서 사용하기 좋은 헬퍼:
     *   OHLC(Object[] 또는 double[]) 리스트를 받아 정배열 시작 지점을 탐지.
     *
     * ohlcList:
     *   각 원소는 [0]=time(long), [1]=open, [2]=high, [3]=low, [4]=close
     */
    public static List<AlignmentPoint> findSmaAlignmentStartsFromOhlc(List<?> ohlcList,
                                                                      int minSlopeDays) {
        if (ohlcList == null || ohlcList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Candle> candles = convertToCandles(ohlcList);
        return findSmaAlignmentStarts(candles, minSlopeDays);
    }

    /**
     * 정배열 이후, 5일선 N자 + 양봉 3일 또는 장대양봉 시그널 탐지(OHLC 입력 버전).
     *
     * @param ohlcList            Highcharts용 OHLC 리스트
     * @param minSlopeDays        정배열 판정 시 각 MA가 우상향해야 하는 최소 일수
     * @param consecutiveBullDays 양봉 연속 일수 (기본 3일이면 3)
     * @param longBullBodyRatio   장대양봉 판정 기준 (평균 몸통 대비 배수, 예: 1.8)
     */
    public static List<NPatternSignal> findNPatternSignalsFromOhlc(List<?> ohlcList,
                                                                   int minSlopeDays,
                                                                   int consecutiveBullDays,
                                                                   double longBullBodyRatio) {
        if (ohlcList == null || ohlcList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Candle> candles = convertToCandles(ohlcList);
        return findNPatternSignals(candles, minSlopeDays, consecutiveBullDays, longBullBodyRatio);
    }

    /**
     * 내부: Object[] / double[] 리스트를 Candle 리스트로 변환.
     */
    private static List<Candle> convertToCandles(List<?> ohlcList) {
        List<Candle> candles = new ArrayList<>(ohlcList.size());

        for (Object row : ohlcList) {
            if (row instanceof Object[]) {
                Object[] arr = (Object[]) row;
                long time = toLong(arr[0]);
                double open = toDouble(arr[1]);
                double high = toDouble(arr[2]);
                double low = toDouble(arr[3]);
                double close = toDouble(arr[4]);
                candles.add(new Candle(time, open, high, low, close));
            } else if (row instanceof double[]) {
                double[] arr = (double[]) row;
                long time = (long) arr[0];
                double open = arr[1];
                double high = arr[2];
                double low = arr[3];
                double close = arr[4];
                candles.add(new Candle(time, open, high, low, close));
            } else {
                throw new IllegalArgumentException("지원하지 않는 OHLC 타입: " + row.getClass());
            }
        }

        return candles;
    }

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.parseLong(String.valueOf(v));
    }

    private static double toDouble(Object v) {
        if (v == null) return Double.NaN;
        if (v instanceof Number) return ((Number) v).doubleValue();
        return Double.parseDouble(String.valueOf(v));
    }

    /**
     * 핵심 1: 정배열 시작 지점 찾기.
     */
    public static List<AlignmentPoint> findSmaAlignmentStarts(List<Candle> candles,
                                                              int minSlopeDays) {

        if (candles == null || candles.isEmpty()) {
            return Collections.emptyList();
        }

        int n = candles.size();

        int p5 = 5;
        int p20 = 20;
        int p60 = 60;
        int p120 = 120;
        int p240 = 240;

        int maxPeriod = Math.max(p240,
                Math.max(p120, Math.max(p60, Math.max(p20, p5))));

        double[] ma5 = calcSma(candles, p5);
        double[] ma20 = calcSma(candles, p20);
        double[] ma60 = calcSma(candles, p60);
        double[] ma120 = calcSma(candles, p120);
        double[] ma240 = calcSma(candles, p240);

        List<AlignmentPoint> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (i < maxPeriod - 1) {
                continue;
            }

            boolean isNowAligned =
                    isPerfectAscendingOrder(i, ma5, ma20, ma60, ma120, ma240) &&
                    isAllMaUpSlope(i, minSlopeDays, ma5, ma20, ma60, ma120, ma240);

            if (!isNowAligned) {
                continue;
            }

            boolean prevAligned = false;
            if (i > 0) {
                prevAligned =
                        isPerfectAscendingOrder(i - 1, ma5, ma20, ma60, ma120, ma240) &&
                        isAllMaUpSlope(i - 1, minSlopeDays, ma5, ma20, ma60, ma120, ma240);
            }

            if (!prevAligned) {
                Candle c = candles.get(i);
                result.add(new AlignmentPoint(i, c.getTime(), c.getClose()));
            }
        }

        return result;
    }

    /**
     * 핵심 2: 정배열 이후 N자 5일선 + 양봉 패턴 시그널 찾기.
     */
    public static List<NPatternSignal> findNPatternSignals(List<Candle> candles,
                                                           int minSlopeDays,
                                                           int consecutiveBullDays,
                                                           double longBullBodyRatio) {

        if (candles == null || candles.isEmpty()) {
            return Collections.emptyList();
        }

        int n = candles.size();

        int p5 = 5;
        int p20 = 20;
        int p60 = 60;
        int p120 = 120;
        int p240 = 240;

        int maxPeriod = Math.max(p240,
                Math.max(p120, Math.max(p60, Math.max(p20, p5))));

        double[] ma5 = calcSma(candles, p5);
        double[] ma20 = calcSma(candles, p20);
        double[] ma60 = calcSma(candles, p60);
        double[] ma120 = calcSma(candles, p120);
        double[] ma240 = calcSma(candles, p240);

        // 1) 각 일자별로 정배열 여부를 먼저 계산
        boolean[] aligned = new boolean[n];
        Arrays.fill(aligned, false);

        for (int i = 0; i < n; i++) {
            if (i < maxPeriod - 1) {
                continue;
            }
            boolean isAligned =
                    isPerfectAscendingOrder(i, ma5, ma20, ma60, ma120, ma240) &&
                    isAllMaUpSlope(i, minSlopeDays, ma5, ma20, ma60, ma120, ma240);
            aligned[i] = isAligned;
        }

        // 2) 첫 정배열이 나온 시점
        int firstAlignedIndex = -1;
        for (int i = 0; i < n; i++) {
            if (aligned[i]) {
                firstAlignedIndex = i;
                break;
            }
        }

        if (firstAlignedIndex < 0) {
            return Collections.emptyList();
        }

        List<NPatternSignal> result = new ArrayList<>();

        // 장대양봉 판정 시 평균 몸통 계산에 사용할 기간
        int avgBodyPeriod = 20;

        for (int i = firstAlignedIndex; i < n; i++) {

            if (!aligned[i]) {
                continue;
            }

            // 5일선 N자 모양 조건
            if (!isNShapeOnMa5(i, ma5)) {
                continue;
            }

            boolean threeBullsOk = isConsecutiveBullish(candles, i, consecutiveBullDays);
            boolean longBullOk = isLongBullish(candles, i, avgBodyPeriod, longBullBodyRatio);

            if (!threeBullsOk && !longBullOk) {
                continue;
            }

            Candle c = candles.get(i);
            NPatternSignal.SignalType type =
                    threeBullsOk ? NPatternSignal.SignalType.THREE_BULLS
                                 : NPatternSignal.SignalType.LONG_BULL;

            result.add(new NPatternSignal(i, c.getTime(), c.getClose(), type));
        }

        return result;
    }

    /**
     * 단일 주기용 SMA 계산.
     * 구간이 부족하면 Double.NaN.
     */
    private static double[] calcSma(List<Candle> candles, int period) {
        int n = candles.size();
        double[] sma = new double[n];
        Arrays.fill(sma, Double.NaN);

        if (period <= 0 || n == 0) {
            return sma;
        }

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double close = candles.get(i).getClose();
            sum += close;

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
     * 정배열 위치 조건:
     *   ma5 >= ma20 >= ma60 >= ma120 >= ma240, NaN 없음.
     */
    private static boolean isPerfectAscendingOrder(int idx,
                                                   double[] ma5,
                                                   double[] ma20,
                                                   double[] ma60,
                                                   double[] ma120,
                                                   double[] ma240) {
        double v5 = ma5[idx];
        double v20 = ma20[idx];
        double v60 = ma60[idx];
        double v120 = ma120[idx];
        double v240 = ma240[idx];

        if (Double.isNaN(v5) || Double.isNaN(v20) || Double.isNaN(v60)
                || Double.isNaN(v120) || Double.isNaN(v240)) {
            return false;
        }

        return v5 >= v20 && v20 >= v60 && v60 >= v120 && v120 >= v240;
    }

    /**
     * 모든 이동평균이 최근 minDays 동안 상승(또는 횡보) 했는지 확인.
     */
    private static boolean isAllMaUpSlope(int idx,
                                          int minDays,
                                          double[]... maArrays) {
        if (minDays <= 0) {
            return true;
        }

        for (double[] ma : maArrays) {
            if (!isMaUpSlope(idx, minDays, ma)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMaUpSlope(int idx, int minDays, double[] ma) {
        for (int d = 0; d < minDays; d++) {
            int cur = idx - d;
            int prev = cur - 1;
            if (prev < 0) {
                return false;
            }
            double curVal = ma[cur];
            double prevVal = ma[prev];

            if (Double.isNaN(curVal) || Double.isNaN(prevVal)) {
                return false;
            }
            if (curVal < prevVal) {
                return false;
            }
        }
        return true;
    }

    /**
     * 5일선 N자 조건:
     *   ma5[i-3] > ma5[i-2]  (한 번 눌림)
     *   ma5[i-2] < ma5[i-1] < ma5[i]  (3일 연속 상승)
     */
    private static boolean isNShapeOnMa5(int idx, double[] ma5) {
        int i0 = idx - 3;
        int i1 = idx - 2;
        int i2 = idx - 1;
        int i3 = idx;

        if (i0 < 0) {
            return false;
        }

        double v0 = ma5[i0];
        double v1 = ma5[i1];
        double v2 = ma5[i2];
        double v3 = ma5[i3];

        if (Double.isNaN(v0) || Double.isNaN(v1) || Double.isNaN(v2) || Double.isNaN(v3)) {
            return false;
        }

        if (!(v0 > v1)) {
            return false;
        }

        if (!(v1 < v2 && v2 < v3)) {
            return false;
        }

        return true;
    }

    /**
     * idx 를 마지막으로 하는 양봉 연속 days 일인지 검사.
     */
    private static boolean isConsecutiveBullish(List<Candle> candles, int idx, int days) {
        if (days <= 0) {
            return true;
        }
        if (idx - days + 1 < 0) {
            return false;
        }

        for (int i = idx - days + 1; i <= idx; i++) {
            Candle c = candles.get(i);
            if (!(c.getClose() > c.getOpen())) {
                return false;
            }
        }
        return true;
    }

    /**
     * idx 의 캔들이 "장대 양봉" 인지 판정.
     *  - 양봉
     *  - 몸통 길이 >= 직전 avgPeriod 개 평균 몸통 * ratio
     */
    private static boolean isLongBullish(List<Candle> candles,
                                         int idx,
                                         int avgPeriod,
                                         double ratio) {

        Candle cur = candles.get(idx);
        if (!(cur.getClose() > cur.getOpen())) {
            return false;
        }

        if (avgPeriod <= 0) {
            return false;
        }

        int start = Math.max(0, idx - avgPeriod);
        if (start >= idx) {
            return false;
        }

        double sumBody = 0.0;
        int cnt = 0;

        for (int i = start; i < idx; i++) {
            Candle c = candles.get(i);
            double body = Math.abs(c.getClose() - c.getOpen());
            sumBody += body;
            cnt++;
        }

        if (cnt == 0) {
            return false;
        }

        double avgBody = sumBody / cnt;
        double curBody = Math.abs(cur.getClose() - cur.getOpen());

        return curBody >= avgBody * ratio;
    }

    /**
     * 디버그용 main.
     */
    public static void main(String[] args) {
        List<Candle> sample = new ArrayList<>();
        long base = System.currentTimeMillis();

        for (int i = 0; i < 300; i++) {
            double price = 100 + i * 0.5;
            sample.add(new Candle(base + i * 86400000L, price, price, price, price));
        }

        List<AlignmentPoint> aligns = findSmaAlignmentStarts(sample, 3);
        System.out.println("Alignment size = " + aligns.size());

        List<NPatternSignal> signals =
                findNPatternSignals(sample, 3, 3, 1.8);
        System.out.println("Signals size = " + signals.size());
    }
}
