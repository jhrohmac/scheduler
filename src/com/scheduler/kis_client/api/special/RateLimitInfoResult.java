package com.scheduler.kis_client.api.special;

import com.scheduler.kis_client.api.ApiResult;

import lombok.Data;

@Data
public class RateLimitInfoResult implements ApiResult {

    int remainingQuota;

	public int getRemainingQuota() {
		return remainingQuota;
	}

	public void setRemainingQuota(int remainingQuota) {
		this.remainingQuota = remainingQuota;
	}

}
