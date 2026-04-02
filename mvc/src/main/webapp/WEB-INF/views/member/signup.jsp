<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp" />

<div class="min-h-[calc(100vh-200px)] flex items-center justify-center py-16 px-4 animate-[fadeIn_0.5s_ease-out]">
    <div class="bg-white p-10 rounded-[2.5rem] shadow-[0_20px_60px_-15px_rgba(0,0,0,0.1)] w-full max-w-[480px] border border-gray-100 relative overflow-hidden">

        <div class="absolute top-[-50px] left-[-50px] w-40 h-40 bg-purple-50 rounded-full blur-3xl opacity-60 pointer-events-none"></div>

        <div class="text-center mb-8 relative z-10">
            <h2 class="text-2xl font-black text-gray-900 tracking-tight">회원가입</h2>
            <p class="text-gray-500 text-xs mt-2 font-medium">SecondHand Books의 회원이 되어보세요.</p>
        </div>

        <form action="/auth/signup" method="post" onsubmit="return validateForm()" class="space-y-5 relative z-10">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 ml-1">아이디 <span class="text-red-500">*</span></label>
                <div class="flex gap-2">
                    <input type="text" name="login_id" id="login_id" required
                           class="flex-1 px-5 py-3.5 bg-gray-50 border-0 rounded-2xl text-sm font-bold focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition outline-none placeholder-gray-400"
                           placeholder="영문 소문자/숫자 4자 이상" />
                    <button type="button" onclick="checkLoginId()" id="checkLoginIdBtn"
                            class="px-5 py-3.5 bg-white border border-gray-200 text-gray-600 rounded-2xl text-xs font-bold hover:bg-gray-50 hover:text-gray-900 transition shadow-sm whitespace-nowrap">
                        중복확인
                    </button>
                </div>
                <p id="loginIdMsg" class="text-[11px] ml-1 font-medium min-h-[1rem]"></p>
            </div>

            <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 ml-1">이메일 <span class="text-red-500">*</span></label>
                <div class="flex gap-2">
                    <input type="email" name="member_email" id="member_email" required
                           class="flex-1 px-5 py-3.5 bg-gray-50 border-0 rounded-2xl text-sm font-bold focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition outline-none placeholder-gray-400"
                           placeholder="email@example.com" />
                    <button type="button" onclick="checkEmail()" id="checkEmailBtn"
                            class="px-5 py-3.5 bg-white border border-gray-200 text-gray-600 rounded-2xl text-xs font-bold hover:bg-gray-50 hover:text-gray-900 transition shadow-sm whitespace-nowrap">
                        중복확인
                    </button>
                </div>
                <p id="emailMsg" class="text-[11px] ml-1 font-medium min-h-[1rem]"></p>

                <button type="button" id="sendAuthBtn" onclick="sendEmailAuth()"
                        class="hidden w-full py-3 bg-blue-50 text-blue-600 rounded-2xl text-xs font-bold hover:bg-blue-100 transition mt-2">
                    인증번호 발송
                </button>

                <div id="authCodeBox" class="hidden mt-3 p-4 bg-gray-50 rounded-2xl border border-gray-100">
                    <div class="flex justify-between items-center mb-2">
                        <span class="text-xs font-bold text-gray-500">인증번호 입력</span>
                        <span id="timer" class="text-xs font-bold text-red-500">03:00</span>
                    </div>
                    <div class="flex gap-2">
                        <input type="text" id="authCodeInput" maxlength="6"
                               class="flex-1 px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm font-bold outline-none text-center tracking-widest"
                               placeholder="123456" />
                        <button type="button" onclick="verifyAuthCode()" id="verifyBtn"
                                class="px-4 py-2.5 bg-gray-900 text-white text-xs font-bold rounded-xl hover:bg-black shadow-sm">
                            확인
                        </button>
                    </div>
                    <div class="flex justify-between items-center mt-2 px-1">
                        <p id="authMsg" class="text-[10px] text-gray-400 font-medium"></p>
                        <button type="button" onclick="sendEmailAuth()" class="text-[10px] font-bold text-gray-400 hover:text-gray-600 underline">
                            재전송
                        </button>
                    </div>
                </div>
            </div>

            <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 ml-1">비밀번호 <span class="text-red-500">*</span></label>
                <input type="password" name="member_pwd" id="member_pwd" required
                       class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl text-sm font-bold focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition outline-none placeholder-gray-400"
                       placeholder="8자 이상 입력" />
            </div>

            <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 ml-1">비밀번호 확인 <span class="text-red-500">*</span></label>
                <input type="password" name="confirmPwd" id="confirmPwd" required
                       class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl text-sm font-bold focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition outline-none placeholder-gray-400"
                       placeholder="비밀번호 재입력" />
            </div>

            <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 ml-1">닉네임 <span class="text-red-500">*</span></label>
                <div class="flex gap-2">
                    <input type="text" name="member_nicknm" id="member_nicknm" required
                           class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl text-sm font-bold focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition outline-none placeholder-gray-400"
                           placeholder="사용하실 별명" />
                    <button type="button" onclick="checkNicknm()" id="checkNicknmBtn"
                            class="px-5 py-3.5 bg-white border border-gray-200 text-gray-600 rounded-2xl text-xs font-bold hover:bg-gray-50 hover:text-gray-900 transition shadow-sm whitespace-nowrap">
                        중복확인
                    </button>
                </div>
                <p id="nickNmMsg" class="text-[11px] ml-1 font-medium min-h-[1rem]"></p>
            </div>

            <div class="space-y-1.5">
                <label class="text-xs font-bold text-gray-500 ml-1">휴대폰 번호 <span class="text-red-500">*</span></label>
                <input type="tel" name="member_tel_no" id="member_tel_no" required maxlength="13" oninput="autoHyphen(this)"
                       class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl text-sm font-bold focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition outline-none placeholder-gray-400"
                       placeholder="010-0000-0000" />
            </div>

            <div id="errorMsg" class="text-xs text-red-500 font-bold text-center min-h-[1rem]"></div>

            <div class="bg-gray-50 p-5 rounded-2xl border border-gray-100 space-y-3">
                <label class="flex items-center gap-3 cursor-pointer hover:text-gray-900 transition group">
                    <input type="checkbox" id="termsAll" onchange="toggleAll(this)" class="w-4 h-4 rounded text-primary-600 focus:ring-primary-500 border-gray-300 transition cursor-pointer"/>
                    <span class="text-sm font-black text-gray-800 group-hover:text-primary-600 transition-colors">전체 동의하기</span>
                </label>
                <div class="border-t border-gray-200 my-1"></div>

                <div class="flex items-center justify-between text-xs font-medium text-gray-500">
                    <label class="flex items-center gap-2 cursor-pointer hover:text-gray-800 transition">
                        <input type="checkbox" name="terms1" class="terms-checkbox w-3.5 h-3.5 rounded text-primary-600 focus:ring-primary-500 border-gray-300 cursor-pointer"/>
                        <span>[필수] 만 14세 이상입니다</span>
                    </label>
                </div>

                <div class="flex items-center justify-between text-xs font-medium text-gray-500">
                    <label class="flex items-center gap-2 cursor-pointer hover:text-gray-800 transition">
                        <input type="checkbox" name="terms2" class="terms-checkbox w-3.5 h-3.5 rounded text-primary-600 focus:ring-primary-500 border-gray-300 cursor-pointer"/>
                        <span>[필수] 이용약관 동의</span>
                    </label>
                    <button type="button" onclick="openModal('service')" class="text-gray-400 underline hover:text-primary-600 transition">보기</button>
                </div>

                <div class="flex items-center justify-between text-xs font-medium text-gray-500">
                    <label class="flex items-center gap-2 cursor-pointer hover:text-gray-800 transition">
                        <input type="checkbox" name="terms3" class="terms-checkbox w-3.5 h-3.5 rounded text-primary-600 focus:ring-primary-500 border-gray-300 cursor-pointer"/>
                        <span>[필수] 개인정보 수집 및 이용 동의</span>
                    </label>
                    <button type="button" onclick="openModal('privacy')" class="text-gray-400 underline hover:text-primary-600 transition">보기</button>
                </div>
            </div>

            <button type="submit" class="w-full bg-primary-600 text-white py-4 rounded-2xl font-bold text-sm hover:bg-primary-700 hover:-translate-y-0.5 shadow-lg transition-all duration-300">
                회원가입 완료
            </button>
        </form>

        <div class="mt-8 text-center">
            <p class="text-xs text-gray-400 font-medium">
                이미 계정이 있으신가요?
                <a href="/login" class="text-gray-800 font-bold hover:underline ml-1">로그인</a>
            </p>
        </div>
    </div>
</div>

<div id="termsModal" class="hidden fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm transition-opacity" onclick="closeModal()"></div>
    <div class="bg-white w-full max-w-md rounded-[2rem] shadow-2xl overflow-hidden relative z-10 transform transition-all scale-100 max-h-[80vh] flex flex-col">
        <div class="p-5 border-b border-gray-100 flex justify-between items-center bg-white/80 backdrop-blur sticky top-0 z-10">
            <h3 id="modalTitle" class="text-lg font-black text-gray-900 tracking-tight">약관 상세</h3>
            <button onclick="closeModal()" class="p-2 bg-gray-100 rounded-full text-gray-500 hover:bg-gray-200 transition">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>
        </div>
        <div class="p-6 overflow-y-auto text-xs text-gray-600 leading-relaxed bg-gray-50 flex-1 custom-scrollbar" id="modalContent">
        </div>
        <div class="p-5 border-t border-gray-100 bg-white">
            <button onclick="closeModal()" class="w-full bg-gray-900 text-white py-3.5 rounded-2xl font-bold text-sm hover:bg-black transition-all shadow-md">확인했습니다</button>
        </div>
    </div>
</div>

<script>
    // 기존 변수들 ...
    let loginIdChecked = false;
    let emailChecked = false;
    let emailVerified = false;
    let timerInterval;
    let nickNmChecked = false;

    // [추가] 약관 데이터 (일반적인 표준 약관 내용)
    const termsData = {
        'service': `
            <p class="font-bold mb-2 text-gray-900">제 1 조 (목적)</p>
            <p class="mb-4">본 약관은 SecondHand Books(이하 "회사")가 제공하는 중고 서적 거래 및 관련 제반 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.</p>

            <p class="font-bold mb-2 text-gray-900">제 2 조 (회원의 의무)</p>
            <p class="mb-2">1. 회원은 관계법령, 본 약관의 규정, 이용안내 및 주의사항 등 회사가 통지하는 사항을 준수하여야 합니다.</p>
            <p class="mb-4">2. 회원은 다음 행위를 하여서는 안 됩니다.<br>
            - 회원가입 신청 또는 변경 시 허위 내용 등록<br>
            - 타인의 정보 도용<br>
            - 회사가 게시한 정보의 변경<br>
            - 회사 및 기타 제3자의 저작권 등 지적재산권에 대한 침해</p>

            <p class="font-bold mb-2 text-gray-900">제 3 조 (서비스의 제공 등)</p>
            <p class="mb-4">회사는 회원에게 다음과 같은 서비스를 제공합니다.<br>
            1. 중고 서적 판매 및 구매 중개<br>
            2. 독서 모임 커뮤니티 서비스<br>
            3. 기타 회사가 정하는 업무</p>
        `,
        'privacy': `
            <p class="font-bold mb-2 text-gray-900">1. 개인정보 수집 항목</p>
            <p class="mb-4">회사는 회원가입, 상담, 서비스 신청 등을 위해 아래와 같은 개인정보를 수집하고 있습니다.<br>
            - 필수항목: 아이디, 비밀번호, 이름, 휴대폰번호, 이메일, 닉네임</p>

            <p class="font-bold mb-2 text-gray-900">2. 개인정보의 수집 및 이용목적</p>
            <p class="mb-4">회사는 수집한 개인정보를 다음의 목적을 위해 활용합니다.<br>
            - 서비스 제공에 따른 본인 인증, 구매 및 요금 결제<br>
            - 회원 관리: 회원제 서비스 이용에 따른 본인확인, 개인식별, 불량회원의 부정 이용 방지<br>
            - 마케팅 및 광고에 활용: 신규 서비스 개발 및 특화, 이벤트 등 광고성 정보 전달</p>

            <p class="font-bold mb-2 text-gray-900">3. 개인정보의 보유 및 이용기간</p>
            <p class="mb-4">원칙적으로, 개인정보 수집 및 이용목적이 달성된 후에는 해당 정보를 지체 없이 파기합니다. 단, 관계법령의 규정에 의하여 보존할 필요가 있는 경우 회사는 아래와 같이 관계법령에서 정한 일정한 기간 동안 회원정보를 보관합니다.<br>
            - 로그인 기록: 3개월 (통신비밀보호법)<br>
            - 대금결제 및 재화 등의 공급에 관한 기록: 5년 (전자상거래 등에서의 소비자보호에 관한 법률)</p>
        `
    };

    // [추가] 모달 열기 함수
    function openModal(type) {
        const modal = document.getElementById('termsModal');
        const title = document.getElementById('modalTitle');
        const content = document.getElementById('modalContent');

        if (type === 'service') {
            title.textContent = "서비스 이용약관";
            content.innerHTML = termsData['service'];
        } else if (type === 'privacy') {
            title.textContent = "개인정보 수집 및 이용 동의";
            content.innerHTML = termsData['privacy'];
        }

        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden'; // 배경 스크롤 방지
    }

    // [추가] 모달 닫기 함수
    function closeModal() {
        const modal = document.getElementById('termsModal');
        modal.classList.add('hidden');
        document.body.style.overflow = ''; // 배경 스크롤 복구
    }

    // [추가] 모달 바깥 배경 클릭 시 닫기
    document.getElementById('termsModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeModal();
        }
    });

    document.getElementById('login_id').addEventListener('input', function() {
        loginIdChecked = false;
        const msg = document.getElementById('loginIdMsg');
        const btn = document.getElementById('checkLoginIdBtn');
        msg.textContent = '';
        btn.textContent = '중복확인';
        btn.className = 'px-5 py-3.5 bg-white border border-gray-200 text-gray-600 rounded-2xl text-xs font-bold hover:bg-gray-50 hover:text-gray-900 transition shadow-sm whitespace-nowrap';
    });

    document.getElementById('member_nicknm').addEventListener('input', function() {
        nickNmChecked = false;
        const msg = document.getElementById('nickNmMsg');
        const btn = document.getElementById('checkNicknmBtn');
        msg.textContent = '';
        btn.textContent = '중복확인';
        btn.className = 'px-5 py-3.5 bg-white border border-gray-200 text-gray-600 rounded-2xl text-xs font-bold hover:bg-gray-50 hover:text-gray-900 transition shadow-sm whitespace-nowrap';
    });

    document.getElementById('member_email').addEventListener('input', function() {
        const msg = document.getElementById('emailMsg');
        const btn = document.getElementById('checkEmailBtn');
        const sendAuthBtn = document.getElementById('sendAuthBtn');
        const authCodeBox = document.getElementById('authCodeBox');

        emailChecked = false;
        emailVerified = false;

        msg.textContent = '';
        btn.textContent = '중복확인';
        btn.disabled = false;
        btn.className = "text-xs px-3 py-2.5 rounded-sm font-bold whitespace-nowrap border bg-gray-800 text-white border-gray-800 hover:bg-gray-900 transition";

        sendAuthBtn.classList.add('hidden');
        sendAuthBtn.disabled = false;
        sendAuthBtn.textContent = "인증번호 발송";

        authCodeBox.classList.add('hidden');
        document.getElementById('authCodeInput').value = '';
        document.getElementById('authCodeInput').disabled = false;
        document.getElementById('verifyBtn').disabled = false;

        if (timerInterval) clearInterval(timerInterval);
        document.getElementById('timer').textContent = "03:00";
    });

    function checkLoginId() {
        const login_id = document.getElementById('login_id').value;
        const msg = document.getElementById('loginIdMsg');
        const btn = document.getElementById('checkLoginIdBtn');

        if (login_id.length < 4) {
            msg.textContent = '아이디는 4글자 이상이어야 합니다.';
            msg.className = 'text-xs mt-1 text-red-500';
            return;
        }

        $.ajax({
            url: '/auth/ajax/idCheck',
            type: 'GET',
            data: {login_id: login_id},
            success: function(res) {
                if (res > 0) {
                    msg.textContent = '이미 사용 중인 아이디입니다.';
                    msg.className = 'text-xs mt-1 text-red-500';
                    loginIdChecked = false;
                } else {
                    msg.textContent = '사용 가능한 아이디입니다.';
                    msg.className = 'text-xs mt-1 text-green-600';
                    loginIdChecked = true;
                    btn.textContent = '✓';
                    btn.className = 'text-xs px-3 py-2.5 rounded-sm font-bold whitespace-nowrap border border-green-500 text-green-600 bg-white';
                }
            },
            error: function() {
                alert('서버 통신 중 오류가 발생했습니다.');
            }
        })
    }

    function checkEmail() {
        const member_email = document.getElementById('member_email').value;
        const msg = document.getElementById('emailMsg');
        const btn = document.getElementById('checkEmailBtn');
        const sendAuthBtn = document.getElementById('sendAuthBtn');

        const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

        if (!emailPattern.test(member_email)) {
            msg.textContent = '올바른 이메일 형식이 아닙니다.';
            msg.className = 'text-xs mt-1 text-red-500';
            return;
        }

        $.ajax({
            url: '/auth/ajax/emailCheck',
            type: 'GET',
            data: {member_email: member_email},
            success: function(res) {
                if (res > 0) {
                    msg.textContent = '이미 사용 중인 이메일입니다.';
                    msg.className = 'text-xs mt-1 text-red-500';
                    emailChecked = false;
                } else {
                    msg.textContent = '사용 가능한 이메일입니다. 인증번호를 발송해 입력해주세요.';
                    msg.className = 'text-xs mt-1 text-green-600';
                    emailChecked = true;
                    btn.textContent = '✓';
                    btn.className = 'text-xs px-3 py-2.5 rounded-sm font-bold whitespace-nowrap border border-green-500 text-green-600 bg-white';
                    sendAuthBtn.classList.remove('hidden');
                }
            },
            error: function() {
                alert('서버 통신 중 오류가 발생했습니다.');
            }
        })
    }

    function sendEmailAuth() {
        const email = document.getElementById('member_email').value;
        if (!emailChecked) {
            alert("이메일 중복 확인을 먼저 해주세요.");
            return;
        }

        const sendAuthBtn = document.getElementById('sendAuthBtn');
        const authCodeBox = document.getElementById('authCodeBox');
        const authInput = document.getElementById('authCodeInput');
        const verifyBtn = document.getElementById('verifyBtn');
        const authMsg = document.getElementById('authMsg');

        sendAuthBtn.textContent = "전송 중..";
        sendAuthBtn.disabled = true;

        $.ajax({
            url: '/auth/ajax/sendEmail',
            type: 'GET',
            data: {email: email},
            success: function(res) {
                if (res === "success") {
                    alert("인증번호가 발송되었습니다. 메일함을 확인해주세요.");
                    sendAuthBtn.classList.add('hidden');
                    authCodeBox.classList.remove('hidden');
                    authInput.value = '';
                    authInput.disabled = false;
                    verifyBtn.disabled = false;
                    authMsg.textContent = "3분 이내에 입력해주세요.";
                    authMsg.className = "text-xs text-gray-500";
                    startTimer(180);
                } else {
                    alert("메일 발송에 실패했습니다. 이메일 주소를 확인해주세요.");
                    sendAuthBtn.textContent = "인증번호 발송";
                    sendAuthBtn.disabled = false;
                }
            },
            error: function() {
                alert("서버 오류로 메일 발송에 실패했습니다.");
                sendAuthBtn.textContent = "인증번호 발송";
                sendAuthBtn.disabled = false;
            }
        })
    }

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
                display.textContent = "시간만료";
                document.getElementById('authCodeInput').disabled = true;
                document.getElementById('verifyBtn').disabled = true;
                document.getElementById('authMsg').textContent = "인증 시간이 만료되었습니다.";
                document.getElementById('authMsg').className = "text-xs text-red-500";
            }
        }, 1000);
    }

    function verifyAuthCode() {
        const email = document.getElementById('member_email').value;
        const code = document.getElementById('authCodeInput').value;
        const authMsg = document.getElementById('authMsg');
        const authInput = document.getElementById('authCodeInput');
        const verifyBtn = document.getElementById('verifyBtn');

        if (code.length < 6) {
            authMsg.textContent = "6자리 인증번호를 입력해주세요.";
            authMsg.className = "text-xs text-red-500";
            return;
        }

        $.ajax({
            url: '/auth/ajax/checkEmailCode',
            type: 'GET',
            data: {email: email, code: code},
            success: function(res) {
                if (res) {
                    authMsg.textContent = "인증이 완료되었습니다.";
                    authMsg.className = "text-xs text-green-600 font-bold";
                    clearInterval(timerInterval);
                    document.getElementById('timer').textContent = "";
                    authInput.disabled = true;
                    verifyBtn.disabled = true;

                    document.getElementById('member_email').readOnly = true;
                    document.getElementById('checkEmailBtn').disabled = true;

                    emailVerified = true;
                } else {
                    authMsg.textContent = "인증번호가 일치하지 않거나 만료되었습니다.";
                    authMsg.className = "text-xs text-red-500";
                    emailVerified = false;
                }
            },
            error: function() {
                alert("인증 확인 중 오류가 발생했습니다.");
            }
        })
    }

    function checkNicknm() {
        const member_nicknm = document.getElementById('member_nicknm').value;
        const msg = document.getElementById('nickNmMsg');
        const btn = document.getElementById('checkNicknmBtn');

        if (member_nicknm.length < 2) {
            msg.textContent = '닉네임은 2글자 이상이어야 합니다.';
            msg.className = 'text-xs mt-1 text-red-500';
            return;
        }

        $.ajax({
            url: '/auth/ajax/nicknmCheck',
            type: 'GET',
            data: {member_nicknm: member_nicknm},
            success: function(res) {
                if (res > 0) {
                    msg.textContent = '이미 사용 중인 닉네임입니다.';
                    msg.className = 'text-xs mt-1 text-red-500';
                    nickNmChecked = false;
                } else {
                    msg.textContent = '사용 가능한 닉네임입니다.';
                    msg.className = 'text-xs mt-1 text-green-600';
                    nickNmChecked = true;
                    btn.textContent = '✓';
                    btn.className = 'text-xs px-3 py-2.5 rounded-sm font-bold whitespace-nowrap border border-green-500 text-green-600 bg-white';
                }
            }
        })
    }

    // 휴대폰 번호 자동 하이픈 (010-0000-0000)
    function autoHyphen(target) {
        target.value = target.value
            .replace(/[^0-9]/g, '') // 숫자가 아닌 문자 제거
            .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3") // 3-4-4 자리로 나눠서 하이픈 추가
            .replace(/(\-{1,2})$/g, ""); // 끝에 남은 하이픈 제거
    }

    function validateForm() {
        const errorMsg = document.getElementById('errorMsg');
        errorMsg.textContent = "";

        if (!loginIdChecked) {
            errorMsg.textContent = '아이디 중복 확인을 해주세요.';
            return false;
        }

        if (!emailChecked) {
            errorMsg.textContent = '이메일 중복 확인을 해주세요.';
            return false;
        }

        if (!emailVerified) {
            errorMsg.textContent = "이메일 인증을 완료해주세요.";
            return false;
        }

        const member_pwd = document.getElementById('member_pwd').value;
        const confirmPwd = document.getElementById('confirmPwd').value;

        if (member_pwd.length < 8) {
            errorMsg.textContent = '비밀번호는 8자 이상이어야 합니다.';
            return false;
        }

        if (member_pwd !== confirmPwd) {
            errorMsg.textContent = '비밀번호가 일치하지 않습니다.';
            return false;
        }

        const member_nicknm = document.getElementById('member_nicknm').value;
        if (member_nicknm.length < 2) {
            errorMsg.textContent = '닉네임은 2글자 이상이어야 합니다.';
            return false;
        }

        const member_tel_no = document.getElementById('member_tel_no').value;
        const telPattern = /^\d{3}-\d{4}-\d{4}$/;

        if (!member_tel_no) {
            errorMsg.textContent = '휴대폰 번호를 입력해주세요.';
            return false;
        }

        if (!telPattern.test(member_tel_no)) {
            errorMsg.textContent = '휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)';
            return false;
        }

        const terms1 = document.querySelector('input[name="terms1"]').checked;
        const terms2 = document.querySelector('input[name="terms2"]').checked;
        const terms3 = document.querySelector('input[name="terms3"]').checked;

        if (!terms1 || !terms2 || !terms3) {
            errorMsg.textContent = '필수 약관에 모두 동의해주세요.';
            return false;
        }

        return true;
    }

    function toggleAll(checkbox) {
        const checkboxes = document.querySelectorAll('.terms-checkbox');
        checkboxes.forEach(cb => cb.checked = checkbox.checked);
    }

    document.querySelectorAll('.terms-checkbox').forEach(cb => {
        cb.addEventListener('change', () => {
            const allChecked = Array.from(document.querySelectorAll('.terms-checkbox')).every(c => c.checked);
            document.getElementById('termsAll').checked = allChecked;
        });
    });
</script>

<jsp:include page="../common/footer.jsp" />