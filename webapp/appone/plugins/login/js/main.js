$(function() {
	'use strict';

  $('.form-control').on('input', function() {
	  var $field = $(this).closest('.form-group');
	  if (this.value) {
	    $field.addClass('field--not-empty');
	  } else {
	    $field.removeClass('field--not-empty');
	  }
	});
	
	// 새로고침(F5) 또는 페이지 새로 열기 시 로그인 화면으로 이동
	$(window).on("keydown", function(event) {
		if (event.keyCode === 116) { // F5 키
	    	event.preventDefault();
	    	window.location.href = "login.do";
		}
	});

	$(window).on("beforeunload", function() {
		window.location.href = "login.do";
	});
});