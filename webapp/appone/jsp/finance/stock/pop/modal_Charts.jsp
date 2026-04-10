<%@page import="org.apache.poi.util.SystemOutLogger"%>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="modal fade" id="modal_${modal_id}">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header p-2">
				<h4 class="modal-title text-sm"><i class="far fa-chart-bar" id="title_${modal_id}"></i></h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body p-2">
				<h3><label>Chart Options</label></h3>
				<div class="row">
					<div class="col-sm-4">
						<div class="form-group">
							<h3><label>LIMIT</label></h3>
							<c:forEach var="codeVo" items="${codeList}">
							    <c:set var="code_id" value="${codeVo.getCode_id()}"/>
							    <c:set var="limit" value="${fn:indexOf(code_id, 'I-')}"/>
							    <c:if test="${limit ne '-1'}">
							    	<c:set var="limit_val" value="${codeVo.getCode_value()}"/>
							    	<input type="text" class="form-control class_${modal_id}" id="${code_id}" 
							    	value="${codeVo.getCode_value()}" maxlength="3">
							    </c:if>
						    </c:forEach>
						</div>
					</div>
				</div>
				<h3><label>INDICATORS</label></h3>
				<div class="row">
					<div class="col-sm-12">
						<div class="form-group">
						<c:forEach var="codeVo" items="${codeList}">
						    <c:set var="code_id" value="${codeVo.getCode_id()}"/>
						    <c:set var="code_div" value="${fn:indexOf(code_id, 'F-')}"/>
						    <c:set var="code_val" value="${codeVo.getCode_value()}"/>
							<c:if test="${code_div ne '-1'}">
								<div class="custom-control custom-switch custom-switch-on-success ">
									<input type="checkbox" class="custom-control-input class_${modal_id}" id="${codeVo.getCode_id()}" value="${code_val}" 
										<c:if test="${code_val eq 'Y'}">checked</c:if>>
									<label class="custom-control-label" for="${codeVo.getCode_id()}">${codeVo.getCode_nm()}</label>
								</div>
							</c:if>
						</c:forEach>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer justify-content-end p-1">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<input type="hidden" id="modal_id" value="${modal_id}">
			</div>
		</div>
	</div>
</div>
<script>
$('.class_'+'${modal_id}').change(function(){
	// USER DEFAULT STOCK SETTING
	var modal_id = $("#modal_id").val();
	var code_reset = "N";
	var code_id = this.id;
	var code_value = this.value;
	if(code_id != "I-LIMIT"){
		code_value = document.getElementById(code_id).checked;
		if(code_value == true ){
			code_value ="Y";
		}else{
			code_value ="N";
		}
	}else{
		/* if(code_value > 600){
			alert("600이상은 조회 할수 없습니다.");
			return false;
		} */
	}
	/*Modal Options Save*/
	var url = "/scheduler/finance/saveModalOptions.do";
	var param = "modal_id="+modal_id+"&code_id="+code_id+"&code_value="+code_value+"&code_reset="+code_reset;
	var type = "json";
	fn_sendAjax(url, type, param, fn_saveOptionsResult);
	/* Link System Display */
	function fn_saveOptionsResult(data) {
		var modal_id = $("#modal_id").val();
		if(modal_id == "MarketChart"){
			fn_MarketDetails();	
		}else{
			fn_Chart();
		}
	}
});
$("#LIMIT").val('${limit_val}');
</script>
