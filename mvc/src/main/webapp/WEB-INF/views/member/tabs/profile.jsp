<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="bg-white rounded-[2rem] border border-gray-100 p-8 shadow-sm">
    <div class="flex justify-between items-center mb-8 pb-4 border-b border-gray-100">
        <h2 class="text-xl font-black text-gray-900 tracking-tight">내 프로필</h2>
        <button id="btn-edit-profile" onclick="ProfileTab.toggleEditMode(true)"
                class="flex items-center gap-1.5 text-xs font-bold text-gray-600 bg-gray-50 px-4 py-2 rounded-full hover:bg-gray-100 transition shadow-sm">
            <i data-lucide="edit-2" class="w-3.5 h-3.5"></i> 수정하기
        </button>
    </div>

    <div id="view-mode" class="space-y-8 animate-[fadeIn_0.3s_ease-out]">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
                <span class="text-xs font-bold text-gray-400 block mb-2 uppercase tracking-wider">닉네임</span>
                <span class="text-gray-900 font-bold text-lg"><c:out value="${sessionScope.loginSess.member_nicknm}"/></span>
            </div>
            <div>
                <span class="text-xs font-bold text-gray-400 block mb-2 uppercase tracking-wider">이메일</span>
                <span class="text-gray-900 font-bold text-lg"><c:out value="${sessionScope.loginSess.member_email}"/></span>
            </div>
            <div>
                <span class="text-xs font-bold text-gray-400 block mb-2 uppercase tracking-wider">휴대폰 번호</span>
                <span class="text-gray-900 font-bold text-lg tracking-tight"><c:choose><c:when test="${not empty sessionScope.loginSess.member_tel_no}"><c:out value="${sessionScope.loginSess.member_tel_no}"/></c:when><c:otherwise>미등록</c:otherwise></c:choose></span>
            </div>
        </div>

        <div class="pt-8 mt-4 border-t border-gray-50">
            <button onclick="ProfileTab.deleteAccount()"
                    class="flex items-center gap-2 text-xs font-bold text-gray-400 hover:text-red-500 transition-colors">
                <i data-lucide="trash-2" class="w-4 h-4"></i> 회원 탈퇴
            </button>
        </div>
    </div>

    <form id="edit-mode" action="/member/update" method="post" onsubmit="return ProfileTab.validateForm()" class="hidden max-w-md space-y-6 animate-[fadeIn_0.3s_ease-out]">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="space-y-1.5">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">닉네임 <span class="text-red-500">*</span></label>
            <div class="flex gap-2">
                <input type="text" id="member_nicknm" name="member_nicknm"
                       value="<c:out value='${sessionScope.loginSess.member_nicknm}'/>"
                       maxlength="10"
                       oninput="ProfileTab.onNickChange()"
                       class="flex-1 px-5 py-3.5 bg-gray-50 border-0 rounded-2xl focus:bg-white focus:ring-2 focus:ring-primary-500/20 text-sm font-bold transition outline-none"
                       placeholder="2~10자 이내" />
                <button type="button" id="btn-check-nick" onclick="ProfileTab.checkDuplicate()"
                        class="px-5 py-3.5 bg-white border border-gray-200 text-gray-600 text-xs font-bold rounded-2xl hover:bg-gray-50 hover:text-gray-900 transition shadow-sm whitespace-nowrap">
                    중복확인
                </button>
            </div>
            <p id="nickMsg" class="text-xs ml-1 font-medium min-h-[1rem]"></p>
        </div>

        <div class="space-y-1.5">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">이메일</label>
            <input type="email" name="member_email" value="<c:out value='${sessionScope.loginSess.member_email}'/>" readonly
                   class="w-full px-5 py-3.5 bg-gray-100/50 border-0 rounded-2xl text-gray-400 text-sm font-bold cursor-not-allowed outline-none" />
        </div>

        <div class="space-y-1.5">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider ml-1">휴대폰 번호 <span class="text-red-500">*</span></label>
            <input type="tel" name="member_tel_no" id="member_tel_no"
                   value="<c:out value='${sessionScope.loginSess.member_tel_no}'/>"
                   placeholder="010-0000-0000"
                   maxlength="13"
                   oninput="ProfileTab.autoHyphen(this)"
                   class="w-full px-5 py-3.5 bg-gray-50 border-0 rounded-2xl focus:bg-white focus:ring-2 focus:ring-primary-500/20 text-sm font-bold transition outline-none" />
        </div>

        <div class="flex gap-3 pt-4">
            <button type="button" onclick="ProfileTab.toggleEditMode(false)"
                    class="flex-1 py-4 bg-white border border-gray-200 rounded-2xl text-sm font-bold text-gray-600 hover:bg-gray-50 transition shadow-sm">취소</button>
            <button type="submit"
                    class="flex-1 py-4 bg-gray-900 text-white rounded-2xl text-sm font-bold hover:bg-black shadow-lg hover:shadow-xl hover:-translate-y-0.5 transition-all">
                저장하기
            </button>
        </div>
    </form>
</div>

<script>
    (function() { // IIFE
        let isNickChecked = true;

        const originalNick = "${fn:escapeXml(sessionScope.loginSess.member_nicknm)}";

        const actions = {
            toggleEditMode: (isEdit) => {
                const view = document.getElementById('view-mode');
                const edit = document.getElementById('edit-mode');
                const btn = document.getElementById('btn-edit-profile');

                if (isEdit) {
                    view.classList.add('hidden');
                    edit.classList.remove('hidden');
                    btn.classList.add('hidden');

                    const nickInput = document.getElementById('member_nicknm');
                    if(nickInput) nickInput.value = originalNick;
                    document.getElementById('nickMsg').textContent = "";
                    isNickChecked = true;
                } else {
                    view.classList.remove('hidden');
                    edit.classList.add('hidden');
                    btn.classList.remove('hidden');
                }
            },

            onNickChange: () => {
                const currentVal = document.getElementById('member_nicknm').value;
                const btn = document.getElementById('btn-check-nick');
                const msg = document.getElementById('nickMsg');

                if (currentVal === originalNick) {
                    isNickChecked = true;
                    msg.textContent = "현재 사용 중인 닉네임입니다.";
                    msg.className = "text-xs ml-1 font-bold text-green-600";
                    btn.classList.add('bg-gray-100', 'text-gray-400', 'cursor-not-allowed');
                    btn.disabled = true;
                } else {
                    isNickChecked = false;
                    msg.textContent = "중복 확인이 필요합니다.";
                    msg.className = "text-xs ml-1 font-bold text-red-500";
                    btn.classList.remove('bg-gray-100', 'text-gray-400', 'cursor-not-allowed');
                    btn.classList.add('bg-white', 'text-gray-600', 'hover:bg-gray-50');
                    btn.disabled = false;
                }
            },

            checkDuplicate: () => {
                const nickInput = document.getElementById('member_nicknm');
                const nick = nickInput.value.trim();
                const msg = document.getElementById('nickMsg');

                // ★ 추가: 원래 닉네임이면 서버 호출 없이 바로 통과
                if (nick === originalNick) {
                    msg.textContent = "현재 사용 중인 닉네임입니다.";
                    msg.className = "text-xs ml-1 font-bold text-green-600";
                    isNickChecked = true;
                    return;
                }
                if (nick.length < 2) {
                    alert("닉네임은 2글자 이상이어야 합니다.");
                    return;
                }
                $.ajax({
                    url: '/auth/ajax/nicknmCheck',
                    type: 'GET',
                    data: { member_nicknm: nick },
                    success: function(res) {
                        if (res > 0) {
                            msg.textContent = "이미 사용 중인 닉네임입니다.";
                            msg.className = "text-xs ml-1 font-bold text-red-500";
                            isNickChecked = false;
                        } else {
                            msg.textContent = "사용 가능한 닉네임입니다.";
                            msg.className = "text-xs ml-1 font-bold text-green-600";
                            isNickChecked = true;
                        }
                    },
                    error: function() { alert("서버 오류가 발생했습니다."); }
                });
            },

            autoHyphen: (target) => {
                target.value = target.value
                    .replace(/[^0-9]/g, '')
                    .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
                    .replace(/(\-{1,2})$/g, "");
            },

            validateForm: () => {
                const nick = document.getElementById('member_nicknm').value.trim();
                const tel = document.getElementById('member_tel_no').value.trim();

                if (nick.length < 2) { alert("닉네임은 2글자 이상이어야 합니다."); return false; }
                if (!isNickChecked) { alert("닉네임 중복 확인을 해주세요."); return false; }
                if (!tel) { alert("휴대폰 번호를 입력해주세요."); return false; }
                const telPattern = /^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$/;
                if (!telPattern.test(tel)) { alert("올바른 휴대폰 번호 형식이 아닙니다."); return false; }

                return true;
            },

            deleteAccount: () => {
                if (confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = '/member/delete';
                    const csrf = getCsrfToken();
                    if (csrf.token) {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = '_csrf';
                        input.value = csrf.token;
                        form.appendChild(input);
                    }
                    document.body.appendChild(form);
                    form.submit();
                }
            }
        };

        window.ProfileTab = actions;
        if(window.lucide) lucide.createIcons();
    })();
</script>