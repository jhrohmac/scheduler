package com.scheduler.finance.kis.quote;

/**
 * 신규 quote endpoint와 기존 current endpoint의 호환 응답 wrapper.
 * 프론트는 quote를 우선 사용하고, legacy 화면은 output을 계속 읽을 수 있다.
 */
public class KisQuoteResponse {
    private KisQuoteDto quote;
    private Object output;
    private Object raw;

    public KisQuoteDto getQuote() {
        return quote;
    }

    public void setQuote(KisQuoteDto quote) {
        this.quote = quote;
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }
}
