<!doctype html>
<html>
<%@ page pageEncoding="UTF-8"%>
	<head>
	  	<title>IPSCORP</title>
	    <meta charset="utf-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	    <%@ include file="/appone/plugins/login/js/login_js.jsp" %>
		<%@ include file="/appone/plugins/login/css/login_css.jsp" %>
	</head>
<body>
  <div class="d-lg-flex half" id="log_container">
    <div class="bg order-1 order-md-2" style="background-image: url('/scheduler/appone/plugins/login/images/bg_login.png'); width: 80%;"></div>
    <div class="contents order-2 order-md-1">
      <div class="container">
        <div class="row align-items-center justify-content-center">
          <div class="col-md-7 text-center">
            <div class="mb-4">
           		<img class="logo-img" alt="" src="/scheduler/appone/plugins/login/images/ipslogo2.png">
            </div>
              <div class="form-group first">
                <label for="ID">ID</label>
                <input type="text" class="form-control" id="in_userId"
                 onkeydown="javascript:fn_enter(chkLogin);" autocomplete="off">
                   <small id="idError" class="form-text text-danger"></small>
              </div>
              <div class="form-group last mb-3">
                <label for="password">Password</label>
                <input type="password" class="form-control" id="in_userPwd" onkeydown="javascript:fn_enter(chkLogin);">
                  <small id="pwdError" class="form-text text-danger"></small>
              </div>
              <button type="button" class="btn btn-primary btn-block" id="btn_login" value="Login">Sing in</button>
              <div class="d-flex mb-5 align-items-center">
                <!-- <label class="control control--checkbox mb-0"><span class="caption">Remember me</span>
                  <input type="checkbox" id="remember"/>
                  <div class="control__indicator"></div>
                </label> -->
                <span><a href="register.do" class="forgot-pass" id="register">Sing up</a></span> 
                <span class="ml-auto"><a href="forgotPassword.do" class="forgot-pass" id="forgotPassword">Forgot Password</a></span>
              </div>
          </div>
        </div>
      </div>
    </div>
</div>
<script type="text/javascript" charset="utf-8">

	var $login_View = $('#log_container');
	var cookieStoreID = "USR_ID";
	// 이벤트 핸들러와 쿠키 등록
	function doInit() {
		$login_View.find('#in_userId').val("");
		$login_View.find('#in_userPwd').val("");
		
		var temp_login_id	= $.cookie('temp_login_id');
		var temp_login_pwd	= $.cookie('temp_login_pwd');
		
	    if(temp_login_id != undefined) {
	    	$login_View.find('#in_userId').val(temp_login_id);
	    	$login_View.find('#in_userPwd').val(temp_login_pwd);
	    	$login_View.find("#remember").prop("checked",true);
	    } else {
	    	$login_View.find('#in_userId').focus();
	    }
		
		/* Login 버튼 클릭 이벤트 */
		$login_View.on('click', '#btn_login',function() {
			chkLogin();
		});
	}
	
	/* 쿠키 세팅 */
  	function getCookie(key) {	
		var cook 	= document.cookie + ";",
			idx 	= cook.indexOf(escape(key), 0),
			val 	= "";
		
		if( idx != -1 ) {
			cook 	= cook.substring(idx, cook.length);
			begin 	= cook.indexOf("=", 0) + 1;
			end 	= cook.indexOf(";", begin);
			val 	= unescape( cook.substring(begin, end) );
		}
		
		return val;
	}
	
	/* 로그인 버튼 클릭시 로그인 요청 */
	function chkLogin() {
		var $id = $login_View.find('#in_userId');
		var $password = $login_View.find('#in_userPwd');

	    // 필수 입력란 체크
/* 	    if ($id.val().trim() === "") {
	        alert("User ID를 입력하세요.");
	        $id.focus();
	        return;
	    } */

/* 	    if ($password.val().trim() === "") {
	        alert("Password를 입력하세요.");
	        $password.focus();
	        return;
	    } */
		/* 로그인 페이지 -> 로그인 -> 벨류데이션 체크 */
		var url   		= "/scheduler/login/chkUser.do",
			type  		= "json";
			param 		= "in_userId="+$id.val(),
		
		ajaxCall(url, type, param, doLogin, false);
			
		// 로그인 요청 결과를 받아서 처리
		function doLogin(data) {
			var result = data.result;
			
			var jsonObj = new Object();
			jsonObj['in_userId'] = $id.val();
			jsonObj['in_userPwd'] = $password.val();
			
			var url = "/scheduler/login/login.do";
			var dataType = "json";
			var dataParam = "in_userId="+$id.val()+"&in_userPwd="+$password.val()+"&chkSaveId="+result;
			
			ajaxCall(url, dataType, dataParam, fn_loginResult, false);

			function fn_loginResult(data){
				var resultCode	= data.result_code;
				var resultMsg	= data.result_msg;
				if(resultCode == 'S'){
					if($login_View.find('#remember').prop("checked")){
						$.cookie('temp_login_id',	$id.val());
						$.cookie('temp_login_pwd',	$password.val());
					} else {
						$.removeCookie('temp_login_id');
						$.removeCookie('temp_login_pwd');
					}
					document.location.href='/scheduler/main.do';
				}else{
					showAlert("error", resultMsg,3000);
					return;
				}
			}
		}
	}
	
	function inputFocus() {
		login_form.in_userId.focus();
	}
	
	function fn_enter(_func) {
		var e = window.event;
		if (!e || e.keyCode != "13") {
			return;
		}
		_func();
	}
	$(document).ready(function () {
	  	$("#username, #password").attr("autocomplete", "off");
	  	$("#password").attr("autocomplete", "new-password");
	  	
	});
	doInit();

	  function validateInputRealtime(inputId, errorId, label) {
	    var value = document.getElementById(inputId).value.trim();
	    var errorElem = document.getElementById(errorId);

	    if (!isPasswordValid(value)) {
	      errorElem.innerText = label + '는 영문 대/소문자, 숫자,특수 문자 입력 가능합니다.';
	      return false;
	    } else {
	      errorElem.innerText = '';
	      return true;
	    }
	  }

	  function validateAll() {
	    var idValid = validateInputRealtime('in_userId', 'idError', '아이디');
	    var pwdValid = validateInputRealtime('in_userPwd', 'pwdError', '비밀번호');
	    return idValid && pwdValid;
	  }

	  // 로그인 버튼 클릭 시 전체 검증 후 로그인 실행
	  document.getElementById('btn_login').addEventListener('click', function() {
	    //if (validateAll()) {
	      chkLogin(); // 기존 로그인 함수 호출
	    //}
	  });

	  // 키보드 입력 시 실시간 검증
	  document.getElementById('in_userId').addEventListener('keyup', function() {
	    validateInputRealtime('in_userId', 'idError', '아이디');
	  });
	  document.getElementById('in_userPwd').addEventListener('keyup', function() {
	    validateInputRealtime('in_userPwd', 'pwdError', '비밀번호');
	  });
</script>
<style>
.d-lg-flex {
        display: -webkit-box !important;
        display: -ms-flexbox !important;
        display: flex !important;
        flex-direction: row-reverse;
        flex-wrap: nowrap;
    }
</style>
  </body>
</html>

