/************************************************************************
*  Title        : 공통 자바스크립트 모음 ( 취합 )

*  Description  :  구성
*       1)팝업 관련 Method
*       2)폼 관련 Method
*       3)Util Method( Date, String, Etc...)
*  Copyright    : Copyright (c) 2003.03.02
*  Company      : Soft on mobile
*  사용환경       : Explorer 5.5 이상 최적
*  @author	    : feelhouse@orgio.net
*  @version     : 0.9
*************************************************************************/
    var ws;
    function connectWebSocket(symbol_id) {
    	
        // WebSocket 서버의 URL
        var websocketURL = "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0";
		var approval_key = "404870b0-2fd8-41f8-ac4d-a83e979a2222";
        // WebSocket 연결
        ws = new WebSocket(websocketURL);

        // WebSocket이 열릴 때 호출되는 함수
        ws.onopen = function(event) {
        	$('#message-input').text("WebSocket connection opened");
            console.log("WebSocket connection opened");
            var requestData =  {
                "header":
                {
                         "approval_key": approval_key,
                         "custtype":"P",
                         "tr_type":"1",
                         "content-type":"utf-8"
                },
                "body":
                {
                         "input":
                         {
                                	  "tr_id":"H0STCNT0",
                                      "tr_key": symbol_id
                         }
                }
       		};
            // JSON 형태로 변환하여 WebSocket 서버로 전송
            ws.send(JSON.stringify(requestData));
        };

        // WebSocket 메시지를 수신했을 때 호출되는 함수
        ws.onmessage = function(event) {
        	$('#message-input').text(symbol_id+" 연결되었습니다.");
            //console.log("Received message: " + event.data);
            
            // 받은 메시지를 화면에 표시
            //document.getElementById("response").innerHTML = event.data;
            // 받은 메시지를 화면에 표시
            //document.getElementById("response").innerHTML = parseData(event.data);
             
            // 받은 메시지를 파싱하여 각 필드에 맞게 표시
            parseData(event.data);
        };
    }
    
    // WebSocket 연결이 닫혔을 때 호출되는 함수
    function closeSocket() {
    	console.log("ws===========");
    	console.log(ws);
    	if(ws != undefined){
    		ws.close();
        	ws = null;
        	$('#message-input').text('연결이 종료되었습니다.');
    	}
    }
    
    function parseData(data) {
        // 데이터를 ^ 기준으로 분할
        var dataArray = data.split("^");
        
        // 각 필드에 데이터를 맵핑하여 화면에 표시
        document.getElementById("MKSC_SHRN_ISCD").innerHTML = "유가증권 단축 종목코드 : " + dataArray[0];
        document.getElementById("STCK_CNTG_HOUR").innerHTML = "주식 체결 시간 : " + dataArray[1];
        document.getElementById("STCK_PRPR").innerHTML = "주식 현재가 : " + dataArray[2];
        document.getElementById("PRDY_VRSS_SIGN").innerHTML = "전일 대비 부호 : " + dataArray[3];
        /* 	1 : 상한
        	2 : 상승
        	3 : 보합
        	4 : 하한
        	5 : 하락 
       	*/
        document.getElementById("PRDY_VRSS").innerHTML = "전일 대비 : " + dataArray[4];
        document.getElementById("PRDY_CTRT").innerHTML = "전일 대비율 : " + dataArray[5];
        document.getElementById("WGHN_AVRG_STCK_PRC").innerHTML = "가중 평균 주식 가격 : " + dataArray[6];
        document.getElementById("STCK_OPRC").innerHTML = "주식 시가 : " + dataArray[7];
        document.getElementById("STCK_HGPR").innerHTML = "주식 최고가 : " + dataArray[8];
        document.getElementById("STCK_LWPR").innerHTML = "주식 최저가 : " + dataArray[9];
        document.getElementById("ASKP1").innerHTML = "매도호가1 : " + dataArray[10];
        document.getElementById("BIDP1").innerHTML = "매수호가1 : " + dataArray[11];
        document.getElementById("CNTG_VOL").innerHTML = "체결 거래량 : " + dataArray[12];
        document.getElementById("ACML_VOL").innerHTML = "누적 거래량 : " + dataArray[13];
        document.getElementById("ACML_TR_PBMN").innerHTML = "누적 거래 대금 : " + dataArray[14];
        document.getElementById("SELN_CNTG_CSNU").innerHTML = "매도 체결 건수 : " + dataArray[15];
        document.getElementById("SHNU_CNTG_CSNU").innerHTML = "매수 체결 건수 : " + dataArray[16];
        document.getElementById("NTBY_CNTG_CSNU").innerHTML = "순매수 체결 건수 : " + dataArray[17];
        document.getElementById("CTTR").innerHTML = "체결강도 : " + dataArray[18];
        document.getElementById("SELN_CNTG_SMTN").innerHTML = "총 매도 수량 : " + dataArray[19];
        document.getElementById("SHNU_CNTG_SMTN").innerHTML = "총 매수 수량 : " + dataArray[20];
        document.getElementById("CCLD_DVSN").innerHTML = "체결구분 : " + dataArray[21];
        document.getElementById("SHNU_RATE").innerHTML = "매수비율 : " + dataArray[22];
        document.getElementById("PRDY_VOL_VRSS_ACML_VOL_RATE").innerHTML = "전일 거래량 대비 등락율 : " + dataArray[23];
        document.getElementById("OPRC_HOUR").innerHTML = "시가 시간 : " + dataArray[24];
        document.getElementById("OPRC_VRSS_PRPR_SIGN").innerHTML = "시가대비구분 : " + dataArray[25];
        document.getElementById("OPRC_VRSS_PRPR").innerHTML = "시가대비 : " + dataArray[26];
        document.getElementById("HGPR_HOUR").innerHTML = "최고가 시간 : " + dataArray[27];
        document.getElementById("HGPR_VRSS_PRPR_SIGN").innerHTML = "고가대비구분 : " + dataArray[28];
        document.getElementById("HGPR_VRSS_PRPR").innerHTML = "고가대비 : " + dataArray[29];
        document.getElementById("LWPR_HOUR").innerHTML = "최저가 시간 : " + dataArray[30];
        document.getElementById("LWPR_VRSS_PRPR_SIGN").innerHTML = "저가대비구분 : " + dataArray[31];
        document.getElementById("LWPR_VRSS_PRPR").innerHTML = "저가대비 : " + dataArray[32];
        document.getElementById("BSOP_DATE").innerHTML = "영업 일자 : " + dataArray[33];
        document.getElementById("NEW_MKOP_CLS_CODE").innerHTML = "신 장운영 구분 코드 : " + dataArray[34];
        document.getElementById("TRHT_YN").innerHTML = "거래정지 여부 : " + dataArray[35];
        document.getElementById("ASKP_RSQN1").innerHTML = "매도호가 잔량1 : " + dataArray[36];
        document.getElementById("BIDP_RSQN1").innerHTML = "매수호가 잔량1 : " + dataArray[37];
        document.getElementById("TOTAL_ASKP_RSQN").innerHTML = "총 매도호가 잔량 : " + dataArray[38];
        document.getElementById("TOTAL_BIDP_RSQN").innerHTML = "총 매수호가 잔량 : " + dataArray[39];
        document.getElementById("VOL_TNRT").innerHTML = "거래량 회전율 : " + dataArray[40];
        document.getElementById("PRDY_SMNS_HOUR_ACML_VOL").innerHTML = "전일 동시간 누적 거래량 : " + dataArray[41];
        document.getElementById("PRDY_SMNS_HOUR_ACML_VOL_RATE").innerHTML = "전일 동시간 누적 거래량 비율 : " + dataArray[42];
        document.getElementById("HOUR_CLS_CODE").innerHTML = "시간 구분 코드 : " + dataArray[43];
        document.getElementById("MRKT_TRTM_CLS_CODE").innerHTML = "임의종료구분코드 : " + dataArray[44];
        document.getElementById("VI_STND_PRC").innerHTML = "정적VI발동기준가 : " + dataArray[45];
    }