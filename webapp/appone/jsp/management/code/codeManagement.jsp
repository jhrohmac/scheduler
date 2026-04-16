<%@ page contentType="text/html; charset=utf-8" %>
<link rel="stylesheet" href="/scheduler/appone/jsp/management/code/css/codeManagement.css">
<div class="container-fluid" id="div_code_main">
	<div class="card card-primary card-outline">
		<div class="card-header pb-2 pt-2">
			<h5 class="card-title">조회 조건</h5>
			<div class="card-tools">
				<button type="button" class="btn btn-tool"
					data-card-widget="collapse" title="Collapse">
					<i class="fas fa-minus"></i>
				</button>
			</div>
		</div>
		<div class="card-body p-2">
			<div class="form-group search_row">
				<label for="sel_codeName" class="col-sm-1 col-form-label" style="text-align: right;">Code Name</label>
				<div class="col-sm-3">
					<input type="text" id="sel_codeName" name="sel_codeName" onkeyup="fn_codeGridOneData();"class="form-control form-control-sm" placeholder="코드 명">
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn" onchange="fn_codeGridOneData();">
					</select>
				</div>
				<div class="col-sm-6">
					<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right">
						<i class="fa fa-search"></i>
					</button>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-4">
			<div class="card card-info">
					<div class="card-body table-responsive p-2">
						<div class="card-controls"  style="text-align: right">
							<button type="button" class="btn btn-xs btPlus" id="main">
								<i class="fa fa-fw fa-plus text-blue right"></i>
							</button>
						</div>
						<table class="table table-hover table-sm" id="codeGridOneData">
							<thead>
								<tr>
									<th>No.</th>
									<th>Code ID</th>
									<th>Code Name</th>
									<th>Y/N</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="col-8">
			<div class="card">
				<div class="card-body table-responsive p-2">
				<div class="card-controls" style="text-align: right">
					<button type="button" class="btn btn-xs btPlus" id="sub">
						<i class="fa fa-fw fa-plus text-blue"></i>
					</button>
				</div>
					<table class="table table-hover table-sm" id="codeGridTwoData">
						<thead>
							<tr>
								<th>No.</th>
								<th>Code ID</th>
								<th>Code Value</th>
								<th>Y/N</th>
								<th></th>
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

<div class="modal fade" id="modal_code_info" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title" id="code_info_title">code 등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span>&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_sortOrder" class="col-sm-4 col-form-label" style="text-align: right;">Code No.</label>
							<div class="col-sm-3">
								<input type="text" id="in_sortOrder" class="form-control input-sm" placeholder="코드 순서">
							</div>
						</div>
						<div class="form-group row" id="row_codeParent">
							<label for="in_codeParent" class="col-sm-4 col-form-label" style="text-align: right;">Code Group</label>
							<div class="col-sm-6" id="CODEGROUP">
								<select class="form-control custom-select" id="in_codeParent"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_codeId" class="col-sm-4 col-form-label" style="text-align: right;">Code ID</label>
							<div class="col-sm-7">
								<div class="input-group">
									<input type="text" class="form-control input-sm" id="in_codeId" placeholder="코드 ID">
									<div class="input-group-append">
										<span class="input-group-text" style="cursor: pointer;" id="btn_duplicate">
											<i class="fas fa-check"></i>
										</span>
									</div>
								</div>								
							</div>
						</div>
						<div class="form-group row" id="row_codeValue">
							<label for="in_codeValue" class="col-sm-4 col-form-label" style="text-align: right;">Code Value</label>
							<div class="col-sm-7">
								<input type="text" id="in_codeValue" class="form-control input-sm" placeholder="코드 ID">
							</div>
						</div>
						<div class="form-group row"  id="row_codeName">
							<label for="in_codeName" class="col-sm-4 col-form-label" style="text-align: right;">Code Name</label>
							<div class="col-sm-5">
								<input type="text" id="in_codeName" class="form-control input-sm" placeholder="코드 명">
							</div>
						</div>
						<div class="form-group row"  id="row_codeNameEng">
							<label for="in_codeNameEng" class="col-sm-4 col-form-label" style="text-align: right;">Code Eng Name</label>
							<div class="col-sm-7">
								<input type="text" id="in_codeNameEng" class="form-control input-sm" placeholder="코드 영문명">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_defaultYn" class="col-sm-4 col-form-label" style="text-align: right;">Default YN</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_defaultYn" class="flat-red">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useYn" class="col-sm-4 col-form-label" style="text-align: right;">Use YN</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useYn" class="flat-red">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_codeDesc" class="col-sm-4 col-form-label" style="text-align: right;">Code DESC</label>
							<div class="col-sm-8">
								<input type="text" id="in_codeDesc" class="form-control input-sm" placeholder="코드 설명">
							</div>
						</div>
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_gridDiv" value="">
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
<script src="/scheduler/appone/jsp/management/code/js/codeManagement.js"></script>
