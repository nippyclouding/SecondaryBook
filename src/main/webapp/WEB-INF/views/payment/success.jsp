<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="min-h-[calc(100vh-200px)] flex items-center justify-center py-10">
    <div class="w-full max-w-lg mx-4">
        <div class="bg-white rounded-3xl border border-gray-200 shadow-sm overflow-hidden">

            <div class="pt-12 pb-8 text-center bg-gradient-to-b from-green-50 to-white">
                <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6 9 17l-5-5"/>
                    </svg>
                </div>
                <h1 class="text-2xl font-bold text-gray-900">결제가 완료되었습니다</h1>
                <p class="text-sm text-gray-500 mt-2">안전하게 거래가 접수되었습니다.</p>
            </div>

            <div class="px-8 py-6 border-t border-gray-100 bg-gray-50/30">
                <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-orange-500">
                        <path d="M10 17h4V5H2v12h3m0 0a2 2 0 1 0 4 0 2 2 0 1 0-4 0m11 0a2 2 0 1 0 4 0 2 2 0 1 0-4 0m4-3h1l4 4v3h-4v-7Z"/>
                    </svg>
                    배송 정보
                </h3>

                <div class="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
                    <c:choose>
                        <%-- 직거래/반값택배 선택 시 --%>
                        <c:when test="${payment.addr_type == 'direct'}">
                            <div class="flex items-center gap-3">
                                <span class="px-2 py-1 bg-red-50 text-red-600 text-[10px] font-bold rounded-md">직거래/반택</span>
                                <div>
                                    <p class="text-sm font-bold text-gray-900">판매자와 협의 예정</p>
                                    <p class="text-xs text-gray-500 mt-0.5">상세 위치는 채팅으로 조율해 주세요.</p>
                                </div>
                            </div>
                        </c:when>

                        <%-- 일반 배송지(기존/신규) 선택 시 --%>
                        <c:otherwise>
                            <div class="space-y-2">
                                <div class="flex items-center gap-2">
                                    <span class="text-[11px] font-bold text-gray-400">우편번호</span>
                                    <span class="text-sm font-mono font-medium text-gray-900"><c:out value="${payment.post_no}"/></span>
                                </div>
                                <div>
                                    <p class="text-sm font-bold text-gray-900"><c:out value="${payment.addr_h}"/></p>
                                    <p class="text-sm text-gray-600 mt-1"><c:out value="${payment.addr_d}"/></p>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="px-8 py-6 border-t border-gray-100">
                <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4 flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-primary-500">
                        <rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/>
                    </svg>
                    결제 상세
                </h3>

                <div class="space-y-4">
                    <div class="flex justify-between items-center">
                        <span class="text-sm text-gray-500">결제수단</span>
                        <span class="text-sm font-bold text-gray-900"><c:out value="${payment.method}"/></span>
                    </div>

                    <c:if test="${not empty payment.card_company}">
                        <div class="flex justify-between items-center">
                            <span class="text-sm text-gray-500">카드 정보</span>
                            <span class="text-sm font-medium text-gray-900"><c:out value="${payment.card_company}"/> (<c:out value="${payment.card_number}"/>)</span>
                        </div>
                    </c:if>

                    <div class="flex justify-between items-center">
                        <span class="text-sm text-gray-500">결제 상태</span>
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-green-100 text-green-700">
                            결제승인 완료
                        </span>
                    </div>

                    <div class="pt-4 border-t border-gray-50 flex justify-between items-center">
                        <span class="text-base font-bold text-gray-900">총 결제 금액</span>
                        <span class="text-2xl font-black text-primary-600">
                            <fmt:formatNumber value="${payment.amount}" type="number"/>원
                        </span>
                    </div>
                </div>
            </div>

            <div class="px-8 pb-8 flex gap-3">
                <a href="/" class="flex-1 py-4 bg-gray-50 hover:bg-gray-100 text-gray-600 rounded-2xl font-bold text-sm text-center transition-all border border-gray-100">
                    홈으로 이동
                </a>
                <a href="/mypage/purchases" class="flex-1 py-4 bg-gray-900 hover:bg-black text-white rounded-2xl font-bold text-sm text-center transition-all shadow-lg shadow-gray-200">
                    주문내역 확인
                </a>
            </div>
        </div>

        <div class="mt-6 text-center">
            <p class="text-xs text-gray-400 leading-relaxed">
                결제 관련 영수증은 토스페이먼츠 앱 혹은 이메일에서 확인 가능합니다.<br>
                문제가 발생했다면 <span class="underline cursor-pointer">고객센터</span>로 문의해 주세요.
            </p>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>