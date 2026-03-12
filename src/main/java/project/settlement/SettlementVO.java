package project.settlement;

import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import project.settlement.SettlementStatus;

@Data
public class SettlementVO implements Serializable {
    private long settlement_seq;
    private long trade_seq;
    private long member_seller_seq;

    // 금액
    private int sale_price;
    private int delivery_cost;
    private BigDecimal commission_rate; // 0.01 = 1%
    private int commission;            // 수수료 금액
    private int settlement_amount;     // 정산 금액

    // 상태
    private SettlementStatus settlement_st; // REQUESTED, COMPLETED
    private boolean transfer_confirmed_yn; // 관리자 이체 완료 확인 여부

    // 일시
    private LocalDateTime request_dtm;
    private LocalDateTime settled_dtm;
    private LocalDateTime crt_dtm;
    private LocalDateTime upd_dtm;

    // JOIN 조회용 (관리자 페이지)
    private String sale_title;
    private String member_seller_nm;   // 판매자 닉네임

    // JOIN 조회용 (배치 처리 + 이체 목록)
    private String bank_code;          // 은행코드
    @ToString.Exclude
    private String bank_account_no;    // 계좌번호 (로그 노출 방지)
    private String account_holder_nm;  // 예금주명
}
