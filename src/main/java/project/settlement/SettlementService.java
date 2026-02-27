package project.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.member.MailService;
import project.member.MemberBankAccountMapper;
import project.member.MemberMapper;
import project.member.MemberVO;
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
    private final MemberMapper memberMapper;
    private final MailService mailService;

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

        // 2. 정산 계좌 등록 여부 확인 (계좌 미등록 시 배치에서 '계좌 미등록'으로 처리되므로 신청 단계에서 차단)
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
        return settlementMapper.findByStatus(status);
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
     * 이체 대기 목록 조회 (관리자 페이지)
     * settlement_st = COMPLETED + transfer_confirmed_yn = 0 인 건
     * 관리자가 은행 앱에서 수동 이체해야 할 대상 목록 (계좌번호 복호화 후 반환)
     */
    public List<SettlementVO> findTransferPending() {
        List<SettlementVO> list = settlementMapper.findTransferPending();
        for (SettlementVO s : list) {
            if (s.getBank_account_no() != null) {
                s.setBank_account_no(aesEncryptionUtil.decrypt(s.getBank_account_no()));
            }
        }
        return list;
    }

    /**
     * 이체 대기 총액 (관리자 페이지 합계 표시)
     */
    public long sumTransferPending() {
        return settlementMapper.sumTransferPending();
    }

    /**
     * 잔액 부족 처리 (Spring Batch skip 리스너에서 호출)
     * settlement_st = INSUFFICIENT_BALANCE, trade.settlement_st = INSUFFICIENT_BALANCE
     */
    @Transactional
    public void markAsInsufficient(long settlement_seq, long trade_seq, long member_seller_seq) {
        // ① 핵심 로직: 상태 변경
        settlementMapper.updateToInsufficient(settlement_seq);
        settlementMapper.updateTradeSettlementSt(trade_seq, SettlementStatus.INSUFFICIENT_BALANCE);
        log.warn("잔액 부족으로 정산 처리 실패: settlement_seq={}", settlement_seq);

        // ② 판매자 이메일 조회 후 알림 발송 (실패해도 트랜잭션에 영향 없음)
        MemberVO seller = memberMapper.findByMemberSeq(member_seller_seq);
        if (seller != null && seller.getMember_email() != null) {
            SettlementVO settlement = settlementMapper.findBySettlementSeq(settlement_seq);
            int amount = settlement != null ? settlement.getSettlement_amount() : 0;
            mailService.sendInsufficientBalanceEmail(
                    seller.getMember_email(),
                    seller.getMember_nicknm(),
                    amount,
                    trade_seq
            );
        }
    }

    /**
     * 잔액 부족 건 재처리 (관리자가 잔액 충전 후 호출)
     * INSUFFICIENT_BALANCE → REQUESTED 로 되돌려 다음 배치에서 재시도
     * trade.settlement_st도 동일하게 REQUESTED로 복원
     */
    @Transactional
    public boolean resetToRequested(long settlement_seq) {
        SettlementVO settlement = settlementMapper.findBySettlementSeq(settlement_seq);
        if (settlement == null) return false;
        int updated = settlementMapper.resetToRequested(settlement_seq);
        if (updated > 0) {
            settlementMapper.updateTradeSettlementSt(settlement.getTrade_seq(), SettlementStatus.REQUESTED);
            log.info("정산 재처리 설정: settlement_seq={}, trade_seq={}", settlement_seq, settlement.getTrade_seq());
        }
        return updated > 0;
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
     * 이체 완료 확인 (관리자가 수동 이체 후 클릭)
     * transfer_confirmed_yn = 1 로 업데이트 + admin_account_log에 확인 이력 기록
     */
    @Transactional
    public boolean confirmTransfer(long settlement_seq) {
        int updated = settlementMapper.confirmTransfer(settlement_seq);
        if (updated > 0) {
            // 감사 로그: 이체 확인 이벤트를 admin_account_log에 기록
            SettlementVO settlement = settlementMapper.findBySettlementSeq(settlement_seq);
            Long currentBalance = settlementMapper.getAdminBalanceReadOnly(ADMIN_ACCOUNT_SEQ);
            if (settlement != null && currentBalance != null) {
                settlementMapper.insertAccountLog(
                        ADMIN_ACCOUNT_SEQ,
                        settlement_seq,
                        0,           // 잔액 변동 없음 (이미 배치에서 차감 완료)
                        currentBalance,
                        "거래#" + settlement.getTrade_seq() + " 이체 완료 확인"
                );
            }
            log.info("이체 완료 확인: settlement_seq={}, trade_seq={}",
                    settlement_seq, settlement != null ? settlement.getTrade_seq() : "unknown");
        }
        return updated > 0;
    }
}
