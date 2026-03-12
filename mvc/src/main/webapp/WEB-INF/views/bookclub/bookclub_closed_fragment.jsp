<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%--
    종료된 모임 안내 fragment (fetch로 로드됨)
    - header/footer/include/link/script 절대 금지
    - .bc-content-wrapper로 감싸서 UI 통일
--%>
<div class="bc-content-wrapper">
    <div class="bc-card" style="text-align: center; padding: 3rem 1.5rem;">
        <!-- 아이콘 -->
        <svg width="64" height="64" fill="none" stroke="currentColor" viewBox="0 0 24 24"
            style="margin: 0 auto 1.5rem; opacity: 0.4; color: #718096;">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>

        <!-- 안내 문구 -->
        <h3 style="font-size: 1.125rem; font-weight: 600; color: #2d3748; margin: 0 0 0.5rem 0;">
            종료된 모임입니다
        </h3>
        <p style="color: #718096; font-size: 0.875rem; margin: 0; line-height: 1.6;">
            이 모임은 종료되어 더 이상 이용할 수 없습니다.
        </p>
    </div>
</div>
