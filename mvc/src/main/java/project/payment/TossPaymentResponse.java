package project.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TossPaymentResponse {

    private String paymentKey;      // 결제 고유 키
    private String orderId;         // 주문 ID
    private String status;          // 결제 상태 (DONE, CANCELED, FAILED 등)
    private String method;          // 결제 수단 (카드, 계좌이체 등)
    private int totalAmount;        // 총 결제 금액
    private String approvedAt;      // 결제 승인 시각

    // 에러 정보 (실패 시)
    private String code;            // 에러 코드
    private String message;         // 에러 메시지

}