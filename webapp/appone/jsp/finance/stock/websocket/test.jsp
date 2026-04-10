<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ page import="org.apache.taglibs.standard.tag.rt.core.IfTag" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="/appone/plugins/system/js/jsLink.jsp" %>
<%@ include file="/appone/plugins/system/css/cssLink.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>WebSocket Test</title>

<script type="text/javascript">
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

    $(document).ready(function () {
    	
    	$('#btnClose').on('click', function (event) {
    		closeSocket();
    	});
    	
    	var url = "/scheduler/finance/searchStocksKeyword.do";
    	
    	$(".select2").select2({
    		placeholder: "종목명 & 코드",
    		minimumInputLength: 2, // 최소 검색어 길이
    		ajax: {
    			url: url,
    		    dataType: "json",
    		    delay: 400,
    		    data: function (params) {
    		    		return {
    		    			keyword : params.term
    		        	};
    		      	},
    		      	processResults: function (data, params) {
    		        	return { results: data.data // AJAX로 받은 결과
    		        	};
    		      	},		     
    		      	cache: true
    		    },
    			templateResult: formatState,
    			templateSelection: formatState
    	});
    	
    	$(".select2").on("select2:select", function(e) {
    		closeSocket();
    		var code = e.params.data.code;
    		connectWebSocket(code);
    		//fn_StockDetails(code);		// 선택된 Stock 정보 DB에 저장
    		$('.select2').val(null).trigger('change');	//select2 multiple 항목 삭제
    	});

    	function formatState(state) {
    		if (state.id === undefined) {
    			return state.text;
    		}else{
    			var	logo = "";
    			if(state.logo != null){
    				logo = "<img src='"+state.logo+"'class='img-size-32 img-circle'>";
    			}
    				var $container = $(
    					"<div class='media'>"+logo+
    					"	<div class='media-body'>"+
    					"		<h3 class='dropdown-item-title p-0'>"+state.ko_name+"</h3>"+
    					"		<span class='text-sm text-muted p-0'>"+state.code +" "+ state.market+"</span>"+
    					"	</div>"+
    					"</div>"
    				);
    			return $container;
    		}
    	};
    });

</script>
</head>
<body>
    <h1>WebSocket Test</h1>
   	<div class="row">
		<div class="col-lg-3">
			<div class="card mb-1">
				<div class="card-body p-1">
					<div class="row">
						<div class="col-lg-10 p-0">
							<div class="col-sm d-flex align-items-center justify-content-center">
								<select class="form-control select2" multiple="multiple" style="width: 100%;"></select>
							</div>
						</div>
						<div class="col-lg-2 p-0">
							<button type="button" class="btn btn-light btn-sm chartTime" id="btnClose">종료</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-lg-6">
			<div class="card mb-1">
				<div class="card-body p-1">
				    <label>상태메세지 : </label>
					<span id="message-input"></span><br>
				
				    <div id="MKSC_SHRN_ISCD"></div>
				    <div id="STCK_CNTG_HOUR"></div>
				    <div id="STCK_PRPR"></div>
				    <div id="PRDY_VRSS_SIGN"></div>
				    <div id="PRDY_VRSS"></div>
				    <div id="PRDY_CTRT"></div>
				    <div id="WGHN_AVRG_STCK_PRC"></div>
				    <div id="STCK_OPRC"></div>
				    <div id="STCK_HGPR"></div>
				    <div id="STCK_LWPR"></div>
				    <div id="ASKP1"></div>
				    <div id="BIDP1"></div>
				    <div id="CNTG_VOL"></div>
				    <div id="ACML_VOL"></div>
				    <div id="ACML_TR_PBMN"></div>
				    <div id="SELN_CNTG_CSNU"></div>
				    <div id="SHNU_CNTG_CSNU"></div>
				    <div id="NTBY_CNTG_CSNU"></div>
				    <div id="CTTR"></div>
				    <div id="SELN_CNTG_SMTN"></div>
				    <div id="SHNU_CNTG_SMTN"></div>
				    <div id="CCLD_DVSN"></div>
				    <div id="SHNU_RATE"></div>
				    <div id="PRDY_VOL_VRSS_ACML_VOL_RATE"></div>
				    <div id="OPRC_HOUR"></div>
				    <div id="OPRC_VRSS_PRPR_SIGN"></div>
				    <div id="OPRC_VRSS_PRPR"></div>
				    <div id="HGPR_HOUR"></div>
				    <div id="HGPR_VRSS_PRPR_SIGN"></div>
				    <div id="HGPR_VRSS_PRPR"></div>
				    <div id="LWPR_HOUR"></div>
				    <div id="LWPR_VRSS_PRPR_SIGN"></div>
				    <div id="LWPR_VRSS_PRPR"></div>
				    <div id="BSOP_DATE"></div>
				    <div id="NEW_MKOP_CLS_CODE"></div>
				    <div id="TRHT_YN"></div>
				    <div id="ASKP_RSQN1"></div>
				    <div id="BIDP_RSQN1"></div>
				    <div id="TOTAL_ASKP_RSQN"></div>
				    <div id="TOTAL_BIDP_RSQN"></div>
				    <div id="VOL_TNRT"></div>
				    <div id="PRDY_SMNS_HOUR_ACML_VOL"></div>
				    <div id="PRDY_SMNS_HOUR_ACML_VOL_RATE"></div>
				    <div id="HOUR_CLS_CODE"></div>
				    <div id="MRKT_TRTM_CLS_CODE"></div>
				    <div id="VI_STND_PRC"></div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>