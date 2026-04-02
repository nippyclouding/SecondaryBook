package spring.batch.job.settlement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import spring.batch.member.BatchMailService;
import spring.batch.member.MemberMapper;
import spring.batch.settlement.BatchSettlementService;
import spring.batch.settlement.SettlementMapper;
import spring.batch.settlement.SettlementStatus;
import spring.batch.settlement.SettlementVO;
import spring.batch.util.AesEncryptionUtil;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 정산 배치 통합 테스트 (Spring context 없이 컴포넌트 조합 검증).
 */
@ExtendWith(MockitoExtension.class)
class SettlementBatchIntegrationTest {

    @Mock private SettlementMapper settlementMapper;
    @Mock private AesEncryptionUtil aesEncryptionUtil;
    @Mock private MemberMapper memberMapper;
    @Mock private BatchMailService batchMailService;

    @InjectMocks
    private BatchSettlementService batchSettlementService;

    private SettlementItemWriter writer;
    private SettlementSkipListener skipListener;

    @BeforeEach
    void setUp() {
        writer = new SettlementItemWriter(settlementMapper);
        skipListener = new SettlementSkipListener(batchSettlementService);
    }

    @Nested
    @DisplayName("잔액 부족 흐름: Writer → skip → Listener → DB 상태 갱신")
    class InsufficientBalanceFlow {

        @Test
        @DisplayName("잔액 부족 → InsufficientBalanceException → 두 테이블 INSUFFICIENT_BALANCE 갱신")
        void insufficientBalance_WriterThrows_ListenerUpdatesStatus() {
            SettlementVO settlement = createSettlement(1L, 10L, 50000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100L);

            InsufficientBalanceException caught = catchThrowableOfType(
                    () -> writer.write(new Chunk<>(settlement)),
                    InsufficientBalanceException.class
            );
            assertThat(caught).isNotNull();

            skipListener.onSkipInWrite(settlement, caught);

            verify(settlementMapper).updateToInsufficient(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.INSUFFICIENT_BALANCE);
            verify(settlementMapper, never()).updateAdminBalance(anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("정상 처리 흐름")
    class SuccessFlow {

        @Test
        @DisplayName("정상 처리 → skipListener.onSkipInWrite 호출 없음")
        void successfulBatch_ListenerNotInvoked() throws Exception {
            SettlementVO settlement = createSettlement(1L, 10L, 5000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(new Chunk<>(settlement));

            verify(settlementMapper, never()).updateToInsufficient(anyLong());
            verify(settlementMapper).updateToCompleted(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.COMPLETED);
            verify(settlementMapper).updateAdminBalance(1L, 5000);
        }
    }

    @Nested
    @DisplayName("동시 배치 감지: updateToCompleted=0 → IllegalStateException")
    class ConcurrentBatchDetection {

        @Test
        @DisplayName("이미 처리된 건 → IllegalStateException")
        void alreadyProcessed_ThrowsIllegalStateException() {
            SettlementVO settlement = createSettlement(1L, 10L, 5000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(0);

            assertThatThrownBy(() -> writer.write(new Chunk<>(settlement)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("동시 배치 실행 감지");
        }
    }

    private SettlementVO createSettlement(long seq, long tradeSeq, int amount) {
        SettlementVO s = new SettlementVO();
        s.setSettlement_seq(seq);
        s.setTrade_seq(tradeSeq);
        s.setSettlement_amount(amount);
        return s;
    }
}
