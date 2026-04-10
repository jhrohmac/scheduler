package com.scheduler.kis_client.client.socket;

import java.util.Map;

import com.scheduler.kis_client.client.NetworkResponse;

public class WebSocketResponse implements NetworkResponse {

    private Map<String, String> header;
    private Body body;

    public WebSocketResponse() {
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    // 편의 메서드: 헤더에서 키로 바로 꺼내기
    public String getHeader(String key) {
        if (header == null) {
            return null;
        }
        return header.get(key);
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    // WebSocket 응답 body
    public static class Body {

        private String rtCd;
        private String msgCd;
        private String msg;
        private Map<String, String> output;

        public Body() {
        }

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

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Map<String, String> getOutput() {
            return output;
        }

        public void setOutput(Map<String, String> output) {
            this.output = output;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WebSocketResponse{");
        sb.append("header=").append(header);
        if (body != null) {
            sb.append(", rtCd=").append(body.getRtCd());
            sb.append(", msgCd=").append(body.getMsgCd());
            sb.append(", msg=").append(body.getMsg());
            sb.append(", output=").append(body.getOutput());
        } else {
            sb.append(", body=null");
        }
        sb.append('}');
        return sb.toString();
    }
}
