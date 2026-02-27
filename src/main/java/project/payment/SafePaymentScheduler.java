package project.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.trade.TradeService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SafePaymentScheduler {

    private final TradeService tradeService;

    // 1분마다 실행
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSafePayments() {
        int count = tradeService.resetExpiredSafePayments();
        if (count > 0) {
            log.info("만료된 안전결제 {}건 초기화 완료", count);
        }
    }

    // 15일 지난 구매 자동 확정 (1일 1회 실행)
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    public void autoConfirmExpiredPurchases() {
        int count = tradeService.autoConfirmExpiredPurchases();
        if (count > 0) {
            log.info("15일 경과 구매 확정 처리: {}건", count);
        }
    }
}