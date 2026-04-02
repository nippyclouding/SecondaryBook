package project.payment;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import project.trade.TradeVO;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"payment_key", "card_number", "card_company"})
public class PaymentVO implements Serializable {

    private Long payment_seq;
    private Long trade_seq;         // 거래 번호 (FK)
    private Long member_buyer_seq;  // 구매자 회원 번호

    private String payment_key;     // 토스 paymentKey

    private int amount;             // 결제 금액
    private String status;          // 결제 상태 (DONE, CANCELED 등)
    private String method;          // 결제 수단

    private String card_company;    // 카드사
    private String card_number;     // 카드번호 (마스킹)

    private LocalDateTime approved_at;  // 결제 승인 시각
    private LocalDateTime created_at;   // 생성일

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