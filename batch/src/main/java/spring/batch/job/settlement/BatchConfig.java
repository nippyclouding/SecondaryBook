package spring.batch.job.settlement;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import spring.batch.settlement.SettlementMapper;
import spring.batch.settlement.SettlementVO;
import spring.batch.util.AesEncryptionUtil;

/**
 * Spring Batch 5 정산 Job 설정.
 *
 * chunk = 1: 정산 1건 = 독립 트랜잭션. FOR UPDATE 락이 1건 처리 후 즉시 해제된다.
 * MyBatisCursorItemReader: DB 커서로 1건씩 스트리밍 → 대용량 처리 시 메모리 절약.
 * skip(InsufficientBalanceException): 잔액 부족 건은 롤백 후 skip, 나머지 건 계속 처리.
 */
@Configuration
public class BatchConfig {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private AesEncryptionUtil aesEncryptionUtil;

    @Autowired
    private SettlementSkipListener settlementSkipListener;

    @Bean
    public Job settlementJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             MyBatisCursorItemReader<SettlementVO> settlementItemReader) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(processSettlementStep(jobRepository, transactionManager, settlementItemReader))
                .build();
    }

    @Bean
    public Step processSettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                      MyBatisCursorItemReader<SettlementVO> settlementItemReader) {
        return new StepBuilder("processSettlementStep", jobRepository)
                .<SettlementVO, SettlementVO>chunk(1, transactionManager)
                .reader(settlementItemReader)
                .processor(settlementItemProcessor())
                .writer(settlementItemWriter())
                .faultTolerant()
                .skip(InsufficientBalanceException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(settlementSkipListener)
                .build();
    }

    /**
     * MyBatisCursorItemReader: DB 커서를 열어 1건씩 스트리밍.
     * ListItemReader와 달리 전체를 메모리에 올리지 않아 대용량에 적합.
     * @StepScope: Step 실행 시점에 커서를 열고, Step 종료 시 커서를 닫는다.
     */
    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public MyBatisCursorItemReader<SettlementVO> settlementItemReader(SqlSessionFactory sqlSessionFactory) {
        return new MyBatisCursorItemReaderBuilder<SettlementVO>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("spring.batch.settlement.SettlementMapper.findAllRequested")
                .build();
    }

    @Bean
    public SettlementItemProcessor settlementItemProcessor() {
        return new SettlementItemProcessor(aesEncryptionUtil);
    }

    @Bean
    public SettlementItemWriter settlementItemWriter() {
        return new SettlementItemWriter(settlementMapper);
    }
}
