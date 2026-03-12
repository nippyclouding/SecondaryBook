<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp" />

<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="<c:url value='/resources/js/trade/openDaumPostcode.js'/>"></script>

<style>
    /* Chrome, Safari, Edge, Opera */
    input::-webkit-outer-spin-button,
    input::-webkit-inner-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }
    /* Firefox */
    input[type=number] {
        -moz-appearance: textfield;
    }
</style>

<div class="bg-gradient-to-br from-gray-50 to-blue-50/30 min-h-screen py-16 px-4 animate-[fadeIn_0.6s_cubic-bezier(0.22, 1, 0.36, 1)]">
    <div class="max-w-3xl mx-auto">

        <div class="text-center mb-14">
            <h1 class="text-4xl font-black text-gray-900 tracking-tighter mb-4">판매글 수정</h1>
            <p class="text-gray-500 text-lg font-medium">등록된 정보를 수정하여 다시 올립니다.</p>
        </div>

        <div class="bg-white rounded-[2.5rem] shadow-[0_20px_60px_-15px_rgba(0,0,0,0.05)] overflow-hidden border border-gray-100 p-8 md:p-12 relative">

            <div class="absolute top-0 right-0 w-64 h-64 bg-blue-50/50 rounded-full blur-3xl -mr-32 -mt-32 pointer-events-none"></div>

            <form action="/trade/modify/${trade.trade_seq}" method="post" enctype="multipart/form-data" class="space-y-12 relative z-10">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <section>
                    <div class="flex items-center gap-4 mb-8">
                        <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center shadow-sm">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
                        </div>
                        <div>
                            <h2 class="text-xl font-bold text-gray-900">도서 정보</h2>
                            <p class="text-sm text-gray-400 font-medium">판매할 책을 변경하려면 다시 검색해주세요.</p>
                        </div>
                    </div>

                    <div class="relative group z-30">
                        <div class="flex items-center bg-gray-50 border border-gray-200 focus-within:border-blue-500 focus-within:bg-white focus-within:ring-4 focus-within:ring-blue-500/10 rounded-2xl transition-all duration-300 overflow-hidden shadow-sm">
                            <div class="pl-5 text-gray-400">
                                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
                            </div>
                            <input type="text" id="bookSearch"
                                   placeholder="책 제목, 저자 또는 ISBN 검색"
                                   class="flex-1 px-4 py-4 bg-transparent focus:outline-none text-gray-900 placeholder-gray-400 font-bold text-lg" />
                            <button type="button" id="searchBtn"
                                    class="px-6 py-2.5 mr-2 bg-gray-900 text-white text-sm font-bold rounded-xl hover:bg-black hover:scale-105 active:scale-95 transition-all shadow-md">
                                검색
                            </button>
                        </div>

                        <div id="searchResults" class="absolute left-0 top-full mt-2 w-full bg-white/95 backdrop-blur-xl border border-gray-100 rounded-2xl shadow-xl hidden max-h-96 overflow-y-auto z-50 p-2 custom-scrollbar animate-[fadeInDown_0.2s_ease-out]">
                        </div>
                    </div>

                    <div id="selectedBookPreview" class="mt-6 p-6 bg-gradient-to-r from-blue-50 to-white rounded-[2rem] border border-blue-100 flex items-start gap-6 relative animate-[fadeIn_0.4s_cubic-bezier(0.22, 1, 0.36, 1)] group">
                        <img id="previewImg" src="${trade.book_img}" alt="책 표지" class="w-28 h-40 object-cover rounded-xl shadow-lg bg-white shrink-0 transform transition-transform group-hover:scale-105 duration-500" />

                        <div class="flex-1 py-1">
                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-blue-600 text-white text-[11px] font-bold rounded-full mb-3 shadow-md shadow-blue-500/20">
                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                                선택 완료
                            </span>
                            <h3 id="previewTitle" class="text-xl font-black text-gray-900 leading-tight mb-2 tracking-tight"><c:out value="${trade.book_title}"/></h3>
                            <div class="space-y-1 mb-4">
                                <p class="text-sm font-bold text-gray-600 flex items-center gap-2">
                                    <span id="previewAuthor"><c:out value="${trade.book_author}"/></span>
                                </p>
                                <p id="previewPublisher" class="text-xs font-medium text-gray-400"><c:out value="${trade.book_publisher}"/></p>
                            </div>
                            <div class="flex items-baseline gap-1">
                                <span class="text-xs font-medium text-gray-400">정가</span>
                                <p id="previewPrice" class="text-lg text-blue-600 font-black">
                                    ${trade.book_org_price > 0 ? trade.book_org_price : ''}원
                                </p>
                            </div>
                        </div>
                        <button type="button" id="clearBookBtn" class="absolute top-5 right-5 p-2 bg-white rounded-full text-gray-300 hover:text-red-500 hover:bg-red-50 transition-all shadow-sm border border-gray-100">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                        </button>
                    </div>

                    <input type="hidden" id="isbn" name="isbn" value="<c:out value='${trade.isbn}'/>" required />
                    <input type="hidden" id="book_title" name="book_title" value="<c:out value='${trade.book_title}'/>" required />
                    <input type="hidden" id="book_author" name="book_author" value="<c:out value='${trade.book_author}'/>" required />
                    <input type="hidden" id="book_publisher" name="book_publisher" value="<c:out value='${trade.book_publisher}'/>" required />
                    <input type="hidden" id="book_org_price" name="book_org_price" value="${trade.book_org_price}" />
                    <input type="hidden" id="book_img" name="book_img" value="<c:out value='${trade.book_img}'/>" required />
                </section>

                <hr class="border-gray-100">

                <section>
                    <div class="flex items-center gap-4 mb-10">
                        <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center shadow-sm">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.91 8.84 8.56 2.23a1.93 1.93 0 0 0-1.81 0L3.1 4.13a2.12 2.12 0 0 0-.05 3.69l12.22 6.93a2 2 0 0 0 1.94 0L21 12.51a2.12 2.12 0 0 0-.09-3.67Z"/><path d="m3.09 8.84 12.35-6.61a1.93 1.93 0 0 1 1.81 0l3.65 1.9a2.12 2.12 0 0 1 .1 3.69L8.73 14.75a2 2 0 0 1-1.94 0L3 12.51a2.12 2.12 0 0 1 .09-3.67Z"/></svg>
                        </div>
                        <div>
                            <h2 class="text-xl font-bold text-gray-900">판매 정보</h2>
                            <p class="text-sm text-gray-400 font-medium">수정할 내용을 꼼꼼히 확인해주세요.</p>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-10">

                        <div class="md:col-span-2 group">
                            <label for="sale_title" class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                판매글 제목 <span class="text-blue-500">*</span>
                            </label>
                            <input type="text" id="sale_title" name="sale_title" required
                                   value="<c:out value='${trade.sale_title}'/>"
                                   placeholder="예) 한번 읽은 깨끗한 책 팝니다"
                                   class="w-full px-6 py-4 bg-gray-50 border border-transparent rounded-2xl text-gray-900 placeholder-gray-400 font-bold focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none" />
                        </div>

                        <div class="relative">
                            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                카테고리 <span class="text-blue-500">*</span>
                            </label>
                            <div class="relative">
                                <select id="categorySelect" name="category_seq" required
                                        class="w-full px-6 py-4 bg-gray-50 border border-transparent rounded-2xl text-gray-900 font-bold focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none appearance-none cursor-pointer">
                                    <option value="">선택하세요</option>
                                    <c:forEach var="cat" items="${category}">
                                        <option value="${cat.category_seq}" data-nm="${cat.category_nm}"
                                            ${cat.category_nm eq trade.category_nm ? 'selected' : ''}>
                                            ${cat.category_nm}
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="absolute right-5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
                                </div>
                                <input type="hidden" name="category_nm" id="category_nm" value="<c:out value='${trade.category_nm}'/>">
                            </div>
                        </div>

                        <div>
                            <label for="book_st" class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                책 상태 <span class="text-blue-500">*</span>
                            </label>
                            <div class="relative">
                                <select id="book_st" name="book_st" required
                                        class="w-full px-6 py-4 bg-gray-50 border border-transparent rounded-2xl text-gray-900 font-bold focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none appearance-none cursor-pointer">
                                    <option value="">선택하세요</option>
                                    <option value="NEW" ${trade.book_st == 'NEW' ? 'selected' : ''}>새책 (미사용)</option>
                                    <option value="LIKE_NEW" ${trade.book_st == 'LIKE_NEW' ? 'selected' : ''}>거의 새책 (사용감 없음)</option>
                                    <option value="GOOD" ${trade.book_st == 'GOOD' ? 'selected' : ''}>좋음 (깨끗함)</option>
                                    <option value="USED" ${trade.book_st == 'USED' ? 'selected' : ''}>사용됨 (흔적 있음)</option>
                                </select>
                                <div class="absolute right-5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
                                </div>
                            </div>
                        </div>

                        <div>
                            <label for="sale_price" class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                판매가격 <span class="text-blue-500">*</span>
                            </label>
                            <div class="relative">
                                <input type="number" id="sale_price" name="sale_price" required max="10000000"
                                       value="${trade.sale_price}" placeholder="0"
                                       class="w-full pl-6 pr-12 py-4 bg-gray-50 border border-transparent rounded-2xl text-gray-900 font-black text-lg focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none text-right" />
                                <span class="absolute right-6 top-1/2 -translate-y-1/2 text-sm font-bold text-gray-400 pointer-events-none">원</span>
                            </div>
                            <p id="sale_price_error" class="mt-2 text-[11px] font-bold text-red-500 hidden pl-1 animate-pulse">0원 이상 1천만원 이하로 입력해주세요.</p>
                        </div>

                        <div>
                            <label for="delivery_cost" class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                배송비 <span class="text-blue-500">*</span>
                            </label>
                            <div class="relative">
                                <input type="number" id="delivery_cost" name="delivery_cost" required max="50000"
                                       value="${trade.delivery_cost}" placeholder="3000"
                                       class="w-full pl-6 pr-12 py-4 bg-gray-50 border border-transparent rounded-2xl text-gray-900 font-black text-lg focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none text-right" />
                                <span class="absolute right-6 top-1/2 -translate-y-1/2 text-sm font-bold text-gray-400 pointer-events-none">원</span>
                            </div>
                            <p id="delivery_cost_error" class="mt-2 text-[11px] font-bold text-red-500 hidden pl-1 animate-pulse">0원 이상 5만원 이하로 입력해주세요.</p>
                        </div>

                        <div class="md:col-span-2">
                            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-3 ml-1">
                                거래방법 <span class="text-blue-500">*</span>
                            </label>
                            <div class="grid grid-cols-2 gap-4">
                                <label class="cursor-pointer relative">
                                    <input type="radio" name="payment_type" value="account" class="peer sr-only" required ${trade.payment_type == 'account' ? 'checked' : ''}/>
                                    <div class="px-6 py-5 rounded-2xl bg-gray-50 border-2 border-transparent text-center font-bold text-gray-500 peer-checked:bg-white peer-checked:border-blue-500 peer-checked:text-blue-600 peer-checked:shadow-lg peer-checked:shadow-blue-500/20 transition-all duration-200 hover:bg-gray-100">
                                        계좌이체
                                    </div>
                                    <div class="absolute top-4 right-4 opacity-0 peer-checked:opacity-100 transition-opacity text-blue-500">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                                    </div>
                                </label>
                                <label class="cursor-pointer relative">
                                    <input type="radio" name="payment_type" value="tosspay" class="peer sr-only" ${trade.payment_type == 'tosspay' ? 'checked' : ''} />
                                    <div class="px-6 py-5 rounded-2xl bg-gray-50 border-2 border-transparent text-center font-bold text-gray-500 peer-checked:bg-white peer-checked:border-blue-500 peer-checked:text-blue-600 peer-checked:shadow-lg peer-checked:shadow-blue-500/20 transition-all duration-200 hover:bg-gray-100">
                                        토스페이
                                    </div>
                                    <div class="absolute top-4 right-4 opacity-0 peer-checked:opacity-100 transition-opacity text-blue-500">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                                    </div>
                                </label>
                            </div>
                        </div>

                        <div class="md:col-span-2">
                            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                판매지역 <span class="text-gray-400 font-normal ml-1">(시, 군, 구만 표기됩니다)</span>
                            </label>
                            <div class="flex gap-3">
                                <div class="relative flex-1 group">
                                    <div class="absolute left-6 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500 transition-colors">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
                                    </div>
                                    <input type="text" id="sale_rg" name="sale_rg" readonly
                                           value="<c:out value='${trade.sale_rg}'/>" placeholder="우측 버튼으로 주소를 검색하세요"
                                           onclick="clearOnClick(this)"
                                           class="w-full pl-14 pr-5 py-4 bg-gray-50 border border-transparent rounded-2xl text-gray-900 font-bold focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none cursor-default" />
                                </div>
                                <button type="button" onclick="searchRG()"
                                        class="px-8 bg-gray-900 text-white rounded-2xl font-bold text-sm hover:bg-black hover:scale-105 active:scale-95 transition-all shadow-lg shadow-gray-900/20 shrink-0">
                                    주소 검색
                                </button>
                            </div>
                        </div>

                        <div class="md:col-span-2">
                            <label for="sale_cont" class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                상세설명 <span class="text-blue-500">*</span>
                            </label>
                            <textarea id="sale_cont" name="sale_cont" required rows="8"
                                      placeholder="책의 상태, 구입 시기, 특이사항 등을 자세히 적어주세요. (500자 제한)"
                                      maxlength="500"
                                      class="w-full px-6 py-5 bg-gray-50 border border-transparent rounded-3xl text-gray-900 font-medium focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none resize-none leading-relaxed"><c:out value="${trade.sale_cont}"/></textarea>
                        </div>
                    </div>
                </section>

                <hr class="border-gray-100">

                <section>
                    <div class="flex items-center justify-between mb-6">
                        <div class="flex items-center gap-4">
                            <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center shadow-sm">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>
                            </div>
                            <div>
                                <h2 class="text-xl font-bold text-gray-900">사진 수정</h2>
                                <p class="text-sm text-gray-400 font-medium">기존 사진을 삭제하거나 새 사진을 추가할 수 있습니다.</p>
                            </div>
                        </div>
                        <span class="text-xs font-bold text-blue-600 bg-blue-50 border border-blue-100 px-4 py-1.5 rounded-full">최대 3장</span>
                    </div>

                    <div class="mb-4">
                        <p class="text-xs font-bold text-gray-500 mb-3 ml-1">현재 등록된 이미지 (<span id="existingImageCount">${not empty trade.trade_img ? trade.trade_img.size() : 0}</span>장)</p>
                        <div id="existingImagesContainer" class="flex gap-4 flex-wrap">
                            <c:if test="${not empty trade.trade_img && trade.trade_img.size() > 0}">
                                <c:forEach var="img" items="${trade.trade_img}" varStatus="status">
                                    <div class="existing-image-item relative w-32 h-32 rounded-2xl border border-gray-100 overflow-hidden group shadow-sm transition-all hover:shadow-md" data-img-url="${img.img_url}">
                                        <img src="${img.img_url.startsWith('http') ? img.img_url : img.img_url.startsWith('/') ? img.img_url : pageContext.request.contextPath.concat('/img/').concat(img.img_url)}"
                                             alt="상품 이미지" class="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-500" />

                                        <button type="button" onclick="removeExistingImage(this)"
                                                class="absolute top-2 right-2 w-7 h-7 bg-white/90 backdrop-blur-sm text-red-500 rounded-full flex items-center justify-center shadow-sm opacity-0 group-hover:opacity-100 transition-all hover:bg-red-50 hover:scale-110">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                                        </button>
                                        <input type="hidden" name="keepImageUrls" value="${img.img_url}" />
                                    </div>
                                </c:forEach>
                            </c:if>
                        </div>
                    </div>

                    <div class="relative w-full h-48 rounded-[2rem] border-2 border-dashed border-gray-200 bg-gray-50 hover:bg-blue-50/50 hover:border-blue-300 transition-all duration-300 flex flex-col items-center justify-center group cursor-pointer overflow-hidden mt-6">
                        <input type="file" id="uploadFiles" name="uploadFiles" accept="image/*" multiple
                               class="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10" />

                        <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-sm mb-4 group-hover:scale-110 group-hover:shadow-md transition-all duration-300">
                            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-gray-400 group-hover:text-blue-600"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/></svg>
                        </div>
                        <p class="text-base font-bold text-gray-600 group-hover:text-blue-600 transition-colors">새 이미지 추가 (클릭 또는 드래그)</p>
                        <p id="fileMsg" class="text-xs text-gray-400 mt-1 font-medium">기존 이미지 포함 최대 3장까지 가능합니다</p>
                    </div>
                </section>

                <div class="pt-6 text-center">
                    <p class="text-xs text-gray-400 leading-relaxed">
                        토스페이 안전결제 이용 시 정산 금액에서 <span class="font-bold text-gray-500">수수료 1%</span>가 차감됩니다.<br/>
                        안전결제 정산 금액은 구매확정 후 정산 신청 시 <span class="font-bold text-gray-500">매일 새벽 3시</span>에 자동으로 처리됩니다.
                    </p>
                </div>

                <div class="pt-4 flex gap-4">
                    <button type="button" onclick="history.back()"
                            class="flex-1 py-4 bg-white border border-gray-200 text-gray-600 rounded-2xl font-bold hover:bg-gray-50 hover:border-gray-300 hover:text-gray-900 transition-all shadow-sm">
                        취소하기
                    </button>
                    <button type="submit"
                            class="flex-[2] py-4 bg-blue-600 text-white rounded-2xl font-bold text-lg hover:bg-blue-700 transition-all shadow-xl shadow-blue-600/30 hover:-translate-y-0.5 active:scale-[0.99]">
                        수정 완료
                    </button>
                </div>

            </form>
        </div>
    </div>
</div>

<script>
    // --- [1] 기본 변수 설정 ---
    const searchInput = document.getElementById('bookSearch');
    const searchBtn = document.getElementById('searchBtn');
    const searchResults = document.getElementById('searchResults');
    const selectedBookPreview = document.getElementById('selectedBookPreview');
    const clearBookBtn = document.getElementById('clearBookBtn');

    // 카테고리 이름 바인딩
    bindCategoryName('categorySelect', 'category_nm');

    function bindCategoryName(selectId, hiddenId) {
        const select = document.getElementById(selectId);
        const hidden = document.getElementById(hiddenId);
        if (!select || !hidden) return;
        select.addEventListener('change', function () {
            const option = this.options[this.selectedIndex];
            hidden.value = option.dataset.nm || '';
        });
    }

    // 주소 검색 창 클릭 시 값 초기화 (flag 설정)
    function clearOnClick(el) {
        if (el.value !== '') {
            el.value = '';
            el.dataset.cleared = 'true';
        }
    }

    // --- [2] 이미지 처리 로직 (기존 + 신규 통합) ---

    // 기존 이미지 개수 확인
    function getExistingImageCount() {
        return document.querySelectorAll('.existing-image-item').length;
    }

    // 기존 이미지 삭제
    window.removeExistingImage = function(btn) {
        const imageItem = btn.closest('.existing-image-item');
        imageItem.remove();
        updateExistingImageCount();
        updateFileMsg();
    };

    // UI 업데이트 (카운트)
    function updateExistingImageCount() {
        const count = getExistingImageCount();
        const cntSpan = document.getElementById('existingImageCount');
        if(cntSpan) cntSpan.textContent = count;
    }

    // UI 업데이트 (메시지)
    function updateFileMsg() {
        const existingCount = getExistingImageCount();
        const remainingSlots = 3 - existingCount;
        const msgEl = document.getElementById('fileMsg');

        if (remainingSlots <= 0) {
            msgEl.textContent = '이미지가 최대 3장입니다. 추가하려면 기존 이미지를 삭제하세요.';
            msgEl.style.color = '#ef4444'; // red-500
            msgEl.style.fontWeight = 'bold';
        } else {
            msgEl.textContent = `새 이미지를 \${remainingSlots}장까지 더 추가할 수 있습니다. (5MB 이하)`;
            msgEl.style.color = '';
            msgEl.style.fontWeight = '';
        }
    }

    // 새 파일 유효성 검사
    function validateImageUpload(inputEl, msgEl) {
        const MAX_COUNT = 3;
        const MAX_TOTAL_SIZE = 5 * 1024 * 1024; // 5MB

        inputEl.addEventListener('change', () => {
            const files = Array.from(inputEl.files);
            const existingCount = getExistingImageCount();
            const totalCount = existingCount + files.length;

            if(files.length > 0) {
                // UI: 선택된 파일 수 표시
                msgEl.textContent = `\${files.length}개의 새 파일이 선택되었습니다.`;
                msgEl.style.color = '#2563eb'; // blue-600
                msgEl.style.fontWeight = 'bold';
            }

            // 개수 체크
            if (totalCount > MAX_COUNT) {
                showFileError(`전체 이미지는 최대 3장까지만 가능합니다. (기존 \${existingCount}장 + 새파일 \${files.length}장)`);
                return;
            }

            let totalSize = 0;
            for (let file of files) {
                if (!file.type.startsWith('image/')) {
                    showFileError('이미지 파일만 업로드 가능합니다.');
                    return;
                }
                if (file.size > MAX_TOTAL_SIZE) {
                    showFileError('이미지 파일은 1개당 5MB 이하만 업로드 가능합니다.');
                    return;
                }
                totalSize += file.size;
            }

            if (totalSize > MAX_TOTAL_SIZE) {
                showFileError('새로 추가하는 이미지의 총 용량은 5MB 이하여야 합니다.');
                return;
            }
        });

        function showFileError(message) {
            msgEl.textContent = message;
            msgEl.style.color = '#ef4444';
            msgEl.style.fontWeight = 'bold';
            inputEl.value = ''; // 초기화
        }
    }

    // 적용
    const fileInput = document.getElementById('uploadFiles');
    const msg = document.getElementById('fileMsg');
    validateImageUpload(fileInput, msg);
    updateFileMsg(); // 초기 실행


    // --- [3] 책 검색 로직 (Form과 동일) ---
    searchBtn.addEventListener('click', searchBooks);
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchBooks();
        }
    });

    function searchBooks() {
        const query = searchInput.value.trim();
        if (!query) {
            alert('검색어를 입력하세요');
            return;
        }

        fetch('/trade/book?query=' + encodeURIComponent(query))
            .then(response => response.json())
            .then(books => {
                displaySearchResults(books);
            })
            .catch(error => {
                console.error('검색 오류:', error);
                alert('검색 중 오류가 발생했습니다');
            });
    }

    function displaySearchResults(books) {
        searchResults.innerHTML = '';
        if (!books || books.length === 0) {
            searchResults.innerHTML = '<div class="p-6 text-center text-gray-500 font-medium">검색 결과가 없습니다</div>';
            searchResults.classList.remove('hidden');
            return;
        }

        books.forEach(book => {
            const item = document.createElement('div');
            item.className = 'flex gap-4 p-4 hover:bg-blue-50 cursor-pointer border-b border-gray-50 last:border-b-0 transition-colors duration-200';

            const bTitle = book.book_title || '';
            const bIsbn = book.isbn || "isbn 조회 불가";
            const bAuthor = book.book_author || '';
            const bImg = book.book_img;
            const bPrice = book.book_org_price;

            var img = document.createElement('img');
            img.src = bImg ? bImg : '/img/no-image.png';
            img.alt = bTitle;
            img.className = 'w-12 h-16 object-cover rounded-lg shadow-sm bg-white shrink-0';

            var infoDiv = document.createElement('div');
            infoDiv.className = 'flex-1 min-w-0 flex flex-col justify-center';

            var pTitle = document.createElement('p');
            pTitle.className = 'font-bold text-gray-900 text-sm truncate';
            pTitle.textContent = bTitle;

            var pAuthor = document.createElement('p');
            pAuthor.className = 'text-xs text-gray-500 truncate mt-0.5';
            pAuthor.textContent = bAuthor;

            var priceDiv = document.createElement('div');
            priceDiv.className = 'flex justify-between items-end mt-1';
            var pIsbn = document.createElement('p');
            pIsbn.className = 'text-[10px] text-gray-400 font-mono truncate';
            pIsbn.textContent = bIsbn;
            var pPrice = document.createElement('p');
            pPrice.className = 'text-xs text-blue-600 font-bold';
            pPrice.textContent = bPrice ? bPrice.toLocaleString() + '원' : '';
            priceDiv.appendChild(pIsbn);
            priceDiv.appendChild(pPrice);

            infoDiv.appendChild(pTitle);
            infoDiv.appendChild(pAuthor);
            infoDiv.appendChild(priceDiv);

            item.appendChild(img);
            item.appendChild(infoDiv);
            item.addEventListener('click', () => selectBook(book));
            searchResults.appendChild(item);
        });

        searchResults.classList.remove('hidden');
    }

    function selectBook(book) {
        document.getElementById('isbn').value = book.isbn || '';
        document.getElementById('book_title').value = book.book_title || '';
        document.getElementById('book_author').value = book.book_author || '';
        document.getElementById('book_publisher').value = book.book_publisher || '';
        document.getElementById('book_org_price').value = book.book_org_price || '';
        document.getElementById('book_img').value = book.book_img || '';

        document.getElementById('previewImg').src = book.book_img || '/img/no-image.png';
        document.getElementById('previewTitle').textContent = book.book_title || '';
        document.getElementById('previewAuthor').textContent = book.book_author || '';
        document.getElementById('previewPublisher').textContent = book.book_publisher || '';
        document.getElementById('previewPrice').textContent = book.book_org_price ? '정가: ' + book.book_org_price.toLocaleString() + '원' : '';

        // 수정 페이지에서는 이미 보여지고 있으나, 확실히 표시
        selectedBookPreview.classList.remove('hidden');
        selectedBookPreview.classList.add('flex');
        searchResults.classList.add('hidden');
        searchInput.value = '';
    }

    clearBookBtn.addEventListener('click', function() {
        ['isbn', 'book_title', 'book_author', 'book_publisher', 'book_org_price', 'book_img'].forEach(id => document.getElementById(id).value = '');
        selectedBookPreview.classList.add('hidden');
        selectedBookPreview.classList.remove('flex');
    });

    // 외부 클릭 시 검색결과 닫기
    document.addEventListener('click', function(e) {
        if (!searchResults.contains(e.target) && e.target !== searchInput && e.target !== searchBtn) {
            searchResults.classList.add('hidden');
        }
    });

    // 주소 검색
    window.searchRG = function() {
        new daum.Postcode({
            oncomplete: function(data) {
                var region = data.sido + ' ' + data.sigungu;
                document.getElementById('sale_rg').value = region;
            }
        }).open();
    };

    // 가격 검증
    function validatePrice(inputId, min, max, errorId) {
        const input = document.getElementById(inputId);
        const error = document.getElementById(errorId);
        input.addEventListener('blur', function () {
            if (this.value === '') return;
            const value = Number(this.value);
            if (value < min || value > max) {
                this.classList.add('ring-2', 'ring-red-500/50', 'border-red-500');
                error.classList.remove('hidden');
                this.focus();
            }
        });
        input.addEventListener('input', function () {
            this.classList.remove('ring-2', 'ring-red-500/50', 'border-red-500');
            error.classList.add('hidden');
        });
    }

    validatePrice('sale_price', 0, 10000000, 'sale_price_error');
    validatePrice('delivery_cost', 0, 50000, 'delivery_cost_error');


    // --- [4] 폼 제출 검증 ---
    document.querySelector('form').addEventListener('submit', function(e) {
        // 1. 책 선택 여부
        const bookTitle = document.getElementById('book_title').value.trim();
        if (!bookTitle) {
            e.preventDefault();
            alert('판매하실 책을 검색하여 선택해주세요.');
            searchInput.focus();
            return;
        }

        // 2. 이미지 개수 (기존 + 신규)
        const existingCount = getExistingImageCount();
        const newFiles = document.getElementById('uploadFiles').files;
        const totalImageCount = existingCount + newFiles.length;

        if (totalImageCount > 3) {
            e.preventDefault();
            alert('이미지는 최대 3장까지 등록 가능합니다. (현재: ' + totalImageCount + '장)');
            return;
        }

        // 3. 신규 이미지 용량
        if (newFiles.length > 0) {
            let totalSize = 0;
            for (let file of newFiles) {
                totalSize += file.size;
            }
            if (totalSize > 5 * 1024 * 1024) {
                e.preventDefault();
                alert('새로 추가하는 이미지의 총 용량은 5MB 이하여야 합니다.');
                return;
            }
        }

        // 4. 가격
        const salePrice = Number(document.getElementById('sale_price').value);
        if (isNaN(salePrice) || salePrice < 0 || salePrice > 10000000) {
            e.preventDefault();
            alert('판매 금액을 확인해주세요 (0 ~ 1천만원).');
            document.getElementById('sale_price').focus();
            return;
        }

        // 5. 배송비
        const deliveryCost = Number(document.getElementById('delivery_cost').value);
        if (isNaN(deliveryCost) || deliveryCost < 0 || deliveryCost > 50000) {
            e.preventDefault();
            alert('배송비를 확인해주세요 (0 ~ 5만원).');
            document.getElementById('delivery_cost').focus();
            return;
        }
    });
</script>

<jsp:include page="../common/footer.jsp" />