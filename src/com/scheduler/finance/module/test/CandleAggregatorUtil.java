package com.scheduler.finance.module.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.scheduler.finance.vo.StockDataVo;

public class CandleAggregatorUtil {

    private CandleAggregatorUtil() {
    }

    public static List<StockDataVo> aggregateMinutes(List<StockDataVo> source, int intervalMinutes) {

        if (source == null || source.size() == 0) {
            return new ArrayList<>();
        }
        if (intervalMinutes <= 0) {
            return new ArrayList<>(source);
        }

        List<StockDataVo> sorted = new ArrayList<>(source);
        Collections.sort(sorted, Comparator.comparingLong(StockDataVo::getDate));

        long intervalMs = intervalMinutes * 60L * 1000L;

        Map<Long, Agg> buckets = new LinkedHashMap<>();

        for (StockDataVo c : sorted) {
            if (c == null) {
                continue;
            }

            long bucketStart = (c.getDate() / intervalMs) * intervalMs;

            Agg agg = buckets.get(bucketStart);
            if (agg == null) {
                agg = new Agg(bucketStart);
                buckets.put(bucketStart, agg);
            }

            agg.add(c);
        }

        List<StockDataVo> result = new ArrayList<>();
        for (Agg agg : buckets.values()) {
            result.add(agg.toCandle());
        }
        return result;
    }

    private static class Agg {
        private final long bucketStart;

        private boolean initialized = false;
        private double open;
        private double high;
        private double low;
        private double close;
        private double volume;

        Agg(long bucketStart) {
            this.bucketStart = bucketStart;
        }

        void add(StockDataVo c) {
            if (!initialized) {
                open = c.getOpen();
                high = c.getHigh();
                low = c.getLow();
                close = c.getClose();
                volume = c.getVolume();
                initialized = true;
                return;
            }

            high = Math.max(high, c.getHigh());
            low = Math.min(low, c.getLow());
            close = c.getClose();
            volume += c.getVolume();
        }

        StockDataVo toCandle() {
            if (!initialized) {
                return new StockDataVo(bucketStart, 0, 0, 0, 0, 0);
            }
            return new StockDataVo(bucketStart, open, high, low, close, volume);
        }
    }
}
