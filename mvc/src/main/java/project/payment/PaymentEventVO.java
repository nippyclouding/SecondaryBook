package project.payment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentEventVO {
    private Long payment_event_seq;
    private Long trade_seq;
    private Long member_seq;
    private PaymentEventType event_type;
    private String payment_key;
    private String order_id;
    private Integer amount;
    private String method;
    private String toss_status;
    private String toss_code;
    private String toss_message;
    private LocalDateTime created_dtm;
}
