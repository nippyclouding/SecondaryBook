# Spring Batch — 정산 배치

> 정산 처리에 Spring Batch를 적용한 이유와 각 기술이 어떻게 동작하는지 정리한다.

---

## 왜 Spring Batch를 도입했나

초기에는 `SettlementService.processSettlements()`라는 일반 서비스 메서드 안에서 `@Transactional`로 모든 정산 건을 하나의 트랜잭션으로 처리했다.

```
[기존 방식의 문제]

REQUESTED 상태 건 50개를 하나의 트랜잭션으로 묶어 처리
→ 49건 성공 후 50번째 건에서 예외 발생
→ 전체 50건 롤백
→ 성공한 49건도 모두 취소됨
```

정산은 **부분 실패가 전체를 취소해서는 안 되는** 업무다.
잔액 부족으로 한 건이 실패해도 나머지 건은 정상 처리되어야 한다.

Spring Batch는 이 요구사항을 chunk + skip 메커니즘으로 해결한다.

---

## 핵심 개념과 적용

### 1. Chunk 처리 — 건별 독립 트랜잭션

Chunk는 "한 번에 처리하는 단위"다.
읽기(Reader) → 처리(Processor) → 쓰기(Writer) 흐름이 chunk 단위로 트랜잭션에 묶인다.

**이 프로젝트는 chunk=1로 설정했다.**

```
[chunk=1의 의미]

정산 건 1개를 읽고 → 처리하고 → 쓰는 작업이 하나의 트랜잭션
트랜잭션이 커밋되면 다음 건으로 넘어감

건 A: TX-1 시작 → 처리 → COMMIT
건 B: TX-2 시작 → 처리 → COMMIT
건 C: TX-3 시작 → 처리 → ROLLBACK (잔액 부족)
건 D: TX-4 시작 → 처리 → COMMIT

→ C가 실패해도 A, B, D는 이미 커밋되어 있음
```

chunk=1을 선택한 이유는 두 가지다:
- **독립성**: 한 건의 실패가 다른 건에 영향을 주지 않는다
- **FOR UPDATE 잠금 최소화**: 잔액 조회 시 DB 행 잠금이 걸리는데, chunk=1이면 1건 처리 직후 잠금이 해제된다. chunk=10이면 10건 처리 내내 잠금이 유지된다.

---

### 2. Skip 메커니즘 — 잔액 부족 건을 건너뛰고 계속 진행

Fault Tolerant(오류 허용) 모드에서 특정 예외를 skip 대상으로 지정하면, 해당 예외가 발생해도 Job이 중단되지 않고 그 건만 건너뛰고 다음 건으로 넘어간다.

```
[Skip 설정]

.faultTolerant()
.skip(InsufficientBalanceException.class)   ← 이 예외는 skip
.skipLimit(Integer.MAX_VALUE)               ← 잔액 부족 건은 무제한 skip 허용
```

skipLimit은 "몇 건까지 skip을 허용할지"를 지정한다.
잔액 부족은 관리자 계좌 잔액 상황에 따라 수십 건이 한꺼번에 실패할 수 있으므로 무제한으로 설정했다.

```
[Skip vs 전체 실패의 차이]

IllegalStateException (동시 배치 감지) → skip 대상 아님 → Job 전체 실패
InsufficientBalanceException (잔액 부족) → skip 대상     → 해당 건만 skip, 계속 진행
```

예외의 심각도에 따라 skip 여부를 분리했다.
잔액 부족은 정상 업무 케이스지만, 동시 배치 감지는 시스템 이상이므로 Job을 즉시 멈추고 알려야 한다.

---

### 3. SkipListener — 롤백 후 상태 기록

skip이 발생하면 Spring Batch는 해당 청크를 롤백한 뒤 SkipListener를 호출한다.

```
[SkipListener의 타이밍]

청크 트랜잭션: 시작 → Writer 실행 → 예외 → ROLLBACK
                                                  ↓
SkipListener.onSkipInWrite() 호출  ← 롤백 이후, 트랜잭션 바깥에서 실행
  → 새 트랜잭션 시작
  → settlement_st = INSUFFICIENT_BALANCE 기록
  → COMMIT
```

SkipListener는 반드시 **청크 트랜잭션이 끝난 뒤에** 호출된다.
덕분에 청크가 롤백되더라도 SkipListener 안에서 새 트랜잭션을 열어 상태를 기록할 수 있다.

만약 SkipListener를 사용하지 않았다면, 잔액 부족 건은 상태 변경 없이 REQUESTED로 남아버린다.
(다음 배치 실행 때 또 시도됨 → 또 실패 → 무한 반복)

SkipListener 덕분에 잔액 부족 건은 INSUFFICIENT_BALANCE로 마킹되어 "이 건은 처리 시도를 했고 잔액이 부족했다"는 이력이 남는다.

---

### 4. @StepScope — Step 실행 시점에 Bean 생성

`@StepScope`는 Spring Batch에서 제공하는 특수한 스코프로, Step이 실행되는 순간 Bean이 생성된다.

```
[@StepScope가 필요한 이유]

Reader는 REQUESTED 상태인 모든 정산 건을 Step 시작 시 한 번에 로딩한다.
→ ApplicationContext 초기화 시점이 아니라 Step 실행 시점에 DB를 조회해야 한다.
→ @StepScope를 붙이면 Bean 생성이 Step 실행까지 지연된다.
```

정산 배치에서 Reader, Processor, Writer 모두 `@StepScope`로 선언했다.
Job이 여러 번 실행되어도 매 실행마다 새 인스턴스가 만들어지므로, 이전 실행의 상태가 남아있는 문제가 없다.

---

### 5. JobLauncher + JobParameters — 매일 같은 Job을 새 실행으로 인식

Spring Batch는 동일한 JobParameters로 실행된 Job을 "같은 Job 실행"으로 간주하고 재실행을 막는다.
매일 반복 실행하려면 매번 다른 파라미터를 넘겨야 한다.

```
[해결 방법]

JobParameters params = new JobParametersBuilder()
    .addLong("run.id", System.currentTimeMillis())  ← 매 실행마다 다른 타임스탬프
    .toJobParameters();

jobLauncher.run(settlementJob, params);
```

`run.id`에 현재 시각(밀리초)을 넣어 항상 새로운 실행으로 인식되도록 한다.

---

### 6. Reader → Processor → Writer 흐름 상세

```
[정산 배치 전체 흐름]

SettlementItemReader
  ├── Step 시작 시 DB에서 REQUESTED 상태 전체 로딩 (한 번만 조회)
  └── 메모리에 List로 보관 → chunk마다 1건씩 꺼내 줌

SettlementItemProcessor
  ├── Reader가 넘긴 SettlementVO를 받아 변환
  └── bank_account_no가 있으면 AES-256 복호화
      (Writer는 복호화된 평문 계좌번호로 로그를 기록)

SettlementItemWriter
  ├── ① SELECT balance FOR UPDATE → 행 잠금 + 최신 잔액 획득
  ├── ② 잔액 < 정산금액 → InsufficientBalanceException → skip
  ├── ③ updateToCompleted (WHERE settlement_st='REQUESTED')
  │       → 반환값 0이면 이미 처리된 건 → IllegalStateException → Job 실패
  ├── ④ insertAccountLog (차감 금액, 차감 후 잔액, 이체 대상 계좌 정보)
  ├── ⑤ updateTradeSettlementSt → COMPLETED
  └── ⑥ updateAdminBalance (잔액 차감)

SettlementSkipListener (InsufficientBalanceException skip 시)
  ├── settlement.settlement_st → INSUFFICIENT_BALANCE
  └── sb_trade_info.settlement_st → INSUFFICIENT_BALANCE
```

---

### 7. 관리자 정산 업무의 기술적 배경

**관리자가 배치 이후 하는 일과 그 기술적 원리**

| 관리자 행동 | 기술적 동작 |
|------------|------------|
| 어드민 페이지에서 이체 대기 목록 확인 | `settlement_st=COMPLETED AND transfer_confirmed_yn=0` 조회, 계좌번호 AES-256 복호화 후 표시 |
| 계좌번호 보고 은행 이체 | 관리자가 직접 은행 앱으로 수동 이체 (시스템 외부 작업) |
| "이체 완료" 버튼 클릭 | `confirmTransfer()` → `transfer_confirmed_yn = 1` 업데이트, `@Transactional` |
| 관리자 계좌 잔액 확인 | `getAdminBalance()` → sb_account.balance 단순 조회 |

배치가 처리한 내용(`insertAccountLog`)은 관리자 계좌의 거래 이력으로 남아, 언제 얼마가 어떤 정산 건으로 차감됐는지 추적 가능하다.

---

## 적용 기술 요약

| 기술 | 역할 |
|------|------|
| `chunk=1` | 건별 독립 트랜잭션, FOR UPDATE 잠금 점유 시간 최소화 |
| `faultTolerant + skip` | 잔액 부족 건만 건너뛰고 나머지 계속 처리 |
| `SkipListener` | 롤백 이후 독립 트랜잭션으로 상태 기록 |
| `@StepScope` | Step 실행 시 Bean 생성 (늦은 초기화) |
| `JobParameters (run.id)` | 매일 반복 실행 가능하게 파라미터 유니크화 |
| `SELECT FOR UPDATE` | 잔액 조회-차감 원자성 보장, 이중 차감 방지 |
| `updateToCompleted 반환값 검증` | 동시 배치 실행 감지, 멱등성 보장 |
| `AES-256 (Processor)` | 계좌번호 복호화를 Reader-Writer 사이 변환 단계에서 처리 |

---

## 관련 파일 위치

```
batch/
├── BatchConfig.java                ← Job/Step/Reader/Processor/Writer Bean 정의
├── SettlementItemReader.java       ← ListItemReader 확장, Step 시작 시 DB 로딩
├── SettlementItemProcessor.java    ← 계좌번호 AES 복호화
├── SettlementItemWriter.java       ← FOR UPDATE → 4단계 DB 처리
├── SettlementSkipListener.java     ← skip 후 INSUFFICIENT_BALANCE 기록
└── InsufficientBalanceException.java ← skip 전용 예외 (RuntimeException)

settlement/
└── SettlementScheduler.java        ← @Scheduled(cron="0 0 3 * * *") → JobLauncher.run()
```
