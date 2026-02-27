package com.scheduler.management.vo;

/**
 * Profile 화면용 확장 VO
 * - MyBatis resultType은 기존 userVo(alias) 사용을 권장
 * - 본 VO는 컨트롤러 단의 폼 바인딩, 화면 파라미터 캐리 목적(선택 사용)
 */
public class ProfileVo extends UserVo {

    // 화면 전용(쿼리 매핑 없음)
    private String userNewPwd;
    private String userNewPwd2;

    public String getUserNewPwd() {
        return userNewPwd;
    }
    public void setUserNewPwd(String userNewPwd) {
        this.userNewPwd = userNewPwd;
    }
    public String getUserNewPwd2() {
        return userNewPwd2;
    }
    public void setUserNewPwd2(String userNewPwd2) {
        this.userNewPwd2 = userNewPwd2;
    }
}
