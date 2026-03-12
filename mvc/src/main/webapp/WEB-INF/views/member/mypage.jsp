<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="../common/header.jsp" />

<div class="flex flex-col lg:flex-row gap-8 max-w-6xl mx-auto px-4 sm:px-6">

    <div class="lg:w-1/4">
        <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden sticky top-24">
            <div class="p-8 text-center border-b border-gray-50 bg-primary-50/30"> <div class="w-20 h-20 bg-white border-2 border-white shadow-md rounded-full flex items-center justify-center text-2xl font-black text-primary-600 mx-auto mb-4">
                <c:out value="${fn:substring(sessionScope.loginSess.member_nicknm, 0, 1)}"/>
            </div>
                <h2 class="font-extrabold text-gray-900 text-lg tracking-tight"><c:out value="${sessionScope.loginSess.member_nicknm}"/>님</h2>
                <p class="text-xs font-medium text-gray-400 mt-1"><c:out value="${sessionScope.loginSess.member_email}"/></p>
            </div>

            <nav class="p-3 space-y-1">
                <a href="#" data-tab="profile" onclick="loadTab(event, 'profile')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="user" class="w-4 h-4"></i> 내 프로필
                </a>

                <a href="#" data-tab="purchases" onclick="loadTab(event, 'purchases')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="shopping-bag" class="w-4 h-4"></i> 구매 내역
                </a>

                <a href="#" data-tab="sales" onclick="loadTab(event, 'sales')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="package" class="w-4 h-4"></i> 판매 내역
                </a>

                <a href="#" data-tab="wishlist" onclick="loadTab(event, 'wishlist')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="heart" class="w-4 h-4"></i> 찜한 상품
                </a>

                <a href="#" data-tab="groups" onclick="loadTab(event, 'groups')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="users" class="w-4 h-4"></i> 내 모임
                </a>

                <a href="#" data-tab="addresses" onclick="loadTab(event, 'addresses')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="map-pin" class="w-4 h-4"></i> 배송지 관리
                </a>

                <a href="#" data-tab="bankaccount" onclick="loadTab(event, 'bankaccount')"
                   class="nav-btn block w-full text-left px-5 py-3.5 rounded-2xl text-sm font-bold text-gray-500 hover:bg-primary-50 hover:text-primary-600 transition-all flex items-center gap-3">
                    <i data-lucide="landmark" class="w-4 h-4"></i> 정산 계좌
                </a>
            </nav>

            <div class="p-4 border-t border-gray-100">
                <form method="post" action="/logout" style="display:inline;">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <button type="submit"
                            class="w-full flex items-center justify-center gap-2 text-xs font-bold text-gray-400 hover:text-red-500 py-2 transition-colors">
                        <i data-lucide="log-out" class="w-3.5 h-3.5"></i> 로그아웃
                    </button>
                </form>
            </div>
        </div>
    </div>

    <div class="lg:flex-1 min-h-[600px]">
        <div id="tab-content" class="bg-transparent"></div>
    </div>
</div>

<script>
    window.loadTab = async function(event, tabName) {
        if(event) event.preventDefault();

        // 탭 스타일 업데이트
        updateActiveTab(tabName);

        try {
            document.getElementById('tab-content').innerHTML = `
                <div class="animate-pulse space-y-6">
                    <div class="h-8 bg-gray-200 rounded-lg w-1/4"></div>
                    <div class="h-64 bg-white border border-gray-100 rounded-3xl shadow-sm"></div>
                </div>
             `;

            const response = await fetch('/mypage/tab/' + tabName);
            if (!response.ok) throw new Error('Network response was not ok');
            const html = await response.text();

            const tabContent = document.getElementById('tab-content');
            tabContent.innerHTML = html;

            const scripts = tabContent.querySelectorAll('script');
            scripts.forEach(script => {
                const newScript = document.createElement('script');
                if (script.src) newScript.src = script.src;
                else newScript.textContent = script.textContent;
                document.body.appendChild(newScript);
                document.body.removeChild(newScript);
            });

            if(window.lucide) lucide.createIcons();

            history.pushState({tab: tabName}, '', '/mypage/' + tabName);

        } catch (error) {
            console.error('Tab error:', error);
            document.getElementById('tab-content').innerHTML =
                '<div class="text-center py-20 text-red-500 font-bold">컨텐츠를 불러오지 못했습니다.</div>';
        }
    };

    function updateActiveTab(tabName) {
        document.querySelectorAll('[data-tab]').forEach(link => {
            // [수정] 기존 검은색(bg-gray-900) 제거
            link.classList.remove('bg-primary-600', 'text-white', 'shadow-md');
            link.classList.add('text-gray-500', 'hover:bg-primary-50', 'hover:text-primary-600');
            const icon = link.querySelector('svg');
            if(icon) icon.style.color = '';
        });

        const activeLink = document.querySelector('[data-tab="' + tabName + '"]');
        if (activeLink) {
            // [수정] 파란색(bg-primary-600) 적용
            activeLink.classList.remove('text-gray-500', 'hover:bg-primary-50', 'hover:text-primary-600');
            activeLink.classList.add('bg-primary-600', 'text-white', 'shadow-md');
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const pathParts = window.location.pathname.split('/');
        const currentTab = pathParts[2] || 'profile';
        loadTab(null, currentTab);
    });

    window.addEventListener('popstate', (event) => {
        if (event.state && event.state.tab) {
            loadTab(null, event.state.tab);
        }
    });
</script>

<jsp:include page="../common/footer.jsp" />