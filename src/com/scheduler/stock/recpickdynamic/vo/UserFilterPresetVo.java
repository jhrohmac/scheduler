package com.scheduler.stock.recpickdynamic.vo;

/**
 * 사용자 화면 필터/지표 설정 프리셋 (TB_USER_FILTER_PRESET).
 */
public class UserFilterPresetVo {

    private String userId;
    private String presetType;     // REC_PICK_DYNAMIC, ...
    private String filterJson;     // 전체 설정 JSON 문자열
    private String regDt;
    private String updDt;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPresetType() { return presetType; }
    public void setPresetType(String presetType) { this.presetType = presetType; }
    public String getFilterJson() { return filterJson; }
    public void setFilterJson(String filterJson) { this.filterJson = filterJson; }
    public String getRegDt() { return regDt; }
    public void setRegDt(String regDt) { this.regDt = regDt; }
    public String getUpdDt() { return updDt; }
    public void setUpdDt(String updDt) { this.updDt = updDt; }
}
