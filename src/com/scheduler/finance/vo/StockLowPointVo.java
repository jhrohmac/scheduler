package com.scheduler.finance.vo;

/**
 * 차트의 마지막 캔들(가장 최근 종가)을 기준으로
 * "전저점"을 표현하기 위한 VO.
 *
 * 사용 예:
 *  - lastTime           : 마지막 캔들의 timestamp(ms)
 *  - lastClose          : 마지막 캔들의 종가
 *  - previousLowTime    : 마지막 종가 이하를 기록했던 전저점 timestamp(ms)
 *  - previousLowPrice   : 위 캔들의 low 값
 *  - previousLowFound   : 전저점 존재 여부
 */
public class StockLowPointVo {

    private Long lastTime;
    private Double lastClose;

    private Long previousLowTime;
    private Double previousLowPrice;

    private boolean previousLowFound;

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

    public Long getPreviousLowTime() {
        return previousLowTime;
    }

    public void setPreviousLowTime(Long previousLowTime) {
        this.previousLowTime = previousLowTime;
    }

    public Double getPreviousLowPrice() {
        return previousLowPrice;
    }

    public void setPreviousLowPrice(Double previousLowPrice) {
        this.previousLowPrice = previousLowPrice;
    }

    public boolean isPreviousLowFound() {
        return previousLowFound;
    }

    public void setPreviousLowFound(boolean previousLowFound) {
        this.previousLowFound = previousLowFound;
    }

    @Override
    public String toString() {
        return "StockLowPointVo{" +
                "lastTime=" + lastTime +
                ", lastClose=" + lastClose +
                ", previousLowTime=" + previousLowTime +
                ", previousLowPrice=" + previousLowPrice +
                ", previousLowFound=" + previousLowFound +
                '}';
    }
}
