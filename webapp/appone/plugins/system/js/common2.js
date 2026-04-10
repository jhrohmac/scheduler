

/**
 * 공통 AJAX 호출 및 에러/알림 처리 모듈
 * - 중복됐던 ajaxCall, fn_ajaxCall_login, ajaxCall_beta, fn_ajaxCall_Error, fn_alertpop 등을 하나로 통합
 * - 세션 체크 여부, 에러 처리, 알림 옵션 등을 일괄 설정 가능
 */

/**
 * AJAX 요청을 수행합니다.
 * @param {Object} options 설정 객체
 *   - url (string): 요청 URL
 *   - method (string, default: "POST"): HTTP 메서드
 *   - dataType (string, default: "json"): 응답 데이터 형식
 *   - data (object|string, default: {}): 전송할 파라미터
 *   - async (boolean, default: false): 비동기 여부
 *   - sessionCheck (boolean, default: true): 세션 체크 수행 여부
 *   - alertOnError (boolean, default: true): 에러 발생 시 토스트 알림 표시 여부
 *   - alertOptions (object): { icon, title, timer }
 *   - onSuccess (function): 성공 콜백
 *   - onError (function): 에러 콜백 (커스텀 처리)
 */
function ajaxRequest(options) {
  var cfg = $.extend(true, {
    method: "POST",
    dataType: "json",
    data: {},
    async: false,
    sessionCheck: true,
    alertOnError: true,
    alertOptions: { icon: 'error', timer: 2000 },
    onSuccess: function() {},
    onError: null
  }, options);

  function doCall() {
    $.ajax({
      type: cfg.method,
      url: cfg.url,
      dataType: cfg.dataType,
      data: cfg.data,
      async: cfg.async,
      success: cfg.onSuccess,
      error: function(req, status, err) {
        if (cfg.alertOnError) showAlert(cfg.alertOptions.icon, err, cfg.alertOptions.timer);
        if (typeof cfg.onError === 'function') {
          cfg.onError(req, status, err);
        } else {
          errorView({ error_msg: err, error_code: req.status, error_dtl: req.responseText });
        }
      }
    });
  }

  if (cfg.sessionCheck) {
    $.ajax({
      type: "POST",
      url: "/scheduler/session/sessionchk.do",
      dataType: "json",
      async: false,
      success: function(res) {
        if (res.system_code === '0000') {
          doCall();
        } else {
          showAlert('error', '세션이 만료되었습니다. 다시 로그인해주세요.', 2000, redirectLogout);
        }
      },
      error: function() {
        showAlert('error', '세션 확인 중 오류가 발생했습니다.', 2000);
      }
    });
  } else {
    doCall();
  }
}

/**
 * 간편 호출 함수
 * @param {string} url 요청 URL
 * @param {string} method HTTP 메서드
 * @param {object|string} dataParam getJQParams 등으로 생성된 파라미터
 * @param {function} callback 성공 콜백 함수 (response 데이터 인수)
 * @param {string} [dataType='json'] 응답 데이터 형식
 */
function ajaxCall(url, dataType, dataParam, callback) {
  ajaxRequest({
    url: url,
    method: 'POST',
    data: dataParam,
    dataType: dataType || 'json',
    onSuccess: callback
  });
}

/**
 * 토스트 알림 표시
 */
function showAlert(icon, title, timer, callback) {
  var Toast = Swal.mixin({
    toast: true,
    position: 'center',
    showConfirmButton: false,
    timerProgressBar: true,
    timer: timer
  });
  Toast.fire({ icon: icon, title: title });
  if (typeof callback === 'function') setTimeout(callback, timer);
}

/**
 * 에러 뷰 표시
 */
function errorView(params) {
  ajaxRequest({
    url: '/scheduler/comm/result/error_view.do',
    method: 'POST',
    dataType: 'html',
    data: params,
    async: false,
    sessionCheck: false,
    alertOnError: false,
    onSuccess: function(html) { $('#mainContent').html(html); }
  });
}

/**
 * 로그아웃 처리
 */
function redirectLogout() {
  location.href = '/scheduler/logout.do';
}



/************************************************************************
*  Title        : 공통 자바스크립트 모음
*  Description  : 공통 구성
*  Copyright    : Copyright (c) 2024.12.32
*  Company      : IPS CORP
*  사용환경        : Web
*  @author	    : admin@ipscorp.co.kr
*  @version     : 3.0.0
*************************************************************************/

/*Login  Call Function*/
function fn_ajaxCall_login(url, dataType, param, call_function) {
	$.ajax({
		type: "POST",
		url: url,
		dataType: dataType,
		data: param,
		async: false,
		success: call_function,
		error: function(request, status, error) {
			if (request.status == '200') {
				fn_alertpop('', "error", error);
			} else {
				var err_param = "error_msg=" + error + "&error_code=" + request.status + "&error_dtl=" + request.responseText;
				fn_ajaxCall_Error(err_param, fn_ErrorContents);
			}
		}
	});
}

function ajaxCall_beta(url, dataType, param, call_function) {
	$.ajax({
		type: "POST",
		url: url,
		dataType: dataType,
		data: param,
		async: false,
		success: call_function,
		error: function(request, status, error) {
			if (request.status == "200") {
				var err_param = "error_msg=" + error + "&error_code=" + request.status + "&error_dtl=" + request.responseText;
				fn_ajaxCall_Error(err_param, fn_ErrorContents);
			} else {
				var err_param = "error_msg=" + error + "&error_code=" + request.status + "&error_dtl=" + request.responseText;
				fn_ajaxCall_Error(err_param, fn_ErrorContents);
			}
		}
	});
};

var connetion_faile="";
/* 컨트롤러 호출 공통 함수 */
function ajaxCall_OLD(url, dataType, param, call_function) {
	//Session Check
	$.ajax({
		type: "POST",
		url: "/scheduler/session/sessionchk.do",
		dataType: "json",
		async: false,
		success: function(data) {
			if (data.system_code == "0000") {
				connetion_faile = "";
				$.ajax({
					type: "POST",
					url: url,
					dataType: dataType,
					data: param,
					async: false,
					success: call_function,
					error: function(request, status, error) {
						if (request.status == "200") {
							var err_param = "error_msg=" + error + "&error_code=" + request.status + "&error_dtl=" + request.responseText;
							fn_ajaxCall_Error(err_param, fn_ErrorContents);
						} else {
							var err_param = "error_msg=" + error + "&error_code=" + request.status + "&error_dtl=" + request.responseText;
							fn_ajaxCall_Error(err_param, fn_ErrorContents);
						}
					}
				});
			}else {
				if (connetion_faile == "") {
					connetion_faile = "Y";
					fn_alertpop(fn_logout, "error", "Session information is not. Please log in again.","2000")
				}
				fn_alertpop(fn_logout, "error", "Session information is not. Please log in again." + data.result_msg,"2000");
			}
		},
		error: function(request, status, error) {
			if (request.status == '404') {
				fn_alertpop('', "error", error,"3000");
			} else {
				var err_param = "error_msg=" + error + "&error_code=" + request.status + "&error_dtl=" + request.responseText;
				fn_ajaxCall_Error(err_param, fn_ErrorContents);
			}
		}
	});
}

function fn_ErrorContents(data) {
	$('#mainContent').html(data);
}
function fn_login() {
  document.location.href='/scheduler/login.do';
}

/* Alert */
function fn_alertpop(_fn_call, result_code, result_msg,timer) {
	
	var Toast = Swal.mixin({
		toast: true,
		position: 'center',
		showConfirmButton: false,
		timerProgressBar: true,
		timer: timer
	});
	
	Toast.fire({
		icon: result_code,
		title: result_msg
	});

	if (_fn_call != '') {
		setTimeout(_fn_call(), 2000);
	}
}

/* Error Page Call */
function fn_ajaxCall_Error(param, call_function) {
	$.ajax({
		type: "POST",
		url: "/scheduler/comm/result/error_view.do",
		dataType: "html",
		data: param,
		async: false,
		success: call_function,
		error: function(request, status, error) {
			if (request.status == '200') {
				fn_alertpop('', "error", error,"2000")
			} else if (request.status == '0') {
				fn_alertpop(fn_logout, "error", "세션이 끊켰습니다. 재접속 해주세요.","2000")
			}
		}
	});
}
/* Log Out Page */
function fn_logout() {
	document.location.href = '/scheduler/logout.do';
	location.replace("/scheduler/");
};

/**
 * 새창을 여는 함수(왼쪽 상단)
 *
 * @param url
 * @param winName
 * @param sizeW
 * @param sizeH
 */
function jsOpenWin(url, winName, sizeW, sizeH) {
	var nLeft = 0;
	var nTop = 0;
	opt = ",toolbar=no,menubar=no,location=no,scrollbars=yes,status=no";
	window.open(url, winName, "left=" + nLeft + ",top=" + nTop + ",width=" + sizeW + ",height=" + sizeH + opt);
}

/////////////////////////////////////////////////////////////////
//	2)폼 관련 Method
/////////////////////////////////////////////////////////////////

function fn_formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function isBoolean(bool) {
	if (bool == "true" || bool == "false") {
		return true;
	}
	return false;
}

/**
   * 한글로만 이루어져 있는지 체크 한다.
   *
   * @param	han
   * @return	boolean
   */
function isHangul(han) {
	var inText = han.value;
	var ret;

	ret = inText.charCodeAt();
	if (ret > 31 && ret < 127) {
		//alert("한글만 입력 가능합니다.");
		han.value = "";
		han.focus();
		return false;
	}
	return true;
}

/**
 * 영문자 검증
 * @param str
 * @return boolean
 */
function isAlpha(obj) {
	var str = obj.value;
	if (isEmpty(str)) {
		return false;
	}

	for (var idx = 0; idx < str.length; idx++) {
		if (!((str.charAt(idx) >= 'a' && str <= 'z') || (str.charAt(idx) >= 'A' && str <= 'Z'))) {
			return false;
		}
	}
	return true;
}


/**
 * 숫자에서 comma를 없앤다.
 *
 * @param	str
 */
function deleteCommaStr(str) {
	var temp = '';

	for (var i = 0; i < str.length; i++) {
		if (str.charAt(i) == ',') {
			continue;
		} else {
			temp += str.charAt(i);
		}
	}
	return temp;
}

/**
 * 문자에서 분리문자를 없앤다.
 *
 * @param	str, delimeter
 */
function delDelimeterStr(str, delimeter) {
	var temp = '';

	for (var i = 0; i < str.length; i++) {
		if (str.charAt(i) == delimeter) {
			continue;
		} else {
			temp += str.charAt(i);
		}
	}
	return temp;
}


/**
 * 입력값에 스페이스 이외의 의미있는 값이 있는지 체크
 * ex) if (isEmpty(form.keyword)) {
 *         alert("검색조건을 입력하세요.");
 *     }
 */
function isEmpty(input) {
	if (input.value == null || input.value.replace(/ /gi, "") == "") {
		return true;
	}
	return false;
}

/**
 * 입력값이 사용자가 정의한 포맷 형식인지 체크
 * 자세한 format 형식은 자바스크립트의 'regular expression'을 참조
 */
function isValidFormat(inputStr, re) {
	if (re.test(inputStr)) {
		return true; //올바른 포맷 형식
	}
	return false;
}

/**
 * 입력값이 전화번호 형식(숫자-숫자-숫자)인지 (느슨한) 체크 pcs 번호 체크와 같이 사용해도 됨
 */
function isValidPhone2(input) {
	var format = /^(\d+)-(\d+)-(\d+)$/;
	return isValidFormat(input, format);
}

/**
 * 입력값이 핸드폰번호 형식(사업자번호-국-번호)인지 체크
 *
 * 사업자번호 011 016 018 017 019
 */
function isValidPcs(inputStr) {
	if (inputStr == null || inputStr.replace(/ /gi, "") == "") {
		return false;
	} else {
		var re = /^(010|011|016|018|017|019)-[1-9][0-9]{2,3}-[0-9]{4}$/;
		return isValidFormat(inputStr, re);
	}
}

/**
 * 입력값이 핸드폰번호 형식(사업자번호)인지 체크
 *
 * 사업자번호 011 016 018 017 019
 */
function isValidPcs1(inputStr) {
	if (inputStr == null || inputStr.replace(/ /gi, "") == "") {
		return false;
	} else {
		var re = /^(010|011|016|018|017|019)$/;
		return isValidFormat(inputStr, re);
	}
}



/**
 * 아이디 유효성검사
 * @param 아이디 폼객체
 * @return true, false
 */
function isValidId(thisObj) {
	var tmp;
	var frmMemId = thisObj;
	tmp = frmMemId.value;

	if (tmp.length < 4 || tmp.length > 10) {
		alert("ID는4글자 이상, 10 글자 이하입니다.");
		frmMemId.focus();
		return false;
	}

	for (var i = 0; i < tmp.length; i++) {
		if (tmp.charAt(i) >= '0' && tmp.charAt(i) <= '9') continue;
		else if (tmp.charAt(i) >= 'a' && tmp.charAt(i) <= 'z') continue;
		else if (tmp.charAt(i) >= 'A' && tmp.charAt(i) <= 'Z') continue;
		else if (tmp.charAt(i) == '_' || tmp.charAt(i) == '-') continue;
		else {
			alert("ID에는 영문자, 숫자, 기호 ('-' , '_' ) 만 사용하실 수 있습니다.");
			frmMemId.value = "";
			frmMemId.focus();
			return false;
		}
	}

	if (tmp.charAt(0) == '_' || tmp.charAt(0) == '-') {
		alert("'_'와 '-'는 ID의 첫글자로 사용하실 수 없습니다.");
		return false;
	}
	return true;
}

function isValidName(frmObj) {
	var frmMemName = frmObj;

	if (trim(frmMemName.value) == "" || getLength(frmMemName.value) < 1 || getLength(frmMemName.value) > 10) {
		alert("이름를 정확히 입력하세요.");
		frmMemName.focus();
		return false;
	}
	return true;
}

/**
 * 문자에서 Hyphen을 없앤다.
 *
 * @param	str
 */
function deleteHyphen(str) {

	var temp = '';
	if (str == "") return temp;
	for (var i = 0; i < str.length; i++) {
		if (str.charAt(i) == '-') {
			continue;
		} else {
			temp += str.charAt(i);
		}
	}
	return temp;
}

/**
 * Cookie설정하기
 */
function setCookie(name, value, expire) {
	document.cookie = name + "=" + escape(value)
		+ ((expire) ? "; expires=" + expire.toGMTString() : "");
}

/**
 * Cookie 구하기
 */
function getCookie(uName) {

	var flag = document.cookie.indexOf(uName + '=');
	if (flag != -1) {
		flag += uName.length + 1;
		end = document.cookie.indexOf(';', flag);

		if (end == -1) end = document.cookie.length;
		return unescape(document.cookie.substring(flag, end));
	}
}

/**
 * 특정 폼의 모든 elements 들을 disable 처리하기
 */
function disableFormElements(form) {
	var c = form.elements;
	if (!c.length) return;
	for (var i = 0; i < c.length; i++) {
		c[i].disabled = true;
	}
}

/**
 * 특정 폼의 모든 elements 들을 enable 처리하기
 */
function enableFormElements(form) {
	var c = form.elements;
	if (!c.length) return;
	for (var i = 0; i < c.length; i++) {
		c[i].disabled = false;
	}
}

/**
 * yyyymm 스트링에 addmm 개월을 더한 년월을 리턴
 * parameter yyyymm: yyyymm 형식의 스트링 날짜
 * parameter addmm : 숫자 Type
 * return          : 년월(yyyymm) 스트링
 */
function toAddMonth(yyyymm, addmm) {
	var year = eval(yyyymm.substr(0, 4));
	var month = eval(yyyymm.substr(4, 2)) + year * 12;

	if (addmm == 0) {
		return yyyymm;
	}

	if ((month + addmm) % 12 == 0) {
		year = Math.floor((month + addmm) / 12) - 1;
		month = 12;
	}
	else {
		year = Math.floor((month + addmm) / 12);
		month = Math.abs((month + addmm) % 12);
	}

	return lpad(new String(year), 4, '0') + lpad(new String(month), 2, '0');
}

// Left 빈자리 만큼 padStr 을 붙인다.
function lpad(src, len, padStr) {
	var retStr = "";
	var padCnt = Number(len) - String(src).length;
	for (var i = 0; i < padCnt; i++) retStr += String(padStr);
	return retStr + src;
}

// Right 빈자리 만큼 padStr 을 붙인다.
function rpad(src, len, padStr) {
	var retStr = "";
	var padCnt = Number(len) - String(src).length;
	for (var i = 0; i < padCnt; i++) retStr += String(padStr);
	return src + retStr;
}

// AppException이 발생하였을 경우 에러핸들러를 호출한다.
function callAppErrorHandler(o) {
	try {
		errorHandle(o);
	} catch (e) {
	}
}


//해당폼의 패러미터구하기
function getParams(frm) {

	var params = "";
	if (frm != null) {
		for (var i = 0; i < frm.elements.length; i++) {
			if (params.length > 0) params += "&";
			if (frm.elements[i].type == "radio") {
				if (frm.elements[i].checked) {
					params += (frm.elements[i].name + "=" + frm.elements[i].value);
				}
			} else if (frm.elements[i].type == "select-one") {
				params += (frm.elements[i].name + "=" + frm.elements[i].value);
			} else {
				params += (frm.elements[i].name + "=" + frm.elements[i].value);
			}
		}
	}
	return params.replace("&&", "&");
}

function getSearchParams(frm) {

	var params = "";
	if (frm != null) {
		for (var i = 0; i < frm.elements.length; i++) {
			if (params.length > 0) params += "&";
			if (frm.elements[i].type == "radio") {
				if (frm.elements[i].checked) {
					params += ("s_" + frm.elements[i].name + "=" + frm.elements[i].value);
				}
			} else if (frm.elements[i].type == "select-one") {
				params += ("s_" + frm.elements[i].name + "=" + frm.elements[i].value);
			} else {
				params += ("s_" + frm.elements[i].name + "=" + frm.elements[i].value);
			}
		}
	}
	return params.replace("&&", "&");
}

//2013.05.11 추가 - dialog popup
function fn_popup_display(divName, width) {
	divName.dialog({
		autoOpen: false,
		show: { effect: "blind", duration: 500 }
	});
	divName.dialog("option", "width", width);
	divName.dialog("option", "resizable", false);
	divName.dialog("option", "draggable", true);
	divName.dialog("open");
}


function parseDateFormat(str) {
	var result;
	if (str.length == 8) {
		result = str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);
	} else {
		result = str;
	}
	return result;
}

function parseTimeFormat(str) {
	var result;
	if (str.length == 4) {
		result = str.substring(0, 2) + ":" + str.substring(2, 4);
	} else {
		result = str;
	}
	return result;
}

function getItem_sync_insupd(objID, itemName) {
	$.ajax({
		async: false,
		type: "POST",
		url: "/scheduler/comm/itemCode_insupd.do",
		dataType: "html",
		data: "itemName=" + itemName,
		success: function(msg) {
			objID.find('#' + itemName + ' select').html(msg);
		}
	});
}

function fn_enter(_func) {
	var e = window.event;
	if (!e || e.keyCode != "13") {
		return;
	}
	_func();
}

function fn_loding(name) {
	// ajax 실행 및 완료시 'Loading 이미지'의 동작을 컨트롤하자.
	$('#viewLoading').ajaxStart(function() {
		// 로딩이미지의 위치 및 크기조절	
		/*$('#viewLoading').css('position', 'absolute');
		$('#viewLoading').css('left', $("#"+name).offset().left);
		$('#viewLoading').css('top', $("#"+name).offset().top);
		$('#viewLoading').css('width', $("#"+name).css('width'));
		$('#viewLoading').css('height', $("#"+name).css('height'));*/
		//$(this).show();
		$(this).fadeIn(400);
	})
		.ajaxStop(function() {
			//$(this).hide();
			$(this).fadeOut(400);
		});
}
// 숫자 콤마를 찍어준다.
function numberWithCommas(x) {
	x = String(x);
	return x.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, "$1,");
}

/************************************************************************
* side menu F2 인터페이스 Quick Search
*************************************************************************/
document.onkeyup = function(e) {
	if (e.which == 113) {
		$("#sidesearch").focus();
	}
}

/************************************************************************
* 페이지 이동
*************************************************************************/
function fn_PageMove(in_menuId) {
	if (countdown) {
		countdown.stop();
	}
	$('#mainContent').empty();
	var in_menuObj = "";
	var url = "/scheduler/menu/selPageMove.do";
	var type = "html";
	var param = "in_menuId=" + in_menuId + "&in_menuObj=" + in_menuObj;
	ajaxCall(url, type, param, fn_setPageContents);

	function fn_setPageContents(data) {
		fn_PageTitle(in_menuId);
		$('#mainContent').html(data);
		fn_PageLoadInit();
	}
}

/************************************************************************
* PAGE MENU TITLE 
*************************************************************************/
function fn_PageTitle(menu_id) {
	$('.main-title').empty();	
	$('.sub-title').empty();
	
	var url = "/scheduler/menu/selectPageInfo.do";
	var type = "json";
	var param = "menu_id=" + menu_id;
	ajaxCall(url, type, param, fn_PageTitleResult);

	function fn_PageTitleResult(data) {
		var menuVo = data.singleData;
		var mainTitle = "<i class='nav-icon fas "+menuVo.menu_icon+"'></i>  "+menuVo.menu_nm;
		var subTitle =  
						 '<li class="breadcrumb-item">'
			            +'  	<a href="/scheduler/main.do">'
			            +'  		<i aria-hidden="true" class="fa fa-home"></i>'
			            +'  	</a>'
              			+'</li>'
						+'<li class="breadcrumb-item">'
						+'	<a href="javascript:void(0)">'
						+		menuVo.menu_nm
						+'	</a>'
						+'</li>';
		
		$('.main-title').append(mainTitle);
		$('.sub-title').append(subTitle);
	}
}

/************************************************************************
* Count down Function
*************************************************************************/
var countdown;
function fn_countdown(count_name, _fn_call) {
	countdown = $("#" + count_name).countdown360({
		radius: 10,
		seconds: 60,
		fontColor: '#FFFFFF',	//count Number
		//strokeStyle : '#007bff',	//count line
		fillStyle: '#00c0ef',	//background
		autostart: true,
		onComplete: function() {
			countdown.start();
			_fn_call();
		}
	});
}

/********************************************************************************
* Countdown reflash
********************************************************************************/
function fn_ag_reflash(_fn_call, inteval_time) {
	countdown.stop();
	if (inteval_time == "STOP") {
		return false;
	} else {
		countdown.settings.seconds = inteval_time;
		_fn_call();
		countdown.start();
	}
}

//page Common Set
function fn_PageLoadInit(){
	//다이얼 로그 창 Move
	$('.modal-dialog').draggable({
	    handle: ".modal-header"
	});
	//Page내 Class에서 공통함수 찾기
	const elementList = document.querySelectorAll('.common');
	elementList.forEach((item) =>{
		const element_id = item.id;
		const classes = item.classList;
		const selectElement = item.querySelector('select');
		var itemALL = "";
		var itemObj = new Object();
		
		if (classes.contains('select-all')) {
			itemALL = "Y";
		}
		
		if (classes.contains('select-notall')) {
			itemALL = "N";
		}
		
		if(element_id.length > 0){
			itemObj['itemName'] = element_id;
			itemObj['itemALL'] = itemALL;
			var url = "/scheduler/comm/itemCode.do";
			var type = "html";
			var param = "itemObj=" + encodeURIComponent(JSON.stringify(itemObj));
				
			ajaxCall(url, type, param, fn_itemCodeResult);
			function fn_itemCodeResult(data) {
				$('#' + selectElement.id).html(data);
			}
		}
	});
    //iCheck
    $('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
      checkboxClass: 'icheckbox_flat-green',
      radioClass   : 'iradio_flat-green'
    })
	$('.select2').select2();
}

/************************************************************************
* DATA TABLE Default Set List Version 3.5
*************************************************************************/
function dataTableGridNew(gridObj, gridOptions) {
    var grid_id = gridObj.grid_id;

	// ▶ 이미 DataTable이 초기화되어 있으면, 데이터만 업데이트 (파라미터 반영)
	if ($.fn.dataTable.isDataTable('#' + grid_id)) {
	    var table = $('#' + grid_id).DataTable();
		// 기존 체크박스 선택 해제: DataTable selection 초기화
        if (columnCheck) {
            table.rows().deselect();
        }
	    // 동적으로 파라미터 재설정
	    table.settings()[0].ajax.data = gridObj.param;
	    table.ajax.reload(null, false);  // 페이지 리셋 없이 데이터만 갱신
	    return;
	}

    var url                  = gridObj.url;
    var param                = gridObj.param;
    var columns              = gridObj.columns;
    var columnDefs           = gridObj.columnDefs;
    var columnCheck          = gridObj.columnCheck;    // 그리드에 체크박스 넣을지 유무
    var footerCallbackSetting = gridObj.footerCallback; // ★ footerCallback 정보 가져옴

    if (columnCheck) {
        columnDefs = [
            gridObj.columnDefs,
            {
                targets: 0,
                orderable: false,
                className: "text-center",
                checkboxes: {
                    selectRow: true,
                    selectCallback: function (nodes) {
                        $('input[type="checkbox"]', nodes).iCheck('update');
                    },
                    selectAllCallback: function (nodes) {
                        $('input[type="checkbox"]', nodes).iCheck('update');
                    }
                }
            }
        ];
    } else {
        columnDefs = gridObj.columnDefs;
    }

    // 기본 옵션 병합
    var defaultOptions = $.extend({
        ajax: {
            url: url,
            type: "POST",
            data: param
        },
		columns: columns,
		columnDefs: columnDefs,
		language: language,
        drawCallback: function () {
            $('#' + grid_id + ' input[type="checkbox"]').iCheck({
                checkboxClass: 'icheckbox_flat-blue'
            });
        },
        initComplete: function (settings, json) {
            // 데이터가 없을 때 cursorCols 옵션 제거
            if (!json || !json.data || json.data.length === 0) {
                delete defaultOptions.cursorCols;
            }
        },
        footerCallback: footerCallbackSetting ? function (row, data, start, end, display) {
            var api = this.api();
            Object.keys(footerCallbackSetting).forEach(function (colIdx) {
                var sum = api
                    .column(colIdx, { page: 'current' })
                    .data()
                    .reduce(function (a, b) {
                        var x = parseFloat(a) || 0;
                        var y = parseFloat(b) || 0;
                        return x + y;
                    }, 0);
                $(api.column(colIdx).footer()).html(sum);
            });
        } : null
    }, gridOptions);

	// DataTable 초기화
	var grid_table = $('#' + grid_id).DataTable(defaultOptions);

	// ▶ Checkbox event 핸들러 추가
	if (columnCheck) {
	    var container = grid_table.table().container();
	    $(container).on('ifChanged', '.dt-checkboxes-select-all input[type="checkbox"], .dt-checkboxes', function() {
	        var $el = $(this);
	        if ($el.closest('th').length) {
	            grid_table.column($el.closest('th'), { filter: 'applied' }).checkboxes.select(this.checked);
	        } else {
	            grid_table.cell($el.closest('td')).checkboxes.select(this.checked);
	        }
	    });
	}

	//그리드 커서
	if (gridOptions.cursorCols && gridOptions.cursorCols.length > 0) {
	    gridOptions.cursorCols.forEach(function (idx) {
	    $('#' + grid_id + ' tbody')
	        .on('mouseenter', 'td:nth-child(' + idx + ')', function () {
	            $(this).addClass('nav-link').css('cursor', 'pointer');
	        })
	        .on('mouseleave', 'td:nth-child(' + idx + ')', function () {
	            $(this).removeClass('nav-link').css('cursor', 'default');
	        });
	    });
	}
}

// Utility 함수 예시
function fn_formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

/************************************************************************
* DATA TABLE Default Set List Version 3.0
*************************************************************************/
function dataTableGrid(gridObj, gridOptions) {
    var grid_id = gridObj.grid_id;
    var url = gridObj.url;
    var param = gridObj.param;
    var columns = gridObj.columns;
    var columnDefs = gridObj.columnDefs;
    var columnCheck = gridObj.columnCheck;	//그리드에 체크박스 넣을지 유무
    //var columnDownload = gridObj.download;
	var footerCallbackSetting = gridObj.footerCallback; // ★ footerCallback 정보 가져옴

    if (columnCheck) {
        columnDefs = [
            gridObj.columnDefs,
            {
                targets: 0,
                orderable: false,
                className: "text-center",
                checkboxes: {
                    selectRow: true,
                    selectCallback: function (nodes) {
                        $('input[type="checkbox"]', nodes).iCheck('update');
                    },
                    selectAllCallback: function (nodes) {
                        $('input[type="checkbox"]', nodes).iCheck('update');
                    }
                }
            }
        ];
    } else {
        columnDefs = [gridObj.columnDefs];
    }

    var defaultOptions = {
        ajax: {
            url: url,
            type: "POST",
            data: param
        },
        columns: columns,
        columnDefs: columnDefs,
        language: language,
        drawCallback: function () {
            $('#' + grid_id + ' input[type="checkbox"]').iCheck({
                checkboxClass: 'icheckbox_flat-blue'
            });
        },
        initComplete: function (data) {
            if (data.json.system_code !== "0000") {
                fn_alertpop('', 'error', grid_id + " : " + data.json.syste_msg, "2000");
            }
        }
    };
	// ★ footerCallback이 있을 때만 설정 추가
	 if (footerCallbackSetting && footerCallbackSetting.sumColumns && footerCallbackSetting.sumColumns.length > 0) {
	     defaultOptions.footerCallback = function (row, data, start, end, display) {
	         var api = this.api();
			 // 쉼표(,)나 달러($) 같은 걸 제거해서 숫자로 변환 (Remove the formatting to get integer data for summation)
	         var intVal = function (i) {
	             return typeof i === 'string' ?
	                 i.replace(/[\$,]/g, '') * 1 :
	                 typeof i === 'number' ?
	                     i : 0;
	         };

	         footerCallbackSetting.sumColumns.forEach(function (colIdx) {
				// 합계(total)(computing column Total the complete result) 
	             var total = api.column(colIdx).data().reduce(function (a, b) {
	                 return intVal(a) + intVal(b);
	             }, 0);
				 // 합계를 구할 컬럼 번호 (4번째, 5번째 컬럼)
	             $(api.column(colIdx).footer()).html(fn_toNumber(total));
	         });
	     };
	 }

    $.ajax({
	        type: "POST",
	        url: "/scheduler/session/sessionchk.do",
	        dataType: "json",
	        async: false,
	        success: function (data) {
            	if (data.system_code === "0000") {

					// ★ 데이터가 없을 경우 cursorCols 옵션 제거
					if (gridOptions.cursorCols && Array.isArray(gridOptions.cursorCols) && data.data && data.data.length === 0) {
					    delete gridOptions.cursorCols;
					}
					
	                var options = $.extend(true, {}, defaultOptions, gridOptions);
	                var grid_table = $('#' + grid_id).DataTable(options);
					
					//행 이동을 허용하는 열 선택자
					if(gridOptions.rowReorder){
						// rowReorder 이벤트 추가
					   grid_table.on('row-reorder', function (e, details, changes) {
							var orderData = [];
							details.forEach(function (detail) {
								var table = $('#' + grid_id).dataTable();
							   	var row_position = table.fnGetPosition(detail.node);
						   		var row_data = table.fnGetData(row_position);
								orderData.push({
					               	in_menuSeq: row_data.menu_seq,
					               	in_parentId: row_data.parent_id,
					               	in_sortOrder: row_data.sort_order,
								   	in_newOrder: detail.newPosition,
									in_oldOrder: detail.oldPosition
					           	});
					       });
						   
						   if(orderData.length > 1){
					       		saveRowOrder(grid_id, orderData);
						   }
					   });
					}
				if (columnCheck) {
					//Checkbox 생성
					$(grid_table.table().container()).on('ifChanged', '.dt-checkboxes-select-all input[type="checkbox"]', function() {
						var col = grid_table.column($(this).closest('th'), { filter: 'applied' });
						col.checkboxes.select(this.checked);
					});
	
					$(grid_table.table().container()).on('ifChanged', '.dt-checkboxes', function() {
						var cell = grid_table.cell($(this).closest('td'));
						cell.checkboxes.select(this.checked);
					});
				};
				//그리드 커서
	            if (gridOptions.cursorCols && gridOptions.cursorCols.length > 0) {
	                gridOptions.cursorCols.forEach(function (idx) {
	                $('#' + grid_id + ' tbody')
	                    .on('mouseenter', 'td:nth-child(' + idx + ')', function () {
	                        $(this).addClass('nav-link').css('cursor', 'pointer');
	                    })
	                    .on('mouseleave', 'td:nth-child(' + idx + ')', function () {
	                        $(this).removeClass('nav-link').css('cursor', 'default');
	                    });
	                });
	            }
	        } else {
	            fn_alertpop(fn_logout, "error", "Session information is not. Please log in again. " + data.result_msg, "2000");
	        }
	    },
	    error: function (request) {
	        if (request.status === 404) {
	            fn_alertpop('', "error", "Error: 404 Not Found", "3000");
	        } else {
	            var err_param = 'error_msg=${request.statusText}&error_code=${request.status}&error_dtl=${request.responseText}';
	            fn_ajaxCall_Error(err_param, fn_ErrorContents);
				/*<div class="modal-backdrop fade show"></div>*/
	        }
	    }
    });
};

function saveRowOrder(grid_id, orderData) {
	
	var param = "orderObj=" + encodeURIComponent(JSON.stringify(orderData));
    $.ajax({
        type: "POST",
        url: "/scheduler/menu/saveMenuOrder.do",
        dataType: "json",
        data: param,
        success: function (response) {
            if (response.system_code === "0000") {
                fn_alertpop('', 'success', 'Menu order saved successfully!', "2000");
            } else {
                fn_alertpop('', 'error', 'Failed to save menu order: ' + response.result_msg, "2000");
            }
        },
        error: function (request) {
            fn_alertpop('', 'error', 'Error saving menu order: ' + request.statusText, "3000");
        }
    });
}

/************************************************************************
* DATA TABLE Option List
*************************************************************************/
/*
var tableOptions = {
    // 서버 사이드 처리를 활성화합니다. start, length 등 매개변수가 서버로 전송됩니다.
    'serverSide': true, 
    // 데이터 테이블 인터페이스에서 검색 박스를 비활성화합니다.
    'searching': false,
    // 테이블에 페이지네이션(페이징) 기능을 활성화합니다.
    'paging': true, 
    // 사용자 정의 버튼 렌더링을 비활성화합니다.
    'button': false,
    // 사용자가 페이지당 표시할 행 수를 변경하지 못하도록 설정합니다.
    'lengthChange': false,
    // 테이블 정보(예: "1에서 10까지 표시 중, 총 50개 항목")를 표시합니다.
    'info': true, 
    // 콘텐츠에 맞게 열 너비를 자동으로 조정합니다.
    'autoWidth': false,
    // 작은 화면에서도 테이블이 잘 표시되도록 반응형 레이아웃을 활성화합니다.
    'responsive': true,
    // DataTable을 삭제하고 다시 초기화할 수 있도록 허용합니다.
    'bDestroy': true, 
    // 데이터를 가져오는 동안 처리 표시기를 표시합니다.
    'processing': true,
    // 열 정렬 기능을 비활성화합니다.
    'ordering': false, 
    // 테이블의 초기 정렬을 설정합니다. 예: [[0, 'asc']]는 첫 번째 열을 오름차순으로 정렬.
    'order': order, 
    // 드래그 앤 드롭을 통해 행 재정렬을 활성화합니다.
    'rowReorder': true,
    // 행 재정렬에 사용할 열을 지정합니다.
    'cursorCols': [5], 
    // 고정 높이 테이블에 대해 스크롤 기능을 활성화합니다.
    'scrollY': "300px", // 세로 스크롤 높이를 설정합니다.
    // 많은 열을 가진 테이블에 대해 가로 스크롤을 활성화합니다.
    'scrollX': true, 
    // 기본적으로 페이지당 표시할 행 수를 지정합니다.
    'pageLength': 10,
    // 빈 테이블에 대한 기본 메시지를 정의합니다.
    'language': {
        'emptyTable': "테이블에 데이터가 없습니다",
        'loadingRecords': "로딩 중..."
    } 
};
*/

var language = {
	"emptyTable": "데이터가 없습니다",
	"lengthMenu": "페이지당 _MENU_ 개씩 보기",
	"info": "현재 _START_ - _END_ / _TOTAL_건",
	"infoEmpty": "데이터 없음",
	//"infoFiltered": "( _MAX_건의 데이터에서 필터링됨 )",
	"infoFiltered": "",
	"search": "내용 검색: ",
	"zeroRecords": "일치하는 데이터가 없습니다.",
	"loadingRecords": "로딩중...",
	"processing": "잠시만 기다려 주세요...",
	"paginate": {
		"next": "다음",
		"previous": "이전"
	}
};

/* 그리드 선택 리스트 */
function fn_SelectGridList(event_div, grid_id) {
	var rows = $("input[type=checkbox]:checked", grid_id.fnGetNodes());
	var rows_length = rows.length;
	var list = [];
	if (rows_length > 0) {
		if (confirm(event_div + "하시겠습니까??") == true) {
			rows.each(function() {
				list.push($(this).closest("tr").find("td:eq(1)").text());
			});
		} else {
			return false;
		}
	} else {
		alert(event_div + " 목록을 선택하세요.");
	}
	return list;
}

/*Export Excel, CSV, Print*/
function newexportaction(e, dt, button, config) {
	var self = this;
	var oldStart = dt.settings()[0]._iDisplayStart;
	dt.one('preXhr', function(e, s, data) {
		// Just this once, load all data from the server...
		data.start = 0;
		data.length = 2147483647;
		dt.one('preDraw', function(e, settings) {
			// Call the original action function
			if (button[0].className.indexOf('buttons-copy') >= 0) {
				$.fn.dataTable.ext.buttons.copyHtml5.action.call(self, e, dt, button, config);
			} else if (button[0].className.indexOf('buttons-excel') >= 0) {
				$.fn.dataTable.ext.buttons.excelHtml5.available(dt, config) ?
					$.fn.dataTable.ext.buttons.excelHtml5.action.call(self, e, dt, button, config) :
					$.fn.dataTable.ext.buttons.excelFlash.action.call(self, e, dt, button, config);
			} else if (button[0].className.indexOf('buttons-csv') >= 0) {
				$.fn.dataTable.ext.buttons.csvHtml5.available(dt, config) ?
					$.fn.dataTable.ext.buttons.csvHtml5.action.call(self, e, dt, button, config) :
					$.fn.dataTable.ext.buttons.csvFlash.action.call(self, e, dt, button, config);
			} else if (button[0].className.indexOf('buttons-pdf') >= 0) {
				$.fn.dataTable.ext.buttons.pdfHtml5.available(dt, config) ?
					$.fn.dataTable.ext.buttons.pdfHtml5.action.call(self, e, dt, button, config) :
					$.fn.dataTable.ext.buttons.pdfFlash.action.call(self, e, dt, button, config);
			} else if (button[0].className.indexOf('buttons-print') >= 0) {
				$.fn.dataTable.ext.buttons.print.action(e, dt, button, config);
			}
			dt.one('preXhr', function(e, s, data) {
				// DataTables thinks the first item displayed is index 0, but we're not drawing that.
				// Set the property to what it was before exporting.
				settings._iDisplayStart = oldStart;
				data.start = oldStart;
			});
			// Reload the grid with the original page. Otherwise, API functions like table.cell(this) don't work properly.
			setTimeout(dt.ajax.reload, 0);
			// Prevent rendering of the full data to the DOM
			return false;
		});
	});
	// Requery the server with the new one-time export settings
	dt.ajax.reload();
};

/*******************************************************
 * DATE Functions
 *******************************************************/
//var nowDate = new Date();

//console.log(nowDate.toString());
// Mon Aug 16 2021 19:53:03 GMT+0900 (한국 표준시)
//console.log(nowDate.toDateString());
// Mon Aug 16 2021
//console.log(nowDate.toLocaleString());
// 2021. 8. 16. 오후 7:53:03
//console.log(nowDate.toLocaleDateString());
// 2021. 8. 16.
//console.log(nowDate.toLocaleTimeString());
// 오후 7:53:03

function dateFormet(date, format) {
	//"yyyy-MM-dd hh:mm:ss"
	Date.prototype.formating = function() {
		var yyyy = this.getFullYear().toString();
		var MM = pad(this.getMonth() + 1, 2);
		var dd = pad(this.getDate(), 2);
		var hh = pad(this.getHours(), 2);
		var mm = pad(this.getMinutes(), 2)
		var ss = pad(this.getSeconds(), 2)

		var return_date = "";
		switch (format) {
			case "YYYYMMDDHHMMSS":
				return_date = yyyy + MM + dd + hh + mm + ss;
				break;
			case "YYYY-MM-DD HH:MM:SS":
				return_date = yyyy + '-' + MM + '-' + dd + ' ' + hh + ':' + mm + ':' + ss;
				break;
			case "YYYY-MM-DD":
				return_date = yyyy + '-' + MM + '-' + dd;
				break;
			case "HH:MM":
				return_date = hh + ':' + mm;
				break;
			case "HH:MM:SS":
				return_date = hh + ':' + mm + ':' + ss;
				break;
			default:
				break;
		}
		return return_date;
	};

	function pad(number, length) {
		var str = '' + number;
		while (str.length < length) {
			str = '0' + str;
		}
		return str;
	}
	// var nowDate = new Date();
	//console.log(nowDate);
	// Mon Aug 16 2021 19:56:50 GMT+0900 (한국 표준시)

	//console.log(nowDate.YYYYMMDDHHMMSS());
	// 20210816195650
}

//YYYY-MM-DD HH:MM:SS 포맷
//YYYY-MM-DD 포맷 + HH:MM:SS 포맷
function toDateFormat(date, format){
	const TIME_ZONE = 3240 * 10000;
	var return_date = "";
	switch (format) {
		/*case "YYYYMMDDHHMMSS":
			break;*/
		case "YYYY-MM-DD HH:MM:SS":
			return_date = new Date(+ date + TIME_ZONE).toISOString().split('T')[0] + ' ' + date.toTimeString().split(' ')[0];
			break;
		case "YYYY-MM-DD":  //qtoISOString() 메서드는 "YYYY-MM-DDTHH:mm:ss.sssZ" 형식
			return_date = new Date(+ date + TIME_ZONE).toISOString().split('T')[0];
			break;
		case "HH:MM":
			return_date = date.toTimeString().split(' ')[0];
			break;
		case "HH:MM:SS":
			return_date = date.toTimeString().split(' ')[0];
			break;
		default:
			break;
	}
	return return_date;
}


/**
 * 자바스크립트 Date 객체를 Time 스트링으로 변환
 * parameter date: JavaScript Date Object
 */

function toDateString(date) { //formatTime(date)
	var year = date.getFullYear();
	var month = date.getMonth() + 1; // 1월=0,12월=11이므로 1 더함
	var day = date.getDate()-1;

	if (("" + month).length == 1) { month = "0" + month; }
	if (("" + day).length == 1) { day = "0" + day; }

	return ("" + year + '-' + month + '-' + day);
}

/**
 * 자바스크립트 Date 객체를 Time 스트링으로 변환
 * parameter date: JavaScript Date Object
 */
function toTimeString_time(date) { //formatTime(date)
	var hour = date.getHours();
	var min = date.getMinutes();

	if (("" + hour).length == 1) { hour = "0" + hour; }
	if (("" + min).length == 1) { min = "0" + min; }

	return ("" + hour + ':' + min);
}

/**
 * 자바스크립트 Date 객체를 Time 스트링으로 변환
 * parameter date: JavaScript Date Object
 */
function asTimeString_time(date) { //formatTime(date)
	var hour = date.setHours(date.getHours() - 1);
	var min = date.getMinutes();

	if (("" + hour).length == 1) { hour = "0" + hour; }
	if (("" + min).length == 1) { min = "0" + min; }

	return ("" + hour + ':' + min);
}

/**
 * 자바스크립트 Date 객체를 Time 스트링으로 변환
 * parameter date: JavaScript Date Object
 */
function toDateTimeString(date) { //formatTime(date)
	var year = date.getFullYear();
	var month = date.getMonth() + 1; // 1월=0,12월=11이므로 1 더함
	var day = date.getDate();
	var hour = date.getHours();
	var min = date.getMinutes();

	if (("" + month).length == 1) { month = "0" + month; }
	if (("" + day).length == 1) { day = "0" + day; }
	if (("" + hour).length == 1) { hour = "0" + hour; }
	if (("" + min).length == 1) { min = "0" + min; }

	var reval = "" + year + "-" + month + "-" + day + " " + hour + ":" + min;
	return reval;
}

/**
 * 자바스크립트 Date 객체를 Time 스트링으로 변환
 * parameter date: JavaScript Date Object
 */
function toDateString(date) { //formatTime(date)
	var year = date.getFullYear();
	var month = date.getMonth() + 1; // 1월=0,12월=11이므로 1 더함
	var day = date.getDate();

	if (("" + month).length == 1) { month = "0" + month; }
	if (("" + day).length == 1) { day = "0" + day; }

	var reval = "" + year + "-" + month + "-" + day;
	return reval;
}

/**
 * 자바스크립트 숫자 포멧으로 변환
 * parameter number
 */
function fn_toNumber(data) {
	return data.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

function validate_empty_check(div, id, msg) {
	if (div.find(id).val() == "") {
		div.find(id).focus();
		fn_alertpop('', "error", msg + "을(를) 입력하세요." ,"2000")
		return false;
	}
}

function fn_validateForm2(formId) {
	var isValid = true;
	//Page내 Class에서 공통함수 찾기
	const elementList = document.querySelectorAll('#'+formId+' .required');

    for (const item of elementList) {
        const inputId = item.getAttribute('for');
        const inputElement = document.getElementById(inputId);
		const classes = inputElement.classList;
		console.log(inputId)
        if (inputElement && inputElement.value.trim() === "") {
        	var msg = "[ "+item.textContent+ " ] is required.";
            fn_alertpop('', "error", msg, "2000");
			
			
			// is-invalid 추가, is-valid 제거
            inputElement.classList.add("is-invalid");
            inputElement.classList.remove("is-valid");
            inputElement.focus();
            isValid = false;
            break;  // 루프 중단
        }else{
			console.log(inputElement+"===="+classes);
			if (classes.contains('is-invalid')) {
				inputElement.focus();
				isValid = false;
			}else{
				// is-valid 추가, is-invalid 제거
				inputElement.classList.add("is-valid");
				inputElement.classList.remove("is-invalid");	
			}
		}
    }
    return isValid;
}

/* jqueryObject를 받아 인풋값의 null & 공백사용 여부를 체크 */
function fn_validate_empty_check(jqueryObject, emptyCheckFlag, msg) {
	var value = jqueryObject.val();

	if (Array.isArray(value)) {
		var arrays = value;

		if (arrays.length == 1) {
			fn_alertpop('', "error", "해당 필드를 입력하세요.[" + msg + "]","2000")	
			return false;
		} else {
			for (var i = 1; i < arrays.length; i++) {

				var arrayValue = arrays[i];

				if (!arrayValue || arrayValue == "") {
					fn_alertpop('', "error", "해당 필드를 입력하세요.[" + msg + "]","2000")
					jqueryObject.focus();
					return false;
				}
			}
		}
	} else {
		if (!value || value == "") {
			fn_alertpop('', "error", "해당 필드를 입력하세요.[" + msg + "]","2000")
			jqueryObject.focus();
			return false;
		}
	}

	var pattern = /\s/g;

	//emptyCheckFlag Y인 경우 문자 내 공백 허용 안됨. EX) [A 인터페이스 입니다.] 허용 안함.  
	if (emptyCheckFlag == 'Y') {
		if (Array.isArray(value)) {
			var arrays = value;

			for (var i = 0; i < arrays.length; i++) {
				if (arrays[i].match(pattern)) {
					fn_alertpop('', "error", "해당 필드에 공백이 있습니다. [" + msg + "]" ,"2000")
					jqueryObject.focus();
					return false;
				}
			}
		} else {
			if (value.match(pattern)) {
				fn_alertpop('', "error", "해당 필드에 공백이 있습니다. [" + msg + "]" ,"2000")
				jqueryObject.focus();
				return false;
			}
		}
	}
}

function isEmptyChart(str) {

	if (typeof str == "undefined" || str == null || str == "")
		return true;
	else
		return false;
}

var empty =
{
	isEmpty: function(str) {
		return (str == '' || str == undefined || str == null || str == 'null');
	},

	isNotEmpty: function(str) {
		return !this.isEmpty(str);
	}
};

// 파라미터 구하기
function getJQParams(divName) {

	const inputs = divName.find('input, select, textarea');
	const params = new URLSearchParams();

	inputs.each(function () {
	    const input = $(this);
	    const id = input.attr('id');
	    let value;

	    if (input.attr('type') === 'checkbox') {
	        value = input.is(':checked') ? 'Y' : 'N';
	    } else {
	        value = input.val();
	    }
	    if (id) {
	        params.append(id, value);
	    }
	});
	
	return params.toString();
}

//체크박스 값 셋팅
function setCheckBoxYN(div, id) {
	if (div.find(id).is(':checked') == true) {
		div.find(id).val('Y');
	} else {
		div.find(id).val('N');
	}
}

//CHECK BOX Y/N 인지 판별하여 체크/언체크
function setCheckBox(param, div, check_id) {
	if (param === "Y") {
		$(check_id).iCheck('check');
		div.find(check_id).iCheck('check');
		div.find(check_id).attr("checked", true);
	} else {
		$(check_id).iCheck('uncheck');
		div.find(check_id).iCheck('uncheck');
		div.find(check_id).attr("checked", false);
	}
}

//page loading 시 검색 영역의 selectbox Item Setting
function getItem(itemName) {
	$.ajax({
		async: false,
		type: "POST",
		url: "/scheduler/comm/itemCode.do",
		dataType: "html",
		data: "itemName=" + itemName,
		success: function(msg) {
			$('#' + itemName + ' select').html(msg);
		}
	});
}

function getItem_select(objID, itemName) {
	$.ajax({
		type: "POST",
		url: "/scheduler/comm/itemCode.do",
		dataType: "html",
		data: "itemName=" + itemName,
		success: function(msg) {
			objID.find('#' + itemName + ' select').html(msg);
		}
	});
}

function getItem_insupd(objID, itemName) {
	$.ajax({
		type: "POST",
		url: "/scheduler/comm/itemCode_insupd.do",
		dataType: "html",
		data: "itemName=" + itemName,
		success: function(msg) {
			objID.find('#' + itemName + ' select').html(msg);
		}
	});
}

function fn_inputReset(item) {
	// Reset all input fields (text, hidden, checkbox)
	$(item+' input').each(function() {
	    if ($(this).attr('type') === 'checkbox') {
	        $(this).prop('checked', false); // Uncheck checkboxes
	    } else {
	        $(this).val(''); // Clear text and hidden inputs
	    }
	});
}

function fn_selectReset(item){
	// Reset all select elements
	$(item+' select').each(function() {
	    $(this).prop('selectedIndex', 0); // Reset to first option
	});
}

// 
function fn_resetForm(modal_name) {
    var resetDiv = modal_name;
    // input[type=text], input[type=hidden] 초기화
    resetDiv.find('input[type="text"], input[type="hidden"]').val('');
    // select 초기화
    resetDiv.find('select').prop('selectedIndex', 0);
    // checkbox 초기화 (unchecked)
    resetDiv.find('input[type="checkbox"]').prop('checked', false).iCheck('update'); // iCheck 사용하는 경우 update 필요
}

$(document).ready(function () {
    // 모든 .modal에 대해 ESC 키 활성화 + backdrop 클릭 방지
    $.fn.modal.Constructor.Default.keyboard = true;
    $.fn.modal.Constructor.Default.backdrop = 'static';
});