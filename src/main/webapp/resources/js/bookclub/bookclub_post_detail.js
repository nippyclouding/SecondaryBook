/**
 * 독서모임 게시글 상세 페이지 JS
 * - 댓글 입력 버튼 활성/비활성 토글
 * - Enter 전송 (Shift+Enter는 줄바꿈)
 */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var textarea = document.getElementById('bcCommentTextarea');
    var submitBtn = document.getElementById('bcCommentSubmit');
    var form = document.getElementById('bcCommentForm');

    if (!textarea || !submitBtn || !form) {
      return;
    }

    // 버튼 활성/비활성 토글 함수
    function updateButtonState() {
      var hasContent = textarea.value.trim().length > 0;
      submitBtn.disabled = !hasContent;
    }

    // 초기 상태 설정
    updateButtonState();

    // textarea 입력 시 버튼 상태 업데이트
    textarea.addEventListener('input', updateButtonState);

    // Enter 전송 (Shift+Enter는 줄바꿈)
    textarea.addEventListener('keydown', function (e) {
      // IME(한글 조합) 중이면 무시
      if (e.isComposing) {
        return;
      }

      // Enter 키이고 Shift 안 눌렀으면 전송
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();

        // 내용이 있을 때만 전송
        if (textarea.value.trim().length > 0) {
          form.requestSubmit();
        }
      }
    });
  });
})();
