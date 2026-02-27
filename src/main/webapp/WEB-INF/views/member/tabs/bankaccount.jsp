<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="bg-white rounded-[2rem] border border-gray-100 p-8 shadow-sm">

    <%-- 헤더 --%>
    <div class="flex justify-between items-center mb-8 pb-4 border-b border-gray-100">
        <div>
            <h2 class="text-xl font-black text-gray-900 tracking-tight">정산 계좌 관리</h2>
            <p class="text-xs font-medium text-gray-400 mt-1">판매 대금을 받을 계좌를 등록해주세요.</p>
        </div>
        <button id="btn-edit-bank" onclick="BankAccountTab.toggleEditMode(true)"
                class="flex items-center gap-1.5 text-xs font-bold text-gray-600 bg-gray-50 px-4 py-2 rounded-full hover:bg-gray-100 transition shadow-sm">
            <i data-lucide="edit-2" class="w-3.5 h-3.5"></i> 수정하기
        </button>
    </div>

    <%-- 뷰 모드 --%>
    <div id="bank-view-mode">

        <%-- 스켈레톤 (로딩 중) --%>
        <div id="bank-skeleton" class="animate-pulse space-y-4">
            <div class="h-5 bg-gray-100 rounded-lg w-1/4"></div>
            <div class="h-5 bg-gray-100 rounded-lg w-2/4"></div>
            <div class="h-5 bg-gray-100 rounded-lg w-1/3"></div>
        </div>

        <%-- 계좌 등록됨 --%>
        <div id="bank-registered" class="hidden space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div>
                    <span class="text-xs font-bold text-gray-400 block mb-2 uppercase tracking-wider">은행</span>
                    <span id="view-bank-nm" class="text-gray-900 font-bold text-lg"></span>
                </div>
                <div>
                    <span class="text-xs font-bold text-gray-400 block mb-2 uppercase tracking-wider">계좌번호</span>
                    <span id="view-account-no" class="text-gray-900 font-bold text-lg tracking-widest"></span>
                </div>
                <div>
                    <span class="text-xs font-bold text-gray-400 block mb-2 uppercase tracking-wider">예금주</span>
                    <span id="view-holder-nm" class="text-gray-900 font-bold text-lg"></span>
                </div>
            </div>
            <div class="mt-4 p-4 bg-amber-50 border border-amber-100 rounded-2xl flex items-start gap-3">
                <i data-lucide="info" class="w-4 h-4 text-amber-500 mt-0.5 shrink-0"></i>
                <p class="text-xs font-medium text-amber-700 leading-relaxed">
                    정산은 매일 새벽 3시 배치 처리 후 관리자가 해당 계좌로 수동 이체합니다.<br>
                    계좌 변경 시 다음 정산부터 적용됩니다.
                </p>
            </div>
        </div>

        <%-- 계좌 미등록 --%>
        <div id="bank-empty" class="hidden py-16 text-center">
            <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                <i data-lucide="landmark" class="w-8 h-8"></i>
            </div>
            <p class="text-base text-gray-500 font-bold mb-2">등록된 정산 계좌가 없습니다.</p>
            <p class="text-sm text-gray-400 mb-6">판매 대금을 받으려면 계좌를 먼저 등록해주세요.</p>
            <button onclick="BankAccountTab.toggleEditMode(true)"
                    class="inline-flex items-center gap-1.5 text-sm font-bold text-white bg-primary-600 px-5 py-2.5 rounded-full hover:bg-primary-700 transition shadow-md">
                <i data-lucide="plus" class="w-4 h-4"></i> 계좌 등록하기
            </button>
        </div>
    </div>

    <%-- 편집 모드 --%>
    <form id="bank-edit-mode" onsubmit="BankAccountTab.submit(event)"
          class="hidden max-w-md space-y-6 animate-[fadeIn_0.3s_ease-out]">

        <div class="space-y-1.5">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">
                은행 <span class="text-red-500">*</span>
            </label>
            <select id="bank_code" name="bank_code" required
                    class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl focus:bg-white focus:ring-2 focus:ring-primary-500/20 text-sm font-bold text-gray-900 transition outline-none appearance-none cursor-pointer">
                <option value="">은행을 선택해주세요</option>
                <option value="004">국민은행 (004)</option>
                <option value="020">우리은행 (020)</option>
                <option value="088">신한은행 (088)</option>
                <option value="081">하나은행 (081)</option>
                <option value="003">IBK기업은행 (003)</option>
                <option value="011">농협은행 (011)</option>
                <option value="023">SC제일은행 (023)</option>
                <option value="032">부산은행 (032)</option>
                <option value="045">새마을금고 (045)</option>
                <option value="064">산림조합 (064)</option>
                <option value="090">카카오뱅크 (090)</option>
                <option value="089">케이뱅크 (089)</option>
                <option value="092">토스뱅크 (092)</option>
            </select>
        </div>

        <div class="space-y-1.5">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">
                계좌번호 <span class="text-red-500">*</span>
            </label>
            <input type="text" id="bank_account_no" name="bank_account_no"
                   placeholder="숫자만 입력해주세요 (예: 12345678901234)"
                   maxlength="20"
                   oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                   class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl focus:bg-white focus:ring-2 focus:ring-primary-500/20 text-sm font-bold tracking-widest transition outline-none"
                   required />
            <p class="text-xs text-gray-400 ml-1">하이픈(-) 없이 숫자만 입력하세요.</p>
        </div>

        <div class="space-y-1.5">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">
                예금주명 <span class="text-red-500">*</span>
            </label>
            <input type="text" id="account_holder_nm" name="account_holder_nm"
                   placeholder="예금주명을 입력해주세요"
                   maxlength="20"
                   class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl focus:bg-white focus:ring-2 focus:ring-primary-500/20 text-sm font-bold transition outline-none"
                   required />
        </div>

        <div class="flex gap-3 pt-4">
            <button type="button" onclick="BankAccountTab.toggleEditMode(false)"
                    class="flex-1 py-4 bg-white border border-gray-200 rounded-2xl text-sm font-bold text-gray-600 hover:bg-gray-50 transition shadow-sm">
                취소
            </button>
            <button type="submit" id="btn-bank-submit"
                    class="flex-1 py-4 bg-gray-900 text-white rounded-2xl text-sm font-bold hover:bg-black shadow-lg hover:shadow-xl hover:-translate-y-0.5 transition-all">
                저장하기
            </button>
        </div>
    </form>

</div>

<script>
(function () {

    const BANK_NAMES = {
        '004': '국민은행', '020': '우리은행', '088': '신한은행', '081': '하나은행',
        '003': 'IBK기업은행', '011': '농협은행', '023': 'SC제일은행', '032': '부산은행',
        '045': '새마을금고', '064': '산림조합', '090': '카카오뱅크', '089': '케이뱅크',
        '092': '토스뱅크'
    };

    let currentAccount = null;

    function getCsrfInfo() {
        const token = document.querySelector('meta[name="_csrf"]')?.content || '';
        const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        return { token, header };
    }

    const actions = {

        init: async () => {
            await actions.loadAccount();
        },

        loadAccount: async () => {
            try {
                const res = await fetch('/profile/bankaccount/get');
                currentAccount = await res.json();

                document.getElementById('bank-skeleton').classList.add('hidden');

                if (currentAccount && currentAccount.bank_code) {
                    actions.renderView(currentAccount);
                    document.getElementById('bank-registered').classList.remove('hidden');
                    document.getElementById('btn-edit-bank').classList.remove('hidden');
                } else {
                    currentAccount = null;
                    document.getElementById('bank-empty').classList.remove('hidden');
                    document.getElementById('btn-edit-bank').classList.add('hidden');
                }
            } catch (e) {
                console.error('계좌 조회 실패', e);
                document.getElementById('bank-skeleton').classList.add('hidden');
                document.getElementById('bank-empty').classList.remove('hidden');
            }
        },

        renderView: (account) => {
            const bankNm = BANK_NAMES[account.bank_code] || account.bank_code;
            document.getElementById('view-bank-nm').textContent = bankNm + ' (' + account.bank_code + ')';
            document.getElementById('view-account-no').textContent = account.bank_account_no;
            document.getElementById('view-holder-nm').textContent = account.account_holder_nm;
        },

        toggleEditMode: (isEdit) => {
            const viewMode = document.getElementById('bank-view-mode');
            const editMode = document.getElementById('bank-edit-mode');
            const editBtn = document.getElementById('btn-edit-bank');

            if (isEdit) {
                viewMode.classList.add('hidden');
                editMode.classList.remove('hidden');
                editBtn.classList.add('hidden');

                // 기존 계좌 정보 폼에 채우기
                if (currentAccount && currentAccount.bank_code) {
                    document.getElementById('bank_code').value = currentAccount.bank_code;
                    document.getElementById('bank_account_no').value = currentAccount.bank_account_no;
                    document.getElementById('account_holder_nm').value = currentAccount.account_holder_nm;
                } else {
                    document.getElementById('bank-edit-mode').reset();
                }
            } else {
                viewMode.classList.remove('hidden');
                editMode.classList.add('hidden');
                if (currentAccount && currentAccount.bank_code) {
                    editBtn.classList.remove('hidden');
                }
            }
            if (window.lucide) lucide.createIcons();
        },

        submit: async (e) => {
            e.preventDefault();

            const bankCode = document.getElementById('bank_code').value;
            const accountNo = document.getElementById('bank_account_no').value.replace(/[^0-9]/g, '');
            const holderNm = document.getElementById('account_holder_nm').value.trim();

            if (!bankCode) { alert('은행을 선택해주세요.'); return; }
            if (accountNo.length < 7) { alert('올바른 계좌번호를 입력해주세요.'); return; }
            if (!holderNm) { alert('예금주명을 입력해주세요.'); return; }

            const submitBtn = document.getElementById('btn-bank-submit');
            submitBtn.disabled = true;
            submitBtn.textContent = '저장 중...';

            const csrf = getCsrfInfo();
            const params = new URLSearchParams();
            params.append('bank_code', bankCode);
            params.append('bank_account_no', accountNo);
            params.append('account_holder_nm', holderNm);

            try {
                const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
                if (csrf.token) headers[csrf.header] = csrf.token;

                const res = await fetch('/profile/bankaccount/save', {
                    method: 'POST',
                    headers: headers,
                    body: params.toString()
                });
                const data = await res.json();

                if (data.success) {
                    // 성공: 뷰 모드로 전환 후 데이터 다시 로드
                    await actions.loadAccount();
                    // 등록 직후 뷰 상태 정리
                    document.getElementById('bank-view-mode').classList.remove('hidden');
                    document.getElementById('bank-edit-mode').classList.add('hidden');
                    document.getElementById('bank-empty').classList.add('hidden');
                    alert(data.message);
                } else {
                    alert(data.message || '저장에 실패했습니다.');
                }
            } catch (err) {
                console.error(err);
                alert('오류가 발생했습니다.');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = '저장하기';
            }
        }
    };

    window.BankAccountTab = actions;
    actions.init();
})();
</script>
