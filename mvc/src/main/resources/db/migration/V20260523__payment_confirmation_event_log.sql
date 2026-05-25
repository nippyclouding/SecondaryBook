-- Existing MySQL databases must run this once before deploying the application.
UPDATE sb_trade_info
SET safe_payment_st = 'NONE'
WHERE safe_payment_st IS NULL;

ALTER TABLE sb_trade_info
    MODIFY safe_payment_st ENUM('NONE','PENDING','CONFIRMING','COMPLETED') NOT NULL DEFAULT 'NONE';

-- payment_key is retained for reconciliation/cancel recovery. Do not add foreign keys:
-- payment audit history must remain queryable after logical deletion of a member or trade.
-- Keep payment_event_log for at least 5 years as a payment/supply transaction record
-- under the Korean Electronic Commerce Consumer Protection Act enforcement decree.
CREATE TABLE IF NOT EXISTS payment_event_log (
    payment_event_seq       BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trade_seq               BIGINT          NOT NULL,
    member_seq              BIGINT          NULL,
    event_type              ENUM('CONFIRMING','CONFIRM_UNKNOWN','SUCCESS','TOSS_FAIL','USER_CANCEL','PAGE_LEAVE','TIMEOUT','EXPIRED_BY_SCHEDULER','CANCEL_FAILED','RECONCILE_REQUIRED','RECONCILED_CANCEL','RECONCILED_FAILURE') NOT NULL,
    payment_key             VARCHAR(200)    NULL,
    order_id                VARCHAR(100)    NULL,
    amount                  INT             NULL,
    method                  VARCHAR(50)     NULL,
    toss_status             VARCHAR(50)     NULL,
    toss_code               VARCHAR(100)    NULL,
    toss_message            VARCHAR(500)    NULL,
    created_dtm             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_event_trade_latest (trade_seq, payment_event_seq),
    INDEX idx_payment_event_type (event_type, created_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE payment_event_log
    MODIFY event_type ENUM('CONFIRMING','CONFIRM_UNKNOWN','SUCCESS','TOSS_FAIL','USER_CANCEL','PAGE_LEAVE','TIMEOUT','EXPIRED_BY_SCHEDULER','CANCEL_FAILED','RECONCILE_REQUIRED','RECONCILED_CANCEL','RECONCILED_FAILURE') NOT NULL;
