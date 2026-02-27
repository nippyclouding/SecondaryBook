<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="../common/header.jsp" />

<div class="bg-[#F8F9FA] min-h-[calc(100vh-200px)] py-12 animate-[fadeIn_0.5s_ease-out]">
    <div class="max-w-6xl mx-auto px-4 sm:px-6">

        <!-- Breadcrumb -->
        <div class="flex items-center gap-2 text-xs font-bold text-gray-400 mb-8">
            <a href="/" class="hover:text-gray-900 transition-colors">홈</a>
            <i data-lucide="chevron-right" class="w-3 h-3"></i>
            <a href="/trade" class="hover:text-gray-900 transition-colors">중고거래</a>
            <i data-lucide="chevron-right" class="w-3 h-3"></i>
            <span class="text-primary-600"><c:out value="${trade.category_nm}"/></span>
        </div>

        <!-- Main grid -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-10">

            <!-- 이미지 영역 (64dev) -->
            <div class="lg:col-span-7 space-y-4 select-none">
                <div class="bg-white rounded-[2.5rem] border border-gray-100 shadow-[0_10px_40px_-10px_rgba(0,0,0,0.05)] overflow-hidden relative group aspect-[1/1.2]">
                    <c:choose>
                        <c:when test="${not empty trade.trade_img && trade.trade_img.size() > 0}">
                            <c:set var="firstImg" value="${trade.trade_img[0].img_url}" />
                            <img id="mainImage"
                                 src="${firstImg.startsWith('http') ? firstImg : firstImg.startsWith('/') ? firstImg : pageContext.request.contextPath.concat('/img/').concat(firstImg)}"
                                 alt="${trade.book_title}"
                                 onclick="openHoloModal(this.src)"
                                 class="w-full h-full object-contain p-8 transition-transform duration-500 cursor-zoom-in" />
                        </c:when>
                        <c:otherwise>
                            <img id="mainImage"
                                 src="${trade.book_img}"
                                 alt="${trade.book_title}"
                                 onclick="openHoloModal(this.src)"
                                 class="w-full h-full object-contain p-8 transition-transform duration-500 cursor-zoom-in"/>
                        </c:otherwise>
                    </c:choose>

                    <!-- 좌우 버튼 & 인디케이터 -->
                    <c:if test="${not empty trade.trade_img && trade.trade_img.size() > 1}">
                        <button onclick="prevImage()" class="absolute left-4 top-1/2 -translate-y-1/2 bg-white/80 backdrop-blur-md border border-white/50 p-3 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-all hover:scale-110 active:scale-95 text-gray-800">
                            <i data-lucide="chevron-left" class="w-5 h-5"></i>
                        </button>
                        <button onclick="nextImage()" class="absolute right-4 top-1/2 -translate-y-1/2 bg-white/80 backdrop-blur-md border border-white/50 p-3 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-all hover:scale-110 active:scale-95 text-gray-800">
                            <i data-lucide="chevron-right" class="w-5 h-5"></i>
                        </button>
                        <div id="imageIndicator" class="absolute bottom-6 left-1/2 -translate-x-1/2 bg-black/60 backdrop-blur-md text-white text-[10px] font-bold px-3 py-1 rounded-full shadow-sm">
                            1 / ${trade.trade_img.size()}
                        </div>
                    </c:if>
                </div>

                <!-- 썸네일 -->
                <c:if test="${not empty trade.trade_img && trade.trade_img.size() > 1}">
                    <div class="flex gap-3 overflow-x-auto py-2 px-1 scrollbar-hide">
                        <c:forEach var="img" items="${trade.trade_img}" varStatus="status">
                            <div onclick="setImage(${status.index})"
                                 id="thumb-${status.index}"
                                 class="w-20 h-20 rounded-2xl overflow-hidden cursor-pointer border-2 transition-all duration-300 ${status.index == 0 ? 'border-primary-500 ring-2 ring-primary-500/20 scale-105' : 'border-transparent hover:border-gray-200'}">
                                <img src="${img.img_url.startsWith('http') ? img.img_url : img.img_url.startsWith('/') ? img.img_url : pageContext.request.contextPath.concat('/img/').concat(img.img_url)}" class="w-full h-full object-cover"/>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

            <!-- UI 영역 (dev) -->
            <div class="lg:col-span-5 flex flex-col h-full">
                <div class="bg-white p-8 rounded-[2.5rem] border border-gray-100 shadow-[0_10px_40px_-10px_rgba(0,0,0,0.05)] h-full relative overflow-hidden">

                    <div class="absolute top-0 right-0 w-32 h-32 bg-primary-50 rounded-bl-full opacity-50 pointer-events-none"></div>

                    <div class="relative z-10">
                        <div class="flex items-center gap-2 mb-3">
                            <span class="px-3 py-1 rounded-full bg-gray-100 text-gray-600 text-[10px] font-bold uppercase tracking-wider">
                                <c:choose>
                                    <c:when test="${trade.book_st eq 'LIKE_NEW'}">S급 · 거의 새책</c:when>
                                    <c:when test="${trade.book_st eq 'GOOD'}">A급 · 좋음</c:when>
                                    <c:when test="${trade.book_st eq 'USED'}">B급 · 사용감 있음</c:when>
                                    <c:when test="${trade.book_st eq 'NEW'}">N급 · 미사용</c:when>
                                    <c:otherwise>상태 정보 없음</c:otherwise>
                                </c:choose>
                            </span>
                            <span class="px-3 py-1 rounded-full bg-primary-50 text-primary-600 text-[10px] font-bold uppercase tracking-wider flex items-center gap-1">
                                <i data-lucide="map-pin" class="w-3 h-3"></i> <c:out value="${trade.sale_rg}"/>
                            </span>
                        </div>

                        <h1 class="text-2xl font-black text-gray-900 leading-tight mb-2 tracking-tight">
                            <c:out value="${trade.sale_title}"/>
                        </h1>
                        <div class="text-sm font-medium text-gray-400 mb-6 flex flex-wrap gap-2 items-center">
                            <span><c:out value="${trade.book_title}"/></span>
                            <span class="w-1 h-1 rounded-full bg-gray-300"></span>
                            <span><c:out value="${trade.book_author}"/></span>
                            <span class="w-1 h-1 rounded-full bg-gray-300"></span>
                            <span><c:out value="${trade.book_publisher}"/></span>
                        </div>
                    </div>

                    <!-- 가격 영역 -->
                    <div class="mb-8 p-5 bg-gray-50 rounded-2xl border border-gray-100">
                        <c:if test="${trade.book_org_price > 0}">
                            <fmt:parseNumber var="discountRate" value="${((trade.book_org_price - trade.sale_price) * 100) / trade.book_org_price}" integerOnly="true" />
                            <div class="flex items-baseline gap-2 mb-1">
                                <span class="text-sm font-bold text-red-500 bg-red-50 px-2 py-0.5 rounded-md">${discountRate}% OFF</span>
                                <span class="text-sm text-gray-400 line-through decoration-gray-400"><fmt:formatNumber value="${trade.book_org_price}" pattern="#,###" />원</span>
                            </div>
                        </c:if>
                        <div class="flex items-end justify-between">
                            <div class="flex items-baseline gap-1">
                                <span class="text-4xl font-black text-gray-900 tracking-tighter"><fmt:formatNumber value="${trade.sale_price}" pattern="#,###" /></span>
                                <span class="text-lg font-bold text-gray-500">원</span>
                            </div>
                            <div class="text-xs font-bold text-gray-500">
                                배송비 <span class="text-gray-900 ml-1">
                                    <c:choose>
                                        <c:when test="${trade.delivery_cost > 0}"><fmt:formatNumber value="${trade.delivery_cost}" pattern="#,###" />원</c:when>
                                        <c:otherwise>무료배송</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- 판매자 영역 -->
                    <div class="flex items-center justify-between mb-8 pb-6 border-b border-gray-100">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-full bg-gradient-to-br from-primary-100 to-primary-200 border border-primary-100 flex items-center justify-center text-primary-600 font-black shadow-inner">
                                <c:choose>
                                    <c:when test="${not empty seller_info}">
                                        <c:out value="${seller_info.member_nicknm.substring(0, 1)}"/>
                                    </c:when>
                                    <c:otherwise>?</c:otherwise>
                                </c:choose>
                            </div>
                            <div>
                                <p class="text-xs text-gray-400 font-bold mb-0.5">판매자</p>
                                <p class="text-sm font-bold text-gray-900">
                                    <c:choose>
                                        <c:when test="${not empty seller_info}"><c:out value="${seller_info.member_nicknm}"/></c:when>
                                        <c:otherwise>알 수 없음</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </div>
                    </div>

                    <!-- 상품설명 -->
                    <div class="mb-8">
                        <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">상품 설명</h3>
                        <div class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap font-medium"><c:out value="${trade.sale_cont}"/></div>
                    </div>

                    <!-- 찜 / 채팅 / 수정삭제 -->
                    <div class="mt-auto space-y-3">
                        <c:if test="${not empty sessionScope.loginSess and trade.member_seller_seq != sessionScope.loginSess.member_seq}">
                            <div class="flex gap-3">
                                <!-- 찜하기 -->
                                <form id="wishForm-${trade.trade_seq}" class="shrink-0">
                                    <input type="hidden" name="trade_seq" value="${trade.trade_seq}" />
                                    <button type="button" onclick="toggleWish(${trade.trade_seq})"
                                            class="w-14 h-14 rounded-2xl flex flex-col items-center justify-center gap-0.5 transition-all border-2 ${wished ? 'border-red-100 bg-red-50 text-red-500' : 'border-gray-100 bg-white text-gray-400 hover:border-red-100 hover:text-red-400'}">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="${wished ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
                                        </svg>
                                        <span id="wishCount-${trade.trade_seq}" class="text-[10px] font-bold">${wishCount}</span>
                                    </button>
                                </form>

                                <!-- 채팅 -->
                                <form action="/chatrooms" method="post" class="flex-1">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                    <input type="hidden" name="trade_seq" value="${trade.trade_seq}">
                                    <input type="hidden" name="member_seller_seq" value="${trade.member_seller_seq}">
                                    <input type="hidden" name="sale_title" value="<c:out value='${trade.sale_title}'/>">
                                    <button type="submit" class="w-full h-14 bg-gray-900 hover:bg-black text-white rounded-2xl font-bold text-sm transition-all shadow-lg hover:shadow-xl hover:-translate-y-0.5 flex items-center justify-center gap-2">
                                        <i data-lucide="message-circle" class="w-5 h-5"></i>
                                        채팅으로 거래하기
                                    </button>
                                </form>
                            </div>
                        </c:if>

                        <c:if test="${trade.sale_st ne 'SOLD' and (not empty sessionScope.loginSess and sessionScope.loginSess.member_seq == trade.member_seller_seq)}">
                            <div class="flex gap-3">
                                <a href="/trade/modify/${trade.trade_seq}"
                                   class="flex-1 h-14 flex items-center justify-center bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-2xl font-bold text-sm transition-all">
                                    <i data-lucide="edit-2" class="w-4 h-4 mr-2"></i> 수정
                                </a>
                                <form action="${pageContext.request.contextPath}/trade/delete/${trade.trade_seq}" method="post" class="flex-1"
                                      onsubmit="return confirm('정말 삭제하시겠습니까? 복구할 수 없습니다.');">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                    <button type="submit"
                                            class="w-full h-14 bg-red-50 hover:bg-red-100 text-red-500 rounded-2xl font-bold text-sm transition-all border border-red-100">
                                        <i data-lucide="trash-2" class="w-4 h-4 mr-2 inline"></i> 삭제
                                    </button>
                                </form>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- JS 영역 -->
<script>
const images = [
    <c:choose>
        <c:when test="${not empty trade.trade_img}">
            <c:forEach var="img" items="${trade.trade_img}" varStatus="s">
                "${img.img_url}"<c:if test="${!s.last}">,</c:if>
            </c:forEach>
        </c:when>
        <c:otherwise>
            "${trade.book_img}"
        </c:otherwise>
    </c:choose>
];

let idx = 0;

function setImage(i) { idx = i; update(); }
function prevImage() { idx = (idx - 1 + images.length) % images.length; update(); }
function nextImage() { idx = (idx + 1) % images.length; update(); }

function update() {
    const mainImg = document.getElementById("mainImage");
    const currentPath = images[idx];

    if (currentPath.startsWith('http') || currentPath.startsWith('/')) {
        mainImg.src = currentPath;
    } else {
        mainImg.src = "${pageContext.request.contextPath}/img/" + currentPath;
    }

    const ind = document.getElementById("imageIndicator");
    if (ind) ind.innerText = (idx + 1) + " / " + images.length;

    images.forEach((_, i) => {
        const t = document.getElementById("thumb-" + i);
        if (t) {
            t.classList.toggle("border-primary-500", i === idx);
            t.classList.toggle("ring-2", i === idx);
            t.classList.toggle("ring-primary-500/20", i === idx);
            t.classList.toggle("scale-105", i === idx);
            t.classList.toggle("border-transparent", i !== idx);
            t.classList.toggle("hover:border-gray-200", i !== idx);
        }
    });
}

function toggleWish(tradeSeq) {
    const form = document.getElementById(`wishForm-${tradeSeq}`);
    if (!form) return;
    const btn = form.querySelector("button");
    const countSpan = document.getElementById(`wishCount-${tradeSeq}`);
    const heartIcon = btn.querySelector('svg');
    const formData = new FormData(form);

        fetch("/trade/like", {
            method: "POST",
            body: formData,
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
            .then(async res => {
                const text = await res.text();
                try { return JSON.parse(text); }
                catch (e) { throw new Error("Server Error"); }
            })
            .then(data => {
                if (!data.success) {
                    alert(data.message || "찜 처리 실패");
                    return;
                }

                // UI 업데이트 (토글)
                if (data.wished) {
                    // 찜 설정됨
                    btn.classList.add("border-red-100", "bg-red-50", "text-red-500");
                    btn.classList.remove("border-gray-100", "bg-white", "text-gray-400", "hover:border-red-100", "hover:text-red-400");
                    if (heartIcon) heartIcon.setAttribute('fill', 'currentColor');
                    countSpan.textContent = parseInt(countSpan.textContent) + 1;

                    // 튀어오르는 애니메이션 효과
                    btn.animate([
                        { transform: 'scale(1)' },
                        { transform: 'scale(1.2)' },
                        { transform: 'scale(1)' }
                    ], { duration: 300 });

                } else {
                    // 찜 해제됨
                    btn.classList.remove("border-red-100", "bg-red-50", "text-red-500");
                    btn.classList.add("border-gray-100", "bg-white", "text-gray-400", "hover:border-red-100", "hover:text-red-400");
                    if (heartIcon) heartIcon.setAttribute('fill', 'none');
                    countSpan.textContent = parseInt(countSpan.textContent) - 1;
                }
            })
            .catch(err => {
                console.error(err);
                alert("요청 처리 중 오류가 발생했습니다.");
            });
    }

    // 아이콘 초기화
    document.addEventListener("DOMContentLoaded", () => {
        if(window.lucide) lucide.createIcons();
    });
</script>

<jsp:include page="include/holo_card_modal.jsp" />
<jsp:include page="../common/footer.jsp" />