<%@ include file="/appone/plugins/system/js/jsLink.jsp" %>
<%@ include file="/appone/plugins/system/css/cssLink.jsp" %>
<style>
.img {
	background-size: cover;
	background-repeat: no-repeat;
	background-position: center center;
}

.login-wrap {
	position: relative;
	color: rgba(255, 255, 255, 0.9);
}

.login-wrap h3 {
	font-weight: 300;
	color: #fff;
}

.login-wrap .social {
	width: 100%;
}

.login-wrap .social a {
	width: 100%;
	display: block;
	border: 1px solid rgba(255, 255, 255, 0.4);
	color: #000;
	background: #fff;
}

.login-wrap .social a:hover {
	background: #000;
	color: #fff;
	border-color: #000;
}

.form-group {
	position: relative;
}

.field-icon {
	position: absolute;
	top: 50%;
	right: 15px;
	-webkit-transform: translateY(-50%);
	-ms-transform: translateY(-50%);
	transform: translateY(-50%);
	color: rgba(255, 255, 255, 0.9);
}

.form-control_gl {
	background: transparent;
	border: none;
	height: 50px;
	width: 100%;
	color: white !important;
	border: 1px solid transparent;
	background: rgba(255, 255, 255, 0.08);
	border-radius: 40px;
	padding-left: 20px;
	padding-right: 20px;
	-webkit-transition: 0.3s;
	-o-transition: 0.3s;
	transition: 0.3s;
}

@media ( prefers-reduced-motion : reduce) {
	.form-control_gl {
		-webkit-transition: none;
		-o-transition: none;
		transition: none;
	}
}

.form-control_gl::-webkit-input-placeholder {
	/* Chrome/Opera/Safari */
	color: rgba(255, 255, 255, 0.8) !important;
}

.form-control_gl::-moz-placeholder {
	/* Firefox 19+ */
	color: rgba(255, 255, 255, 0.8) !important;
}

.form-control_gl:-ms-input-placeholder {
	/* IE 10+ */
	color: rgba(255, 255, 255, 0.8) !important;
}

.form-control_gl:-moz-placeholder {
	/* Firefox 18- */
	color: rgba(255, 255, 255, 0.8) !important;
}

.form-control_gl:hover, .form-control_gl:focus {
	background: transparent;
	outline: none;
	-webkit-box-shadow: none;
	box-shadow: none;
	border-color: rgba(255, 255, 255, 0.4);
}

.form-control_gl:focus {
	border-color: rgba(255, 255, 255, 0.4);
}

textarea.form-control_gl {
	height: inherit !important;
}

.btn_cilcle {
	cursor: pointer;
	border-radius: 40px;
	-webkit-box-shadow: none !important;
	box-shadow: none !important;
	font-size: 15px;
	text-transform: uppercase;
}

.btn_cilcle:hover, .btn_cilcle:active, .btn_cilcle:focus {
	outline: none;
}

.btn_cilcle.blue {
	background: #4da0d1 !important;
	border: 1px solid #4da0d1 !important;
	color: #fff !important;
}

.d-md-flex {
	display: -webkit-box !important;
	display: -ms-flexbox !important;
	display: flex !important;
}

@media ( min-width : 768px) {
	.text-md-left {
		text-align: left !important;
	}
	.text-md-right {
		text-align: right !important;
	}
	.text-md-center {
		text-align: center !important;
	}
}

.text-center {
  text-align: center !important; }
  
.w-50 {
  width: 100% !important; }
</style>