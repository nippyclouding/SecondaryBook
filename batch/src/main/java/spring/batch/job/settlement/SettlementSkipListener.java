package spring.batch.job.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;
import spring.batch.settlement.BatchSettlementService;
import spring.batch.settlement.SettlementVO;

/**
 * 정산 배치 Skip 리스너.
 * InsufficientBalanceException으로 skip될 때 settlement 상태를 INSUFFICIENT_BALANCE로 갱신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSkipListener implements SkipListener<SettlementVO, SettlementVO> {

    private final BatchSettlementService batchSettlementService;

    @Override
    public void onSkipInWrite(SettlementVO item, Throwable t) {
        if (t instanceof InsufficientBalanceException) {
            log.warn("정산 skip: settlement_seq={}, trade_seq={}, 사유={}",
                    item.getSettlement_seq(), item.getTrade_seq(), t.getMessage());
            batchSettlementService.markAsInsufficient(
                    item.getSettlement_seq(), item.getTrade_seq(), item.getMember_seller_seq());
        }
    }

    @Override
    public void onSkipInRead(Throwable t) {
    }

    @Override
    public void onSkipInProcess(SettlementVO item, Throwable t) {
    }
}
