package project.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    // 매일 새벽 3시에 정산 배치 실행
    @Scheduled(cron = "0 0 3 * * *")
    public void processSettlements() {
        log.info("정산 배치 시작");
        try {
            // run.id를 현재 시각으로 설정하여 매 실행마다 새로운 JobInstance가 생성되도록 함
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(settlementJob, params);

            long writeCount = execution.getStepExecutions().stream()
                    .mapToLong(s -> s.getWriteCount()).sum();
            long skipCount = execution.getStepExecutions().stream()
                    .mapToLong(s -> s.getSkipCount()).sum();

            log.info("정산 배치 완료: status={}, 처리={}건, 스킵={}건",
                    execution.getStatus(), writeCount, skipCount);

        } catch (Exception e) {
            log.error("정산 배치 실행 실패", e);
        }
    }
}
