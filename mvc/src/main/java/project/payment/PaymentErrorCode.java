package project.payment;

import lombok.Getter;

@Getter
public enum PaymentErrorCode {
    TRADE_NOT_FOUND("PAYMENT_001", "거래 정보를 찾을 수 없습니다."),
    SESSION_EXPIRED("PAYMENT_002", "세션이 만료되었습니다. 다시 결제를 시도해주세요."),
    AMOUNT_MISMATCH("PAYMENT_003", "결제 금액이 일치하지 않습니다."),
    PAYMENT_UNAVAILABLE("PAYMENT_004", "이미 판매 완료된 상품이거나 결제 요청시간이 만료된 상품입니다."),
    PAYMENT_FORBIDDEN("PAYMENT_005", "결제 권한이 없거나 결제 요청이 만료되었습니다."),
    PAYMENT_TIMEOUT("PAYMENT_006", "결제 시간이 만료되었습니다. 다시 시도해주세요."),
    CONFIRM_IN_PROGRESS("PAYMENT_007", "결제 요청시간이 만료되었거나 이미 처리 중인 결제입니다."),
    EVENT_LOG_FAILURE("PAYMENT_008", "결제 처리 준비 중 오류가 발생했습니다. 다시 시도해주세요."),
    TOSS_CONFIRM_FAILURE("PAYMENT_009", "결제 승인 실패"),
    CONFIRM_UNKNOWN("PAYMENT_010", "결제 승인 결과를 확인 중입니다. 확인 후 자동 처리됩니다."),
    INVALID_ADDRESS("PAYMENT_011", "배송지 정보가 올바르지 않습니다. 결제가 자동 취소되었습니다."),
    CANCEL_REVIEW_REQUIRED("PAYMENT_012", "결제 또는 취소 처리 상태 확인이 필요합니다. 고객센터로 문의해 주세요."),
    PROCESSING_FAILURE("PAYMENT_013", "결제 처리 중 오류가 발생하여 자동 취소되었습니다. 다시 시도해주세요."),
    NOT_AUTHENTICATED("PAYMENT_014", "로그인이 필요합니다."),
    NOT_PENDING("PAYMENT_015", "진행 중인 결제를 찾을 수 없습니다."),
    ACTION_FORBIDDEN("PAYMENT_016", "권한이 없습니다.");

    private final String code;
    private final String message;

    PaymentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
