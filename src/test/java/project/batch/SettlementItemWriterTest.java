package project.batch;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.settlement.SettlementMapper;
import project.settlement.SettlementStatus;
import project.settlement.SettlementVO;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementItemWriterTest {

    @InjectMocks
    private SettlementItemWriter writer;

    @Mock
    private SettlementMapper settlementMapper;

    // ========== 정상 처리 ==========

    @Nested
    @DisplayName("write - 정상 처리")
    class WriteSuccess {

        @Test
        @DisplayName("계좌 등록 판매자 - 로그 description에 계좌 정보 포함")
        void writeWithBankAccount() throws Exception {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(12870);
            s1.setBank_code("004");
            s1.setBank_account_no("123456789012");
            s1.setAccount_holder_nm("홍길동");

            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(Collections.singletonList(s1));

            verify(settlementMapper).insertAccountLog(
                    eq(1L), eq(1L), eq(-12870), eq(87130L),
                    argThat(desc -> desc.contains("홍길동")
                            && desc.contains("004")
                            && desc.contains("9012"))
            );
            verify(settlementMapper).updateToCompleted(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.COMPLETED);
            verify(settlementMapper).updateAdminBalance(1L, 12870);
        }

        @Test
        @DisplayName("계좌 미등록 판매자 - 로그 description에 '계좌 미등록' 표시")
        void writeWithoutBankAccount() throws Exception {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(12870);
            // bank_account_no = null (계좌 미등록)

            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(Collections.singletonList(s1));

            verify(settlementMapper).insertAccountLog(
                    eq(1L), eq(1L), eq(-12870), eq(87130L),
                    argThat(desc -> desc.contains("계좌 미등록"))
            );
            verify(settlementMapper).updateToCompleted(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.COMPLETED);
            verify(settlementMapper).updateAdminBalance(1L, 12870);
        }

        @Test
        @DisplayName("복수 건 - chunk=1이므로 write() 2회 호출, 각 건 독립 처리")
        void writeMultipleItemsSequentially() throws Exception {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(10000);
            s1.setBank_code("020");
            s1.setBank_account_no("111111111");
            s1.setAccount_holder_nm("김철수");

            SettlementVO s2 = new SettlementVO();
            s2.setSettlement_seq(2L);
            s2.setTrade_seq(20L);
            s2.setSettlement_amount(20000);
            s2.setBank_code("088");
            s2.setBank_account_no("222222222");
            s2.setAccount_holder_nm("이영희");

            // 각 chunk 트랜잭션에서 최신 잔액을 FOR UPDATE로 조회
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L, 90000L);
            when(settlementMapper.updateToCompleted(anyLong())).thenReturn(1);

            writer.write(Collections.singletonList(s1));  // chunk 1
            writer.write(Collections.singletonList(s2));  // chunk 2

            // 각 건마다 즉시 updateAdminBalance 호출 (합산 1회가 아니라 건별 1회)
            verify(settlementMapper).updateAdminBalance(1L, 10000);
            verify(settlementMapper).updateAdminBalance(1L, 20000);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.COMPLETED);
            verify(settlementMapper).updateTradeSettlementSt(20L, SettlementStatus.COMPLETED);
        }
    }

    // ========== 잔액 부족 ==========

    @Nested
    @DisplayName("write - 잔액 부족")
    class WriteInsufficientBalance {

        @Test
        @DisplayName("잔액 부족 시 InsufficientBalanceException 발생 → Spring Batch가 skip 처리")
        void throwsInsufficientBalanceException() {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(50000);

            when(settlementMapper.getAdminBalance(1L)).thenReturn(100L); // 잔액 부족

            assertThatThrownBy(() -> writer.write(Collections.singletonList(s1)))
                    .isInstanceOf(InsufficientBalanceException.class);

            // DB 변경 없음
            verify(settlementMapper, never()).insertAccountLog(anyLong(), anyLong(), anyInt(), anyLong(), anyString());
            verify(settlementMapper, never()).updateToCompleted(anyLong());
            verify(settlementMapper, never()).updateAdminBalance(anyLong(), anyInt());
        }

        @Test
        @DisplayName("잔액이 정확히 정산금액과 같으면 정상 처리 (경계값)")
        void exactBalanceIsProcessed() throws Exception {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(5000);

            when(settlementMapper.getAdminBalance(1L)).thenReturn(5000L); // 잔액 == 정산금액
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(Collections.singletonList(s1));

            verify(settlementMapper).updateToCompleted(1L);
            verify(settlementMapper).updateAdminBalance(1L, 5000);
        }
    }

    // ========== 관리자 계좌 없음 ==========

    @Nested
    @DisplayName("write - 관리자 계좌 없음")
    class WriteNoAdminAccount {

        @Test
        @DisplayName("관리자 계좌가 없으면 IllegalStateException 발생")
        void throwsWhenAdminAccountMissing() {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(10000);

            when(settlementMapper.getAdminBalance(1L)).thenReturn(null);

            assertThatThrownBy(() -> writer.write(Collections.singletonList(s1)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("관리자 계좌");

            verify(settlementMapper, never()).updateToCompleted(anyLong());
        }
    }
}
