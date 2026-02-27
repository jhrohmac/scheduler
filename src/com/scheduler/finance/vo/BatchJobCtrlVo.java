package com.scheduler.finance.vo;

public class BatchJobCtrlVo {

    private String job_id;
    private String job_name;
    private String enable_yn;
    private String interval_min;
    private String limit_cnt;
    private String stale_days;
    private String country_code;

    private String running_yn;
    private String last_start_dt;
    private String last_end_dt;
    private String last_result_code;
    private String last_result_msg;
    private String last_run_cnt;

    private String reg_dt;
    private String mod_dt;

    public String getJob_id() {
        return job_id;
    }
    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }
    public String getJob_name() {
        return job_name;
    }
    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }
    public String getEnable_yn() {
        return enable_yn;
    }
    public void setEnable_yn(String enable_yn) {
        this.enable_yn = enable_yn;
    }
    public String getInterval_min() {
        return interval_min;
    }
    public void setInterval_min(String interval_min) {
        this.interval_min = interval_min;
    }
    public String getLimit_cnt() {
        return limit_cnt;
    }
    public void setLimit_cnt(String limit_cnt) {
        this.limit_cnt = limit_cnt;
    }
    public String getStale_days() {
        return stale_days;
    }
    public void setStale_days(String stale_days) {
        this.stale_days = stale_days;
    }
    public String getCountry_code() {
        return country_code;
    }
    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }
    public String getRunning_yn() {
        return running_yn;
    }
    public void setRunning_yn(String running_yn) {
        this.running_yn = running_yn;
    }
    public String getLast_start_dt() {
        return last_start_dt;
    }
    public void setLast_start_dt(String last_start_dt) {
        this.last_start_dt = last_start_dt;
    }
    public String getLast_end_dt() {
        return last_end_dt;
    }
    public void setLast_end_dt(String last_end_dt) {
        this.last_end_dt = last_end_dt;
    }
    public String getLast_result_code() {
        return last_result_code;
    }
    public void setLast_result_code(String last_result_code) {
        this.last_result_code = last_result_code;
    }
    public String getLast_result_msg() {
        return last_result_msg;
    }
    public void setLast_result_msg(String last_result_msg) {
        this.last_result_msg = last_result_msg;
    }
    public String getLast_run_cnt() {
        return last_run_cnt;
    }
    public void setLast_run_cnt(String last_run_cnt) {
        this.last_run_cnt = last_run_cnt;
    }
    public String getReg_dt() {
        return reg_dt;
    }
    public void setReg_dt(String reg_dt) {
        this.reg_dt = reg_dt;
    }
    public String getMod_dt() {
        return mod_dt;
    }
    public void setMod_dt(String mod_dt) {
        this.mod_dt = mod_dt;
    }
}
