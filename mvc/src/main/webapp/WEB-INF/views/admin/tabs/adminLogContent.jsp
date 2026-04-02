<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <script src="/resources/js/paging/paging.js"></script>

    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-gray-100 bg-gray-50/50">
        <h3 class="font-bold text-lg text-gray-900">관리자 활동 로그</h3>
        <p class="text-xs text-gray-500 mt-1">관리자 로그인 및 주요 활동 기록</p>
      </div>
      <!-- 검색 영역 -->
      <div class="px-6 py-5 bg-gray-50/50 border-b border-gray-100">
        <div class="flex items-center gap-3">
          <!-- 검색 타입 -->
          <select id="adminLogSearchType"
            class="px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition">
            <option value="all">전체</option>
            <option value="nickname">닉네임</option>
            <option value="ip">IP</option>
            <option value="accessTime">접속시간</option>
          </select>

          <!-- 검색 입력창 -->
          <div class="flex-1 relative">
            <input type="text" id="adminLogSearchKeyword" placeholder="검색어를 입력하세요..."
              class="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition pl-10"
              onkeypress="if(event.keyCode === 13) searchAdminLog()">
            <i data-lucide="search" class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
          </div>

          <!-- 버튼 그룹 -->
          <div class="flex gap-2">
            <button type="button" onclick="searchAdminLog()"
              class="px-5 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-all flex items-center gap-2 shadow-sm hover:shadow">
              <i data-lucide="search" class="w-4 h-4"></i>
              검색
            </button>
            <button type="button" onclick="adminLog_resetSearch()"
              class="px-5 py-2.5 bg-white border border-gray-300 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 transition-all flex items-center gap-2">
              <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
              초기화
            </button>
          </div>
        </div>
      </div>
      <table class="w-full">
        <thead
          class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">
          <tr>
            <th class="px-6 py-4 text-left">관리자</th>
            <th class="px-6 py-4 text-left">접속 IP</th>
            <th class="px-6 py-4 text-left">접속 시간</th>
            <th class="px-6 py-4 text-left">접속 종료시간</th>
          </tr>
        </thead>
        <tbody id="adminLogTableBody" class="divide-y divide-gray-50">
          <c:choose>
            <c:when test="${not empty adminLogs}">
              <c:forEach var="log" items="${adminLogs}">
                <tr class="hover:bg-gray-50/50 transition-colors">
                  <td class="px-6 py-4">
                    <p class="text-sm font-bold text-gray-900">${log.admin_login_id}</p>
                  </td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${log.login_ip}</td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${log.formattedLoginDtm}</td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${log.formattedLogoutDtm}</td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="4" class="px-6 py-12 text-center text-sm text-gray-400">활동 기록이 없습니다</td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
      <div class="px-6 py-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-center">
        <div id="adminLogPaginationInfo" class="text-sm text-gray-500">
        </div>
        <div id="adminLogPaginationButtons" class="flex gap-1">
        </div>
      </div>
    </div>

    <script>
      function searchAdminLog(page) {
        const p = page || 1;
        const searchType = document.getElementById('adminLogSearchType').value;
        const keyword = document.getElementById('adminLogSearchKeyword').value;
        const url = '/admin/api/adminLogs?page=' + p
          + '&size=10'
          + '&keyword=' + encodeURIComponent(keyword)
          + '&searchType=' + searchType;
        fetch(url)
          .then(function (response) {
            return response.json();
          })
          .then(function (data) {
            renderAdminLogTable(data.list);

            renderCommonPagination(
              'adminLogPaginationButtons',
              data.total,
              data.curPage,
              data.size,
              'searchAdminLog'
            );
          })
          .catch(function (error) {
            console.error('검색 중 오류 발생:', error);
          });
      }

      function renderAdminLogTable(adminLogs) {
        const tbody = document.querySelector('#adminLogTableBody');
        tbody.innerHTML = ''; // 기존 내용 삭제

        if (!adminLogs || adminLogs.length === 0) {
          const tr = document.createElement('tr');
          const td = document.createElement('td');
          td.colSpan = 4;
          td.className = 'px-6 py-12 text-center text-gray-500';
          td.textContent = '검색 결과가 없습니다.';
          tr.appendChild(td);
          tbody.appendChild(tr);
          return;
        }

        adminLogs.forEach(log => {
          const tr = document.createElement('tr');
          tr.className = 'hover:bg-gray-50/50 transition-colors';

          const tdAdmin = document.createElement('td');
          tdAdmin.className = 'px-6 py-4';

          const adminP = document.createElement('p');
          adminP.className = 'text-sm font-bold text-gray-900';
          adminP.textContent = log.admin_login_id || '이름 없음';
          tdAdmin.appendChild(adminP);

          const tdIP = document.createElement('td');
          tdIP.className = 'px-6 py-4 text-xs text-gray-500';
          tdIP.textContent = log.login_ip || '-';

          const tdLoginTime = document.createElement('td');
          tdLoginTime.className = 'px-6 py-4 text-xs text-gray-500 font-mono';
          tdLoginTime.textContent = log.formattedLoginDtm || (log.login_dtm ? String(log.login_dtm).replace('T', ' ') : '-');

          const tdLogoutTime = document.createElement('td');
          tdLogoutTime.className = 'px-6 py-4 text-xs text-gray-500 font-mono';
          tdLogoutTime.textContent = log.formattedLogoutDtm || (log.logout_dtm ? String(log.logout_dtm).replace('T', ' ') : '-');

          tr.appendChild(tdAdmin);
          tr.appendChild(tdIP);
          tr.appendChild(tdLoginTime);
          tr.appendChild(tdLogoutTime);

          tbody.appendChild(tr);
        });

        if (window.lucide) {
          lucide.createIcons();
        }
      }

      function adminLog_resetSearch() {
        document.getElementById('adminLogSearchKeyword').value = '';
        document.getElementById('adminLogSearchType').value = 'all';
        searchAdminLog(1);
      }

      // 페이지 로드 시 자동으로 첫 페이지 데이터와 페이징 버튼을 가져옵니다.
      document.addEventListener('DOMContentLoaded', function () {
        searchAdminLog(1);
      });
    </script>