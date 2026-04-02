package project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 배치 서버 분리 후 남은 스케줄러: LogoutPendingScheduler
 * ShedLock 불필요 (배치 분리됨)
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
