-- =============================================
-- SecondaryBook 추가 인덱스
-- MySQL.create.sql의 기존 인덱스 외에 필요한 인덱스
-- =============================================
-- 기존 DDL에 이미 포함된 단일 컬럼 인덱스는 제외
-- 실제 쿼리 패턴(MyBatis Mapper XML) 분석 기반


-- =============================================
-- [CRITICAL] 스케줄러 쿼리용 복합 인덱스
-- 매분/매일 반복 실행되므로 풀 테이블 스캔 방지 필수
-- =============================================

-- 안전결제 만료 리셋 (SafePaymentScheduler, 매 60초)
-- WHERE safe_payment_st = 'PENDING' AND safe_payment_expire_dtm < NOW()
CREATE INDEX idx_trade_payment_expiry
    ON sb_trade_info (safe_payment_st, safe_payment_expire_dtm);

-- 자동 구매확정 (SafePaymentScheduler, 매일 자정)
-- WHERE confirm_purchase = false AND sale_st = 'SOLD' AND sale_st_dtm < DATE_SUB(NOW(), INTERVAL 15 DAY)
CREATE INDEX idx_trade_auto_confirm
    ON sb_trade_info (confirm_purchase, sale_st, sale_st_dtm);

-- 정산 배치 처리 (SafePaymentScheduler, 매일 새벽 3시)
-- WHERE settlement_st = 'REQUESTED' ORDER BY request_dtm ASC
CREATE INDEX idx_settlement_requested
    ON settlement (settlement_st, request_dtm);


-- =============================================
-- [HIGH] 거래 목록/검색 (HomeController, 유저 메인 페이지)
-- 가장 빈번한 조회 쿼리
-- =============================================

-- 기본 목록 조회: WHERE del_dtm IS NULL ORDER BY crt_dtm DESC
-- 기존 idx_trade_del_dtm(del_dtm), idx_trade_crt_dtm(crt_dtm) 각각 존재하지만
-- 복합 인덱스가 없으면 두 조건을 동시에 커버하지 못함
CREATE INDEX idx_trade_list_default
    ON sb_trade_info (del_dtm, crt_dtm DESC);

-- 카테고리 + 판매상태 필터: WHERE del_dtm IS NULL AND category_seq = ? AND sale_st = ?
CREATE INDEX idx_trade_list_filtered
    ON sb_trade_info (del_dtm, category_seq, sale_st, crt_dtm DESC);

-- 관리자 안전결제 내역: WHERE payment_type = 'tosspay' AND safe_payment_st = 'COMPLETED'
CREATE INDEX idx_trade_safepay_admin
    ON sb_trade_info (payment_type, safe_payment_st, confirm_purchase, sale_st_dtm DESC);


-- =============================================
-- [HIGH] 채팅 관련 복합 인덱스
-- =============================================

-- 채팅방 목록: WHERE (member_seller_seq = ? OR member_buyer_seq = ?) ORDER BY crt_dtm DESC
-- OR 조건은 두 개의 인덱스를 각각 타야 함 (Index Merge 또는 UNION)
-- 기존 idx_chatroom_buyer, idx_chatroom_seller는 crt_dtm을 포함하지 않아 정렬에 filesort 발생
CREATE INDEX idx_chatroom_seller_time
    ON chatroom (member_seller_seq, crt_dtm DESC);

CREATE INDEX idx_chatroom_buyer_time
    ON chatroom (member_buyer_seq, crt_dtm DESC);

-- 메시지 조회: WHERE chat_room_seq = ? ORDER BY sent_dtm ASC
-- 기존 idx_chat_msg_room(chat_room_seq)은 sent_dtm을 포함하지 않아 정렬 시 filesort 발생
CREATE INDEX idx_chat_msg_room_time
    ON chat_msg (chat_room_seq, sent_dtm);

-- 읽음 처리: WHERE chat_room_seq = ? AND sender_seq != ? AND read_yn = false
-- 기존 idx_chat_msg_read(chat_room_seq, read_yn)에 sender_seq 추가
CREATE INDEX idx_chat_msg_unread
    ON chat_msg (chat_room_seq, read_yn, sender_seq);


-- =============================================
-- [HIGH] 독서모임 복합 인덱스
-- =============================================

-- 모임 목록: WHERE book_club_deleted_dt IS NULL ORDER BY crt_dtm DESC
-- 기존 idx_book_club_deleted(book_club_deleted_dt)에 crt_dtm 미포함
CREATE INDEX idx_bookclub_list_default
    ON book_club (book_club_deleted_dt, crt_dtm DESC);

-- 멤버 상태 조회: WHERE book_club_seq = ? AND join_st = 'JOINED'
-- 기존에 idx_bcm_club, idx_bcm_join_st 각각 존재하지만 복합 인덱스 없음
-- 멤버 수 카운트, 가입 여부 확인 등에서 빈번하게 사용됨
CREATE INDEX idx_bcm_club_status
    ON book_club_member (book_club_seq, join_st);

-- 가입 신청 상태 조회: WHERE book_club_seq = ? AND request_member_seq = ? AND request_st = ?
-- 기존에 각각 단일 인덱스만 존재
CREATE INDEX idx_bcr_club_member_st
    ON book_club_request (book_club_seq, request_member_seq, request_st);

-- 게시판 원글 목록: WHERE book_club_seq = ? AND parent_seq IS NULL AND deleted_yn = 0
CREATE INDEX idx_bcb_root_posts
    ON book_club_board (book_club_seq, parent_book_club_board_seq, board_deleted_dtm);

-- 댓글 목록: WHERE book_club_seq = ? AND parent_seq = ? AND deleted_yn = 0
-- 위의 idx_bcb_root_posts 인덱스가 댓글 조회에도 사용 가능 (별도 생성 불필요)


-- =============================================
-- [MEDIUM] 거래 이미지
-- =============================================

-- 이미지 조회: WHERE trade_seq = ? ORDER BY sort_seq
-- 기존 idx_book_image_trade(trade_seq)에 sort_seq 미포함
CREATE INDEX idx_book_image_trade_sort
    ON book_image (trade_seq, sort_seq);


-- =============================================
-- [MEDIUM] 로그인 기록 (관리자 페이지)
-- =============================================

-- 관리자 로그인 이력: WHERE admin_seq IS NOT NULL ORDER BY login_dtm DESC
-- 기존 idx_login_info_admin(admin_seq)에 login_dtm 미포함
CREATE INDEX idx_login_admin_time
    ON login_info (admin_seq, login_dtm DESC);

-- 회원 로그인 이력: WHERE member_seq IS NOT NULL ORDER BY login_dtm DESC
-- 기존 idx_login_info_member(member_seq)에 login_dtm 미포함
CREATE INDEX idx_login_member_time
    ON login_info (member_seq, login_dtm DESC);

-- 로그아웃 처리: WHERE admin_seq = ? AND logout_dtm IS NULL
CREATE INDEX idx_login_admin_active
    ON login_info (admin_seq, logout_dtm);

CREATE INDEX idx_login_member_active
    ON login_info (member_seq, logout_dtm);


-- =============================================
-- [MEDIUM] 배송지
-- =============================================

-- 배송지 목록: WHERE member_seq = ? ORDER BY default_yn DESC, addr_seq DESC
-- 기존 idx_address_member(member_seq)에 정렬 컬럼 미포함
CREATE INDEX idx_address_member_sort
    ON address_info (member_seq, default_yn DESC, addr_seq DESC);


-- =============================================
-- [MEDIUM] 공지사항
-- =============================================

-- 활성 공지 목록: WHERE active = 1 ORDER BY notice_priority DESC, crt_dtm DESC
-- 기존 idx_notice_priority(notice_priority, crt_dtm)에 active 미포함
CREATE INDEX idx_notice_active_list
    ON notice (active, notice_priority DESC, crt_dtm DESC);


-- =============================================
-- [MEDIUM] 회원 조회
-- =============================================

-- 전화번호로 아이디 찾기: WHERE member_tel_no = ? AND member_deleted_dtm IS NULL
CREATE INDEX idx_member_tel
    ON member_info (member_tel_no, member_deleted_dtm);

-- idx_settlement_trade 삭제: settlement.trade_seq UNIQUE 제약으로 자동 생성되는 유니크 인덱스가 이미 존재함
-- CREATE INDEX idx_settlement_trade ON settlement (trade_seq);  ← 중복, 생성 불필요

-- 판매자별 정산 내역 조회: WHERE member_seller_seq = ? AND settlement_st = ?
-- 마이페이지 정산 내역 필터링에 사용
CREATE INDEX idx_settlement_seller_st
    ON settlement (member_seller_seq, settlement_st);

-- 관리자 계좌 잔액 변동 이력: WHERE account_seq = ? ORDER BY crt_dtm DESC
-- 관리자 대시보드 잔액 히스토리 조회에 사용
CREATE INDEX idx_account_log_time
    ON admin_account_log (account_seq, crt_dtm DESC);

-- 정산별 로그 조회: WHERE settlement_seq = ?
-- confirmTransfer 감사 이력 추적에 사용
CREATE INDEX idx_account_log_settlement
    ON admin_account_log (settlement_seq);


-- =============================================
-- [참고] 기존 DDL에 이미 포함된 인덱스 (중복 생성 불필요)
-- =============================================
-- member_info: login_id (UNIQUE), member_email (UNIQUE), member_nicknm (UNIQUE)
-- member_oauth: (provider, provider_id) (UNIQUE)
-- sb_trade_info: member_seller_seq, member_buyer_seq, del_dtm, sale_st, category_seq, crt_dtm, safe_payment_st
-- trade_wish: (trade_seq, member_seq) (UNIQUE)
-- book_image: trade_seq
-- chatroom: trade_seq, member_buyer_seq, member_seller_seq
-- chat_msg: chat_room_seq, (chat_room_seq, read_yn)
-- login_info: admin_seq, member_seq, login_dtm
-- book_club: book_club_deleted_dt, book_club_leader_seq
-- book_club_member: book_club_seq, member_seq, join_st + (book_club_seq, member_seq) (UNIQUE)
-- book_club_request: book_club_seq, request_member_seq, request_st
-- book_club_wish: (book_club_seq, member_seq) (UNIQUE)
-- book_club_board: book_club_seq, parent_book_club_board_seq, board_deleted_dtm
-- book_club_board_like: book_club_board_seq + (book_club_board_seq, member_seq) (UNIQUE)
-- notice: (notice_priority, crt_dtm)
-- address_info: member_seq
-- banner: (is_active, order_idx)


-- =============================================
-- [참고] 기존 단일 인덱스 중 복합 인덱스로 대체 가능한 것
-- =============================================
-- 복합 인덱스는 선두 컬럼만으로도 단일 인덱스 역할을 하므로,
-- 아래 기존 인덱스는 복합 인덱스 생성 후 삭제해도 무방합니다.
-- (삭제는 선택 사항이며, 운영 환경에서 EXPLAIN 확인 후 판단 권장)
--
-- DROP INDEX idx_trade_del_dtm ON sb_trade_info;       → idx_trade_list_default (del_dtm, crt_dtm) 로 대체
-- DROP INDEX idx_trade_safe_payment ON sb_trade_info;   → idx_trade_payment_expiry (safe_payment_st, expire_dtm) 로 대체
-- DROP INDEX idx_chat_msg_room ON chat_msg;             → idx_chat_msg_room_time (chat_room_seq, sent_dtm) 로 대체
-- DROP INDEX idx_chat_msg_read ON chat_msg;             → idx_chat_msg_unread (chat_room_seq, read_yn, sender_seq) 로 대체
-- DROP INDEX idx_book_club_deleted ON book_club;        → idx_bookclub_list_default (deleted_dt, crt_dtm) 로 대체
-- DROP INDEX idx_bcm_club ON book_club_member;          → idx_bcm_club_status (book_club_seq, join_st) 로 대체
-- DROP INDEX idx_bcr_club ON book_club_request;         → idx_bcr_club_member_st (club, member, st) 로 대체
-- DROP INDEX idx_bcb_club ON book_club_board;           → idx_bcb_root_posts (club, parent, deleted) 로 대체
-- DROP INDEX idx_book_image_trade ON book_image;        → idx_book_image_trade_sort (trade_seq, sort_seq) 로 대체
-- DROP INDEX idx_login_info_admin ON login_info;        → idx_login_admin_time (admin_seq, login_dtm) 로 대체
-- DROP INDEX idx_login_info_member ON login_info;       → idx_login_member_time (member_seq, login_dtm) 로 대체
-- DROP INDEX idx_notice_priority ON notice;             → idx_notice_active_list (active, priority, crt_dtm) 로 대체
-- DROP INDEX idx_address_member ON address_info;        → idx_address_member_sort (member_seq, default_yn, addr_seq) 로 대체
-- idx_settlement_trade ON settlement(trade_seq)         → settlement.trade_seq UNIQUE 제약의 암묵적 유니크 인덱스로 대체 (생성 불필요)
