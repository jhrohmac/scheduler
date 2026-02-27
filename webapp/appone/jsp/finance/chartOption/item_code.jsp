<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="chartOptionVo" items="${optionList}">
	<c:choose>
		<c:when test="${chartOptionVo.getOption_seq() == '0'}">
			<option value="${chartOptionVo.getOption_seq()}" selected>${chartOptionVo.getOption_nm()}</option>
		</c:when>
		<c:otherwise>
			<option value="${chartOptionVo.getOption_seq()}">${chartOptionVo.getOption_nm()}</option>
		</c:otherwise>
	</c:choose>
</c:forEach>