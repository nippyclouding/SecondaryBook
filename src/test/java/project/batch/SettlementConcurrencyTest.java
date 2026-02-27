package project.batch;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.settlement.SettlementMapper;
import project.settlement.SettlementVO;

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
 *
 * <p>실제 MySQL의 SELECT ... FOR UPDATE는 DB 레벨에서 직렬화를 보장한다.
 * 이 테스트는 AtomicLong + synchronized 블록으로 DB 잠금 동작을 시뮬레이션하여
 * Writer 로직이 정확히 동작하는지 검증한다.</p>
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

    // ========== 순차 처리 (chunk=1 기본 동작) ==========

    @Nested
    @DisplayName("순차 처리 - chunk=1 기본 동작")
    class SequentialProcessing {

        @Test
        @DisplayName("각 chunk는 FOR UPDATE로 최신 잔액을 재조회한다")
        void eachChunkReadsCurrentBalance() throws Exception {
            // 첫 번째 chunk: 잔액 10000, 7000 차감 후 3000
            // 두 번째 chunk: 잔액 3000 (재조회), 5000 필요 → 부족
            when(settlementMapper.getAdminBalance(1L))
                    .thenReturn(10000L)   // 첫 번째 청크
                    .thenReturn(3000L);   // 두 번째 청크 (DB 차감 반영)
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            SettlementVO s1 = createSettlement(1L, 10L, 7000);
            writer.write(List.of(s1));

            verify(settlementMapper).updateAdminBalance(1L, 7000);

            SettlementVO s2 = createSettlement(2L, 20L, 5000);
            assertThatThrownBy(() -> writer.write(List.of(s2)))
                    .isInstanceOf(InsufficientBalanceException.class);

            // 잔액 차감 없음 (s2 실패)
            verify(settlementMapper, times(1)).updateAdminBalance(anyLong(), anyInt());
        }

        @Test
        @DisplayName("updateToCompleted 반환 0 → 동시 배치 감지 → IllegalStateException")
        void detectsAlreadyProcessedSettlement() {
            // 다른 Job이 이미 COMPLETED로 변경 → updateToCompleted 반환 0
            when(settlementMapper.getAdminBalance(1L)).thenReturn(100000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(0);

            SettlementVO settlement = createSettlement(1L, 10L, 5000);

            assertThatThrownBy(() -> writer.write(List.of(settlement)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("동시 배치 실행 감지");

            // 잔액 차감 없음 (이중 처리 방지)
            verify(settlementMapper, never()).updateAdminBalance(anyLong(), anyInt());
            verify(settlementMapper, never()).insertAccountLog(anyLong(), anyLong(), anyInt(), anyLong(), anyString());
        }

        @Test
        @DisplayName("잔액 경계값: 잔액 == 정산금액 → 정상 처리 (남은 잔액 0)")
        void exactBalanceSucceeds() throws Exception {
            when(settlementMapper.getAdminBalance(1L)).thenReturn(5000L);
            when(settlementMapper.updateToCompleted(1L)).thenReturn(1);

            writer.write(List.of(createSettlement(1L, 10L, 5000)));

            verify(settlementMapper).insertAccountLog(eq(1L), eq(1L), eq(-5000), eq(0L), anyString());
            verify(settlementMapper).updateAdminBalance(1L, 5000);
        }

        @Test
        @DisplayName("잔액 경계값: 잔액 == 정산금액 - 1 → 잔액 부족")
        void oneShortFails() {
            when(settlementMapper.getAdminBalance(1L)).thenReturn(4999L);

            assertThatThrownBy(() -> writer.write(List.of(createSettlement(1L, 10L, 5000))))
                    .isInstanceOf(InsufficientBalanceException.class);
        }
    }

    // ========== 멀티스레드 동시 접근 ==========

    @Nested
    @DisplayName("멀티스레드 - FOR UPDATE 시뮬레이션")
    class MultiThreaded {

        /**
         * 잔액 6000원 상황에서 두 스레드가 5000원씩 동시 처리를 시도한다.
         *
         * MySQL SELECT ... FOR UPDATE는 트랜잭션 전체(read→check→write)를 직렬화한다.
         * 이를 시뮬레이션하기 위해 writer.write() 호출 전체를 synchronized로 감싼다:
         * - 첫 스레드: 잔액 6000 ≥ 5000 → 처리 성공, 잔액 1000
         * - 둘째 스레드: 잔액 1000 < 5000 → InsufficientBalanceException
         *
         * 최종 잔액은 반드시 0 이상이어야 한다 (이중 차감 없음).
         */
        @Test
        @DisplayName("동시 실행 시 잔액 이중 차감 없음 (FOR UPDATE 시뮬레이션)")
        void noDoubleDeductionUnderConcurrency() throws InterruptedException {
            AtomicLong dbBalance = new AtomicLong(6000L);
            // MySQL FOR UPDATE는 트랜잭션 전체를 직렬화 → write() 전체를 synchronized로 시뮬레이션
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
                        // MySQL SELECT ... FOR UPDATE는 트랜잭션 전체를 직렬화:
                        // write() 전체를 synchronized로 감싸 동일 효과를 시뮬레이션
                        synchronized (rowLock) {
                            writer.write(List.of(createSettlement(seq, seq * 10, 5000)));
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

            // 총 처리 건수 = 성공 + 실패
            assertThat(successCount.get() + exceptions.size()).isEqualTo(threadCount);
            // 잔액은 0 이상이어야 함 (음수 = 이중 차감 발생)
            assertThat(dbBalance.get()).isGreaterThanOrEqualTo(0L);
        }
    }

    // ========== 헬퍼 ==========

    private SettlementVO createSettlement(long seq, long tradeSeq, int amount) {
        SettlementVO s = new SettlementVO();
        s.setSettlement_seq(seq);
        s.setTrade_seq(tradeSeq);
        s.setSettlement_amount(amount);
        return s;
    }
}
