
-- =============================================
-- MySQL은 CREATE TYPE ENUM을 지원하지 않음
-- 대신 각 컬럼에서 ENUM(...) 타입을 직접 선언

-- DB 초기화
DROP DATABASE IF EXISTS secondHandBook;
CREATE DATABASE secondHandBook
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
-- =============================================
-- 1. Independent Tables (FK 의존 없음)
-- =============================================


use secondHandBook;
-- 회원 정보
CREATE TABLE IF NOT EXISTS member_info (
    member_seq          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    login_id            VARCHAR(100)    NOT NULL UNIQUE,
    member_pwd          VARCHAR(255)    NOT NULL,
    member_email        VARCHAR(255)    UNIQUE,
    member_tel_no       VARCHAR(20),
    member_nicknm       VARCHAR(50)     UNIQUE,
    member_deleted_dtm  TIMESTAMP       NULL,
    member_last_login_dtm TIMESTAMP     NULL,
    member_st           ENUM('JOIN', 'BAN') NOT NULL DEFAULT 'JOIN',
    crt_dtm             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    upd_dtm             TIMESTAMP       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 관리자
CREATE TABLE IF NOT EXISTS admin (
    admin_seq           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    admin_login_id      VARCHAR(100)    NOT NULL UNIQUE,
    admin_password      VARCHAR(255)    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 카테고리
CREATE TABLE IF NOT EXISTS category (
    category_seq        BIGINT          AUTO_INCREMENT PRIMARY KEY,
    category_nm         VARCHAR(100)    NOT NULL,
    category_sort_seq   BIGINT,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm             TIMESTAMP       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 배너
CREATE TABLE IF NOT EXISTS banner (
    banner_seq          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200),
    subtitle            VARCHAR(200),
    bg_color_from       VARCHAR(20),
    bg_color_to         VARCHAR(20),
    btn_text            VARCHAR(100),
    btn_link            VARCHAR(500),
    icon_name           VARCHAR(100),
    order_idx           INT             DEFAULT 0,
    is_active           TINYINT(1)      DEFAULT 1,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 임시 페이지
CREATE TABLE IF NOT EXISTS temp_page (
    page_seq            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200),
    content             TEXT,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 2. member_info 의존 테이블
-- =============================================

-- 배송지 정보
CREATE TABLE IF NOT EXISTS address_info (
    addr_seq            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_seq          BIGINT          NOT NULL,
    addr_nm             VARCHAR(100),
    post_no             VARCHAR(10),
    addr_h              VARCHAR(200),
    addr_d              VARCHAR(200),
    default_yn          SMALLINT        DEFAULT 0,
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 소셜 로그인 연동
CREATE TABLE IF NOT EXISTS member_oauth (
    member_oauth_seq    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_seq          BIGINT          NOT NULL,
    provider            ENUM('KAKAO', 'NAVER') NOT NULL,
    provider_id         VARCHAR(255)    NOT NULL,
    connected_dt        DATE            DEFAULT (CURRENT_DATE),
    UNIQUE (provider, provider_id),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 3. 중고거래 관련 테이블
-- =============================================

-- 중고거래 정보
CREATE TABLE IF NOT EXISTS sb_trade_info (
    trade_seq                   BIGINT              AUTO_INCREMENT PRIMARY KEY,
    member_seller_seq           BIGINT              NOT NULL,
    member_buyer_seq            BIGINT              NULL,
    category_seq                BIGINT              NULL,
    sale_title                  VARCHAR(200)        NOT NULL,
    book_st                     ENUM('NEW', 'LIKE_NEW', 'GOOD', 'USED'),
    sale_cont                   TEXT,
    sale_price                  INT,
    delivery_cost               INT                 DEFAULT 0,
    sale_rg                     VARCHAR(100),
    sale_st                     ENUM('SALE', 'RESERVED', 'SOLD') DEFAULT 'SALE',
    sale_st_dtm                 TIMESTAMP           NULL,
    views                       BIGINT              DEFAULT 0,
    wish_cnt                    BIGINT              DEFAULT 0,
    book_sale_dtm               TIMESTAMP           NULL,
    post_no                     VARCHAR(10),
    addr_h                      VARCHAR(200),
    addr_d                      VARCHAR(200),
    recipient_ph                VARCHAR(20),
    recipient_nm                VARCHAR(50),
    crt_dtm                     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    upd_dtm                     TIMESTAMP           NULL,
    del_dtm                     TIMESTAMP           NULL,
    payment_type                ENUM('account', 'tosspay'),
    category_nm                 VARCHAR(100),
    isbn                        VARCHAR(20),
    book_title                  VARCHAR(255),
    book_author                 VARCHAR(255),
    book_publisher              VARCHAR(255),
    book_img                    VARCHAR(500),
    book_org_price              INT,
    safe_payment_st             ENUM('NONE','PENDING','COMPLETED') DEFAULT 'NONE',
    safe_payment_expire_dtm     TIMESTAMP           NULL,
    pending_buyer_seq           BIGINT,
    confirm_purchase            TINYINT(1)          NULL,
    settlement_st               ENUM('NONE','READY','REQUESTED','COMPLETED','INSUFFICIENT_BALANCE') DEFAULT 'NONE',
    FOREIGN KEY (member_seller_seq) REFERENCES member_info(member_seq),
    FOREIGN KEY (member_buyer_seq) REFERENCES member_info(member_seq),
    FOREIGN KEY (category_seq) REFERENCES category(category_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 거래 찜
CREATE TABLE IF NOT EXISTS trade_wish (
    trade_wish_seq      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trade_seq           BIGINT          NOT NULL,
    member_seq          BIGINT          NOT NULL,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (trade_seq, member_seq),
    FOREIGN KEY (trade_seq) REFERENCES sb_trade_info(trade_seq),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 거래 이미지
CREATE TABLE IF NOT EXISTS book_image (
    book_img_seq        BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trade_seq           BIGINT          NOT NULL,
    img_url             VARCHAR(500),
    sort_seq            BIGINT          DEFAULT 0,
    FOREIGN KEY (trade_seq) REFERENCES sb_trade_info(trade_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- 4. 채팅 관련 테이블
-- =============================================

-- 채팅방
CREATE TABLE IF NOT EXISTS chatroom (
    chat_room_seq       BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trade_seq           BIGINT          NOT NULL,
    member_buyer_seq    BIGINT          NOT NULL,
    member_seller_seq   BIGINT          NOT NULL,
    last_msg            TEXT,
    last_msg_dtm        TIMESTAMP       NULL,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trade_seq) REFERENCES sb_trade_info(trade_seq),
    FOREIGN KEY (member_buyer_seq) REFERENCES member_info(member_seq),
    FOREIGN KEY (member_seller_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅 메시지
CREATE TABLE IF NOT EXISTS chat_msg (
    chat_msg_seq        BIGINT          AUTO_INCREMENT PRIMARY KEY,
    chat_room_seq       BIGINT          NOT NULL,
    trade_seq           BIGINT,
    sender_seq          BIGINT          NOT NULL,
    chat_cont           TEXT,
    sent_dtm            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    read_yn             TINYINT(1)      DEFAULT 0,
    FOREIGN KEY (chat_room_seq) REFERENCES chatroom(chat_room_seq)
    -- trade_seq FK 제거: 시스템 메시지(결제 알림 등)는 trade_seq가 없어 FK 위반 발생
    -- trade 컨텍스트는 chat_room_seq → chatroom.trade_seq로 조회 가능
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- 5. 로그인 기록
-- =============================================

CREATE TABLE IF NOT EXISTS login_info (
    login_info_seq      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    admin_seq           BIGINT          NULL,
    member_seq          BIGINT          NULL,
    login_dtm           TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    login_ip            VARCHAR(45),
    logout_dtm          TIMESTAMP       NULL,
    logout_ip           VARCHAR(45),
    FOREIGN KEY (admin_seq) REFERENCES admin(admin_seq),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- 6. 독서모임 관련 테이블
-- =============================================

-- 독서모임
CREATE TABLE IF NOT EXISTS book_club (
    book_club_seq           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    book_club_leader_seq    BIGINT          NOT NULL,
    book_club_name          VARCHAR(100)    NOT NULL,
    book_club_desc          TEXT,
    book_club_rg            VARCHAR(100),
    book_club_max_member    INT,
    book_club_deleted_dt    TIMESTAMP       NULL,
    banner_img_url          VARCHAR(500),
    book_club_schedule      VARCHAR(200),
    crt_dtm                 TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm                 TIMESTAMP       NULL,
    FOREIGN KEY (book_club_leader_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 독서모임 멤버
CREATE TABLE IF NOT EXISTS book_club_member (
    book_club_member_seq    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    book_club_seq           BIGINT          NOT NULL,
    member_seq              BIGINT          NOT NULL,
    leader_yn               TINYINT(1)      DEFAULT 0,
    join_st                 ENUM('WAIT', 'JOINED', 'REJECTED', 'LEFT', 'KICKED') DEFAULT 'WAIT',
    join_st_update_dtm      TIMESTAMP       NULL,
    UNIQUE (book_club_seq, member_seq),
    FOREIGN KEY (book_club_seq) REFERENCES book_club(book_club_seq),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 독서모임 가입 신청
CREATE TABLE IF NOT EXISTS book_club_request (
    book_club_request_seq   BIGINT          AUTO_INCREMENT PRIMARY KEY,
    book_club_seq           BIGINT          NOT NULL,
    request_member_seq      BIGINT          NOT NULL,
    request_cont            TEXT,
    request_st              ENUM('WAIT', 'APPROVED', 'REJECTED') DEFAULT 'WAIT',
    request_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    request_processed_dt    DATE            NULL,
    FOREIGN KEY (book_club_seq) REFERENCES book_club(book_club_seq),
    FOREIGN KEY (request_member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 독서모임 찜
CREATE TABLE IF NOT EXISTS book_club_wish (
    book_club_wish_seq      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    book_club_seq           BIGINT          NOT NULL,
    member_seq              BIGINT          NOT NULL,
    crt_dtm                 TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (book_club_seq, member_seq),
    FOREIGN KEY (book_club_seq) REFERENCES book_club(book_club_seq),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 독서모임 게시판
CREATE TABLE IF NOT EXISTS book_club_board (
    book_club_board_seq         BIGINT          AUTO_INCREMENT PRIMARY KEY,
    book_club_seq               BIGINT          NOT NULL,
    member_seq                  BIGINT          NOT NULL,
    parent_book_club_board_seq  BIGINT          NULL,
    board_title                 VARCHAR(200),
    board_cont                  TEXT,
    board_img_url               VARCHAR(500),
    isbn                        VARCHAR(20),
    book_title                  VARCHAR(255),
    book_author                 VARCHAR(255),
    book_img_url                VARCHAR(500),
    board_deleted_dtm           TIMESTAMP       NULL,
    crt_dtm                     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm                     TIMESTAMP       NULL,
    FOREIGN KEY (book_club_seq) REFERENCES book_club(book_club_seq),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq),
    FOREIGN KEY (parent_book_club_board_seq) REFERENCES book_club_board(book_club_board_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 독서모임 게시글 좋아요
CREATE TABLE IF NOT EXISTS book_club_board_like (
    like_seq                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    book_club_board_seq     BIGINT          NOT NULL,
    member_seq              BIGINT          NOT NULL,
    crt_dtm                 TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (book_club_board_seq, member_seq),
    FOREIGN KEY (book_club_board_seq) REFERENCES book_club_board(book_club_board_seq),
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- 7. 공지사항
-- =============================================

CREATE TABLE IF NOT EXISTS notice (
    notice_seq          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    admin_seq           BIGINT          NULL,
    notice_title        VARCHAR(200),
    notice_cont         TEXT,
    notice_priority     INT             DEFAULT 0,
    active              TINYINT(1)      DEFAULT 1,
    view_count          INT             DEFAULT 0,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm             TIMESTAMP       NULL,
    FOREIGN KEY (admin_seq) REFERENCES admin(admin_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- 8. 정산 관련
-- =============================================

CREATE TABLE IF NOT EXISTS settlement (
    settlement_seq          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trade_seq               BIGINT          NOT NULL UNIQUE,  -- 1거래 1정산 보장
    member_seller_seq       BIGINT          NOT NULL,
    sale_price              INT,
    delivery_cost           INT             DEFAULT 0,
    commission_rate         DECIMAL(5,4),
    commission              INT,
    settlement_amount       INT,
    settlement_st           ENUM('NONE','READY','REQUESTED','COMPLETED','INSUFFICIENT_BALANCE') DEFAULT 'REQUESTED',
    transfer_confirmed_yn   TINYINT(1)      DEFAULT 0,  -- 관리자 이체 완료 확인 여부
    request_dtm             TIMESTAMP       NULL,
    settled_dtm             TIMESTAMP       NULL,
    crt_dtm                 TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm                 TIMESTAMP       NULL,
    FOREIGN KEY (trade_seq) REFERENCES sb_trade_info(trade_seq),
    FOREIGN KEY (member_seller_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 판매자 정산 계좌 정보
-- 배치가 COMPLETED 처리 후 이 테이블의 계좌로 관리자가 수동 이체
CREATE TABLE IF NOT EXISTS member_bank_account (
    bank_account_seq    BIGINT          AUTO_INCREMENT PRIMARY KEY,
    member_seq          BIGINT          NOT NULL UNIQUE,   -- 1인 1계좌
    bank_code           VARCHAR(10)     NOT NULL,          -- 은행코드 (004=국민, 020=우리 등)
    bank_account_no     VARCHAR(100)    NOT NULL,          -- 계좌번호 (AES-256 암호화 저장, 최대 69자)
    account_holder_nm   VARCHAR(50)     NOT NULL,          -- 예금주명
    verified_yn         TINYINT(1)      DEFAULT 0,         -- 계좌 실명확인 여부 (추후 오픈뱅킹 연동 시 활용)
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm             TIMESTAMP       NULL,
    FOREIGN KEY (member_seq) REFERENCES member_info(member_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 관리자 계좌
CREATE TABLE IF NOT EXISTS admin_account (
    account_seq         BIGINT          AUTO_INCREMENT PRIMARY KEY,
    balance             BIGINT          DEFAULT 0,
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    upd_dtm             TIMESTAMP       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 관리자 계좌 로그
CREATE TABLE IF NOT EXISTS admin_account_log (
    log_seq             BIGINT          AUTO_INCREMENT PRIMARY KEY,
    account_seq         BIGINT          NOT NULL,
    settlement_seq      BIGINT,
    amount              BIGINT,          -- BIGINT: 잔액 변동 금액 (음수=차감, 0=이체확인)
    balance_after       BIGINT,
    description         VARCHAR(500),
    crt_dtm             TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_seq) REFERENCES admin_account(account_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- 9. Indexes (성능 최적화)
-- =============================================

-- 회원
CREATE INDEX idx_member_info_login_id ON member_info(login_id);
CREATE INDEX idx_member_info_email ON member_info(member_email);
CREATE INDEX idx_member_info_deleted ON member_info(member_deleted_dtm);

-- 거래
CREATE INDEX idx_trade_seller ON sb_trade_info(member_seller_seq);
CREATE INDEX idx_trade_buyer ON sb_trade_info(member_buyer_seq);
CREATE INDEX idx_trade_del_dtm ON sb_trade_info(del_dtm);
CREATE INDEX idx_trade_sale_st ON sb_trade_info(sale_st);
CREATE INDEX idx_trade_category ON sb_trade_info(category_seq);
CREATE INDEX idx_trade_crt_dtm ON sb_trade_info(crt_dtm);
CREATE INDEX idx_trade_safe_payment ON sb_trade_info(safe_payment_st);

-- 거래 찜
CREATE INDEX idx_trade_wish_trade ON trade_wish(trade_seq);
CREATE INDEX idx_trade_wish_member ON trade_wish(member_seq);

-- 거래 이미지
CREATE INDEX idx_book_image_trade ON book_image(trade_seq);

-- 채팅방
CREATE INDEX idx_chatroom_trade ON chatroom(trade_seq);
CREATE INDEX idx_chatroom_buyer ON chatroom(member_buyer_seq);
CREATE INDEX idx_chatroom_seller ON chatroom(member_seller_seq);

-- 채팅 메시지
CREATE INDEX idx_chat_msg_room ON chat_msg(chat_room_seq);
CREATE INDEX idx_chat_msg_read ON chat_msg(chat_room_seq, read_yn);

-- 로그인 기록
CREATE INDEX idx_login_info_admin ON login_info(admin_seq);
CREATE INDEX idx_login_info_member ON login_info(member_seq);
CREATE INDEX idx_login_info_dtm ON login_info(login_dtm);

-- 독서모임
CREATE INDEX idx_book_club_deleted ON book_club(book_club_deleted_dt);
CREATE INDEX idx_book_club_leader ON book_club(book_club_leader_seq);

-- 독서모임 멤버
CREATE INDEX idx_bcm_club ON book_club_member(book_club_seq);
CREATE INDEX idx_bcm_member ON book_club_member(member_seq);
CREATE INDEX idx_bcm_join_st ON book_club_member(join_st);

-- 독서모임 가입 신청
CREATE INDEX idx_bcr_club ON book_club_request(book_club_seq);
CREATE INDEX idx_bcr_member ON book_club_request(request_member_seq);
CREATE INDEX idx_bcr_st ON book_club_request(request_st);

-- 독서모임 게시판
CREATE INDEX idx_bcb_club ON book_club_board(book_club_seq);
CREATE INDEX idx_bcb_parent ON book_club_board(parent_book_club_board_seq);
CREATE INDEX idx_bcb_deleted ON book_club_board(board_deleted_dtm);

-- 독서모임 좋아요
CREATE INDEX idx_bcbl_board ON book_club_board_like(book_club_board_seq);

-- 정산
CREATE INDEX idx_settlement_st ON settlement(settlement_st);
CREATE INDEX idx_settlement_transfer ON settlement(settlement_st, transfer_confirmed_yn);

-- 판매자 계좌
CREATE INDEX idx_bank_account_member ON member_bank_account(member_seq);

-- 공지사항
CREATE INDEX idx_notice_priority ON notice(notice_priority, crt_dtm);

-- 주소
CREATE INDEX idx_address_member ON address_info(member_seq);

-- 배너
CREATE INDEX idx_banner_active ON banner(is_active, order_idx);


-- =============================================
-- 11. 초기 데이터
-- =============================================

-- 카테고리
INSERT INTO category (category_nm, category_sort_seq) VALUES
    ('소설', 1),
    ('시/에세이', 2),
    ('인문', 3),
    ('역사', 4),
    ('사회/정치', 5),
    ('경제/경영', 6),
    ('자기계발', 7),
    ('과학', 8),
    ('IT/프로그래밍', 9),
    ('예술/대중문화', 10),
    ('여행', 11),
    ('요리', 12),
    ('건강', 13),
    ('종교', 14),
    ('아동', 15),
    ('청소년', 16),
    ('만화', 17),
    ('외국어', 18),
    ('수험서/자격증', 19),
    ('참고서', 20);

-- 관리자 계정 (id: admin, pw: 1234, BCrypt)
INSERT INTO admin (admin_login_id, admin_password) VALUES
    ('admin', '$2y$10$iCNAQBozFX2PQqVJZAmkYOrjMNDPo.n3hGD6CAxV7BCoSy7BuRgr2');

-- 관리자 계좌 초기화
INSERT INTO admin_account (balance) VALUES (0);



-- ====================== 스프링 배치 ===========================
CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE (
  JOB_INSTANCE_ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  VERSION BIGINT,
  JOB_NAME VARCHAR(100) NOT NULL,
  JOB_KEY VARCHAR(32) NOT NULL,
  UNIQUE KEY JOB_INST_UN (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION (
  JOB_EXECUTION_ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  VERSION BIGINT,
  JOB_INSTANCE_ID BIGINT NOT NULL,
  CREATE_TIME DATETIME(6) NOT NULL,
  START_TIME DATETIME(6) DEFAULT NULL,
  END_TIME DATETIME(6) DEFAULT NULL,
  STATUS VARCHAR(10),
  EXIT_CODE VARCHAR(2500),
  EXIT_MESSAGE VARCHAR(2500),
  LAST_UPDATED DATETIME(6),
  JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
  CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
  JOB_EXECUTION_ID BIGINT NOT NULL,
  TYPE_CD VARCHAR(6) NOT NULL,
  KEY_NAME VARCHAR(100) NOT NULL,
  STRING_VAL VARCHAR(250),
  DATE_VAL DATETIME(6) DEFAULT NULL,
  LONG_VAL BIGINT,
  DOUBLE_VAL DOUBLE PRECISION,
  IDENTIFYING CHAR(1) NOT NULL,
  CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION (
  STEP_EXECUTION_ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  VERSION BIGINT NOT NULL,
  STEP_NAME VARCHAR(100) NOT NULL,
  JOB_EXECUTION_ID BIGINT NOT NULL,
  START_TIME DATETIME(6) NOT NULL,
  END_TIME DATETIME(6) DEFAULT NULL,
  STATUS VARCHAR(10),
  COMMIT_COUNT BIGINT,
  READ_COUNT BIGINT,
  FILTER_COUNT BIGINT,
  WRITE_COUNT BIGINT,
  READ_SKIP_COUNT BIGINT,
  WRITE_SKIP_COUNT BIGINT,
  PROCESS_SKIP_COUNT BIGINT,
  ROLLBACK_COUNT BIGINT,
  EXIT_CODE VARCHAR(2500),
  EXIT_MESSAGE VARCHAR(2500),
  LAST_UPDATED DATETIME(6),
  CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT (
  STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
  SHORT_CONTEXT VARCHAR(2500) NOT NULL,
  SERIALIZED_CONTEXT TEXT,
  CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT (
  JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
  SHORT_CONTEXT VARCHAR(2500) NOT NULL,
  SERIALIZED_CONTEXT TEXT,
  CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

-- ID 생성용 테이블
CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ (ID BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO BATCH_STEP_EXECUTION_SEQ VALUES(0);
CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ (ID BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO BATCH_JOB_EXECUTION_SEQ VALUES(0);
CREATE TABLE IF NOT EXISTS BATCH_JOB_SEQ (ID BIGINT NOT NULL) ENGINE=InnoDB;
INSERT INTO BATCH_JOB_SEQ VALUES(0);
