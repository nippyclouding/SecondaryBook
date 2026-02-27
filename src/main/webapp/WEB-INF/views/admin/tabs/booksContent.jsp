<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
      <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
        <script src="/resources/js/paging/paging.js"></script>

        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
          <div class="p-6 border-b border-gray-100 bg-gray-50/50">
            <h3 class="font-bold text-lg text-gray-900">상품 관리</h3>
          </div>
          <!-- 검색 영역 -->
          <div class="px-6 py-5 bg-gray-50/50 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <!-- 검색 타입 -->
              <select id="bookSearchType"
                class="px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition">
                <option value="all">전체</option>
                <option value="sale_title">제목</option>
                <option value="bookName">상품명</option>
                <option value="region">지역</option>
              </select>

              <!-- 검색 입력창 -->
              <div class="flex-1 relative">
                <input type="text" id="bookSearchKeyword" placeholder="검색어를 입력하세요..."
                  class="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition pl-10"
                  onkeypress="if(event.keyCode === 13) searchBooks()">
                <i data-lucide="search" class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
              </div>

              <!-- 버튼 그룹 -->
              <div class="flex gap-2">
                <button onclick="searchBooks()"
                  class="px-5 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-all flex items-center gap-2 shadow-sm hover:shadow">
                  <i data-lucide="search" class="w-4 h-4"></i>
                  검색
                </button>
                <button onclick="books_resetSearch()"
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
                <th class="px-6 py-4 text-left">상품명</th>
                <th class="px-6 py-4 text-left">가격</th>
                <th class="px-6 py-4 text-left">지역</th>
                <th class="px-6 py-4 text-left">상태</th>
                <th class="px-6 py-4 text-left">등록일</th>
              </tr>
            </thead>
            <tbody id="tradeTableBody" class="divide-y divide-gray-50">
              <c:forEach var="t" items="${trades}">
                <tr class="hover:bg-gray-50/50 transition-colors">
                  <td class="px-6 py-4">
                    <p class="text-sm font-bold text-gray-900 w-64 truncate">${t.sale_title}</p>
                    <p class="text-[10px] text-gray-400">${t.book_title}</p>
                  </td>
                  <td class="px-6 py-4 text-sm font-black text-primary-600">
                    <fmt:formatNumber value="${t.sale_price}" pattern="#,###" />원
                  </td>
                  <td class="px-6 py-4 text-xs text-gray-500">${t.sale_rg}</td>
                  <td class="px-6 py-4"><span
                      class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold ${t.sale_st == 'SALE' ? 'bg-green-50 text-green-600' : 'bg-gray-100 text-gray-500'}">${t.sale_st}</span>
                  </td>
                  <td class="px-6 py-4 text-xs text-gray-500 font-mono">${fn:substring(t.crt_dtm, 0, 10)}</td>
                </tr>
              </c:forEach>
            </tbody>
          </table>

          <div class="px-6 py-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-center">
            <div id="bookPaginationButtons" class="flex gap-1"></div>
          </div>

        </div>

        <script>
          function searchBooks(page) {
            const p = page || 1;
            const searchType = document.getElementById('bookSearchType').value;
            const keyword = document.getElementById('bookSearchKeyword').value;

            const url = '/admin/api/trades?page=' + p
              + '&size=10'
              + '&keyword=' + encodeURIComponent(keyword)
              + '&searchType=' + searchType
              + '&status=all';
            fetch(url)
              .then(function (response) {
                return response.json();
              })
              .then(function (data) {
                renderBookTable(data.list);

                renderCommonPagination(
                  'bookPaginationButtons',
                  data.total,
                  data.curPage,
                  data.size,
                  'searchBooks'
                );
              })
              .catch(function (error) {
                console.error('검색 중 오류 발생', error);
              });
          }

          function renderBookTable(books) {
            const tbody = document.querySelector('#tradeTableBody');
            tbody.innerHTML = '';

            if (!books || books.length === 0) {
              const tr = document.createElement('tr');
              const td = document.createElement('td');
              td.colSpan = 5;
              td.className = 'px-6 py-12 text-center text-gray-500';
              td.textContent = '검색 결과가 없습니다.';
              tr.appendChild(td);
              tbody.appendChild(tr);
              return;
            }

            books.forEach(t => {
              const tr = document.createElement('tr');
              tr.className = 'hover:bg-gray-50/50 transition-colors cursor-pointer group';

              // 클릭 시 상세 페이지로 이동 (관리자 헤더 적용됨)
              tr.onclick = () => window.location.href = '/admin/trade/' + t.trade_seq;

              const tdTitle = document.createElement('td');
              tdTitle.className = 'px-6 py-4';

              const mainP = document.createElement('p');
              mainP.className = 'text-sm font-bold text-gray-900 w-64 truncate';
              mainP.textContent = t.sale_title;

              const subP = document.createElement('p');
              subP.className = 'text-[10px] text-gray-400';
              subP.textContent = t.book_title;

              tdTitle.appendChild(mainP);
              tdTitle.appendChild(subP);

              const tdPrice = document.createElement('td');
              tdPrice.className = 'px-6 py-4 text-sm font-black text-primary-600';

              const priceText = Number(t.sale_price).toLocaleString() + '원';
              tdPrice.textContent = priceText;

              const tdRegion = document.createElement('td');
              tdRegion.className = 'px-6 py-4 text-xs text-gray-500';
              tdRegion.textContent = t.sale_rg || '-';

              const tdStatus = document.createElement('td');
              tdStatus.className = 'px-6 py-4';
              const statusBadge = document.createElement('span');
              statusBadge.className = 'inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold ';
              if (t.sale_st === 'SALE') {
                statusBadge.className += 'bg-green-50 text-green-600';
              } else {
                statusBadge.className += 'bg-gray-100 text-gray-500';
              }
              statusBadge.textContent = t.sale_st;
              tdStatus.appendChild(statusBadge);

              const tdDate = document.createElement('td');
              tdDate.className = 'px-6 py-4 text-xs text-gray-500 font-mono';
              tdDate.textContent = t.crt_dtm ? String(t.crt_dtm) : '-';

              const tdAction = document.createElement('td');

              tr.appendChild(tdTitle);
              tr.appendChild(tdPrice);
              tr.appendChild(tdRegion);
              tr.appendChild(tdStatus);
              tr.appendChild(tdDate);

              tbody.appendChild(tr);
            });
            if (window.lucide) {
              lucide.createIcons();
            }
          }

          function books_resetSearch() {
            document.getElementById('bookSearchKeyword').value = '';
            document.getElementById('bookSearchType').value = 'all';
            searchBooks(1);
          }

          // 페이지 로드 시 자동으로 첫 페이지 데이터와 페이징 버튼을 가져옵니다.
          document.addEventListener('DOMContentLoaded', function () {
            searchBooks(1);
          });
        </script>