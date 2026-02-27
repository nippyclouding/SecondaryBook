<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="min-h-[calc(100vh-200px)] flex items-center justify-center">
    <div class="w-full max-w-lg">
        <!-- 결제 실패 카드 -->
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <!-- 실패 아이콘 -->
            <div class="pt-10 pb-6 text-center bg-gradient-to-b from-red-50 to-white">
                <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="15" y1="9" x2="9" y2="15"/>
                        <line x1="9" y1="9" x2="15" y2="15"/>
                    </svg>
                </div>
                <h1 class="text-2xl font-bold text-gray-900">결제에 실패했습니다</h1>
                <p class="text-sm text-gray-500 mt-2">결제 처리 중 문제가 발생했습니다</p>
            </div>

            <!-- 에러 정보 -->
            <div class="px-6 py-6 border-t border-gray-100">
                <div class="bg-red-50 border border-red-100 rounded-xl p-4">
                    <div class="flex items-start gap-3">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="flex-shrink-0 mt-0.5">
                            <circle cx="12" cy="12" r="10"/>
                            <line x1="12" y1="8" x2="12" y2="12"/>
                            <line x1="12" y1="16" x2="12.01" y2="16"/>
                        </svg>
                        <div>
                            <p class="text-sm font-semibold text-red-800">오류 메시지</p>
                            <p class="text-sm text-red-700 mt-1">
                                <c:choose>
                                    <c:when test="${not empty errorMessage}">
                                        <c:out value="${errorMessage}"/>
                                    </c:when>
                                    <c:otherwise>
                                        알 수 없는 오류가 발생했습니다. 다시 시도해주세요.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </div>
                </div>

                <!-- 도움말 -->
                <div class="mt-4 space-y-2">
                    <p class="text-xs text-gray-500 flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="9 11 12 14 22 4"/>
                            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                        </svg>
                        카드 잔액 및 한도를 확인해주세요
                    </p>
                    <p class="text-xs text-gray-500 flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="9 11 12 14 22 4"/>
                            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                        </svg>
                        카드 정보가 정확한지 확인해주세요
                    </p>
                    <p class="text-xs text-gray-500 flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="9 11 12 14 22 4"/>
                            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                        </svg>
                        문제가 지속되면 카드사에 문의해주세요
                    </p>
                </div>
            </div>

            <!-- 버튼 영역 -->
            <div class="px-6 pb-6">
                <a href="/" class="block w-full py-3 bg-primary-500 hover:bg-primary-600 text-white rounded-xl font-semibold text-sm text-center transition-all shadow-sm hover:shadow-md">
                    홈으로
                </a>
            </div>
        </div>

        <!-- 안내 문구 -->
        <p class="text-center text-xs text-gray-400 mt-4">
            결제 관련 문의: help@secondarybook.com
        </p>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
