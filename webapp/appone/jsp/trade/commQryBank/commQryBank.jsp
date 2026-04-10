<%@ page contentType="text/html; charset=utf-8" %>

<div class="container-fluid" id="div_qryBank_main">
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
				<label class="col-sm-1 col-form-label" style="text-align: right;">조회 기간</label>	
				<div class="col-sm-2">
					<div class="input-group">
						<input type="text" class="form-control form-control-border" id="reportrange" placeholder="날짜를 입력하세요.">
						<div class="input-group-prepend">
							<span class="input-group-check p-0">
								<button type="button" class="btn btn-default float-right p-1" id="daterange-btn">
	                      			<i class="far fa-calendar-alt"></i>
	                      			<i class="fas fa-caret-down"></i>
                    			</button>
							</span>
						</div>
					</div>
				</div>
				<label class="col-sm-1 col-form-label" style="text-align: right;">DESK</label>
				<div class="col-sm-1 common select-all" id="QRYBANK">
					<select class="form-control custom-select custom-select-sm" id="sel_qryDesk"></select>
				</div>
				<label class="col-sm-1 col-form-label" style="text-align: right;">BANK</label>
				<div class="col-sm-1 common select-all" id="QRYBANKALL">
					<select class="form-control custom-select custom-select-sm" id="sel_qryBank"></select>
				</div>
				<div class="col-sm-2">
					<input type="text" id="sel_qrySearch" class="form-control form-control-sm">
				</div>
				<div class="col-sm-3">
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
					<table class="table table-hover table-sm" id="qryBankGridData">
						<thead>
							<tr>
								<th>은행</th>
								<th>딜러</th>
								<th>브로커</th>
								<th>FX</th>
								<th>MM</th>
								<th>IRS</th>
								<th>SUM</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
			</div>
			<div class="card">
				<div class="card-body table-responsive p-2">
					<table class="table table-hover table-sm" id="qryBankSUMGridData">
						<thead>
							<tr>
								<th>은행</th>
								<th>딜러</th>
								<th>브로커</th>
								<th>FX</th>
								<th>MM</th>
								<th>IRS</th>
								<th>TOTAL</th>
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
	var div_qryBank_main = $('#div_qryBank_main');

	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	div_qryBank_main.on('click', '#btn_main_search', function() {
		//fn_qryBankGridData();
	});
	
	function fn_qryBank(){
		fn_qryBankGridData();
		fn_qryBankSUMGridData();
	};
	
	$(document).ready(function () {
		
		$('#reportrange').daterangepicker({
			locale: {
			    format: 'YYYY-MM-DD',
			    language: 'ko'
			},
			autoclose: true,		
			todayHighlight: true
		});
	    
	    const selQryInfo = $('#sel_qryDesk');
	    const inputContainer = $('.col-sm-3');
	    selQryInfo.change(function () {
	        // 현재 선택된 값 가져오기
	        const selectedValue = $(this).val();
	        const selectedText = $(this).find('option:selected').text();
	        console.log(selectedText);
	        $('#label_qryBank').text(selectedText);
	    });
	    fn_qryBank();
	});
	
	/************************************************************************
	* qryBank list
	*************************************************************************/
	function fn_qryBankGridData(){
		
		// 호출 URL
		var url = "/scheduler/commQryBank/selectQryBankAll.do";
		//조회 조건
		var param = {
				 	"reportrange" 	: $('#reportrange').val(),				 	
				 	"sel_qrySearch"	: $('#sel_qrySearch').val(),
	                "sel_qryDesk"	: $('#sel_qryDesk').val()
		};
		//컬럼 옵션
		var columns = [
						{"data": "bank_nm"},
						{"data": "dealer_nm"},
						{"data": "fx_amt",
							"render": function(data, type, row){
								return fn_toNumber(data);
							}
          				},
						{"data": "mm_amt",
							"render": function(data, type, row){
								return fn_toNumber(data);
							}
          				},
						{"data": "irs_amt",
							"render": function(data, type, row){
								return fn_toNumber(data);
							}
          				},
						{"data": "m_sum",
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
		        'grid_id': "qryBankGridData",
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

	function fn_qryBankSUMGridData(){

		// 호출 URL
		var url = "/scheduler/commQryBank/selectQryBankSUMAll.do";
		//조회 조건
		var param = {
				 	"reportrange" 	: $('#reportrange').val(),				 	
				 	"sel_qrySearch"	: $('#sel_qrySearch').val(),
	                "sel_qryDesk"	: $('#sel_qryDesk').val()
		};
		//컬럼 옵션
		var columns = [
						{"data": "bank_nm"},
						{"data": "dealer_nm"},
						{"data": "fx_amt",
							"render": function(data, type, row){
								return fn_toNumber(data);
							}
          				},
						{"data": "mm_amt",
							"render": function(data, type, row){
								return fn_toNumber(data);
							}
          				},
						{"data": "irs_amt",
							"render": function(data, type, row){
								return fn_toNumber(data);
							}
          				},
						{"data": "m_sum",
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
		        'grid_id': "qryBankSUMGridData",
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
	$('#daterange-btn').daterangepicker({
	    linkedCalendars: false,  
	    alwaysShowCalendars: true,
	    ranges: {
	        'Today': [moment(), moment()],
	        '30일전': [moment().subtract(29, 'days'), moment()],
	        '이번달': [moment().startOf('month'), moment().endOf('month')],
	        '저번달': [
	            moment().subtract(1, 'month').startOf('month'),
	            moment().subtract(1, 'month').endOf('month')
	        ],
	        '6개월전': [
	            moment().subtract(6, 'months').startOf('month'), 
	            moment().subtract(1, 'months').endOf('month')
	        ],
	        '1년전': [
	            moment().subtract(1, 'years').startOf('month'), 
	            moment().subtract(1, 'months').endOf('month')
	        ]
	    },
	    startDate: moment(), // 오늘로 설정
	    endDate: moment(),   // 오늘로 설정
	    locale: {
	        format: 'YYYY-MM-DD',
	        language: 'ko'
	    }
	}, function (start, end) {
	    $('#reportrange').val(start.format('YYYY-MM-DD') + ' - ' + end.format('YYYY-MM-DD'));
	    fn_qryBank();
	});
	
</script>