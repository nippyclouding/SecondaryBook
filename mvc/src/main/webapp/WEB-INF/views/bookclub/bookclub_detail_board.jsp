<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

                <style>
                    /* 게시글 카드 호버 시 액션 버튼 표시 */
                    .bc-board-card {
                        position: relative;
                        overflow: hidden;
                    }
                    .bc-board-card .bc-card-actions {
                        opacity: 0;
                        transition: opacity 0.2s ease;
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        gap: 0.5rem;
                        z-index: 10;
                        background: rgba(255, 255, 255, 0.85);
                        backdrop-filter: blur(2px);
                        pointer-events: none;
                    }
                    .bc-board-card:hover .bc-card-actions {
                        opacity: 1;
                    }
                    /* 모바일(터치 디바이스)에서는 항상 표시 */
                    @media (hover: none) {
                        .bc-board-card .bc-card-actions {
                            opacity: 1;
                            position: static;
                            background: transparent;
                            backdrop-filter: none;
                            justify-content: flex-start;
                            padding-top: 0.75rem;
                            border-top: 1px solid #e2e8f0;
                            margin-top: 0.75rem;
                        }
                    }
                    .bc-card-action-btn {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        gap: 0.375rem;
                        padding: 0.5rem 1rem;
                        border: none;
                        border-radius: 0.5rem;
                        cursor: pointer;
                        transition: all 0.2s;
                        font-size: 0.8125rem;
                        font-weight: 600;
                        pointer-events: auto;
                    }
                    .bc-card-action-btn.view {
                        background: #ebf8ff;
                        color: #3182ce;
                    }
                    .bc-card-action-btn.view:hover {
                        background: #bee3f8;
                    }
                    .bc-card-action-btn.edit {
                        background: #edf2f7;
                        color: #4a5568;
                    }
                    .bc-card-action-btn.edit:hover {
                        background: #e2e8f0;
                    }
                    .bc-card-action-btn.delete {
                        background: #fed7d7;
                        color: #c53030;
                    }
                    .bc-card-action-btn.delete:hover {
                        background: #feb2b2;
                    }
                </style>

                <!-- 게시판 탭 본문 (fragment) - header/footer 없음 -->
                <div class="bc-content-wrapper">
                    <!-- 상단: 타이틀 + 글쓰기 버튼 -->
                    <div
                        style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                        <h2 class="bc-card-title" style="margin: 0;">
                            <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                                style="display: inline-block; vertical-align: middle; margin-right: 0.5rem;">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            게시판
                        </h2>
                        <c:if test="${empty adminSess}">
                        <button type="button" onclick="location.href='${pageContext.request.contextPath}/bookclubs/${bookClub.book_club_seq}/posts'"
                            style="display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.625rem 1rem; background: #4299e1; color: white; border: none; border-radius: 0.5rem; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: background 0.2s;">
                            <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M12 4v16m8-8H4" />
                            </svg>
                            글쓰기
                        </button>
                        </c:if>
                    </div>

                    <!-- 게시글 목록 -->
                    <c:choose>
                        <c:when test="${empty boards}">
                            <!-- 빈 상태 -->
                            <div class="bc-card" style="text-align: center; padding: 3rem 1.5rem;">
                                <svg width="48" height="48" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                                    style="margin: 0 auto 1rem; opacity: 0.3;">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                                <p style="color: #a0aec0; font-size: 0.875rem; margin: 0;">등록된 게시글이 없습니다.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <!-- 게시글 카드 리스트 -->
                            <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                                <c:forEach var="board" items="${boards}">
                                    <div class="bc-card bc-board-card"
                                        style="display: flex; flex-direction: column; gap: 0; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; padding: 1rem 1.25rem;"
                                        onclick="location.href='${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${board.book_club_board_seq}'"
                                        onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)';"
                                        onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='0 1px 3px 0 rgba(0,0,0,0.1), 0 1px 2px 0 rgba(0,0,0,0.06)';">

                                        <!-- 호버 시 나타나는 액션 버튼 -->
                                        <div class="bc-card-actions">
                                            <%-- 자세히 보기: 모든 사용자 --%>
                                            <button type="button" class="bc-card-action-btn view"
                                                onclick="event.stopPropagation(); location.href='${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${board.book_club_board_seq}'"
                                                title="자세히 보기">
                                                <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                                </svg>
                                                자세히
                                            </button>
                                            <%-- 수정 버튼: 작성자만 --%>
                                            <c:if test="${board.member_seq == loginMemberSeq}">
                                                <button type="button" class="bc-card-action-btn edit"
                                                    onclick="event.stopPropagation(); location.href='${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${board.book_club_board_seq}/edit'"
                                                    title="수정">
                                                    <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                                    </svg>
                                                    수정
                                                </button>
                                            </c:if>
                                            <%-- 삭제 버튼: 작성자 + 모임장 --%>
                                            <c:if test="${board.member_seq == loginMemberSeq or isLeader}">
                                                <button type="button" class="bc-card-action-btn delete"
                                                    onclick="event.stopPropagation(); confirmDeletePost(${bookClubId}, ${board.book_club_board_seq})"
                                                    title="삭제">
                                                    <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                    </svg>
                                                    삭제
                                                </button>
                                            </c:if>
                                        </div>

                                        <!-- 컨텐츠 영역 -->
                                        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem;">
                                            <!-- 왼쪽: 텍스트 영역 -->
                                            <div style="flex: 1; min-width: 0;">
                                                <!-- 제목 -->
                                                <h3 style="font-size: 1rem; font-weight: 600; color: #2d3748; margin: 0 0 0.375rem 0; line-height: 1.4; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                    ${fn:escapeXml(board.board_title)}
                                                </h3>

                                                <!-- 본문 미리보기 -->
                                                <c:if test="${not empty board.board_cont}">
                                                    <p style="color: #718096; font-size: 0.8125rem; line-height: 1.5; margin: 0 0 0.5rem 0; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical;">
                                                        ${fn:escapeXml(board.board_cont)}
                                                    </p>
                                                </c:if>

                                                <!-- 메타 정보 (작성자 + 작성일 + 댓글 수 + 좋아요 수) -->
                                                <div style="display: flex; align-items: center; gap: 0.625rem; font-size: 0.75rem; color: #a0aec0;">
                                                    <span>${fn:escapeXml(board.member_nicknm)}</span>
                                                    <span>·</span>
                                                    <span>
                                                        <c:choose>
                                                            <c:when test="${not empty board.board_crt_dtm_text}">
                                                                ${board.board_crt_dtm_text}
                                                            </c:when>
                                                            <c:otherwise>방금</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <span style="display: flex; align-items: center; gap: 0.25rem; color: #4299e1;">
                                                        <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                                                        </svg>
                                                        ${empty board.comment_count ? 0 : board.comment_count}
                                                    </span>
                                                    <span style="display: flex; align-items: center; gap: 0.25rem; color: #2b6cb0;">
                                                        <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                                d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                                                        </svg>
                                                        ${empty board.like_count ? 0 : board.like_count}
                                                    </span>
                                                </div>
                                            </div>

                                            <!-- 오른쪽: 썸네일 -->
                                            <c:choose>
                                                <%-- 1순위: 첨부 사진 --%>
                                                <c:when test="${not empty board.board_img_url}">
                                                    <div style="flex-shrink: 0; width: 80px; height: 80px; border-radius: 0.5rem; overflow: hidden; background: #f7fafc;">
                                                        <c:choose>
                                                            <c:when test="${board.board_img_url.startsWith('http://') or board.board_img_url.startsWith('https://')}">
                                                                <img src="${fn:escapeXml(board.board_img_url)}" alt="게시글 이미지"
                                                                    style="width: 100%; height: 100%; object-fit: cover;">
                                                            </c:when>
                                                            <c:otherwise>
                                                                <img src="${pageContext.request.contextPath}${fn:escapeXml(board.board_img_url)}" alt="게시글 이미지"
                                                                    style="width: 100%; height: 100%; object-fit: cover;">
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </c:when>
                                                <%-- 2순위: 책 API 사진 --%>
                                                <c:when test="${not empty board.book_img_url}">
                                                    <div style="flex-shrink: 0; width: 60px; height: 80px; border-radius: 0.375rem; overflow: hidden; background: #f7fafc; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
                                                        <img src="${fn:escapeXml(board.book_img_url)}" alt="책 표지"
                                                            style="width: 100%; height: 100%; object-fit: cover;">
                                                    </div>
                                                </c:when>
                                                <%-- 3순위: 없음 --%>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                            <!-- confirmDeletePost 함수는 메인 페이지(bookclub_detail.jsp)에 정의됨 -->
                        </c:otherwise>
                    </c:choose>
                </div>
