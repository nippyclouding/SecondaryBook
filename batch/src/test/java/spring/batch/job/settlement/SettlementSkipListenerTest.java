package spring.batch.job.settlement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.batch.settlement.BatchSettlementService;
import spring.batch.settlement.SettlementVO;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementSkipListenerTest {

    @InjectMocks
    private SettlementSkipListener listener;

    @Mock
    private BatchSettlementService batchSettlementService;

    private SettlementVO settlement;

    @BeforeEach
    void setUp() {
        settlement = new SettlementVO();
        settlement.setSettlement_seq(1L);
        settlement.setTrade_seq(10L);
        settlement.setMember_seller_seq(42L);
        settlement.setSettlement_amount(5000);
    }

    @Nested
    @DisplayName("onSkipInWrite - 쓰기 skip 처리")
    class OnSkipInWrite {

        @Test
        @DisplayName("InsufficientBalanceException → markAsInsufficient 호출")
        void callsMarkAsInsufficientOnBalanceShortage() {
            listener.onSkipInWrite(settlement, new InsufficientBalanceException("잔액 부족"));
            verify(batchSettlementService).markAsInsufficient(1L, 10L, 42L);
        }

        @Test
        @DisplayName("다른 예외 → markAsInsufficient 미호출")
        void ignoresOtherExceptions() {
            listener.onSkipInWrite(settlement, new RuntimeException("DB 연결 오류"));
            verify(batchSettlementService, never()).markAsInsufficient(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("IllegalStateException → markAsInsufficient 미호출")
        void ignoresIllegalStateException() {
            listener.onSkipInWrite(settlement, new IllegalStateException("이미 처리된 건"));
            verify(batchSettlementService, never()).markAsInsufficient(anyLong(), anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("onSkipInRead / onSkipInProcess - no-op 검증")
    class NoOpListeners {

        @Test
        @DisplayName("onSkipInRead - 서비스 미호출")
        void onSkipInReadIsNoOp() {
            listener.onSkipInRead(new RuntimeException("읽기 오류"));
            verify(batchSettlementService, never()).markAsInsufficient(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("onSkipInProcess - 서비스 미호출")
        void onSkipInProcessIsNoOp() {
            listener.onSkipInProcess(settlement, new RuntimeException("처리 오류"));
            verify(batchSettlementService, never()).markAsInsufficient(anyLong(), anyLong(), anyLong());
        }
    }
}
