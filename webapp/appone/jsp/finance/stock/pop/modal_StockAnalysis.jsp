<%@page import="org.apache.poi.util.SystemOutLogger"%>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="groupMarket" value="${groupMarket}"/>
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
				<h3><label>${modal_id} Options</label></h3>
				<div class="row">
					<div class="col-sm-6">
						<div class="form-group">
							<label>SORT FLAG</label>
							<select class="custom-select" id="set_id">
								<c:forEach var="codeVo" items="${codeList}">
								    <c:set var="code_id" value="${codeVo.getCode_id()}"/>
								    <c:set var="market" value="${fn:substring(code_id,0,2)}"/>
								    <c:set var="from" value="${fn:indexOf(code_id, 'FROM')}"/>
								    
									<c:set var="default_yn" value="${codeVo.getDefault_yn()}" />	<!-- 평균갯수 -->
								    
								    <!-- Market all, 코스피ㅡ코스닥, 나스닥-다우 setValue  -->
								    <c:if test="${market eq groupMarket}">
								    	
								    	<c:set var="stockMarket" value="${fn:indexOf(code_id, 'STOCK_MARKET')}"/>
								    	<c:if test="${stockMarket ne '-1'}">
								    		<c:set var="stockMarketValue" value="${codeVo.getCode_value()}"/>
								    	</c:if>
										<c:choose>
											<c:when test="${from ne '-1'}">
												<c:set var="range_from" value="${codeVo.getCode_value()}"/>
											</c:when>
											<c:otherwise>
												<c:set var="range_to" value="${codeVo.getCode_value()}"/>
											</c:otherwise>
										</c:choose>
									</c:if>
									
									<!-- SELECT BOX LIST SET -->
									<c:set var="select" value="${fn:indexOf(code_id, '-')}"/>
									<c:if test="${select eq '-1'}">
										<c:choose>
											<c:when test="${default_yn eq 'Y'}">
												<c:set var="default_val" value="${codeVo.getCode_value()}"/>
												<option value="${codeVo.getCode_id()}" selected>${codeVo.getCode_nm()}</option>
											</c:when>
											<c:otherwise>
												<option value="${codeVo.getCode_id()}" >${codeVo.getCode_nm()}</option>	
											</c:otherwise>
										</c:choose>
									</c:if>
								</c:forEach>
							</select>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="form-group">
							<label>SORT ORDER</label>
							<select class="custom-select" id="set_value">
								<option value="DESC">DESC</option>
								<option value="ASC">ASC</option>
							</select>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-sm-6">
						<div class="form-group">
							<label>SMA LINES</label>
							<select class="custom-select" id="set_id">
								<option value="" selected>ALL</option>
								<option value="5,20,60,120,240">5,20,60,120,240</option>
								<option value="5,20,60,240,120">5,20,60,240,120</option>
								<option value="5,20,60,120,240">5,20,60,120,240</option>
								<option value="5,20,60,120,240">5,20,60,120,240</option>
								<option value="5,20,60,120,240">5,20,60,120,240</option>
							</select>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="form-group">
							<label>SORT ORDER</label>
							<select class="custom-select" id="set_value">
								<option value="DESC">DESC</option>
								<option value="ASC">ASC</option>
							</select>
						</div>
					</div>
				</div>
				<h3><label>INDICATORS</label></h3>
				<div class="row">
					<div class="col-sm-6">
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
					<div class="col-sm-6">
						<div class="form-group">
							<div class="custom-control custom-radio">
								<input class="custom-control-input" type="radio" id="R-ALL" name="marketRadio"  value=""> 
								<label for="R-ALL" class="custom-control-label">ALL</label>
							</div>
							<c:if test="${groupMarket eq 'US'}">
								<div class="custom-control custom-radio">
									<input class="custom-control-input" type="radio" id="R-NASDAQ" name="marketRadio"  value="nasdaq" > 
									<label for="R-NASDAQ" class="custom-control-label">나스닥</label>
								</div>
								<div class="custom-control custom-radio">
									<input class="custom-control-input" type="radio" id="R-NYSE" name="marketRadio" value="nyse"> 
									<label for="R-NYSE" class="custom-control-label">NYSE</label>
								</div>
							</c:if>
							<c:if test="${groupMarket eq 'KR'}">
								<div class="custom-control custom-radio">
									<input class="custom-control-input" type="radio" id="R-KOSPI" name="marketRadio" value="kospi"> 
									<label for="R-KOSPI" class="custom-control-label">코스피</label>
								</div>
								<div class="custom-control custom-radio">
									<input class="custom-control-input" type="radio" id="R-KOSDAQ" name="marketRadio"  value="kosdaq" > 
									<label for="R-KOSDAQ" class="custom-control-label">코스닥</label>
								</div>
							</c:if>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-sm-10">
						<div class="form-group">
		                    <input id="price_range" type="text" name="price_range" value="" >
						</div>
					</div>
					<div class="col-sm-2  d-flex align-items-center justify-content-center">
						<div class="form-group">
		                    <button type="button" class="btn btn-xs btn-success" id="range_sumit">적용</button>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer justify-content-end p-1">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<input type="hidden" id="modal_id" value="${modal_id}">
				<input type="hidden" id="code_id" value="">
				<input type="hidden" id="code_value" value="">
				<input type="hidden" id="code_reset" value="">
			</div>
		</div>
	</div>
</div>
<script>
	$('input:radio[name="marketRadio"]').change(function(){
			$("#code_reset").val("N");
			$("#code_id").val(groupMarket+"-STOCK_MARKET");
			$("#code_value").val(this.value);
			fn_saveModalOptions();
	});
	
	/*Modal Options Save*/
	$("#range_sumit").click(function() {
		// Saving it's instance to var
		var slider = $("#price_range").data("ionRangeSlider");
		// Get values
		var from = slider.result.from;
		var to = slider.result.to;
		var arrNm = new Array(groupMarket+"-RANGE_FROM", groupMarket+"-RANGE_TO");
		var arrVal = new Array(from, to); 
		$("#code_reset").val("N");
		for (var i = 0; i < arrNm.length; i++) {
			$("#code_id").val(arrNm[i]);
			$("#code_value").val(arrVal[i]);
			fn_saveModalOptions();
		} 
	});
	
	$("#set_id , #set_value").change(function() {
		$("#code_reset").val("Y");
		$("#code_id").val($("#set_id").val());
		$("#code_value").val($("#set_value").val());
		fn_saveModalOptions();
	});
	
	$('.class_'+'${modal_id}').change(function(){
		$("#code_reset").val("N");
		var code_id = this.id;
		var code_value = this.value;
		var code = code_id.split('-')
		if(code[0] == "F"){
			code_value = document.getElementById(code_id).checked;
			if(code_value == true ){
				code_value ="Y";
			}else{
				code_value ="N";
			}
		}
		$("#code_id").val(code_id);
		$("#code_value").val(code_value);
		fn_saveModalOptions();
	});
	function fn_saveModalOptions(){
		var modal_id = $("#modal_id").val();
		var url = "/scheduler/finance/saveModalOptions.do";
		var param = getJQParams($("#modal_" + modal_id));
		var type = "json";
		fn_sendAjax(url, type, param, fn_saveModalOptionsResult);
		/* Link System Display */
		function fn_saveModalOptionsResult(data) {
			fn_watchtab();
		}
	};
	
	//Page Init Functions
	$(document).ready(function() {
		var min = 0;
		var max = '${range_to}' * 2;
		var from = '${range_from}';
		var to = '${range_to}';
		var step;
		var prefix;
		if (groupMarket == "KR") {
			
			step = 10000;
			prefix = "₩";
		} else {
			/* min = 50;
			max = 1000;
			from = 50;
			to = 900; */
			step = 10;
			prefix = '$';
		}
		/* ION SLIDER */
		$('#price_range').ionRangeSlider({
			min : min,
			max : max,
			from : from,
			to : to,
			type : 'double',
			step : step,
			prefix : prefix,
			prettify : false,
			hasGrid : true,
			grid : true,
			prettify_enabled : true,
			prettify_separator : ","
		});
		
		var setvalue = '${stockMarketValue}';
		var radioButtons = document.getElementsByName("marketRadio");
		for (var i = 0; i < radioButtons.length; i++) {
		    if (radioButtons[i].value === setvalue) {
		        radioButtons[i].checked = true;
		        break;
		    }
		}
	});
	$("#set_value").val('${default_val}');
</script>