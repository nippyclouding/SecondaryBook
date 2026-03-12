<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="https://js.tosspayments.com/v1/payment"></script>

<div class="min-h-[calc(100vh-200px)] pb-20">
    <div id="paymentTimer" class="fixed top-16 left-0 right-0 z-40 bg-gradient-to-r from-red-500 to-orange-500 text-white py-3 px-4 shadow-lg">
        <div class="max-w-6xl mx-auto flex items-center justify-center gap-3">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span class="font-semibold">남은 결제 시간:</span>
            <span id="timerDisplay" class="text-2xl font-bold tracking-wider">05:00</span>
        </div>
    </div>

    <div class="h-14"></div>

    <div class="mb-8 mt-8">
        <h1 class="text-2xl font-bold text-gray-900">결제하기</h1>
    </div>

    <div class="flex gap-8">
        <div class="flex-1 space-y-6">
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-100 bg-gray-50 font-bold text-gray-800">구매자 정보</div>
                <div class="p-6 grid grid-cols-3 gap-6 text-sm">
                    <div><label class="block text-gray-500 mb-1">이름</label><p class="font-semibold"><c:out value="${sessionScope.loginSess.member_nicknm}"/></p></div>
                    <div><label class="block text-gray-500 mb-1">이메일</label><p class="font-semibold"><c:out value="${sessionScope.loginSess.member_email}"/></p></div>
                    <div><label class="block text-gray-500 mb-1">연락처</label><p class="font-semibold"><c:out value="${sessionScope.loginSess.member_tel_no}"/></p></div>
                </div>
            </div>

            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-100 bg-gray-50 font-bold text-gray-800">배송지 설정</div>
                <div class="p-6">
                    <div class="flex flex-wrap gap-4 mb-8">
                        <label class="flex-1 min-w-[140px] cursor-pointer">
                            <input type="radio" name="addr_type" value="existing" class="peer sr-only" ${not empty addressList ? 'checked' : 'disabled'}>
                            <div class="p-4 border-2 rounded-xl text-center transition-all peer-checked:border-primary-500 peer-checked:bg-primary-50 peer-disabled:opacity-50 peer-disabled:bg-gray-50">
                                <p class="font-bold text-sm">내 배송지 선택</p>
                            </div>
                        </label>
                        <label class="flex-1 min-w-[140px] cursor-pointer">
                            <input type="radio" name="addr_type" value="manual" class="peer sr-only" ${empty addressList ? 'checked' : ''}>
                            <div class="p-4 border-2 rounded-xl text-center transition-all peer-checked:border-primary-500 peer-checked:bg-primary-50">
                                <p class="font-bold text-sm">직접 등록</p>
                            </div>
                        </label>
                        <label class="flex-1 min-w-[140px] cursor-pointer">
                            <input type="radio" name="addr_type" value="direct" class="peer sr-only">
                            <div class="p-4 border-2 rounded-xl text-center transition-all peer-checked:border-red-500 peer-checked:bg-red-50">
                                <p class="font-bold text-sm">직거래/반값택배</p>
                            </div>
                        </label>
                    </div>

                    <div id="section-existing" class="addr-section ${empty addressList ? 'hidden' : ''}">
                        <div class="flex items-center justify-between mb-4 px-1">
                            <h4 class="text-sm font-bold text-gray-700 italic underline decoration-primary-200 underline-offset-4">현재 선택된 주소</h4>
                            <button type="button" onclick="toggleAddressModal()" class="text-xs px-3 py-1.5 bg-white border border-gray-200 rounded-lg shadow-sm font-bold text-primary-600 hover:bg-gray-50">주소록 변경</button>
                        </div>
                        <div id="selectedAddressArea" class="p-5 bg-gray-50 rounded-2xl border border-gray-200">
                            <c:set var="defaultAddr" value="" />
                            <c:forEach var="addr" items="${addressList}"><c:if test="${addr.default_yn == 1}"><c:set var="defaultAddr" value="${addr}" /></c:if></c:forEach>
                            <c:set var="displayAddr" value="${not empty defaultAddr ? defaultAddr : addressList[0]}" />
                            <c:if test="${not empty addressList}">
                                <div id="selectedAddress">
                                    <p class="font-bold text-gray-900 mb-1" id="addrName">${displayAddr.addr_nm}</p>
                                    <p class="text-sm text-gray-600">
                                        [<span id="addrPostNo">${displayAddr.post_no}</span>]
                                        <span id="addrH">${displayAddr.addr_h}</span> <span id="addrD">${displayAddr.addr_d}</span>
                                    </p>
                                    <input type="hidden" id="selectedAddrSeq" value="${displayAddr.addr_seq}">
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <div id="section-manual" class="addr-section ${not empty addressList ? 'hidden' : ''} space-y-3">
                        <div class="flex items-center justify-between mb-1">
                            <h4 class="text-sm font-bold text-gray-700">새 배송 정보</h4>
                            <button type="button" onclick="execDaumPostcode()" class="text-xs px-3 py-1.5 bg-gray-900 text-white rounded-lg font-bold hover:bg-gray-800 transition">주소 찾기</button>
                        </div>
                        <div class="grid grid-cols-2 gap-3">

                            <input type="text" id="newPostNo" placeholder="우편번호" readonly class="w-full border bg-gray-50 rounded-xl px-4 py-3 text-sm outline-none cursor-not-allowed">
                        </div>
                        <input type="text" id="newAddrH" placeholder="기본 주소 (주소 찾기를 이용하세요)" readonly class="w-full border bg-gray-50 rounded-xl px-4 py-3 text-sm outline-none cursor-not-allowed">
                        <input type="text" id="newAddrD" placeholder="상세 주소를 입력해주세요" class="w-full border rounded-xl px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary-500">
                    </div>

                    <div id="section-direct" class="addr-section hidden py-10 text-center bg-red-50 rounded-2xl border border-red-100">
                        <p class="text-red-600 font-bold text-lg">직거래 또는 반값택배</p>
                        <p class="text-sm text-red-500 mt-2">상세 위치는 결제 후 판매자와 채팅으로 정해주세요.</p>
                    </div>
                </div>
            </div>

            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden p-6">
                <p class="font-bold text-gray-800 mb-4">결제 수단</p>
                <div class="p-4 bg-blue-50 rounded-xl border border-blue-100 flex items-center gap-3">
                    <div class="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white font-bold italic">T</div>
                    <span class="font-bold text-gray-900">토스 안전 결제</span>
                </div>
            </div>
        </div>

        <div class="w-96">
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 sticky top-24">
                <div class="flex gap-3 pb-5 border-b mb-5">
                    <img src="${trade.book_img}" class="w-12 h-16 object-cover rounded shadow-sm">
                    <div class="min-w-0">
                        <p class="text-xs font-bold text-gray-900 truncate">${trade.book_title}</p>
                        <p class="text-[10px] text-gray-500 truncate">${trade.book_author}</p>
                    </div>
                </div>
                <div class="space-y-3 mb-6">
                    <div class="flex justify-between text-sm text-gray-600"><span>상품 금액</span><span class="font-bold text-gray-900"><fmt:formatNumber value="${trade.sale_price}"/>원</span></div>
                    <div class="flex justify-between text-sm text-gray-600"><span>배송비</span><span class="font-bold text-gray-900"><fmt:formatNumber value="${trade.delivery_cost}"/>원</span></div>
                    <div class="pt-3 border-t flex justify-between items-center"><span class="font-bold">총 결제금액</span><span class="text-2xl font-bold text-primary-600"><fmt:formatNumber value="${trade.sale_price + trade.delivery_cost}"/>원</span></div>
                </div>
                <label class="flex items-start gap-2 cursor-pointer mb-6">
                    <input type="checkbox" id="agreeCheckbox" class="mt-1">
                    <span class="text-[11px] text-gray-500 leading-tight">주문 정보 확인 및 개인정보 제공에 동의합니다. (필수)</span>
                </label>
                <button id="payBtn" disabled class="w-full py-4 bg-gray-300 text-gray-500 rounded-2xl font-bold text-lg transition-all">결제하기</button>
            </div>
        </div>
    </div>
</div>

<div id="addressModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 hidden items-center justify-center">
    <div class="bg-white rounded-3xl w-full max-w-md mx-4 overflow-hidden shadow-2xl">
        <div class="px-6 py-5 border-b flex items-center justify-between bg-gray-50">
            <h3 class="font-bold text-gray-900">내 배송지 선택</h3>
            <button onclick="toggleAddressModal()" class="text-2xl text-gray-400 hover:text-gray-600">&times;</button>
        </div>
        <div class="p-4 max-h-96 overflow-y-auto space-y-3">
            <c:forEach var="addr" items="${addressList}">
                <div class="address-item p-4 border-2 rounded-2xl hover:border-primary-500 hover:bg-primary-50 cursor-pointer transition-all"
                     data-addr-seq="${addr.addr_seq}" data-post-no="${addr.post_no}"
                     data-addr-h="${addr.addr_h}" data-addr-d="${addr.addr_d}" data-addr-nm="${addr.addr_nm}">
                    <p class="font-bold text-sm text-gray-900">${addr.addr_nm}</p>
                    <p class="text-xs text-gray-500 mt-1">[${addr.post_no}] ${addr.addr_h} ${addr.addr_d}</p>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

       <script>
           // 1. 배송지 타입 전환 로직
           const addrRadios = document.querySelectorAll('input[name="addr_type"]');
           const sections = document.querySelectorAll('.addr-section');

           addrRadios.forEach(radio => {
               radio.addEventListener('change', (e) => {
                   const targetId = 'section-' + e.target.value;
                   sections.forEach(section => {
                       section.id === targetId ? section.classList.remove('hidden') : section.classList.add('hidden');
                   });
               });
           });

           // 2. 다음 주소 API (execDaumPostcode)
           function execDaumPostcode() {
               new daum.Postcode({
                   oncomplete: function(data) {
                       // 우편번호와 주소 정보를 해당 필드에 넣는다.
                       document.getElementById('newPostNo').value = data.zonecode;
                       document.getElementById('newAddrH').value = data.address;
                       // 커서를 상세주소 필드로 이동한다.
                       document.getElementById('newAddrD').focus();
                   }
               }).open();
           }

           // 3. 모달 및 기존 주소 선택 (\ 이스케이프 유지)
           function toggleAddressModal() {
               const modal = document.getElementById('addressModal');
               modal.classList.toggle('hidden');
               modal.classList.toggle('flex');
           }

           document.querySelectorAll('.address-item').forEach(item => {
               item.addEventListener('click', function() {
                   const area = document.getElementById('selectedAddressArea');
                   area.innerHTML = `
                       <div id="selectedAddress">
                           <p class="font-bold text-gray-900 mb-1" id="addrName">\${this.dataset.addrNm}</p>
                           <p class="text-sm text-gray-600">
                               [<span id="addrPostNo">\${this.dataset.postNo}</span>]
                               <span id="addrH">\${this.dataset.addrH}</span> <span id="addrD">\${this.dataset.addrD}</span>
                           </p>
                           <input type="hidden" id="selectedAddrSeq" value="\${this.dataset.addrSeq}">
                       </div>
                   `;
                   toggleAddressModal();
               });
           });

           // 4. 결제 관련 (Toss & Button)
               document.getElementById('agreeCheckbox').addEventListener('change', function() {
                   const btn = document.getElementById('payBtn');
                   btn.disabled = !this.checked;
                   btn.classList.toggle('bg-primary-500', this.checked);
                   btn.classList.toggle('text-white', this.checked);
                   btn.classList.toggle('cursor-pointer', this.checked);
               });

               const tossPayments = TossPayments("test_ck_KNbdOvk5rka22P9eoqA43n07xlzm");

               document.getElementById('payBtn').addEventListener('click', function() {
                   // --- 배송지 정보 수집 시작 ---
                   const addrType = document.querySelector('input[name="addr_type"]:checked').value;
                   let addrParams = "&addr_type=" + addrType;

                   if (addrType === 'existing') {
                       // '내 배송지 선택'인 경우 화면에 렌더링된 텍스트 가져오기
                       const postNo = document.getElementById('addrPostNo')?.innerText || "";
                       const addrH = document.getElementById('addrH')?.innerText || "";
                       const addrD = document.getElementById('addrD')?.innerText || "";

                       addrParams += "&post_no=" + encodeURIComponent(postNo);
                       addrParams += "&addr_h=" + encodeURIComponent(addrH);
                       addrParams += "&addr_d=" + encodeURIComponent(addrD);
                   }
                   else if (addrType === 'manual') {
                       // '직접 등록'인 경우 input 필드 값 가져오기
                       const postNo = document.getElementById('newPostNo').value;
                       const addrH = document.getElementById('newAddrH').value;
                       const addrD = document.getElementById('newAddrD').value;

                       if(!postNo || !addrH) {
                           alert("주소를 입력해주세요.");
                           return;
                       }

                       // --- 글자 수 제한 체크 (180자) ---
                           if (addrH.length > 180 || addrD.length > 180) {
                               alert("주소 정보가 너무 깁니다. 180자 이내로 입력해주세요.");
                               return;
                           }

                       addrParams += "&post_no=" + encodeURIComponent(postNo);
                       addrParams += "&addr_h=" + encodeURIComponent(addrH);
                       addrParams += "&addr_d=" + encodeURIComponent(addrD);
                   }
                   // 'direct'인 경우 addr_type만 넘어가고 나머지는 백엔드에서 null 처리된다

                   tossPayments.requestPayment("토스페이", {
                       amount: Number("${trade.sale_price}") + Number("${trade.delivery_cost}"),
                       orderId: "ORDER_" + "${trade.trade_seq}" + "_" + new Date().getTime(),
                       orderName: "${trade.book_title}",
                       customerName: "${sessionScope.loginSess.member_nicknm}",
                       // successUrl 뒤에 수집한 addrParams를 붙여줍니다.
                       successUrl: window.location.origin + "/payments/success?trade_seq=${trade.trade_seq}" + addrParams,
                       failUrl: window.location.origin + "/payments/fail?trade_seq=${trade.trade_seq}"
                   });
               });

               const salePrice = Number("${trade.sale_price}");
               const deliveryCost = Number("${trade.delivery_cost}");
               const totalAmount = salePrice + deliveryCost;
               const memberNicknm = "${sessionScope.loginSess.member_nicknm}";
               const bookTitle = "${trade.book_title}";
               const tradeSeq = "${trade.trade_seq}";

               // ========== 결제 타이머 ==========
               let remainingSeconds = Number("${remainingSeconds}") || 300; // 서버에서 받은 남은 시간 (기본 5분)
               let timerInterval = null;
               let isPaymentProcessing = false; // 결제 진행 중 플래그

               function formatTime(seconds) {
                   const mins = Math.floor(seconds / 60);
                   const secs = seconds % 60;
                   return String(mins).padStart(2, '0') + ':' + String(secs).padStart(2, '0');
               }

               function updateTimerDisplay() {
                   const timerDisplay = document.getElementById('timerDisplay');
                   const timerBar = document.getElementById('paymentTimer');

                   timerDisplay.textContent = formatTime(remainingSeconds);

                   // 1분 미만이면 빨간색 강조
                   if (remainingSeconds <= 60) {
                       timerBar.classList.add('animate-pulse');
                       timerDisplay.classList.add('text-yellow-300');
                   }
               }

               function startTimer() {
                   updateTimerDisplay();

                   timerInterval = setInterval(function() {
                       remainingSeconds--;
                       updateTimerDisplay();

                       if (remainingSeconds <= 0) {
                           clearInterval(timerInterval);
                           handleTimeout();
                       }
                   }, 1000);
               }

               function handleTimeout() {
                   // 타임아웃 API 호출
                   fetch('/payments/timeout?trade_seq=' + tradeSeq, {
                       method: 'POST',
                       headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                   }).then(function() {
                       alert('결제 시간이 만료되었습니다. 다시 시도해주세요.');
                       window.location.href = '/payments/fail?trade_seq=' + tradeSeq + '&message=' + encodeURIComponent('결제 시간 만료');
                   }).catch(function() {
                       window.location.href = '/payments/fail?trade_seq=' + tradeSeq + '&message=' + encodeURIComponent('결제 시간 만료');
                   });
               }

               // 페이지 이탈 감지 (다른 사이트로 이동, 탭 닫기 등)
               function handlePageLeave() {
                   if (isPaymentProcessing) return; // 결제 진행 중이면 무시

                   // sendBeacon으로 비동기 요청 (페이지 닫혀도 전송됨)
                   navigator.sendBeacon('/payments/timeout?trade_seq=' + tradeSeq);
               }

               // beforeunload 이벤트 (페이지 이탈 시)
               window.addEventListener('beforeunload', function(e) {
                   if (isPaymentProcessing) return; // 결제 진행 중이면 무시

                   handlePageLeave();

                   // 경고 메시지 표시 (일부 브라우저에서만 동작)
                   e.preventDefault();
                   e.returnValue = '결제가 진행 중입니다. 페이지를 떠나시겠습니까?';
                   return e.returnValue;
               });

               // 페이지 로드 시 타이머 시작
               document.addEventListener('DOMContentLoaded', function() {
                   if (remainingSeconds > 0) {
                       startTimer();
                   } else {
                       handleTimeout();
                   }
               });
               // ========== 결제 타이머 끝 ==========

               // 현재 도메인 기준 URL 생성
               const baseUrl = window.location.origin;

               const payBtn = document.getElementById('payBtn');
               const agreeCheckbox = document.getElementById('agreeCheckbox');
               const agreeError = document.getElementById('agreeError');

               // 체크박스 상태에 따라 버튼 활성화/비활성화
               agreeCheckbox.addEventListener('change', function() {
                   if (this.checked) {
                       payBtn.disabled = false;
                       payBtn.classList.remove('bg-gray-300', 'text-gray-500', 'cursor-not-allowed');
                       payBtn.classList.add('bg-primary-500', 'hover:bg-primary-600', 'text-white', 'shadow-sm', 'hover:shadow-md');
                       agreeError.classList.add('hidden');
                   } else {
                       payBtn.disabled = true;
                       payBtn.classList.add('bg-gray-300', 'text-gray-500', 'cursor-not-allowed');
                       payBtn.classList.remove('bg-primary-500', 'hover:bg-primary-600', 'text-white', 'shadow-sm', 'hover:shadow-md');
                   }
               });

               payBtn.addEventListener('click', function() {
                   if (!agreeCheckbox.checked) {
                       agreeError.classList.remove('hidden');
                       return;
                   }

                   // 결제 진행 중 플래그 설정 (이탈 감지 무시)
                   isPaymentProcessing = true;

                   tossPayments.requestPayment("토스페이", {
                       amount: totalAmount,
                       orderId: "ORDER_" + tradeSeq + "_" + new Date().getTime(),
                       orderName: bookTitle,
                       customerName: memberNicknm,
                       successUrl: baseUrl + "/payments/success?trade_seq=" + tradeSeq,
                       failUrl: baseUrl + "/payments/fail?trade_seq=" + tradeSeq
                   }).catch(function(error) {
                       isPaymentProcessing = false; // 결제 취소/실패 시 플래그 해제

                       if (error.code === "USER_CANCEL") {
                           // 사용자가 결제창을 닫음 - 타이머는 계속 진행
                       } else {
                           alert(error.message);
                       }
                   });
               });
       </script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>