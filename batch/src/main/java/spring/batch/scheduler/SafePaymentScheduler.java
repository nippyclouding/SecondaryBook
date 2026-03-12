package spring.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spring.batch.trade.TradeMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SafePaymentScheduler {

    private final TradeMapper tradeMapper;

    // 1분마다 실행
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSafePayments() {
        int count = tradeMapper.resetExpiredSafePayments();
        if (count > 0) {
            log.info("만료된 안전결제 {}건 초기화 완료", count);
        }
    }

    // 15일 지난 구매 자동 확정 (1일 1회 실행)
    @Scheduled(cron = "0 0 0 * * *")
    public void autoConfirmExpiredPurchases() {
        int count = tradeMapper.autoConfirmExpiredPurchases();
        if (count > 0) {
            log.info("15일 경과 구매 확정 처리: {}건", count);
        }
    }
}
