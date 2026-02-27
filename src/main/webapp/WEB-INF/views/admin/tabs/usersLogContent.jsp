<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <script src="/resources/js/paging/paging.js"></script>

    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-gray-100 bg-gray-50/50">
        <h3 class="font-bold text-lg text-gray-900">회원 접속 기록</h3>
        <p class="text-xs text-gray-500 mt-1">최근 로그인 기록 및 활동 내역</p>
      </div>

      <!-- 검색 영역 -->
      <div class="px-6 py-5 bg-gray-50/50 border-b border-gray-100">
        <div class="flex items-center gap-3">
          <!-- 검색 타입 -->
          <select id="usersLogSearchType"
            class="px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition">
            <option value="all">전체</option>
            <option value="nickname">닉네임</option>
            <option value="ip">IP</option>
            <option value="accessTime">접속시간</option>
          </select>

          <!-- 검색 입력창 -->
          <div class="flex-1 relative">
            <input type="text" id="usersLogSearchKeyword" placeholder="검색어를 입력하세요..."
              class="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition pl-10"
              onkeypress="if(event.keyCode === 13) searchUsersLog()">
            <i data-lucide="search" class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
          </div>

          <!-- 버튼 그룹 -->
          <div class="flex gap-2">
            <button type="button" onclick="searchUsersLog()"
              class="px-5 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-all flex items-center gap-2 shadow-sm hover:shadow">
              <i data-lucide="search" class="w-4 h-4"></i>
              검색
            </button>
            <button type="button" onclick="usersLog_resetSearch()"
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
            <th class="px-6 py-4 text-left">회원</th>
            <th class="px-6 py-4 text-left">접속 IP</th>
            <th class="px-6 py-4 text-left">접속 시간</th>
            <th class="px-6 py-4 text-left">접속 종료시간</th>
          </tr>
        </thead>
        <tbody id="usersLogTableBody" class="divide-y divide-gray-50">
          <c:choose>
            <c:when test="${not empty userLogs}">
              <c:forEach var="log" items="${userLogs}">
                <tr class="hover:bg-gray-50/50 transition-colors">
                  <td class="px-6 py-4">
                    <p class="text-sm font-bold text-gray-900">${log.member_nicknm}</p>
                    <c:if test="${not empty log.member_email}">
                      <p class="text-xs text-gray-500 mt-0.5">${log.member_email}</p>
                    </c:if>
                  </td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${log.login_ip}</td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${log.formattedLoginDtm}</td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${log.formattedLogoutDtm}</td>
                </tr>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="4" class="px-6 py-12 text-center text-sm text-gray-400">접속 기록이 없습니다</td>
              </tr>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
      <div class="px-6 py-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-center">
        <div id="usersLogPaginationInfo" class="text-sm text-gray-500">
        </div>
        <div id="usersLogPaginationButtons" class="flex gap-1">
        </div>
      </div>
    </div>

    <script>
      function searchUsersLog(page) {
        const p = page || 1;
        const searchType = document.getElementById('usersLogSearchType').value;
        const keyword = document.getElementById('usersLogSearchKeyword').value;
        const url = '/admin/api/userLogs?page=' + p
          + '&size=10'
          + '&keyword=' + encodeURIComponent(keyword)
          + '&searchType=' + searchType;
        fetch(url)
          .then(function (response) {
            return response.json();
          })
          .then(function (data) {
            renderUsersLogTable(data.list);

            renderCommonPagination(
              'usersLogPaginationButtons',
              data.total,
              data.curPage,
              data.size,
              'searchUsersLog'
            );
          })
          .catch(function (error) {
            console.error('검색 중 오류 발생:', error);
          });
      }

      function renderUsersLogTable(userLogs) {
        const tbody = document.querySelector('#usersLogTableBody');
        tbody.innerHTML = ''; // 기존 내용 삭제

        if (!userLogs || userLogs.length === 0) {
          const tr = document.createElement('tr');
          const td = document.createElement('td');
          td.colSpan = 4;
          td.className = 'px-6 py-12 text-center text-gray-500';
          td.textContent = '검색 결과가 없습니다.';
          tr.appendChild(td);
          tbody.appendChild(tr);
          return;
        }

        userLogs.forEach(log => {
          const tr = document.createElement('tr');
          tr.className = 'hover:bg-gray-50/50 transition-colors';

          const tdUser = document.createElement('td');
          tdUser.className = 'px-6 py-4';

          const userP = document.createElement('p');
          userP.className = 'text-sm font-bold text-gray-900';
          userP.textContent = log.member_nicknm || '이름 없음';
          tdUser.appendChild(userP);

          const tdIP = document.createElement('td');
          tdIP.className = 'px-6 py-4 text-xs text-gray-500';
          tdIP.textContent = log.login_ip || '-';

          const tdLoginTime = document.createElement('td');
          tdLoginTime.className = 'px-6 py-4 text-xs text-gray-500 font-mono';
          tdLoginTime.textContent = log.formattedLoginDtm || (log.login_dtm ? String(log.login_dtm).replace('T', ' ') : '-');

          const tdLogoutTime = document.createElement('td');
          tdLogoutTime.className = 'px-6 py-4 text-xs text-gray-500 font-mono';
          tdLogoutTime.textContent = log.formattedLogoutDtm || (log.logout_dtm ? String(log.logout_dtm).replace('T', ' ') : '-');

          tr.appendChild(tdUser);
          tr.appendChild(tdIP);
          tr.appendChild(tdLoginTime);
          tr.appendChild(tdLogoutTime);

          tbody.appendChild(tr);
        });

        if (window.lucide) {
          lucide.createIcons();
        }
      }

      function usersLog_resetSearch() {
        document.getElementById('usersLogSearchKeyword').value = '';
        document.getElementById('usersLogSearchType').value = 'all';
        searchUsersLog(1);
      }

      // 페이지 로드 시 자동으로 첫 페이지 데이터와 페이징 버튼을 가져옵니다.
      document.addEventListener('DOMContentLoaded', function () {
        searchUsersLog(1);
      });
    </script>