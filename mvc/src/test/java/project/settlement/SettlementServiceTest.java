package project.settlement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.member.MailService;
import project.member.MemberBankAccountMapper;
import project.member.MemberBankAccountVO;
import project.member.MemberMapper;
import project.member.MemberVO;
import project.settlement.SettlementStatus;
import project.trade.ENUM.SafePaymentStatus;
import project.trade.TradeMapper;
import project.trade.TradeVO;
import project.util.AesEncryptionUtil;
import project.util.exception.ForbiddenException;
import project.util.exception.settlement.SettlementException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @InjectMocks
    private SettlementService settlementService;

    @Mock
    private SettlementMapper settlementMapper;

    @Mock
    private TradeMapper tradeMapper;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

    @Mock
    private MemberBankAccountMapper memberBankAccountMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private MailService mailService;

    // ========== requestSettlement ==========

    @Nested
    @DisplayName("requestSettlement - 정산 신청")
    class RequestSettlement {

        private TradeVO createValidTrade(long sellerSeq) {
            TradeVO trade = new TradeVO();
            trade.setTrade_seq(1L);
            trade.setMember_seller_seq(sellerSeq);
            trade.setSafe_payment_st(SafePaymentStatus.COMPLETED);
            trade.setConfirm_purchase(true);
            trade.setSettlement_st(SettlementStatus.READY);
            trade.setSale_price(10000);
            trade.setDelivery_cost(3000);
            return trade;
        }

        @Test
        @DisplayName("정상 정산 신청 - 수수료 1% 차감된 금액으로 INSERT")
        void successfulRequest() {
            TradeVO trade = createValidTrade(100L);
            when(tradeMapper.findBySeqForUpdate(1L)).thenReturn(trade);
            when(memberBankAccountMapper.findByMemberSeq(100L)).thenReturn(new MemberBankAccountVO());
            when(settlementMapper.insertSettlement(any())).thenReturn(1);

            boolean result = settlementService.requestSettlement(1L, 100L);

            assertThat(result).isTrue();

            ArgumentCaptor<SettlementVO> captor = ArgumentCaptor.forClass(SettlementVO.class);
            verify(settlementMapper).insertSettlement(captor.capture());

            SettlementVO captured = captor.getValue();
            assertThat(captured.getSale_price()).isEqualTo(10000);
            assertThat(captured.getDelivery_cost()).isEqualTo(3000);
            assertThat(captured.getCommission()).isEqualTo(130);     // (10000+3000) * 0.01
            assertThat(captured.getSettlement_amount()).isEqualTo(12870); // 13000 - 130

            verify(settlementMapper).updateTradeSettlementSt(1L, SettlementStatus.REQUESTED);
        }

        @Test
        @DisplayName("판매자가 아닌 사용자의 정산 신청 - ForbiddenException")
        void rejectNonSeller() {
            TradeVO trade = createValidTrade(100L);
            when(tradeMapper.findBySeqForUpdate(1L)).thenReturn(trade);

            assertThatThrownBy(() -> settlementService.requestSettlement(1L, 999L))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("안전결제 미완료 거래 - SettlementException")
        void rejectNotCompletedPayment() {
            TradeVO trade = createValidTrade(100L);
            trade.setSafe_payment_st(SafePaymentStatus.PENDING);
            when(tradeMapper.findBySeqForUpdate(1L)).thenReturn(trade);

            assertThatThrownBy(() -> settlementService.requestSettlement(1L, 100L))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining("안전결제");
        }

        @Test
        @DisplayName("구매확정 미완료 - SettlementException")
        void rejectNotConfirmed() {
            TradeVO trade = createValidTrade(100L);
            trade.setConfirm_purchase(false);
            when(tradeMapper.findBySeqForUpdate(1L)).thenReturn(trade);

            assertThatThrownBy(() -> settlementService.requestSettlement(1L, 100L))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining("구매확정");
        }

        @Test
        @DisplayName("이미 정산 신청된 거래 (REQUESTED) - SettlementException")
        void rejectAlreadyRequested() {
            TradeVO trade = createValidTrade(100L);
            trade.setSettlement_st(SettlementStatus.REQUESTED);
            when(tradeMapper.findBySeqForUpdate(1L)).thenReturn(trade);

            assertThatThrownBy(() -> settlementService.requestSettlement(1L, 100L))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining("이미 정산");
        }

        @Test
        @DisplayName("존재하지 않는 거래 - SettlementException")
        void rejectNotFound() {
            when(tradeMapper.findBySeqForUpdate(1L)).thenReturn(null);

            assertThatThrownBy(() -> settlementService.requestSettlement(1L, 100L))
                    .isInstanceOf(SettlementException.class);
        }
    }

    // ========== findTransferPending / sumTransferPending ==========

    @Nested
    @DisplayName("findTransferPending - 이체 대기 목록 조회")
    class FindTransferPending {

        @Test
        @DisplayName("이체 대기 목록 조회 - mapper 위임 후 계좌번호 복호화")
        void delegatesToMapper() {
            SettlementVO s1 = new SettlementVO();
            s1.setSettlement_seq(1L);
            s1.setSettlement_amount(12870);
            s1.setBank_code("004");
            s1.setBank_account_no("ENCRYPTED_ACCOUNT");  // DB에 암호화된 값
            s1.setAccount_holder_nm("홍길동");

            when(settlementMapper.findTransferPending()).thenReturn(Collections.singletonList(s1));
            when(aesEncryptionUtil.decrypt("ENCRYPTED_ACCOUNT")).thenReturn("123456789012");

            List<SettlementVO> result = settlementService.findTransferPending();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBank_account_no()).isEqualTo("123456789012"); // 복호화됨
            assertThat(result.get(0).getAccount_holder_nm()).isEqualTo("홍길동");
            verify(settlementMapper).findTransferPending();
        }

        @Test
        @DisplayName("이체 대기 없으면 빈 리스트")
        void emptyWhenNoPending() {
            when(settlementMapper.findTransferPending()).thenReturn(Collections.emptyList());

            List<SettlementVO> result = settlementService.findTransferPending();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("이체 미확인 총액 - mapper 위임")
        void sumTransferPendingDelegates() {
            when(settlementMapper.sumTransferPending()).thenReturn(55000L);

            long total = settlementService.sumTransferPending();

            assertThat(total).isEqualTo(55000L);
            verify(settlementMapper).sumTransferPending();
        }
    }

    // ========== confirmTransfer ==========

    @Nested
    @DisplayName("confirmTransfer - 이체 완료 확인")
    class ConfirmTransfer {

        @Test
        @DisplayName("정상 이체 완료 확인 - true 반환")
        void successConfirm() {
            when(settlementMapper.confirmTransfer(1L)).thenReturn(1);

            boolean result = settlementService.confirmTransfer(1L);

            assertThat(result).isTrue();
            verify(settlementMapper).confirmTransfer(1L);
        }

        @Test
        @DisplayName("이미 확인됐거나 없는 건 - false 반환")
        void alreadyConfirmedOrNotFound() {
            when(settlementMapper.confirmTransfer(99L)).thenReturn(0);

            boolean result = settlementService.confirmTransfer(99L);

            assertThat(result).isFalse();
        }
    }

    // ========== markAsInsufficient ==========

    @Nested
    @DisplayName("markAsInsufficient - 잔액 부족 정산 처리")
    class MarkAsInsufficient {

        @Test
        @DisplayName("settlement + trade 상태 모두 INSUFFICIENT_BALANCE로 갱신")
        void updatesSettlementAndTradeStatus() {
            // member_seller_seq=0 → memberMapper 반환 null → 이메일 발송 건너뜀
            settlementService.markAsInsufficient(1L, 10L, 0L);

            verify(settlementMapper).updateToInsufficient(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("다른 settlement_seq/trade_seq와 혼동 없음")
        void correctSeqPassedToMapper() {
            settlementService.markAsInsufficient(99L, 200L, 0L);

            verify(settlementMapper).updateToInsufficient(99L);
            verify(settlementMapper).updateTradeSettlementSt(200L, SettlementStatus.INSUFFICIENT_BALANCE);
            verify(settlementMapper, never()).updateToInsufficient(1L);
        }

        @Test
        @DisplayName("판매자 이메일 정상 발송 - DB 상태 변경 후 이메일 1회 호출")
        void sendsEmailToSeller() {
            MemberVO seller = new MemberVO();
            seller.setMember_email("seller@test.com");
            seller.setMember_nicknm("판매자A");
            when(memberMapper.findByMemberSeq(42L)).thenReturn(seller);

            SettlementVO settlement = new SettlementVO();
            settlement.setSettlement_amount(12870);
            when(settlementMapper.findBySettlementSeq(1L)).thenReturn(settlement);

            settlementService.markAsInsufficient(1L, 10L, 42L);

            verify(settlementMapper).updateToInsufficient(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.INSUFFICIENT_BALANCE);
            verify(mailService).sendInsufficientBalanceEmail("seller@test.com", "판매자A", 12870, 10L);
        }

        @Test
        @DisplayName("판매자 조회 실패(null) - 이메일 미발송, 예외 없이 정상 완료")
        void skipEmailWhenSellerNotFound() {
            when(memberMapper.findByMemberSeq(99L)).thenReturn(null);

            settlementService.markAsInsufficient(1L, 10L, 99L);

            verify(settlementMapper).updateToInsufficient(1L);
            verify(settlementMapper).updateTradeSettlementSt(10L, SettlementStatus.INSUFFICIENT_BALANCE);
            verify(mailService, never()).sendInsufficientBalanceEmail(any(), any(), anyInt(), anyLong());
        }
    }
}
