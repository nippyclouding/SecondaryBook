package spring.batch.settlement;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementVO implements Serializable {
    private long settlement_seq;
    private long trade_seq;
    private long member_seller_seq;

    private int sale_price;
    private int delivery_cost;
    private BigDecimal commission_rate;
    private int commission;
    private int settlement_amount;

    private SettlementStatus settlement_st;
    private boolean transfer_confirmed_yn;

    private LocalDateTime request_dtm;
    private LocalDateTime settled_dtm;
    private LocalDateTime crt_dtm;
    private LocalDateTime upd_dtm;

    private String sale_title;
    private String member_seller_nm;

    private String bank_code;
    @ToString.Exclude
    private String bank_account_no;
    private String account_holder_nm;
}
