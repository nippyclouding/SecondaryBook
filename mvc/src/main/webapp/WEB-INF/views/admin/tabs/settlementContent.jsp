<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="space-y-6">

  <%-- ===== 요약 카드 ===== --%>
  <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">관리자 잔액</p>
      <p id="stl-admin-balance" class="text-2xl font-black text-gray-900 mb-3">-</p>
      <a href="${pageContext.request.contextPath}/admin/balance/charge"
         class="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition">
        + 잔액 충전
      </a>
    </div>
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">정산 신청 대기</p>
      <p id="stl-requested-count" class="text-2xl font-black text-primary-600">-</p>
      <p class="text-xs text-gray-400 mt-1">이체 후 완료 처리 필요</p>
    </div>
    <div class="bg-white rounded-2xl border border-green-100 shadow-sm p-6 bg-green-50/30">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">정산 완료</p>
      <p id="stl-completed-count" class="text-2xl font-black text-green-600">-</p>
      <p class="text-xs text-gray-400 mt-1">처리 완료</p>
    </div>
  </div>

  <%-- ===== 정산 신청 목록 (REQUESTED) ===== --%>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
    <div class="p-6 border-b border-gray-100 bg-gray-50/50 flex items-center justify-between">
      <div>
        <h3 class="font-bold text-lg text-gray-900">정산 신청 목록</h3>
        <p class="text-xs text-gray-500 mt-0.5">판매자가 정산 신청 완료 — 계좌 이체 후 '정산 완료 처리' 버튼을 클릭하세요</p>
      </div>
      <button onclick="loadSettlementData()" class="px-4 py-2 text-sm bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition flex items-center gap-2">
        <i data-lucide="refresh-cw" class="w-4 h-4"></i> 새로고침
      </button>
    </div>
    <div class="overflow-x-auto">
      <table class="w-full min-w-max">
        <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">
          <tr>
            <th class="px-5 py-4 text-left whitespace-nowrap">상품명</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">판매자</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">은행</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">계좌번호</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">예금주</th>
            <th class="px-5 py-4 text-right whitespace-nowrap">판매가</th>
            <th class="px-5 py-4 text-right whitespace-nowrap">배송비</th>
            <th class="px-5 py-4 text-right whitespace-nowrap">수수료(1%)</th>
            <th class="px-5 py-4 text-right whitespace-nowrap">정산금액</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">신청일</th>
            <th class="px-5 py-4 text-center whitespace-nowrap">처리</th>
          </tr>
        </thead>
        <tbody id="stl-requested-tbody" class="divide-y divide-gray-50">
          <tr>
            <td colspan="11" class="px-6 py-12 text-center text-gray-400 text-sm">데이터를 불러오는 중...</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <%-- ===== 정산 완료 내역 (COMPLETED 전체) ===== --%>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
    <div class="p-6 border-b border-gray-100 bg-gray-50/50">
      <h3 class="font-bold text-lg text-gray-900">정산 완료 내역</h3>
      <p class="text-xs text-gray-500 mt-0.5">정산 완료 처리된 전체 내역</p>
    </div>
    <div class="overflow-x-auto">
      <table class="w-full min-w-max">
        <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">
          <tr>
            <th class="px-5 py-4 text-left whitespace-nowrap">상품명</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">판매자</th>
            <th class="px-5 py-4 text-right whitespace-nowrap">정산금액</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">신청일</th>
            <th class="px-5 py-4 text-left whitespace-nowrap">정산 완료일</th>
          </tr>
        </thead>
        <tbody id="stl-completed-tbody" class="divide-y divide-gray-50">
          <tr>
            <td colspan="5" class="px-6 py-12 text-center text-gray-400 text-sm">데이터를 불러오는 중...</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

</div>

<script>
  if (typeof lucide !== 'undefined') lucide.createIcons();

  const BANK_NAMES = {
    '003': 'IBK기업', '004': 'KB국민', '011': 'NH농협', '020': '우리',
    '023': 'SC제일', '032': '부산', '045': '새마을금고', '064': '산림조합',
    '081': '하나', '088': '신한', '089': '케이뱅크', '090': '카카오뱅크',
    '092': '토스뱅크'
  };

  function fmtMoney(n) {
    if (n == null) return '-';
    return Number(n).toLocaleString('ko-KR') + '원';
  }
  function fmtDate(s) {
    if (!s) return '-';
    return s.substring(0, 10);
  }

  function loadSettlementData() {
    loadSummary();
    loadRequested();
    loadCompleted();
  }

  function loadSummary() {
    adminFetch('/admin/api/settlement/requested')
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          document.getElementById('stl-requested-count').textContent = (data.count || 0) + '건';
          document.getElementById('stl-admin-balance').textContent = fmtMoney(data.adminBalance);
        }
      }).catch(() => {});

    adminFetch('/admin/api/settlement/completed')
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          document.getElementById('stl-completed-count').textContent = (data.count || 0) + '건';
        }
      }).catch(() => {});
  }

  function loadRequested() {
    const tbody = document.getElementById('stl-requested-tbody');
    adminFetch('/admin/api/settlement/requested')
      .then(r => r.json())
      .then(data => {
        if (!data.success || !data.list || data.list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="11" class="px-6 py-12 text-center text-gray-400 text-sm">정산 신청 건이 없습니다.</td></tr>';
          return;
        }
        tbody.innerHTML = data.list.map(item => {
          const bankName = BANK_NAMES[item.bank_code] || item.bank_code || '-';
          const accountNo = item.bank_account_no || '-';
          const holderNm = item.account_holder_nm || '-';
          return `
            <tr class="hover:bg-gray-50 transition">
              <td class="px-5 py-4 text-sm text-gray-800 font-medium max-w-[160px] truncate" title="\${item.sale_title || ''}">\${item.sale_title || '-'}</td>
              <td class="px-5 py-4 text-sm text-gray-600 whitespace-nowrap">\${item.member_seller_nm || '-'}</td>
              <td class="px-5 py-4 text-sm text-gray-600 whitespace-nowrap">\${bankName}</td>
              <td class="px-5 py-4 text-sm font-mono text-gray-800 whitespace-nowrap">\${accountNo}</td>
              <td class="px-5 py-4 text-sm text-gray-600 whitespace-nowrap">\${holderNm}</td>
              <td class="px-5 py-4 text-sm text-right text-gray-700 whitespace-nowrap">\${fmtMoney(item.sale_price)}</td>
              <td class="px-5 py-4 text-sm text-right text-gray-700 whitespace-nowrap">\${fmtMoney(item.delivery_cost)}</td>
              <td class="px-5 py-4 text-sm text-right text-red-500 whitespace-nowrap">-\${fmtMoney(item.commission)}</td>
              <td class="px-5 py-4 text-sm font-bold text-right text-primary-700 whitespace-nowrap">\${fmtMoney(item.settlement_amount)}</td>
              <td class="px-5 py-4 text-sm text-gray-500 whitespace-nowrap">\${fmtDate(item.request_dtm)}</td>
              <td class="px-5 py-4 text-center whitespace-nowrap">
                <button onclick="confirmTransfer(\${item.settlement_seq}, this)"
                  class="px-3 py-1.5 text-xs font-bold bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition whitespace-nowrap">
                  정산 완료 처리
                </button>
              </td>
            </tr>`;
        }).join('');
        if (typeof lucide !== 'undefined') lucide.createIcons();
      }).catch(() => {
        tbody.innerHTML = '<tr><td colspan="11" class="px-6 py-12 text-center text-red-400 text-sm">데이터 로드 실패</td></tr>';
      });
  }

  function loadCompleted() {
    const tbody = document.getElementById('stl-completed-tbody');
    adminFetch('/admin/api/settlement/completed')
      .then(r => r.json())
      .then(data => {
        if (!data.success || !data.list || data.list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-12 text-center text-gray-400 text-sm">정산 완료 내역이 없습니다.</td></tr>';
          return;
        }
        tbody.innerHTML = data.list.map(item => `
          <tr class="hover:bg-gray-50 transition">
            <td class="px-5 py-4 text-sm text-gray-800 font-medium">\${item.sale_title || '-'}</td>
            <td class="px-5 py-4 text-sm text-gray-600">\${item.member_seller_nm || '-'}</td>
            <td class="px-5 py-4 text-sm font-bold text-right text-green-700">\${fmtMoney(item.settlement_amount)}</td>
            <td class="px-5 py-4 text-sm text-gray-500">\${fmtDate(item.request_dtm)}</td>
            <td class="px-5 py-4 text-sm text-gray-500">\${fmtDate(item.settled_dtm)}</td>
          </tr>`).join('');
      }).catch(() => {
        tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-12 text-center text-red-400 text-sm">데이터 로드 실패</td></tr>';
      });
  }

  function confirmTransfer(settlementSeq, btn) {
    if (!confirm('계좌 이체를 완료하셨습니까?\n확인 시 정산 완료 처리됩니다.')) return;
    btn.disabled = true;
    btn.textContent = '처리 중...';
    adminFetch('/admin/api/settlement/confirm-transfer/' + settlementSeq, { method: 'POST' })
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          const row = btn.closest('tr');
          row.classList.add('opacity-0', 'transition-opacity', 'duration-300');
          setTimeout(() => { row.remove(); loadSummary(); loadCompleted(); }, 300);
        } else {
          alert(data.message || '처리 실패');
          btn.disabled = false;
          btn.textContent = '정산 완료 처리';
        }
      }).catch(() => {
        alert('오류가 발생했습니다.');
        btn.disabled = false;
        btn.textContent = '정산 완료 처리';
      });
  }
</script>
