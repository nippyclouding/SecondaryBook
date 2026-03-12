<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--
    게시판 권한 없음 fragment (fetch로 로드됨)
    - header/footer/include/link/script 절대 금지
    - .bc-content-wrapper로 감싸서 UI 통일
--%>
<div class="bc-content-wrapper">
    <div class="bc-card" style="text-align: center; padding: 3rem 1.5rem;">
        <!-- 아이콘 -->
        <svg width="64" height="64" fill="none" stroke="currentColor" viewBox="0 0 24 24"
            style="margin: 0 auto 1.5rem; opacity: 0.4; color: #718096;">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
        </svg>

        <!-- 안내 문구 -->
        <h3 style="font-size: 1.125rem; font-weight: 600; color: #2d3748; margin: 0 0 0.5rem 0;">
            멤버만 볼 수 있는 공간입니다
        </h3>
        <p style="color: #718096; font-size: 0.875rem; margin: 0 0 1.5rem 0; line-height: 1.6;">
            게시판은 독서모임에 가입한 멤버만 이용할 수 있습니다.
        </p>

        <!-- 버튼 영역 -->
        <div style="display: flex; flex-direction: column; gap: 0.75rem; align-items: center;">
            <%-- 비로그인 시 로그인 버튼 표시 --%>
            <c:if test="${not isLogin}">
                <button type="button" onclick="redirectToLogin()"
                   style="display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem;
                          padding: 0.75rem 1.5rem; background: #4299e1; color: white;
                          border: none; border-radius: 0.5rem; font-size: 0.875rem; font-weight: 600;
                          text-decoration: none; cursor: pointer; transition: background 0.2s; min-width: 140px;">
                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
                    </svg>
                    로그인
                </button>
            </c:if>

            <%-- 돌아가기 버튼 --%>
            <a href="${ctx}/bookclubs/${bookClubId}"
               style="display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem;
                      padding: 0.75rem 1.5rem; background: #e2e8f0; color: #4a5568;
                      border: none; border-radius: 0.5rem; font-size: 0.875rem; font-weight: 600;
                      text-decoration: none; cursor: pointer; transition: background 0.2s; min-width: 140px;">
                <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M15 19l-7-7 7-7"/>
                </svg>
                돌아가기
            </a>
        </div>
    </div>
</div>
