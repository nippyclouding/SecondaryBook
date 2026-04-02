<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="max-w-4xl mx-auto py-8">
    <!-- Page Title -->
    <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">공지사항 수정</h1>
        <p class="text-gray-600">공지사항 내용을 수정해주세요</p>
    </div>

    <!-- Form -->
    <form id="noticeEditForm" enctype="multipart/form-data" class="space-y-8">

        <input type="hidden" id="notice_seq" name="notice_seq" value="${notice.notice_seq}">

        <!-- 기본 정보 섹션 -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/>
                    <polyline points="14 2 14 8 20 8"/>
                </svg>
                기본 정보
            </h2>

            <div class="space-y-4">
                <!-- 공지사항 제목 -->
                <div>
                    <label for="notice_title" class="block text-sm font-bold text-gray-700 mb-2">
                        제목 <span class="text-red-500">*</span>
                    </label>
                    <input type="text" id="notice_title" name="notice_title" required
                            value="<c:out value='${notice.notice_title}'/>"
                           placeholder="공지사항 제목을 입력하세요"
                           maxlength="100"
                           class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500" />
                    <p class="text-xs text-gray-500 mt-1">최대 100자까지 입력 가능합니다.</p>
                </div>

                <!-- 중요 공지 여부 및 공개 상태 -->
                <div class="grid grid-cols-2 gap-4">
                    <!-- 중요 공지 -->
                    <div>
                        <label class="block text-sm font-bold text-gray-700 mb-2">
                            공지 유형
                        </label>
                        <div class="flex items-center gap-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
                            <label class="flex items-center gap-2 cursor-pointer">
                                <input type="checkbox" id="is_important" name="is_important" value="true"
                                ${notice.notice_priority == 1 ? 'checked' : ''}
                                       class="w-4 h-4 text-red-600 bg-gray-100 border-gray-300 rounded focus:ring-red-500" />
                                <span class="text-sm text-gray-700 flex items-center gap-1">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-red-500">
                                        <circle cx="12" cy="12" r="10"/>
                                        <line x1="12" x2="12" y1="8" y2="12"/>
                                        <line x1="12" x2="12.01" y1="16" y2="16"/>
                                    </svg>
                                    중요 공지로 표시
                                </span>
                            </label>
                        </div>
                        <p class="text-xs text-gray-500 mt-1">중요 공지는 목록 상단에 고정됩니다.</p>
                    </div>

                    <!-- 공개 상태 -->
                    <div>
                        <label for="active" class="block text-sm font-bold text-gray-700 mb-2">
                            공개 상태 <span class="text-red-500">*</span>
                        </label>
                        <select id="active" name="active" required
                                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500">
                            <option value="true" ${notice.active ? 'selected' : ''}>공개</option>
                            <option value="false" ${!notice.active ? 'selected' : ''}>비공개</option>
                        </select>
                        <p class="text-xs text-gray-500 mt-1">비공개는 관리자만 볼 수 있습니다.</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 내용 섹션 -->
        <div class="bg-white rounded-lg border border-gray-200 p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                공지 내용
            </h2>

            <div class="space-y-4">
                <!-- 내용 입력 -->
                <div>
                    <label for="notice_cont" class="block text-sm font-bold text-gray-700 mb-2">
                        내용 <span class="text-red-500">*</span>
                    </label>
                    <textarea id="notice_cont" name="notice_cont" required rows="15"
                              placeholder="공지사항 내용을 작성해주세요."
                              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none font-mono text-sm"><c:out value="${notice.notice_cont}"/></textarea>
                    <div class="flex items-center justify-between mt-2">
                        <p class="text-xs text-gray-500">작성하신 내용은 회원들에게 바로 노출됩니다.</p>
                        <p id="charCount" class="text-xs text-gray-500">0자</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 제출 버튼 -->
        <div class="flex gap-3 sticky bottom-4 bg-white p-4 rounded-lg border border-gray-200 shadow-lg">
            <button type="button" onclick="switchView('notice-write')"
                    class="flex-1 px-6 py-4 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-bold">
                취소
            </button>
            <button type="submit"
                    class="flex-1 px-6 py-4 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition font-bold shadow-sm">
                수정하기
            </button>
        </div>
    </form>

    <!-- 로딩 표시 -->
    <div id="loadingIndicator" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6">
            <div class="flex items-center gap-3">
                <svg class="animate-spin h-5 w-5 text-primary-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span class="text-gray-700">불러오는 중...</span>
            </div>
        </div>
    </div>
</div>

<script>
// URL에서 notice_seq 파라미터 가져오기
function getNoticeSeqFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('notice_seq');
}

// 글자 수 카운터
const noticeEditContent = document.getElementById('notice_cont');
const charCountEdit = document.getElementById('charCount');

noticeEditContent.addEventListener('input', function() {
    const length = this.value.length;
    charCountEdit.textContent = length.toLocaleString() + '자';

    if (length > 5000) {
        charCountEdit.classList.add('text-red-500', 'font-bold');
    } else {
        charCountEdit.classList.remove('text-red-500', 'font-bold');
    }
});


// 폼 제출
document.getElementById('noticeEditForm').addEventListener('submit', function(e) {
    e.preventDefault();

    // 필수 필드 검증
    const title = document.getElementById('notice_title').value.trim();
    const content = document.getElementById('notice_cont').value.trim();
    const noticeSeq = document.getElementById('notice_seq').value;

    if (!title || !content) {
        alert('제목과 내용을 모두 입력해주세요.');
        return;
    }

    if (!noticeSeq) {
        alert('공지사항 번호가 없습니다.');
        return;
    }

    // 제출 전 확인
    if (!confirm('공지사항을 수정하시겠습니까?')) {
        return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    // FormData로 데이터 전송
    const formData = new FormData(this);
    const headers = {};
      if (csrfToken && csrfHeader) {
          headers[csrfHeader] = csrfToken;
      }

    fetch('/admin/notices/' + noticeSeq, {
        method: 'POST',
        headers: headers,
        body: formData
    })
 .then(response => {
       console.log('📡 응답 상태:', response.status);
       return response.json();  // text() → json()으로 되돌리기
   })
   .then(data => {
       console.log('📦 받은 데이터:', data);

       if (data.success) {
           alert('공지사항이 수정되었습니다.');
           location.href = '/admin?tab=notice';
       } else {
           alert('수정 중 오류가 발생했습니다: ' + (data.message || '알 수 없는 오류'));
       }
   })
    .catch(error => {
        console.error('❌ 에러:', error);
        alert('수정 중 오류가 발생했습니다: ' + error.message);
    });
});

</script>