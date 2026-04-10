package com.scheduler.finance.module;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 이동평균 정배열(종가 > MA5 > MA20 > MA60 > MA120 > MA240) 구간 분석 모듈
 *
 * chartlist : getChartData에서 resultData.put("data", chartlist) 하는 동일 구조
 *  - 각 원소: [time, open, high, low, close] (List/ArrayList, Number 타입)
 */
public class MovingAverageAlignmentModule {

    /**
     * 응답용 결과 구조
     */
    public static class BullishAlignmentResult {
        private boolean found;
        private int index;
        private long time;
        private double close;
        private Map<Integer, Double> smaValues = new LinkedHashMap<>();

        public boolean isFound() {
            return found;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public double getClose() {
            return close;
        }

        public void setClose(double close) {
            this.close = close;
        }

        public Map<Integer, Double> getSmaValues() {
            return smaValues;
        }

        public void setSmaValues(Map<Integer, Double> smaValues) {
            this.smaValues = smaValues;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("found", found);
            map.put("index", index);
            map.put("time", time);
            map.put("close", close);

            Map<String, Object> smaMap = new LinkedHashMap<>();
            if (smaValues != null) {
                for (Map.Entry<Integer, Double> e : smaValues.entrySet()) {
                    smaMap.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
            map.put("smaValues", smaMap);
            return map;
        }
    }

    /**
     * 기본 기간(5,20,60,120,240) 기준 정배열 시작 구간 분석
     * chartList : StockUtil.getRebuildChartData(...) 결과 (ArrayList)
     */
    public static Map<String, Object> findDefaultBullishAlignmentAsMapFromChartList(List<?> chartList) {
        int[] defaultPeriods = {5, 20, 60, 120, 240};
        BullishAlignmentResult res = findBullishAlignmentFromChartList(chartList, defaultPeriods);
        if (res == null || !res.isFound()) {
            return null;
        }
        return res.toMap();
    }

    /**
     * chartList(OHLC 배열) 기준 정배열 시작 구간 분석
     */
    public static BullishAlignmentResult findBullishAlignmentFromChartList(List<?> chartList, int[] periods) {
        if (chartList == null || chartList.isEmpty() || periods == null || periods.length == 0) {
            return null;
        }

        int size = chartList.size();
        long[] times = new long[size];
        double[] closes = new double[size];

        // chartList -> time[], close[] 로 변환
        for (int i = 0; i < size; i++) {
            Object rowObj = chartList.get(i);
            if (!(rowObj instanceof List)) {
                return null;
            }
            @SuppressWarnings("rawtypes")
            List row = (List) rowObj;
            if (row.size() < 5) {
                return null;
            }
            Object tObj = row.get(0);
            Object cObj = row.get(4);
            if (!(tObj instanceof Number) || !(cObj instanceof Number)) {
                return null;
            }
            times[i] = ((Number) tObj).longValue();
            closes[i] = ((Number) cObj).doubleValue();
        }

        return findBullishAlignment(times, closes, periods);
    }

    /**
     * time[], close[] 기준 정배열 시작 구간 분석
     */
    public static BullishAlignmentResult findBullishAlignment(long[] times, double[] closes, int[] periods) {
        if (times == null || closes == null || times.length == 0 || closes.length == 0) {
            return null;
        }
        if (times.length != closes.length) {
            return null;
        }
        if (periods == null || periods.length == 0) {
            return null;
        }

        int size = closes.length;
        int[] sortedPeriods = Arrays.copyOf(periods, periods.length);
        Arrays.sort(sortedPeriods);

        Map<Integer, double[]> smaMap = new LinkedHashMap<>();
        for (int p : sortedPeriods) {
            double[] sma = calcSma(closes, p);
            smaMap.put(p, sma);
        }

        int maxPeriod = sortedPeriods[sortedPeriods.length - 1];
        boolean prevBullish = false;

        for (int i = maxPeriod - 1; i < size; i++) {
            boolean current = isBullishAtIndex(i, closes, smaMap, sortedPeriods);
            if (!prevBullish && current) {
                BullishAlignmentResult result = new BullishAlignmentResult();
                result.setFound(true);
                result.setIndex(i);
                result.setTime(times[i]);
                result.setClose(closes[i]);

                Map<Integer, Double> smaValues = new LinkedHashMap<>();
                for (int p : sortedPeriods) {
                    double[] series = smaMap.get(p);
                    if (series != null && i < series.length) {
                        smaValues.put(p, series[i]);
                    }
                }
                result.setSmaValues(smaValues);
                return result;
            }
            prevBullish = current;
        }

        return null;
    }

    private static boolean isBullishAtIndex(int index,
                                            double[] closes,
                                            Map<Integer, double[]> smaMap,
                                            int[] sortedPeriods) {
        double close = closes[index];

        double firstSma = getSmaValue(smaMap, sortedPeriods[0], index);
        if (Double.isNaN(firstSma)) {
            return false;
        }
        if (!(close > firstSma)) {
            return false;
        }

        double prev = firstSma;
        for (int i = 1; i < sortedPeriods.length; i++) {
            double v = getSmaValue(smaMap, sortedPeriods[i], index);
            if (Double.isNaN(v)) {
                return false;
            }
            if (!(prev > v)) {
                return false;
            }
            prev = v;
        }
        return true;
    }

    private static double getSmaValue(Map<Integer, double[]> smaMap, int period, int index) {
        double[] series = smaMap.get(period);
        if (series == null || index < 0 || index >= series.length) {
            return Double.NaN;
        }
        return series[index];
    }

    /**
     * 단순 이동평균(SMA)
     */
    private static double[] calcSma(double[] closes, int period) {
        int n = closes.length;
        double[] sma = new double[n];
        Arrays.fill(sma, Double.NaN);

        if (period <= 0 || n < period) {
            return sma;
        }

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += closes[i];
            if (i >= period) {
                sum -= closes[i - period];
            }
            if (i >= period - 1) {
                sma[i] = sum / period;
            }
        }
        return sma;
    }
}
