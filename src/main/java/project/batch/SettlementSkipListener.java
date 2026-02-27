package project.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;
import project.settlement.SettlementService;
import project.settlement.SettlementVO;

/**
 * 정산 배치 Skip 리스너.
 *
 * <p>Spring Batch가 {@link InsufficientBalanceException}으로 항목을 skip할 때 호출된다.
 * 청크 트랜잭션 롤백 후 별도 트랜잭션으로 settlement 상태를 INSUFFICIENT_BALANCE로 갱신한다.</p>
 *
 * <p>처리 흐름:</p>
 * <pre>
 * Writer → InsufficientBalanceException → 청크 롤백
 *   → onSkipInWrite() → markAsInsufficient()
 *     → settlement.settlement_st = INSUFFICIENT_BALANCE
 *     → sb_trade_info.settlement_st = INSUFFICIENT_BALANCE
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSkipListener implements SkipListener<SettlementVO, SettlementVO> {

    private final SettlementService settlementService;

    @Override
    public void onSkipInWrite(SettlementVO item, Throwable t) {
        if (t instanceof InsufficientBalanceException) {
            log.warn("정산 skip: settlement_seq={}, trade_seq={}, 사유={}",
                    item.getSettlement_seq(), item.getTrade_seq(), t.getMessage());
            settlementService.markAsInsufficient(item.getSettlement_seq(), item.getTrade_seq(), item.getMember_seller_seq());
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
        // 읽기 오류는 별도 처리 없음
    }

    @Override
    public void onSkipInProcess(SettlementVO item, Throwable t) {
        // 처리 오류는 별도 처리 없음
    }
}
