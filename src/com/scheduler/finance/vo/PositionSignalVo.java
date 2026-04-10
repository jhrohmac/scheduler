package com.scheduler.finance.vo;

/**
 * 포지션 평가 결과로 생성되는 이벤트/신호 VO
 */
public class PositionSignalVo {

    private String type;      // ENTRY_READY | INFO | WARNING | ...
    private long time;        // epoch ms
    private double price;     // 기준 가격(선택)
    private String message;   // 설명

    public PositionSignalVo() {}

    public PositionSignalVo(String type, long time, double price, String message) {
        this.type = type;
        this.time = time;
        this.price = price;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
