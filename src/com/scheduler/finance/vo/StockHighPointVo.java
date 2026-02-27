package com.scheduler.finance.vo;

/**
 * 차트의 마지막 캔들(가장 최근 종가)을 기준으로
 * "전고점"을 표현하기 위한 VO.
 *
 * 사용 예:
 *  - lastTime            : 마지막 캔들의 timestamp(ms)
 *  - lastClose           : 마지막 캔들의 종가
 *  - previousHighTime    : 마지막 종가 이상을 기록했던 직전 캔들의 timestamp(ms)
 *  - previousHighPrice   : 위 캔들의 high 값
 *  - previousHighFound   : 전고점 존재 여부 (false 면 신고가 영역)
 */
public class StockHighPointVo {

    private Long lastTime;
    private Double lastClose;

    private Long previousHighTime;
    private Double previousHighPrice;

    private boolean previousHighFound;

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Double getLastClose() {
        return lastClose;
    }

    public void setLastClose(Double lastClose) {
        this.lastClose = lastClose;
    }

    public Long getPreviousHighTime() {
        return previousHighTime;
    }

    public void setPreviousHighTime(Long previousHighTime) {
        this.previousHighTime = previousHighTime;
    }

    public Double getPreviousHighPrice() {
        return previousHighPrice;
    }

    public void setPreviousHighPrice(Double previousHighPrice) {
        this.previousHighPrice = previousHighPrice;
    }

    public boolean isPreviousHighFound() {
        return previousHighFound;
    }

    public void setPreviousHighFound(boolean previousHighFound) {
        this.previousHighFound = previousHighFound;
    }

    @Override
    public String toString() {
        return "StockHighPointVo{" +
                "lastTime=" + lastTime +
                ", lastClose=" + lastClose +
                ", previousHighTime=" + previousHighTime +
                ", previousHighPrice=" + previousHighPrice +
                ", previousHighFound=" + previousHighFound +
                '}';
    }
}
