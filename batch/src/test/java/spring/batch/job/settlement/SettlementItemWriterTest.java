package spring.batch.job.settlement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import spring.batch.settlement.SettlementMapper;
import spring.batch.settlement.SettlementStatus;
import spring.batch.settlement.SettlementVO;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementItemWriterTest {

    @InjectMocks
    private SettlementItemWriter writer;

    @Mock
    private SettlementMapper settlementMapper;

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

            writer.write(new Chunk<>(s1));

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

            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(new Chunk<>(s1));

            verify(settlementMapper).insertAccountLog(
                    eq(1L), eq(1L), eq(-12870), eq(87130L),
                    argThat(desc -> desc.contains("계좌 미등록"))
            );
        }
    }

    @Nested
    @DisplayName("write - 잔액 부족")
    class WriteInsufficientBalance {

        @Test
        @DisplayName("잔액 부족 시 InsufficientBalanceException 발생")
        void throwsInsufficientBalanceException() {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(50000);

            when(settlementMapper.getAdminBalance(1L)).thenReturn(100L);

            assertThatThrownBy(() -> writer.write(new Chunk<>(s1)))
                    .isInstanceOf(InsufficientBalanceException.class);

            verify(settlementMapper, never()).updateAdminBalance(anyLong(), anyInt());
        }

        @Test
        @DisplayName("잔액이 정확히 정산금액과 같으면 정상 처리 (경계값)")
        void exactBalanceIsProcessed() throws Exception {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setTrade_seq(10L);
            s1.setSettlement_amount(5000);

            when(settlementMapper.getAdminBalance(1L)).thenReturn(5000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(new Chunk<>(s1));

            verify(settlementMapper).updateAdminBalance(1L, 5000);
        }
    }

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

            assertThatThrownBy(() -> writer.write(new Chunk<>(s1)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("관리자 계좌");
        }
    }
}
