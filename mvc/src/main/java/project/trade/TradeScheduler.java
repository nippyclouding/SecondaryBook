package project.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.payment.SafePaymentService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeScheduler {

    private final TradeService tradeService;
    private final SafePaymentService safePaymentService;

    // 1분마다: 결제 대기 만료 및 승인 결과 불명확 건을 정리한다.
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSafePayments() {
        safePaymentService.cleanupExpiredSafePayments();
        safePaymentService.reconcileUnknownConfirmations();
    }

    // 매일 자정: 15일 경과 구매 자동 확정
    @Scheduled(cron = "0 0 0 * * *")
    public void autoConfirmExpiredPurchases() {
        int count = tradeService.autoConfirmExpiredPurchases();
        if (count > 0) {
            log.info("15일 경과 구매 확정 처리: {}건", count);
        }
    }
}
