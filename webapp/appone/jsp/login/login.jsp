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
<body class="login-page">
  <div class="login-shell" id="log_container">
    <section class="login-visual" aria-hidden="true">
      <div class="login-visual__content">
        <span class="login-visual__eyebrow">IPSCORP Scheduler</span>
        <h1 class="login-visual__title">시장 흐름과 업무 실행을 한 화면에서 관리합니다.</h1>
        <p class="login-visual__copy">
          로그인 후 일정, 분석, 운영 기능을 데스크톱과 모바일 환경에서 동일하게 이어서 사용할 수 있습니다.
        </p>
      </div>
    </section>

    <main class="login-panel">
      <div class="login-card">
        <img class="logo-img login-logo" alt="IPSCORP" src="/scheduler/appone/plugins/login/images/ipslogo2.png">
        <h2 class="login-title">Sign in</h2>
        <p class="login-subtitle">
          계정 정보를 입력하면 바로 메인 화면으로 이동합니다.
        </p>

        <div class="login-field form-group">
          <label for="in_userId">ID</label>
          <input type="text" class="form-control" id="in_userId"
            onkeydown="javascript:fn_enter(chkLogin);" autocomplete="off" inputmode="text">
          <small id="idError" class="form-text text-danger"></small>
        </div>

        <div class="login-field form-group">
          <label for="in_userPwd">Password</label>
          <input type="password" class="form-control" id="in_userPwd"
            onkeydown="javascript:fn_enter(chkLogin);" autocomplete="current-password">
          <small id="pwdError" class="form-text text-danger"></small>
        </div>

        <button type="button" class="btn btn-primary login-submit" id="btn_login" value="Login">Sign in</button>

        <div class="login-links">
          <a href="register.do" class="forgot-pass" id="register">Sign up</a>
          <a href="forgotPassword.do" class="forgot-pass" id="forgotPassword">Forgot Password</a>
        </div>
      </div>
    </main>
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
	  	$("#in_userId").attr("autocomplete", "username");
	  	$("#in_userPwd").attr("autocomplete", "current-password");
	  	
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

	  // 키보드 입력 시 실시간 검증
	  document.getElementById('in_userId').addEventListener('keyup', function() {
	    validateInputRealtime('in_userId', 'idError', '아이디');
	  });
	  document.getElementById('in_userPwd').addEventListener('keyup', function() {
	    validateInputRealtime('in_userPwd', 'pwdError', '비밀번호');
	  });
</script>
<style>
.login-page {
  min-height: 100vh;
  margin: 0;
  background:
    radial-gradient(circle at top left, rgba(15, 93, 184, 0.16), transparent 32%),
    linear-gradient(135deg, #eef5ff 0%, #f7fbff 42%, #ffffff 100%);
}

.login-shell {
  min-height: 100vh;
  display: flex;
}

.login-visual {
  flex: 1 1 56%;
  min-height: 360px;
  position: relative;
  isolation: isolate;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  padding: clamp(32px, 5vw, 64px);
  background-image: url('/scheduler/appone/plugins/login/images/bg_login.png');
  background-size: cover;
  background-position: center;
}

.login-visual::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(9, 41, 84, 0.08) 0%, rgba(9, 41, 84, 0.14) 52%, rgba(9, 41, 84, 0.26) 100%);
  pointer-events: none;
  z-index: 0;
}

.login-visual::after {
  content: "";
  position: absolute;
  inset: auto 0 0 0;
  height: 42%;
  background: linear-gradient(180deg, rgba(9, 41, 84, 0) 0%, rgba(9, 41, 84, 0.42) 100%);
  pointer-events: none;
  z-index: 0;
}

.login-visual__content {
  position: relative;
  z-index: 1;
  max-width: 520px;
  color: #ffffff;
}

.login-visual__eyebrow {
  display: inline-block;
  margin-bottom: 16px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.88);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-visual__title {
  margin: 0 0 16px;
  color: #ffffff;
  font-size: clamp(32px, 4vw, 52px);
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.03em;
}

.login-visual__copy {
  margin: 0;
  max-width: 440px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 16px;
  line-height: 1.7;
}

.login-panel {
  flex: 0 0 min(520px, 44%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 28px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: clamp(28px, 4vw, 40px);
  border: 1px solid rgba(12, 66, 121, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 64px rgba(19, 62, 118, 0.16);
}

.login-logo {
  display: block;
  width: clamp(84px, 10vw, 118px);
  margin: 0 auto 24px;
}

.login-title {
  margin: 0;
  color: #123252;
  font-size: clamp(30px, 3vw, 36px);
  font-weight: 700;
  text-align: center;
}

.login-subtitle {
  margin: 12px 0 28px;
  color: #607080;
  font-size: 15px;
  line-height: 1.7;
  text-align: center;
}

.login-field {
  margin-bottom: 18px;
}

.login-field label {
  position: static;
  display: block;
  margin-bottom: 8px;
  color: #17406f;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.02em;
  transform: none;
}

.login-field .form-control {
  height: 56px;
  padding: 0 18px;
  border: 1px solid #d7e1ee;
  border-radius: 16px;
  background: #f8fbfe;
  font-size: 15px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.login-field .form-control:focus {
  border-color: #1f78d1;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(31, 120, 209, 0.12);
}

.login-field small {
  display: block;
  min-height: 20px;
  margin-top: 6px;
  padding-left: 4px;
}

.login-submit {
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f5db8, #1e88e5);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.01em;
  box-shadow: 0 18px 34px rgba(30, 136, 229, 0.24);
}

.login-submit:hover,
.login-submit:focus {
  background: linear-gradient(135deg, #0d56ab, #1976d2);
}

.login-links {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.login-links .forgot-pass {
  color: #0f5db8;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

.login-links .forgot-pass:hover {
  color: #0b468c;
  text-decoration: underline;
}

@media (max-width: 991.98px) {
  .login-shell {
    flex-direction: column;
  }

  .login-visual {
    min-height: clamp(260px, 42vh, 420px);
    padding: 28px 22px 84px;
  }

  .login-panel {
    flex: 1 1 auto;
    padding: 0 18px 28px;
    margin-top: -56px;
  }

  .login-card {
    position: relative;
    z-index: 1;
  }
}

@media (max-width: 575.98px) {
  .login-visual {
    padding: 24px 18px 72px;
  }

  .login-visual__copy {
    font-size: 14px;
  }

  .login-panel {
    margin-top: -40px;
    padding: 0 14px 22px;
  }

  .login-card {
    padding: 24px 20px;
    border-radius: 22px;
  }

  .login-field .form-control,
  .login-submit {
    height: 52px;
  }

  .login-links {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
  </body>
</html>
