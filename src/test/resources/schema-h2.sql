-- H2 인메모리 DB 테스트 스키마 (MySQL MODE 호환)
-- ENUM → VARCHAR, FK 제약 제거 (테스트 데이터 삽입 순서 자유)

-- ==================== member_info ====================
CREATE TABLE IF NOT EXISTS MEMBER_INFO (
    MEMBER_SEQ            BIGINT AUTO_INCREMENT PRIMARY KEY,
    LOGIN_ID              VARCHAR(100) NOT NULL,
    MEMBER_PWD            VARCHAR(255) NOT NULL,
    MEMBER_EMAIL          VARCHAR(255),
    MEMBER_TEL_NO         VARCHAR(20),
    MEMBER_NICKNM         VARCHAR(50),
    MEMBER_DELETED_DTM    TIMESTAMP NULL,
    MEMBER_LAST_LOGIN_DTM TIMESTAMP NULL,
    MEMBER_ST             VARCHAR(10) NOT NULL DEFAULT 'JOIN',
    CRT_DTM               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPD_DTM               TIMESTAMP NULL
);

-- ==================== member_oauth ====================
CREATE TABLE IF NOT EXISTS MEMBER_OAUTH (
    MEMBER_OAUTH_SEQ BIGINT AUTO_INCREMENT PRIMARY KEY,
    MEMBER_SEQ       BIGINT NOT NULL,
    PROVIDER         VARCHAR(10) NOT NULL,
    PROVIDER_ID      VARCHAR(255) NOT NULL,
    CONNECTED_DT     DATE DEFAULT CURRENT_DATE
);

-- ==================== member_bank_account ====================
CREATE TABLE IF NOT EXISTS member_bank_account (
    bank_account_seq  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_seq        BIGINT NOT NULL,
    bank_code         VARCHAR(10),
    bank_account_no   VARCHAR(100),
    account_holder_nm VARCHAR(50),
    verified_yn       TINYINT(1) DEFAULT 0,
    crt_dtm           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    upd_dtm           TIMESTAMP NULL
);

-- ==================== sb_trade_info ====================
CREATE TABLE IF NOT EXISTS sb_trade_info (
    trade_seq           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_seller_seq   BIGINT NOT NULL,
    member_buyer_seq    BIGINT,
    category_seq        BIGINT NOT NULL DEFAULT 1,
    book_info_seq       BIGINT NOT NULL DEFAULT 1,
    settlement_seq      BIGINT,
    pending_buyer_seq   BIGINT,
    sale_title          VARCHAR(200) NOT NULL,
    book_st             VARCHAR(20)  DEFAULT 'GOOD',
    sale_cont           VARCHAR(1000),
    sale_price          INT          NOT NULL DEFAULT 0,
    delivery_cost       INT          NOT NULL DEFAULT 0,
    sale_rg             VARCHAR(100),
    sale_st             VARCHAR(20)  DEFAULT 'SALE',
    sale_st_dtm         TIMESTAMP NULL,
    views               BIGINT       DEFAULT 0,
    wish_cnt            BIGINT       DEFAULT 0,
    book_sale_dtm       TIMESTAMP NULL,
    post_no             VARCHAR(10),
    addr_h              VARCHAR(200),
    addr_d              VARCHAR(200),
    recipient_ph        VARCHAR(20),
    recipient_nm        VARCHAR(50),
    upd_dtm             TIMESTAMP NULL,
    payment_type        VARCHAR(20)  DEFAULT 'ALL',
    category_nm         VARCHAR(100),
    settlement_st       VARCHAR(30)  DEFAULT 'READY',
    isbn                VARCHAR(50),
    book_title          VARCHAR(255),
    book_author         VARCHAR(255),
    book_publisher      VARCHAR(255),
    book_img            VARCHAR(500),
    book_org_price      INT          DEFAULT 0,
    safe_payment_st     VARCHAR(20)  DEFAULT 'NONE',
    safe_payment_expire_dtm TIMESTAMP NULL,
    confirm_purchase    TINYINT(1)   DEFAULT 0,
    crt_dtm             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==================== settlement ====================
CREATE TABLE IF NOT EXISTS settlement (
    settlement_seq        BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_seq             BIGINT NOT NULL UNIQUE,
    member_seller_seq     BIGINT NOT NULL,
    sale_price            INT    NOT NULL DEFAULT 0,
    delivery_cost         INT    NOT NULL DEFAULT 0,
    commission_rate       DECIMAL(5,4) DEFAULT 0.0100,
    commission            INT    DEFAULT 0,
    settlement_amount     INT    NOT NULL DEFAULT 0,
    settlement_st         VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    transfer_confirmed_yn TINYINT(1)  DEFAULT 0,
    request_dtm           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_dtm           TIMESTAMP NULL,
    crt_dtm               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    upd_dtm               TIMESTAMP NULL
);

-- ==================== admin_account ====================
CREATE TABLE IF NOT EXISTS admin_account (
    account_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    balance     BIGINT NOT NULL DEFAULT 0,
    upd_dtm     TIMESTAMP NULL
);

-- ==================== admin_account_log ====================
CREATE TABLE IF NOT EXISTS admin_account_log (
    log_seq       BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_seq   BIGINT NOT NULL,
    settlement_seq BIGINT NOT NULL,
    amount        BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    description   VARCHAR(200),
    crt_dtm       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
