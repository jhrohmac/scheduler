<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ page import="org.apache.taglibs.standard.tag.rt.core.IfTag" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="/appone/plugins/system/js/jsLink.jsp" %>
<%@ include file="/appone/plugins/system/css/cssLink.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>Scheduler System</title>
</head>
<link rel="stylesheet" href="/scheduler/appone/plugins/system/css/scheduler-custom.css">

<body class="hold-transition sidebar-mini scheduler-shell-page" >
	<div class="wrapper scheduler-app" id="main_View">
 		<!-- Navbar -->
		<nav class="main-header navbar navbar-expand navbar-white navbar-light scheduler-topbar">
			<!-- Left navbar links -->
			<ul class="navbar-nav scheduler-topbar__group scheduler-topbar__group--left">
				<li class="nav-item">
					<a class="nav-link" data-widget="pushmenu" href="#" role="button">
						<i class="fas fa-bars"></i> 
					</a>
				</li>
				<li class="nav-item d-none d-sm-inline-block">
					<a href="/scheduler/main.do" class="nav-link">Home</a>
				</li>
			</ul>
			<!-- Right navbar links -->
			<ul class="navbar-nav ml-auto scheduler-topbar__group scheduler-topbar__group--right">
				<!-- [SESSION] 타임아웃 표시 (최소 수정) -->
                <li class="nav-item d-flex align-items-center pr-2" id="nav_session_timer" title="세션 남은 시간 / 설정값">
                    <i class="far fa-clock mr-1" aria-hidden="true"></i>
                    <span class="text-muted small">
                        <span id="sessionCountdown">--:--</span>
                        <span id="sessionTimeoutMinutesLabel" class="ml-1"></span>
                    </span>
                </li>
				
				<li class="dropdown user user-menu d-flex align-items-center" >
					<a class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
		              	<img src="/scheduler/appone/plugins/dist/img/avatar5.png" 
		              		id="navbar_avatar" class="user-image img-circle img-bordered-sm">
		              	<span class="hidden-xs header-user-info" id="pro_user_nm"></span>
		            </a>
		            <ul class="dropdown-menu">
						<li class="user-header">
							<div class="text-center">
								<span class="profile-badge">
									<i class="fas fa-pen"></i>
								</span>
								<img class="profile-user-img img-fluid img-circle" id="profile_avatar"
									src="/scheduler/appone/plugins/dist/img/avatar5.png">
							</div>
							<h5 class="profile-username text-center mb-1"></h5>
							<small class="profile-Email"></small>
						</li>
						<li class="user-body p-2">
				        	<div class="row no-print">
				            	<div class="col-3 float-right">
					            	<a class="btn btn-app bg-info" id="app_orgChart">
					            		<i class="fas fa-solid fa-sitemap"></i> 조직도
					                </a>
				                </div>
				                <div class="col-3 float-right">
					                <a class="btn btn-app bg-info" id="app_users">
					                  <span class="badge bg-danger">1</span>
					                  <i class="fas fa-users"></i> Users
					                </a>
				                </div>
				                <div class="col-3 float-right">
					                <a class="btn btn-app bg-info" id="app_profile">
					                  <span class="badge bg-danger">11</span>
					                  <i class="fas fa-address-card"></i> Profile
					                </a>
				                </div>
				                <div class="col-3">
					                <a class="btn btn-app bg-info" id="app_setting">
					                  <i class="fas fa-gear"></i>Setting
					                </a>
				                </div>
				          	</div>
						</li>
						<li class="user-body p-2">
							<div class="row no-print">
						   		<div class="col-12">
						     		<a id="logout" rel="noopener" target="_blank" class="btn btn-sm float-right">
						     		<i class="fa-solid fa-right-from-bracket fa-lg mr-2"></i> Log out</a>
						   		</div>
						 	</div>
						</li>
					</ul>
				</li>
			<li class="nav-item">
				<a class="nav-link"
					data-widget="fullscreen" href="#" role="button"> 
					<i class="fas fa-expand-arrows-alt"></i>
				</a>
			</li>
			<!-- <li class="nav-item">
		    	<a class="nav-link" data-widget="control-sidebar" data-slide="true" href="#" role="button">
		        	<i class="fas fa-th-large"></i>
		        </a>
	      	</li> -->
		</ul>
	</nav>
	<%@ include file="/appone/jsp/slidemenu/slide_menu.jsp" %>
	<div class="content-wrapper scheduler-content-wrapper">
		<section class="content-header pt-1 pb-1 pl-3 pr-3 scheduler-content-header">
	    	<div class="container-fluid">
	        	<div class="row mb-0">
	          		<div class="col-sm-6 d-flex align-items-center justify-content-left">
	          			<h6 class="m-0 main-title"></h6>
	          		</div>
	          		<div class="col-sm-6">
	            		<ol class="breadcrumb float-sm-right sub-title"></ol>
	          		</div>
	        	</div>
			</div>
  		</section>
	    <section class="content pt-1 pb-1 pl-3 pr-3 scheduler-main-content" id="mainContent"></section>
	</div>
	<footer class="main-footer p-1 scheduler-footer">
	    <div class="float-right d-none d-sm-block scheduler-footer__version">
	    	<b>Version</b> 3.2.0
	    </div>
	    <div class="scheduler-footer__copyright">
	    	<strong>CREATE &copy;2024 Contact as : <a href="">ipsit@ipscorp.co.kr</a></strong> All rights reserved.
	    </div>
  	</footer>
  	<aside class="control-sidebar control-sidebar-light">
  	</aside>
</div>

<!-- 등록 / 수정 POPUP -->
<div class="modal fade scheduler-modal" id="modal_userProfile" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog modal-sm scheduler-modal__dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title">사용자 정보</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<div class="box-profile">
					<div class="text-center">
						<img class="profile-user-img img-fluid img-circle" src="${user_avatar}">
					</div>
					<h3 class="profile-username text-center">${user_nm}</h3>
					<p class="text-muted text-center">${desk_div}</p>
					<ul class="list-group list-group-unbordered mb-3">
						<li class="list-group-item">
							<b>E-Mail</b> <a class="float-right">${email}</a>
						</li>
						<li class="list-group-item">
							<b>Mobile</b> <a class="float-right">${hp_no}</a>
						</li>
						<li class="list-group-item">
							<b>입사일자</b> <a class="float-right">${employment_date}</a>
						</li>
					</ul>
					<a href="#" class="btn btn-primary btn-block" id="btn_profile"><b>상세 정보</b></a>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="modal fade scheduler-modal" id="modal_orgChart" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog modal-lg modal-dialog-scrollable scheduler-modal__dialog scheduler-modal__dialog--org" role="document">
		<div class="modal-content">
			<div class="modal-header">
					<h5 class="modal-title" id="orgChartModalLabel">조직도</h5>
					<button type="button" class="close" data-dismiss="modal"
						aria-label="닫기">
					</button>
				</div>
				<div class="modal-body">
					<div id="orgChartContainer" style="min-height: 400px;">
						<p class="text-center text-muted">조직도 데이터를 불러오는 중입니다...</p>
					</div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary"
						data-dismiss="modal">닫기</button>
				</div>
			</div>
		</div>
	</div>
	<!-- /.modal -->
	
	<script>
		var $main_View = $('#main_View');
		/************************************************************************
		 * 메인 페이지 -> 페이지 로딩 이벤트 처리
		 *************************************************************************/
		$(document).ready(function() {
			//User 시작페이지 셋팅
			fn_PageMove('${default_page}');
		});

		/* 메인 페이지 -> 로그아웃 버튼 클릭 이벤트 */
		$main_View.on('click', '#logout', function() {
			fn_displayLogoutPop();
		});

		/* 프로파일 화면 전환 */
		$("#btn_profile").click(function() {
			$("#modal_userProfile").modal("hide");
			fn_PageMove("S.Profile");
		});
		/* 프로파일 화면 전환 */
		$("#app_profile").click(function() {		
			$("#modal_userProfile").modal("hide");
			fn_PageMove("S.Profile");
		});

		// 클릭 이벤트가 부모 요소로 전파되지 않도록 설정
		$('.navbar-nav>.user-menu>.dropdown-menu').on('click', function(event) {
			event.stopPropagation(); // 클릭 이벤트 전파 방지
		});
		// Profile 아이콘 클릭
		$('.profile-badge').on('click', function(event) {
			$("#modal_userProfile").modal("show");
		});
		// 모든 아바타 아이템을 선택
		$(".dropdown-avatarItem img").on("click", function() {
			// 클릭한 이미지의 src를 mainAvatar의 src로 변경
			$("#mainAvatar").attr("src", $(this).attr("src"));
		});

		// Log Out
		$("#logout").on("click", function() {
			fn_displayLogoutPop();
		});

		/************************************************************************
		 * 메인 페이지 -> 로그아웃
		 *************************************************************************/
		function fn_displayLogoutPop() {
			showConfirm('info', '로그아웃 하시겠습니까?', '', 'Log out', 'Cancel',
					redirectLogout);
		}

		var user_id = '${user_id}';
		var user_nm = '${user_nm}';
		var user_avatar = '${user_avatar}';
		$("#pro_user_nm").text(user_nm);
		$(".profile-username").text(user_nm);
		$(".profile-Email").text('${email}');
		$("#navbar_avatar").attr("src", user_avatar);
		$("#profile_avatar").attr("src", user_avatar);

		/********************************************************************************
		 * Alert ->  화면 리프레쉬
		 ********************************************************************************/
		//setInterval(function(){fn_getAlertList()}, 5000);
		//페이지 초기 셋팅
		//$('[data-toggle="tooltip"]').tooltip();	//tooltip 적용
		//$("body").toggleClass("sidebar-collapse"); //사이드바 자동 close
		$(function() {
			$('#app_orgChart').on('click', function(e) {
				e.preventDefault();
				// 기존 프로필 드롭다운 닫기
				$('.user-menu .dropdown-menu').removeClass('show');
				// 조직도 모달 열기
				$('#modal_orgChart').modal('show');
				// 데이터 로드
				loadOrgChartSample();
			});
		});

		function loadOrgChartSample() {
			// — 샘플 노드 + 프로필 데이터
			var sampleData = [ {
				id : "node_ceo",
				text : "CEO",
				state : {
					opened : true
				},
				data : {
					name : "Alice CEO",
					email : "alice@company.com",
					img : "https://via.placeholder.com/40"
				},
				children : [ {
					id : "node_cto",
					text : "CTO",
					data : {
						name : "Bob CTO",
						email : "bob@company.com",
						img : "https://via.placeholder.com/40"
					},
					children : [ {
						id : "node_dev_mgr",
						text : "Development Manager",
						data : {
							name : "Carol Dev",
							email : "carol@company.com",
							img : "https://via.placeholder.com/40"
						}
					}, {
						id : "node_qa_mgr",
						text : "QA Manager",
						data : {
							name : "Dan QA",
							email : "dan@company.com",
							img : "https://via.placeholder.com/40"
						}
					} ]
				}, {
					id : "node_cfo",
					text : "CFO",
					data : {
						name : "Eve CFO",
						email : "eve@company.com",
						img : "https://via.placeholder.com/40"
					},
					children : [ {
						id : "node_acc_mgr",
						text : "Accounting Manager",
						data : {
							name : "Frank Acc",
							email : "frank@company.com",
							img : "https://via.placeholder.com/40"
						}
					}, {
						id : "node_fin_mgr",
						text : "Finance Manager",
						data : {
							name : "Grace Fin",
							email : "grace@company.com",
							img : "https://via.placeholder.com/40"
						}
					} ]
				} ]
			} ];

			// 컨테이너 초기화 + jsTree 생성 + 클릭 이벤트 바인딩
			$('#orgChartContainer')
					.empty()
					.jstree({
						'core' : {
							'data' : sampleData
						}
					})
					.on(
							'select_node.jstree',
							function(e, data) {
								var node = data.node;
								var profile = node.original.data;
								if (!profile)
									return;

								// 기존 팝오버 제거
								$('.jstree-anchor').popover('dispose');

								// 클릭된 앵커 요소
								var $anchor = $('#' + node.id
										+ ' > .jstree-anchor');
								$anchor
										.popover(
												{
													container : 'body',
													html : true,
													placement : 'right',
													trigger : 'focus',
													title : profile.name,
													content : '<div class="media">'
															+ '<img src="' + profile.img + '" ' +
                  'class="mr-2 rounded-circle" ' +
                  'style="width:40px;height:40px;">'
															+ '<div class="media-body">'
															+ '<p class="mb-0">'
															+ profile.email
															+ '</p>'
															+ '</div>'
															+ '</div>'
												}).popover('show');
							});
		}
		
		function loadOrgChart() {
			$('#orgChartContainer').html(
					'<p class="text-center text-muted">로딩 중...</p>');
			$
					.ajax({
						url : '<c:url value="/scheduler/getOrgChartData"/>',
						method : 'GET',
						dataType : 'json'
					})
					.done(function(data) {
						$('#orgChartContainer').jstree({
							'core' : {
								'data' : data
							}
						});
					})
					.fail(
							function() {
								$('#orgChartContainer')
										.html(
												'<p class="text-danger text-center">데이터 로드에 실패했습니다.</p>');
							});
		}

		/*******************************
		 * 세션 남은 시간 표시 (navbar)
		 *******************************/
		(function(){
			try {
				// 서버 기준 현재 남은 시간(sec)과 설정(min)
				// Use actual session maxInactiveInterval to keep UI aligned with real timeout
				var SESSION_TIMEOUT_SEC = (function(){
					var raw = parseInt('<%= (session != null ? session.getMaxInactiveInterval() : 0) %>', 10);
					if (isNaN(raw) || raw < 0) return 0;
					return raw;
				})();
				var SESSION_TIMEOUT_MINUTES = (function(){
					if (!SESSION_TIMEOUT_SEC) return 0;
					return Math.max(1, Math.round(SESSION_TIMEOUT_SEC / 60));
				})();
				var SESSION_REMAIN_SEC = (SESSION_TIMEOUT_SEC > 0) ? SESSION_TIMEOUT_SEC : (SESSION_TIMEOUT_MINUTES * 60);

				function fmt(sec) {
					if (sec < 0) sec = 0;
					var h = Math.floor(sec / 3600);
					var m = Math.floor((sec % 3600) / 60);
					var s = Math.floor(sec % 60);
					function pad(n){ return (n<10 ? '0' : '') + n; }
					return (h>0 ? pad(h)+':' : '') + pad(m) + ':' + pad(s);
				}

				function render() {
					var $cd = $("#sessionCountdown");
					var $lbl = $("#sessionTimeoutMinutesLabel");
					if ($cd.length) $cd.text(fmt(SESSION_REMAIN_SEC));
					if ($lbl.length) $lbl.text('(' + SESSION_TIMEOUT_MINUTES + '분 설정)');
				}
				function resetSessionCountdown() {
					if (SESSION_TIMEOUT_SEC > 0) {
						SESSION_REMAIN_SEC = SESSION_TIMEOUT_SEC;
					} else if (SESSION_TIMEOUT_MINUTES > 0) {
						SESSION_REMAIN_SEC = SESSION_TIMEOUT_MINUTES * 60;
					} else {
						SESSION_REMAIN_SEC = 0;
					}
					render();
				}

				render();

				if (window.__sessionCountdownTimer) {
					clearInterval(window.__sessionCountdownTimer);
				}
				window.__sessionCountdownTimer = setInterval(function(){
					SESSION_REMAIN_SEC--;
					if (SESSION_REMAIN_SEC < 0) SESSION_REMAIN_SEC = 0;
					var $cd = $("#sessionCountdown");
					if ($cd.length) $cd.text(fmt(SESSION_REMAIN_SEC));
				}, 1000);

				$(document).off('session:touch.schedulerTimer').on('session:touch.schedulerTimer', function(){
					resetSessionCountdown();
				});
			} catch(e) {
				// 표시 실패는 무시
				console && console.warn && console.warn('session timer init failed', e);
			}
		})();
	</script>
<style>
.header-user-info {
    font-size: 15px;
    font-weight : 700;
    color: #68737f;
    top:20px;
}

</style>
<!-- <script src="/scheduler/appone/plugins/dist/js/demo.js"></script> -->
</body>
</html>





