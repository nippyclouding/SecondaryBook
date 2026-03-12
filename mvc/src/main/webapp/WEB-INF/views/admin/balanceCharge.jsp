<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <sec:csrfMetaTags />
  <title>관리자 잔액 충전 - Admin Console</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          colors: {
            primary: { 50: '#eff6ff', 100: '#dbeafe', 500: '#3b82f6', 600: '#0046FF', 700: '#1d4ed8' }
          },
          fontFamily: { sans: ['Noto Sans KR', 'sans-serif'] }
        }
      }
    }
  </script>
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap" rel="stylesheet">
  <script src="https://js.tosspayments.com/v1/payment"></script>
</head>
<body class="bg-gray-50 min-h-screen font-sans">

  <div class="max-w-xl mx-auto px-4 py-12">

    <%-- 헤더 --%>
    <div class="flex items-center justify-between mb-8">
      <div>
        <p class="text-xs font-bold text-primary-600 uppercase tracking-widest mb-1">Admin Console</p>
        <h1 class="text-2xl font-black text-gray-900">관리자 잔액 충전</h1>
        <p class="text-sm text-gray-500 mt-1">정산 배치에서 사용하는 운영 잔액을 충전합니다.</p>
      </div>
      <a href="${pageContext.request.contextPath}/admin"
         class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800 transition">
        ← 대시보드로
      </a>
    </div>

    <%-- 성공 메시지 --%>
    <c:if test="${not empty chargeSuccess}">
      <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-2xl flex items-center gap-3">
        <svg class="w-5 h-5 text-green-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
        </svg>
        <p class="text-sm font-bold text-green-700"><c:out value="${chargeSuccess}"/></p>
      </div>
    </c:if>

    <%-- 오류 메시지 --%>
    <c:if test="${not empty chargeError}">
      <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center gap-3">
        <svg class="w-5 h-5 text-red-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
        <p class="text-sm font-bold text-red-700"><c:out value="${chargeError}"/></p>
      </div>
    </c:if>

    <%-- 현재 잔액 카드 --%>
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 mb-6">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">현재 운영 잔액</p>
      <p class="text-4xl font-black text-gray-900">
        <fmt:formatNumber value="${currentBalance}" type="number"/>
        <span class="text-xl font-bold text-gray-500">원</span>
      </p>
    </div>

    <%-- 충전 금액 선택 --%>
    <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 mb-4">
      <p class="text-sm font-bold text-gray-700 mb-4">충전 금액 선택</p>

      <div class="grid grid-cols-3 gap-3 mb-5">
        <button type="button" class="preset-btn py-3 rounded-xl border-2 border-gray-200 text-sm font-bold text-gray-700
                                     hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600 transition"
                data-amount="100000">10만원</button>
        <button type="button" class="preset-btn py-3 rounded-xl border-2 border-gray-200 text-sm font-bold text-gray-700
                                     hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600 transition"
                data-amount="300000">30만원</button>
        <button type="button" class="preset-btn py-3 rounded-xl border-2 border-gray-200 text-sm font-bold text-gray-700
                                     hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600 transition"
                data-amount="500000">50만원</button>
        <button type="button" class="preset-btn py-3 rounded-xl border-2 border-gray-200 text-sm font-bold text-gray-700
                                     hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600 transition"
                data-amount="1000000">100만원</button>
        <button type="button" class="preset-btn py-3 rounded-xl border-2 border-gray-200 text-sm font-bold text-gray-700
                                     hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600 transition"
                data-amount="3000000">300만원</button>
        <button type="button" class="preset-btn py-3 rounded-xl border-2 border-gray-200 text-sm font-bold text-gray-700
                                     hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600 transition"
                data-amount="5000000">500만원</button>
      </div>

      <div>
        <label class="block text-xs font-bold text-gray-500 mb-2">직접 입력 (원)</label>
        <input type="number" id="customAmount" min="1000" step="1000"
               placeholder="금액을 입력하세요 (최소 1,000원)"
               class="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm font-bold text-gray-900
                      outline-none focus:border-primary-500 transition">
      </div>

      <div class="mt-5 pt-5 border-t border-gray-100 flex items-center justify-between">
        <span class="text-sm text-gray-500 font-medium">선택된 충전 금액</span>
        <span id="selectedAmountDisplay" class="text-xl font-black text-primary-600">-</span>
      </div>
    </div>

    <%-- 충전 버튼 --%>
    <button id="chargeBtn" disabled
            class="w-full py-4 bg-gray-200 text-gray-400 rounded-2xl font-black text-lg transition-all cursor-not-allowed">
      토스페이로 충전하기
    </button>

    <p class="text-center text-xs text-gray-400 mt-4">
      결제 실패 시 자동으로 취소(환불)됩니다.
    </p>
  </div>

  <script>
    const tossPayments = TossPayments('${tossClientKey}');
    const contextPath   = '${pageContext.request.contextPath}';

    let selectedAmount = 0;

    const presetBtns    = document.querySelectorAll('.preset-btn');
    const customInput   = document.getElementById('customAmount');
    const displayEl     = document.getElementById('selectedAmountDisplay');
    const chargeBtn     = document.getElementById('chargeBtn');

    function updateSelection(amount) {
      selectedAmount = amount;
      if (amount > 0) {
        displayEl.textContent = amount.toLocaleString('ko-KR') + '원';
        chargeBtn.disabled = false;
        chargeBtn.className = 'w-full py-4 bg-primary-600 hover:bg-primary-700 text-white rounded-2xl font-black text-lg transition-all cursor-pointer';
      } else {
        displayEl.textContent = '-';
        chargeBtn.disabled = true;
        chargeBtn.className = 'w-full py-4 bg-gray-200 text-gray-400 rounded-2xl font-black text-lg transition-all cursor-not-allowed';
      }
    }

    // 프리셋 버튼 클릭
    presetBtns.forEach(btn => {
      btn.addEventListener('click', function () {
        presetBtns.forEach(b => b.classList.remove('border-primary-500', 'bg-primary-50', 'text-primary-600'));
        this.classList.add('border-primary-500', 'bg-primary-50', 'text-primary-600');
        customInput.value = '';
        updateSelection(Number(this.dataset.amount));
      });
    });

    // 직접 입력
    customInput.addEventListener('input', function () {
      presetBtns.forEach(b => b.classList.remove('border-primary-500', 'bg-primary-50', 'text-primary-600'));
      const val = Math.floor(Number(this.value));
      updateSelection(val >= 1000 ? val : 0);
    });

    // 충전 버튼 클릭 → Toss 결제 요청
    chargeBtn.addEventListener('click', function () {
      if (selectedAmount < 1000) {
        alert('최소 충전 금액은 1,000원입니다.');
        return;
      }
      this.disabled = true;
      this.textContent = '결제창 열는 중...';

      const orderId = 'CHARGE_' + new Date().getTime();

      tossPayments.requestPayment('토스페이', {
        amount:       selectedAmount,
        orderId:      orderId,
        orderName:    '관리자 운영 잔액 충전',
        customerName: '관리자',
        successUrl:   window.location.origin + contextPath + '/admin/balance/success',
        failUrl:      window.location.origin + contextPath + '/admin/balance/fail'
      }).catch(error => {
        chargeBtn.disabled = false;
        chargeBtn.textContent = '토스페이로 충전하기';
        if (error.code !== 'USER_CANCEL') {
          alert(error.message || '결제 요청 중 오류가 발생했습니다.');
        }
      });
    });
  </script>
</body>
</html>
