/**
 * 독서모임 상세 페이지 - 탭 전환 (비동기 로딩)
 * - 홈 탭: 서버 첫 렌더, 클릭 시 DOM 토글만
 * - 게시판 탭: 첫 클릭 시 fetch로 fragment 로드, 이후 캐싱
 */

(function () {
    'use strict';

    // contextPath (JSP에서 window.__CTX 주입)
    const ctx = window.__CTX || '';

    // DOM 요소
    const pageWrapper = document.querySelector('.bc-page-wrapper');
    const tabLinks = document.querySelectorAll('.bc-tab-link');
    const homeContainer = document.getElementById('bc-home-container');
    const boardContainer = document.getElementById('bc-board-container');

    // 게시판 fragment 캐싱 플래그
    let boardLoaded = false;

    // bookClubId 가져오기
    const bookClubId = pageWrapper ? pageWrapper.dataset.bookclubId : null;

    if (!bookClubId) {
        console.error('[bookclub_detail.js] data-bookclub-id를 찾을 수 없습니다.');
        return;
    }

    /**
     * 탭 전환 처리
     * @param {string} tabName - 'home' 또는 'board'
     */
    function switchTab(tabName) {
        // active 클래스 토글
        tabLinks.forEach(function (link) {
            const isActive = link.dataset.tab === tabName;
            if (isActive) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        // 컨테이너 표시/숨김
        if (tabName === 'home') {
            homeContainer.style.display = 'block';
            boardContainer.style.display = 'none';
        } else if (tabName === 'board') {
            homeContainer.style.display = 'none';
            boardContainer.style.display = 'block';

            // 게시판 첫 클릭 시 fetch로 로드
            if (!boardLoaded) {
                loadBoardFragment();
            }
        }
    }

    /**
     * 게시판 fragment fetch로 로드
     */
    function loadBoardFragment() {
        // 로딩 메시지 표시
        boardContainer.innerHTML = '<div class="bc-content-wrapper"><div class="bc-card"><p style="color: #718096;">게시판을 불러오는 중...</p></div></div>';

        const url = ctx + '/bookclubs/' + bookClubId + '/board-fragment';

        fetch(url, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.text();
            })
            .then(function (html) {
                // fragment 삽입
                boardContainer.innerHTML = html;
                boardLoaded = true; // 캐싱 플래그 설정
            })
            .catch(function (error) {
                console.error('[bookclub_detail.js] 게시판 로드 실패:', error);
                boardContainer.innerHTML = '<div class="bc-content-wrapper"><div class="bc-card"><p style="color: #e53e3e;">게시판을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.</p></div></div>';
            });
    }

    /**
     * 탭 클릭 이벤트 등록
     */
    tabLinks.forEach(function (link) {
        link.addEventListener('click', function (event) {
            event.preventDefault();
            const tabName = link.dataset.tab;
            switchTab(tabName);
        });
    });

    // URL 파라미터 ?tab=board 확인 → 게시판 탭 자동 활성화
    if (new URLSearchParams(window.location.search).get('tab') === 'board') {
        switchTab('board');
    }

    /**
     * CSRF 토큰 가져오기 (meta 태그에서 추출)
     */
    function getCsrfToken() {
        var token = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        return {
            token: token ? token.getAttribute('content') : null,
            header: header ? header.getAttribute('content') : null
        };
    }

    /**
     * 탈퇴하기 버튼 이벤트
     */
    (function initLeaveButton() {
        var btnLeave = document.getElementById('btnLeaveBookClub');
        if (!btnLeave) {
            return;
        }

        var clubId = btnLeave.dataset.clubId;
        var isLeader = btnLeave.dataset.isLeader === 'true';

        btnLeave.addEventListener('click', function () {
            // 모임장 탈퇴 시 추가 경고
            var confirmMsg = isLeader
                ? '모임장이 탈퇴하면 다른 멤버에게 모임장이 자동 승계됩니다.\n멤버가 없으면 모임이 종료됩니다.\n\n정말 탈퇴하시겠습니까?'
                : '정말 모임을 나가시겠습니까?';

            if (!confirm(confirmMsg)) {
                return;
            }

            // CSRF 토큰 가져오기
            var csrf = getCsrfToken();
            var headers = {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            };
            if (csrf.token && csrf.header) {
                headers[csrf.header] = csrf.token;
            }

            btnLeave.disabled = true;
            btnLeave.textContent = '처리 중...';

            var url = ctx + '/bookclubs/' + clubId + '/leave';

            fetch(url, {
                method: 'POST',
                headers: headers
            })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (result) {
                if (result.success) {
                    alert(result.message);

                    // 모임 종료된 경우 목록으로 이동
                    if (result.clubClosed) {
                        window.location.href = ctx + '/bookclubs';
                    } else {
                        // 일반 탈퇴 또는 승계: 페이지 새로고침
                        window.location.reload();
                    }
                } else {
                    alert(result.message || '탈퇴 처리에 실패했습니다.');
                    btnLeave.disabled = false;
                    btnLeave.textContent = '탈퇴하기';
                }
            })
            .catch(function (err) {
                console.error('[bookclub_detail.js] 탈퇴 요청 실패:', err);
                alert('탈퇴 처리 중 오류가 발생했습니다.');
                btnLeave.disabled = false;
                btnLeave.textContent = '탈퇴하기';
            });
        });
    })();

    /**
     * 가입 신청 모달
     */
    (function initApplyModal() {
        var btnOpen = document.getElementById('btnOpenApplyModal');
        var modal = document.getElementById('applyModal');

        if (!btnOpen || !modal) {
            return;
        }

        var overlay = modal.querySelector('.bc-apply-modal-overlay');
        var btnCancel = document.getElementById('btnCancelApply');
        var btnSubmit = document.getElementById('btnSubmitApply');
        var reasonInput = document.getElementById('applyReasonInput');

        function openModal() {
            modal.classList.add('bc-apply-modal-active');
            document.body.style.overflow = 'hidden';
        }

        function closeModal() {
            modal.classList.remove('bc-apply-modal-active');
            document.body.style.overflow = '';
            reasonInput.value = '';
        }

        btnOpen.addEventListener('click', openModal);
        btnCancel.addEventListener('click', closeModal);
        overlay.addEventListener('click', closeModal);

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && modal.classList.contains('bc-apply-modal-active')) {
                closeModal();
            }
        });

        btnSubmit.addEventListener('click', function () {
            var reason = reasonInput.value.trim();

            if (!reason) {
                alert('지원 동기를 입력해주세요.');
                reasonInput.focus();
                return;
            }

            btnSubmit.disabled = true;
            btnSubmit.textContent = '신청 중...';

            var url = ctx + '/bookclubs/' + bookClubId + '/join';

            // CSRF 토큰 가져오기
            var csrf = getCsrfToken();
            var headers = {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            };
            if (csrf.token && csrf.header) {
                headers[csrf.header] = csrf.token;
            } else {
                console.warn('[bookclub_detail.js] CSRF 토큰을 찾을 수 없습니다. 요청을 계속 진행합니다.');
            }

            fetch(url, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ reason: reason })
            })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function () {
                alert('가입 신청이 완료되었습니다.');
                closeModal();
                location.reload();
            })
            .catch(function (err) {
                console.error('[bookclub_detail.js] 가입 신청 실패:', err);
                alert('가입 신청에 실패했습니다. 잠시 후 다시 시도해주세요.');
            })
            .finally(function () {
                btnSubmit.disabled = false;
                btnSubmit.textContent = '가입 신청';
            });
        });
    })();
})();
