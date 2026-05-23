package project.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.trade.TradeMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventService")
class PaymentEventServiceTest {

    @Mock PaymentEventMapper paymentEventMapper;
    @Mock TradeMapper tradeMapper;

    @InjectMocks PaymentEventService paymentEventService;

    @Test
    @DisplayName("승인 진입은 현재 결제 시도 만료시각을 조건으로 전달하고 이벤트를 기록한다")
    void beginConfirmation_passesAttemptExpiryAndRecordsConfirmation() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 5, 23, 14, 5);
        when(tradeMapper.beginSafePaymentConfirmation(100L, 2L, expiresAt)).thenReturn(1);

        boolean result = paymentEventService.beginConfirmation(
                100L, 2L, "pay-key", "order-id", 13000, expiresAt);

        assertThat(result).isTrue();
        verify(tradeMapper).beginSafePaymentConfirmation(100L, 2L, expiresAt);
        ArgumentCaptor<PaymentEventVO> eventCaptor = ArgumentCaptor.forClass(PaymentEventVO.class);
        verify(paymentEventMapper).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEvent_type()).isEqualTo(PaymentEventType.CONFIRMING);
        assertThat(eventCaptor.getValue().getTrade_seq()).isEqualTo(100L);
    }
}
