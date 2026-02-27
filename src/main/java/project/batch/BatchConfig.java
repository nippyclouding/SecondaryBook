package project.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.settlement.SettlementMapper;
import project.settlement.SettlementVO;
import project.util.AesEncryptionUtil;

/**
 * Spring Batch 설정.
 *
 * <p>chunk = 1: 정산 1건 = 독립 트랜잭션. FOR UPDATE 락이 1건 처리 후 즉시 해제된다.</p>
 * <p>skip(InsufficientBalanceException): 잔액 부족 건은 롤백 후 skip, 나머지 건 계속 처리.</p>
 *
 * <p>DefaultBatchConfigurer가 MvcConfig의 DataSource(@Primary)와
 * PlatformTransactionManager를 자동으로 사용한다.</p>
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private AesEncryptionUtil aesEncryptionUtil;

    @Autowired
    private SettlementSkipListener settlementSkipListener;

    @Bean
    public Job settlementJob() {
        return jobs.get("settlementJob")
                .start(processSettlementStep())
                .build();
    }

    @Bean
    public Step processSettlementStep() {
        return steps.get("processSettlementStep")
                .<SettlementVO, SettlementVO>chunk(1)
                .reader(settlementItemReader())
                .processor(settlementItemProcessor())
                .writer(settlementItemWriter())
                .faultTolerant()
                .skip(InsufficientBalanceException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(settlementSkipListener)
                .build();
    }

    @Bean
    @StepScope
    public SettlementItemReader settlementItemReader() {
        return new SettlementItemReader(settlementMapper);
    }

    @Bean
    @StepScope
    public SettlementItemProcessor settlementItemProcessor() {
        return new SettlementItemProcessor(aesEncryptionUtil);
    }

    @Bean
    @StepScope
    public SettlementItemWriter settlementItemWriter() {
        return new SettlementItemWriter(settlementMapper);
    }
}
