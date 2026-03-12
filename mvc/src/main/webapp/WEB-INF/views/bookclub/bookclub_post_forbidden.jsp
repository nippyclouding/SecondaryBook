<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <c:set var="ctx" value="${pageContext.request.contextPath}" />
        <jsp:include page="/WEB-INF/views/common/header.jsp" />

        <%-- 게시글 권한 없음 풀 페이지 - header/footer 포함 - 멤버가 아닌 사용자가 게시글에 직접 접근 시 표시 --%>
            <style>
                .bc-forbidden-wrapper {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 4rem 1rem;
                    text-align: center;
                }

                .bc-forbidden-card {
                    background: white;
                    border-radius: 0.75rem;
                    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
                    padding: 3rem 2rem;
                }

                .bc-forbidden-icon {
                    width: 80px;
                    height: 80px;
                    margin: 0 auto 1.5rem;
                    color: #a0aec0;
                }

                .bc-forbidden-title {
                    font-size: 1.5rem;
                    font-weight: 700;
                    color: #2d3748;
                    margin: 0 0 0.75rem 0;
                }

                .bc-forbidden-desc {
                    color: #718096;
                    font-size: 1rem;
                    line-height: 1.6;
                    margin: 0 0 2rem 0;
                }

                .bc-forbidden-actions {
                    display: flex;
                    flex-direction: column;
                    gap: 0.75rem;
                    align-items: center;
                }

                .bc-forbidden-btn {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    gap: 0.5rem;
                    padding: 0.875rem 1.75rem;
                    border: none;
                    border-radius: 0.5rem;
                    font-size: 0.9375rem;
                    font-weight: 600;
                    text-decoration: none;
                    cursor: pointer;
                    transition: background 0.2s, transform 0.1s;
                    min-width: 200px;
                }

                .bc-forbidden-btn:hover {
                    transform: translateY(-1px);
                }

                .bc-forbidden-btn-primary {
                    background: #4299e1;
                    color: white;
                }

                .bc-forbidden-btn-primary:hover {
                    background: #3182ce;
                }
            </style>

            <div class="bc-forbidden-wrapper">
                <div class="bc-forbidden-card">
                    <!-- 아이콘 -->
                    <svg class="bc-forbidden-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>

                    <!-- 안내 문구 -->
                    <h1 class="bc-forbidden-title">멤버만 볼 수 있는 게시글입니다</h1>
                    <p class="bc-forbidden-desc">
                        이 게시글은 독서모임에 가입한 멤버만 열람할 수 있습니다.<br>
                        모임에 가입하시면 게시판의 모든 글을 확인하실 수 있습니다.
                    </p>

                    <!-- 버튼 영역 -->
                    <div class="bc-forbidden-actions">
                        <%-- 독서모임 페이지로 돌아가기 (가입 신청 가능) --%>
                            <a href="${ctx}/bookclubs/${bookClubId}" class="bc-forbidden-btn bc-forbidden-btn-primary">
                                <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M15 19l-7-7 7-7" />
                                </svg>
                                독서모임 페이지로 돌아가기
                            </a>

                    </div>
                </div>
            </div>

            <jsp:include page="/WEB-INF/views/common/footer.jsp" />