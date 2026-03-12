<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="space-y-5 animate-[fadeIn_0.3s_ease-out]">
    <div class="flex justify-between items-center mb-1 px-1">
        <h2 class="text-2xl font-black text-gray-900 tracking-tight flex items-center gap-2">
            구매 내역 <span class="text-sm font-bold text-gray-500 bg-gray-100 px-2.5 py-0.5 rounded-full">${purchaseList != null ? purchaseList.size() : 0}건</span>
        </h2>
    </div>

    <c:if test="${empty purchaseList}">
        <div class="py-24 text-center border-2 border-dashed border-gray-200 rounded-[2rem] bg-gray-50/50">
            <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300 shadow-sm">
                <i data-lucide="shopping-bag" class="w-8 h-8"></i>
            </div>
            <p class="text-base text-gray-500 font-bold">구매 내역이 없습니다.</p>
        </div>
    </c:if>

    <div class="space-y-4">
        <c:forEach var="trade" items="${purchaseList}">
            <div class="bg-white p-5 rounded-3xl border border-gray-100 hover:border-gray-200 hover:shadow-lg transition-all duration-300 flex gap-5 items-center relative group">

                <div class="w-20 h-24 bg-gray-50 rounded-2xl border border-gray-100 flex-shrink-0 overflow-hidden shadow-inner">
                    <c:choose>
                        <c:when test="${not empty trade.book_img}">
                            <img src="<c:out value='${trade.book_img}'/>" alt="<c:out value='${trade.sale_title}'/>" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                                 onerror="this.src='https://placehold.co/200x300?text=No+Image'"/>
                        </c:when>
                        <c:otherwise>
                            <div class="w-full h-full flex items-center justify-center text-gray-300"><i data-lucide="book" class="w-8 h-8"></i></div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="flex-1 min-w-0 py-0.5">
                    <div class="flex items-center gap-2 mb-1.5">
                        <span class="text-[10px] font-bold text-gray-400 bg-gray-100 px-2 py-0.5 rounded-md date-format" data-date="${trade.sale_st_dtm}"></span>
                        <c:if test="${trade.confirm_purchase}">
                            <span class="text-[10px] font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded-md">구매확정 완료</span>
                        </c:if>
                    </div>
                    <h3 class="font-bold text-gray-900 text-base mb-1 truncate group-hover:text-primary-600 transition-colors"><c:out value="${trade.sale_title}"/></h3>

                    <div class="mb-2">
                        <p class="text-sm font-medium text-gray-500">
                            <fmt:formatNumber value="${trade.sale_price}" pattern="#,###" /><span class="text-xs font-normal ml-0.5">원</span>
                        </p>
                    </div>

                    <div class="p-2 bg-gray-50/80 rounded-lg border border-gray-100/80">
                        <c:choose>
                            <c:when test="${not empty trade.post_no}">
                                <div class="flex items-center gap-2">
                                    <span class="text-[10px] font-bold bg-blue-100 text-blue-600 px-1.5 py-0.5 rounded shrink-0">배송지</span>

                                    <c:set var="fullAddr" value="${trade.addr_h} ${trade.addr_d}" />
                                    <p class="text-[11px] text-gray-600 leading-tight truncate" title="<c:out value='${fullAddr}'/>">
                                        <c:choose>
                                            <c:when test="${fn:length(fullAddr) > 50}">
                                                <c:out value="${fn:substring(fullAddr, 0, 50)}"/>...
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${fullAddr}"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="flex items-center gap-1.5 text-orange-600">
                                    <i data-lucide="info" class="w-3 h-3 shrink-0"></i>
                                    <p class="text-[11px] font-medium leading-tight truncate">
                                        직거래/반값택배 판매 내역입니다.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>


                <div class="flex flex-col gap-2 min-w-[90px] justify-center h-full">
                    <button onclick="location.href='/trade/${trade.trade_seq}'"
                            class="px-4 py-2 bg-gray-50 text-gray-600 rounded-xl text-xs font-bold hover:bg-gray-100 hover:text-gray-900 transition text-center w-full border border-transparent hover:border-gray-200">
                        상세보기
                    </button>

                    <c:if test="${!trade.confirm_purchase}">
                        <button onclick="PurchasesTab.confirmPurchase(${trade.trade_seq})"
                                class="px-4 py-2 bg-primary-600 text-white rounded-xl text-xs font-bold hover:bg-primary-700 transition shadow-md hover:shadow-lg w-full">
                            구매 확정
                        </button>
                    </c:if>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<script>
    {
        const actions = {
            init: () => {
                actions.formatDates();
                if(window.lucide) lucide.createIcons();
            },

            formatDates: () => {
                document.querySelectorAll('.date-format').forEach(el => {
                    const dateStr = el.dataset.date;
                    if (dateStr) {
                        const date = dateStr.split('T')[0].replace(/-/g, '.');
                        el.textContent = date;
                    }
                });
            },

            confirmPurchase: (tradeSeq) => {
                if (!confirm('구매를 확정하시겠습니까?\n확정 후에는 취소할 수 없습니다.')) return;

                fetch('/trade/confirm/' + tradeSeq, {
                    method: 'POST',
                    headers: {'X-Requested-With': 'XMLHttpRequest'} // AJAX 요청 헤더 추가
                })
                    .then(res => res.json())
                    .then(data => {
                        if (data.success) {
                            alert('구매가 확정되었습니다.');
                            // 현재 탭 리로드 (loadTab 함수는 mypage.jsp에 전역으로 정의되어 있어야 함)
                            if (typeof loadTab === 'function') {
                                loadTab(null, 'purchases');
                            } else {
                                location.reload();
                            }
                        } else {
                            alert(data.message || '처리에 실패했습니다.');
                        }
                    })
                    .catch(err => {
                        console.error(err);
                        alert('오류가 발생했습니다.');
                    });
            }
        };

        window.PurchasesTab = actions;
        actions.init();
    }
</script>