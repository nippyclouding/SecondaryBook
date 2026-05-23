package project.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentIncidentResponse {
    private final Long tradeSeq;
    private final Long memberSeq;
    private final PaymentEventType eventType;
    private final String orderId;
    private final Integer amount;
    private final String tossStatus;
    private final String tossCode;
    private final String tossMessage;
    private final LocalDateTime createdDtm;

    public static PaymentIncidentResponse from(PaymentEventVO event) {
        return new PaymentIncidentResponse(
                event.getTrade_seq(),
                event.getMember_seq(),
                event.getEvent_type(),
                event.getOrder_id(),
                event.getAmount(),
                event.getToss_status(),
                event.getToss_code(),
                event.getToss_message(),
                event.getCreated_dtm()
        );
    }
}
