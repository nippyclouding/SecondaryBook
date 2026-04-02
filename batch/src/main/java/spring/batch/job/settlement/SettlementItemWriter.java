package spring.batch.job.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import spring.batch.settlement.SettlementMapper;
import spring.batch.settlement.SettlementStatus;
import spring.batch.settlement.SettlementVO;

/**
 * 정산 ItemWriter - chunk = 1이므로 items에는 항상 1건만 들어온다.
 *
 * 처리 순서 (모두 동일 트랜잭션 안에서 실행):
 * 1. SELECT balance FROM admin_account FOR UPDATE → 최신 잔액 획득 + 행 락
 * 2. 잔액 부족 → InsufficientBalanceException → Spring Batch skip → 다음 건 진행
 * 3. updateToCompleted (WHERE settlement_st IN ('REQUESTED','INSUFFICIENT_BALANCE')) → 0행이면 이미 처리된 건 → IllegalStateException
 * 4. 잔액 충분 + 상태 갱신 성공 → insertAccountLog + updateTradeSettlementSt + updateAdminBalance
 * ※ INSUFFICIENT_BALANCE(잔액 부족 재시도) 건도 동일 흐름으로 처리됨
 */
@Slf4j
@RequiredArgsConstructor
public class SettlementItemWriter implements ItemWriter<SettlementVO> {

    private static final long ADMIN_ACCOUNT_SEQ = 1L;

    private final SettlementMapper settlementMapper;

    @Override
    public void write(Chunk<? extends SettlementVO> chunk) throws Exception {
        for (SettlementVO settlement : chunk) {

            Long balance = settlementMapper.getAdminBalance(ADMIN_ACCOUNT_SEQ);
            if (balance == null) {
                throw new IllegalStateException(
                        "관리자 계좌가 존재하지 않습니다. account_seq=" + ADMIN_ACCOUNT_SEQ);
            }

            int settlementAmount = settlement.getSettlement_amount();

            if (balance < settlementAmount) {
                throw new InsufficientBalanceException(
                        "잔액 부족: 잔액=" + balance + "원, 필요=" + settlementAmount
                                + "원, settlement_seq=" + settlement.getSettlement_seq());
            }

            int updated = settlementMapper.updateToCompleted(settlement.getSettlement_seq());
            if (updated == 0) {
                throw new IllegalStateException(
                        "이미 처리된 정산 건 (동시 배치 실행 감지): settlement_seq="
                                + settlement.getSettlement_seq());
            }

            long balanceAfter = balance - settlementAmount;

            String bankInfo = (settlement.getBank_account_no() != null)
                    ? settlement.getAccount_holder_nm() + " / "
                      + settlement.getBank_code() + " " + maskAccountNumber(settlement.getBank_account_no())
                    : "계좌 미등록";

            settlementMapper.insertAccountLog(
                    ADMIN_ACCOUNT_SEQ,
                    settlement.getSettlement_seq(),
                    -settlementAmount,
                    balanceAfter,
                    "거래#" + settlement.getTrade_seq() + " 정산 | 이체 대상: " + bankInfo
            );

            settlementMapper.updateTradeSettlementSt(settlement.getTrade_seq(), SettlementStatus.COMPLETED);
            settlementMapper.updateAdminBalance(ADMIN_ACCOUNT_SEQ, settlementAmount);

            log.info("정산 완료: settlement_seq={}, trade_seq={}, 금액={}원, 잔액={}원",
                    settlement.getSettlement_seq(), settlement.getTrade_seq(),
                    settlementAmount, balanceAfter);
        }
    }

    private String maskAccountNumber(String accountNo) {
        if (accountNo == null || accountNo.length() <= 4) return "****";
        return "*".repeat(accountNo.length() - 4) + accountNo.substring(accountNo.length() - 4);
    }
}
