<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="../common/header.jsp" />

<div class="bg-[#F8F9FA] min-h-[calc(100vh-200px)] flex items-center justify-center py-12 px-4 font-sans">
    <div class="bg-white p-10 rounded-[2.5rem] shadow-xl shadow-gray-200/50 border border-gray-100 max-w-lg w-full text-center animate-fade-in-up">

        <div class="w-20 h-20 bg-orange-50 rounded-full flex items-center justify-center mx-auto mb-6 shadow-sm">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
        </div>

        <h1 class="text-3xl font-black text-gray-900 mb-3 tracking-tight">잘못된 요청입니다</h1>
        <p class="text-gray-500 text-sm mb-8 leading-relaxed font-medium">
            <c:choose>
                <c:when test="${not empty errorMessage}">
                    ${errorMessage}
                </c:when>
                <c:otherwise>
                    요청하신 페이지를 찾을 수 없거나,<br>
                    잘못된 경로로 접근하셨습니다.
                </c:otherwise>
            </c:choose>
        </p>

        <a href="/" class="inline-flex items-center gap-2 bg-gray-900 text-white px-8 py-3.5 rounded-full font-bold text-sm hover:bg-black transition-all shadow-lg hover:shadow-xl hover:-translate-y-0.5">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            홈으로 돌아가기
        </a>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />