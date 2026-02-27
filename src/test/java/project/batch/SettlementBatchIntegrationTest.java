package project.batch;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.member.MailService;
import project.member.MemberBankAccountMapper;
import project.member.MemberMapper;
import project.settlement.SettlementMapper;
import project.settlement.SettlementService;
import project.settlement.SettlementStatus;
import project.settlement.SettlementVO;
import project.trade.TradeMapper;
import project.util.AesEncryptionUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 정산 배치 통합 테스트 (Spring context 없이 컴포넌트 조합 검증).
 *
 * <p>실제 Spring Batch Job 실행 없이 Writer → Exception → SkipListener → Service 흐름을
 * 직접 연결하여 INSUFFICIENT_BALANCE 처리 시나리오를 검증한다.</p>
 */
@ExtendWith(MockitoExtension.class)
class SettlementBatchIntegrationTest {

    @Mock private SettlementMapper settlementMapper;
    @Mock private TradeMapper tradeMapper;
    @Mock private AesEncryptionUtil aesEncryptionUtil;
    @Mock private MemberMapper memberMapper;
    @Mock private MailService mailService;
    @Mock private MemberBankAccountMapper memberBankAccountMapper;

    @InjectMocks
    private SettlementService settlementService;

    private SettlementItemWriter writer;
    private SettlementSkipListener skipListener;

    @BeforeEach
    void setUp() {
        writer = new SettlementItemWriter(settlementMapper);
        skipListener = new SettlementSkipListener(settlementService);
    }

    // ========== 잔액 부족 전체 흐름 ==========

    @Nested
    @DisplayName("잔액 부족 흐름: Writer → skip → Listener → DB 상태 갱신")
    class InsufficientBalanceFlow {

        @Test
        @DisplayName("잔액 부족 → InsufficientBalanceException → 두 테이블 INSUFFICIENT_BALANCE 갱신")
        void insufficientBalance_WriterThrows_ListenerUpdatesStatus() {
            SettlementVO settlement = createSettlement(1L, 10L, 50000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100L); // 잔액 100 < 필요 50000

            // ① Writer: InsufficientBalanceException 발생
            InsufficientBalanceException caught = catchThrowableOfType(
                    () -> writer.write(List.of(settlement)),
                    InsufficientBalanceException.class
            );
            assertThat(caught).isNotNull();

            // ② Spring Batch가 SkipListener.onSkipInWrite() 호출 (시뮬레이션)
            skipListener.onSkipInWrite(settlement, caught);

            // ③ DB 상태 갱신 검증
            verify(settlementMapper).updateToInsufficient(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.INSUFFICIENT_BALANCE);

            // ④ 잔액 차감 없음
            verify(settlementMapper, never()).updateAdminBalance(anyLong(), anyInt());
            verify(settlementMapper, never()).insertAccountLog(anyLong(), anyLong(), anyInt(), anyLong(), anyString());
        }

        @Test
        @DisplayName("잔액 부족 건은 updateToCompleted 미호출 (REQUESTED 상태 유지)")
        void insufficientBalance_SettlementRemainsRequested() {
            SettlementVO settlement = createSettlement(1L, 10L, 50000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100L);

            catchThrowableOfType(
                    () -> writer.write(List.of(settlement)),
                    InsufficientBalanceException.class
            );

            // REQUESTED → COMPLETED 변경 없음 (rollback)
            verify(settlementMapper, never()).updateToCompleted(anyLong());
        }
    }

    // ========== 정상 처리 전체 흐름 ==========

    @Nested
    @DisplayName("정상 처리 흐름: Writer 성공 → SkipListener 미호출")
    class SuccessFlow {

        @Test
        @DisplayName("정상 처리 → skipListener.onSkipInWrite 호출 없음")
        void successfulBatch_ListenerNotInvoked() throws Exception {
            SettlementVO settlement = createSettlement(1L, 10L, 5000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(List.of(settlement));

            // 정상 처리 → INSUFFICIENT_BALANCE 갱신 없음
            verify(settlementMapper, never()).updateToInsufficient(anyLong());
            verify(settlementMapper, never()).updateTradeSettlementSt(anyLong(), eq(SettlementStatus.INSUFFICIENT_BALANCE));

            // 정상 처리 DB 작업 확인
            verify(settlementMapper).updateToCompleted(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.COMPLETED);
            verify(settlementMapper).updateAdminBalance(1L, 5000);
        }

        @Test
        @DisplayName("정상 처리 후 계좌 정보 포함된 로그 기록")
        void successfulBatch_LogContainsBankInfo() throws Exception {
            SettlementVO settlement = createSettlement(1L, 10L, 12870);
            settlement.setBank_code("004");
            settlement.setBank_account_no("123456789012");
            settlement.setAccount_holder_nm("홍길동");

            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(List.of(settlement));

            verify(settlementMapper).insertAccountLog(
                    eq(1L), eq(1L), eq(-12870), eq(87130L),
                    argThat(desc -> desc.contains("홍길동")
                            && desc.contains("004")
                            && desc.contains("9012"))
            );
        }
    }

    // ========== 동시 배치 감지 흐름 ==========

    @Nested
    @DisplayName("동시 배치 감지: updateToCompleted=0 → IllegalStateException")
    class ConcurrentBatchDetection {

        @Test
        @DisplayName("이미 처리된 건 → IllegalStateException (skip 아닌 Job 실패)")
        void alreadyProcessed_ThrowsIllegalStateException() {
            SettlementVO settlement = createSettlement(1L, 10L, 5000);
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(0); // 이미 처리됨

            assertThatThrownBy(() -> writer.write(List.of(settlement)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("동시 배치 실행 감지");

            // InsufficientBalanceException이 아니므로 skipListener는 INSUFFICIENT_BALANCE로 갱신하지 않음
            skipListener.onSkipInWrite(settlement, new IllegalStateException("이미 처리"));
            verify(settlementMapper, never()).updateToInsufficient(anyLong());
        }
    }

    // ========== 헬퍼 ==========

    private SettlementVO createSettlement(long seq, long tradeSeq, int amount) {
        SettlementVO s = new SettlementVO();
        s.setSettlement_seq(seq);
        s.setTrade_seq(tradeSeq);
        s.setSettlement_amount(amount);
        return s;
    }
}
