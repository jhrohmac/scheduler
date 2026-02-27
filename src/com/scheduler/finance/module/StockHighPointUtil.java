package com.scheduler.finance.module;

import java.util.List;

import com.scheduler.finance.vo.StockHighPointVo;
import com.scheduler.finance.vo.StockLowPointVo;

/**
 * 차트 데이터(OHLC 배열)에서
 * "마지막 종가 기준 전고점/전저점"을 계산해 주는 유틸.
 *
 * chartData 구조:
 *  - List<List<Object>> 또는 List<ArrayList<Object>>
 *  - 각 row: [0]=time(ms), [1]=open, [2]=high, [3]=low, [4]=close
 *
 * 전고점 정의:
 *  - 마지막 캔들의 종가(lastClose)보다 크거나 같은 high 값들 중
 *    가장 높은 high 를 가진 캔들
 *
 * 전저점 정의:
 *  - 마지막 캔들의 종가(lastClose)보다 작거나 같은 low 값들 중
 *    가장 낮은 low 를 가진 캔들
 */
public class StockHighPointUtil {

    /**
     * 마지막 종가 기준 전고점 계산
     */
    @SuppressWarnings("rawtypes")
    public static StockHighPointVo findPreviousHighForLastClose(List chartData) {

        StockHighPointVo vo = new StockHighPointVo();

        if (chartData == null || chartData.size() == 0) {
            vo.setPreviousHighFound(false);
            return vo;
        }

        int lastIndex = chartData.size() - 1;
        List lastCandle = safeGetCandle(chartData, lastIndex);
        if (lastCandle == null || lastCandle.size() < 5) {
            vo.setPreviousHighFound(false);
            return vo;
        }

        Long lastTime = toLong(lastCandle.get(0));
        Double lastClose = toDouble(lastCandle.get(4));

        vo.setLastTime(lastTime);
        vo.setLastClose(lastClose);

        if (lastClose == null) {
            vo.setPreviousHighFound(false);
            return vo;
        }

        // 이전 모든 캔들(0 ~ lastIndex-1)에서
        // high >= lastClose 인 지점들 중 "가장 높은 high"
        Double bestHigh = null;
        Long bestTime = null;

        for (int i = 0; i < lastIndex; i++) {
            List candle = safeGetCandle(chartData, i);
            if (candle == null || candle.size() < 5) {
                continue;
            }

            Double high = toDouble(candle.get(2));
            Long time = toLong(candle.get(0));

            if (high == null) {
                continue;
            }

            if (high >= lastClose) {
                if (bestHigh == null) {
                    bestHigh = high;
                    bestTime = time;
                } else {
                    if (high > bestHigh) {
                        bestHigh = high;
                        bestTime = time;
                    } else if (high.equals(bestHigh) && time != null && bestTime != null && time > bestTime) {
                        bestTime = time;
                    }
                }
            }
        }

        if (bestHigh != null && bestTime != null) {
            vo.setPreviousHighFound(true);
            vo.setPreviousHighPrice(bestHigh);
            vo.setPreviousHighTime(bestTime);
        } else {
            vo.setPreviousHighFound(false);
        }

        return vo;
    }

    /**
     * 마지막 종가 기준 전저점 계산
     */
    @SuppressWarnings("rawtypes")
    public static StockLowPointVo findPreviousLowForLastClose(List chartData) {

        StockLowPointVo vo = new StockLowPointVo();

        if (chartData == null || chartData.size() == 0) {
            vo.setPreviousLowFound(false);
            return vo;
        }

        int lastIndex = chartData.size() - 1;
        List lastCandle = safeGetCandle(chartData, lastIndex);
        if (lastCandle == null || lastCandle.size() < 5) {
            vo.setPreviousLowFound(false);
            return vo;
        }

        Long lastTime = toLong(lastCandle.get(0));
        Double lastClose = toDouble(lastCandle.get(4));

        vo.setLastTime(lastTime);
        vo.setLastClose(lastClose);

        if (lastClose == null) {
            vo.setPreviousLowFound(false);
            return vo;
        }

        // 이전 모든 캔들(0 ~ lastIndex-1)에서
        // low <= lastClose 인 지점들 중 "가장 낮은 low"
        Double bestLow = null;
        Long bestTime = null;

        for (int i = 0; i < lastIndex; i++) {
            List candle = safeGetCandle(chartData, i);
            if (candle == null || candle.size() < 5) {
                continue;
            }

            Double low = toDouble(candle.get(3));
            Long time = toLong(candle.get(0));

            if (low == null) {
                continue;
            }

            if (low <= lastClose) {
                if (bestLow == null) {
                    bestLow = low;
                    bestTime = time;
                } else {
                    if (low < bestLow) {
                        bestLow = low;
                        bestTime = time;
                    } else if (low.equals(bestLow) && time != null && bestTime != null && time > bestTime) {
                        bestTime = time;
                    }
                }
            }
        }

        if (bestLow != null && bestTime != null) {
            vo.setPreviousLowFound(true);
            vo.setPreviousLowPrice(bestLow);
            vo.setPreviousLowTime(bestTime);
        } else {
            vo.setPreviousLowFound(false);
        }

        return vo;
    }

    @SuppressWarnings("rawtypes")
    private static List safeGetCandle(List chartData, int index) {
        if (index < 0 || index >= chartData.size()) {
            return null;
        }
        Object row = chartData.get(index);
        if (row instanceof List) {
            return (List) row;
        }
        return null;
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
