<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="../common/header.jsp" />

<div class="bg-[#F8F9FA] min-h-[calc(100vh-200px)] flex items-center justify-center py-12 px-4 font-sans">
    <div class="bg-white p-10 rounded-[2.5rem] shadow-xl shadow-gray-200/50 border border-gray-100 max-w-2xl w-full text-center animate-fade-in-up">

        <div class="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6 shadow-sm">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
        </div>

        <h1 class="text-3xl font-black text-gray-900 mb-3 tracking-tight">서비스 이용에 불편을 드려 죄송합니다</h1>
        <p class="text-gray-500 text-sm mb-8 leading-relaxed font-medium">
            <c:choose>
                <c:when test="${not empty errorMessage}">
                    ${errorMessage}
                </c:when>
                <c:otherwise>
                    일시적인 시스템 오류가 발생했습니다.<br>
                    잠시 후 다시 시도해 주세요.
                </c:otherwise>
            </c:choose>
        </p>

        <div class="flex justify-center gap-3 mb-8">
            <a href="javascript:history.back()" class="px-6 py-3 bg-gray-100 text-gray-600 rounded-full font-bold text-sm hover:bg-gray-200 transition-all">
                이전 페이지
            </a>
            <a href="/" class="px-6 py-3 bg-gray-900 text-white rounded-full font-bold text-sm hover:bg-black transition-all shadow-lg hover:shadow-xl hover:-translate-y-0.5">
                홈으로 가기
            </a>
        </div>

        <%-- 프로덕션 환경: 스택 트레이스를 사용자에게 노출하지 않음 (서버 로그에서만 확인) --%>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />