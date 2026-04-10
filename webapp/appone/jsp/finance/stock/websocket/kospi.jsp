<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>KOSPI 실시간 지수</title>
</head>
<body>
<h1>KOSPI 실시간 지수</h1>
<div id="kospi-index"></div>

<script>
// 웹소켓 연결
 var ws;	
        // WebSocket 서버의 URL
        var websocketURL = "ws://sandbox-apigw.koscom.co.kr/v2/market/stocks/realtime";
		var approval_key = "404870b0-2fd8-41f8-ac4d-a83e979a2222";	
		// WebSocket 연결
		ws = new WebSocket(websocketURL);
// API 키
// KOSPI 종목 코드
const kospiCode = 'KOSPI';

ws.onopen = function() {
    console.log('WebSocket connected');

    // 구독 요청 메시지 작성
    const subscribeMsg = {
        action: 'subscribe',
        data: {
            channel: kospiCode
        },
        requestId: 'your-request-id'
    };

    // 메시지 전송
    ws.send(JSON.stringify(subscribeMsg));
};

ws.onerror = function(error) {
    console.error('WebSocket error:', error);
};

ws.onmessage = function(e) {
    // 수신된 메시지 파싱
    const data = JSON.parse(e.data);

    // KOSPI 지수 메시지 처리
    if (data.channel === kospiCode) {
        // KOSPI 지수 업데이트
        document.getElementById('kospi-index').innerText = 'KOSPI Index: ' + data.data.totIdx;
    }
};

ws.onclose = function() {
    console.log('WebSocket connection closed');
};
</script>
</body>
</html>
