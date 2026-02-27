package project.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import project.settlement.SettlementMapper;
import project.settlement.SettlementStatus;
import project.settlement.SettlementVO;

import java.util.List;

/**
 * 정산 ItemWriter - chunk = 1이므로 items에는 항상 1건만 들어온다.
 *
 * <p>처리 순서 (모두 동일 트랜잭션 안에서 실행):</p>
 * <ol>
 *   <li>SELECT balance FROM admin_account FOR UPDATE → 최신 잔액 획득 + 행 락</li>
 *   <li>잔액 부족 → {@link InsufficientBalanceException} → Spring Batch skip → 다음 건 진행</li>
 *   <li>updateToCompleted (WHERE settlement_st='REQUESTED') → 0행이면 이미 처리된 건 → {@link IllegalStateException}</li>
 *   <li>잔액 충분 + 상태 갱신 성공 → insertAccountLog + updateTradeSettlementSt + updateAdminBalance</li>
 * </ol>
 *
 * <p>updateToCompleted에 AND settlement_st='REQUESTED' 조건이 있어서, 두 배치 Job이 동시에
 * 실행되더라도 같은 건을 이중 처리할 수 없다 (멱등성 보장).</p>
 */
@Slf4j
@RequiredArgsConstructor
public class SettlementItemWriter implements ItemWriter<SettlementVO> {

    private static final long ADMIN_ACCOUNT_SEQ = 1L;

    private final SettlementMapper settlementMapper;

    @Override
    public void write(List<? extends SettlementVO> items) throws Exception {
        for (SettlementVO settlement : items) {

            // ① FOR UPDATE로 최신 잔액 조회 (admin_account 행 락 획득)
            Long balance = settlementMapper.getAdminBalance(ADMIN_ACCOUNT_SEQ);
            if (balance == null) {
                throw new IllegalStateException(
                        "관리자 계좌가 존재하지 않습니다. account_seq=" + ADMIN_ACCOUNT_SEQ);
            }

            int settlementAmount = settlement.getSettlement_amount();

            // ② 잔액 부족 → InsufficientBalanceException (skip 대상, 롤백 후 다음 건 진행)
            if (balance < settlementAmount) {
                throw new InsufficientBalanceException(
                        "잔액 부족: 잔액=" + balance + "원, 필요=" + settlementAmount
                                + "원, settlement_seq=" + settlement.getSettlement_seq());
            }

            // ③ settlement 상태 REQUESTED → COMPLETED (AND settlement_st='REQUESTED' 조건으로 멱등성 보장)
            //    반환 0 = 이미 다른 배치 Job이 처리 완료 → IllegalStateException으로 Job 실패 처리
            int updated = settlementMapper.updateToCompleted(settlement.getSettlement_seq());
            if (updated == 0) {
                throw new IllegalStateException(
                        "이미 처리된 정산 건 (동시 배치 실행 감지): settlement_seq="
                                + settlement.getSettlement_seq());
            }

            long balanceAfter = balance - settlementAmount;

            // ④ 이체 대상 계좌 정보 포함 로그 기록 (계좌번호는 마스킹하여 저장)
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

            // ⑤ sb_trade_info의 settlement_st → COMPLETED
            settlementMapper.updateTradeSettlementSt(settlement.getTrade_seq(), SettlementStatus.COMPLETED);

            // ⑥ 관리자 잔액 즉시 차감
            settlementMapper.updateAdminBalance(ADMIN_ACCOUNT_SEQ, settlementAmount);

            log.info("정산 완료: settlement_seq={}, trade_seq={}, 금액={}원, 잔액={}원",
                    settlement.getSettlement_seq(), settlement.getTrade_seq(),
                    settlementAmount, balanceAfter);
        }
    }

    /** 계좌번호 마스킹: 뒤 4자리만 표시, 나머지는 * 처리 */
    private String maskAccountNumber(String accountNo) {
        if (accountNo == null || accountNo.length() <= 4) return "****";
        return "*".repeat(accountNo.length() - 4) + accountNo.substring(accountNo.length() - 4);
    }
}
