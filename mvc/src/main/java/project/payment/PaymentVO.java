package project.payment;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentVO implements Serializable {

    private int amount;             // 결제 금액
    private String method;          // 결제 수단

    // JSP 출력용 배송 정보 필드 (DB 저장 x)
    private String addr_type;
    private String post_no;
    private String addr_h;
    private String addr_d;

    // 컨트롤러 검증용
    private String safe_payment_status;   // 안전결제 상태 (PENDING / NONE)
    private Long pending_buyer_seq;       // 안전결제 대상 구매자
    private long remaining_seconds;       // 남은 결제 시간(초)
}
