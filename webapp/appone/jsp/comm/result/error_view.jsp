<%@ page contentType="text/html; charset=utf-8" %>
<!-- Main content -->
<%
	String	error_code = (String)request.getAttribute("error_code");
	String	error_msg = (String)request.getAttribute("error_msg");
	String	error_dtl = (String)request.getAttribute("error_dtl");
%>

<div class="error-page">
	<h3 class="headline text-warning">${error_code}</h3>
	<div class="error-content">
		<h3>
			<i class="fas fa-exclamation-triangle text-warning"></i>
			${error_dtl}
		</h3>
		<%-- <h6>${error_dtl}</h6> --%>
		<form class="search-form">
			<div class="input-group">
				<button type="button" class="btn btn-default btn-lrg ajax"
					title="Ajax Request"
					onclick="javascript:fn_PageMove('${default_page}');">
					<i class="fa fa-spin fa-refresh"></i>&nbsp; Come back Home
				</button>
			</div>
		</form>
	</div>
</div>
<script type="text/javascript">
// Modal 배경 제거
$('.modal-backdrop').remove();
$('body').removeClass('modal-open');
</script>