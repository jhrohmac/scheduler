package com.scheduler.finance.vo;

public class StockRecommendVo {

    private String stock_code;
    private String stock_ko_name;
    private String stock_market;
    private String stock_country_code;

    private String daily_close;
    private String daily_dist20_pct;

    private String now_price;
    private String now_diff;
    private String now_pct;


    private String reco_score;
    private String reco_signal_code;
    private String reco_signal_name;
    private String reco_trend_text;
    private String reco_reason_detail;
    private String reco_event_summary;

    // 시장 레짐 점수(표시용)
    private String regime_kr_score;
    private String regime_us_score;

    private String modify_date;

    public String getStock_code() {
        return stock_code;
    }
    public void setStock_code(String stock_code) {
        this.stock_code = stock_code;
    }
    public String getStock_ko_name() {
        return stock_ko_name;
    }
    public void setStock_ko_name(String stock_ko_name) {
        this.stock_ko_name = stock_ko_name;
    }
    public String getStock_market() {
        return stock_market;
    }
    public void setStock_market(String stock_market) {
        this.stock_market = stock_market;
    }
    public String getStock_country_code() {
        return stock_country_code;
    }
    public void setStock_country_code(String stock_country_code) {
        this.stock_country_code = stock_country_code;
    }

    public String getDaily_close() {
        return daily_close;
    }
    public void setDaily_close(String daily_close) {
        this.daily_close = daily_close;
    }
    public String getDaily_dist20_pct() {
        return daily_dist20_pct;
    }
    public void setDaily_dist20_pct(String daily_dist20_pct) {
        this.daily_dist20_pct = daily_dist20_pct;
    }

    public String getNow_price() {
        return now_price;
    }
    public void setNow_price(String now_price) {
        this.now_price = now_price;
    }
    public String getNow_diff() {
        return now_diff;
    }
    public void setNow_diff(String now_diff) {
        this.now_diff = now_diff;
    }
    public String getNow_pct() {
        return now_pct;
    }
    public void setNow_pct(String now_pct) {
        this.now_pct = now_pct;
    }

    public String getReco_score() {
        return reco_score;
    }
    public void setReco_score(String reco_score) {
        this.reco_score = reco_score;
    }
    public String getReco_signal_code() {
        return reco_signal_code;
    }
    public void setReco_signal_code(String reco_signal_code) {
        this.reco_signal_code = reco_signal_code;
    }
    public String getReco_signal_name() {
        return reco_signal_name;
    }
    public void setReco_signal_name(String reco_signal_name) {
        this.reco_signal_name = reco_signal_name;
    }
    public String getReco_trend_text() {
        return reco_trend_text;
    }
    public void setReco_trend_text(String reco_trend_text) {
        this.reco_trend_text = reco_trend_text;
    }
    public String getReco_reason_detail() {
        return reco_reason_detail;
    }
    public void setReco_reason_detail(String reco_reason_detail) {
        this.reco_reason_detail = reco_reason_detail;
    }

    public String getReco_event_summary() {
        return reco_event_summary;
    }
    public void setReco_event_summary(String reco_event_summary) {
        this.reco_event_summary = reco_event_summary;
    }

    public String getRegime_kr_score() {
        return regime_kr_score;
    }
    public void setRegime_kr_score(String regime_kr_score) {
        this.regime_kr_score = regime_kr_score;
    }
    public String getRegime_us_score() {
        return regime_us_score;
    }
    public void setRegime_us_score(String regime_us_score) {
        this.regime_us_score = regime_us_score;
    }

    public String getModify_date() {
        return modify_date;
    }
    public void setModify_date(String modify_date) {
        this.modify_date = modify_date;
    }
}
