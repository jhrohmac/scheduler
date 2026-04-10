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
    <div class="forgot-password-container">
      <img src="/scheduler/appone/plugins/login/images/ipslogo.png" alt="Logo" class="container-logo-img">
      <h3 class="text-center">Forgot Password</h3>
      <p class="text-center">Enter your email to reset your password.</p>
      <div id="forgotPasswordForm">
        <div class="mb-3">
        	<label for="in_userId" class="form-label required">User ID</label>
            <input type="text" class="form-control" id="in_userId" 
            	onkeydown="javascript:fn_enter(fn_registerUserCheck);" autocomplete="off" placeholder="User ID">
            	<small id="idError" class="form-text text-danger"></small>
        </div>
        <div class="mb-3">
          <label for="email" class="form-label required">User Email</label>
          <input type="email" class="form-control" id="in_userEmail" name="in_userEmail" 
          onkeydown="javascript:fn_enter(fn_registerUserCheck);" autocomplete="off" placeholder="User Email">
          <small id="emailError" class="form-text text-danger"></small> 
        </div>
        <button type="button" class="btn btn-primary w-100" id="btn_resetPwd">Reset Password</button>
      </div>
      <p class="text-center mt-3">
        <a href="login.do">Back to Login</a>
      </p>
    </div>
  </body>
  <script type="text/javascript">

	var forgotPws = $(".forgot-password-container");

	function fn_registerUserCheck(){
    	var param = getJQParams(forgotPws);
		var type = "json";
		var url = "/scheduler/register/registerUserCheck.do";
		ajaxCall(url, type, param, fn_registerUserCheckResult, false);
		function fn_registerUserCheckResult(data){
			if (data.result_code == "0000") {
				fn_resetPassword();
			} else {
				showAlert("error", data.result_msg,"2000");
				return false;
			}
		}
	}
	function fn_resetPassword(){
		showConfirm(
				  'info',
				  '패스워드를 리셋하시겠습니까?',
				  '',
				  '리셋',
				  '취소'
				);
		var param = getJQParams(forgotPws);
		var type = "json";
		var url = "/scheduler/register/resetPassword.do";
		ajaxCall(url, type, param, fn_resetPasswordResult, false);
		function fn_resetPasswordResult(data){
			console.log(data);
			if (data.result_code != "0000") {
				showAlert("error", data.result_msg,2000)	
			} else {
				showAlert("success", data.result_msg,3000)
			}
		}
	};
	
/* 	$(document).ready(function () {
	  // 실시간 검증
	  $('#in_userId').on('keyup', function() {
	    validateRealtimeForgot('in_userId', 'idError', 'id');
	  });
	  $('#in_userEmail').on('keyup', function() {
	    validateRealtimeForgot('in_userEmail', 'emailError', 'email');
	  });
	
	  // 버튼 클릭 시 전체 검증 → 기존 fn_registerUserCheck 호출
	  $('#btn_resetPwd').on('click', function() {
	    var okId    = validateRealtimeForgot('in_userId',    'idError',    'id');
	    var okEmail = validateRealtimeForgot('in_userEmail', 'emailError', 'email');
	    if (okId && okEmail) {
	      fn_registerUserCheck();  // 기존 로직
	    }
	  });
	}); */
		
  </script>
</html>
