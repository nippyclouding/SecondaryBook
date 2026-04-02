<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
                <sec:csrfMetaTags />
                <jsp:include page="/WEB-INF/views/common/header.jsp" />

                <!-- 독서모임 상세 페이지 전용 CSS -->
                <link rel="stylesheet"
                    href="${pageContext.request.contextPath}/resources/css/bookclub/bookclub_detail.css">

                <c:choose>
                    <c:when test="${not empty errorMessage}">
                        <!-- 에러 메시지 -->
                        <div class="bc-error">
                            <p><c:out value="${errorMessage}"/></p>
                        </div>
                    </c:when>
                    <c:when test="${not empty bookClub}">
                        <!-- 페이지 래퍼 (data-bookclub-id 추가) -->
                        <div class="bc-page-wrapper" data-bookclub-id="${bookClub.book_club_seq}">
                            <!-- 한 덩어리 카드 래퍼 -->
                            <div class="bc-detail-shell">
                                <!-- 배너(히어로) 섹션 -->
                                <section class="bc-hero-section">
                                    <!-- 배경 이미지 또는 기본 그라데이션 -->
                                    <c:choose>
                                        <c:when test="${not empty bookClub.banner_img_url}">
                                            <c:choose>
                                                <%-- HTTP/HTTPS로 시작하면 그대로 사용 --%>
                                                    <c:when
                                                        test="${bookClub.banner_img_url.startsWith('http://') or bookClub.banner_img_url.startsWith('https://')}">
                                                        <img class="bc-hero-background" src="${fn:escapeXml(bookClub.banner_img_url)}"
                                                            alt="${fn:escapeXml(bookClub.book_club_name)} 배너">
                                                    </c:when>
                                                    <%-- /로 시작하는 상대경로면 contextPath 붙이기 --%>
                                                        <c:when test="${bookClub.banner_img_url.startsWith('/')}">
                                                            <img class="bc-hero-background"
                                                                src="${pageContext.request.contextPath}${fn:escapeXml(bookClub.banner_img_url)}"
                                                                alt="${fn:escapeXml(bookClub.book_club_name)} 배너">
                                                        </c:when>
                                                        <%-- 그 외 --%>
                                                            <c:otherwise>
                                                                <div class="bc-hero-gradient"></div>
                                                            </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <!-- 배너가 없으면 기본 그라데이션 -->
                                            <div class="bc-hero-gradient"></div>
                                        </c:otherwise>
                                    </c:choose>

                                    <!-- 오버레이 -->
                                    <div class="bc-hero-overlay">
                                        <!-- 상단: 뒤로가기 + 찜 버튼 -->
                                        <div class="bc-hero-top">
                                            <%-- <button class="bc-back-btn" onclick="history.back()" aria-label="뒤로가기">
                                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                        stroke-width="2" d="M15 19l-7-7 7-7" />
                                                </svg>
                                                </button>
                                                --%>
                                                <a href="${pageContext.request.contextPath}/bookclubs"
                                                    class="bc-back-btn">
                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round"
                                                            stroke-width="2" d="M15 19l-7-7 7-7" />
                                                    </svg>
                                                </a>
                                                <button class="bc-wish-btn ${isWished ? 'wished' : ''}" id="wishBtn"
                                                    onclick="toggleWish(${bookClub.book_club_seq})" aria-label="찜하기">
                                                    <svg fill="${isWished ? 'currentColor' : 'none'}"
                                                        stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round"
                                                            stroke-width="2"
                                                            d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                                    </svg>
                                                </button>
                                        </div>

                                        <!-- 하단: 지역 뱃지 + 모임명 + 메타 -->
                                        <div class="bc-hero-bottom">
                                            <c:if test="${not empty bookClub.book_club_rg}">
                                                <span class="bc-region-badge"><c:out value="${bookClub.book_club_rg}"/></span>
                                            </c:if>
                                            <h1 class="bc-hero-title"><c:out value="${bookClub.book_club_name}"/></h1>
                                            <div class="bc-hero-meta">
                                                <div class="bc-meta-item">
                                                    <svg width="16" height="16" fill="none" stroke="currentColor"
                                                        viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round"
                                                            stroke-width="2"
                                                            d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                                                    </svg>
                                                    <span>${empty joinedMemberCount ? 0 : joinedMemberCount} /
                                                        ${bookClub.book_club_max_member}명</span>
                                                </div>
                                                <div class="bc-meta-item">
                                                    <svg width="16" height="16" fill="none" stroke="currentColor"
                                                        viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round"
                                                            stroke-width="2"
                                                            d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                                    </svg>
                                                    <span>${wishCount} 찜</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </section>

                                <!-- 탭 네비게이션 -->
                                <div class="bc-tabs-wrapper">
                                    <nav class="bc-tabs-nav">
                                        <a href="${pageContext.request.contextPath}/bookclubs/${bookClub.book_club_seq}"
                                            class="bc-tab-link active" data-tab="home">
                                            홈
                                        </a>
                                        <a href="${pageContext.request.contextPath}/bookclubs/${bookClub.book_club_seq}/board"
                                            class="bc-tab-link" data-tab="board">
                                            게시판
                                        </a>
                                    </nav>
                                </div>

                                <!-- 본문 컨테이너 (홈 탭: 서버 렌더 + 기본 표시) -->
                                <div id="bc-home-container">
                                    <jsp:include page="/WEB-INF/views/bookclub/bookclub_detail_home.jsp" />
                                </div>

                                <!-- 본문 컨테이너 (게시판 탭: fetch로 로드) -->
                                <div id="bc-board-container" style="display:none;"></div>

                                <!-- 하단 고정 바 -->
                                <div class="bc-bottom-bar">
                                    <div class="bc-bottom-content">
                                        <div class="bc-bottom-info">
                                            <div class="bc-bottom-label">현재 참여 인원</div>
                                            <div class="bc-bottom-count">
                                                <span class="current">${empty joinedMemberCount ? 0 :
                                                    joinedMemberCount}</span>/${bookClub.book_club_max_member}명 참여 중
                                            </div>
                                        </div>
                                        <!-- CTA 버튼 분기 처리 (ctaStatus 기반) -->
                                        <div class="bc-bottom-actions">
                                            <c:choose>
                                            <%-- 1. 관리자 세션이 있는 경우: 버튼을 아예 표시하지 않음 --%>
                                                    <c:when test="${not empty adminSess}">
                                                        </c:when>

                                                <%-- 1. 비로그인 (관리자도 아니고 일반 회원도 아님) --%>
                                                    <c:when test="${(not isLogin) and (empty adminSess)}">
                                                        <button type="button" onclick="redirectToLogin()"
                                                            class="bc-btn bc-btn-secondary">
                                                            로그인 후 이용
                                                        </button>
                                                    </c:when>
                                                        <%-- 3. 로그인한 일반 회원 --%>
                                                            <c:otherwise>
                                                                <%-- 3-1. 모임장(Leader) --%>
                                                                    <c:if test="${isLeader}">
                                                                        <a href="${pageContext.request.contextPath}/bookclubs/${bookClub.book_club_seq}/manage"
                                                                            class="bc-btn bc-btn-primary"
                                                                            style="margin-right: 8px;">
                                                                            모임 관리하기
                                                                        </a>
                                                                    </c:if>

                                                                    <%-- 3-2. 일반 멤버 및 가입 희망자 (CTA 상태별 분기) --%>
                                                                        <c:choose>
                                                                            <%-- 이미 멤버인 경우 (모임장 포함 - 탈퇴 버튼은 모임장 제외하고 표시
                                                                                등 로직 필요하면 추가) --%>
                                                                                <c:when test="${ctaStatus == 'JOINED'}">
                                                                                    <button type="button"
                                                                                        id="btnLeaveBookClub"
                                                                                        class="bc-btn bc-btn-danger"
                                                                                        data-club-id="${bookClub.book_club_seq}"
                                                                                        data-is-leader="${isLeader}">
                                                                                        탈퇴하기
                                                                                    </button>
                                                                                </c:when>

                                                                                <%-- 승인 대기 --%>
                                                                                    <c:when
                                                                                        test="${ctaStatus == 'WAIT'}">
                                                                                        <button type="button"
                                                                                            class="bc-btn bc-btn-secondary"
                                                                                            disabled>승인 대기중</button>
                                                                                    </c:when>

                                                                                    <%-- 거절됨 --%>
                                                                                        <c:when
                                                                                            test="${ctaStatus == 'REJECTED'}">
                                                                                            <button type="button"
                                                                                                id="btnOpenApplyModal"
                                                                                                class="bc-btn bc-btn-primary">다시
                                                                                                신청하기</button>
                                                                                        </c:when>

                                                                                        <%-- 미가입 (신청하기) --%>
                                                                                            <c:otherwise>
                                                                                                <button type="button"
                                                                                                    id="btnOpenApplyModal"
                                                                                                    class="bc-btn bc-btn-primary">가입
                                                                                                    신청하기</button>
                                                                                            </c:otherwise>
                                                                        </c:choose>
                                                            </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div><!-- /.bc-detail-shell -->
                        </div>
                    </c:when>
                    <c:otherwise>
                        <!-- bookClub도 errorMessage도 없는 경우 -->
                        <div class="bc-error">
                            <p>독서모임을 찾을 수 없습니다.</p>
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- contextPath를 JS에 전달 -->
                <script>
                    window.__CTX = "${pageContext.request.contextPath}";
                    // CSRF 토큰 전역 변수 (동적 폼용)
                    window.CSRF = {
                        param: '${_csrf.parameterName}',
                        token: '${_csrf.token}',
                        header: '${_csrf.headerName}'
                    };

                    // 게시글 삭제 확인 (게시판 탭 fragment에서 사용)
                    function confirmDeletePost(bookClubId, postId) {
                        if (confirm('정말 이 게시글을 삭제하시겠습니까?')) {
                            // 동적으로 폼 생성하여 제출
                            const form = document.createElement('form');
                            form.method = 'post';
                            form.action = window.__CTX + '/bookclubs/' + bookClubId + '/posts/' + postId + '/delete';
                            // CSRF 토큰 hidden input 추가
                            const csrfInput = document.createElement('input');
                            csrfInput.type = 'hidden';
                            csrfInput.name = window.CSRF.param;
                            csrfInput.value = window.CSRF.token;
                            form.appendChild(csrfInput);
                            document.body.appendChild(form);
                            form.submit();
                        }
                    }

                    // 찜 토글
                    function toggleWish(clubSeq) {
                        // CSRF 토큰 가져오기
                        var csrfToken = document.querySelector('meta[name="_csrf"]');
                        var csrfHeader = document.querySelector('meta[name="_csrf_header"]');

                        var headers = {
                            'Content-Type': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest'
                        };

                        if (csrfToken && csrfHeader) {
                            headers[csrfHeader.getAttribute('content')] = csrfToken.getAttribute('content');
                        } else {
                            console.warn('[toggleWish] CSRF 토큰을 찾을 수 없습니다. 요청을 계속 진행합니다.');
                        }

                        fetch(window.__CTX + '/bookclubs/' + clubSeq + '/wish', {
                            method: 'POST',
                            headers: headers
                        })
                            .then(function (res) { return res.json(); })
                            .then(function (data) {
                                if (data.needLogin) {
                                    alert('로그인이 필요합니다.');
                                    redirectToLogin();
                                    return;
                                }
                                if (data.status === 'ok') {
                                    var btn = document.getElementById('wishBtn');
                                    var svg = btn.querySelector('svg');
                                    var wishCountSpan = document.querySelector('.bc-meta-item span');

                                    if (data.wished) {
                                        btn.classList.add('wished');
                                        svg.setAttribute('fill', 'currentColor');
                                    } else {
                                        btn.classList.remove('wished');
                                        svg.setAttribute('fill', 'none');
                                    }

                                    // 찜 개수 업데이트 (하단 메타 정보)
                                    var wishCountEl = document.querySelector('.bc-hero-meta .bc-meta-item:last-child span');
                                    if (wishCountEl) {
                                        wishCountEl.textContent = data.wishCount + ' 찜';
                                    }
                                } else {
                                    alert(data.message || '오류가 발생했습니다.');
                                }
                            })
                            .catch(function (err) {
                                console.error('찜 토글 실패:', err);
                                alert('오류가 발생했습니다.');
                            });
                    }
                </script>

                <!-- 독서모임 상세 페이지 전용 JS -->
                <script defer
                    src="${pageContext.request.contextPath}/resources/js/bookclub/bookclub_detail.js"></script>

                <!-- 가입 신청 모달 -->
                <c:if test="${not empty bookClub}">
                    <div id="applyModal" class="bc-apply-modal">
                        <div class="bc-apply-modal-overlay"></div>
                        <div class="bc-apply-modal-container">
                            <h2 class="bc-apply-modal-header">모임 가입 신청</h2>
                            <h3 class="bc-apply-modal-club-name"><c:out value="${bookClub.book_club_name}"/></h3>
                            <div class="bc-apply-modal-desc"><c:out value="${bookClub.book_club_desc}"/></div>
                            <div class="bc-apply-modal-form">
                                <label class="bc-apply-modal-label">지원 동기</label>
                                <textarea id="applyReasonInput" class="bc-apply-modal-textarea"
                                    placeholder="모임장에게 보낼 간단한 인사를 적어주세요."></textarea>
                            </div>
                            <div class="bc-apply-modal-actions">
                                <button type="button" id="btnCancelApply"
                                    class="bc-apply-btn bc-apply-btn-cancel">취소</button>
                                <button type="button" id="btnSubmitApply" class="bc-apply-btn bc-apply-btn-submit">가입
                                    신청</button>
                            </div>
                        </div>
                    </div>
                </c:if>

                <jsp:include page="/WEB-INF/views/common/footer.jsp" />