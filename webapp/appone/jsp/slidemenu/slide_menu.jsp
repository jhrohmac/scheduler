<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script type="text/javascript">
	/************************************************************************
	 * 레프트 사이드바 메뉴 세팅 (AdminLTE 트리뷰 규격)
	 * - /scheduler/menu/setMenu.do 호출 결과를 그대로 사용(HTML 또는 JSON)
	 * - 최상위 메뉴 클릭 시에도 'active' 음영 + 'menu-open' 유지
	 * - 하위 메뉴 클릭 시 상위 전체에 활성 상태 전파
	 * - 마지막 선택 메뉴 localStorage 복원
	 ************************************************************************/
	
	$(document).ready(function() {
		fn_slide_menu();
	});

	function fn_slide_menu(){
		ajaxCall("/scheduler/menu/setMenu.do", "json", "",
		function fn_setMenuList(data){
			//메뉴 리셋 후 셋팅
		  	const parent = document.getElementById('topmenu');
		  	const defaultPage = data.singleData;
		  	parent.textContent = "";
		
			$.each(data, function(msg){
				//TOP Menu
				$(this).each(function(i){
					var parent_id 	= this.parent_id;
					var menu_seq 	= this.menu_seq;
					var popup_flag 	= this.popup_flag;
					var pagemv 		= "javascript:fn_PageMove('"+this.menu_id+"')";
					var popup_icon	= "";
					
					//메뉴 popup 아이콘 
					if(popup_flag == "Y"){
						popup_icon= "<span class='badge right'><i class='right fas fa-external-link'></i></span>"
					};
					
					if(parent_id == ""){
						//부모 메뉴
						var parent_cnt 		= this.parent_cnt;
						var angle_left		= "<i class='right fas fa-angle-left'></i>";
						var nav_treeview	= "<ul class='nav nav-treeview' id="+menu_seq+"></ul>"

						// 자식이 없는 최상위 메뉴일 경우, 꺾쇠/트리뷰 제거
						if(parent_id === "" && (parent_cnt === 0 || parent_cnt === "0")){
							angle_left  = "";
							nav_treeview= "";
						}
						
						//부모 메뉴
						var setMain =   "<li class='nav-item'>"
									  +"	<a href="+pagemv+" class='nav-link'>"
									  +"		<i class='nav-icon fas "+this.menu_icon+"'></i>"
									  +"		<p>"+this.menu_nm+angle_left+popup_icon+"</p>"
									  +"	</a>"
									  +		nav_treeview
									  +"</li>";
						$("#topmenu").append(setMain);
					}else{
						//자식 메뉴
						var subActive = (this.menu_id == defaultPage) ? " active" : "";
			   			var setSub =  "<li class='nav-item'>"
			   						 +"		<a href="+pagemv+" class='nav-link menu-info"+ subActive +"'>"
			   						 +"			<i class='nav-icon fas "+this.menu_icon+"'></i>"
			   						 +"			<p>"+this.menu_nm+popup_icon+"</p>"
			   						 +"		</a>"
			   						 +"</li>";
			   			$("#"+parent_id).append(setSub);
			   			//부모 메뉴 Open
			   			if(this.menu_id == defaultPage){
							$("#"+parent_id).parent().addClass(" menu-is-opening menu-open");
						}
					}
				});
			});
		});
	};
	
	$(document).on('click', '#topmenu a.menu-info', function(e){
	    var $li = $(this).closest('li');
	    // 자식 <ul>이 없을 때(leaf 메뉴)
	    if ($li.children('ul').length === 0) {
	        // 모든 메뉴에서 active 제거
	        $('#topmenu a.menu-info').removeClass('active');
	        // 클릭한 메뉴에만 active 추가
	        $(this).addClass('active');
	    }
	});
</script>

<aside class="main-sidebar elevation-4 sidebar-light-lime">
	<!-- <aside class="main-sidebar sidebar-dark-primary elevation-4"> -->
     <!-- Brand Logo -->
    <a href="" class="brand-link p-2">
      <img src="./appone/plugins/dist/img/IPSLogo.png" alt="Scheduler System" width="210px;" height="30px;">
      <span class="brand-text font-weight-light"></span>
    </a>
    <div class="sidebar">
		<!-- SidebarSearch Form -->
      <div class="form-inline mt-2 pb-2 mb-2 d-flex">
        <div class="input-group" data-widget="sidebar-search">
          <input class="form-control form-control-sidebar" type="search" placeholder="Search" aria-label="Search">
          <div class="input-group-append">
            <button class="btn btn-sidebar">
              <i class="fas fa-search fa-fw"></i>
            </button>
          </div>
        </div>
      </div>
      <nav class="mt-2">
      	<ul class="nav nav-pills nav-sidebar flex-column nav-child-indent" data-widget="treeview" role="menu" 
      		data-accordion="false" id="topmenu">
      	</ul>
      </nav>
    </div>
</aside>