package com.scheduler.finance.vo;


import java.time.format.DateTimeFormatter;

public class StockDataVo {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final long date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;

    public StockDataVo(long date, double open, double high, double low, double close, double volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public long getDate() {
        return date;
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
    
	public double getVolume() {
		return volume;
	}
    
}
enum SignalType {
BUY,
SELL,
HOLD
}