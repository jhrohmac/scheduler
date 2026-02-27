<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="itemVo" items="${itemList}">
	<c:choose>
		<c:when test="${itemVo.getFlag() == 'Y'}">
			<option value="${itemVo.getValue()}" selected="selected">${itemVo.getCode()}</option>
		</c:when>
		<c:otherwise>
			<option value="${itemVo.getValue()}">${itemVo.getCode()}</option>
		</c:otherwise>
	</c:choose>
</c:forEach>