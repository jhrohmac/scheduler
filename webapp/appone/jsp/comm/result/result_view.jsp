<%@ page contentType="text/html; charset=utf-8" %>

var result_code = '${RESULT_CODE}';
var result_msg = '${RESULT_MSG}';
var result_icon = "error";

if(result_code =="0000"){
	result_icon = "success";
}else if(result_code == "0523"){
	result_icon = "error";
}

$(function() {
	var Toast = Swal.mixin({
	  toast: true,
	  position: 'center',
	  showConfirmButton: false,
	  timer: 1500
	});
	Toast.fire({
	  icon: result_icon,
	  title: "'${RESULT_MSG}'"
	});
	
	//SESSION FAIL
	if(result_code == "0523" || result_code == "9999"){
		setTimeout(() => fn_logout(), 2000);
	}
});
