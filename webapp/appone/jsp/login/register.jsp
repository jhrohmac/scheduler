<!doctype html>
<html lang="en">
<%@ page pageEncoding="UTF-8"%>
  <head>
    <title>Register | IPSCORP</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <%@ include file="/appone/plugins/login/js/login_js.jsp" %>
	<%@ include file="/appone/plugins/login/css/login_css.jsp" %>
  </head>
  
<body>
    <div class="register-container">
      <img src="/scheduler/appone/plugins/login/images/ipslogo.png" alt="Logo" class="container-logo-img">
      <h3 class="text-center">New Members</h3>
      
      <div id="registerForm">
        <div class="mb-3">
          <label for="in_userName" class="form-label required">Username</label>
          <input type="text" class="form-control" id="in_userName" name="in_userName"  autocomplete="off">
          <small id="nameError" class="form-text text-danger"></small>  
        </div>
        
        <div class="mb-3">
          <label for="in_userId" class="form-label required">User ID</label>
          <input type="text" class="form-control" id="in_userId" name="in_userId" autocomplete="off">
          <small id="idError" class="form-text text-danger"></small>  
        </div>
        
        <div class="mb-3">
          <label for="in_userEmail" class="form-label required">Email</label>
          <input type="email" class="form-control" id="in_userEmail" name="in_userEmail"  autocomplete="off">
          <small id="emailError" class="form-text text-danger"></small>
        </div>
        
        <div class="mb-3">
          <label for="in_userPwd" class="form-label required">Password</label>
          <input type="password" class="form-control" id="in_userPwd" name="in_userPwd"  autocomplete="new-password">
           <small id="pwdError" class="form-text text-danger"></small>  
        </div>

        <div class="mb-3">
          <label for="in_confirmPassword" class="form-label required">Confirm Password</label>
          <input type="password" class="form-control" id="in_confirmPassword" name="in_confirmPassword" autocomplete="new-password">
          <small id="confirmError" class="form-text text-danger"></small>
        </div>

        <button type="button" id ="btn_save" class="btn btn-primary w-100">Sign Up</button>
      </div>

      <p class="text-center mt-3">
        Already have an account? <a href="login.do">Log in</a>
      </p>
    </div>

    <!-- jQuery for Password Matching -->
	<script>
		function fn_duplicateCheck(){
			var param = getJQParams($("#registerForm"));
			var type = "json";
			var url = "/scheduler/userSetting/selectDuplicateUser.do";
			ajaxCall(url, type, param, fn_duplicateResult, false);
			function fn_duplicateResult(data){
				if (data.result_code != "0000") {
					showAlert("error", data.result_msg, 2000);
				} else {
					showAlert("success", data.result_msg, 1500, fn_save);
				}
			}
		}
		/* 코드 신규 등록 및 수정 */ 
		function fn_save(){
			showConfirm(
					  'info',
					  '신규 등록하시겠습니까?',
					  '',
					  '등록',
					  '취소'
					);
			var param = getJQParams($("#registerForm"));
			var type = "json";
			var url = "/scheduler/register/registerUser.do";
			ajaxCall(url, type, param, fn_saveResult, false);
			function fn_saveResult(data){
				if (data.result_code != "0000") {
					showAlert("error", data.result_msg, 2000);
				} else {
					showAlert("error", data.result_msg, 2000, fn_login);
				}
			}
		}
	
	  $(document).ready(function(){
	    // 실시간 검증 바인딩
	    $('#in_userName').on('keyup',           function(){ validateRealtimeReg('in_userName',      'nameError',    'text');    });
	    $('#in_userId').on('keyup',             function(){ validateRealtimeReg('in_userId',        'idError',      'id');      });
	    $('#in_userEmail').on('keyup',          function(){ validateRealtimeReg('in_userEmail',     'emailError',   'email');   });
	    $('#in_userPwd').on('keyup',            function(){ validateRealtimeReg('in_userPwd',       'pwdError',     'pwd');     });
	    $('#in_confirmPassword').on('keyup',    function(){ validateRealtimeReg('in_confirmPassword','confirmError','confirm'); });

	    // 가입 버튼 클릭 시
	    $('#btn_save').on('click', function(){
	      var ok1 = validateRealtimeReg('in_userName',      'nameError',    'text');
	      var ok2 = validateRealtimeReg('in_userId',        'idError',      'id');
	      var ok3 = validateRealtimeReg('in_userEmail',     'emailError',   'email');
	      var ok4 = validateRealtimeReg('in_userPwd',       'pwdError',     'pwd');
	      var ok5 = validateRealtimeReg('in_confirmPassword','confirmError','confirm');
	      if (ok1 && ok2 && ok3 && ok4 && ok5) {
	    	  fn_duplicateCheck();  // 기존 fn_duplicateCheck → fn_save 호출
	      }
	    });
	  });
	</script>
  </body>
  <style>
  /* ① 팝업 전체 크기/여백/폰트 크기 조정 */
.swal2-popup {
  font-size: 0.9rem;       /* 전반적인 폰트 크기 */
  padding: 1rem;           /* 팝업 여백 */
}

/* ③ 제목 크기 조정 */
.swal2-popup .swal2-title {
  font-size: 1.1rem !important;
  margin: .5rem 0;
}

/* ④ 본문(텍스트) 크기 조정 */
.swal2-popup .swal2-content {
  font-size: 0.9rem;
  margin-bottom: .8rem;
}

/* ⑤ 버튼 크기 및 여백 조정 */
.swal2-popup .swal2-styled {
  font-size: 0.85rem;      /* 버튼 텍스트 크기 */
  padding: .4em 1em;       /* 위아래 패딩:0.4em, 좌우:1em */
  margin: 0 .3em;          /* 버튼간 간격 */
}
  
  </style>
</html>
