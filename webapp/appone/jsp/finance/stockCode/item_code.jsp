<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="codeVo" items="${codeList}">
	<c:choose>
		<c:when test="${codeVo.getCode_seq() == '0'}">
			<option value="${codeVo.getCode_seq()}" selected>${codeVo.getCode_nm()}</option>
		</c:when>
		<c:otherwise>
			<option value="${codeVo.getCode_seq()}">${codeVo.getCode_nm()}</option>
		</c:otherwise>
	</c:choose>
</c:forEach>