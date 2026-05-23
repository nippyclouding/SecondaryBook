package project.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trade.TradeMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentEventService {

    private final PaymentEventMapper paymentEventMapper;
    private final TradeMapper tradeMapper;

    @Transactional
    public boolean beginConfirmation(Long tradeSeq, Long memberSeq, String paymentKey, String orderId,
                                     Integer amount, LocalDateTime safePaymentExpireDtm) {
        if (tradeMapper.beginSafePaymentConfirmation(tradeSeq, memberSeq, safePaymentExpireDtm) == 0) {
            return false;
        }
        record(tradeSeq, memberSeq, PaymentEventType.CONFIRMING, paymentKey, orderId, amount, null);
        return true;
    }

    @Transactional
    public void record(Long tradeSeq, Long memberSeq, PaymentEventType eventType,
                       String paymentKey, String orderId, Integer amount,
                       TossPaymentResponse tossResponse) {
        PaymentEventVO event = new PaymentEventVO();
        event.setTrade_seq(tradeSeq);
        event.setMember_seq(memberSeq);
        event.setEvent_type(eventType);
        event.setPayment_key(paymentKey);
        event.setOrder_id(orderId);
        event.setAmount(amount);
        if (tossResponse != null) {
            event.setMethod(tossResponse.getMethod());
            event.setToss_status(tossResponse.getStatus());
            event.setToss_code(tossResponse.getCode());
            event.setToss_message(tossResponse.getMessage());
        }
        paymentEventMapper.save(event);
    }

    public void record(Long tradeSeq, Long memberSeq, PaymentEventType eventType) {
        record(tradeSeq, memberSeq, eventType, null, null, null, null);
    }

    public List<PaymentEventVO> findUnresolvedConfirmUnknownEvents() {
        return paymentEventMapper.findUnresolvedConfirmUnknownEvents();
    }
}
