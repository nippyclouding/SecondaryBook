<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

  <%-- 이미 로그인된 경우 홈으로 리다이렉트 --%>
  <c:if test="${not empty sessionScope.loginSess}">
      <c:redirect url="/"/>
  </c:if>

  <%-- 캐시 방지 --%>
  <%
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setDateHeader("Expires", 0);
  %>
<jsp:include page="../common/header.jsp" />

<%-- JavaScript로 세션 체크 (캐시된 페이지에서도 동작) --%>
<script>
(function() {
    fetch('/api/session-check', {
        method: 'GET',
        credentials: 'same-origin'
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.loggedIn) {
            window.location.replace('/');
        }
    })
    .catch(function(err) {
        console.log('세션 체크 실패:', err);
    });
})();
</script>

<div class="min-h-[calc(100vh-200px)] flex items-center justify-center py-12 px-4 animate-[fadeIn_0.5s_ease-out]">
    <div class="bg-white p-10 rounded-[2.5rem] shadow-[0_20px_60px_-15px_rgba(0,0,0,0.1)] w-full max-w-[420px] border border-gray-100 relative overflow-hidden">

        <div class="absolute top-[-50px] right-[-50px] w-32 h-32 bg-primary-50 rounded-full blur-3xl opacity-60 pointer-events-none"></div>
        <div class="absolute bottom-[-30px] left-[-30px] w-24 h-24 bg-blue-50 rounded-full blur-2xl opacity-60 pointer-events-none"></div>

        <div class="text-center mb-10 relative z-10">
            <h1 class="text-3xl font-black text-gray-900 tracking-tighter cursor-pointer inline-flex items-center gap-1 group" onclick="location.href='/home'">
                SecondHand<span class="text-primary-600 group-hover:text-primary-700 transition-colors">Books</span>
            </h1>
            <p class="text-gray-500 text-sm mt-2 font-medium">금융처럼 안전한 중고 서적 거래</p>
        </div>

        <form action="/login" method="post" class="space-y-5 relative z-10">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${not empty redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${redirect}'/>"/>
            </c:if>

            <div class="space-y-3">
                <div>
                    <input type="text" name="login_id" placeholder="아이디" required
                           class="w-full px-5 py-4 bg-gray-50 border-0 rounded-2xl text-sm font-bold text-gray-900 placeholder-gray-400 focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition-all outline-none shadow-sm" />
                </div>
                <div>
                    <input type="password" name="member_pwd" placeholder="비밀번호" required
                           class="w-full px-5 py-4 bg-gray-50 border-0 rounded-2xl text-sm font-bold text-gray-900 placeholder-gray-400 focus:bg-white focus:ring-2 focus:ring-primary-500/20 transition-all outline-none shadow-sm" />
                </div>
            </div>

            <div class="flex justify-between items-center text-xs font-bold text-gray-500 px-1">
                <label class="flex items-center gap-2 cursor-pointer hover:text-gray-900 transition-colors select-none">
                    <input type="checkbox" name="remember" class="w-4 h-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500 transition cursor-pointer"/>
                    <span>로그인 상태 유지</span>
                </label>
                <div class="flex gap-3">
                    <a href="/findAccount" class="hover:text-primary-600 transition-colors">아이디 찾기</a>
                    <span class="text-gray-300">|</span>
                    <a href="/findAccount" class="hover:text-primary-600 transition-colors">비밀번호 찾기</a>
                </div>
            </div>

            <button type="submit" class="w-full bg-gray-900 text-white py-4 rounded-2xl font-bold text-sm hover:bg-black hover:shadow-lg hover:-translate-y-0.5 transition-all duration-300 shadow-md">
                로그인
            </button>
        </form>

        <div class="relative mt-10 mb-8">
            <div class="absolute inset-0 flex items-center">
                <div class="w-full border-t border-gray-100"></div>
            </div>
            <div class="relative flex justify-center text-xs">
                <span class="px-3 bg-white text-gray-400 font-bold">SNS 계정으로 시작하기</span>
            </div>
        </div>

        <div class="space-y-3 relative z-10">
            <a href="https://kauth.kakao.com/oauth/authorize?client_id=${kakaoClientId}&redirect_uri=${kakaoRedirectUri}&response_type=code"
               class="w-full py-3.5 bg-[#FEE500] text-[#3c1e1e] rounded-2xl font-bold text-sm hover:bg-[#fdd835] hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2.5 shadow-sm hover:shadow-md">
                <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3C7.58 3 4 5.28 4 8.1c0 1.77 1.43 3.34 3.73 4.25-.16.59-.57 2.06-.66 2.4-.1.38.14.38.29.28.2.13 2.82-1.92 3.96-2.7.22.03.45.04.68.04 4.42 0 8-2.28 8-5.1S16.42 3 12 3z"/></svg>
                카카오로 시작하기
            </a>
            <a href="https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${naverClientId}&redirect_uri=${naverRedirectUri}&state=1234"
               class="w-full py-3.5 bg-[#03C75A] text-white rounded-2xl font-bold text-sm hover:bg-[#02b351] hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2.5 shadow-sm hover:shadow-md">
                <span class="font-black text-[10px] border-[1.5px] border-white rounded-full w-4 h-4 flex items-center justify-center leading-none pt-0.5">N</span>
                네이버로 시작하기
            </a>
        </div>

        <div class="mt-10 text-center">
            <p class="text-xs text-gray-400 font-medium">
                아직 회원이 아니신가요?
                <a href="/signup" class="text-primary-600 font-bold hover:underline ml-1">회원가입</a>
            </p>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />