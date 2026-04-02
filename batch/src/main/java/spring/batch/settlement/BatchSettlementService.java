package spring.batch.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.batch.member.BatchMailService;
import spring.batch.member.MemberMapper;
import spring.batch.member.MemberVO;

/**
 * 배치 전용 정산 서비스.
 * 배치에서 필요한 메서드만 포함 (잔액 부족 처리).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchSettlementService {

    private final SettlementMapper settlementMapper;
    private final MemberMapper memberMapper;
    private final BatchMailService batchMailService;

    /**
     * 잔액 부족 처리 (Spring Batch skip 리스너에서 호출)
     */
    @Transactional
    public void markAsInsufficient(long settlement_seq, long trade_seq, long member_seller_seq) {
        settlementMapper.updateToInsufficient(settlement_seq);
        settlementMapper.updateTradeSettlementSt(trade_seq, SettlementStatus.INSUFFICIENT_BALANCE);
        log.warn("잔액 부족으로 정산 처리 실패: settlement_seq={}", settlement_seq);

        MemberVO seller = memberMapper.findByMemberSeq(member_seller_seq);
        if (seller != null && seller.getMember_email() != null) {
            SettlementVO settlement = settlementMapper.findBySettlementSeq(settlement_seq);
            int amount = settlement != null ? settlement.getSettlement_amount() : 0;
            try {
                batchMailService.sendInsufficientBalanceEmail(
                        seller.getMember_email(),
                        seller.getMember_nicknm(),
                        amount,
                        trade_seq
                );
            } catch (Exception e) {
                log.warn("잔액 부족 이메일 발송 실패 (정산 상태 변경은 유지됨): settlement_seq={}, email={}",
                        settlement_seq, seller.getMember_email(), e);
            }
        }
    }
}
