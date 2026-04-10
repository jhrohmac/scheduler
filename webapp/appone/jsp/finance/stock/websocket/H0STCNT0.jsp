<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/appone/plugins/system/js/jsLink.jsp" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>WebSocket Test</title>
<script type="text/javascript">
    var ws;
    function connectWebSocket() {
    	var realtimediv = $("#realtimediv").val();
        // WebSocket 서버의 URL
        var websocketURL = "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0";

        // WebSocket 연결
        ws = new WebSocket(websocketURL);

        // WebSocket이 열릴 때 호출되는 함수
        ws.onopen = function(event) {
            console.log("WebSocket connection opened");
            var requestData =  {
                "header":
                {
                         "approval_key": "3d1b9b71-addb-4ada-ba07-fe4d799a6226",
                         "custtype":"P",
                         "tr_type":"1",
                         "content-type":"utf-8"
                },
                "body":
                {
                         "input":
                         {
                                  "tr_id":"H0STCNT0",
                                  "tr_key":"005930"
                         }
                }
       		};
            
            
            // JSON 형태로 변환하여 WebSocket 서버로 전송
            ws.send(JSON.stringify(requestData));
        };

        // WebSocket 메시지를 수신했을 때 호출되는 함수
        ws.onmessage = function(event) {
            console.log("Received message: " + event.data);
            
            // 받은 메시지를 화면에 표시
            //document.getElementById("response").innerHTML = event.data;
            
            // 받은 메시지를 파싱하여 각 필드에 맞게 표시
            parseData(event.data);
        };

        // WebSocket 연결이 닫혔을 때 호출되는 함수
        ws.onclose = function(event) {
            console.log("WebSocket connection closed");
        };
    }

    function parseData(data) {
        // 데이터를 ^ 기준으로 분할
        var dataArray = data.split("^");
        
        // 각 필드에 데이터를 맵핑하여 화면에 표시
        document.getElementById("MKSC_SHRN_ISCD").innerHTML = "유가증권 단축 종목코드 : " + dataArray[0];
        document.getElementById("BSOP_HOUR").innerHTML = "영업 시간 : " + dataArray[1];
        document.getElementById("HOUR_CLS_CODE").innerHTML = "시간 구분 코드 : " + "장중"; // 시간 구분 코드가 0이므로
        document.getElementById("ASKP1").innerHTML = "매도호가1 : " + dataArray[2];
        document.getElementById("ASKP2").innerHTML = "매도호가2 : " + dataArray[3];
        document.getElementById("ASKP3").innerHTML = "매도호가3 : " + dataArray[4];
        document.getElementById("ASKP4").innerHTML = "매도호가4 : " + dataArray[5];
        document.getElementById("ASKP5").innerHTML = "매도호가5 : " + dataArray[6];
        document.getElementById("ASKP6").innerHTML = "매도호가6 : " + dataArray[7];
        document.getElementById("ASKP7").innerHTML = "매도호가7 : " + dataArray[8];
        document.getElementById("ASKP8").innerHTML = "매도호가8 : " + dataArray[9];
        document.getElementById("ASKP9").innerHTML = "매도호가9 : " + dataArray[10];
        document.getElementById("ASKP10").innerHTML = "매도호가10 : " + dataArray[11];
        document.getElementById("BIDP1").innerHTML = "매수호가1 : " + dataArray[12];
        document.getElementById("BIDP2").innerHTML = "매수호가2 : " + dataArray[13];
        document.getElementById("BIDP3").innerHTML = "매수호가3 : " + dataArray[14];
        document.getElementById("BIDP4").innerHTML = "매수호가4 : " + dataArray[15];
        document.getElementById("BIDP5").innerHTML = "매수호가5 : " + dataArray[16];
        document.getElementById("BIDP6").innerHTML = "매수호가6 : " + dataArray[17];
        document.getElementById("BIDP7").innerHTML = "매수호가7 : " + dataArray[18];
        document.getElementById("BIDP8").innerHTML = "매수호가8 : " + dataArray[19];
        document.getElementById("BIDP9").innerHTML = "매수호가9 : " + dataArray[20];
        document.getElementById("BIDP10").innerHTML = "매수호가10 : " + dataArray[21];
        document.getElementById("ASKP_RSQN1").innerHTML = "매도호가 잔량1 : " + dataArray[22];
        document.getElementById("ASKP_RSQN2").innerHTML = "매도호가 잔량2 : " + dataArray[23];
        document.getElementById("ASKP_RSQN3").innerHTML = "매도호가 잔량3 : " + dataArray[24];
        document.getElementById("ASKP_RSQN4").innerHTML = "매도호가 잔량4 : " + dataArray[25];
        document.getElementById("ASKP_RSQN5").innerHTML = "매도호가 잔량5 : " + dataArray[26];
        document.getElementById("ASKP_RSQN6").innerHTML = "매도호가 잔량6 : " + dataArray[27];
        document.getElementById("ASKP_RSQN7").innerHTML = "매도호가 잔량7 : " + dataArray[28];
        document.getElementById("ASKP_RSQN8").innerHTML = "매도호가 잔량8 : " + dataArray[29];
        document.getElementById("ASKP_RSQN9").innerHTML = "매도호가 잔량9 : " + dataArray[30];
        document.getElementById("ASKP_RSQN10").innerHTML = "매도호가 잔량10 : " + dataArray[31];
        document.getElementById("BIDP_RSQN1").innerHTML = "매수호가 잔량1 : " + dataArray[32];
        document.getElementById("BIDP_RSQN2").innerHTML = "매수호가 잔량2 : " + dataArray[33];
        document.getElementById("BIDP_RSQN3").innerHTML = "매수호가 잔량3 : " + dataArray[34];
        document.getElementById("BIDP_RSQN4").innerHTML = "매수호가 잔량4 : " + dataArray[35];
        document.getElementById("BIDP_RSQN5").innerHTML = "매수호가 잔량5 : " + dataArray[36];
        document.getElementById("BIDP_RSQN6").innerHTML = "매수호가 잔량6 : " + dataArray[37];
        document.getElementById("BIDP_RSQN7").innerHTML = "매수호가 잔량7 : " + dataArray[38];
        document.getElementById("BIDP_RSQN8").innerHTML = "매수호가 잔량8 : " + dataArray[39];
        document.getElementById("BIDP_RSQN9").innerHTML = "매수호가 잔량9 : " + dataArray[40];
        document.getElementById("BIDP_RSQN10").innerHTML = "매수호가 잔량10 : " + dataArray[41];
        document.getElementById("TOTAL_ASKP_RSQN").innerHTML = "총 매도호가 잔량 : " + dataArray[42];
        document.getElementById("TOTAL_BIDP_RSQN").innerHTML = "총 매수호가 잔량 : " + dataArray[43];
        document.getElementById("OVTM_TOTAL_ASKP_RSQN").innerHTML = "시간외 총 매도호가 잔량 : " + dataArray[44];
        document.getElementById("OVTM_TOTAL_BIDP_RSQN").innerHTML = "시간외 총 매수호가 잔량 : " + dataArray[45];
        document.getElementById("ANTC_CNPR").innerHTML = "예상 체결가 : " + dataArray[46];
        document.getElementById("ANTC_CNQN").innerHTML = "예상 체결량 : " + dataArray[47];
        document.getElementById("ANTC_VOL").innerHTML = "예상 거래량 : " + dataArray[48];
        document.getElementById("ANTC_CNTG_VRSS").innerHTML = "예상 체결 대비 : " + dataArray[49];
        document.getElementById("ANTC_CNTG_VRSS_SIGN").innerHTML = "예상 체결 대비 부호 : " + dataArray[50];
        document.getElementById("ANTC_CNTG_PRDY_CTRT").innerHTML = "예상 체결 전일 대비율 : " + dataArray[51];
        document.getElementById("ACML_VOL").innerHTML = "누적 거래량 : " + dataArray[52];
        document.getElementById("TOTAL_ASKP_RSQN_ICDC").innerHTML = "총 매도호가 잔량 증감 : " + dataArray[53];
        document.getElementById("TOTAL_BIDP_RSQN_ICDC").innerHTML = "총 매수호가 잔량 증감 : " + dataArray[54];
        document.getElementById("OVTM_TOTAL_ASKP_ICDC").innerHTML = "시간외 총 매도호가 증감 : " + dataArray[55];
        document.getElementById("OVTM_TOTAL_BIDP_ICDC").innerHTML = "시간외 총 매수호가 증감 : " + dataArray[56];
    }
</script>
</head>
<body>
    <h1>WebSocket Test</h1>
    <button onclick="connectWebSocket()">Connect WebSocket</button>
    <select id="realtimediv">
    	<option value="H0STCNT0">실시간 체결가</option>
    	<option value="H0STASP0">실시간 호가</option>
    	<option value="H0STCNI0">실시간 체결통보</option>
   	</select>
   	
    <div id="MKSC_SHRN_ISCD"></div>
    <div id="BSOP_HOUR"></div>
    <div id="HOUR_CLS_CODE"></div>
    <div id="ASKP1"></div>
    <div id="ASKP2"></div>
    <div id="ASKP3"></div>
    <div id="ASKP4"></div>
    <div id="ASKP5"></div>
    <div id="ASKP6"></div>
    <div id="ASKP7"></div>
    <div id="ASKP8"></div>
    <div id="ASKP9"></div>
    <div id="ASKP10"></div>
    <div id="BIDP1"></div>
    <div id="BIDP2"></div>
    <div id="BIDP3"></div>
    <div id="BIDP4"></div>
    <div id="BIDP5"></div>
    <div id="BIDP6"></div>
    <div id="BIDP7"></div>
    <div id="BIDP8"></div>
    <div id="BIDP9"></div>
    <div id="BIDP10"></div>
    <div id="ASKP_RSQN1"></div>
    <div id="ASKP_RSQN2"></div>
    <div id="ASKP_RSQN3"></div>
    <div id="ASKP_RSQN4"></div>
    <div id="ASKP_RSQN5"></div>
    <div id="ASKP_RSQN6"></div>
    <div id="ASKP_RSQN7"></div>
    <div id="ASKP_RSQN8"></div>
    <div id="ASKP_RSQN9"></div>
    <div id="ASKP_RSQN10"></div>
    <div id="BIDP_RSQN1"></div>
    <div id="BIDP_RSQN2"></div>
    <div id="BIDP_RSQN3"></div>
    <div id="BIDP_RSQN4"></div>
    <div id="BIDP_RSQN5"></div>
    <div id="BIDP_RSQN6"></div>
    <div id="BIDP_RSQN7"></div>
    <div id="BIDP_RSQN8"></div>
    <div id="BIDP_RSQN9"></div>
    <div id="BIDP_RSQN10"></div>
    <div id="TOTAL_ASKP_RSQN"></div>
    <div id="TOTAL_BIDP_RSQN"></div>
    <div id="OVTM_TOTAL_ASKP_RSQN"></div>
    <div id="OVTM_TOTAL_BIDP_RSQN"></div>
    <div id="ANTC_CNPR"></div>
    <div id="ANTC_CNQN"></div>
    <div id="ANTC_VOL"></div>
    <div id="ANTC_CNTG_VRSS"></div>
    <div id="ANTC_CNTG_VRSS_SIGN"></div>
    <div id="ANTC_CNTG_PRDY_CTRT"></div>
    <div id="ACML_VOL"></div>
    <div id="TOTAL_ASKP_RSQN_ICDC"></div>
    <div id="TOTAL_BIDP_RSQN_ICDC"></div>
    <div id="OVTM_TOTAL_ASKP_ICDC"></div>
    <div id="OVTM_TOTAL_BIDP_ICDC"></div>
</body>
</html>