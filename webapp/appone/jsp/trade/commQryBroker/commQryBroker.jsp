<%@ page contentType="text/html; charset=utf-8" %>

<div class="container-fluid" id="div_qryBroker_main">
	<div class="card card-primary card-outline">
		<div class="card-header pb-2 pt-2">
			<h3 class="card-title">조회 조건</h3>
			<div class="card-tools">
				<button type="button" class="btn btn-tool"
					data-card-widget="collapse" title="Collapse">
					<i class="fas fa-minus"></i>
				</button>
			</div>
		</div>
		<div class="card-body p-2">
			<div class="form-group search_row">
				<label class="col-sm-1 col-form-label" style="text-align: right;">조회 년도</label>
				<div class="col-sm-1">
					<div class="input-group date" id="reservationdate" data-target-input="nearest">
	                    <input type="text" class="form-control form-control-sm datetimepicker-input"
	                    	id="sel_qryDate" style="text-align: center;" data-target="#reservationdate" readonly="readonly"/>
	                    <div class="input-group-append" data-target="#reservationdate" data-toggle="datetimepicker">
	                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
	                    </div>
	                </div>
				</div>
				<label class="col-sm-1 col-form-label" style="text-align: right;">부서</label>
				<div class="col-sm-1 common select-all" id="QRYBANK">
					<select class="form-control custom-select custom-select-sm" id="sel_qryDESK"></select>
				</div>
				<label class="col-sm-1 col-form-label" style="text-align: right;">조회 조건</label>
				<div class="col-sm-1 common select-all" id="QRYBROKER">
					<select class="form-control custom-select custom-select-sm" id="sel_qryInfo"></select>
				</div>
				<div class="col-sm-2">
					<input type="text" id="sel_qryBroker" class="form-control form-control-sm">
				</div>
				<div class="col-sm-4">
					<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right">
						<i class="fa fa-search"></i>
					</button>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-12">
			<div class="card">
				<div class="card-body table-responsive p-2">
					<table class="table table-hover table-sm" id="qryBrokerGridData">
						<thead>
							<tr>
								<th>DESK</th>
								<th>브로커</th>
								<th>은행</th>
								<th>딜러</th>
								<th>SUM</th>
								<th>JAN</th>
								<th>FEB</th>
								<th>MAR</th>
								<th>APR</th>
								<th>MAY</th>
								<th>JUN</th>
								<th>JUL</th>
								<th>AUG</th>
								<th>SEP</th>
								<th>OCT</th>
								<th>NOV</th>
								<th>DEC</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
			</div>
			<div class="card">
				<div class="card-body table-responsive p-2">
					<table class="table table-hover table-sm" id="qryBrokerSUMGridData">
						<thead>
							<tr>
								<th>DESK</th>
								<th>TOTAL</th>
								<th>1월</th>
								<th>2월</th>
								<th>3월</th>
								<th>4월</th>
								<th>5월</th>
								<th>6월</th>
								<th>7월</th>
								<th>8월</th>
								<th>9월</th>
								<th>10월</th>
								<th>11월</th>
								<th>12월</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>
<script>
	/* 각 역영들을 미리 선언 */
	var div_qryBroker_main = $('#div_qryBroker_main');
	
	$(function () {
	    /* 검색 키워드 입력 또는 조회 버튼 클릭 시 */
	    div_qryBroker_main.on('keyup', '#sel_qryBroker',  function(){
	    	fn_qryBroker();
	    });
	});

	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	div_qryBroker_main.on('click', '#btn_main_search', function() {
		fn_qryBroker();
	});
	
	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	div_qryBroker_main.on('change', '#QRYBANK #QRYBROKER', function() {
		fn_qryBroker();
	});
	
   	$('#sel_qryDESK').on('change', function() {
   		fn_qryBroker();
   	});
   	$('#sel_qryInfo').on('change', function() {
   		fn_qryBroker();
   	});
	
	$('#reservationdate').on('change.datetimepicker', function (e) {
		fn_qryBroker();
    });
	
	function fn_qryBroker(){
		fn_qryBrokerGridData();
        fn_qryBrokerSUMGridData();
	};
	
	$(document).ready(function () {
		//Date picker
	    $('#reservationdate').datetimepicker({
	        format: 'YYYY'
	    });

        // 현재 연도로 기본값 설정
        const currentYear = new Date().getFullYear();
        $('#sel_qryDate').val(currentYear);
                
	    const selQryInfo = $('#sel_qryInfo');
	    const inputContainer = $('.col-sm-3');
	    selQryInfo.change(function () {
	        // 현재 선택된 값 가져오기
	        const selectedValue = $(this).val();
	        const selectedText = $(this).find('option:selected').text();
	        console.log(selectedText);
	        $('#label_qryBroker').text(selectedText);
	        
	        //$('#sel_qryBroker').val("");
	        
	    });
	    fn_qryBrokerGridData();
	    fn_qryBrokerSUMGridData();
	});
	
	/************************************************************************
	* qryBroker list
	*************************************************************************/
	function fn_qryBrokerGridData(){
		
		// 호출 URL
		var url = "/scheduler/commQryBroker/selectQryBrokerAll.do";
		//조회 조건
		var param = {
				 	"sel_qryDate" 		: $('#sel_qryDate').val(),
				 	"sel_qryDESK" 		: $('#sel_qryDESK').val(),
	                "sel_qryInfo"		: $('#sel_qryInfo').val(),
				 	"sel_qryBroker" 	: $('#sel_qryBroker').val()
		};
		//컬럼 옵션
		var columns = [
					{"data": "desk_cd"},
					{"data": "broker_nm"},
					{"data": "bank_nm"},
					{"data": "dealer_nm"},
					{"data": "m_sum",
						"render": function(data, type, row){
							return fn_toNumber(data);
						}
         				}, 
					{"data": "m_jan",
						"render": function(data, type, row){
							return fn_toNumber(data);
                 			}
					}, 
					{"data": "m_feb",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_mar",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_apr",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_may",
						"render": function(data, type, row){
							return fn_toNumber(data);
						}
					},
					{"data": "m_jun",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_jul",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_aug",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_sep",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_oct",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_nov",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_dec",
						"render": function(data, type, row){
                   			return fn_toNumber(data);
							}
					}
	           ];
		
		var columnDefs = {
                    "targets": [0], // 가격과 수량 열의 인덱스
			    	"className": "text-center"
		};
	    
		var gridObj = {
		        'grid_id': "qryBrokerGridData",
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
	};
	
	function fn_qryBrokerSUMGridData(){
		// 호출 URL
		var url = "/scheduler/commQryBroker/selectQryBrokerSUMAll.do";
		//조회 조건
		var param = {
			 	"sel_qryDate" 		: $('#sel_qryDate').val(),
			 	"sel_qryDESK" 		: $('#sel_qryDESK').val(),
                "sel_qryInfo"		: $('#sel_qryInfo').val(),
			 	"sel_qryBroker" 	: $('#sel_qryBroker').val()
		};
		//컬럼 옵션
		var columns = [
					{"data": "desk_cd"},
					{"data": "m_sum",
						"render": function(data, type, row){
							return fn_toNumber(data);
						}
         			}, 
					{"data": "m_jan",
						"render": function(data, type, row){
							return fn_toNumber(data);
                 			}
					}, 
					{"data": "m_feb",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_mar",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_apr",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_may",
						"render": function(data, type, row){
							return fn_toNumber(data);
						}
					},
					{"data": "m_jun",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_jul",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_aug",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_sep",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_oct",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_nov",
						"render": function(data, type, row){
							return fn_toNumber(data);
							}
					},
					{"data": "m_dec",
						"render": function(data, type, row){
                   			return fn_toNumber(data);
							}
					}
	           ];
		
		var columnDefs = {
                    "targets": [1,2,3,4,5,6,7,8,9,10,11,12,13], // 가격과 수량 열의 인덱스
			    	"className": "text-right"
		};
	    
		var gridObj = {
		        'grid_id': "qryBrokerSUMGridData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': false
		};
		
		var gridOptions = {
		        'serverSide': true,
		        'searching': false,
		        'paging': false,
		        'button': false,
		        'lengthChange': false, 
		        'info': false,
		        'autoWidth': false,
		        'responsive': true,
		        'bDestroy': true,
		        'processing': true,
		        'ordering': false
	    };
		dataTableGridNew(gridObj,gridOptions);
	};
	
</script>