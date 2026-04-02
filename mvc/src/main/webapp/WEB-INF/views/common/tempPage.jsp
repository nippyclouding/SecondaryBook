<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="header.jsp" />

<div class="max-w-4xl mx-auto px-6 py-16 min-h-[600px]">

  <div class="text-center mb-12">
    <h1 class="text-4xl font-extrabold text-gray-900 mb-4"><c:out value="${page.title}"/></h1>
    <div class="h-1 w-20 bg-primary-600 mx-auto rounded-full"></div>
  </div>

  <div class="prose max-w-none bg-white p-8 rounded-2xl border border-gray-100 shadow-sm text-gray-800 leading-relaxed">
    ${page.content}
  </div>

  <div class="mt-12 flex justify-center gap-4">
    <a href="/" class="px-8 py-3 bg-gray-900 text-white rounded-xl font-bold hover:bg-gray-800 transition shadow-lg hover:shadow-xl transform hover:-translate-y-0.5">
      메인으로 이동
    </a>
    <button onclick="history.back()" class="px-8 py-3 bg-white border border-gray-200 text-gray-700 rounded-xl font-bold hover:bg-gray-50 transition">
      뒤로가기
    </button>
  </div>

</div>

<jsp:include page="footer.jsp" />