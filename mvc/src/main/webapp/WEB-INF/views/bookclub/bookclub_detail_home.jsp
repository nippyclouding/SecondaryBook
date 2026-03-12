<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 홈 탭 본문 (fragment) -->
<div class="bc-content-wrapper">
    <!-- 카드1: 모임 소개 -->
    <div class="bc-card">
        <h2 class="bc-card-title">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            모임 소개
        </h2>
        <p class="bc-intro-text"><c:out value="${empty bookClub.book_club_desc ? '소개글이 없습니다.' : fn:trim(bookClub.book_club_desc)}" /></p>

        <c:if test="${not empty bookClub.book_club_schedule}">
            <div class="bc-schedule-box">
                <div class="bc-schedule-label">정기 모임 일정</div>
                <div class="bc-schedule-text"><c:out value="${bookClub.book_club_schedule}"/></div>
            </div>
        </c:if>
    </div>

    <!-- 카드2: 함께하는 멤버 -->
    <div class="bc-card">
        <h2 class="bc-card-title">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
            </svg>
            함께하는 멤버 <span style="font-size: 0.875rem; font-weight: 400; color: #718096;">(${joinedMemberCount}명)</span>
        </h2>

        <c:choose>
            <c:when test="${empty members}">
                <div style="text-align: center; padding: 2rem; color: #a0aec0; font-size: 0.875rem;">
                    아직 멤버가 없습니다.
                </div>
            </c:when>
            <c:otherwise>
                <div class="bc-members-grid">
                    <c:forEach var="member" items="${members}">
                        <div class="bc-member-item">
                            <div class="bc-member-avatar">
                                <c:choose>
                                    <c:when test="${not empty member.profileImgUrl}">
                                        <img src="${fn:escapeXml(member.profileImgUrl)}" alt="${fn:escapeXml(member.nickname)}"
                                             style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;">
                                    </c:when>
                                    <c:otherwise>
                                        <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                                        </svg>
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${member.leaderYn == 'Y'}">
                                    <span class="bc-leader-badge">모임장</span>
                                </c:if>
                            </div>
                            <div class="bc-member-name">${fn:escapeXml(member.nickname)}</div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
