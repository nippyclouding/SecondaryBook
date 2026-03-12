package spring.batch.job.settlement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import spring.batch.settlement.SettlementMapper;
import spring.batch.settlement.SettlementVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SettlementItemWriter 동시성 테스트.
 */
@ExtendWith(MockitoExtension.class)
class SettlementConcurrencyTest {

    @Mock
    private SettlementMapper settlementMapper;

    private SettlementItemWriter writer;

    @BeforeEach
    void setUp() {
        writer = new SettlementItemWriter(settlementMapper);
    }

    @Nested
    @DisplayName("순차 처리 - chunk=1 기본 동작")
    class SequentialProcessing {

        @Test
        @DisplayName("각 chunk는 FOR UPDATE로 최신 잔액을 재조회한다")
        void eachChunkReadsCurrentBalance() throws Exception {
            when(settlementMapper.getAdminBalance(1L))
                    .thenReturn(10000L)
                    .thenReturn(3000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            SettlementVO s1 = createSettlement(1L, 10L, 7000);
            writer.write(new Chunk<>(s1));
            verify(settlementMapper).updateAdminBalance(1L, 7000);

            SettlementVO s2 = createSettlement(2L, 20L, 5000);
            assertThatThrownBy(() -> writer.write(new Chunk<>(s2)))
                    .isInstanceOf(InsufficientBalanceException.class);

            verify(settlementMapper, times(1)).updateAdminBalance(anyLong(), anyInt());
        }

        @Test
        @DisplayName("updateToCompleted 반환 0 → 동시 배치 감지 → IllegalStateException")
        void detectsAlreadyProcessedSettlement() {
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(0);

            assertThatThrownBy(() -> writer.write(new Chunk<>(createSettlement(1L, 10L, 5000))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("동시 배치 실행 감지");
        }

        @Test
        @DisplayName("잔액 경계값: 잔액 == 정산금액 → 정상 처리")
        void exactBalanceSucceeds() throws Exception {
            when(settlementMapper.getAdminBalance(1L)).thenReturn(5000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(new Chunk<>(createSettlement(1L, 10L, 5000)));

            verify(settlementMapper).insertAccountLog(eq(1L), eq(1L), eq(-5000), eq(0L), anyString());
        }

        @Test
        @DisplayName("잔액 경계값: 잔액 == 정산금액 - 1 → 잔액 부족")
        void oneShortFails() {
            when(settlementMapper.getAdminBalance(1L)).thenReturn(4999L);

            assertThatThrownBy(() -> writer.write(new Chunk<>(createSettlement(1L, 10L, 5000))))
                    .isInstanceOf(InsufficientBalanceException.class);
        }
    }

    @Nested
    @DisplayName("멀티스레드 - FOR UPDATE 시뮬레이션")
    class MultiThreaded {

        @Test
        @DisplayName("동시 실행 시 잔액 이중 차감 없음")
        void noDoubleDeductionUnderConcurrency() throws InterruptedException {
            AtomicLong dbBalance = new AtomicLong(6000L);
            Object rowLock = new Object();

            lenient().when(settlementMapper.getAdminBalance(1L)).thenAnswer(inv -> dbBalance.get());
            lenient().doAnswer(inv -> {
                dbBalance.addAndGet(-(int) inv.getArgument(1));
                return 1;
            }).when(settlementMapper).updateAdminBalance(eq(1L), anyInt());
            lenient().when(settlementMapper.updateToCompleted(anyLong())).thenReturn(1);

            int threadCount = 2;
            CountDownLatch ready = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);
            List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 1; i <= threadCount; i++) {
                final long seq = i;
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        synchronized (rowLock) {
                            writer.write(new Chunk<>(createSettlement(seq, seq * 10, 5000)));
                        }
                        successCount.incrementAndGet();
                    } catch (InsufficientBalanceException | IllegalStateException e) {
                        exceptions.add(e);
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                });
            }

            ready.await();
            start.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(successCount.get() + exceptions.size()).isEqualTo(threadCount);
            assertThat(dbBalance.get()).isGreaterThanOrEqualTo(0L);
        }
    }

    private SettlementVO createSettlement(long seq, long tradeSeq, int amount) {
        SettlementVO s = new SettlementVO();
        s.setSettlement_seq(seq);
        s.setTrade_seq(tradeSeq);
        s.setSettlement_amount(amount);
        return s;
    }
}
