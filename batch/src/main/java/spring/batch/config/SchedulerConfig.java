package spring.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 배치 서버는 단독 EC2 1대로 운영되므로 ShedLock 불필요.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
