<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
      <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
        <script src="/resources/js/paging/paging.js"></script>

        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">

          <div class="p-6 border-b border-gray-100 bg-gray-50/50">
            <h3 class="font-bold text-lg text-gray-900">안전결제 내역</h3>
          </div>

          <div class="px-6 py-5 bg-gray-50/50 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <select id="paySearchType"
                class="px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition">
                <option value="all">전체</option>
                <option value="sale_title">제목</option>
                <option value="bookName">상품명</option>
                <option value="member_seller">판매자</option>
                <option value="member_buyer">구매자</option>
              </select>

              <div class="flex-1 relative">
                <input type="text" id="paySearchKeyword" placeholder="검색어를 입력하세요..."
                  class="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition pl-10"
                  onkeypress="if(event.keyCode === 13) searchPay(1)">
                <i data-lucide="search" class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2"></i>
              </div>

              <div class="flex gap-2">
                <button onclick="searchPay(1)"
                  class="px-5 py-2.5 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-all flex items-center gap-2 shadow-sm hover:shadow">
                  <i data-lucide="search" class="w-4 h-4"></i>
                  검색
                </button>
                <button onclick="pay_resetSearch()"
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
                <th class="px-6 py-4 text-left">상품 가격</th>
                <th class="px-6 py-4 text-left">배송비</th>
                <th class="px-6 py-4 text-left">수수료 (1%)</th>
                <th class="px-6 py-4 text-left">총 금액</th>
                <th class="px-6 py-4 text-left">판매자</th>
                <th class="px-6 py-4 text-left">구매자</th>
                <th class="px-6 py-4 text-left">구매확정일</th>
              </tr>
            </thead>
            <tbody id="payTableBody" class="divide-y divide-gray-50">
              <tr>
                <td colspan="8" class="px-6 py-12 text-center text-gray-500">데이터를 불러오는 중...</td>
              </tr>
            </tbody>
          </table>

          <div class="px-6 py-4 bg-gray-50/50 border-t border-gray-100 flex items-center justify-center">
            <div id="payPaginationButtons" class="flex gap-1"></div>
          </div>

        </div>

        <script>
          // Lucide 아이콘 초기화 (페이지 로드 시)
          if (typeof lucide !== 'undefined') {
            lucide.createIcons();
          }

          // 검색 및 페이징 함수
          function searchPay(page) {
            const p = page || 1;
            const searchType = document.getElementById('paySearchType').value;
            const keyword = document.getElementById('paySearchKeyword').value;

            const url = '/admin/api/safepaylist?page=' + p
              + '&size=10'
              + '&keyword=' + encodeURIComponent(keyword)
              + '&searchType=' + searchType
              + '&status=all';

            console.log("Request URL:", url); // 디버깅용 로그

            fetch(url)
              .then(function (response) {
                if (!response.ok) {
                  throw new Error('Network response was not ok');
                }
                return response.json();
              })
              .then(function (data) {
                console.log("Response Data:", data); // 디버깅용 로그

                // 테이블 렌더링
                renderPayTable(data.list);

                // 공통 페이징 함수 호출 (paging.js 필요)
                // data.total: 전체 개수, data.curPage: 현재 페이지, data.size: 페이지당 개수
                if (typeof renderCommonPagination === 'function') {
                  renderCommonPagination(
                    'payPaginationButtons', // 컨테이너 ID
                    data.total,             // 전체 데이터 수
                    data.curPage,           // 현재 페이지 번호
                    data.size,              // 페이지당 데이터 수
                    'searchPay'             // 클릭 시 호출할 함수 이름 (문자열)
                  );
                } else {
                  console.error('paging.js가 로드되지 않았거나 renderCommonPagination 함수가 없습니다.');
                }
              })
              .catch(function (error) {
                console.error('검색 중 오류 발생', error);
                const tbody = document.querySelector('#payTableBody');
                tbody.innerHTML = '<tr><td colspan="8" class="px-6 py-12 text-center text-red-500">데이터 로드 중 오류가 발생했습니다.</td></tr>';
              });
          }

          // 테이블 렌더링 함수
          function renderPayTable(list) {
            const tbody = document.querySelector('#payTableBody');
            tbody.innerHTML = '';

            if (!list || list.length === 0) {
              const tr = document.createElement('tr');
              tr.innerHTML = '<td colspan="8" class="px-6 py-12 text-center text-gray-500">검색 결과가 없습니다.</td>';
              tbody.appendChild(tr);
              return;
            }

            list.forEach(t => {
              const tr = document.createElement('tr');
              tr.className = 'hover:bg-gray-50/50 transition-colors';

              // 수수료 계산
              const price = Number(t.sale_price) || 0;
              const delivery = Number(t.delivery_cost) || 0;
              const fee = Math.floor((price + delivery) * 0.01);
              const total = (price + delivery) - fee;

              // 날짜 포맷팅
              let formattedDate = '-';
              if (t.sale_st_dtm) {
                if (Array.isArray(t.sale_st_dtm)) {
                  // 배열: [year, month, day, hour, minute, second]
                  const [year, month, day] = t.sale_st_dtm;
                  // 월은 0부터 시작하지 않음 (Java LocalDateTime 배열은 1부터 시작) -> 10보다 작으면 0 붙이기
                  const m = String(month).padStart(2, '0');
                  const d = String(day).padStart(2, '0');
                  formattedDate = year + '-' + m + '-' + d;
                } else if (typeof t.sale_st_dtm === 'string') {
                  formattedDate = t.sale_st_dtm.substring(0, 10);
                }
              }

              tr.innerHTML =
                '<td class="px-6 py-4">' +
                '<p class="text-sm font-bold text-gray-900 w-55 truncate">' + (t.sale_title || '-') + '</p>' +
                '<p class="text-[10px] text-gray-400">' + (t.book_title || '-') + '</p>' +
                '</td>' +
                '<td class="px-6 py-4 text-sm font-black">' + price.toLocaleString() + '원</td>' +
                '<td class="px-6 py-4 text-sm font-black">' + delivery.toLocaleString() + '원</td>' +
                '<td class="px-6 py-4 text-sm font-black">' + fee.toLocaleString() + '원</td>' +
                '<td class="px-6 py-4 text-sm font-black text-primary-600">' + total.toLocaleString() + '원</td>' +
                '<td class="px-6 py-4 text-xs text-gray-500">' + (t.member_seller_nm || '-') + '</td>' +
                '<td class="px-6 py-4 text-xs text-gray-500">' + (t.member_buyer_nm || '-') + '</td>' +
                '<td class="px-6 py-4 text-xs text-gray-500 font-mono">' + formattedDate + '</td>';

              tbody.appendChild(tr);
            });

            // 아이콘 리로드 (필요시)
            if (window.lucide) {
              lucide.createIcons();
            }
          }

          // 검색 초기화
          function pay_resetSearch() {
            document.getElementById('paySearchKeyword').value = '';
            document.getElementById('paySearchType').value = 'all';
            searchPay(1);
          }

          // 페이지 로드 시 초기 데이터 로드 (즉시 실행 함수 아님)
          // 동적 로드 환경(AJAX로 탭 로드 등)에서는 이 스크립트가 실행될 때 호출됨
          searchPay(1);

        </script>