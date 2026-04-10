<%@ page contentType="text/html; charset=utf-8" %>
<script src="/scheduler/appone/plugins/fullcalendar/main.js"></script>
<link rel="stylesheet" href="/scheduler/appone/plugins/fullcalendar/main.css">
<link rel="stylesheet" href="/scheduler/appone/plugins/system/css/custom_calendar.css">
<div class="container-fluid" id="div_calendar_main">
	<div class="row">
		<div class="col-md-2">
			<div class="sticky-top mb-3">
				<div class="card">
					<div class="card-header">
						<h4 class="card-title">Draggable Events</h4>
					</div>
					<div class="card-body">
						<div id="external-events">
							<div class="external-event bg-success">일정</div>
							<div class="external-event bg-warning">todo</div>
							<div class="external-event bg-info">Do homework</div>
							<div class="external-event bg-primary">MarkitWire</div>
							<div class="external-event bg-danger">Sleep tight</div>
							<div class="checkbox">
								<label for="drop-remove"> 
									<input type="checkbox" class="flat-red"  id="drop-remove"> Remove after drop
								</label>
							</div>
						</div>
					</div>
				</div>
				<!-- /.card -->
              <div class="card">
                <div class="card-header">
                  <h3 class="card-title">Create Event</h3>
                </div>
                <div class="card-body">
                  <div class="btn-group" style="width: 100%; margin-bottom: 10px;">
                    <ul class="fc-color-picker" id="color-chooser">
                      <li><a class="text-primary" href="#"><i class="fas fa-square"></i></a></li>
                      <li><a class="text-warning" href="#"><i class="fas fa-square"></i></a></li>
                      <li><a class="text-success" href="#"><i class="fas fa-square"></i></a></li>
                      <li><a class="text-danger" href="#"><i class="fas fa-square"></i></a></li>
                      <li><a class="text-muted" href="#"><i class="fas fa-square"></i></a></li>
                    </ul>
                  </div>
                  <!-- /btn-group -->
                  <div class="input-group">
                    <input id="new-event" type="text" class="form-control" placeholder="Event Title">
                    <div class="input-group-append">
                      <button id="add-new-event" type="button" class="btn btn-primary">Add</button>
                    </div>
                    <!-- /btn-group -->
                  </div>
                  <!-- /input-group -->
                </div>
              </div>
			</div>
		</div>
		<!-- fullcalendar-->
		<div class="card card-primary card-outline card-outline-tabs p-0 col-md-10">
			<div class="card-header p-0 border-bottom-0">
                <ul class="nav nav-tabs" id="custom-tabs-four-tab" role="tablist">
                  <li class="nav-item">
                    <a class="nav-link active" id="calendar_tab" data-toggle="pill" href="#custom_calendar_tab" role="tab" aria-controls="custom_calendar_tab" aria-selected="true">Calendar</a>
                  </li>
                </ul>
            </div>
            <div class="card-body  p-0">
                <div class="tab-content" id="custom-tabs-four-tabContent">
                  <div class="tab-pane fade show active" id="custom_calendar_tab" role="tabpanel" aria-labelledby="calendar_tab">
                  	<div id="calendar"></div>
                  </div>
                </div>
            </div>
        </div>
	</div>
</div>
<!-- /.container-fluid -->
<div class="modal fade" id="modal_calendar">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header modal-color">
				<h6 class="modal-title">일정 등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<div class="form-group row">
						<label for="in_title" class="col-sm-3 control-label"><span>*</span>제목</label>
						<div class="col-lg-7">
							<input type="text" class="form-control form-control-border" id="in_title" placeholder="제목을 입력하세요.">
						</div>
						<div class="col-lg-1">
						<!-- button with a dropdown -->
		                  <div class="btn-group">
		                    <button type="button" class="btn btn-sm dropdown-toggle" id="btngroup-color" data-toggle="dropdown" data-offset="-7">
		                      <i class="fas fa-bars"></i>
		                    </button>
		                    <div class="dropdown-menu calendar-dropdown-menu p-0 popColor" role="menu">
		                    	<ul class="fc-color-picker dropdown-colorItem">
									<li class="fc-color-picker dropdown-colorItem text-primary p-0 m-0">
										<i class="fas fa-square"></i>
									</li>
								</ul>
		                    	<ul class="fc-color-picker dropdown-colorItem">
									<li class="fc-color-picker dropdown-colorItem text-warning p-0 m-0">
										<i class="fas fa-square"></i>
									</li>
								</ul>
		                    	<ul class="fc-color-picker dropdown-colorItem">
									<li class="fc-color-picker dropdown-colorItem text-success p-0 m-0">
										<i class="fas fa-square"></i>
									</li>
								</ul>
		                    	<ul class="fc-color-picker dropdown-colorItem">
									<li class="fc-color-picker dropdown-colorItem text-muted p-0 m-0">
										<i class="fas fa-square"></i>
									</li>
								</ul>
		                    	<ul class="fc-color-picker dropdown-colorItem">
									<li class="fc-color-picker dropdown-colorItem text-danger p-0 m-0">
										<i class="fas fa-square"></i>
									</li>
								</ul>
		                    	<ul class="fc-color-picker dropdown-colorItem">
									<li class="fc-color-picker calendar-color-picker p-0 mb-1">
										<input type="color" id="in_color" value="" />
									</li>
								</ul>
		                    </div>
		                  </div>
                 		</div>
					</div>
					<div class="form-group row">
                 		<label for="reportrange" class="col-sm-3 control-label"><span>*</span>일시</label>
                 		<div class="col-lg-8">
                 			<div class="input-group">
								<input type="text" class="form-control form-control-border" id="reportrange" placeholder="일정을 입력하세요.">
								<div class="input-group-prepend">
									<span class="input-group-check pr-0">
										<input type="checkbox" id="in_allDay" class="flat-red" >
									</span>
								</div>
							</div>
                 		</div>
                	</div>
					<div class="form-group row">
						<label for="in_calendarDesc" class="col-sm-3 control-label"><span>*</span>설명</label>
						<div class="col-lg-8">
							<textarea class="form-control" id="in_content" rows="3" placeholder="설명 ..."></textarea>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_calendarDiv" class="col-sm-3 control-label"><span>*</span>캘린더 구분</label>
						<div class="col-lg-8" id="CALENDARGROUP">
							<select class="custom-select form-control-border border-width-2" id="in_calendarDiv">
			                    <option value="A">기본 캘린더 1</option>
			                    <option value="B">기본 캘린더 2</option>
			                    <option value="C">기본 캘린더 3</option>
							</select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_calendarDiv" class="col-sm-3 control-label">팀 공유</label>
						<div class="col-lg-8 common select-all" id="DEPT">
							<select class="custom-select form-control-border border-width-2" id="in_deskDiv"></select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_calendarFile" class="col-sm-3 control-label">파일</label>
						<div class="custom-file col-lg-8">
	                    	<div class="btn btn-default btn-sm btn-file ">
		                    	<i class="fas fa-paperclip"></i>첨부 파일
		                    	<input type="file" name="in_calendarFile" id="in_calendarFile">
		                  </div>
	                    </div>
					</div>
					<div class="form-group row">
						<label class="col-sm-3 control-label">등록자</label>
						<div class="col-lg-8">
							<input type="text" class="form-control form-control-border" style="background-color:#fff"
							 id="in_createrId" readonly="readonly">
						</div>
					</div>
					<div class="form-group row">
						<label class="col-sm-3 control-label">등록일자</label>
						<div class="col-lg-8">
							<input type="text" class="form-control form-control-border" style="background-color:#fff"
							 id="in_createDate" readonly="readonly">
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
	//전역 변수로 선언
	var calendar; 
	var div_calendar_main = $("#div_calendar_main");
	var modal_calendar = $("#modal_calendar");
	
$(function () {
		
   	/* -----------------------------------------------------------------
   		initialize the external events
    	-----------------------------------------------------------------*/
    	ini_events($('#external-events div.external-event'));
   		function ini_events(ele) {
    		ele.each(function () {
	     		var eventObject = {
	       			title: $.trim($(this).text())          
	    		};
	        	// store the Event Object in the DOM element so we can get to it later
	        	$(this).data('eventObject', eventObject);
	       		// make the event draggable using jQuery UI
	       		$(this).draggable({
	          		zIndex        : 1070,
	          		revert        : true, // will cause the event to go back to its
	          		revertDuration: 0  //  original position after the drag
	       		});
			});
		};
	
		var Draggable = FullCalendar.Draggable;
		var Calendar = FullCalendar.Calendar;
		var containerEl = document.getElementById('external-events');
		var checkbox = document.getElementById('drop-remove');
		var calendarEl = document.getElementById('calendar');
	
	   	new Draggable(containerEl, {
			itemSelector: '.external-event',
			drop : function(eventEl){}
		});

	   	calendar = new Calendar(calendarEl, {
	   		googleCalendarApiKey: 'ko.south_korea#holiday@group.v.calendar.google.com',// 여기에 구글캘린더 api키 입력하시면 됩니다.
	        /* customButtons:{
	            myCustomButton:{
	              text:"일정 추가하기",
	              click : function(){
	                    //부트스트랩 모달 열기
	                    $("#modal_calendar").modal("show");
	              }
	            }
	          }, */
	      	headerToolbar: {
		        left  : 'prev,next today',
		        center: 'title',
		        right : 'dayGridMonth,timeGridWeek,timeGridDay,listMonth'
	      	},
	      	height: '700px', // calendar 높이 설정
	      	expandRows: true, // 화면에 맞게 높이 재설정
	   		themeSystem: 'bootstrap',
	      	droppable : true, // this allows things to be dropped onto the calendar !!!
			initialView: 'dayGridMonth', // 초기 로드 될때 보이는 캘린더 화면(기본 설정: 달)
			//initialDate: '2021-07-15', // 초기 날짜 설정 (설정하지 않으면 오늘 날짜가 보인다.)
			navLinks: true, // 날짜를 선택하면 Day 캘린더나 Week 캘린더로 링크
			editable: true, // 수정 가능?
			selectable: true, // 달력 일자 드래그 설정가능
			selectMirror: true,
			nowIndicator: true, // 현재 시간 마크
			dayMaxEvents: true, // 이벤트가 오버되면 높이 제한 (+ 몇 개식으로 표현)
			locale: 'ko', // 한국어 설정
			visibleRange: function(currentDate) {
				// Generate a new date for manipulating in the next step
				var startDate = new Date(currentDate.valueOf());
				var endDate = new Date(currentDate.valueOf());
				
				// Adjust the start & end dates, respectively
				startDate.setDate(startDate.getDate() - 1); // One day in the past
				endDate.setDate(endDate.getDate() + 2); // Two days into the future
			    return { start: startDate, end: endDate };
			},
			//timeZone: 'UTC',
			//allDayText : '종일',
			buttonText: {
				today:    '오늘',
			    month:    '월간',
			    week:     '주간',
			    day:      '일간',
			    list:     '목록',		
			},
			//공휴일 데이터 추가
	 		 eventSources: [{ // 구글 캘린더 API 키를 발급받은 경우 공휴일 데이터 추가
				 googleCalendarId: "ko.south_korea#holiday@group.v.calendar.google.com",
				 backgroundColor: "transparent",
				 borderColor: "transparent",
				 className: "kr-holiday",
				 textColor: "red"
			 }], 
	   		//Random default events
			eventSources: [{
				events: function(info, successCallback, failureCallback) {
					var url = "/scheduler/calendar/calendar.do";
					var param = "in_startDate="+toDateString(info.start)+"&in_endDate="+toDateString(info.end);
					var type = "json";
					ajaxCall(url, type, param, fn_calendar_result);
					function fn_calendar_result(data){
						console.log("===========eventSources==============")
						console.log(data.data);
						console.log("===========eventSources==============")
						const eventData = data.data;
				          //이벤트에 넣을 배열 선언하기
				          const eventArray = [];
				          eventData.forEach((res) => {
				            eventArray.push({
				              title: res.title,
				              start: res.start,
				              end: res.end,
				              allDay: res.allDay,			              
				              backgroundColor: res.borderColor,
				              backgroundColor: res.backgroundColor,
				              extendedProps: res
				            });
				          });
				          successCallback(eventArray);
					}
				}
			}],
			select : function(arg) {
				fn_resetForm(modal_calendar);
				$('.modal-body form').each(function() {
		      		this.reset();
		  		});
				modal_calendar.modal('show');
				
				console.log("=====select==================");
				console.log(arg);
				console.log(arg.allDay);
				console.log(arg.backgroundColor);
				console.log("=====select==================");
				var checkyn = arg.allDay ? "Y" : "N";
				var start_date = arg.start;
				var end_date = arg.end;
				fn_calendarColor(arg.backgroundColor);
				setCheckBox(checkyn , modal_calendar, '#in_allDay');
				if(arg.allDay){
					$('#reportrange').val(toDateString(start_date) + ' - ' + toDateString(end_date));	
				}else{
					$('#reportrange').val(toDateTimeString(start_date) + ' - ' + toDateTimeString(end_date));
				}
				$("#in_title").focus();
				$("#in_eventDiv").val("C");
				calendar.unselect();
			},
			eventClick : function(arg) {
				$('.modal-body form').each(function() {
		      		this.reset();
		  		});
				
				console.log("=======eventClick==============")
				console.log(arg);
				
				var event = arg.event;
				var extendedProps = arg.event.extendedProps;
				var start_date = event.start;
				var end_date = event.end;
				if(end_date == null){
					end_date = start_date; 
				}
				if(event.allDay){
					$('#reportrange').val(toDateString(start_date) + ' - ' + toDateString(end_date));	
				}else{
					$('#reportrange').val(toDateTimeString(start_date) + ' - ' + toDateTimeString(end_date));
				}
				var checkyn = event.allDay ? "Y" : "N";
		  		setCheckBox(checkyn , modal_calendar, '#in_allDay');
				$("#in_calendarDiv").val(arg.event.extendedProps.calendar_div);
				$("#in_deskDiv").val(arg.event.extendedProps.desk_div);
				$("#in_title").val(event.title);
				$("#in_color").val(event.backgroundColor);
				$("#in_content").val(arg.event.extendedProps.content);
				$('#reportrange').val(toDateString(start_date) + ' - ' + toDateString(end_date));
				$("#in_createrId").val(arg.event.extendedProps.event_user);
				$("#in_createDate").val(arg.event.extendedProps.create_date);
	   			
				console.log("=======eventClick==============")
				fn_calendarColor(event.backgroundColor);
				$("#in_eventDiv").val("M");
				$("#modal_calendar").modal('show');
			},
	      	eventAdd: function(arg) { // 이벤트가 추가되면 발생하는 이벤트
	      		console.log("===eventAdd====");
	            console.log(arg);
			},
			eventChange: function(arg) { // 이벤트가 수정되면 발생하는 이벤트
				$("#in_eventDiv").val("M");
				console.log("===eventChange====");
				console.log(arg);
				console.log(arg);
				var event = arg.event;
				var start_date = event.start;
				var end_date = event.end;
				console.log(start_date);
				console.log(end_date);
				
				if(end_date == null){
					end_date = start_date; 
				}
				if(event.allDay){
					$('#reportrange').val(toDateString(start_date) + ' - ' + toDateString(end_date));	
				}else{
					$('#reportrange').val(toDateTimeString(start_date) + ' - ' + toDateTimeString(end_date));
				}
				$("#in_title").val(event.title);
				$("#in_content").val(event.extendedProps.content);
				$("#in_createrId").val(event.extendedProps.event_user);
				$("#in_createDate").val(event.extendedProps.create_date);
				
				fn_eventCalendar(event);
			},
			eventRemove: function(arg){ // 이벤트가 삭제되면 발생하는 이벤트
				console.log("===eventRemove====");
			  	console.log(arg);
			  	calendar.refetchEvents(); // 캘린더 이벤트 새로고침
			},
	      	drop: function(info) {
	      		//Events Drag 했을때 이벤트
	      		console.log("============drop=================");
	      		console.log(info);
	      		console.log(info.draggedEl);
	      		console.log(window.getComputedStyle( info.draggedEl ,null).getPropertyValue('background-color'));
	      		var rgbColor = window.getComputedStyle(info.draggedEl, null).getPropertyValue('background-color');
	      	  // Access dataset properties
	      	    var bgColor = info.draggedEl.dataset.backgroundColor;
	      	    var borderColor = info.draggedEl.dataset.borderColor;
	      	    var textColor = info.draggedEl.dataset.textColor;
	      		console.log(bgColor);
	      		console.log(borderColor);
	      		console.log(textColor);
	      		console.log("============drop=================");
	      		
	      		$('.modal-body form').each(function() {
		      		this.reset();
		  		});
	      		
				var start_date = info.date;
				var end_date = info.date;
				var checkyn = info.allDay ? "Y" : "N";
				setCheckBox(checkyn , modal_calendar, '#in_allDay');
				if(info.allDay){
					$('#reportrange').val(toDateString(start_date) + ' - ' + toDateString(end_date));	
				}else{
					$('#reportrange').val(toDateTimeString(start_date) + ' - ' + toDateTimeString(end_date));
				}
				$("#in_title").val(info.draggedEl.innerText);
	      		fn_calendarColor(rgbToHex(rgbColor));
				$("#in_eventDiv").val("C");
	       		$("#modal_calendar").modal('show');
	       		calendar.refetchEvents(); // 캘린더 이벤트 새로고침
			}
		});
	 	// 캘린더를 그려준다.
	   	calendar.render();

		// 마우스 휠 이벤트 감지
		calendarEl.addEventListener('wheel', function (e) {
		  e.preventDefault();  // 페이지 전체 스크롤 방지
		
		  if (e.deltaY > 0) {
		    calendar.next(); // 다음 주로 이동
		  } else {
		    calendar.prev(); // 이전 주로 이동
		  }
		}, { passive: false });  // preventDefault 허용
	   	
	    /* ADDING EVENTS */
	    var currColor = '#17a2b8' //Red by default
	   	// Color chooser button
	   	$('#color-chooser > li > a').click(function (e) {
	     		e.preventDefault();
	     		// Save color
	     		currColor = $(this).css('color');
	     		// Add color effect to button
			$('#add-new-event').css({
			  'background-color': currColor,
			  'border-color'    : currColor
			});
	   	});
	    	
	   	$('#add-new-event').click(function (e) {
			e.preventDefault()
			// Get value and make sure it is not null
			var val = $('#new-event').val()
			if (val.length == 0) {
					return
			}
			
			// Create events
			var event = $('<div />')
			event.css({
			  'background-color': currColor,
			  'border-color'    : currColor,
			  'color'           : '#fff'
			}).addClass('external-event')
			event.text(val);
			$('#external-events').prepend(event);
			// Add draggable funtionality
			ini_events(event)
			// Remove event from text input
			$('#new-event').val('');
			
		    //color picker with addon
		    $('.my-colorpicker2').colorpicker()
	
		    $('.my-colorpicker2').on('colorpickerChange', function(event) {
		      $('.my-colorpicker2 .fa-square').css('color', event.color.toString());
		    })
		});
	});
	function fn_calendarColor(currColor){
		if(currColor == undefined){
			currColor = '${user_color}';
		}
		$('.modal-color').css({
			'background-color': currColor,
			'border-color'    : currColor
		});
		
		$('#btngroup-color').css({
		  	'background-color': currColor,
		  	'border-color'    : currColor
		});
		$('#in_color').val(currColor);
	};
	
	$('#reportrange').daterangepicker({
		locale: {
		    format: 'YYYY-MM-DD',
		    language: 'ko'
		},
		autoclose: true,		
		todayHighlight: true
	});
	
	/* 캘린더 등록 및 수정 */ 
	function fn_eventCalendar(){
		var param = getJQParams(modal_calendar);
		var type = "script";
		var url = "/scheduler/calendar/eventCalendar.do";
		ajaxCall(url, type, param, fn_eventCalendarResult);
		function fn_eventCalendarResult(){
			$("#modal_calendar").modal('hide');			//menu 수정 Popup Open
	        calendar.refetchEvents(); 					// 캘린더 이벤트 새로고침
		}
	};
	
	function fn_calendar_init(){
		/* SAVE 클릭시 */
		modal_calendar.on('click', '#btn_save', function(){
			fn_eventCalendar();
		});
		
		/* DELETE 클릭시 */
		modal_calendar.on('click', '#btn_del', function(){
			$("#in_eventDiv").val("D");
			fn_eventCalendar();
		});
	};
	
	// RGB를 HEX로 변환하는 함수
	function rgbToHex(rgb) {
	    var result = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
	    return result ? "#" + ((1 << 24) | (parseInt(result[1]) << 16) | (parseInt(result[2]) << 8) | parseInt(result[3])).toString(16).slice(1).toUpperCase() : rgb;
	};
	
	$(document).ready(function(){
		fn_calendar_init();
		  // 컬러 입력 요소 가져오기
	    var colorInput = $("#in_color");
	    // 컬러 변경 이벤트 리스너 추가
	    colorInput.on("input", function () {
	    	fn_calendarColor(colorInput.val()); // 변경된 색상 값 전달
	    });
	});
   	// Color chooser button
   	$('.popColor > ul > li').click(function (e) {
   	    var inputChild = $(this).find("input[type='color']");
        if (inputChild.length) {
            var selectedColor = inputChild.val();
            fn_calendarColor(selectedColor);
        } else {
        	var currColor = $(this).css('color');
     		fn_calendarColor(rgbToHex(currColor));
        }
   	});
</script>
