<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="_csrf" content="${fn:escapeXml(_csrf.token)}">
    <meta name="_csrf_header" content="${fn:escapeXml(_csrf.headerName)}">
    <title><c:out value="${bookclub.name}"/> - 신한북스</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bookclub.css">
</head>
<body>
    <h1> bookclubs </h1>




</body>
</html>
