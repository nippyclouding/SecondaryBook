/**
 * 독서모임 관리 페이지 - 탭 전환 기능만 구현
 * (서버 통신/fetch 로직은 추후 구현)
 */

const BookClubManage = (() => {

    let currentTab = 'tabRequests'; // 기본 활성 탭: 가입 신청

    /**
     * 탭 전환 초기화
     */
    function initTabs() {
        const tabBtns = document.querySelectorAll('.tab-btn');

        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetId = btn.getAttribute('aria-controls');
                switchTab(targetId);
            });
        });
    }

    /**
     * 탭 전환 처리
     */
    function switchTab(targetId) {
        // 모든 탭 버튼 비활성화
        const allTabBtns = document.querySelectorAll('.tab-btn');
        allTabBtns.forEach(btn => {
            btn.classList.remove('active');
            btn.setAttribute('aria-selected', 'false');
        });

        // 모든 탭 패널 숨김
        const allPanels = document.querySelectorAll('.tab-panel');
        allPanels.forEach(panel => {
            panel.classList.remove('active');
        });

        // 선택한 탭 활성화
        const targetBtn = document.querySelector(`[aria-controls="${targetId}"]`);
        const targetPanel = document.getElementById(targetId);

        if (targetBtn && targetPanel) {
            targetBtn.classList.add('active');
            targetBtn.setAttribute('aria-selected', 'true');
            targetPanel.classList.add('active');
            currentTab = targetId;
        }
    }

    /**
     * 모달 초기화 (거절 사유 입력)
     */
    function initModal() {
        const modal = document.getElementById('rejectModal');
        if (!modal) return;

        const closeBtns = modal.querySelectorAll('[data-dismiss="modal"]');

        closeBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                closeModal();
            });
        });

        // 오버레이 클릭 시 닫기
        const overlay = modal.querySelector('.modal-overlay');
        overlay?.addEventListener('click', () => {
            closeModal();
        });
    }

    /**
     * 모달 열기
     */
    function openModal() {
        const modal = document.getElementById('rejectModal');
        if (modal) {
            modal.setAttribute('aria-hidden', 'false');
        }
    }

    /**
     * 모달 닫기
     */
    function closeModal() {
        const modal = document.getElementById('rejectModal');
        if (modal) {
            modal.setAttribute('aria-hidden', 'true');
            // 폼 초기화
            const form = document.getElementById('rejectForm');
            form?.reset();
        }
    }

    /**
     * 배너 이미지 URL 미리보기
     */
    function initImagePreview() {
        const bannerImgUrlInput = document.getElementById('bannerImgUrl');
        const bannerFileInput = document.getElementById('bannerFile');
        const bannerPreview = document.getElementById('bannerPreview');

        // bannerPreview만 필수 조건으로 변경
        if (!bannerPreview) return;

        // URL 입력 이벤트 (input이 존재할 때만)
        if (bannerImgUrlInput) {
            bannerImgUrlInput.addEventListener('blur', () => {
                const url = bannerImgUrlInput.value.trim();
                updateBannerPreview(url);
            });
        }

        // 파일 선택 시 미리보기 업데이트 (항상 등록)
        if (bannerFileInput) {
            bannerFileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (!file) return;

                // 이미지 파일인지 검증
                if (!file.type.startsWith('image/')) {
                    alert('이미지 파일만 선택할 수 있습니다.');
                    bannerFileInput.value = '';
                    return;
                }

                // URL input 비우기 (파일 우선 정책 - input이 존재할 때만)
                if (bannerImgUrlInput) {
                    bannerImgUrlInput.value = '';
                }

                // FileReader로 data URL 생성 후 즉시 미리보기
                const reader = new FileReader();
                reader.onload = (event) => {
                    updateBannerPreview(event.target.result);
                };
                reader.readAsDataURL(file);
            });
        }
    }

    /**
     * 배너 이미지 미리보기 업데이트
     */
    function updateBannerPreview(url) {
        const bannerPreview = document.getElementById('bannerPreview');
        if (!bannerPreview) return;

        const parentEl = bannerPreview.parentElement;

        if (url && url.length > 0) {
            // URL이 있으면 이미지로 교체
            const newImg = document.createElement('img');
            newImg.src = url;
            newImg.alt = '모임 대표 이미지';
            newImg.className = 'banner-image';
            newImg.id = 'bannerPreview';
            newImg.onerror = () => {
                // 이미지 로드 실패 시 플레이스홀더로 복원
                const placeholder = document.createElement('div');
                placeholder.className = 'banner-placeholder';
                placeholder.id = 'bannerPreview';
                placeholder.textContent = '📚';
                if (newImg.parentElement) {
                    newImg.parentElement.replaceChild(placeholder, newImg);
                }
            };
            parentEl.replaceChild(newImg, bannerPreview);
        } else {
            // URL이 없으면 플레이스홀더로 교체
            if (bannerPreview.tagName === 'IMG') {
                const placeholder = document.createElement('div');
                placeholder.className = 'banner-placeholder';
                placeholder.id = 'bannerPreview';
                placeholder.textContent = '📚';
                parentEl.replaceChild(placeholder, bannerPreview);
            }
        }
    }

    /**
     * CSRF 토큰 가져오기 (meta 태그에서 추출)
     * @returns {{ token: string|null, header: string|null, isValid: boolean }}
     */
    function getCsrfToken() {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const isValid = !!(token && header);
        return { token, header, isValid };
    }

    /**
     * API URL 빌드 헬퍼 (contextPath 지원)
     * @param {string} path - API 경로 (예: '/bookclubs/123/manage/requests/456/approve')
     * @returns {string} - 전체 URL
     */
    function buildUrl(path) {
        // contextPath는 JSP에서 전역 변수로 설정 (window.contextPath)
        const ctx = window.contextPath || '';
        return ctx + path;
    }

    /**
     * 알림 배너 표시
     */
    function showAlert(message, type = 'error') {
        const alertBanner = document.getElementById('alertBanner');
        if (!alertBanner) return;

        alertBanner.textContent = message;
        alertBanner.className = `alert-banner alert-${type}`;
        alertBanner.style.display = 'block';

        // 3초 후 자동 숨김
        setTimeout(() => {
            alertBanner.style.display = 'none';
        }, 3000);
    }

    /**
     * 승인/거절 후 request-card DOM 제거
     */
    function removeRequestCard(requestSeq) {
        const card = document.querySelector(`.request-card[data-request-seq="${requestSeq}"]`);
        if (card) {
            card.remove();
        }

        // 목록이 비었으면 empty-state 표시
        const requestList = document.getElementById('requestList');
        const remainingCards = requestList?.querySelectorAll('.request-card');
        if (remainingCards && remainingCards.length === 0) {
            requestList.innerHTML = `
                <div class="empty-state">
                    <p>대기 중인 가입 신청이 없습니다.</p>
                </div>
            `;
        }
    }

    /**
     * 카운트 업데이트 (상단 요약 + 탭 배지)
     */
    function updateCounts(memberCount, pendingCount) {
        // 현재 인원 업데이트
        if (memberCount !== undefined) {
            const memberCountEl = document.getElementById('currentMemberCount');
            if (memberCountEl) {
                memberCountEl.textContent = memberCount;
            }
        }

        // 대기 중인 신청 수 업데이트
        if (pendingCount !== undefined) {
            const pendingCountEl = document.getElementById('pendingRequestCount');
            if (pendingCountEl) {
                pendingCountEl.textContent = pendingCount;
            }

            // 탭 배지 업데이트
            const tabBadge = document.querySelector('#tabBtnRequests .badge-count');
            if (tabBadge) {
                if (pendingCount > 0) {
                    tabBadge.textContent = pendingCount;
                    tabBadge.style.display = 'inline-block';
                } else {
                    tabBadge.style.display = 'none';
                }
            }
        }
    }

    /**
     * 모임 설정 저장 버튼 초기화
     */
    function initSettingsSaveButton() {
        const saveBtn = document.getElementById('btnSaveSettings');
        if (!saveBtn) {
            console.warn('[BookClubManage] btnSaveSettings 버튼을 찾을 수 없습니다.');
            return;
        }

        console.log('[BookClubManage] 설정 저장 버튼 이벤트 리스너 등록 완료');

        saveBtn.addEventListener('click', async () => {
            console.log('[BookClubManage] 저장 버튼 클릭됨');
            // 입력값 수집
            const name = document.getElementById('clubName')?.value.trim();
            const description = document.getElementById('clubDescription')?.value.trim();
            const region = document.getElementById('clubRegion')?.value.trim();
            const schedule = document.getElementById('clubSchedule')?.value.trim();
            const existingBannerUrl = document.getElementById('existingBannerUrl')?.value.trim();
            const bannerFile = document.getElementById('bannerFile')?.files[0];

            // 필수 입력값 검증
            if (!name) {
                showAlert('모임 이름을 입력해주세요.', 'error');
                return;
            }
            if (!description) {
                showAlert('모임 소개를 입력해주세요.', 'error');
                return;
            }

            // 확인 팝업
            if (!confirm('변경사항을 저장하시겠습니까?')) {
                return;
            }

            // 버튼 비활성화 (중복 클릭 방지)
            saveBtn.disabled = true;
            saveBtn.textContent = '저장 중...';

            try {
                // CSRF 토큰 검증
                const csrf = getCsrfToken();
                if (!csrf.isValid) {
                    console.error('CSRF 토큰이 없습니다. meta 태그를 확인하세요.');
                    showAlert('보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하세요.', 'error');
                    saveBtn.disabled = false;
                    saveBtn.textContent = '변경사항 저장';
                    return;
                }

                const bookClubId = window.location.pathname.split('/')[2]; // /bookclubs/{id}/manage
                const url = buildUrl(`/bookclubs/${bookClubId}/manage/settings`);

                // FormData 생성 (multipart/form-data)
                const formData = new FormData();
                formData.append('name', name);
                formData.append('description', description);
                formData.append('region', region || '');
                formData.append('schedule', schedule || '');

                // 파일이 있으면 파일 우선, 없으면 기존 URL 유지
                if (bannerFile) {
                    formData.append('bannerFile', bannerFile);
                } else if (existingBannerUrl) {
                    formData.append('bannerImgUrl', existingBannerUrl);
                }

                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        [csrf.header]: csrf.token
                        // Content-Type은 브라우저가 자동으로 multipart/form-data로 설정
                    },
                    body: formData
                });

                const result = await response.json();

                if (result.success) {
                    showAlert(result.message, 'success');

                    // 업데이트된 데이터로 화면 즉시 갱신
                    if (result.updated) {
                        // 상단 제목/서브타이틀의 모임명 갱신
                        const pageSubtitle = document.querySelector('.page-subtitle');
                        if (pageSubtitle) {
                            pageSubtitle.textContent = result.updated.name;
                        }

                        // 입력 필드 갱신 (서버에서 받은 최신 값으로 동기화)
                        document.getElementById('clubName').value = result.updated.name;
                        document.getElementById('clubDescription').value = result.updated.description;
                        document.getElementById('clubRegion').value = result.updated.region || '';
                        document.getElementById('clubSchedule').value = result.updated.schedule || '';

                        const existingBannerUrlInput = document.getElementById('existingBannerUrl');
                        if (existingBannerUrlInput) {
                            existingBannerUrlInput.value = result.updated.bannerImgUrl || '';
                        }

                        // 배너 이미지 미리보기 갱신
                        updateBannerPreview(result.updated.bannerImgUrl);
                    }

                    // DB 변경사항 확실히 반영 위해 페이지 새로고침
                    setTimeout(() => location.reload(), 600);
                } else {
                    showAlert(result.message, 'error');
                }
            } catch (error) {
                console.error('설정 저장 요청 실패:', error);
                showAlert('설정 저장 중 오류가 발생했습니다.', 'error');
            } finally {
                // 버튼 원래대로
                saveBtn.disabled = false;
                saveBtn.textContent = '변경사항 저장';
            }
        });
    }

    /**
     * 버튼 이벤트 초기화 (실제 fetch 로직 구현)
     */
    function initButtons() {
        // 승인 버튼
        const approveBtns = document.querySelectorAll('.btn-approve');
        approveBtns.forEach(btn => {
            btn.addEventListener('click', async () => {
                const requestSeq = btn.dataset.requestSeq;
                const clubSeq = btn.dataset.clubSeq;

                if (!confirm('이 신청을 승인하시겠습니까?')) {
                    return;
                }

                // 버튼 비활성화 (중복 클릭 방지)
                btn.disabled = true;
                btn.textContent = '처리 중...';

                try {
                    // CSRF 토큰 검증
                    const csrf = getCsrfToken();
                    if (!csrf.isValid) {
                        console.error('CSRF 토큰이 없습니다. meta 태그를 확인하세요.');
                        showAlert('보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하세요.', 'error');
                        btn.disabled = false;
                        btn.textContent = '승인';
                        return;
                    }

                    // URL 빌드 (contextPath 지원)
                    const url = buildUrl(`/bookclubs/${clubSeq}/manage/requests/${requestSeq}/approve`);
                    console.log('승인 요청 URL:', url);

                    const response = await fetch(url, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            [csrf.header]: csrf.token
                        }
                    });

                    const result = await response.json();

                    if (result.success) {
                        showAlert(result.message, 'success');
                        setTimeout(() => location.reload(), 600);
                        return;
                    } else {
                        showAlert(result.message, 'error');
                        // 실패 시 버튼 원래대로
                        btn.disabled = false;
                        btn.textContent = '승인';
                    }
                } catch (error) {
                    console.error('승인 요청 실패:', error);
                    showAlert('승인 처리 중 오류가 발생했습니다.', 'error');
                    // 에러 시 버튼 원래대로
                    btn.disabled = false;
                    btn.textContent = '승인';
                }
            });
        });

        // 거절 버튼
        const rejectBtns = document.querySelectorAll('.btn-reject');
        rejectBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const requestSeq = btn.dataset.requestSeq;
                const clubSeq = btn.dataset.clubSeq;

                // 모달 열고 hidden input에 값 설정
                const rejectRequestSeqInput = document.getElementById('rejectRequestSeq');
                const rejectClubSeqInput = document.getElementById('rejectClubSeq');

                if (rejectRequestSeqInput) rejectRequestSeqInput.value = requestSeq;
                if (rejectClubSeqInput) rejectClubSeqInput.value = clubSeq;

                openModal();
            });
        });

        // 거절 폼 제출
        const rejectForm = document.getElementById('rejectForm');
        rejectForm?.addEventListener('submit', async (e) => {
            e.preventDefault();

            const requestSeq = document.getElementById('rejectRequestSeq').value;
            const clubSeq = document.getElementById('rejectClubSeq').value;
            // 거절 사유(reason)는 1차 단순화로 전송하지 않음 (DB 컬럼 없음)
            // 추후 알림/메시지 기능 추가 시 확장 가능

            // 제출 버튼 비활성화 (중복 제출 방지)
            const submitBtn = rejectForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = '처리 중...';
            }

            try {
                // CSRF 토큰 검증
                const csrf = getCsrfToken();
                if (!csrf.isValid) {
                    console.error('CSRF 토큰이 없습니다. meta 태그를 확인하세요.');
                    showAlert('보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하세요.', 'error');
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = '거절하기';
                    }
                    return;
                }

                // URL 빌드 (contextPath 지원)
                const url = buildUrl(`/bookclubs/${clubSeq}/manage/requests/${requestSeq}/reject`);
                console.log('거절 요청 URL:', url);

                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrf.header]: csrf.token
                    }
                });

                const result = await response.json();

                if (result.success) {
                    showAlert(result.message, 'success');
                    closeModal();
                    setTimeout(() => location.reload(), 600);
                    return;
                } else {
                    showAlert(result.message, 'error');
                }
            } catch (error) {
                console.error('거절 요청 실패:', error);
                showAlert('거절 처리 중 오류가 발생했습니다.', 'error');
            } finally {
                // 버튼 원래대로
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '거절하기';
                }
            }
        });

        // 멤버 퇴장 버튼
        const kickBtns = document.querySelectorAll('.btn-kick');
        kickBtns.forEach(btn => {
            btn.addEventListener('click', async () => {
                const memberSeq = btn.dataset.memberSeq;
                const clubSeq = btn.dataset.clubSeq;
                const memberName = btn.dataset.memberName;

                if (!confirm(`정말로 ${memberName}님을 퇴장시키겠습니까?`)) {
                    return;
                }

                // 버튼 비활성화 (중복 클릭 방지)
                btn.disabled = true;
                btn.textContent = '처리 중...';

                try {
                    // CSRF 토큰 검증
                    const csrf = getCsrfToken();
                    if (!csrf.isValid) {
                        console.error('CSRF 토큰이 없습니다. meta 태그를 확인하세요.');
                        showAlert('보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하세요.', 'error');
                        btn.disabled = false;
                        btn.textContent = '퇴장';
                        return;
                    }

                    // URL 빌드 (contextPath 지원)
                    const url = buildUrl(`/bookclubs/${clubSeq}/manage/members/${memberSeq}/kick`);

                    const response = await fetch(url, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            [csrf.header]: csrf.token
                        }
                    });

                    const result = await response.json();

                    if (result.success) {
                        showAlert(result.message, 'success');
                        setTimeout(() => location.reload(), 600);
                        return;
                    } else {
                        showAlert(result.message, 'error');
                        // 실패 시 버튼 원래대로
                        btn.disabled = false;
                        btn.textContent = '퇴장';
                    }
                } catch (error) {
                    console.error('강퇴 요청 실패:', error);
                    showAlert('강퇴 처리 중 오류가 발생했습니다.', 'error');
                    // 에러 시 버튼 원래대로
                    btn.disabled = false;
                    btn.textContent = '퇴장';
                }
            });
        });
    }

    /**
     * 페이지 초기화 (외부에서 호출)
     */
    function init(clubSeq) {
        console.log('[BookClubManage] 초기화 시작:', clubSeq);

        // CSRF 토큰 확인
        const csrf = getCsrfToken();
        console.log('[BookClubManage] CSRF 토큰 확인:', csrf);

        // 탭 전환 기능 초기화
        initTabs();

        // 모달 초기화
        initModal();

        // 버튼 이벤트 초기화
        initButtons();

        // 이미지 미리보기 초기화
        initImagePreview();

        // 설정 저장 버튼 초기화
        initSettingsSaveButton();

        console.log('[BookClubManage] 초기화 완료');
    }

    // 외부 공개 API
    return {
        init,
        switchTab,
        openModal,
        closeModal,
        getCsrfToken  // 디버깅용 노출
    };
})();
