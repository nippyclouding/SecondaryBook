package project.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeScheduler {

    private final TradeService tradeService;

    // 1분마다: 만료된 안전결제 초기화
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSafePayments() {
        int count = tradeService.resetExpiredSafePayments();
        if (count > 0) {
            log.info("만료된 안전결제 {}건 초기화 완료", count);
        }
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
