<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="p-6 border-b border-gray-100 bg-gray-50/50">
          <h3 class="font-bold text-lg text-gray-900">모임 관리 (최근 생성순)</h3>
        </div>
        <!-- 검색 영역 -->
        <div class="px-6 py-5 bg-gray-50/50 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <!-- 검색 타입 -->
            <select id="groupSearchType"
              class="px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition">
              <option value="all">전체</option>
              <option value="groupName">모임명</option>
              <option value="region">지역</option>
            </select>

            <!-- 검색 입력창 -->
            <div class="flex-1 relative">
              <input type="text" id="groupSearchKeyword" placeholder="검색어를 입력하세요..."
                class="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition pl-10"
                onkeypress="if(event.keyCode === 13) searchGroups()">
              <i data-lucide="search" class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
            </div>

            <!-- 버튼 그룹 -->
            <div class="flex gap-2">
              <button onclick="searchGroups()"
                class="px-5 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-all flex items-center gap-2 shadow-sm hover:shadow">
                <i data-lucide="search" class="w-4 h-4"></i>
                검색
              </button>
              <button onclick="groups_resetSearch()"
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
              <th class="px-6 py-4 text-left">모임명</th>
              <th class="px-6 py-4 text-left">지역</th>
              <th class="px-6 py-4 text-left">정원</th>
              <th class="px-6 py-4 text-left">일정</th>
              <th class="px-6 py-4 text-left">생성일</th>
            </tr>
          </thead>
          <tbody id="groupTableBody" class="divide-y divide-gray-50">
            <c:forEach var="g" items="${clubs}">
              <tr class="hover:bg-gray-50/50 transition-colors">
                <td class="px-6 py-4">
                  <p class="text-sm font-bold text-gray-900">${g.book_club_name}</p>
                </td>
                <td class="px-6 py-4 text-xs text-gray-500">
                  <div class="flex items-center gap-1"><i data-lucide="map-pin" class="w-3 h-3"></i> ${g.book_club_rg}
                  </div>
                </td>
                <td class="px-6 py-4 text-xs font-bold text-primary-600">${g.book_club_max_member}명</td>
                <td class="px-6 py-4 text-xs text-gray-500">${g.book_club_schedule}</td>
                <td class="px-6 py-4 text-xs text-gray-500 font-mono">${fn:substring(g.crt_dtm, 0, 10)}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
        <div class="px-6 py-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-center">
          <div id="groupPaginationInfo" class="text-sm text-gray-500">
          </div>
          <div id="groupPaginationButtons" class="flex gap-1">
          </div>
        </div>
      </div>

      <script>
        function searchGroups(page) {
          const p = page || 1;
          const searchType = document.getElementById('groupSearchType').value;
          const keyword = document.getElementById('groupSearchKeyword').value;

          const url = '/admin/api/clubs?page=' + p
            + '&size=10'
            + '&keyword=' + encodeURIComponent(keyword)
            + '&searchType=' + searchType;
          fetch(url)
            .then(function (response) {
              return response.json();
            })
            .then(function (data) {
              renderGroupTable(data.list);

              renderCommonPagination(
                'groupPaginationButtons',
                data.total,
                data.curPage,
                data.size,
                'searchGroups'
              );
            })
            .catch(function (error) {
              console.error('검색 중 오류 발생', error);
            });
        }

        function renderGroupTable(groups) {
          const tbody = document.querySelector('#groupTableBody');
          tbody.innerHTML = '';

          if (!groups || groups.length === 0) {
            const tr = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = 5;
            td.className = 'px-6 py-12 text-center text-gray-500';
            td.textContent = '검색 결과가 없습니다.';
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
          }

          groups.forEach(g => {
            const tr = document.createElement('tr');
            tr.className = 'hover:bg-gray-50/50 transition-colors cursor-pointer';

            tr.onclick = function () {
              window.location.href = '/admin/bookclubs/' + g.book_club_seq;
            };

            const tdTitle = document.createElement('td');
            tdTitle.className = 'px-6 py-4';
            const titleP = document.createElement('p');
            titleP.className = 'text-sm font-bold text-gray-900';
            titleP.textContent = g.book_club_name || '이름 없음';
            tdTitle.appendChild(titleP);

            const tdRegion = document.createElement('td');
            tdRegion.className = 'px-6 py-4 text-xs text-gray-500';
            const regionDiv = document.createElement('div');
            regionDiv.className = 'flex items-center gap-1';

            const pinIcon = document.createElement('i');
            pinIcon.setAttribute('data-lucide', 'map-pin');
            pinIcon.className = 'w-3 h-3';

            regionDiv.appendChild(pinIcon);
            regionDiv.appendChild(document.createTextNode(' ' + (g.book_club_rg || '-')));
            tdRegion.appendChild(regionDiv);

            const tdMax = document.createElement('td');
            tdMax.className = 'px-6 py-4 text-xs font-bold text-primary-600';
            tdMax.textContent = (g.book_club_max_member || 0) + '명';

            const tdSchedule = document.createElement('td');
            tdSchedule.className = 'px-6 py-4 text-xs text-gray-500';
            tdSchedule.textContent = g.book_club_schedule || '-';

            const tdDate = document.createElement('td');
            tdDate.className = 'px-6 py-4 text-xs text-gray-500 font-mono';
            tdDate.textContent = g.crt_dtm ? String(g.crt_dtm) : '-';

            tr.appendChild(tdTitle);
            tr.appendChild(tdRegion);
            tr.appendChild(tdMax);
            tr.appendChild(tdSchedule);
            tr.appendChild(tdDate);

            tbody.appendChild(tr);
          });
          if (window.lucide) {
            lucide.createIcons();
          }
        }

        function groups_resetSearch() {
          document.getElementById('groupSearchKeyword').value = '';
          document.getElementById('groupSearchType').value = 'all';
          searchGroups(1);
        }

        // 페이지 로드 시 자동으로 첫 페이지 데이터와 페이징 버튼을 가져옵니다.
        document.addEventListener('DOMContentLoaded', function () {
          searchGroups(1);
        });
      </script>