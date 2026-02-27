package com.scheduler.kis_client.api.rest;

import java.util.function.Supplier;

import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.api.annotation.Header;

import lombok.Getter;
import lombok.Setter;

/**
 * 다중 페이지(연속조회)가 필요한 API의 경우
 * PageableResult를 상속받는 ApiResult class를 만듦으로써
 * next() 메서드를 사용할 수 있음.
 *
 * <p>예시</p>
 * {@see com.scheduler.kis_client.api.rest.InquireBalanceResult}
 */
@Setter
@Getter
public abstract class PageableApiResult<T extends ApiResult> implements ApiResult {

    @Header
    private String trCont;

    private String ctxAreaFk;
    private String ctxAreaNk;

    private String ctxAreaFk100;
    private String ctxAreaNk100;

    private String ctxAreaFk200;
    private String ctxAreaNk200;

    private final Supplier<T> nextFunction = null;

    public final T next() {
        if (
            nextFunction == null
            || (
                !trCont.equals("F")
                && !trCont.equals("M")
            )
        ) {
            return null;
        }

        return nextFunction.get();
    }

	public String getTrCont() {
		return trCont;
	}

	public void setTrCont(String trCont) {
		this.trCont = trCont;
	}

	public String getCtxAreaFk() {
		return ctxAreaFk;
	}

	public void setCtxAreaFk(String ctxAreaFk) {
		this.ctxAreaFk = ctxAreaFk;
	}

	public String getCtxAreaNk() {
		return ctxAreaNk;
	}

	public void setCtxAreaNk(String ctxAreaNk) {
		this.ctxAreaNk = ctxAreaNk;
	}

	public String getCtxAreaFk100() {
		return ctxAreaFk100;
	}

	public void setCtxAreaFk100(String ctxAreaFk100) {
		this.ctxAreaFk100 = ctxAreaFk100;
	}

	public String getCtxAreaNk100() {
		return ctxAreaNk100;
	}

	public void setCtxAreaNk100(String ctxAreaNk100) {
		this.ctxAreaNk100 = ctxAreaNk100;
	}

	public String getCtxAreaFk200() {
		return ctxAreaFk200;
	}

	public void setCtxAreaFk200(String ctxAreaFk200) {
		this.ctxAreaFk200 = ctxAreaFk200;
	}

	public String getCtxAreaNk200() {
		return ctxAreaNk200;
	}

	public void setCtxAreaNk200(String ctxAreaNk200) {
		this.ctxAreaNk200 = ctxAreaNk200;
	}

	public Supplier<T> getNextFunction() {
		return nextFunction;
	}

}
