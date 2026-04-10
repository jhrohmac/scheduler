<%@ page contentType="text/html; charset=utf-8" %>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<div class="container-fluid" id="div_systemLog_main">
	<div class="row">
		<div class="col-4">
			<div class="card card-info">
					<div class="card-body table-responsive p-2">
						<div class="card-controls"  style="text-align: right">
							<button type="button" class="btn btn-xs btplus" id="main">
								<i class="fa fa-fw fa-plus text-blue right"></i>
							</button>
						</div>
						<table id="processGridOneData" class="table table-hover table-sm" >
							<thead>
								<tr>
									<th>No.</th>
									<th>Process ID</th>
									<th>Process Event</th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="col-8">
			  <!-- 그래프 영역 -->
		        <div class="card mb-3">
			        <div class="card-header ui-sortable-handle" style="cursor: move;">
		                <h3 class="card-title">Direct Chat</h3>
		                <div class="card-tools">
		                  <span title="3 New Messages" class="badge badge-primary">3</span>
		                  <button type="button" class="btn btn-tool" data-card-widget="collapse">
		                    <i class="fas fa-minus"></i>
		                  </button>
		                  <button type="button" class="btn btn-tool" title="Contacts" data-widget="chat-pane-toggle">
		                    <i class="fas fa-comments"></i>
		                  </button>
		                  <button type="button" class="btn btn-tool" data-card-widget="remove">
		                    <i class="fas fa-times"></i>
		                  </button>
		                </div>
		              </div>
		            <div class="card-body">
		                <canvas id="logsOverTimeChart" height="80"></canvas>
		            </div>
		        </div>
		
		        <!-- 로그 Live Tail 영역 -->
		        <div class="card">
		            <div class="card-header ui-sortable-handle" style="cursor: move;">
		                <h3 class="card-title">Direct Chat</h3>
		                <div class="card-tools">
		                  <span title="3 New Messages" class="badge badge-primary">3</span>
		                  <button type="button" class="btn btn-tool" data-card-widget="collapse">
		                    <i class="fas fa-minus"></i>
		                  </button>
		                  <button type="button" class="btn btn-tool" title="Contacts" data-widget="chat-pane-toggle">
		                    <i class="fas fa-comments"></i>
		                  </button>
		                  <button type="button" class="btn btn-tool" data-card-widget="remove">
		                    <i class="fas fa-times"></i>
		                  </button>
		                </div>
		              </div>
		            <div class="card-body" >
		                <table id="logLiveTailGrid" class="table table-hover table-sm" >
		                    <thead>
		                        <tr>
		                            <th>Date</th>
		                            <th>Severity</th>
		                            <th>Message</th>
		                        </tr>
		                    </thead>
		                </table>
		            </div>
		        </div>
		</div>
	</div>
</div>

<div class="modal fade" id="modal_process_info" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h5 class="modal-title" id="process_info_title">process 등록</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span>&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_sortOrder" class="col-sm-4 col-form-label" style="text-align: right;">Process No.</label>
							<div class="col-sm-3">
								<input type="text" id="in_sortOrder" class="form-control input-sm" placeholder="프로세스 순서">
							</div>
						</div>
						<div class="form-group row" id="row_processParent">
							<label for="in_processParent" class="col-sm-4 col-form-label" style="text-align: right;">Process Group</label>
							<div class="col-sm-6" id="PROCESSGROUP">
								<select class="form-control custom-select" id="in_processParent"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_processId" class="col-sm-4 col-form-label" style="text-align: right;">Process ID</label>
							<div class="col-sm-7">
								<div class="input-group">
									<input type="text" class="form-control input-sm" id="in_processId" placeholder="프로세스 ID">
									<div class="input-group-append" id="row_processCheck">
										<span class="input-group-text" style="cursor: pointer;" id="btn_id_duplicate">
											<i class="fas fa-check"></i>
										</span>
									</div>
								</div>								
							</div>
						</div>
						<div class="form-group row" id="row_processvalue">
							<label for="in_processValue" class="col-sm-4 col-form-label" style="text-align: right;">Process Value</label>
							<div class="col-sm-7">
								<input type="text" id="in_processValue" class="form-control input-sm" placeholder="프로세스 ID">
							</div>
						</div>
						<div class="form-group row"  id="row_processName">
							<label for="in_processName" class="col-sm-4 col-form-label" style="text-align: right;">Process Name</label>
							<div class="col-sm-5">
								<input type="text" id="in_processName" class="form-control input-sm" placeholder="프로세스 명">
							</div>
						</div>
						<div class="form-group row"  id="row_processNameEng">
							<label for="in_processNameEng" class="col-sm-4 col-form-label" style="text-align: right;">Process Eng Name</label>
							<div class="col-sm-7">
								<input type="text" id="in_processNameEng" class="form-control input-sm" placeholder="프로세스 영문명">
							</div>
						</div>
						<div class="form-group row"  id="row_processDir">
							<label for="in_processDir" class="col-sm-4 col-form-label" style="text-align: right;">Process Dir</label>
							<div class="col-sm-7">
								<input type="text" id="in_processDir" class="form-control input-sm" placeholder="프로세스 경로">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_processAccess" class="col-sm-4 col-form-label" style="text-align: right;">Process Access</label>
							<div class="col-sm-4 common select-nonall" id="ROLE">
								<select class="form-control custom-select" id="in_processAccess"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useYn" class="col-sm-4 col-form-label" style="text-align: right;">Use YN</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useYn" class="flat-red">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_processDesc" class="col-sm-4 col-form-label" style="text-align: right;">Process DESC</label>
							<div class="col-sm-8">
								<input type="text" id="in_processDesc" class="form-control input-sm" placeholder="프로세스 설명">
							</div>
						</div>
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventDiv" value="">
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

	//각 영역들을 미리 선언
	var div_process_main = $('#div_process_main');
	var div_process_info = $('#modal_process_info');
	var grid_div ="";
	var oneRowdata ="";
	var process_duplicate = "N";
	function fn_process_init(){
		
		/* 조회버튼 클릭시 */
		div_process_main.on('click', '#btn_main_search', function(){
			fn_processGridOneData();
		});
		
		/* 등록버튼 클릭시 */
		div_process_info.on('click', '#btn_save', function(){
			fn_processSave();
		});
	
		/* 삭제 버튼 클릭시 */
		div_process_info.on('click', '#btn_del', function(){
			fn_processDelete();
		});
		
		$(".btplus").click(function(){			
			fn_processInsert(this.id)
		});
		
		$("#btn_id_duplicate").click(function(){
			fn_duplicate(this.id)
		});
	}
	
	function fn_systemLogGridOneData() {
		// 호출 URL
		var url = "/scheduler/system/selectSystemLogs.do";
		//조회 조건
		var param = {
					"sel_parentId" 		: "",
	                "sel_processDiv" 	: "main",
	                "sel_processName"	: $('#sel_processName').val(),
	                "sel_useYn" 		: $('#sel_useYn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": 'rnum'},
						{"data": "process_id"},
						{"data": "process_nm"},
						{"data": "use_flag"},
						{"data": "process_id",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'
								return data;
							}
						}
		           ];
		
		var columnDefs = 
			{
			    "targets": [0,1,3,4],
			    "className": "text-center",
			};
		
		var gridObj = {
		        'grid_id': "processGridOneData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': false
		};

		var gridOptions = {
		        'serverSide': true,
		        'searching': false,
		        'paging': true,
		        'button': false,
		        'lengthChange': false,
		        'info': true,
		        'autoWidth': false,
		        'responsive': true,
		        'bDestroy': true,
		        'processing': true,
		        'ordering': false,
		        'cursorCols': [2,3,4]
	    };
		dataTableGridNew(gridObj, gridOptions);
	}
	
	// 마우스 클릭 이벤트 핸들러
	$('#processGridOneData tbody').on( 'click', 'tr', function () {
			var table = $("#processGridOneData").dataTable();
			// 현재 클릭한 행
		    var $row = $(this);
		    // 다른 행들의 선택 상태를 해제
		    table.$('tr.selected').not($row).removeClass('selected');
		    // 현재 행의 선택 상태를 토글
		    $row.toggleClass('selected');
		    
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
			var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
			oneRowdata = row_data;
			if(column_index == '4'){
				fn_processUpdate("main",row_data);
			}else{
				fn_processGridTwoData(row_data.process_id);
			}
	});
	
	function fn_processGridTwoData(parentid) {
		// 호출 URL
		var url = "/scheduler/system/selectSystemLogs.do";
		//조회 조건
		var param = {
	                "sel_parentId" 		: parentid,
	                "sel_processDiv"	: "sub",
	                "sel_processName"	: "",
	                "sel_useYn" 		: $('#sel_useYn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "rnum"},
						{"data": "process_id"},
						{"data": "process_value"},						
						{"data": "use_flag"},
						{"data": "process_id",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'
								return data;
							}
						}
		           ];
		
		var columnDefs = 
			{
			    "targets": [0,1,2,3,4],
			    "className": "text-center",
			};
		
		var gridObj = {
		        'grid_id': "logLiveTailGrid",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': false
		};
		
		var gridOptions = {
		        'serverSide': true,
		        'searching': false,
		        'paging': true,
		        'button': false,
		        'lengthChange': false,
		        'info': true,
		        'autoWidth': false,
		        'responsive': true,
		        'bDestroy': true,
		        'processing': true,
		        'ordering': false
		        
		    };
		dataTableGridNew(gridObj,gridOptions);
	}

	$('#processGridTwoData tbody').on( 'click', 'tr', function () {
		var gridData = $('#processGridTwoData').dataTable();
	   	var row_position = gridData.fnGetPosition(this);
   		var row_data = gridData.fnGetData(row_position);
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '4'){
			fn_processUpdate("sub",row_data);
		}
	});
	
	//수정
	function fn_processUpdate(div,row_data){
		fn_showHide(div);
		grid_div = div;
		/* 셋팅 초기화 */
		$('.modal-body form').each(function() {
			setCheckBox("N", div_process_info, '#in_useYn');
      		this.reset();
  		});
		process_duplicate ="Y";
		// disabled 처리
		$("#in_processId").attr("disabled",true);
		$('#row_processCheck').hide();
		
		$("#in_processParent").val(row_data.parent_id);
		$("#in_sortOrder").val(row_data.sort_order);
		$("#in_processId").val(row_data.process_id);
		$("#in_processValue").val(row_data.process_value);
		$("#in_processName").val(row_data.process_nm);
		$("#in_processNameEng").val(row_data.process_nm_eng);		
		$("#in_processDir").val(row_data.process_Dir);		
		$("#in_processAccess").val(row_data.access_right);
		
		setCheckBox(row_data.use_flag , div_process_info, '#in_useYn');
		
		$("#in_processDesc").val(row_data.process_desc);
		$("#in_eventDiv").val("update");
		$("#process_info_title").text("프로세스 수정");
		$("#modal_process_info").modal();
	}
	
	//등록
	function fn_processInsert(div){
		
		if(div == "sub" && oneRowdata == ""){
			alert("Main Process를 먼저 선택해주세요.");
			return false;
		}
		fn_showHide(div);
		grid_div = div;
		
		//중복체크 버튼 Show
		$('#row_processCheck').show();
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
			setCheckBox("N", div_process_info, '#in_useYn');
      		this.reset();
  		});
		
		$("#in_eventDiv").val("insert");
		
		/* Process SortOrder */
		fn_processSortOrder();

		// disabled 삭제
		$("#in_processId").removeAttr("disabled"); 
		if(div =='main'){
			$('#row_processParent').hide();
			$('#in_processParent').val("");
		}else{
			$('#row_processParent').show();
			if(oneRowdata.process_id != undefined){
				$("#in_processParent").val(oneRowdata.process_id).prop("selected", true);
			}
		}
		$("#process_info_title").text("프로세스 등록");
		$("#modal_process_info").modal();
	}
	
	function fn_showHide(div){
		if(div =='main'){
			$('#row_processParent').hide();
			$('#row_processvalue').hide();
			$('#row_processName').show();
			$('#row_processNameEng').show();
		}else{
			fn_processGroup();
			// disabled 처리
			$("#in_processParent").attr("disabled",false);
			$('#row_processName').hide();
			$('#row_processNameEng').hide();
			
			$('#row_processParent').show();
			$('#row_processvalue').show();
		}
	}
	/* 프로세스 프로세스 중복 확인 */
	function fn_duplicate(){
		var in_processId = div_process_info.find('#in_processId').val();
	
		if(in_processId == ""){
			alert("프로세스명을 입력하세요.");
			return false;
		}
		var url = "/scheduler/processManagement/selectProcessCheck.do";
		var param = "in_processId="+in_processId;
		var type = "json";
		
		ajaxCall(url, type, param, fn_duplicateResult);
		function fn_duplicateResult(data){
			alert(data.result_msg);
			process_duplicate = data.result_code;
		}
	}
	/* 프로세스 SEQ */ 
	function fn_processSortOrder(){
		var param = "in_parentId="+oneRowdata.process_id+"&in_gridDiv="+grid_div;
		var type = "json";
		var url = "/scheduler/processManagement/selectSortOrder.do";
		
		ajaxCall(url, type, param, fn_processSortOrderResult);
		function fn_processSortOrderResult(data){
			$("#in_sortOrder").val(data.singleData);
		}
	}
	
	/* 프로세스 신규 등록 및 수정 */ 
	function fn_processSave(){
		if(process_duplicate =="N"){
			alert("프로세스 체크를 해주세요.");
			return false;
		};
		var param = getJQParams(div_process_info);
		var type = "script";
		var url = "/scheduler/processManagement/saveProcess.do";
		ajaxCall(url, type, param, fn_processSaveResult);
		function fn_processSaveResult(){
			$("#modal_process_info").modal('hide');			//process 수정 Popup Open
			if(grid_div =="main"){
				fn_processGridOneData();
			}else{
				if(oneRowdata.process_id != undefined){
					fn_processGridTwoData(oneRowdata.process_id );
				}
			}
		}
	}
	
	/* 프로세스 삭제 */ 
	function fn_processDelete(){
		var param = getJQParams(div_process_info);
		var type = "script";
		var url = "/scheduler/processManagement/deleteProcess.do";
		
		ajaxCall(url, type, param, fn_processDeleteResult);
		function fn_processDeleteResult(){
			$("#modal_process_info").modal('hide');			//process 수정 Popup Open
			if(grid_div =="main"){
				fn_processGridOneData();
			}else{
				if(oneRowdata.process_id != undefined){
					fn_processGridTwoData(oneRowdata.process_id);
				}
			}
		}
	}
	
	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_processGroup(){
		var param;
		var type = "html";
		var url = "/scheduler/processManagement/processGroupList.do";
		ajaxCall(url, type, param, fn_processGroupResult);
		function fn_processGroupResult(data){
			div_process_info.find('#PROCESSGROUP select').html(data);
		}
	};
	
	$(document).ready(function(){
		fn_systemLogGridOneData();
		fn_processGridTwoData('1');
		fn_process_init();
	});

	$(document).ready(function () {

	    // -------------------------------
	    // 1) Logs Over Time Chart
	    // -------------------------------
	    const ctx = document.getElementById('logsOverTimeChart').getContext('2d');
	    const logsChart = new Chart(ctx, {
	        type: 'bar',
	        data: {
	            labels: [], // 시간 라벨
	            datasets: [{
	                label: 'Log Count',
	                data: [],
	                backgroundColor: '#007bff'
	            }]
	        },
	        options: {
	            responsive: true,
	            plugins: { legend: { display: false }},
	            scales: {
	                x: { title: { display: true, text: 'Time' }},
	                y: { title: { display: true, text: 'Count' }}
	            }
	        }
	    });

	    // 그래프 데이터 로드 함수
	    function loadLogChart() {
	        ajaxCall('/scheduler/systemLog/getLogStats.do', 'json', {}, function (res) {
	            logsChart.data.labels = res.labels;
	            logsChart.data.datasets[0].data = res.counts;
	            logsChart.update();
	        });
	    }

	    // -------------------------------
	    // 2) Live Tail DataTable
	    // -------------------------------
	    const logTable = $('#logLiveTailGrid').DataTable({
	        ajax: {
	            url: '/scheduler/systemLog/getLiveLogs.do',
	            type: 'POST',
	            dataSrc: ''
	        },
	        columns: [
	            { data: 'date' },
	            { data: 'severity' },
	            { data: 'message' }
	        ],
	        paging: false,
	        searching: false,
	        info: false,
	        scrollY: '300px',
	        scrollCollapse: true
	    });

	    // -------------------------------
	    // 3) 주기적 업데이트
	    // -------------------------------
	   /*  setInterval(function () {
	        logTable.ajax.reload(null, false); // 데이터 갱신
	        loadLogChart(); // 그래프 갱신
	    }, 5000); */

	    // 초기 로드
	    loadLogChart();
	});	
</script>

<style>
/* Live Tail Severity 색상 */
.table td:nth-child(2):contains('ERROR') {
    color: red;
    font-weight: bold;
}
.table td:nth-child(2):contains('WARN') {
    color: orange;
    font-weight: bold;
}
.table td:nth-child(2):contains('INFO') {
    color: blue;
}
</style>