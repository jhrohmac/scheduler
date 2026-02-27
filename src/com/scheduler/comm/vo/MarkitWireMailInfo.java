package com.scheduler.comm.vo;

import java.time.LocalDateTime;

/**
 * EWS에서 수집된 메일 정보 운반용 VO
 */
public class MarkitWireMailInfo {
    /** 메일 수신 시각 (분 단위) */
    private LocalDateTime receivedTime;
    /** 추출된 사용자 ID */
    private String userId;
    
    /** 추출된 사용자 Password */
    private String userPassword;
    
    /** 추출된 임시 키 */
    private String tempKey;
    /** 추출된 리셋 URL */
    private String url;
    /** 로그인 테스트 성공 여부 */
    private String changeStatus;
    
    private String receivedDate;

    public MarkitWireMailInfo() {
    }

    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(LocalDateTime receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getTempKey() {
        return tempKey;
    }

    public void setTempKey(String tempKey) {
        this.tempKey = tempKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

	public String getChangeStatus() {
		return changeStatus;
	}

	public void setChangeStatus(String changeStatus) {
		this.changeStatus = changeStatus;
	}

	public String getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(String receivedDate) {
		this.receivedDate = receivedDate;
	}

	@Override
    public String toString() {
        return "MarkitWireMailInfo{" +
                "receivedTime=" + receivedTime +
                ", userId='" + userId + '\'' +
                ", tempKey='" + tempKey + '\'' +
                ", url='" + url + '\'' +
                ", changeStatus=" + changeStatus +
                '}';
    }
}
