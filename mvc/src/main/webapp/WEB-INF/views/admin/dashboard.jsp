<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" buffer="512kb"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <sec:csrfMetaTags />
  <title>SecondHand Books Admin Console</title>

  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          colors: {
            primary: { 50: '#eff6ff', 100: '#dbeafe', 500: '#3b82f6', 600: '#0046FF', 700: '#1d4ed8', 900: '#1e3a8a' },
            shinhan: { blue: '#0046FF', gold: '#D4AF37', sky: '#EBF5FF' }
          },
          fontFamily: {
            sans: ['Noto Sans KR', 'sans-serif'],
          }
        }
      }
    }
  </script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap" rel="stylesheet">
  <script>
    function getCsrfToken() {
      const token = document.querySelector('meta[name="_csrf"]');
      const header = document.querySelector('meta[name="_csrf_header"]');
      return {
        token: token ? token.getAttribute('content') : null,
        header: header ? header.getAttribute('content') : null
      };
    }
    // 관리자 AJAX용 fetch wrapper (CSRF 토큰 자동 포함)
    window.adminFetch = function(url, options = {}) {
      const csrf = getCsrfToken();
      if (!options.headers) options.headers = {};
      if (csrf.token && csrf.header) {
        options.headers[csrf.header] = csrf.token;
      }
      return fetch(url, options);
    };
  </script>
</head>
<body class="bg-gray-50 h-screen flex overflow-hidden text-gray-800 font-sans" onclick="closeAllMenus()">

<aside class="w-64 bg-white border-r border-gray-200 flex flex-col z-20 flex-shrink-0">
  <div class="h-16 flex items-center px-6 border-b border-gray-100">
    <div class="flex items-center gap-2 text-primary-600">
      <i data-lucide="shield-check" class="w-6 h-6"></i>
      <span class="text-xl font-black tracking-tight text-gray-900">Admin<span class="text-primary-600">Console</span></span>
    </div>
  </div>

  <nav class="flex-1 overflow-y-auto py-6 px-3 space-y-1">
    <button onclick="switchView('dashboard', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-bold rounded-xl bg-primary-50 text-primary-700 transition-all">
      <i data-lucide="layout-dashboard" class="w-5 h-5"></i> 대시보드
    </button>

    <div class="pt-4 pb-2 px-4 text-[10px] font-extrabold text-gray-400 uppercase tracking-wider">Management</div>

    <button onclick="switchView('users', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="users" class="w-5 h-5"></i> 회원 관리
    </button>
    <button onclick="switchView('usersLog', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="activity" class="w-5 h-5"></i> 회원 접속 기록
    </button>
    <button onclick="switchView('adminLog', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="clipboard-list" class="w-5 h-5"></i> 관리자 활동 로그
    </button>
    <button onclick="switchView('books', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="shopping-bag" class="w-5 h-5"></i> 상품 관리
    </button>
    <button onclick="switchView('safePayList', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
          <i data-lucide="shopping-bag" class="w-5 h-5"></i> 안전결제 내역
    </button>
    <button onclick="switchView('groups', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="book-open" class="w-5 h-5"></i> 모임 관리
    </button>
    <button onclick="switchView('notice', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="megaphone" class="w-5 h-5"></i> 공지사항 관리
    </button>
    <button onclick="switchView('banner', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="image" class="w-5 h-5"></i> 배너 관리
    </button>
    <button onclick="switchView('settlement', this)" class="nav-item w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition-all">
      <i data-lucide="banknote" class="w-5 h-5"></i> 정산 관리
    </button>
  </nav>

  <div class="p-4 border-t border-gray-100">
    <div class="flex items-center gap-3 p-3 rounded-xl bg-gray-50 border border-gray-200">
      <div class="w-9 h-9 rounded-full bg-primary-600 flex items-center justify-center text-white font-bold text-sm">A</div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-bold text-gray-900">${sessionScope.adminSess.admin_login_id}</p>
        <p class="text-xs text-gray-500 truncate">Administrator</p>
      </div>
      <a href="/admin/logout" class="text-gray-400 hover:text-red-500 transition"><i data-lucide="log-out" class="w-4 h-4"></i></a>
    </div>
  </div>
</aside>

<main class="flex-1 overflow-y-auto bg-gray-50/50">
  <header class="bg-white border-b border-gray-200 h-16 flex items-center justify-between px-8 sticky top-0 z-10">
    <div class="flex items-center gap-2 text-sm text-gray-500">
      <span class="font-medium text-gray-400">Console</span>
      <i data-lucide="chevron-right" class="w-4 h-4"></i>
      <span id="page-title" class="font-bold text-gray-900">Dashboard</span>
    </div>
  </header>

  <div class="p-8 max-w-7xl mx-auto space-y-8">
    <div id="view-dashboard" class="view-section animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/dashboardContent.jsp" %>
    </div>

    <div id="view-users" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/usersContent.jsp" %>
    </div>

    <div id="view-usersLog" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/usersLogContent.jsp" %>
    </div>

    <div id="view-adminLog" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/adminLogContent.jsp" %>
    </div>

    <div id="view-books" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/booksContent.jsp" %>
    </div>

    <div id="view-safePayList" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
       <%@ include file="tabs/safePayList.jsp" %>
    </div>

    <div id="view-groups" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/groupsContent.jsp" %>
    </div>

    <div id="view-notice" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/noticeContent.jsp" %>
    </div>

    <div id="view-notice-write" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/noticeWriteForm.jsp" %>
    </div>

     <div id="view-notice-edit" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
          <%@ include file="tabs/noticeEditForm.jsp" %>
        </div>

    <div id="view-banner" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/bannerContent.jsp" %>
    </div>

    <div id="view-settlement" class="view-section hidden animate-[fadeIn_0.3s_ease-out]">
      <%@ include file="tabs/settlementContent.jsp" %>
    </div>
  </div>
</main>


<script>
  (function() {
  const RELOAD_KEY = 'member_page_unload_time';
  const SESSION_CHECK_INTERVAL = 300000;

      document.addEventListener('DOMContentLoaded', function() {

                  adminFetch('/admin/api/cancel-logout', {
                      method: 'POST',
                      credentials: 'same-origin'
                  }).catch(function(err) {
                      console.error('로그아웃 취소 실패:', err);
                  });
              sessionStorage.removeItem(RELOAD_KEY);
      });

      // ========================================
      // 2. 페이지 떠날 때: pending 등록
      // ========================================
      window.addEventListener('pagehide', function(event) {
          sessionStorage.setItem(RELOAD_KEY, Date.now().toString());

          // 빈 JSON 객체를 포함한 Blob을 생성하여 전송
          const blob = new Blob([JSON.stringify({})], { type: 'application/json' });
          navigator.sendBeacon('/admin/api/logout-pending', blob);
      });

      // ========================================
      // 3. 5분마다 세션 체크
      // ========================================
      function checkSession() {
          fetch('/admin/api/session-check', {
              method: 'GET',
              credentials: 'same-origin'
          })
          .then(function(response) {
              return response.json();
          })
          .then(function(data) {
              if (!data.valid) {
                  // 세션 만료
                  alert('세션이 만료되었습니다. 다시 로그인해주세요.');
                  window.location.href = '/admin/login';
                  return;
              }

              // 세션 만료 임박 경고 (5분 이하)
              if (data.remainingSeconds <= 300) {
                  showSessionWarning(data.remainingSeconds);
              }
          })
          .catch(function(err) {
              console.error('세션 체크 실패:', err);
          });
      }

      // 5분마다 세션 체크
      setInterval(checkSession, SESSION_CHECK_INTERVAL);

      // ========================================
      // 4. 세션 만료 경고 표시 (선택)
      // ========================================
      function showSessionWarning(remainingSeconds) {
          const minutes = Math.floor(remainingSeconds / 60);
          const seconds = remainingSeconds % 60;

          // 이미 경고창이 있으면 업데이트만
          let warningEl = document.getElementById('session-warning');

          if (!warningEl) {
              warningEl = document.createElement('div');
              warningEl.id = 'session-warning';
              warningEl.style.cssText = 'position:fixed;top:10px;right:10px;' +
                  'background:#ff6b6b;color:white;padding:15px 20px;' +
                  'border-radius:8px;z-index:9999;font-size:14px;';
              document.body.appendChild(warningEl);
          }

          warningEl.innerHTML = '세션 만료까지 ' + minutes + '분 ' + seconds + '초 남았습니다. ' +
              '<button onclick="extendSession()" style="margin-left:10px;padding:5px 10px;' +
              'cursor:pointer;">연장하기</button>';
      }

      // ========================================
      // 5. 세션 연장 (선택)
      // ========================================
      window.extendSession = function() {
          fetch('/admin/api/session-check', {
              method: 'GET',
              credentials: 'same-origin'
          })
          .then(function() {
              const warningEl = document.getElementById('session-warning');
              if (warningEl) {
                  warningEl.remove();
              }
              alert('세션이 연장되었습니다.');
          });
      };

  })();

  // 1. Initialize Icons
  lucide.createIcons();

  // 2. View Switching Logic
  function switchView(viewName, btn) {
    if (btn) {
      document.querySelectorAll('.nav-item').forEach(el => {
        el.classList.remove('bg-primary-50', 'text-primary-700', 'font-bold');
        el.classList.add('text-gray-600', 'hover:bg-gray-50', 'hover:text-gray-900');
      });
      btn.classList.remove('text-gray-600', 'hover:bg-gray-50', 'hover:text-gray-900');
      btn.classList.add('bg-primary-50', 'text-primary-700', 'font-bold');
    }

    document.querySelectorAll('.view-section').forEach(el => el.classList.add('hidden'));
    const target = document.getElementById('view-' + viewName);
    if(target) target.classList.remove('hidden');

    const titleMap = {
      'dashboard': 'Dashboard',
      'users': 'User Management',
      'usersLog': 'User Access Logs',
      'adminLog': 'Admin Activity Logs',
      'books': 'Product Management',
      'groups': 'Group Management',
      'notice': 'Notice Management',
      'notice-write': 'Create Notice',
      'banner': 'Banner Management',
      'safePayList' : 'Safe Payment List',
      'settlement': 'Settlement Management'
    };
    document.getElementById('page-title').innerText = titleMap[viewName] || 'Dashboard';

    // 탭별 데이터 로드
    if(viewName === 'users' && window.fetchUsers) fetchUsers();
    if(viewName === 'books' && window.fetchTrades) fetchTrades();
    if(viewName === 'safePayList' && window.searchPay) searchPay(1);
    if(viewName === 'groups' && window.fetchGroups) fetchGroups();
    if(viewName === 'banner' && window.loadBannerList) {
      window.loadBannerList(); // 배너 리스트 로드
      if(window.updatePreview) window.updatePreview(); // 미리보기 초기화
    }
    if(viewName === 'settlement' && window.loadSettlementData) loadSettlementData();
  }

  function closeAllMenus() {
    document.querySelectorAll('.action-menu').forEach(menu => {
      menu.classList.add('hidden');
    });
  }

  // 페이지 로드 시 초기화
  document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const tabName = urlParams.get('tab');

    if (tabName) {
        var selector = "button[onclick*='" + tabName +"']";
        var targetBtn = document.querySelector(selector);

        if (targetBtn) {
            switchView(tabName, targetBtn);
        }
    }
  });


</script>

</body>
</html>