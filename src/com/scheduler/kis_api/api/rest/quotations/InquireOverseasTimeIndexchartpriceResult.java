package com.scheduler.kis_api.api.rest.quotations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.scheduler.kis_api.api.CommonRestResult;
import com.scheduler.kis_client.api.annotation.Header;

public class InquireOverseasTimeIndexchartpriceResult extends CommonRestResult {

    @Header
    private String trCont;
    private String rtCd;
    private String msgCd;
    private String msg1;
    private Output1 output1;
    private List<Output2> output2;

    public String getRtCd() {
        return rtCd;
    }

    public void setRtCd(String rtCd) {
        this.rtCd = rtCd;
    }

    public String getMsgCd() {
        return msgCd;
    }

    public void setMsgCd(String msgCd) {
        this.msgCd = msgCd;
    }

    public String getMsg1() {
        return msg1;
    }

    public void setMsg1(String msg1) {
        this.msg1 = msg1;
    }

    public String getTrCont() {
        return trCont;
    }

    public void setTrCont(String trCont) {
        this.trCont = trCont;
    }

    public Output1 getOutput1() {
        return output1;
    }

    public void setOutput1(Output1 output1) {
        this.output1 = output1;
    }

    public List<Output2> getOutput2() {
        return output2;
    }

    public void setOutput2(List<Output2> output2) {
        this.output2 = output2;
    }

    public static class Output1 {
        private final Map<String, Object> fields = new LinkedHashMap<String, Object>();

        @JsonAnySetter
        public void setField(String key, Object value) {
            fields.put(key, value);
        }

        public Object getField(String key) {
            return fields.get(key);
        }

        public Map<String, Object> getFields() {
            return fields;
        }
    }

    public static class Output2 {
        private final Map<String, Object> fields = new LinkedHashMap<String, Object>();

        @JsonAnySetter
        public void setField(String key, Object value) {
            fields.put(key, value);
        }

        public Object getField(String key) {
            return fields.get(key);
        }

        public Map<String, Object> getFields() {
            return fields;
        }
    }
}
