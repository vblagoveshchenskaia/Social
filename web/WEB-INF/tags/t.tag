<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@tag description="The fmt:message tag with empty output for an empty key"%><%@attribute name="m" required="true"%><c:if test="${not empty m}"><fmt:message key="${m}" /></c:if>