package com.scheduler.finance.vo;

/**
 * Signal event record for TB_S_SIGNAL_EVENT.
 * Keep as simple DTO/VO (String-based) to match existing DAO style.
 */
public class SignalEventVo {
    private String stock_code;
    private String country_code;
    private String timeframe;     // DAY | 30MIN
    private String signal_type;   // e.g. DAILY_TREND_OK, PULLBACK_REBOUND, BULLISH_MOMENTUM
    private String signal_grade;  // INFO|GOOD|WARN|BAD
    private String score_delta;   // numeric string
    private String event_time;    // YYYY-MM-DD HH24:MI:SS (or YYYY-MM-DD)
    private String price_ref;     // numeric string
    private String json_params;   // JSON string
    private String message;       // human readable
    private String source_job_id;

    public String getStock_code() { return stock_code; }
    public void setStock_code(String stock_code) { this.stock_code = stock_code; }

    public String getCountry_code() { return country_code; }
    public void setCountry_code(String country_code) { this.country_code = country_code; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public String getSignal_type() { return signal_type; }
    public void setSignal_type(String signal_type) { this.signal_type = signal_type; }

    public String getSignal_grade() { return signal_grade; }
    public void setSignal_grade(String signal_grade) { this.signal_grade = signal_grade; }

    public String getScore_delta() { return score_delta; }
    public void setScore_delta(String score_delta) { this.score_delta = score_delta; }

    public String getEvent_time() { return event_time; }
    public void setEvent_time(String event_time) { this.event_time = event_time; }

    public String getPrice_ref() { return price_ref; }
    public void setPrice_ref(String price_ref) { this.price_ref = price_ref; }

    public String getJson_params() { return json_params; }
    public void setJson_params(String json_params) { this.json_params = json_params; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSource_job_id() { return source_job_id; }
    public void setSource_job_id(String source_job_id) { this.source_job_id = source_job_id; }
}
