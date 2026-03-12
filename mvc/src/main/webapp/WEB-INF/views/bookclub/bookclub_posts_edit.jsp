<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<!-- 독서모임 상세 페이지 전용 CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/bookclub/bookclub_detail.css">

<style>
    /* 글쓰기 폼 전용 스타일 */
    .bc-post-form-wrapper {
        max-width: 900px;
        margin: 0 auto;
        padding: 24px 20px 40px;
    }

    .bc-post-form-shell {
        background: #fff;
        border-radius: 18px;
        overflow: hidden;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    }

    /* 상단 헤더 */
    .bc-post-header {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 20px 24px;
        border-bottom: 1px solid #e2e8f0;
    }

    .bc-post-back-btn {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: #f7fafc;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-post-back-btn:hover {
        background: #e2e8f0;
    }

    .bc-post-back-btn svg {
        width: 20px;
        height: 20px;
        color: #4a5568;
    }

    .bc-post-header-title {
        font-size: 1.25rem;
        font-weight: 700;
        color: #2d3748;
        margin: 0;
    }

    /* 폼 본문 */
    .bc-post-form-body {
        padding: 24px;
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    /* 입력 필드 공통 */
    .bc-post-input {
        width: 100%;
        padding: 16px;
        background: #fff;
        border: #4299E1;
        border-radius: 8px;
        color: black;
        font-size: 1rem;
        box-sizing: border-box;
    }

    .bc-post-input::placeholder {
        color: #a0aec0;
    }

    .bc-post-input:focus {
        outline: none;
        box-shadow: 0 0 0 2px #4299e1;
    }

    /* 제목 입력 */
    .bc-post-title-input {
        font-weight: 600;
        border: 1px solid #4299E1;
    }

    /* 내용 입력 */
    .bc-post-content-input {
        min-height: 280px;
        resize: vertical;
        line-height: 1.6;
        border: 1px solid #4299E1;
    }

    /* 선택된 책 카드 */
    .bc-selected-book {
        display: none;
        background: #f7fafc;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 12px 16px;
        align-items: center;
        gap: 12px;
    }

    .bc-selected-book.show {
        display: flex;
    }

    .bc-selected-book-img {
        width: 50px;
        height: 70px;
        object-fit: cover;
        border-radius: 4px;
        background: #e2e8f0;
    }

    .bc-selected-book-info {
        flex: 1;
        min-width: 0;
    }

    .bc-selected-book-title {
        font-size: 0.9375rem;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 4px 0;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .bc-selected-book-author {
        font-size: 0.8125rem;
        color: #718096;
        margin: 0;
    }

    .bc-selected-book-remove {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: #e2e8f0;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
        flex-shrink: 0;
    }

    .bc-selected-book-remove:hover {
        background: #cbd5e0;
    }

    .bc-selected-book-remove svg {
        width: 14px;
        height: 14px;
        color: #4a5568;
    }

    /* 첨부 이미지 미리보기 */
    .bc-attached-image-wrapper {
        display: none;
    }

    .bc-attached-image-wrapper.show {
        display: block;
    }

    .bc-attached-image {
        position: relative;
        display: inline-block;
    }

    .bc-attached-image img {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
    }

    .bc-attached-image-remove {
        position: absolute;
        top: -8px;
        right: -8px;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: #e53e3e;
        border: 2px solid #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-attached-image-remove:hover {
        background: #c53030;
    }

    .bc-attached-image-remove svg {
        width: 12px;
        height: 12px;
        color: #fff;
    }

    /* 하단 버튼 영역 */
    .bc-post-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 24px;
        border-top: 1px solid #e2e8f0;
        background: #fff;
    }

    .bc-post-actions {
        display: flex;
        gap: 8px;
    }

    .bc-post-action-btn {
        width: 44px;
        height: 44px;
        border-radius: 8px;
        background: #f7fafc;
        border: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-post-action-btn:hover {
        background: #edf2f7;
        border-color: #cbd5e0;
    }

    .bc-post-action-btn svg {
        width: 22px;
        height: 22px;
        color: #718096;
    }

    .bc-post-submit-btn {
        padding: 12px 28px;
        background: #4299e1;
        color: #fff;
        border: none;
        border-radius: 24px;
        font-size: 0.9375rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-post-submit-btn:hover {
        background: #2b6cb0;
    }

    .bc-post-submit-btn:disabled {
        background: #a0aec0;
        cursor: not-allowed;
    }

    /* 숨김 파일 입력 */
    .bc-hidden-input {
        display: none;
    }

    /* 책 검색 모달 */
    .bc-book-modal-overlay {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 1000;
        justify-content: center;
        align-items: center;
    }

    .bc-book-modal-overlay.show {
        display: flex;
    }

    .bc-book-modal {
        background: #fff;
        border-radius: 16px;
        width: 90%;
        max-width: 500px;
        max-height: 80vh;
        overflow: hidden;
        display: flex;
        flex-direction: column;
    }

    .bc-book-modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid #e2e8f0;
    }

    .bc-book-modal-header h3 {
        margin: 0;
        font-size: 1.125rem;
        font-weight: 600;
        color: #2d3748;
    }

    .bc-book-modal-close {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: #f7fafc;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
    }

    .bc-book-modal-close:hover {
        background: #e2e8f0;
    }

    .bc-book-modal-body {
        padding: 16px 20px;
        overflow-y: auto;
        flex: 1;
    }

    .bc-book-search-input {
        width: 100%;
        padding: 12px 16px;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        font-size: 0.9375rem;
        box-sizing: border-box;
    }

    .bc-book-search-input:focus {
        outline: none;
        border-color: #4299e1;
    }

    .bc-book-search-results {
        margin-top: 16px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        max-height: 400px;
        overflow-y: auto;
    }

    .bc-book-search-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;
    }

    .bc-book-search-item:hover {
        background: #f7fafc;
        border-color: #4299e1;
    }

    .bc-book-search-item img {
        width: 40px;
        height: 56px;
        object-fit: cover;
        border-radius: 4px;
    }

    .bc-book-search-item-info {
        flex: 1;
        min-width: 0;
    }

    .bc-book-search-item-title {
        font-size: 0.875rem;
        font-weight: 600;
        color: #2d3748;
        margin: 0 0 2px 0;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .bc-book-search-item-author {
        font-size: 0.75rem;
        color: #718096;
        margin: 0;
    }

    .bc-book-search-empty {
        text-align: center;
        padding: 32px 16px;
        color: #718096;
        font-size: 0.875rem;
    }

    .bc-book-search-loading {
        text-align: center;
        padding: 32px 16px;
        color: #718096;
        font-size: 0.875rem;
    }

    /* 반응형 */
    @media (max-width: 768px) {
        .bc-post-form-wrapper {
            padding: 16px;
        }

        .bc-post-form-body {
            padding: 16px;
        }

        .bc-post-footer {
            padding: 12px 16px;
        }

        .bc-post-content-input {
            min-height: 200px;
        }
    }
</style>

<div class="bc-post-form-wrapper">
    <div class="bc-post-form-shell">
        <!-- 상단 헤더 -->
        <div class="bc-post-header">
            <button type="button" class="bc-post-back-btn" onclick="location.href='${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}'">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
                </svg>
            </button>
            <h1 class="bc-post-header-title">글 수정</h1>
        </div>

        <!-- 폼 -->
        <form id="postForm" method="post" action="${pageContext.request.contextPath}/bookclubs/${bookClubId}/posts/${post.book_club_board_seq}/edit" enctype="multipart/form-data">
            <sec:csrfInput />
            <div class="bc-post-form-body">
                <!-- 제목 입력 -->
                <input type="text" name="boardTitle" class="bc-post-input bc-post-title-input"
                       placeholder="제목" maxlength="100" required value="<c:out value='${post.board_title}'/>">

                <!-- 선택된 책 (기존 책 정보가 있으면 표시) -->
                <div class="bc-selected-book ${not empty post.book_title ? 'show' : ''}" id="selectedBookCard">
                    <img src="${not empty post.book_img_url ? fn:escapeXml(post.book_img_url) : 'https://via.placeholder.com/50x70?text=Book'}"
                         alt="책 표지" class="bc-selected-book-img" id="selectedBookImg"
                         onerror="this.src='https://via.placeholder.com/50x70?text=Book'">
                    <div class="bc-selected-book-info">
                        <p class="bc-selected-book-title" id="selectedBookTitle"><c:out value="${post.book_title}"/></p>
                        <p class="bc-selected-book-author" id="selectedBookAuthor"><c:out value="${post.book_author}"/></p>
                    </div>
                    <button type="button" class="bc-selected-book-remove" onclick="removeSelectedBook()">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                    <!-- 책 정보 hidden inputs -->
                    <input type="hidden" name="isbn" id="isbnInput" value="${fn:escapeXml(post.isbn)}">
                    <input type="hidden" name="bookTitle" id="bookTitleInput" value="${fn:escapeXml(post.book_title)}">
                    <input type="hidden" name="bookAuthor" id="bookAuthorInput" value="${fn:escapeXml(post.book_author)}">
                    <input type="hidden" name="bookImgUrl" id="bookImgUrlInput" value="${fn:escapeXml(post.book_img_url)}">
                </div>

                <!-- 내용 입력 -->
                <textarea name="boardCont" class="bc-post-input bc-post-content-input"
                          placeholder="내용을 입력하세요." required><c:out value="${post.board_cont}"/></textarea>

                <!-- 첨부 이미지 미리보기 -->
                <div class="bc-attached-image-wrapper ${not empty post.board_img_url ? 'show' : ''}" id="attachedImageWrapper">
                    <div class="bc-attached-image">
                        <img src="${fn:escapeXml(post.board_img_url)}" alt="첨부 이미지" id="attachedImagePreview">
                        <button type="button" class="bc-attached-image-remove" onclick="removeAttachedImage()">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                            </svg>
                        </button>
                    </div>
                </div>
                <!-- 기존 이미지 유지 여부 (새 이미지 없을 때 기존 이미지 유지할지) -->
                <input type="hidden" name="keepExistingImage" id="keepExistingImageInput" value="${not empty post.board_img_url ? 'true' : 'false'}">
            </div>

            <!-- 하단 버튼 영역 -->
            <div class="bc-post-footer">
                <div class="bc-post-actions">
                    <!-- 사진 첨부 버튼 -->
                    <button type="button" class="bc-post-action-btn" onclick="document.getElementById('imageInput').click()">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                    </button>
                    <input type="file" id="imageInput" name="boardImage" class="bc-hidden-input"
                           accept="image/*" onchange="previewImage(this)">

                    <!-- 책 선택 버튼 -->
                    <button type="button" class="bc-post-action-btn" onclick="openBookModal()">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                        </svg>
                    </button>
                </div>

                <!-- 수정 완료 버튼 -->
                <button type="submit" class="bc-post-submit-btn" id="postSubmitBtn">수정 완료</button>
            </div>
        </form>
    </div>
</div>

<!-- 책 검색 모달 -->
<div class="bc-book-modal-overlay" id="bookModalOverlay" onclick="closeBookModalOnOverlay(event)">
    <div class="bc-book-modal">
        <div class="bc-book-modal-header">
            <h3>책 검색</h3>
            <button type="button" class="bc-book-modal-close" onclick="closeBookModal()">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" width="16" height="16">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
            </button>
        </div>
        <div class="bc-book-modal-body">
            <input type="text" class="bc-book-search-input" id="bookSearchInput"
                   placeholder="책 제목 또는 저자를 검색하세요" onkeyup="searchBooks(event)">
            <div class="bc-book-search-results" id="bookSearchResults">
                <div class="bc-book-search-empty">책을 검색해주세요</div>
            </div>
        </div>
    </div>
</div>

<script>
    const ctx = '${pageContext.request.contextPath}';

    // 등록 버튼 실시간 활성화/비활성화
    function updateSubmitButtonState() {
        const title = document.querySelector('input[name="boardTitle"]').value.trim();
        const content = document.querySelector('textarea[name="boardCont"]').value.trim();
        const submitBtn = document.getElementById('postSubmitBtn');

        submitBtn.disabled = !(title && content);
    }

    // 제목, 내용 입력 시 버튼 상태 업데이트
    document.querySelector('input[name="boardTitle"]').addEventListener('input', updateSubmitButtonState);
    document.querySelector('textarea[name="boardCont"]').addEventListener('input', updateSubmitButtonState);

    // 초기 버튼 상태 설정
    updateSubmitButtonState();

    // 이미지 미리보기
    function previewImage(input) {
        const wrapper = document.getElementById('attachedImageWrapper');
        const preview = document.getElementById('attachedImagePreview');

        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                wrapper.classList.add('show');
                // 새 이미지가 선택되면 keepExistingImage는 의미 없음 (새 이미지가 저장됨)
                document.getElementById('keepExistingImageInput').value = 'true';
            };
            reader.readAsDataURL(input.files[0]);
        }
    }

    // 첨부 이미지 제거
    function removeAttachedImage() {
        const wrapper = document.getElementById('attachedImageWrapper');
        const input = document.getElementById('imageInput');
        const preview = document.getElementById('attachedImagePreview');

        input.value = '';
        preview.src = '';
        wrapper.classList.remove('show');
        // 이미지 삭제 요청
        document.getElementById('keepExistingImageInput').value = 'false';
    }

    // 책 모달 열기
    function openBookModal() {
        document.getElementById('bookModalOverlay').classList.add('show');
        document.getElementById('bookSearchInput').focus();
    }

    // 책 모달 닫기
    function closeBookModal() {
        document.getElementById('bookModalOverlay').classList.remove('show');
        document.getElementById('bookSearchInput').value = '';
        document.getElementById('bookSearchResults').innerHTML = '<div class="bc-book-search-empty">책을 검색해주세요</div>';
    }

    // 오버레이 클릭 시 모달 닫기
    function closeBookModalOnOverlay(event) {
        if (event.target === document.getElementById('bookModalOverlay')) {
            closeBookModal();
        }
    }

    // 책 검색 (디바운스 적용)
    let searchTimeout;
    function searchBooks(event) {
        clearTimeout(searchTimeout);
        const keyword = event.target.value.trim();

        if (keyword.length < 2) {
            document.getElementById('bookSearchResults').innerHTML = '<div class="bc-book-search-empty">2글자 이상 입력해주세요</div>';
            return;
        }

        document.getElementById('bookSearchResults').innerHTML = '<div class="bc-book-search-loading">검색 중...</div>';

        searchTimeout = setTimeout(function() {
            // 카카오 책 검색 API 호출
            fetch(ctx + '/trade/book?query=' + encodeURIComponent(keyword))
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(function(data) {
                    // API 응답을 렌더링 형식에 맞게 변환
                    const books = data.map(function(book) {
                        return {
                            seq: book.book_seq || 0,
                            title: book.book_title || '',
                            author: book.book_author || '',
                            img: book.book_img || 'https://via.placeholder.com/40x56?text=Book',
                            isbn: book.isbn || ''
                        };
                    });
                    renderBookResults(books);
                })
                .catch(function(error) {
                    console.error('책 검색 오류:', error);
                    document.getElementById('bookSearchResults').innerHTML = '<div class="bc-book-search-empty">검색 중 오류가 발생했습니다</div>';
                });
        }, 300);
    }

    // 검색 결과 렌더링
    function renderBookResults(books) {
        const container = document.getElementById('bookSearchResults');

        if (!books || books.length === 0) {
            container.innerHTML = '<div class="bc-book-search-empty">검색 결과가 없습니다</div>';
            return;
        }

        container.innerHTML = books.map(function(book, index) {
            // 이미지 URL 처리 (빈 값이면 placeholder 사용)
            const imgUrl = book.img || 'https://via.placeholder.com/40x56?text=Book';
            return '<div class="bc-book-search-item" data-index="' + index + '" onclick="selectBookFromList(' + index + ')">' +
                '<img src="' + imgUrl + '" alt="책 표지" onerror="this.src=\'https://via.placeholder.com/40x56?text=Book\'">' +
                '<div class="bc-book-search-item-info">' +
                '<p class="bc-book-search-item-title">' + escapeHtml(book.title) + '</p>' +
                '<p class="bc-book-search-item-author">' + escapeHtml(book.author) + '</p>' +
                '</div>' +
                '</div>';
        }).join('');

        // 검색 결과를 전역 변수에 저장 (선택 시 사용)
        window.bookSearchResults = books;
    }

    // HTML 이스케이프
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 검색 결과 목록에서 책 선택
    function selectBookFromList(index) {
        const book = window.bookSearchResults[index];
        if (!book) return;

        const imgUrl = book.img || 'https://via.placeholder.com/50x70?text=Book';

        // UI 업데이트
        document.getElementById('selectedBookCard').classList.add('show');
        document.getElementById('selectedBookImg').src = imgUrl;
        document.getElementById('selectedBookImg').onerror = function() {
            this.src = 'https://via.placeholder.com/50x70?text=Book';
        };
        document.getElementById('selectedBookTitle').textContent = book.title;
        document.getElementById('selectedBookAuthor').textContent = book.author;

        // hidden input에 책 정보 저장
        document.getElementById('isbnInput').value = book.isbn || '';
        document.getElementById('bookTitleInput').value = book.title || '';
        document.getElementById('bookAuthorInput').value = book.author || '';
        document.getElementById('bookImgUrlInput').value = book.img || '';

        closeBookModal();
    }

    // 선택된 책 제거
    function removeSelectedBook() {
        document.getElementById('selectedBookCard').classList.remove('show');
        document.getElementById('selectedBookImg').src = '';
        document.getElementById('selectedBookTitle').textContent = '';
        document.getElementById('selectedBookAuthor').textContent = '';
        // hidden input 초기화
        document.getElementById('isbnInput').value = '';
        document.getElementById('bookTitleInput').value = '';
        document.getElementById('bookAuthorInput').value = '';
        document.getElementById('bookImgUrlInput').value = '';
    }

    // 폼 제출 검증
    document.getElementById('postForm').addEventListener('submit', function(e) {
        const title = this.boardTitle.value.trim();
        const content = this.boardCont.value.trim();

        if (!title) {
            e.preventDefault();
            alert('제목을 입력해주세요.');
            this.boardTitle.focus();
            return;
        }

        if (!content) {
            e.preventDefault();
            alert('내용을 입력해주세요.');
            this.boardCont.focus();
            return;
        }
    });
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
