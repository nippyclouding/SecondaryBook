package project.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.member.MemberBankAccountMapper;
import project.trade.TradeMapper;
import project.trade.TradeVO;
import project.util.AesEncryptionUtil;
import project.util.exception.ForbiddenException;
import project.util.exception.settlement.SettlementException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementMapper settlementMapper;
    private final TradeMapper tradeMapper;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final MemberBankAccountMapper memberBankAccountMapper;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01"); // 1%
    private static final long ADMIN_ACCOUNT_SEQ = 1L;

    /**
     * 판매자 정산 신청
     * 검증: 판매자 본인 + 안전결제 완료 + 구매확정 + settlement_st = READY
     */
    @Transactional
    public boolean requestSettlement(long trade_seq, long member_seq) {
        // 1. 거래 조회 + 검증 (FOR UPDATE로 row 락 획득 → 동시 정산 신청 차단)
        TradeVO trade = tradeMapper.findBySeqForUpdate(trade_seq);
        if (trade == null) {
            throw new SettlementException("거래를 찾을 수 없습니다.");
        }
        if (trade.getMember_seller_seq() != member_seq) {
            throw new ForbiddenException("판매자만 정산 신청할 수 있습니다.");
        }
        if (trade.getSafe_payment_st() != project.trade.ENUM.SafePaymentStatus.COMPLETED) {
            throw new SettlementException("안전결제가 완료된 거래만 정산 신청할 수 있습니다.");
        }
        if (trade.getConfirm_purchase() == null || !trade.getConfirm_purchase()) {
            throw new SettlementException("구매확정이 완료된 거래만 정산 신청할 수 있습니다.");
        }
        if (trade.getSettlement_st() != SettlementStatus.READY) {
            throw new SettlementException("이미 정산 신청되었거나 정산이 완료된 거래입니다.");
        }

        // 2. 정산 계좌 등록 여부 확인
        if (memberBankAccountMapper.findByMemberSeq(member_seq) == null) {
            throw new SettlementException("정산 계좌를 먼저 등록해주세요. (마이페이지 > 정산 계좌 관리)");
        }

        // 3. 금액 계산
        int totalAmount = trade.getSale_price() + trade.getDelivery_cost();
        int commission = BigDecimal.valueOf(totalAmount)
                .multiply(COMMISSION_RATE)
                .setScale(0, RoundingMode.FLOOR)
                .intValue();
        int settlementAmount = totalAmount - commission;

        // 4. settlement 테이블 INSERT
        SettlementVO settlement = new SettlementVO();
        settlement.setTrade_seq(trade_seq);
        settlement.setMember_seller_seq(member_seq);
        settlement.setSale_price(trade.getSale_price());
        settlement.setDelivery_cost(trade.getDelivery_cost());
        settlement.setCommission_rate(COMMISSION_RATE);
        settlement.setCommission(commission);
        settlement.setSettlement_amount(settlementAmount);

        int result = settlementMapper.insertSettlement(settlement);

        // 5. sb_trade_info의 settlement_st를 REQUESTED로 변경
        if (result > 0) {
            settlementMapper.updateTradeSettlementSt(trade_seq, SettlementStatus.REQUESTED);
            log.info("정산 신청 완료: trade_seq={}, 정산금액={}원", trade_seq, settlementAmount);
        }

        return result > 0;
    }

    // 정산 내역 조회 (trade_seq 기준)
    public SettlementVO findByTradeSeq(long trade_seq) {
        return settlementMapper.findByTradeSeq(trade_seq);
    }

    // 상태별 목록 조회 (관리자)
    public List<SettlementVO> findByStatus(SettlementStatus status) {
        List<SettlementVO> list = settlementMapper.findByStatus(status);
        if (status == SettlementStatus.REQUESTED) {
            for (SettlementVO s : list) {
                if (s.getBank_account_no() != null) {
                    s.setBank_account_no(aesEncryptionUtil.decrypt(s.getBank_account_no()));
                }
            }
        }
        return list;
    }

    // 정산 건수 (관리자 대시보드)
    public int countByStatus(SettlementStatus status) {
        return settlementMapper.countByStatus(status);
    }

    // 관리자 잔액 조회 (대시보드 표시용 - 읽기 전용, FOR UPDATE 없음)
    public Long getAdminBalance() {
        return settlementMapper.getAdminBalanceReadOnly(ADMIN_ACCOUNT_SEQ);
    }

    /**
     * 관리자 잔액 충전 (Toss 결제 승인 완료 후 호출)
     * admin_account.balance 증가 + 감사 로그 INSERT
     */
    @Transactional
    public void chargeAdminBalance(String paymentKey, String orderId, int amount) {
        settlementMapper.increaseAdminBalance(ADMIN_ACCOUNT_SEQ, amount);
        Long balanceAfter = settlementMapper.getAdminBalanceReadOnly(ADMIN_ACCOUNT_SEQ);
        settlementMapper.insertAccountLog(
                ADMIN_ACCOUNT_SEQ,
                0L,
                amount,
                balanceAfter != null ? balanceAfter : 0L,
                "관리자 잔액 충전 | orderId=" + orderId
        );
        log.info("관리자 잔액 충전 완료: {}원, 잔액={}원, orderId={}", amount, balanceAfter, orderId);
    }

    /**
     * 정산 완료 처리 (관리자가 수동 이체 후 클릭)
     * REQUESTED → COMPLETED + 잔액 차감 + 감사 로그
     */
    @Transactional
    public boolean confirmTransfer(long settlement_seq) {
        SettlementVO settlement = settlementMapper.findBySettlementSeq(settlement_seq);
        if (settlement == null) return false;

        int updated = settlementMapper.confirmTransfer(settlement_seq);
        if (updated > 0) {
            settlementMapper.updateTradeSettlementSt(settlement.getTrade_seq(), SettlementStatus.COMPLETED);
            settlementMapper.updateAdminBalance(ADMIN_ACCOUNT_SEQ, settlement.getSettlement_amount());

            Long balanceAfter = settlementMapper.getAdminBalanceReadOnly(ADMIN_ACCOUNT_SEQ);
            settlementMapper.insertAccountLog(
                    ADMIN_ACCOUNT_SEQ,
                    settlement_seq,
                    -settlement.getSettlement_amount(),
                    balanceAfter != null ? balanceAfter : 0L,
                    "거래#" + settlement.getTrade_seq() + " 정산 완료 처리"
            );
            log.info("정산 완료 처리: settlement_seq={}, trade_seq={}, 금액={}원",
                    settlement_seq, settlement.getTrade_seq(), settlement.getSettlement_amount());
        }
        return updated > 0;
    }
}
