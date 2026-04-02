<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp" />

<div class="max-w-[440px] mx-auto mt-16 mb-20">
    <div class="bg-white p-8 rounded-lg border border-gray-200 shadow-sm">

        <div class="text-center mb-8">
            <h1 class="text-2xl font-bold text-gray-900">계정 찾기</h1>
        </div>

        <div class="flex border-b border-gray-200 mb-6">
            <button onclick="switchTab('findId')" id="tab-findId"
                    class="flex-1 pb-3 text-sm font-bold text-primary-600 border-b-2 border-primary-600 transition">
                아이디 찾기
            </button>
            <button onclick="switchTab('findPwd')" id="tab-findPwd"
                    class="flex-1 pb-3 text-sm font-medium text-gray-400 hover:text-gray-600 transition">
                비밀번호 찾기
            </button>
        </div>

        <div id="content-findId" class="space-y-4">
            <div>
                <label class="block text-sm font-bold text-gray-700 mb-1.5">휴대폰 번호</label>
                <input type="tel" id="findId_tel" oninput="autoHyphen(this)" maxlength="13"
                       class="w-full px-4 py-3 border border-gray-300 rounded-md focus:border-primary-500 outline-none text-sm"
                       placeholder="가입 시 등록한 휴대폰 번호" />
            </div>
            <p id="resultMsgId" class="text-sm text-center font-bold min-h-[20px]"></p>

            <button type="button" onclick="findIdAction()"
                    class="w-full bg-primary-500 text-white py-3.5 rounded-md font-bold text-sm hover:bg-primary-600 transition">
                아이디 찾기
            </button>
        </div>

        <div id="content-findPwd" class="hidden space-y-4">

            <div id="step-auth" class="space-y-4">
                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1.5">아이디</label>
                    <input type="text" id="findPwd_id"
                           class="w-full px-4 py-3 border border-gray-300 rounded-md focus:border-primary-500 outline-none text-sm"
                           placeholder="아이디 입력" />
                </div>
                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1.5">이메일</label>
                    <div class="flex gap-2">
                        <input type="email" id="findPwd_email" oninput="resetEmailAuth()"
                               class="flex-1 px-4 py-3 border border-gray-300 rounded-md focus:border-primary-500 outline-none text-sm"
                               placeholder="가입 시 등록한 이메일" />
                        <button type="button" onclick="sendResetCode()" id="btnSendCode"
                                class="px-4 py-2 bg-gray-800 text-white text-xs font-bold rounded-md hover:bg-gray-900 whitespace-nowrap transition">
                            인증번호 전송
                        </button>
                    </div>
                </div>

                <div id="authBox" class="hidden bg-gray-50 p-4 rounded-md border border-gray-200">
                    <div class="flex justify-between items-center mb-2">
                        <span class="text-xs text-gray-600 font-medium">인증번호 입력</span>
                        <span id="timer" class="text-xs text-red-500 font-bold">03:00</span>
                    </div>
                    <div class="flex gap-2">
                        <input type="text" id="authCode" maxlength="6"
                               class="flex-1 px-3 py-2 border border-gray-300 rounded-sm text-sm outline-none"
                               placeholder="인증번호 6자리" />
                        <button type="button" onclick="verifyResetCode()" id="btnVerifyCode"
                                class="px-3 py-2 bg-white border border-gray-300 text-xs font-bold rounded-sm hover:bg-gray-100">
                            확인
                        </button>
                    </div>
                    <div class="flex justify-between items-center mt-2">
                        <p id="authMsg" class="text-xs text-gray-500"></p>
                        <button type="button" onclick="sendResetCode()" class="text-xs text-gray-500 underline hover:text-gray-800">
                            재전송
                        </button>
                    </div>
                </div>
            </div>

            <div id="step-reset" class="hidden space-y-4 border-t border-gray-100 pt-4 mt-2">
                <div class="bg-blue-50 p-3 rounded text-xs text-blue-700 mb-2 font-medium">
                    인증이 완료되었습니다. 새로운 비밀번호를 설정해주세요.
                </div>
                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1.5">새 비밀번호</label>
                    <input type="password" id="new_pwd"
                           class="w-full px-4 py-3 border border-gray-300 rounded-md focus:border-primary-500 outline-none text-sm"
                           placeholder="8자 이상 입력" />
                </div>
                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-1.5">새 비밀번호 확인</label>
                    <input type="password" id="confirm_pwd"
                           class="w-full px-4 py-3 border border-gray-300 rounded-md focus:border-primary-500 outline-none text-sm"
                           placeholder="비밀번호 재입력" />
                </div>

                <button type="button" onclick="resetPasswordAction()"
                        class="w-full bg-primary-500 text-white py-3.5 rounded-md font-bold text-sm hover:bg-primary-600 transition">
                    비밀번호 변경하기
                </button>
            </div>

        </div>

        <div class="mt-6 text-center">
            <a href="/login" class="text-sm text-gray-500 hover:text-gray-800 underline">로그인 페이지로 돌아가기</a>
        </div>
    </div>
</div>

<script>
    let timerInterval;
    let isEmailSent = false; // 메일 발송 여부 체크

    // --- 탭 전환 로직 ---
    function switchTab(tabName) {
        const contentId = document.getElementById('content-findId');
        const contentPwd = document.getElementById('content-findPwd');
        const tabId = document.getElementById('tab-findId');
        const tabPwd = document.getElementById('tab-findPwd');

        if (tabName === 'findId') {
            contentId.classList.remove('hidden');
            contentPwd.classList.add('hidden');
            tabId.className = "flex-1 pb-3 text-sm font-bold text-primary-600 border-b-2 border-primary-600 transition";
            tabPwd.className = "flex-1 pb-3 text-sm font-medium text-gray-400 hover:text-gray-600 transition";
        } else {
            contentId.classList.add('hidden');
            contentPwd.classList.remove('hidden');
            tabId.className = "flex-1 pb-3 text-sm font-medium text-gray-400 hover:text-gray-600 transition";
            tabPwd.className = "flex-1 pb-3 text-sm font-bold text-primary-600 border-b-2 border-primary-600 transition";
        }

        // 초기화
        resetInputs();
    }

    function resetInputs() {
        document.querySelectorAll('input').forEach(input => input.value = '');
        document.getElementById('resultMsgId').innerText = '';
        document.getElementById('authMsg').innerText = '';
        resetEmailAuth(); // 인증 상태 초기화
    }

    // [추가] 이메일 입력값 변경 시 인증 상태 초기화
    function resetEmailAuth() {
        const btn = document.getElementById('btnSendCode');
        const authBox = document.getElementById('authBox');

        // 전송 버튼 복구
        btn.innerText = "인증번호 전송";
        btn.disabled = false;
        btn.classList.remove('bg-gray-400', 'cursor-not-allowed');
        btn.classList.add('bg-gray-800', 'hover:bg-gray-900');

        // 인증박스 숨김 및 초기화
        authBox.classList.add('hidden');
        document.getElementById('authCode').value = '';
        document.getElementById('authCode').disabled = false;
        document.getElementById('btnVerifyCode').disabled = false;
        document.getElementById('authMsg').innerText = '';

        // 타이머 정지
        if (timerInterval) clearInterval(timerInterval);
        document.getElementById('timer').innerText = "03:00";

        isEmailSent = false;
    }

    // --- 아이디 찾기 ---
    function findIdAction() {
        const tel = document.getElementById('findId_tel').value;
        const resultMsg = document.getElementById('resultMsgId');

        if(!tel) {
            alert('휴대폰 번호를 입력해주세요.');
            return;
        }

        $.ajax({
            url: '/auth/ajax/findId',
            type: 'POST',
            data: { member_tel_no: tel },
            success: function(res) {
                if (res && res !== "fail") {
                    resultMsg.textContent = '';
                    resultMsg.appendChild(document.createTextNode('회원님의 아이디는 '));
                    var idSpan = document.createElement('span');
                    idSpan.className = 'text-primary-600 text-lg';
                    idSpan.textContent = res;
                    resultMsg.appendChild(idSpan);
                    resultMsg.appendChild(document.createTextNode(' 입니다.'));
                    resultMsg.className = "text-sm text-center font-bold mt-4 text-gray-800";
                } else {
                    resultMsg.innerText = '일치하는 회원 정보가 없습니다.';
                    resultMsg.className = "text-sm text-center font-bold mt-4 text-red-500";
                }
            },
            error: function() { alert('오류가 발생했습니다.'); }
        });
    }

    // --- 비밀번호 찾기: 인증번호 전송 ---
    function sendResetCode() {
        const id = document.getElementById('findPwd_id').value;
        const email = document.getElementById('findPwd_email').value;
        const btn = document.getElementById('btnSendCode');

        if(!id || !email) {
            alert('아이디와 이메일을 입력해주세요.');
            return;
        }

        btn.disabled = true;
        btn.innerText = "전송중...";

        $.ajax({
            url: '/auth/ajax/sendPwdAuth',
            type: 'POST',
            data: { login_id: id, member_email: email },
            success: function(res) {
                if(res === 'success') {
                    alert('인증번호가 발송되었습니다.\n메일함을 확인해주세요.');

                    // UI 상태 변경
                    document.getElementById('authBox').classList.remove('hidden');
                    btn.innerText = "전송됨";
                    btn.classList.add('bg-gray-400', 'cursor-not-allowed');
                    btn.classList.remove('bg-gray-800', 'hover:bg-gray-900');

                    // 입력창 활성화 상태 유지 (수정 가능하도록 readOnly 제거)
                    // document.getElementById('findPwd_email').readOnly = false;

                    document.getElementById('authCode').value = '';
                    document.getElementById('authCode').focus();

                    isEmailSent = true;
                    startTimer(180);
                } else {
                    alert('일치하는 회원 정보가 없습니다.\n아이디와 이메일을 확인해주세요.');
                    resetEmailAuth(); // 실패 시 버튼 복구
                }
            },
            error: function() {
                alert('서버 오류가 발생했습니다.');
                resetEmailAuth();
            }
        });
    }

    // --- 인증번호 확인 ---
    function verifyResetCode() {
        const email = document.getElementById('findPwd_email').value;
        const code = document.getElementById('authCode').value;
        const authMsg = document.getElementById('authMsg');

        if(code.length < 6) {
            authMsg.innerText = "인증번호 6자리를 입력해주세요.";
            authMsg.className = "text-xs mt-2 text-red-500";
            return;
        }

        $.ajax({
            url: '/auth/ajax/verifyPwdAuth',
            type: 'POST',
            data: { member_email: email, auth_code: code },
            success: function(res) {
                if(res === true) {
                    clearInterval(timerInterval);
                    // 성공 시 이메일/아이디 수정 불가 처리
                    document.getElementById('findPwd_id').readOnly = true;
                    document.getElementById('findPwd_email').readOnly = true;
                    document.getElementById('btnSendCode').disabled = true;

                    document.getElementById('step-auth').classList.add('hidden');
                    document.getElementById('step-reset').classList.remove('hidden');
                } else {
                    authMsg.innerText = "인증번호가 일치하지 않거나 만료되었습니다.";
                    authMsg.className = "text-xs mt-2 text-red-500";
                }
            },
            error: function() { alert('오류가 발생했습니다.'); }
        });
    }

    // --- 비밀번호 재설정 ---
    function resetPasswordAction() {
        const id = document.getElementById('findPwd_id').value;
        const newPwd = document.getElementById('new_pwd').value;
        const confirmPwd = document.getElementById('confirm_pwd').value;

        if(newPwd.length < 8) {
            alert('비밀번호는 8자 이상이어야 합니다.');
            return;
        }

        if(newPwd !== confirmPwd) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        $.ajax({
            url: '/auth/ajax/resetPassword',
            type: 'POST',
            data: { login_id: id, new_pwd: newPwd },
            success: function(res) {
                if(res === 'success') {
                    alert('비밀번호가 변경되었습니다.\n로그인해주세요.');
                    location.href = '/login';
                } else if (res === 'same_password') {
                    alert('이전 비밀번호와 동일합니다.\n다른 비밀번호를 사용해주세요.');
                    document.getElementById('new_pwd').value = '';
                    document.getElementById('confirm_pwd').value = '';
                    document.getElementById('new_pwd').focus();
                } else {
                    alert('비밀번호 변경에 실패했습니다.');
                }
            },
            error: function() { alert('서버 오류가 발생했습니다.'); }
        });
    }

    // 타이머
    function startTimer(duration) {
        let timer = duration, minutes, seconds;
        const display = document.getElementById('timer');
        if (timerInterval) clearInterval(timerInterval);

        timerInterval = setInterval(function() {
            minutes = parseInt(timer / 60, 10);
            seconds = parseInt(timer % 60, 10);
            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;
            display.textContent = minutes + ":" + seconds;

            if (--timer < 0) {
                clearInterval(timerInterval);
                display.textContent = "만료";
                document.getElementById('authCode').disabled = true;
                document.getElementById('btnVerifyCode').disabled = true;
                document.getElementById('authMsg').innerText = "인증 시간이 만료되었습니다.";
            }
        }, 1000);
    }

    function autoHyphen(target) {
        target.value = target.value
            .replace(/[^0-9]/g, '')
            .replace(/^(\d{2,3})(\d{3,4})(\d{4})$/, `$1-$2-$3`);
    }
</script>

<jsp:include page="../common/footer.jsp" />