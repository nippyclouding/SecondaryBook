<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<!-- 독서모임 게시글 상세 페이지 전용 CSS -->
<style>
    .bc-post-detail-wrapper {
        max-width: 900px;
        margin: 0 auto;
        padding: 2rem 1rem;
    }

    .bc-back-link {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        color: #4299e1;
        text-decoration: none;
        font-size: 0.875rem;
        font-weight: 600;
        margin-bottom: 1.5rem;
        transition: color 0.2s;
    }

    .bc-back-link:hover {
        color: #2b6cb0;
    }

    .bc-post-card {
        background: white;
        border-radius: 0.75rem;
        box-shadow: 0 1px 3px 0 rgba(0,0,0,0.1), 0 1px 2px 0 rgba(0,0,0,0.06);
        padding: 2rem;
    }

    .bc-post-title {
        font-size: 1.875rem;
        font-weight: 700;
        color: #1a202c;
        margin: 0;
        line-height: 1.3;
        flex: 1;
    }

    .bc-post-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1rem;
        margin-bottom: 1rem;
    }

    .bc-post-actions {
        display: flex;
        gap: 0.5rem;
        flex-shrink: 0;
    }

    .bc-post-action-btn {
        display: inline-flex;
        align-items: center;
        gap: 0.375rem;
        padding: 0.5rem 0.875rem;
        border-radius: 0.375rem;
        font-size: 0.8125rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        border: none;
    }

    .bc-post-edit-btn {
        background: #edf2f7;
        color: #4a5568;
    }

    .bc-post-edit-btn:hover {
        background: #e2e8f0;
    }

    .bc-post-delete-btn {
        background: #fed7d7;
        color: #c53030;
    }

    .bc-post-delete-btn:hover {
        background: #feb2b2;
    }

    .bc-post-meta {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding-bottom: 1.5rem;
        margin-bottom: 1.5rem;
        border-bottom: 1px solid #e2e8f0;
    }

    .bc-post-meta-item {
        display: flex;
        align-items: center;
        gap: 0.375rem;
        font-size: 0.875rem;
        color: #718096;
    }

    .bc-post-content {
        color: #2d3748;
        font-size: 1rem;
        line-height: 1.75;
        white-space: normal;
        word-break: break-word;
        margin-bottom: 2rem;
    }

    .bc-post-image {
        margin: 1.5rem 0;
        border-radius: 0.5rem;
        overflow: hidden;
        max-width: 400px;
    }

    .bc-post-image img {
        width: 100%;
        height: auto;
        display: block;
        border-radius: 0.5rem;
    }

    /* 책 정보 카드 */
    .bc-book-info-card {
        display: flex;
        align-items: flex-start;
        gap: 1rem;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 0.75rem;
        padding: 1rem 1.25rem;
        margin-bottom: 1.5rem;
    }

    .bc-book-info-img {
        width: 60px;
        height: 85px;
        object-fit: cover;
        border-radius: 0.375rem;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        flex-shrink: 0;
    }

    .bc-book-info-text {
        flex: 1;
        min-width: 0;
    }

    .bc-book-info-title {
        font-size: 0.9375rem;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 0.25rem 0;
        line-height: 1.4;
    }

    .bc-book-info-author {
        font-size: 0.8125rem;
        color: #718096;
        margin: 0;
    }

    .bc-book-info-label {
        display: inline-flex;
        align-items: center;
        gap: 0.375rem;
        font-size: 0.75rem;
        color: #4299e1;
        margin-bottom: 0.5rem;
    }

    .bc-comments-section {
        margin-top: 3rem;
        padding-top: 2rem;
        border-top: 2px solid #e2e8f0;
    }

    .bc-comments-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #2d3748;
        margin-bottom: 1rem;
    }

    .bc-comments-empty {
        background: #f7fafc;
        border: 1px dashed #cbd5e0;
        border-radius: 0.5rem;
        padding: 2rem;
        text-align: center;
        color: #718096;
        font-size: 0.875rem;
    }

    .bc-comment-list {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .bc-comment-item {
        background: #f7fafc;
        border: 1px solid #e2e8f0;
        border-radius: 0.5rem;
        padding: 1rem 1.25rem;
        margin-bottom: 0.75rem;
    }

    .bc-comment-header {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        margin-bottom: 0.5rem;
    }

    .bc-comment-author {
        font-weight: 600;
        color: #2d3748;
        font-size: 0.875rem;
    }

    .bc-comment-date {
        font-size: 0.75rem;
        color: #a0aec0;
    }

    .bc-comment-content {
        color: #4a5568;
        font-size: 0.875rem;
        line-height: 1.6;
        white-space: pre-wrap;
        word-break: break-word;
        margin: 0;
    }

    .bc-comment-actions {
        display: flex;
        gap: 0.5rem;
        margin-left: auto;
    }

    .bc-comment-action-btn {
        display: inline-flex;
        align-items: center;
        gap: 0.25rem;
        padding: 0.25rem 0.5rem;
        border-radius: 0.25rem;
        font-size: 0.75rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        border: none;
        background: transparent;
    }

    .bc-comment-edit-btn {
        color: #718096;
    }

    .bc-comment-edit-btn:hover {
        background: #edf2f7;
        color: #4a5568;
    }

    .bc-comment-delete-btn {
        color: #e53e3e;
    }

    .bc-comment-delete-btn:hover {
        background: #fed7d7;
    }

    /* 댓글 수정 폼 */
    .bc-comment-edit-form {
        display: none;
        margin-top: 0.5rem;
    }

    .bc-comment-edit-form.active {
        display: block;
    }

    .bc-comment-edit-textarea {
        width: 100%;
        resize: none;
        border: 1px solid #e2e8f0;
        border-radius: 0.375rem;
        padding: 0.5rem 0.75rem;
        font-size: 0.875rem;
        font-family: inherit;
        line-height: 1.5;
        min-height: 60px;
    }

    .bc-comment-edit-textarea:focus {
        outline: none;
        border-color: #4299e1;
        box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15);
    }

    .bc-comment-edit-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.5rem;
        margin-top: 0.5rem;
    }

    .bc-comment-edit-cancel-btn {
        padding: 0.375rem 0.75rem;
        border: 1px solid #e2e8f0;
        border-radius: 0.375rem;
        background: white;
        color: #4a5568;
        font-size: 0.8125rem;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-comment-edit-cancel-btn:hover {
        background: #f7fafc;
    }

    .bc-comment-edit-submit-btn {
        padding: 0.375rem 0.75rem;
        border: none;
        border-radius: 0.375rem;
        background: #4299e1;
        color: white;
        font-size: 0.8125rem;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-comment-edit-submit-btn:hover {
        background: #2b6cb0;
    }

    .bc-error-message {
        max-width: 900px;
        margin: 2rem auto;
        padding: 1rem;
        background: #fff5f5;
        border: 1px solid #fc8181;
        border-radius: 0.5rem;
        color: #c53030;
        text-align: center;
    }

    .bc-flash-error {
        background: #fff5f5;
        border: 1px solid #fc8181;
        border-radius: 0.5rem;
        padding: 0.75rem 1rem;
        color: #c53030;
        margin-bottom: 1rem;
        font-size: 0.875rem;
    }

    .bc-comment-form-wrapper {
        position: sticky;
        bottom: 0;
        background: white;
        border-top: 1px solid #e2e8f0;
        padding: 1rem;
        margin-top: 1.5rem;
    }

    .bc-comment-form {
        display: flex;
        gap: 0.75rem;
        align-items: stretch;
    }

    .bc-comment-textarea {
        flex: 1;
        resize: none;
        border: 1px solid #e2e8f0;
        border-radius: 0.5rem;
        padding: 0.625rem 0.75rem;
        font-size: 0.875rem;
        height: 44px;
        max-height: 120px;
        font-family: inherit;
        line-height: 1.5;
        transition: height 0.2s ease;
    }

    .bc-comment-textarea:focus {
        outline: none;
        border-color: #4299e1;
        box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15);
    }

    .bc-comment-submit-btn {
        background: #4299e1;
        color: white;
        border: none;
        width: 44px;
        height: 44px;
        padding: 0;
        border-radius: 9999px;
        cursor: pointer;
        transition: background 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
    }

    .bc-comment-submit-btn svg {
        margin: 0;
    }

    .bc-comment-submit-btn:hover {
        background: #2b6cb0;
    }

    .bc-comment-submit-btn:disabled {
        background: #a0aec0;
        cursor: not-allowed;
    }

    .sr-only {
        position: absolute;
        width: 1px;
        height: 1px;
        padding: 0;
        margin: -1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
        border: 0;
    }

    /* 좋아요 버튼 스타일 - 게시글용 (크고 가운데 정렬) */
    .bc-like-btn {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.75rem 1.5rem;
        border: 2px solid #e2e8f0;
        border-radius: 9999px;
        background: white;
        color: #718096;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-like-btn:hover {
        background: #ebf8ff;
        border-color: #4299e1;
        color: #2b6cb0;
    }

    .bc-like-btn.liked {
        background: #ebf8ff;
        border-color: #4299e1;
        color: #2b6cb0;
    }

    .bc-like-btn.liked svg {
        fill: #4299e1;
    }

    .bc-like-btn svg {
        transition: fill 0.2s;
    }

    .bc-post-footer {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 1.5rem 0;
        border-top: 1px solid #e2e8f0;
        margin-top: 2rem;
    }

    /* 좋아요 버튼 스타일 - 댓글용 */
    .bc-comment-like-btn {
        display: inline-flex;
        align-items: center;
        gap: 0.375rem;
        padding: 0.375rem 0.625rem;
        border: 1px solid #e2e8f0;
        border-radius: 9999px;
        background: white;
        color: #a0aec0;
        font-size: 0.8125rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-comment-like-btn:hover {
        background: #ebf8ff;
        border-color: #4299e1;
        color: #2b6cb0;
    }

    .bc-comment-like-btn.liked {
        background: #ebf8ff;
        border-color: #4299e1;
        color: #2b6cb0;
    }

    .bc-comment-like-btn.liked svg {
        fill: #4299e1;
    }

    .bc-comment-footer {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-top: 0.75rem;
    }
</style>

<c:choose>
    <c:when test="${not empty errorMessage}">
        <!-- 에러 메시지 -->
        <div class="bc-error-message">
            <p><c:out value="${errorMessage}"/></p>
            <a href="${pageContext.request.contextPath}/bookclubs/${bookClubId}"
               style="color: #4299e1; text-decoration: none; font-weight: 600; margin-top: 0.5rem; display: inline-block;">
                독서모임으로 돌아가기
            </a>
        </div>
    </c:when>
    <c:when test="${not empty post}">
        <!-- 게시글 상세 -->
        <div class="bc-post-detail-wrapper">
            <!-- 뒤로가기 링크 -->
            <a href="${pageContext.request.contextPath}/bookclubs/${bookClubId}?tab=board" class="bc-back-link">
                <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
                독서모임으로 돌아가기
            </a>

            <!-- 게시글 카드 -->
            <div class="bc-post-card">
                <!-- 제목 + 수정/삭제 버튼 -->
                <div class="bc-post-header">
                    <h1 class="bc-post-title">${fn:escapeXml(post.board_title)}</h1>

                    <!-- 수정/삭제 버튼 (작성자 또는 모임장만 표시) -->
                    <c:if test="${post.member_seq == loginMemberSeq or isLeader}">
                        <div class="bc-post-actions">
                            <%-- 수정 버튼: 작성자만 --%>
                            <c:if test="${post.member_seq == loginMemberSeq}">
                                <button type="button" class="bc-post-action-btn bc-post-edit-btn"
                                        onclick="location.href='${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}/edit'">
                                    <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                                    </svg>
                                    수정
                                </button>
                            </c:if>
                            <%-- 삭제 버튼: 작성자 + 모임장 --%>
                            <button type="button" class="bc-post-action-btn bc-post-delete-btn"
                                    onclick="confirmDeletePost()">
                                <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                                </svg>
                                삭제
                            </button>
                        </div>
                    </c:if>
                </div>

                <!-- 메타 정보 (작성자 + 작성일) -->
                <div class="bc-post-meta">
                    <div class="bc-post-meta-item">
                        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                        </svg>
                        <span>${fn:escapeXml(post.member_nicknm)}</span>
                    </div>
                    <div class="bc-post-meta-item">
                        <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        <span>
                            <c:choose>
                                <c:when test="${not empty post.board_crt_dtm_text}">
                                    <c:out value="${post.board_crt_dtm_text}"/>
                                </c:when>
                                <c:otherwise>
                                    방금
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>

                <!-- 책 정보 (있을 경우) -->
                <c:if test="${not empty post.book_title}">
                    <div class="bc-book-info-card">
                        <c:if test="${not empty post.book_img_url}">
                            <img src="${fn:escapeXml(post.book_img_url)}" alt="책 표지" class="bc-book-info-img">
                        </c:if>
                        <div class="bc-book-info-text">
                            <div class="bc-book-info-label">
                                <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                                </svg>
                                이 글의 책
                            </div>
                            <p class="bc-book-info-title">${fn:escapeXml(post.book_title)}</p>
                            <c:if test="${not empty post.book_author}">
                                <p class="bc-book-info-author">${fn:escapeXml(post.book_author)}</p>
                            </c:if>
                        </div>
                    </div>
                </c:if>

                <!-- 본문 -->
                <div class="bc-post-content">
                    ${fn:escapeXml(post.board_cont)}
                </div>

                <!-- 첨부 이미지 (있을 경우) -->
                <c:if test="${not empty post.board_img_url}">
                    <div class="bc-post-image">
                        <c:choose>
                            <c:when test="${post.board_img_url.startsWith('http://') or post.board_img_url.startsWith('https://')}">
                                <img src="${fn:escapeXml(post.board_img_url)}" alt="첨부 이미지">
                            </c:when>
                            <c:when test="${post.board_img_url.startsWith('/')}">
                                <img src="${pageContext.request.contextPath}${fn:escapeXml(post.board_img_url)}" alt="첨부 이미지">
                            </c:when>
                        </c:choose>
                    </div>
                </c:if>

                <!-- 게시글 좋아요 버튼 -->
                <div class="bc-post-footer">
                    <button type="button" class="bc-like-btn ${post.is_liked ? 'liked' : ''}"
                            id="post-like-btn"
                            data-board-seq="${post.book_club_board_seq}"
                            onclick="toggleLike(${bookClubId}, ${post.book_club_board_seq}, 'post')">
                        <svg width="24" height="24" fill="${post.is_liked ? 'currentColor' : 'none'}" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5"/>
                        </svg>
                        <span>좋아요</span>
                        <span id="post-like-count">${post.like_count != null ? post.like_count : 0}</span>
                    </button>
                </div>

                <!-- 댓글 영역 -->
                <div class="bc-comments-section" id="comments">
                    <h2 class="bc-comments-title">
                        <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                             style="display: inline-block; vertical-align: middle; margin-right: 0.5rem;">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                        </svg>
                        댓글
                    </h2>

                    <!-- 에러 메시지 (flash) -->
                    <c:if test="${not empty errorMessage}">
                        <div class="bc-flash-error">${fn:escapeXml(errorMessage)}</div>
                    </c:if>

                    <c:choose>
                        <c:when test="${empty comments}">
                            <!-- 댓글 없음 -->
                            <div class="bc-comments-empty">
                                <p style="margin: 0;">댓글이 없습니다</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <!-- 댓글 목록 -->
                            <ul class="bc-comment-list">
                                <c:forEach var="c" items="${comments}">
                                    <li class="bc-comment-item" data-comment-id="${c.book_club_board_seq}">
                                        <div class="bc-comment-header">
                                            <span class="bc-comment-author">${fn:escapeXml(c.member_nicknm)}</span>
                                            <span class="bc-comment-date">
                                                <c:choose>
                                                    <c:when test="${not empty c.board_crt_dtm_text}">
                                                        <c:out value="${c.board_crt_dtm_text}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        방금
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                            <%-- 수정/삭제 버튼 (작성자 또는 모임장) --%>
                                            <c:if test="${c.member_seq == loginMemberSeq or isLeader}">
                                                <div class="bc-comment-actions">
                                                    <%-- 수정 버튼: 작성자만 --%>
                                                    <c:if test="${c.member_seq == loginMemberSeq}">
                                                        <button type="button" class="bc-comment-action-btn bc-comment-edit-btn"
                                                                onclick="toggleCommentEdit(${c.book_club_board_seq})"
                                                                title="수정">
                                                            <svg width="12" height="12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                                                            </svg>
                                                            수정
                                                        </button>
                                                    </c:if>
                                                    <%-- 삭제 버튼: 작성자 + 모임장 --%>
                                                    <button type="button" class="bc-comment-action-btn bc-comment-delete-btn"
                                                            onclick="confirmDeleteComment(${c.book_club_board_seq})"
                                                            title="삭제">
                                                        <svg width="12" height="12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                                                        </svg>
                                                        삭제
                                                    </button>
                                                </div>
                                            </c:if>
                                        </div>
                                        <p class="bc-comment-content" id="comment-content-${c.book_club_board_seq}">${fn:escapeXml(c.board_cont)}</p>

                                        <%-- 댓글 좋아요 버튼 --%>
                                        <div class="bc-comment-footer">
                                            <button type="button" class="bc-comment-like-btn ${c.is_liked ? 'liked' : ''}"
                                                    id="comment-like-btn-${c.book_club_board_seq}"
                                                    data-board-seq="${c.book_club_board_seq}"
                                                    onclick="toggleLike(${bookClubId}, ${c.book_club_board_seq}, 'comment')">
                                                <svg width="14" height="14" fill="${c.is_liked ? 'currentColor' : 'none'}" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                          d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5"/>
                                                </svg>
                                                <span id="comment-like-count-${c.book_club_board_seq}">${c.like_count != null ? c.like_count : 0}</span>
                                            </button>
                                        </div>

                                        <%-- 수정 폼 (작성자만) --%>
                                        <c:if test="${c.member_seq == loginMemberSeq}">
                                            <div class="bc-comment-edit-form" id="comment-edit-form-${c.book_club_board_seq}">
                                                <form action="${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}/comments/${c.book_club_board_seq}/edit"
                                                      method="post">
                                                    <sec:csrfInput />
                                                    <textarea name="commentCont" class="bc-comment-edit-textarea"
                                                              required maxlength="500">${fn:escapeXml(c.board_cont)}</textarea>
                                                    <div class="bc-comment-edit-actions">
                                                        <button type="button" class="bc-comment-edit-cancel-btn"
                                                                onclick="toggleCommentEdit(${c.book_club_board_seq})">취소</button>
                                                        <button type="submit" class="bc-comment-edit-submit-btn">저장</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </c:if>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>

                    <!-- 댓글 입력 폼 (권한 있는 사용자만 표시) -->
                    <c:if test="${canWriteComment}">
                        <div class="bc-comment-form-wrapper">
                            <form id="bcCommentForm" class="bc-comment-form"
                                  action="${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}/comments"
                                  method="post">
                                <sec:csrfInput />
                                <textarea id="bcCommentTextarea" name="commentCont" class="bc-comment-textarea"
                                          placeholder="댓글을 입력하세요..." required maxlength="500"></textarea>
                                <button id="bcCommentSubmit" type="submit" class="bc-comment-submit-btn"
                                        aria-label="댓글 전송" disabled>
                                    <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                              d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                                    </svg>
                                    <span class="sr-only">전송</span>
                                </button>
                            </form>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </c:when>
</c:choose>

<!-- 삭제 폼 (JavaScript에서 동적 제출) -->
<form id="deletePostForm" method="post" action="${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}/delete" style="display: none;">
    <sec:csrfInput />
</form>

<!-- CSRF 토큰 JS 전역 변수 (동적 폼/fetch용) -->
<script>
    window.CSRF = {
        param: '${_csrf.parameterName}',
        token: '${_csrf.token}',
        header: '${_csrf.headerName}'
    };
</script>

<script>
    function confirmDeletePost() {
        if (confirm('정말 이 게시글을 삭제하시겠습니까?')) {
            document.getElementById('deletePostForm').submit();
        }
    }

    // 댓글 수정 폼 토글
    function toggleCommentEdit(commentId) {
        var form = document.getElementById('comment-edit-form-' + commentId);
        var content = document.getElementById('comment-content-' + commentId);

        if (form.classList.contains('active')) {
            form.classList.remove('active');
            content.style.display = 'block';
        } else {
            form.classList.add('active');
            content.style.display = 'none';
        }
    }

    // 댓글 삭제 확인
    function confirmDeleteComment(commentId) {
        if (confirm('정말 이 댓글을 삭제하시겠습니까?')) {
            // 동적으로 폼 생성하여 제출
            var form = document.createElement('form');
            form.method = 'post';
            form.action = '${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}/comments/' + commentId + '/delete';
            // CSRF 토큰 hidden input 추가
            var csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = window.CSRF.param;
            csrfInput.value = window.CSRF.token;
            form.appendChild(csrfInput);
            document.body.appendChild(form);
            form.submit();
        }
    }

    // 좋아요 토글 (게시글/댓글 공통)
    function toggleLike(bookClubId, boardSeq, type) {
        var headers = {
            'Content-Type': 'application/json'
        };
        // CSRF 헤더 추가
        if (window.CSRF && window.CSRF.header && window.CSRF.token) {
            headers[window.CSRF.header] = window.CSRF.token;
        }
        fetch('${pageContext.request.contextPath}/bookclubs/' + bookClubId + '/boards/' + boardSeq + '/like', {
            method: 'POST',
            headers: headers
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'ok') {
                // 좋아요 상태 업데이트
                var btn, countSpan, svg;
                if (type === 'post') {
                    btn = document.getElementById('post-like-btn');
                    countSpan = document.getElementById('post-like-count');
                } else {
                    btn = document.getElementById('comment-like-btn-' + boardSeq);
                    countSpan = document.getElementById('comment-like-count-' + boardSeq);
                }

                if (btn && countSpan) {
                    countSpan.textContent = data.likeCount;
                    svg = btn.querySelector('svg');

                    if (data.liked) {
                        btn.classList.add('liked');
                        if (svg) svg.setAttribute('fill', 'currentColor');
                    } else {
                        btn.classList.remove('liked');
                        if (svg) svg.setAttribute('fill', 'none');
                    }
                }
            } else if (data.needLogin) {
                alert('로그인이 필요합니다.');
                redirectToLogin();
            } else {
                alert(data.message || '좋아요 처리에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('좋아요 오류:', error);
            alert('좋아요 처리 중 오류가 발생했습니다.');
        });
    }
</script>

<script defer src="${pageContext.request.contextPath}/resources/js/bookclub/bookclub_post_detail.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
