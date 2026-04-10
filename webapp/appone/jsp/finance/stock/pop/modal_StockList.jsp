<%@page import="org.apache.poi.util.SystemOutLogger"%>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="modal fade" id="modal_${modal_id}">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header p-2">
				<h4 class="modal-title text-sm"><i class="far fa-chart-bar" id="title_${modal_id}"> ${modal_id}</i></h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body p-2">
				<form>
					<h3><label>${modal_id} Options</label></h3>
					<div class="row">
						<div class="col-sm-6">
							<div class="form-group">
								<label>SORT FLAG</label>
								<select class="custom-select" id="code_id">
									<c:forEach var="codeVo" items="${codeList}">
										<c:set var="default_yn" value="${codeVo.getDefault_yn()}" />	<!-- 평균갯수 -->
										<c:choose>
											<c:when test="${default_yn eq 'Y'}">
												<c:set var="default_val" value="${codeVo.getCode_value()}"/>
												<option value="${codeVo.getCode_id()}" selected>${codeVo.getCode_nm()}</option>
											</c:when>
											<c:otherwise>
												<option value="${codeVo.getCode_id()}" >${codeVo.getCode_nm()}</option>	
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</select>
							</div>
						</div>
						<div class="col-sm-6">
							<div class="form-group">
								<label>SORT ORDER</label>
								<select class="custom-select" id="code_value">
									<option value="DESC">DESC</option>
									<option value="ASC">ASC</option>
								</select>
							</div>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer justify-content-end p-1">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<input type="hidden" id="modal_id" value="${modal_id}">
				<input type="hidden" id="code_reset" value="Y">
			</div>
		</div>
	</div>
</div>
<script>

/*Modal Options Save*/
$("#code_id , #code_value" ).change(function(){
	var modal_id = $("#modal_id").val();
	var url = "/scheduler/finance/saveModalOptions.do";
	var param = getJQParams($("#modal_"+modal_id));
	var type = "json";
	fn_sendAjax(url, type, param, fn_saveModalOptionsResult);
		
	/* Link System Display */
	function fn_saveModalOptionsResult(data) {
		 fn_selectGroupStockList();
	}
});
$("#code_value").val('${default_val}');
</script>