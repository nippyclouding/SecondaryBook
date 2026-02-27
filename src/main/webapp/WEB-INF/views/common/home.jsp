<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<script src="https://unpkg.com/lucide@latest"></script>

<jsp:include page="header.jsp" />

<div class="space-y-10 font-sans text-gray-900 animate-fade-in-up">

    <div class="relative overflow-hidden rounded-[2rem] shadow-xl shadow-blue-100 h-[380px] transform transition-all hover:scale-[1.005] duration-500 group" id="bannerCarousel">
        <div class="flex transition-transform duration-700 cubic-bezier(0.4, 0, 0.2, 1) h-full" id="bannerSlider">
            <c:choose>
                <c:when test="${not empty bannerList}">
                    <c:forEach var="b" items="${bannerList}" varStatus="st">
                        <div class="w-full flex-shrink-0 px-16 pb-12 flex items-center relative"
                             style="background: linear-gradient(135deg, <c:out value='${b.bgColorFrom}'/>, <c:out value='${b.bgColorTo}'/>);">

                            <c:set var="alignClass" value="text-left" />
                            <c:set var="posClass" value="mr-auto" />
                            <c:set var="realSubtitle" value="${b.subtitle}" />

                            <c:if test="${fn:contains(b.subtitle, '|||')}">
                                <c:set var="parts" value="${fn:split(b.subtitle, '|')}" />
                                <c:set var="alignClass" value="${parts[0]}" />
                                <c:choose>
                                    <c:when test="${alignClass eq 'text-center'}"><c:set var="posClass" value="mx-auto" /></c:when>
                                    <c:when test="${alignClass eq 'text-right'}"><c:set var="posClass" value="ml-auto" /></c:when>
                                </c:choose>
                                <c:set var="lastIndex" value="${fn:length(parts) - 1}" />
                                <c:set var="realSubtitle" value="${parts[lastIndex]}" />
                            </c:if>

                            <div class="z-10 text-white max-w-2xl w-full ${alignClass} ${posClass} space-y-5 mb-8">
                                <div class="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-white/20 backdrop-blur-md mb-2 shadow-inner">
                                    <i data-lucide="${b.iconName != null ? b.iconName : 'star'}" class="w-7 h-7 text-white"></i>
                                </div>

                                <h1 class="text-5xl font-black tracking-tight leading-tight drop-shadow-sm"><c:out value="${b.title}"/></h1>
                                <p class="text-white/90 text-xl font-medium tracking-wide leading-relaxed"><c:out value="${realSubtitle}"/></p>

                                <c:if test="${not empty b.btnLink}">
                                    <a href="<c:out value='${b.btnLink}'/>" class="group inline-flex items-center gap-2 bg-white text-blue-900 px-7 py-3.5 rounded-full font-bold text-sm hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl hover:-translate-y-0.5">
                                            <c:out value="${b.btnText}"/>
                                        <i data-lucide="arrow-right" class="w-4 h-4 text-blue-600 transition-transform group-hover:translate-x-1"></i>
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="w-full flex-shrink-0 bg-gradient-to-br from-blue-600 to-blue-800 px-16 pb-12 flex items-center relative">
                        <div class="z-10 text-white max-w-2xl mb-8">
                            <h1 class="text-5xl font-black mb-4 tracking-tight">Secondary Books</h1>
                            <p class="text-white/80 text-xl font-medium">Welcome. Experience better trading.</p>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="absolute bottom-6 left-1/2 transform -translate-x-1/2 flex gap-2 z-20 px-4 py-2 rounded-full" id="bannerDots">
            <c:choose>
                <c:when test="${not empty bannerList}">
                    <c:forEach var="b" items="${bannerList}" varStatus="st">
                        <button onclick="setBanner(${st.index})" class="h-1.5 rounded-full transition-all duration-300 shadow-sm ${st.first ? 'bg-white w-8' : 'bg-white/50 w-2 hover:bg-white/80'}"></button>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <button class="w-8 h-1.5 rounded-full bg-white"></button>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="absolute inset-0 flex justify-between items-center px-4 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
            <button onclick="prevBanner()" class="pointer-events-auto bg-black/20 hover:bg-black/40 backdrop-blur-md p-3 rounded-full text-white transition-all transform hover:scale-110 border border-white/10">
                <i data-lucide="chevron-left" class="w-6 h-6"></i>
            </button>
            <button onclick="nextBanner()" class="pointer-events-auto bg-black/20 hover:bg-black/40 backdrop-blur-md p-3 rounded-full text-white transition-all transform hover:scale-110 border border-white/10">
                <i data-lucide="chevron-right" class="w-6 h-6"></i>
            </button>
        </div>
    </div>

    <div class="flex flex-col gap-6">

        <div class="flex flex-col xl:flex-row xl:items-center justify-between gap-6 pb-2">
            <div class="flex items-center gap-3">
                <h2 class="text-3xl font-black text-gray-900 tracking-tight">전체 상품</h2>
                <c:if test="${not empty totalCount}">
                    <span id="tradeTotalCount" class="text-sm font-bold text-blue-600 bg-blue-50 px-3 py-1 rounded-full shadow-sm border border-blue-100">
                            ${totalCount}
                    </span>
                </c:if>
            </div>

            <div class="flex-1 max-w-3xl relative group">
                <input type="text"
                       id="searchInput"
                       placeholder="찾고 싶은 도서나 저자를 검색해보세요"
                       class="w-full pl-6 pr-16 py-4 bg-gray-50 border border-gray-200 rounded-full text-base font-medium text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-4 focus:ring-blue-100 focus:border-blue-500 focus:bg-white transition-all shadow-sm hover:shadow-md"
                />

                <button type="button"
                        onclick="searchTrade()"
                        class="absolute right-2 top-1/2 transform -translate-y-1/2 w-11 h-11 bg-blue-600 rounded-full flex items-center justify-center text-white hover:bg-blue-700 hover:scale-105 active:scale-95 transition-all shadow-md">
                    <i data-lucide="search" class="w-5 h-5"></i>
                </button>
            </div>

            <div class="flex bg-gray-100 p-1.5 rounded-xl self-start xl:self-center">
                <a href="javascript:void(0)" id="sortNewest" onclick="setSort('newest')"
                   class="px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 text-gray-500 hover:text-gray-900">최신순</a>
                <a href="javascript:void(0)" id="sortPrice" onclick="setSort('priceAsc')"
                   class="px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 text-gray-500 hover:text-gray-900">낮은가격</a>
                <a href="javascript:void(0)" id="sortLikes" onclick="setSort('likeDesc')"
                   class="px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 text-gray-500 hover:text-gray-900">인기순</a>
            </div>
        </div>

        <div class="flex flex-wrap items-center gap-3">

            <c:set var="btnBaseClass" value="px-5 py-2.5 rounded-full text-sm font-bold transition-all flex items-center gap-2 border border-transparent bg-gray-100 text-gray-600 hover:bg-gray-200 hover:text-gray-900 active:scale-95" />

            <div class="relative">
                <button onclick="toggleDropdown('category')" id="categoryBtn" class="${btnBaseClass}">
                    <span id="categoryText">카테고리</span>
                    <i data-lucide="chevron-down" class="w-4 h-4 text-gray-400"></i>
                </button>
                <div id="categoryDropdown" class="hidden absolute top-full left-0 mt-2 w-56 bg-white/95 backdrop-blur-xl rounded-2xl shadow-xl border border-gray-100 z-30 py-2 max-h-[300px] overflow-y-auto animate-fade-in-down">
                    <a href="javascript:selectCategory(null, '전체')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">전체</a>
                    <c:forEach var="cat" items="${category}">
                        <a href="javascript:selectCategory(${cat.category_seq}, '${fn:escapeXml(cat.category_nm)}')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">
                                <c:out value="${cat.category_nm}"/>
                        </a>
                    </c:forEach>
                </div>
            </div>

            <div class="relative">
                <button onclick="toggleDropdown('condition')" id="conditionBtn" class="${btnBaseClass}">
                    <span id="conditionText">상품 상태</span>
                    <i data-lucide="chevron-down" class="w-4 h-4 text-gray-400"></i>
                </button>
                <div id="conditionDropdown" class="hidden absolute top-full left-0 mt-2 w-48 bg-white/95 backdrop-blur-xl rounded-2xl shadow-xl border border-gray-100 z-30 py-2 animate-fade-in-down">
                    <a href="javascript:selectBookStatus(null, '전체보기')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">전체보기</a>
                    <a href="javascript:selectBookStatus('NEW', '새책')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">새책</a>
                    <a href="javascript:selectBookStatus('LIKE_NEW', '보통')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">보통</a>
                    <a href="javascript:selectBookStatus('GOOD', '좋음')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">좋음</a>
                    <a href="javascript:selectBookStatus('USED', '사용됨')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">사용됨</a>
                </div>
            </div>

            <div class="relative">
                <button onclick="toggleDropdown('saleStatus')" id="saleStatusBtn" class="${btnBaseClass}">
                    <span id="saleStatusText">판매중</span>
                    <i data-lucide="chevron-down" class="w-4 h-4 text-gray-400"></i>
                </button>
                <div id="saleStatusDropdown" class="hidden absolute top-full left-0 mt-2 w-48 bg-white/95 backdrop-blur-xl rounded-2xl shadow-xl border border-gray-100 z-30 py-2 animate-fade-in-down">
                    <a href="javascript:selectSaleStatus('SALE', '판매중')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">판매중</a>
                    <a href="javascript:selectSaleStatus('SOLD', '판매완료')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">판매완료</a>
                    <a href="javascript:selectSaleStatus(null, '판매중 & 완료')" class="block px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors">판매중 & 완료</a>
                </div>
            </div>

            <button type="button" onclick="resetFilters()"
                    class="ml-auto px-4 py-2.5 rounded-full text-sm font-bold transition-all flex items-center gap-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50">
                <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
                초기화
            </button>
        </div>
    </div>

    <div id="tradelist" class="mt-8 min-h-[500px]">
        <jsp:include page="/WEB-INF/views/trade/tradelist.jsp" />
    </div>
</div>

<style>
    @keyframes fadeInUp {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    .animate-fade-in-up { animation: fadeInUp 0.5s ease-out forwards; }

    @keyframes fadeInDown {
        from { opacity: 0; transform: translateY(-5px); }
        to { opacity: 1; transform: translateY(0); }
    }
    .animate-fade-in-down { animation: fadeInDown 0.2s ease-out forwards; }
</style>

<script>
    // --- [1. 배너 로직] ---
    let currentBanner = 0;
    const totalBanners = ${not empty bannerList ? fn:length(bannerList) : 1};
    let openDropdown = null;

    function setBanner(index) {
        currentBanner = index;
        updateBanner();
    }

    function prevBanner() {
        currentBanner = (currentBanner - 1 + totalBanners) % totalBanners;
        updateBanner();
    }

    function nextBanner() {
        currentBanner = (currentBanner + 1) % totalBanners;
        updateBanner();
    }

    function updateBanner() {
        const slider = document.getElementById('bannerSlider');
        if (slider) {
            slider.style.transform = `translateX(-\${currentBanner * 100}%)`;
        }

        const dotsContainer = document.getElementById('bannerDots');
        if (dotsContainer) {
            const dots = dotsContainer.children;
            for (let i = 0; i < dots.length; i++) {
                if (i === currentBanner) {
                    // Active Dot
                    dots[i].classList.remove('bg-white/50', 'w-2');
                    dots[i].classList.add('bg-white', 'w-8');
                } else {
                    // Inactive Dot
                    dots[i].classList.remove('bg-white', 'w-8');
                    dots[i].classList.add('bg-white/50', 'w-2');
                }
            }
        }
    }

    setInterval(nextBanner, 5000);


    // --- [2. 검색 및 필터 로직] ---
    const tradeFilter = {
        categorySeq: null,
        book_st: null,
        search_word: null,
        sale_st: 'SALE',
        page: 1,
        sort: null
    };

    function loadTrade() {
        const data = { page: tradeFilter.page };

        if (tradeFilter.categorySeq) data.category_seq = tradeFilter.categorySeq;
        if (tradeFilter.book_st) data.book_st = tradeFilter.book_st;
        if (tradeFilter.search_word) data.search_word = tradeFilter.search_word;
        data.sale_st = tradeFilter.sale_st;
        if (tradeFilter.sort) data.sort = tradeFilter.sort;

        $.ajax({
            url: '/home',
            type: 'GET',
            data: data,
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function (html, status, xhr) {
                $('#tradelist').html(html);
                const totalCount = xhr.getResponseHeader('X-Total-Count');
                if(totalCount !== null) {
                    $('#tradeTotalCount').text(totalCount);
                }
                openDropdown = null;
                if (typeof lucide !== 'undefined') lucide.createIcons();
            },
            error: function (xhr, status, error) { console.error('AJAX 오류:', error); }
        });
    }

    function setSort(sortKey) {
        tradeFilter.sort = sortKey === 'newest' ? null : sortKey;
        tradeFilter.page = 1;
        updateSortCss();
        loadTrade();
    }

    function goPage(page) {
        tradeFilter.page = page;
        loadTrade();
    }

    function updateSortCss() {
        const allSorts = ['sortNewest', 'sortPrice', 'sortLikes'];
        allSorts.forEach(id => {
            const el = document.getElementById(id);
            // Default Style
            el.className = 'px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 text-gray-500 hover:text-gray-900 hover:bg-gray-200/50';
        });

        // Active Style (White bg + Shadow)
        let activeId = 'sortNewest';
        if (tradeFilter.sort === 'priceAsc') activeId = 'sortPrice';
        if (tradeFilter.sort === 'likeDesc') activeId = 'sortLikes';

        const activeEl = document.getElementById(activeId);
        activeEl.className = 'px-4 py-2 rounded-lg text-sm font-bold transition-all duration-200 bg-white text-gray-900 shadow-sm ring-1 ring-black/5';
    }

    // --- 필터 버튼 스타일 관리 (Blue 포인트 적용) ---
    function updateFilterBtnStyle(btnId, isActive) {
        const btn = document.getElementById(btnId);
        // 기본 클래스
        let baseClass = "px-5 py-2.5 rounded-full text-sm font-bold transition-all flex items-center gap-2 border border-transparent bg-gray-100 text-gray-600 hover:bg-gray-200 hover:text-gray-900 active:scale-95";

        if (isActive) {
            // 활성 상태 (Blue Background)
            btn.className = "px-5 py-2.5 rounded-full text-sm font-bold transition-all flex items-center gap-2 border border-transparent bg-blue-600 text-white shadow-md hover:bg-blue-700 active:scale-95";
            const icon = btn.querySelector('svg');
            if(icon) icon.classList.replace('text-gray-400', 'text-white/80');
        } else {
            btn.className = baseClass;
            const icon = btn.querySelector('svg');
            if(icon) icon.classList.replace('text-white/80', 'text-gray-400');
        }
    }

    function selectCategory(seq, name) {
        tradeFilter.categorySeq = seq;
        tradeFilter.page = 1;
        document.getElementById('categoryText').innerText = name;
        document.getElementById('categoryDropdown').classList.add('hidden');
        updateFilterBtnStyle('categoryBtn', seq !== null);
        openDropdown = null;
        loadTrade();
    }

    function selectBookStatus(book_st, name) {
        tradeFilter.book_st = book_st;
        tradeFilter.page = 1;
        document.getElementById('conditionText').innerText = name;
        document.getElementById('conditionDropdown').classList.add('hidden');
        updateFilterBtnStyle('conditionBtn', book_st !== null);
        openDropdown = null;
        loadTrade();
    }

    function selectSaleStatus(sale_st, name) {
        tradeFilter.sale_st = sale_st;
        tradeFilter.page = 1;
        document.getElementById('saleStatusText').innerText = name;
        document.getElementById('saleStatusDropdown').classList.add('hidden');
        updateFilterBtnStyle('saleStatusBtn', sale_st !== 'SALE');
        openDropdown = null;
        loadTrade();
    }

    function searchTrade() {
        const keyword = document.getElementById('searchInput').value;
        tradeFilter.search_word = keyword;
        tradeFilter.page = 1;
        loadTrade();
    }

    document.getElementById('searchInput').addEventListener('keydown', function (e) {
        if (e.key === 'Enter') searchTrade();
    });

    function toggleDropdown(type) {
        const dropdown = document.getElementById(type + 'Dropdown');
        if (openDropdown && openDropdown !== dropdown) {
            openDropdown.classList.add('hidden');
        }
        dropdown.classList.toggle('hidden');
        openDropdown = dropdown.classList.contains('hidden') ? null : dropdown;
    }

    document.addEventListener('click', function(event) {
        if (!event.target.closest('.relative')) {
            if (openDropdown) {
                openDropdown.classList.add('hidden');
                openDropdown = null;
            }
        }
    });

    function resetFilters() {
        tradeFilter.categorySeq = null;
        tradeFilter.book_st = null;
        tradeFilter.search_word = null;
        tradeFilter.sale_st = 'SALE';
        tradeFilter.page = 1;
        tradeFilter.sort = null;

        document.getElementById('searchInput').value = '';
        document.getElementById('categoryText').innerText = '카테고리';
        document.getElementById('conditionText').innerText = '상품 상태';
        document.getElementById('saleStatusText').innerText = '판매중';

        updateFilterBtnStyle('categoryBtn', false);
        updateFilterBtnStyle('conditionBtn', false);
        updateFilterBtnStyle('saleStatusBtn', false);

        if (openDropdown) {
            openDropdown.classList.add('hidden');
            openDropdown = null;
        }

        updateSortCss();
        loadTrade();
    }

    updateSortCss();
    if (typeof lucide !== 'undefined') lucide.createIcons();
    $(document).ready(function() { loadTrade(); });

</script>

<jsp:include page="footer.jsp" />