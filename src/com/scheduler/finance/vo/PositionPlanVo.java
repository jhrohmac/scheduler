package com.scheduler.finance.vo;

/**
 * 포지션 플랜(진입 전 평가용 MVP)
 *
 * - 아직 실제 주문/포지션이 없을 때도 "진입 준비 상태"를 계산하기 위한 구조
 */
public class PositionPlanVo {

    // 상태: NO_POSITION | WATCH | ENTRY_READY
    private String state;

    // 추천 진입가(현재는 last close 기준)
    private double suggestedEntryPrice;

    // 초기 손절(구조 기반, 최근 스윙로우)
    private double initialStopPrice;

    // 초기 위험폭 R = entry - stop
    private double riskR;

    // 원금 회수 1차 목표가(추천: +1R)
    private double takeProfit1Price;

    // 부분 익절 비율(추천: 0.5)
    private double partialTakeProfitRatio;

    public PositionPlanVo() {}

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getSuggestedEntryPrice() {
        return suggestedEntryPrice;
    }

    public void setSuggestedEntryPrice(double suggestedEntryPrice) {
        this.suggestedEntryPrice = suggestedEntryPrice;
    }

    public double getInitialStopPrice() {
        return initialStopPrice;
    }

    public void setInitialStopPrice(double initialStopPrice) {
        this.initialStopPrice = initialStopPrice;
    }

    public double getRiskR() {
        return riskR;
    }

    public void setRiskR(double riskR) {
        this.riskR = riskR;
    }

    public double getTakeProfit1Price() {
        return takeProfit1Price;
    }

    public void setTakeProfit1Price(double takeProfit1Price) {
        this.takeProfit1Price = takeProfit1Price;
    }

    public double getPartialTakeProfitRatio() {
        return partialTakeProfitRatio;
    }

    public void setPartialTakeProfitRatio(double partialTakeProfitRatio) {
        this.partialTakeProfitRatio = partialTakeProfitRatio;
    }
}
