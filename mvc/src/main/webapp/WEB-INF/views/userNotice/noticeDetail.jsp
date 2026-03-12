<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="../common/header.jsp" />

<div class="bg-white min-h-screen">
    <div class="max-w-4xl mx-auto py-12 px-4">
        <div class="mb-8">
            <button onclick="location.href='/notice'"
                    class="group flex items-center gap-2 text-gray-400 hover:text-gray-900 transition-all">
                <i data-lucide="chevron-left" class="w-5 h-5 group-hover:-translate-x-1 transition-transform"></i>
                <span class="font-medium">공지사항 목록</span>
            </button>
        </div>
        <header class="mb-10 pb-10 border-b border-gray-100">
            <div class="flex items-center gap-2 mb-4">
                <c:if test="${notice.notice_priority == 1}">
                    <span class="px-2.5 py-1 bg-orange-50 text-orange-600 text-[11px] font-bold rounded-md border border-orange-100 uppercase tracking-wider">
                        중요
                    </span>
                </c:if>
                <span class="text-sm text-blue-600 font-semibold tracking-wide">공지사항 안내</span>
            </div>

            <h1 class="text-3xl md:text-4xl font-black text-gray-900 leading-tight mb-6">
                <c:out value="${notice.notice_title}" />
            </h1>

            <div class="flex items-center justify-between text-sm text-gray-400">
                <div class="flex items-center gap-4">
                    <span class="flex items-center gap-1.5">
                        <i data-lucide="user" class="w-4 h-4 text-gray-300"></i>
                        관리자
                    </span>
                    <span class="text-gray-200">|</span>
                    <span class="flex items-center gap-1.5">
                        <i data-lucide="calendar" class="w-4 h-4 text-gray-300"></i>
                        ${notice.crtDtmFormatted}
                    </span>
                </div>
                <div class="flex items-center gap-1.5">
                    <i data-lucide="eye" class="w-4 h-4 text-gray-300"></i>
                    조회 <fmt:formatNumber value="${notice.view_count}" pattern="#,###" />
                </div>
            </div>
        </header>

        <article class="min-h-[400px]">
            <div class="text-gray-700 leading-relaxed text-lg whitespace-pre-wrap break-words">
                <c:out value="${notice.notice_cont}" />
            </div>
        </article>

        <div class="mt-16 pt-10 border-t border-gray-100 flex justify-center">
            <button onclick="location.href='/notice'"
                    class="px-10 py-4 bg-gray-900 text-white rounded-full font-bold hover:bg-gray-800 transition-all shadow-lg shadow-gray-200">
                전체 목록보기
            </button>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />

<script>
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
</script>