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
            <h1 class="text-4xl font-black text-gray-900 tracking-tighter mb-4">판매글 등록</h1>
            <p class="text-gray-500 text-lg font-medium">새로운 주인에게 보낼 책의 이야기를 들려주세요.</p>
        </div>

        <div class="bg-white rounded-[2.5rem] shadow-[0_20px_60px_-15px_rgba(0,0,0,0.05)] overflow-hidden border border-gray-100 p-8 md:p-12 relative">
            <div class="absolute top-0 right-0 w-64 h-64 bg-blue-50/50 rounded-full blur-3xl -mr-32 -mt-32 pointer-events-none"></div>

            <form action="/trade" method="post" enctype="multipart/form-data" class="space-y-12 relative z-10">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <section>
                    <div class="flex items-center gap-4 mb-8">
                        <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center shadow-sm">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
                        </div>
                        <div>
                            <h2 class="text-xl font-bold text-gray-900">도서 검색</h2>
                            <p class="text-sm text-gray-400 font-medium">판매할 책을 검색하여 선택해주세요.</p>
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

                    <div id="selectedBookPreview" class="hidden mt-6 p-6 bg-gradient-to-r from-blue-50 to-white rounded-[2rem] border border-blue-100 flex items-start gap-6 relative animate-[fadeIn_0.4s_cubic-bezier(0.22, 1, 0.36, 1)] group">
                        <img id="previewImg" src="" alt="책 표지" class="w-28 h-40 object-cover rounded-xl shadow-lg bg-white shrink-0 transform transition-transform group-hover:scale-105 duration-500" />
                        <div class="flex-1 py-1">
                            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-blue-600 text-white text-[11px] font-bold rounded-full mb-3 shadow-md shadow-blue-500/20">
                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                                선택 완료
                            </span>
                            <h3 id="previewTitle" class="text-xl font-black text-gray-900 leading-tight mb-2 tracking-tight"></h3>
                            <div class="space-y-1 mb-4">
                                <p class="text-sm font-bold text-gray-600 flex items-center gap-2">
                                    <span id="previewAuthor"></span>
                                </p>
                                <p id="previewPublisher" class="text-xs font-medium text-gray-400"></p>
                            </div>
                            <div class="flex items-baseline gap-1">
                                <span class="text-xs font-medium text-gray-400">정가</span>
                                <p id="previewPrice" class="text-lg text-blue-600 font-black"></p>
                            </div>
                        </div>
                        <button type="button" id="clearBookBtn" class="absolute top-5 right-5 p-2 bg-white rounded-full text-gray-300 hover:text-red-500 hover:bg-red-50 transition-all shadow-sm border border-gray-100">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                        </button>
                    </div>

                    <input type="hidden" id="isbn" name="isbn" required />
                    <input type="hidden" id="book_title" name="book_title" required />
                    <input type="hidden" id="book_author" name="book_author" required />
                    <input type="hidden" id="book_publisher" name="book_publisher" required />
                    <input type="hidden" id="book_org_price" name="book_org_price" required/>
                    <input type="hidden" id="book_img" name="book_img" required />
                </section>

                <hr class="border-gray-100">

                <section>
                    <div class="flex items-center gap-4 mb-10">
                        <div class="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center shadow-sm">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.91 8.84 8.56 2.23a1.93 1.93 0 0 0-1.81 0L3.1 4.13a2.12 2.12 0 0 0-.05 3.69l12.22 6.93a2 2 0 0 0 1.94 0L21 12.51a2.12 2.12 0 0 0-.09-3.67Z"/><path d="m3.09 8.84 12.35-6.61a1.93 1.93 0 0 1 1.81 0l3.65 1.9a2.12 2.12 0 0 1 .1 3.69L8.73 14.75a2 2 0 0 1-1.94 0L3 12.51a2.12 2.12 0 0 1 .09-3.67Z"/></svg>
                        </div>
                        <div>
                            <h2 class="text-xl font-bold text-gray-900">판매 정보</h2>
                            <p class="text-sm text-gray-400 font-medium">가격과 상태를 입력해주세요.</p>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-10">

                        <div class="md:col-span-2 group">
                            <label for="sale_title" class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                                판매글 제목 <span class="text-blue-500">*</span>
                            </label>
                            <input type="text" id="sale_title" name="sale_title" required
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
                                        <option value="${cat.category_seq}" data-nm="${cat.category_nm}">${cat.category_nm}</option>
                                    </c:forEach>
                                </select>
                                <div class="absolute right-5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
                                </div>
                                <input type="hidden" name="category_nm" id="category_nm">
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
                                    <option value="NEW">새책 (미사용)</option>
                                    <option value="LIKE_NEW">거의 새책 (사용감 없음)</option>
                                    <option value="GOOD">좋음 (깨끗함)</option>
                                    <option value="USED">사용됨 (흔적 있음)</option>
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
                                <input type="number" id="sale_price" name="sale_price" required max="10000000" placeholder="0"
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
                                <input type="number" id="delivery_cost" name="delivery_cost" required max="50000" placeholder="3000"
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
                                    <input type="radio" name="payment_type" value="account" class="peer sr-only" required />
                                    <div class="px-6 py-5 rounded-2xl bg-gray-50 border-2 border-transparent text-center font-bold text-gray-500 peer-checked:bg-white peer-checked:border-blue-500 peer-checked:text-blue-600 peer-checked:shadow-lg peer-checked:shadow-blue-500/20 transition-all duration-200 hover:bg-gray-100">
                                        계좌이체
                                    </div>
                                    <div class="absolute top-4 right-4 opacity-0 peer-checked:opacity-100 transition-opacity text-blue-500">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                                    </div>
                                </label>
                                <label class="cursor-pointer relative">
                                    <input type="radio" name="payment_type" value="tosspay" class="peer sr-only" />
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
                                           value="${trade.sale_rg}" placeholder="우측 버튼으로 주소를 검색하세요"
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
                                      class="w-full px-6 py-5 bg-gray-50 border border-transparent rounded-3xl text-gray-900 font-medium focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none resize-none leading-relaxed"></textarea>
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
                                <h2 class="text-xl font-bold text-gray-900">사진 추가</h2>
                                <p class="text-sm text-gray-400 font-medium">상품의 상태를 확인할 수 있는 사진을 올려주세요.</p>
                            </div>
                        </div>
                        <span class="text-xs font-bold text-blue-600 bg-blue-50 border border-blue-100 px-4 py-1.5 rounded-full">최대 3장</span>
                    </div>

                    <div class="relative w-full h-48 rounded-[2rem] border-2 border-dashed border-gray-200 bg-gray-50 hover:bg-blue-50/50 hover:border-blue-300 transition-all duration-300 flex flex-col items-center justify-center group cursor-pointer overflow-hidden">
                        <input type="file" name="uploadFiles" accept="image/*" multiple
                               class="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10" />

                        <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-sm mb-4 group-hover:scale-110 group-hover:shadow-md transition-all duration-300">
                            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-gray-400 group-hover:text-blue-600"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" x2="12" y1="3" y2="15"/></svg>
                        </div>
                        <p class="text-base font-bold text-gray-600 group-hover:text-blue-600 transition-colors">클릭하여 사진 업로드</p>
                        <p id="fileMsg" class="text-xs text-gray-400 mt-1 font-medium">또는 파일을 여기로 드래그하세요</p>
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
                        판매글 등록하기
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
// --- [기존 로직 유지] ---

const searchInput = document.getElementById('bookSearch');
const searchBtn = document.getElementById('searchBtn');
const searchResults = document.getElementById('searchResults');
const selectedBookPreview = document.getElementById('selectedBookPreview');
const clearBookBtn = document.getElementById('clearBookBtn');

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

function clearOnClick(el) {
    if (el.value !== '') {
        el.value = '';
        el.dataset.cleared = 'true';
    }
}

function validateImageUpload(inputEl, msgEl) {
    const MAX_COUNT = 3;
    const MAX_SIZE = 5 * 1024 * 1024;

    inputEl.addEventListener('change', () => {
        const files = Array.from(inputEl.files);

        // 파일명 표시를 위한 로직 추가 (UI 개선)
        if(files.length > 0 && files.length <= MAX_COUNT) {
             msgEl.textContent = `\${files.length}개의 파일이 선택되었습니다.`;
             msgEl.style.color = '#2563eb'; // blue-600
             msgEl.style.fontWeight = 'bold';
        }

        if (files.length > MAX_COUNT) {
            showFileError(`최대 \${MAX_COUNT}개까지 업로드 가능합니다.`);
            return;
        }

        for (let file of files) {
            if (!file.type.startsWith('image/')) {
                showFileError('이미지 파일만 업로드 가능합니다.');
                return;
            }
            if (file.size > MAX_SIZE) {
                showFileError('이미지 파일은 1개당 5MB 이하만 업로드 가능합니다.');
                return;
            }
        }

        if(files.length <= MAX_COUNT) {
             // Pass
        }
    });

    function showFileError(message) {
        msgEl.textContent = message;
        msgEl.style.color = '#ef4444'; // red-500
        msgEl.style.fontWeight = 'bold';
        inputEl.value = '';
    }

    function clearFileError() {
        msgEl.textContent = '또는 파일을 여기로 드래그하세요';
        msgEl.style.color = '';
        msgEl.style.fontWeight = '';
    }
}

const fileInput = document.querySelector('input[name="uploadFiles"]');
const msg = document.getElementById('fileMsg');
validateImageUpload(fileInput, msg);

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

// [UI 개선] 검색 결과 표시 HTML 수정
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

document.addEventListener('click', function(e) {
    if (!searchResults.contains(e.target) && e.target !== searchInput && e.target !== searchBtn) {
        searchResults.classList.add('hidden');
    }
});

function searchRG() {
    new daum.Postcode({
        oncomplete: function(data) {
            var region = data.sido + ' ' + data.sigungu;
            document.getElementById('sale_rg').value = region;
        }
    }).open();
}

function validatePrice(inputId, min, max, errorId) {
    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);

    input.addEventListener('blur', function () {
        if (this.value === '') return;
        const value = Number(this.value);
        if (value < min || value > max) {
            this.classList.add('ring-2', 'ring-red-500/50', 'border-red-500'); // Style update
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

document.querySelector('form').addEventListener('submit', function(e) {
    const bookTitle = document.getElementById('book_title').value.trim();
    if (!bookTitle) {
        e.preventDefault();
        alert('판매하실 책을 검색하여 선택해주세요.');
        searchInput.focus();
        return;
    }
    const salePrice = Number(document.getElementById('sale_price').value);
    if (isNaN(salePrice) || salePrice < 0 || salePrice > 10000000) {
        e.preventDefault();
        alert('판매 금액을 확인해주세요.');
        document.getElementById('sale_price').focus();
        return;
    }
    const deliveryCost = Number(document.getElementById('delivery_cost').value);
    if (isNaN(deliveryCost) || deliveryCost < 0 || deliveryCost > 50000) {
        e.preventDefault();
        alert('배송비를 확인해주세요.');
        document.getElementById('delivery_cost').focus();
        return;
    }
});
</script>

<jsp:include page="../common/footer.jsp" />