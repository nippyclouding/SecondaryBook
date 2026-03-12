<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script src="/resources/js/paging/paging.js"></script>

<jsp:include page="../common/header.jsp" />

<div class="bg-[#F8F9FA] min-h-[calc(100vh-200px)] py-12 animate-[fadeIn_0.4s_ease-out]">
    <div class="max-w-4xl mx-auto px-6">

        <div class="mb-10 text-center sm:text-left">
            <h2 class="text-3xl font-black text-gray-900 tracking-tighter">공지사항</h2>
            <p class="mt-2 text-sm text-gray-500 font-medium">SecondHand Books의 새로운 소식과 안내를 확인하세요.</p>
        </div>

        <div class="bg-white rounded-[2rem] shadow-sm border border-gray-100 overflow-hidden">
            <div id="userNoticeList" class="divide-y divide-gray-50">
                <c:forEach var="notice" items="${result.list}">
                    <div class="group relative flex items-center justify-between py-5 px-6 sm:px-8 cursor-pointer hover:bg-primary-50/30 transition-all duration-200"
                         onclick="location.href='/notice/view?notice_seq=${notice.notice_seq}'">

                        <div class="flex items-center gap-4 flex-1 min-w-0 pr-4">
                            <c:if test="${notice.notice_priority == 1}">
                                <span class="flex-shrink-0 inline-flex items-center justify-center px-2 py-1 bg-red-50 text-red-500 text-[10px] font-bold rounded-md uppercase tracking-wider border border-red-100">
                                    중요
                                </span>
                            </c:if>

                            <h3 class="text-[15px] sm:text-base font-bold text-gray-800 group-hover:text-primary-600 transition-colors truncate">
                                <c:out value="${notice.notice_title}" />
                            </h3>
                        </div>

                        <div class="flex items-center gap-4 flex-shrink-0">
                            <span class="text-xs font-medium text-gray-400 group-hover:text-gray-500 transition-colors hidden sm:block">
                                    ${notice.crtDtmFormatted}
                            </span>
                            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 group-hover:text-primary-400 transition-colors"></i>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty result.list}">
                    <div class="py-24 text-center">
                        <div class="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-3">
                            <i data-lucide="inbox" class="w-6 h-6 text-gray-300"></i>
                        </div>
                        <p class="text-sm font-bold text-gray-400">등록된 공지사항이 없습니다.</p>
                    </div>
                </c:if>
            </div>
        </div>

        <div id="userPagination" class="mt-10 flex justify-center"></div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        if(window.lucide) lucide.createIcons();

        renderCommonPagination(
            'userPagination',
            ${result.total},
            ${result.curPage},
            ${result.size},
            'goToPage'
        );
    });

    function goToPage(page) {
        location.href = '/notice?page=' + page;
    }
</script>

<jsp:include page="../common/footer.jsp" />