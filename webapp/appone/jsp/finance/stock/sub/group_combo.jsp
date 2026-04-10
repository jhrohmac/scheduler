<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="groupInfo" items="${groupList}" varStatus="loopStatusCode">
	<c:choose>
		<c:when test="${groupInfo.getGroup_id() == '0'}">
			<option value="${groupInfo.getGroup_id()}" selected>${groupInfo.getGroup_name()}</option>
		</c:when>
		<c:otherwise>
			<option value="${groupInfo.getGroup_id()}">${groupInfo.getGroup_name()}</option>
		</c:otherwise>
	</c:choose>
  <c:if test="${loopStatusCode.first}">
<script>
	fn_selectGroupStockList('${groupInfo.getGroup_id()}');
</script>
  </c:if>
</c:forEach>