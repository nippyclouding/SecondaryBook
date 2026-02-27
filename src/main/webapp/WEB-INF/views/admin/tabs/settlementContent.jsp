<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="space-y-6">

  <%-- ===== 요약 카드 ===== --%>
  <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">관리자 잔액</p>
      <p id="stl-admin-balance" class="text-2xl font-black text-gray-900 mb-3">-</p>
      <a href="${pageContext.request.contextPath}/admin/balance/charge"
         class="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition">
        + 잔액 충전
      </a>
    </div>
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">정산 신청 (REQUESTED)</p>
      <p id="stl-requested-count" class="text-2xl font-black text-primary-600">-</p>
    </div>
    <div class="bg-white rounded-2xl border border-blue-100 shadow-sm p-6 bg-blue-50/30">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">이체 대기 건수</p>
      <p id="stl-pending-count" class="text-2xl font-black text-blue-600">-</p>
    </div>
    <div class="bg-white rounded-2xl border border-orange-100 shadow-sm p-6 bg-orange-50/30">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">잔액 부족 건수</p>
      <p id="stl-insufficient-count" class="text-2xl font-black text-orange-500">-</p>
    </div>
  </div>

  <%-- ===== 이체 대기 목록 ===== --%>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
    <div class="p-6 border-b border-gray-100 bg-gray-50/50 flex items-center justify-between">
      <div>
        <h3 class="font-bold text-lg text-gray-900">이체 대기 목록</h3>
        <p class="text-xs text-gray-500 mt-0.5">배치 처리 완료 후 실제 이체가 필요한 건 — 총 <span id="stl-total-amount" class="font-bold text-primary-600">-</span>원</p>
      </div>
      <button onclick="loadSettlementData()" class="px-4 py-2 text-sm bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition flex items-center gap-2">
        <i data-lucide="refresh-cw" class="w-4 h-4"></i> 새로고침
      </button>
    </div>

    <table class="w-full">
      <thead class="bg-gray-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">
        <tr>
          <th class="px-5 py-4 text-left">상품명</th>
          <th class="px-5 py-4 text-left">판매자</th>
          <th class="px-5 py-4 text-left">은행</th>
          <th class="px-5 py-4 text-left">계좌번호</th>
          <th class="px-5 py-4 text-left">예금주</th>
          <th class="px-5 py-4 text-right">정산금액</th>
          <th class="px-5 py-4 text-left">배치 완료일</th>
          <th class="px-5 py-4 text-center">이체 완료</th>
        </tr>
      </thead>
      <tbody id="stl-transfer-pending-tbody" class="divide-y divide-gray-50">
        <tr>
          <td colspan="8" class="px-6 py-12 text-center text-gray-400 text-sm">데이터를 불러오는 중...</td>
        </tr>
      </tbody>
    </table>
  </div>

  <%-- ===== 잔액 부족 목록 ===== --%>
  <div class="bg-white rounded-2xl border border-orange-200 shadow-sm overflow-hidden">
    <div class="p-6 border-b border-orange-100 bg-orange-50/30">
      <h3 class="font-bold text-lg text-gray-900">잔액 부족 목록</h3>
      <p class="text-xs text-gray-500 mt-0.5">관리자 계좌 잔액 부족으로 처리 실패한 건 — 잔액 충전 후 "재처리 설정" 버튼 클릭 시 다음 배치에서 재시도</p>
    </div>

    <table class="w-full">
      <thead class="bg-orange-50 text-[11px] font-bold text-gray-400 uppercase tracking-widest border-b border-orange-100">
        <tr>
          <th class="px-5 py-4 text-left">상품명</th>
          <th class="px-5 py-4 text-left">판매자</th>
          <th class="px-5 py-4 text-right">정산금액</th>
          <th class="px-5 py-4 text-left">신청일</th>
          <th class="px-5 py-4 text-center">재처리</th>
        </tr>
      </thead>
      <tbody id="stl-insufficient-tbody" class="divide-y divide-orange-50/50">
        <tr>
          <td colspan="5" class="px-6 py-12 text-center text-gray-400 text-sm">데이터를 불러오는 중...</td>
        </tr>
      </tbody>
    </table>
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
    loadTransferPending();
    loadInsufficient();
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

    adminFetch('/admin/api/settlement/transfer-pending')
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          document.getElementById('stl-pending-count').textContent = (data.count || 0) + '건';
          document.getElementById('stl-total-amount').textContent = fmtMoney(data.totalAmount);
        }
      }).catch(() => {});

    adminFetch('/admin/api/settlement/insufficient')
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          document.getElementById('stl-insufficient-count').textContent = (data.count || 0) + '건';
        }
      }).catch(() => {});
  }

  function loadTransferPending() {
    const tbody = document.getElementById('stl-transfer-pending-tbody');
    adminFetch('/admin/api/settlement/transfer-pending')
      .then(r => r.json())
      .then(data => {
        if (!data.success || !data.list || data.list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="8" class="px-6 py-12 text-center text-gray-400 text-sm">이체 대기 건이 없습니다.</td></tr>';
          return;
        }
        tbody.innerHTML = data.list.map(item => {
          const bankName = BANK_NAMES[item.bank_code] || item.bank_code || '-';
          const accountNo = item.bank_account_no || '-';
          const holderNm = item.account_holder_nm || '-';
          return `
            <tr class="hover:bg-gray-50 transition">
              <td class="px-5 py-4 text-sm text-gray-800 font-medium">${item.sale_title || '-'}</td>
              <td class="px-5 py-4 text-sm text-gray-600">${item.member_seller_nm || '-'}</td>
              <td class="px-5 py-4 text-sm text-gray-600">${bankName}</td>
              <td class="px-5 py-4 text-sm font-mono text-gray-800">${accountNo}</td>
              <td class="px-5 py-4 text-sm text-gray-600">${holderNm}</td>
              <td class="px-5 py-4 text-sm font-bold text-right text-primary-700">${fmtMoney(item.settlement_amount)}</td>
              <td class="px-5 py-4 text-sm text-gray-500">${fmtDate(item.settled_dtm)}</td>
              <td class="px-5 py-4 text-center">
                <button onclick="confirmTransfer(${item.settlement_seq}, this)"
                  class="px-3 py-1.5 text-xs font-bold bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition">
                  이체 완료
                </button>
              </td>
            </tr>`;
        }).join('');
        if (typeof lucide !== 'undefined') lucide.createIcons();
      }).catch(() => {
        tbody.innerHTML = '<tr><td colspan="8" class="px-6 py-12 text-center text-red-400 text-sm">데이터 로드 실패</td></tr>';
      });
  }

  function loadInsufficient() {
    const tbody = document.getElementById('stl-insufficient-tbody');
    adminFetch('/admin/api/settlement/insufficient')
      .then(r => r.json())
      .then(data => {
        if (!data.success || !data.list || data.list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-12 text-center text-gray-400 text-sm">잔액 부족 건이 없습니다.</td></tr>';
          return;
        }
        tbody.innerHTML = data.list.map(item => `
          <tr class="hover:bg-orange-50/30 transition">
            <td class="px-5 py-4 text-sm text-gray-800 font-medium">${item.sale_title || '-'}</td>
            <td class="px-5 py-4 text-sm text-gray-600">${item.member_seller_nm || '-'}</td>
            <td class="px-5 py-4 text-sm font-bold text-right text-orange-600">${fmtMoney(item.settlement_amount)}</td>
            <td class="px-5 py-4 text-sm text-gray-500">${fmtDate(item.request_dtm)}</td>
            <td class="px-5 py-4 text-center">
              <button onclick="resetSettlement(${item.settlement_seq}, this)"
                class="px-3 py-1.5 text-xs font-bold bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition">
                재처리 설정
              </button>
            </td>
          </tr>`).join('');
      }).catch(() => {
        tbody.innerHTML = '<tr><td colspan="5" class="px-6 py-12 text-center text-red-400 text-sm">데이터 로드 실패</td></tr>';
      });
  }

  function confirmTransfer(settlementSeq, btn) {
    if (!confirm('이체 완료 처리하시겠습니까?')) return;
    btn.disabled = true;
    btn.textContent = '처리 중...';
    adminFetch('/admin/api/settlement/confirm-transfer/' + settlementSeq, { method: 'POST' })
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          const row = btn.closest('tr');
          row.classList.add('opacity-0', 'transition-opacity', 'duration-300');
          setTimeout(() => { row.remove(); loadSummary(); }, 300);
        } else {
          alert(data.message || '처리 실패');
          btn.disabled = false;
          btn.textContent = '이체 완료';
        }
      }).catch(() => {
        alert('오류가 발생했습니다.');
        btn.disabled = false;
        btn.textContent = '이체 완료';
      });
  }

  function resetSettlement(settlementSeq, btn) {
    if (!confirm('잔액을 충전하셨습니까?\n확인 시 다음 배치 실행 시 자동 재처리됩니다.')) return;
    btn.disabled = true;
    btn.textContent = '처리 중...';
    adminFetch('/admin/api/settlement/reset/' + settlementSeq, { method: 'POST' })
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          const row = btn.closest('tr');
          row.classList.add('opacity-0', 'transition-opacity', 'duration-300');
          setTimeout(() => { row.remove(); loadSummary(); }, 300);
        } else {
          alert(data.message || '처리 실패');
          btn.disabled = false;
          btn.textContent = '재처리 설정';
        }
      }).catch(() => {
        alert('오류가 발생했습니다.');
        btn.disabled = false;
        btn.textContent = '재처리 설정';
      });
  }
</script>
