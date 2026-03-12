<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <!DOCTYPE html>
            <html lang="ko">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="_csrf" content="${fn:escapeXml(_csrf.token)}">
                <meta name="_csrf_header" content="${fn:escapeXml(_csrf.headerName)}">
                <title>독서모임 만들기 - 신한북스</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bookclub.css">
            </head>

            <body>
                <jsp:include page="/WEB-INF/views/common/header.jsp" />

                <main class="bookclub-main">
                    <div class="container">
                        <div class="page-header">
                            <h1>독서모임 만들기</h1>
                            <p class="page-subtitle">새로운 독서모임을 시작해보세요</p>
                        </div>

                        <!-- 에러 메시지 영역 -->
                        <c:if test="${not empty errors}">
                            <div class="alert alert-danger" role="alert">
                                <h2 class="alert-title">입력 오류</h2>
                                <ul class="error-list">
                                    <c:forEach var="error" items="${errors}">
                                        <li>
                                            <c:out value="${error.message}" />
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/bookclubs" method="post" id="createForm"
                            class="create-form" novalidate>

                            <!-- CSRF 토큰 -->
                            <input type="hidden" name="${fn:escapeXml(_csrf.parameterName)}" value="${fn:escapeXml(_csrf.token)}">

                            <!-- 모임 이름 -->
                            <div class="form-group ${not empty errors.name ? 'has-error' : ''}">
                                <label for="name" class="form-label">
                                    모임 이름 <span class="required">*</span>
                                </label>
                                <input type="text" id="name" name="name" class="form-input" placeholder="예: 금요일 밤 북클럽"
                                    value="<c:out value='${param.name}'/>" maxlength="50" required aria-required="true"
                                    aria-describedby="nameHelp">
                                <p id="nameHelp" class="form-help">2-50자 이내로 입력해주세요.</p>
                                <c:if test="${not empty errors.name}">
                                    <p class="form-error">
                                        <c:out value="${errors.name}" />
                                    </p>
                                </c:if>
                            </div>

                            <!-- 모임 설명 -->
                            <div class="form-group ${not empty errors.desc ? 'has-error' : ''}">
                                <label for="desc" class="form-label">
                                    모임 설명 <span class="required">*</span>
                                </label>
                                <textarea id="desc" name="desc" class="form-textarea" rows="5"
                                    placeholder="모임에 대해 소개해주세요" maxlength="500" required aria-required="true"
                                    aria-describedby="descHelp"><c:out value='${param.desc}'/></textarea>
                                <p id="descHelp" class="form-help">10-500자 이내로 입력해주세요.</p>
                                <c:if test="${not empty errors.desc}">
                                    <p class="form-error">
                                        <c:out value="${errors.desc}" />
                                    </p>
                                </c:if>
                            </div>

                            <!-- 지역 -->
                            <div class="form-group ${not empty errors.region ? 'has-error' : ''}">
                                <label for="region" class="form-label">
                                    지역 <span class="required">*</span>
                                </label>
                                <select id="region" name="region" class="form-select" required aria-required="true">
                                    <option value="">지역을 선택해주세요</option>
                                    <option value="서울" ${param.region eq '서울' ? 'selected' : '' }>서울</option>
                                    <option value="경기" ${param.region eq '경기' ? 'selected' : '' }>경기</option>
                                    <option value="인천" ${param.region eq '인천' ? 'selected' : '' }>인천</option>
                                    <option value="부산" ${param.region eq '부산' ? 'selected' : '' }>부산</option>
                                    <option value="대구" ${param.region eq '대구' ? 'selected' : '' }>대구</option>
                                    <option value="광주" ${param.region eq '광주' ? 'selected' : '' }>광주</option>
                                    <option value="대전" ${param.region eq '대전' ? 'selected' : '' }>대전</option>
                                    <option value="울산" ${param.region eq '울산' ? 'selected' : '' }>울산</option>
                                    <option value="세종" ${param.region eq '세종' ? 'selected' : '' }>세종</option>
                                    <option value="강원" ${param.region eq '강원' ? 'selected' : '' }>강원</option>
                                    <option value="충북" ${param.region eq '충북' ? 'selected' : '' }>충북</option>
                                    <option value="충남" ${param.region eq '충남' ? 'selected' : '' }>충남</option>
                                    <option value="전북" ${param.region eq '전북' ? 'selected' : '' }>전북</option>
                                    <option value="전남" ${param.region eq '전남' ? 'selected' : '' }>전남</option>
                                    <option value="경북" ${param.region eq '경북' ? 'selected' : '' }>경북</option>
                                    <option value="경남" ${param.region eq '경남' ? 'selected' : '' }>경남</option>
                                    <option value="제주" ${param.region eq '제주' ? 'selected' : '' }>제주</option>
                                    <option value="온라인" ${param.region eq '온라인' ? 'selected' : '' }>온라인</option>
                                </select>
                                <c:if test="${not empty errors.region}">
                                    <p class="form-error">
                                        <c:out value="${errors.region}" />
                                    </p>
                                </c:if>
                            </div>

                            <!-- 정원 -->
                            <div class="form-group ${not empty errors.maxMember ? 'has-error' : ''}">
                                <label for="maxMember" class="form-label">
                                    정원 <span class="required">*</span>
                                </label>
                                <input type="number" id="maxMember" name="maxMember" class="form-input"
                                    placeholder="예: 10" value="<c:out value='${param.maxMember}'/>" min="2" max="100"
                                    required aria-required="true" aria-describedby="maxMemberHelp">
                                <p id="maxMemberHelp" class="form-help">2-100명 사이로 설정해주세요.</p>
                                <c:if test="${not empty errors.maxMember}">
                                    <p class="form-error">
                                        <c:out value="${errors.maxMember}" />
                                    </p>
                                </c:if>
                            </div>

                            <!-- 모임 일정 -->
                            <div class="form-group ${not empty errors.schedule ? 'has-error' : ''}">
                                <label for="schedule" class="form-label">
                                    모임 일정 <span class="required">*</span>
                                </label>
                                <input type="text" id="schedule" name="schedule" class="form-input"
                                    placeholder="예: 매주 금요일 저녁 7시" value="<c:out value='${param.schedule}'/>"
                                    maxlength="100" required aria-required="true" aria-describedby="scheduleHelp">
                                <p id="scheduleHelp" class="form-help">모임 주기와 시간을 입력해주세요.</p>
                                <c:if test="${not empty errors.schedule}">
                                    <p class="form-error">
                                        <c:out value="${errors.schedule}" />
                                    </p>
                                </c:if>
                            </div>

                            <!-- 배너 이미지 -->
                            <div class="form-group ${not empty errors.bannerImgUrl ? 'has-error' : ''}">
                                <label for="bannerImgUrl" class="form-label">
                                    배너 이미지 URL (선택)
                                </label>
                                <input type="url" id="bannerImgUrl" name="bannerImgUrl" class="form-input"
                                    placeholder="https://example.com/image.jpg"
                                    value="<c:out value='${param.bannerImgUrl}'/>" aria-describedby="bannerHelp">
                                <p id="bannerHelp" class="form-help">
                                    모임을 대표할 배너 이미지 URL을 입력하거나, 추후 추가할 수 있습니다.
                                </p>
                                <c:if test="${not empty errors.bannerImgUrl}">
                                    <p class="form-error">
                                        <c:out value="${errors.bannerImgUrl}" />
                                    </p>
                                </c:if>
                            </div>

                            <!-- 버튼 영역 -->
                            <div class="form-actions">
                                <a href="${pageContext.request.contextPath}/bookclubs" class="btn btn-secondary">
                                    취소
                                </a>
                                <button type="submit" class="btn btn-primary">
                                    모임 만들기
                                </button>
                            </div>
                        </form>
                    </div>
                </main>

                <jsp:include page="/WEB-INF/views/common/footer.jsp" />

                <script src="${pageContext.request.contextPath}/resources/js/bookclub.js"></script>
                <script>
                    // 페이지별 초기화
                    document.addEventListener('DOMContentLoaded', function () {
                        BookClub.initCreate();
                    });
                </script>
            </body>

            </html>