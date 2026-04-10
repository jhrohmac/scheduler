<%@ page contentType="text/html; charset=utf-8" %>
<script src="/scheduler/appone/plugins/fullcalendar/main.js"></script>
<link rel="stylesheet" href="/scheduler/appone/plugins/fullcalendar/main.css">
<link rel="stylesheet" href="/scheduler/appone/plugins/system/css/custom_calendar.css">
<%@ page contentType="text/html; charset=utf-8" %>

<div class="container-fluid" id="div_calendar_main">
  	<div class="card card-primary card-outline">
		<div class="card-header p-0 border-bottom-0">
			<ul class="nav nav-tabs" role="tablist">
				<li class="nav-item">
					<a class="nav-link active" href="#"data-target="#calendarView">Calendar</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" href="#" data-target="#listView">List</a>
				</li>
			</ul>
		</div>

		<div class="card-body tab-content">
	      	<!-- Calendar Tab -->
	      	<div id="calendarView" class="tab-pane active">
	        	<div id="calendar"></div>
	      	</div>
	      		<!-- List Tab -->
	      		<div id="listView" class="tab-pane">
        			<div class="form-group search_row mb-2">
						<label for="sel_startDate" class="col-sm-1 control-label text-right">일시</label>
						<div class="col-lg-1">
							<div class="input-group date" id="setStartDate" data-target-input="nearest">
			                    <input type="text" class="form-control form-control-sm datetimepicker-input"
			                    	id="sel_startDate" style="text-align: center;" data-target="#setStartDate"/>
			                    <div class="input-group-append" data-target="#setStartDate" data-toggle="datetimepicker">
			                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
			                    </div>
		                	</div>
						</div>
						<div class="col-lg-1">
							<div class="input-group date" id="setEndDate" data-target-input="nearest">
			                    <input type="text" class="form-control form-control-sm datetimepicker-input"
			                    	id="sel_endDate" style="text-align: center;" data-target="#setEndDate"/>
			                    <div class="input-group-append" data-target="#setEndDate" data-toggle="datetimepicker">
			                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
			                    </div>
		                	</div>
						</div>
						<label for="sel_listCountry" class="col-sm-1 control-label text-right">Country</label>
						<div class="col-sm-1">
							<select class="form-control custom-select custom-select-sm" id="sel_listCountry">
								<option value="ALL">ALL</option>
								<option value="KR">KR</option>
								<option value="US">US</option>
								<option value="GB">GB</option>
							</select>
						</div>
						<label for="sel_holidayName" class="col-sm-1 col-form-label" style="text-align: right;">Holiday</label>
						<div class="col-sm-3">
							<input type="text" id="sel_holidayName" name="sel_holidayName" class="form-control form-control-sm" placeholder="Holiday Keyword..">
						</div>
						<div class="col-sm-3">
							<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right">
								<i class="fa fa-search"></i>
							</button>
						</div>
					</div>
	        		<table id="holidayGirdTable" class="table table-sm">
			        	<thead>
							<tr>
								<th>No.</th>
								<th>DATE</th>
								<th>COUNTRY</th>
								<th>상세내용</th>
								<th>Summer Time</th>
							</tr>
						</thead>
		          		<tbody></tbody>
	        	</table>
	      	</div>
   		</div>
	</div>
</div>
<!-- /.container-fluid -->
<div class="modal fade" id="modal_calendar" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header modal-color">
				<h6 class="modal-title">Holiday Modal</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<div class="form-group row">
						<label for="in_title" class="col-sm-3 control-label"><span>*</span>제목</label>
						<div class="col-lg-7">
							<input type="text" class="form-control form-control-border" style="background-color:#fff"  id="in_title" placeholder="제목을 입력하세요.">
						</div>
					</div>
					<div class="form-group row">
                 		<label for="in_holidayDate" class="col-sm-3 control-label"><span>*</span>일자</label>
                 		<div class="col-lg-4">
	                 		<input type="text" class="form-control form-control-border" style="background-color:#fff" id="in_holidayDate" readonly>
						</div>
                	</div>
					<div class="form-group row">
						<label for="in_content" class="col-sm-3 control-label"><span>*</span>설명</label>
						<div class="col-lg-8">
							<textarea class="form-control" id="in_content" rows="3" placeholder="설명 ..."></textarea>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_countryDiv" class="col-sm-3 control-label"><span>*</span>캘린더 구분</label>
						<div class="col-lg-8">
							<select id="in_countryDiv" class="custom-select form-control-border border-width-2" style="font-size: 0.8rem;">
				              <option value="KR">한국</option>
				              <option value="US">미국</option>
				              <option value="GB">글로벌</option>
				            </select>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventDiv" value="">
				<input type="hidden" id="in_gridDiv" value="">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-danger" id="btn_del">삭제</button>
					<button type="button" class="btn btn-sm btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>
<script>
(function($) {
  var calendar;
  var modal_calendar = $("#modal_calendar");
  function init() {
    initTabs();
    initDatePicker();
    renderCalendar();
    bindEvents();
  }

  function initTabs() {
    $('.nav-tabs .nav-link').on('click', function(e) {
      e.preventDefault();
      var target = $(this).data('target');
      $('.nav-link').removeClass('active');
      $(this).addClass('active');
      $('.tab-pane').removeClass('active');
      $(target).addClass('active');
      if(target=="#listView"){
    	  fn_loadHolidayList();
      }
    });
  }

  function initDatePicker() {
	  var $startPicker = $('#setStartDate');
	  var $endPicker   = $('#setEndDate');
	  var now = moment();

	  // 시작일 피커 초기화
	  if ($startPicker.length && typeof $startPicker.datetimepicker === 'function') {
	    $startPicker.datetimepicker({ format: 'YYYY-MM', viewMode: 'months' });
	    $startPicker.on('change.datetimepicker', function(e) {
	      var val = e.date ? e.date.format('YYYY-MM') : '';
	      $('#sel_startDate').val(val);
	    });
	    $startPicker.datetimepicker('date', now);
	    $('#sel_startDate').val(now.format('YYYY-MM'));
	  }

	  // 종료일 피커 초기화
	  if ($endPicker.length && typeof $endPicker.datetimepicker === 'function') {
	    $endPicker.datetimepicker({ format: 'YYYY-MM', viewMode: 'months' });
	    $endPicker.on('change.datetimepicker', function(e) {
	      var val = e.date ? e.date.format('YYYY-MM') : '';
	      $('#sel_endDate').val(val);
	    });
	    $endPicker.datetimepicker('date', now);
	    $('#sel_endDate').val(now.format('YYYY-MM'));
	  }
	}

	function renderCalendar() {
    	var el = document.getElementById('calendar');
    	calendar = new FullCalendar.Calendar(el, {
      		headerToolbar: {
        	left: 'prev,next today',
        	center: 'title',
        	right: 'dayGridMonth,listMonth'
      	},
      	buttonText: {
			today:    '오늘',
		    month:    '월간',
		    week:     '주간',
		    day:      '일간',
		    list:     '목록',		
		},
		expandRows: true, 			// 화면에 맞게 높이 재설정
   		themeSystem: 'bootstrap',
		initialView: 'dayGridMonth',// 초기 로드 될때 보이는 캘린더 화면(기본 설정: 달)
		editable: true, 			// 수정 가능
		selectable: true, 			// 달력 일자 드래그 설정가능
		locale: 'ko',
      	height: 'auto',
      	eventSources: [{
        	events: function(info, successCallback, failureCallback) {
	        	var sel_country = $("#sel_country").val();
	        	(sel_country==undefined)? sel_country="ALL": sel_country;
	          	var url = "/scheduler/holidayCalendar/holidayCalendar.do";
	          	var param = "in_startDate=" + toDateString(info.start) 
	          				+ "&in_endDate=" + toDateString(info.end) 
	          				+ "&sel_country=" + sel_country 
				var type = "json";
	          	ajaxCall(url, type, param, function(data) {
	            	const eventArray = [];
	            	(data.data || []).forEach((res) => {
		              	eventArray.push({
			                title: res.title,
			                start: res.start,
			                end: res.end,
			                allDay: res.allDay,
			                backgroundColor: res.backgroundColor,
			                borderColor: res.borderColor,
			                extendedProps: res
		              	});
	           		});
				successCallback(eventArray);
	          	});
        	}
      	}],
		eventClick : function(arg) {
			fn_resetForm(modal_calendar);
			console.log("=======eventClick==============")
			console.log(arg);
			
			var event = arg.event;
			var extendedProps = arg.event.extendedProps;
			var start_date = event.startStr;
			$("#in_title").val(arg.event.extendedProps.content);
			$('#in_holidayDate').val(start_date);
			$("#in_content").val(arg.event.extendedProps.content);
			$("#in_countryDiv").val(arg.event.extendedProps.country_div);
			//$("#in_title").attr("disabled",true);
			$("#in_countryDiv").attr("disabled",true);
			$("#in_eventDiv").val("M");
			fn_calendarColor();
			$(modal_calendar).modal('show');
		},
      	select : function(arg) {
      		fn_resetForm(modal_calendar);
			var start_date = arg.startStr;
			$("#in_title").focus();
			$('#in_holidayDate').val(start_date);
			$("#in_title").removeAttr("disabled"); 
			$("#in_countryDiv").removeAttr("disabled"); 
			$("#in_eventDiv").val("C");
			$(modal_calendar).modal('show');
			fn_calendarColor();
			calendar.unselect();
		}
    });
    calendar.render();
    // ─── 전체 버튼 클래스를 btn btn-block btn-default btn-sm 으로 변경 ─────
	(function() {
		var calendarEl = el;
      	var toolbar = calendarEl.querySelector('.fc-toolbar');
      	if (!toolbar) return;
      
      	var leftChunk = toolbar.querySelector('.fc-toolbar-chunk:first-child');
      	if (!leftChunk) return;

      	var existingGroup = leftChunk.querySelector('.fc-button-group');
      	// ─── Country select 그룹 ───────────────────────────────────────────────────

      	var newGroup = document.createElement('div');
	      	newGroup.className = 'btn-group';
	      	newGroup.style.marginLeft = '8px';

      	// Country select
      	var sel = document.createElement('select');
	      	sel.id = 'sel_country';
	      	sel.className = 'form-control form-control-sm';
	      	sel.style.marginLeft = '8px';
      	['ALL', 'KR', 'US', 'GB'].forEach(function(val) {
        	var opt = document.createElement('option');
        	opt.value = val;
        	opt.text = val;
        	sel.appendChild(opt);
      	});
      
      	newGroup.appendChild(sel);
      	// 3) 삽입: 기존 버튼그룹 바로 옆에 붙여 넣기
      	if (existingGroup && existingGroup.parentNode) {
        	existingGroup.parentNode.insertBefore(newGroup, existingGroup.nextSibling);
      	} else {
        	leftChunk.appendChild(newGroup);
      	}

		// ─── 여기에 연도 입력 필드 추가 ────────────────────────────────────────
		var yearGroup = document.createElement('div');
		yearGroup.className = 'btn-group';
		yearGroup.style.marginLeft = '8px';
		
		var yearInput = document.createElement('input');
		yearInput.type = 'number';
		yearInput.id = 'sel_year';
		yearInput.className = 'form-control form-control-sm';
		yearInput.style.width = '70px';
		yearInput.style.textAlign = 'center';
		// 초기값을 현재 보고 있는 날짜의 연도로 설정
		yearInput.value = calendar.getDate().getFullYear();
		
		yearGroup.appendChild(yearInput);
		newGroup.parentNode.insertBefore(yearGroup, newGroup.nextSibling);

		// 연도 입력 변경 시 캘린더 이동
		yearInput.addEventListener('change', function() {
		  	var year = parseInt(this.value, 10);
		  	if (isNaN(year)) return;
		  	var month = calendar.getDate().getMonth();  // 0 ~ 11
			calendar.gotoDate(new Date(year, month, 1));
		});
      	// ─── Today 버튼 클릭 시 연도 리셋 ────────────────────────────────────
      	var todayBtn = toolbar.querySelector('.fc-today-button');
      	if (todayBtn) {
        	todayBtn.addEventListener('click', function() {
          	var nowY = new Date().getFullYear();
          	yearInput.value = nowY;
       		});
      	}
   	})();

    el.addEventListener('wheel', function(e) {
		  e.preventDefault();
		  if (e.deltaY > 0) calendar.next();
		  else calendar.prev();
		}, { passive: false });
  	}
	function fn_calendarColor(){
		var in_countryDiv = $('#in_countryDiv').val();
		var color = "";
		switch (in_countryDiv) {
		case "KR":
			color = "#dc3545d9";
			break;
		case "US":
			color = "#80bff9";
			break;
		case "GB":
			color = "#28a745";
			break;
		}
		
		$('.modal-color').css({
			'background-color': color,
			'border-color'    : color
		});
	};
	function fn_loadHolidayList() {
		/************************************************************************
		* qryBank list
		*************************************************************************/
			// 호출 URL
			var url = "/scheduler/holidayCalendar/holidayGridData.do";
			//조회 조건
			var param = {
					 	"sel_listCountry" 	: $('#sel_listCountry').val(),
					 	"sel_holidayName" 	: $('#sel_holidayName').val(),
					 	"sel_startDate" 	: $('#sel_startDate').val(),
		                "sel_endDate"		: $('#sel_endDate').val()
			};
			//컬럼 옵션
			var columns = [
							{"data": "rnum"},
							{"data": "start_time"},
							{"data": "country_name"},
							{"data": "content"},
							{"data": "summer_time"}
			           ];
			
			var columnDefs = {
	                    "targets": [1,2,3,4], // 가격과 수량 열의 인덱스
				    	"className": "text-center"
			};
		    
			var gridObj = {
			        'grid_id': "holidayGirdTable"
			        ,'url': url
			        ,'param': param
			        ,'columns': columns
			        ,'columnDefs': columnDefs
			        ,'dom':    'Bfrtip'
			        ,'buttons': [{
					            text: '<i class="fa-solid fa-download"></i>FX Future Tenor',
					            titleAttr: '파일 다운로드',
					            action: function (e, dt, node, cfg) {
					            	fn_exportTenorList();
					            }
					          }]
			        ,'columnCheck': false
			};
			
			var gridOptions = {
			        'serverSide': true,
			        'searching': false,
			        'paging': true,
			        'button': false,
			        'ordering': true, 
			        'lengthChange': false,
			        'info': true,
			        'autoWidth': false,
			        'responsive': true,
			        'bDestroy': true,
			        'processing': true,
			        'ordering': false
		    };
			dataTableGridNew(gridObj,gridOptions);
	};

 	$('#holidayGirdTable tbody').on( 'dblclick', 'tr', function (){
 		var table = $("#holidayGirdTable").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');	    
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
	   	var row_position = table.fnGetPosition(this);
   		var row_data = table.fnGetData(row_position);
   		//선택한 RowData tempData Copy
   		$("#in_title").val(row_data.content);
		$('#in_holidayDate').val(row_data.start_time);
		$("#in_content").val(row_data.content);
		$("#in_countryDiv").val(row_data.country_div);
		$("#in_countryDiv").attr("disabled",true);
		$("#in_eventDiv").val("M");
		$(modal_calendar).modal('show');
		fn_calendarColor();
	});
	/* 캘린더 등록 및 수정 */ 
	function fn_eventCalendar(){
		var param = getJQParams(modal_calendar);
		var type = "script";
		var url = "/scheduler/holidayCalendar/eventCalendar.do";
		ajaxCall(url, type, param, 
			function (){
				$("#modal_calendar").modal('hide');			//menu 수정 Popup Open
		        calendar.refetchEvents(); 					// 캘린더 이벤트 새로고침
		});
	};
	/*  Tenor List*/ 
	function fn_exportTenorList(){
		var listView = $("#listView");
		var param = getJQParams(listView);
		var type = "POST";
		var url = "/scheduler/holidayCalendar/getTenorListDownload.do";
		ajaxCall(url, type, param, 
			function (data){
			console.log(data);
		});
	};
	function bindEvents() {
		var active = $('.nav-tabs .nav-link.active').data('target');
		/* 검색 키워드 입력 또는 조회 버튼 클릭 시 */
	    $("#listView").on('keyup', '#sel_holidayName',  function(){
	    	fn_loadHolidayList();
	    });
		$('#btn_main_search').on('click', function() {
			fn_loadHolidayList();
		});
		$('#btn_save').on('click', function() {
			fn_eventCalendar();
		});
		$('#btn_del').on('click', function() {
			$("#in_eventDiv").val("D");
			fn_eventCalendar();
		});
	
	   	$('#sel_country').on('change', function() {
	    	calendar.refetchEvents();
	     	fn_calendarColor();
	   	});
	   	
	   	$('#sel_listCountry').on('change', function() {
	   		fn_loadHolidayList();
	   	});
	   	
	   	$('#in_countryDiv').on('change', function() {
	    	fn_calendarColor();
	   	});
	};
  $(document).ready(init);
})(jQuery);
</script>
<style>
.dt-buttons { margin-bottom:.25rem }
.dt-buttons .btn {
    background-color: #f8f9fa !important;
    color: #444 !important;
    border: 1px solid #ddd !important;
    padding: .25rem .5rem !important;
    font-size: .875rem !important;
    line-height: 1.5 !important;
    border-radius: .2rem !important;
    cursor: pointer !important;
}
/* 버튼 hover 시 회색 배경 */
.dt-buttons .btn:hover { 
background-color: #e9ecef !important; 
}
.dt-buttons .btn:disabled { opacity:.65 }
.dt-buttons .btn i { margin-right:.25rem }

</style>