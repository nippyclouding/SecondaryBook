<%--<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>--%>
<%--<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>--%>
<%--<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>--%>

<%--<jsp:include page="../common/header.jsp" />--%>

<%--<script src="https://unpkg.com/lucide@latest"></script>--%>

<%--<div class="max-w-7xl mx-auto space-y-8 pb-20">--%>
<%--  <div class="flex justify-between items-end">--%>
<%--    <div>--%>
<%--      <h1 class="text-3xl font-black text-gray-900 tracking-tight">Admin Console</h1>--%>
<%--      <p class="text-gray-500 mt-1">플랫폼 통합 운영 및 안전 관리 대시보드</p>--%>
<%--    </div>--%>
<%--    <div class="flex gap-4">--%>
<%--      <div class="bg-white px-5 py-3 rounded-xl border border-gray-200 shadow-sm text-center">--%>
<%--        <p class="text-[10px] font-bold text-gray-400 mb-0.5 uppercase">Active Books</p>--%>
<%--        <p class="text-xl font-black text-primary-600">128</p>--%>
<%--      </div>--%>
<%--      <div class="bg-white px-5 py-3 rounded-xl border border-gray-200 shadow-sm text-center">--%>
<%--        <p class="text-[10px] font-bold text-gray-400 mb-0.5 uppercase">Groups</p>--%>
<%--        <p class="text-xl font-black text-emerald-500">42</p>--%>
<%--      </div>--%>
<%--    </div>--%>
<%--  </div>--%>

<%--  <div class="flex flex-wrap gap-2 p-1 bg-gray-100 rounded-2xl w-fit">--%>
<%--    <button onclick="switchTab('users', this)" class="tab-btn flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-bold transition-all bg-white text-primary-600 shadow-sm" data-target="tab-users">--%>
<%--      <i data-lucide="users" class="w-4 h-4"></i> 유저--%>
<%--    </button>--%>
<%--    <button onclick="switchTab('books', this)" class="tab-btn flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-bold transition-all text-gray-500 hover:text-gray-700" data-target="tab-books">--%>
<%--      <i data-lucide="shopping-bag" class="w-4 h-4"></i> 상품--%>
<%--    </button>--%>
<%--    <button onclick="switchTab('groups', this)" class="tab-btn flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-bold transition-all text-gray-500 hover:text-gray-700" data-target="tab-groups">--%>
<%--      <i data-lucide="book-open" class="w-4 h-4"></i> 모임--%>
<%--    </button>--%>
<%--    <button onclick="switchTab('notices', this)" class="tab-btn flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-bold transition-all text-gray-500 hover:text-gray-700" data-target="tab-notices">--%>
<%--      <i data-lucide="megaphone" class="w-4 h-4"></i> 공지--%>
<%--    </button>--%>
<%--    <button onclick="switchTab('logs', this)" class="tab-btn flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-bold transition-all text-gray-500 hover:text-gray-700" data-target="tab-logs">--%>
<%--      <i data-lucide="history" class="w-4 h-4"></i> 이력--%>
<%--    </button>--%>
<%--  </div>--%>

<%--  <div class="bg-white rounded-3xl border border-gray-200 shadow-xl overflow-hidden min-h-[600px]">--%>

<%--    <div id="search-container" class="p-8 border-b border-gray-50 bg-white sticky top-0 z-10">--%>
<%--      <div class="flex justify-between items-center">--%>
<%--        <div class="relative w-96">--%>
<%--          <i data-lucide="search" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"></i>--%>
<%--          <input type="text" id="admin-search-input"--%>
<%--                 placeholder="닉네임, 이메일 검색..."--%>
<%--                 class="w-full pl-11 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary-100 outline-none text-sm"--%>
<%--                 onkeyup="filterTable()">--%>
<%--        </div>--%>
<%--        <span class="text-xs text-gray-400 font-bold">Total: <span id="item-count">0</span> items</span>--%>
<%--      </div>--%>
<%--    </div>--%>

<%--    <div id="tab-users" class="tab-content block">--%>
<%--      <table class="w-full">--%>
<%--        <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">--%>
<%--        <tr>--%>
<%--          <th class="px-8 py-4 text-left">회원 정보</th>--%>
<%--          <th class="px-8 py-4 text-left">상태</th>--%>
<%--          <th class="px-8 py-4 text-left">권한</th>--%>
<%--          <th class="px-8 py-4 text-left">가입일</th>--%>
<%--          <th class="px-8 py-4 text-right">관리</th>--%>
<%--        </tr>--%>
<%--        </thead>--%>
<%--        <tbody class="divide-y divide-gray-50 table-body">--%>
<%--        <tr class="hover:bg-gray-50/50 transition-colors">--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex items-center gap-3">--%>
<%--              <div class="w-10 h-10 rounded-full bg-primary-50 flex items-center justify-center font-bold text-primary-600 border border-primary-100">K</div>--%>
<%--              <div>--%>
<%--                <p class="text-sm font-black text-gray-900 searchable-text">kim_dev</p>--%>
<%--                <p class="text-xs text-gray-400 searchable-text">dev@test.com</p>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5">--%>
<%--                            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-black border bg-emerald-50 text-emerald-600 border-emerald-100">--%>
<%--                                <div class="w-1 h-1 rounded-full bg-emerald-500"></div> 정상--%>
<%--                            </span>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <button class="text-[10px] font-bold px-2 py-1 rounded border transition-colors bg-white text-gray-400 border-gray-200 hover:border-primary-500">USER</button>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5 text-xs text-gray-500 font-medium">2024.03.15</td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex justify-end gap-2">--%>
<%--              <button class="p-2 rounded-lg transition text-orange-500 hover:bg-orange-50" title="정지"><i data-lucide="shield-alert" class="w-4 h-4"></i></button>--%>
<%--              <button class="p-2 text-red-500 hover:bg-red-50 rounded-lg transition" title="삭제"><i data-lucide="trash-2" class="w-4 h-4"></i></button>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--        </tr>--%>
<%--        <tr class="hover:bg-gray-50/50 transition-colors">--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex items-center gap-3">--%>
<%--              <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center font-bold text-red-600 border border-red-100">B</div>--%>
<%--              <div>--%>
<%--                <p class="text-sm font-black text-gray-900 searchable-text">bad_user</p>--%>
<%--                <p class="text-xs text-gray-400 searchable-text">bad@spam.com</p>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5">--%>
<%--                            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-black border bg-red-50 text-red-600 border-red-100">--%>
<%--                                <div class="w-1 h-1 rounded-full bg-red-500"></div> 정지됨--%>
<%--                            </span>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <button class="text-[10px] font-bold px-2 py-1 rounded border transition-colors bg-white text-gray-400 border-gray-200 hover:border-primary-500">USER</button>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5 text-xs text-gray-500 font-medium">2024.01.10</td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex justify-end gap-2">--%>
<%--              <button class="p-2 rounded-lg transition text-emerald-500 hover:bg-emerald-50" title="해제"><i data-lucide="shield-check" class="w-4 h-4"></i></button>--%>
<%--              <button class="p-2 text-red-500 hover:bg-red-50 rounded-lg transition" title="삭제"><i data-lucide="trash-2" class="w-4 h-4"></i></button>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--        </tr>--%>
<%--        </tbody>--%>
<%--      </table>--%>
<%--    </div>--%>

<%--    <div id="tab-books" class="tab-content hidden">--%>
<%--      <table class="w-full">--%>
<%--        <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">--%>
<%--        <tr>--%>
<%--          <th class="px-8 py-4 text-left">상품 정보</th>--%>
<%--          <th class="px-8 py-4 text-left">판매자</th>--%>
<%--          <th class="px-8 py-4 text-left">가격</th>--%>
<%--          <th class="px-8 py-4 text-left">상태</th>--%>
<%--          <th class="px-8 py-4 text-right">관리</th>--%>
<%--        </tr>--%>
<%--        </thead>--%>
<%--        <tbody class="divide-y divide-gray-50 table-body">--%>
<%--        <tr class="hover:bg-gray-50/50 transition-colors">--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex items-center gap-4">--%>
<%--              <div class="w-10 h-14 bg-gray-200 rounded shadow-sm flex items-center justify-center text-xs text-gray-400">IMG</div>--%>
<%--              <div class="min-w-0">--%>
<%--                <p class="text-sm font-black text-gray-900 truncate max-w-[200px] searchable-text">클린 코드 (Clean Code)</p>--%>
<%--                <p class="text-xs text-gray-400">IT/개발</p>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5 text-sm font-bold text-gray-700 searchable-text">dev_master</td>--%>
<%--          <td class="px-8 py-5 text-sm font-black text-primary-600">22,000원</td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="relative group">--%>
<%--                                <span class="inline-flex items-center px-3 py-1 rounded-full text-[10px] font-black border bg-green-50 text-green-600 border-green-100 cursor-pointer">--%>
<%--                                    판매중--%>
<%--                                </span>--%>
<%--              <div class="hidden group-hover:block absolute top-full left-0 mt-1 bg-white border border-gray-100 shadow-xl rounded-lg py-1 z-20 w-32 animate-[fadeIn_0.1s_ease-out]">--%>
<%--                <button class="w-full text-left px-4 py-2 text-xs hover:bg-green-50 text-green-600 font-bold whitespace-nowrap">판매중</button>--%>
<%--                <button class="w-full text-left px-4 py-2 text-xs hover:bg-orange-50 text-orange-600 font-bold whitespace-nowrap">예약중</button>--%>
<%--                <button class="w-full text-left px-4 py-2 text-xs hover:bg-gray-50 text-gray-600 font-bold whitespace-nowrap">판매완료</button>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5 text-right">--%>
<%--            <button onclick="confirm('이 게시글을 삭제하시겠습니까?')" class="p-2 text-red-500 hover:bg-red-50 rounded-lg transition">--%>
<%--              <i data-lucide="trash-2" class="w-4 h-4"></i>--%>
<%--            </button>--%>
<%--          </td>--%>
<%--        </tr>--%>
<%--        </tbody>--%>
<%--      </table>--%>
<%--    </div>--%>

<%--    <div id="tab-groups" class="tab-content hidden">--%>
<%--      <table class="w-full">--%>
<%--        <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">--%>
<%--        <tr>--%>
<%--          <th class="px-8 py-4 text-left">모임 정보</th>--%>
<%--          <th class="px-8 py-4 text-left">모임장</th>--%>
<%--          <th class="px-8 py-4 text-left">지역</th>--%>
<%--          <th class="px-8 py-4 text-left">참여 인원</th>--%>
<%--          <th class="px-8 py-4 text-right">관리</th>--%>
<%--        </tr>--%>
<%--        </thead>--%>
<%--        <tbody class="divide-y divide-gray-50 table-body">--%>
<%--        <tr class="hover:bg-gray-50/50 transition-colors">--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex items-center gap-4">--%>
<%--              <div class="w-12 h-12 bg-gray-200 rounded-xl shadow-sm"></div>--%>
<%--              <div>--%>
<%--                <p class="text-sm font-black text-gray-900 searchable-text">주말 아침 독서 모임</p>--%>
<%--                <div class="flex gap-1 mt-1">--%>
<%--                  <span class="text-[9px] bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded">#자기계발</span>--%>
<%--                </div>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5 text-sm font-bold text-gray-700 searchable-text">morning_reader</td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex items-center gap-1.5 text-xs text-gray-500">--%>
<%--              <i data-lucide="map-pin" class="w-3 h-3"></i> 서울 강남--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5">--%>
<%--            <div class="flex items-center gap-2">--%>
<%--              <div class="w-16 h-1.5 bg-gray-100 rounded-full overflow-hidden">--%>
<%--                <div class="h-full bg-primary-500" style="width: 75%"></div>--%>
<%--              </div>--%>
<%--              <span class="text-xs font-bold text-gray-500">6/8</span>--%>
<%--            </div>--%>
<%--          </td>--%>
<%--          <td class="px-8 py-5 text-right">--%>
<%--            <button onclick="confirm('모임을 해산하시겠습니까?')" class="p-2 text-red-500 hover:bg-red-50 rounded-lg transition">--%>
<%--              <i data-lucide="trash-2" class="w-4 h-4"></i>--%>
<%--            </button>--%>
<%--          </td>--%>
<%--        </tr>--%>
<%--        </tbody>--%>
<%--      </table>--%>
<%--    </div>--%>

<%--    <div id="tab-notices" class="tab-content hidden">--%>
<%--      <div class="p-8">--%>
<%--        <div class="flex justify-between items-center mb-8">--%>
<%--          <h2 class="text-xl font-black text-gray-900">플랫폼 공지사항</h2>--%>
<%--          <button onclick="toggleNoticeModal(true)" class="flex items-center gap-2 px-6 py-3 bg-primary-500 text-white rounded-xl text-sm font-bold hover:bg-primary-600 shadow-lg shadow-primary-100 transition">--%>
<%--            <i data-lucide="plus" class="w-4 h-4"></i> 신규 공지 등록--%>
<%--          </button>--%>
<%--        </div>--%>
<%--        <div class="grid grid-cols-1 gap-4">--%>
<%--          <div class="p-6 bg-gray-50 rounded-2xl border border-gray-100 flex justify-between items-center group">--%>
<%--            <div class="flex gap-4">--%>
<%--              <div class="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 bg-red-500 text-white">--%>
<%--                <i data-lucide="megaphone" class="w-5 h-5"></i>--%>
<%--              </div>--%>
<%--              <div>--%>
<%--                <div class="flex items-center gap-2 mb-1">--%>
<%--                  <span class="text-[10px] font-black text-red-600 bg-red-50 px-1.5 py-0.5 rounded">URGENT</span>--%>
<%--                  <h3 class="font-bold text-gray-900">서버 점검 안내 (1/25)</h3>--%>
<%--                </div>--%>
<%--                <p class="text-xs text-gray-400 font-medium">관리자 · 2024.01.20 14:00</p>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--            <button class="p-3 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl opacity-0 group-hover:opacity-100 transition">--%>
<%--              <i data-lucide="trash-2" class="w-5 h-5"></i>--%>
<%--            </button>--%>
<%--          </div>--%>
<%--          <div class="p-6 bg-gray-50 rounded-2xl border border-gray-100 flex justify-between items-center group">--%>
<%--            <div class="flex gap-4">--%>
<%--              <div class="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 bg-primary-50 text-primary-600">--%>
<%--                <i data-lucide="megaphone" class="w-5 h-5"></i>--%>
<%--              </div>--%>
<%--              <div>--%>
<%--                <div class="flex items-center gap-2 mb-1">--%>
<%--                  <h3 class="font-bold text-gray-900">2월 독서 모임 챌린지 오픈</h3>--%>
<%--                </div>--%>
<%--                <p class="text-xs text-gray-400 font-medium">운영팀 · 2024.01.18 09:30</p>--%>
<%--              </div>--%>
<%--            </div>--%>
<%--            <button class="p-3 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl opacity-0 group-hover:opacity-100 transition">--%>
<%--              <i data-lucide="trash-2" class="w-5 h-5"></i>--%>
<%--            </button>--%>
<%--          </div>--%>
<%--        </div>--%>
<%--      </div>--%>
<%--    </div>--%>

<%--    <div id="tab-logs" class="tab-content hidden">--%>
<%--      <div class="p-8">--%>
<%--        <h2 class="text-xl font-black text-gray-900 mb-8">실시간 접속 로그</h2>--%>
<%--        <table class="w-full">--%>
<%--          <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">--%>
<%--          <tr>--%>
<%--            <th class="px-6 py-4 text-left">사용자</th>--%>
<%--            <th class="px-6 py-4 text-left">일시</th>--%>
<%--            <th class="px-6 py-4 text-left">IP ADDRESS</th>--%>
<%--          </tr>--%>
<%--          </thead>--%>
<%--          <tbody class="divide-y divide-gray-50">--%>
<%--          <tr>--%>
<%--            <td class="px-6 py-4">--%>
<%--              <span class="text-sm font-bold text-gray-900">kim_dev</span>--%>
<%--              <span class="text-[10px] text-gray-400 ml-2">(김개발)</span>--%>
<%--            </td>--%>
<%--            <td class="px-6 py-4 text-xs text-gray-500">2024.01.20 15:30:22</td>--%>
<%--            <td class="px-6 py-4">--%>
<%--              <code class="text-[11px] font-mono bg-gray-100 px-2 py-1 rounded text-gray-600">192.168.0.1</code>--%>
<%--            </td>--%>
<%--          </tr>--%>
<%--          </tbody>--%>
<%--        </table>--%>
<%--      </div>--%>
<%--    </div>--%>

<%--  </div>--%>
<%--</div>--%>

<%--<div id="notice-modal" class="hidden fixed inset-0 z-[100] bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">--%>
<%--  <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl overflow-hidden animate-[zoomIn_0.2s_ease-out]">--%>
<%--    <div class="p-8 border-b border-gray-100">--%>
<%--      <h3 class="text-2xl font-black text-gray-900">신규 공지 작성</h3>--%>
<%--    </div>--%>
<%--    <form action="/admin/notice/create" method="post" class="p-8 space-y-6">--%>
<%--      <div>--%>
<%--        <label class="block text-sm font-bold text-gray-700 mb-2">공지 제목</label>--%>
<%--        <input type="text" name="title" required class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-100" />--%>
<%--      </div>--%>
<%--      <div>--%>
<%--        <label class="block text-sm font-bold text-gray-700 mb-2">내용</label>--%>
<%--        <textarea name="content" required class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-100 min-h-[150px]"></textarea>--%>
<%--      </div>--%>
<%--      <label class="flex items-center gap-3 cursor-pointer">--%>
<%--        <input type="checkbox" name="isImportant" class="w-5 h-5 rounded-md border-gray-300 text-primary-500" />--%>
<%--        <span class="text-sm font-bold text-red-600">중요 공지 (상단 노출)</span>--%>
<%--      </label>--%>
<%--      <div class="flex gap-3">--%>
<%--        <button type="button" onclick="toggleNoticeModal(false)" class="flex-1 py-4 bg-gray-100 text-gray-500 rounded-2xl font-bold hover:bg-gray-200 transition">취소</button>--%>
<%--        <button type="submit" class="flex-[2] py-4 bg-primary-500 text-white rounded-2xl font-bold hover:bg-primary-600 transition">등록</button>--%>
<%--      </div>--%>
<%--    </form>--%>
<%--  </div>--%>
<%--</div>--%>

<%--<script>--%>
<%--  // 1. Initialize Icons--%>
<%--  lucide.createIcons();--%>

<%--  // 2. Tab Switching Logic--%>
<%--  function switchTab(tabName, btn) {--%>
<%--    // Update Buttons--%>
<%--    $('.tab-btn').removeClass('bg-white text-primary-600 shadow-sm').addClass('text-gray-500 hover:text-gray-700');--%>
<%--    $(btn).removeClass('text-gray-500 hover:text-gray-700').addClass('bg-white text-primary-600 shadow-sm');--%>

<%--    // Update Content--%>
<%--    $('.tab-content').addClass('hidden').removeClass('block');--%>
<%--    $('#tab-' + tabName).removeClass('hidden').addClass('block');--%>

<%--    // Update Search Bar--%>
<%--    const searchContainer = $('#search-container');--%>
<%--    const searchInput = $('#admin-search-input');--%>

<%--    if (tabName === 'notices' || tabName === 'logs') {--%>
<%--      searchContainer.addClass('hidden');--%>
<%--    } else {--%>
<%--      searchContainer.removeClass('hidden');--%>
<%--      let placeholder = '';--%>
<%--      if (tabName === 'users') placeholder = '닉네임, 이메일 검색...';--%>
<%--      else if (tabName === 'books') placeholder = '상품명, 판매자 검색...';--%>
<%--      else if (tabName === 'groups') placeholder = '모임명, 모임장 검색...';--%>
<%--      searchInput.attr('placeholder', placeholder);--%>
<%--      searchInput.val(''); // Clear search on tab switch--%>
<%--      filterTable(); // Reset table filter--%>
<%--    }--%>

<%--    updateItemCount(tabName);--%>
<%--  }--%>

<%--  // 3. Modal Logic--%>
<%--  function toggleNoticeModal(show) {--%>
<%--    if (show) {--%>
<%--      $('#notice-modal').removeClass('hidden');--%>
<%--    } else {--%>
<%--      $('#notice-modal').addClass('hidden');--%>
<%--    }--%>
<%--  }--%>

<%--  // 4. Simple Filtering Logic (Front-end Only)--%>
<%--  function filterTable() {--%>
<%--    const query = $('#admin-search-input').val().toLowerCase();--%>
<%--    // 현재 활성화된 탭의 visible한 tbody 안의 tr만 찾음--%>
<%--    const rows = $('.tab-content.block tbody tr');--%>

<%--    rows.each(function() {--%>
<%--      const row = $(this);--%>
<%--      // .searchable-text 클래스가 있는 요소들의 텍스트를 검색--%>
<%--      const text = row.find('.searchable-text').text().toLowerCase();--%>

<%--      if (text.includes(query)) {--%>
<%--        row.removeClass('hidden');--%>
<%--      } else {--%>
<%--        row.addClass('hidden');--%>
<%--      }--%>
<%--    });--%>

<%--    // Update Count based on visible rows--%>
<%--    $('#item-count').text(rows.not('.hidden').length);--%>
<%--  }--%>

<%--  // 5. Initial Item Count--%>
<%--  function updateItemCount(tabName) {--%>
<%--    if(tabName === 'notices' || tabName === 'logs') return;--%>
<%--    const count = $('#tab-' + tabName + ' tbody tr').length;--%>
<%--    $('#item-count').text(count);--%>
<%--  }--%>

<%--  // 초기화--%>
<%--  $(document).ready(function() {--%>
<%--    updateItemCount('users');--%>
<%--  });--%>
<%--</script>--%>

<%--<jsp:include page="../common/footer.jsp" />--%>