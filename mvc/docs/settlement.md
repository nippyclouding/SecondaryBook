# 정산 (Settlement)

---

## 1. 비즈니스 흐름 — 전체 개요

정산이란 판매자가 물건을 팔고 플랫폼으로부터 판매 대금을 실제로 받는 과정이다.

```
[전체 흐름]

① 판매자가 물건을 등록하고 구매자가 안전결제로 대금을 납입한다.
   → 돈은 바로 판매자에게 가지 않고 플랫폼(관리자 계좌)이 일시 보관한다.
   → 이것이 "안전결제"의 핵심이다. 물건이 제대로 도착하기 전에 돈이 나가면 안 되기 때문이다.

② 구매자가 물건을 받고 구매확정 버튼을 누른다.
   → 이 시점부터 판매자는 정산을 신청할 자격이 생긴다.

③ 판매자가 마이페이지에서 "정산 신청" 버튼을 클릭한다.
   → 플랫폼은 판매가 + 배송비에서 수수료(1%)를 공제한 금액을 계산해 정산 신청서를 생성한다.
   → 예: 판매가 10,000원 + 배송비 3,000원 = 합계 13,000원 → 수수료 130원 → 정산금액 12,870원
   → 상태가 REQUESTED(신청 완료)로 바뀐다.

④ 매일 새벽 3시, 플랫폼의 자동 배치 프로그램이 REQUESTED 상태인 모든 정산 건을 일괄 처리한다.
   → 관리자 계좌에 잔액이 충분하면 → 처리 완료(COMPLETED) 상태로 바뀐다.
   → 관리자 계좌에 잔액이 부족하면 → 잔액부족(INSUFFICIENT_BALANCE) 상태가 되고,
      해당 건은 건너뛰고 나머지 건들은 계속 처리된다.
      이 시점에 판매자에게 "정산 처리 일시 지연" 이메일이 자동 발송된다.

   ※ 관리자 계좌 잔액이 부족할 경우, 관리자는 어드민 페이지에서
     토스페이로 직접 잔액을 충전한 뒤 잔액 부족 건들을 재처리 설정한다.

⑤ 배치 처리 후 관리자는 어드민 페이지에서 "이체 대기" 목록을 확인한다.
   → COMPLETED 상태이면서 아직 실제 이체를 하지 않은 건들이 표시된다.
   → 판매자의 계좌번호, 예금주명, 금액이 복호화되어 보인다.

⑥ 관리자가 은행 앱으로 직접 각 판매자 계좌에 입금한다.
   → 입금 후 어드민 페이지에서 "이체 완료" 버튼을 클릭한다.
   → transfer_confirmed_yn = 1로 저장되어 정산 흐름이 마무리된다.
```

**정리: 구매자 결제 → 관리자 보관 → 구매확정 → 판매자 신청 → 자동 배치 → 관리자 이체 → 완료**

---

## 2. 정산 상태 흐름

```
[sb_trade_info.settlement_st / settlement.settlement_st 동기화]

  NONE
  (거래 등록 직후)
    ↓ 안전결제 완료 + 구매확정
  READY
  (정산 신청 가능 상태)
    ↓ 판매자 정산 신청
  REQUESTED
  (신청 완료 — 배치 대기 중)
    ↓ 배치 처리 성공
  COMPLETED
  (배치 처리 완료 — 관리자 이체 대기)
    → transfer_confirmed_yn = 1 (이체 완료 확인) → 정산 종료

    ↓ 배치 처리 실패 (잔액 부족)
  INSUFFICIENT_BALANCE
  (잔액 부족으로 처리 실패)
    → 관리자 잔액 충전 후 REQUESTED로 재설정
    → 다음 배치 실행 시 재시도
```

---

## 3. 트랜잭션별 상세 흐름

전체 정산 흐름에서 발생하는 트랜잭션을 순서대로 정리한다.

---

### TX-1 · 안전결제 완료 (구매자 결제)

**진입점**: `PaymentController.success()` → `tradeService.completePurchaseAndNotify()`

```
[하나의 트랜잭션 — @Transactional]

① Toss 결제 승인 API 호출 (트랜잭션 외부)
   → tossApiService.confirmPayment(paymentKey, orderId, amount)
   → DONE이 아니면 트랜잭션 진입하지 않음

② 트랜잭션 시작
   ├── sb_trade_info UPDATE
   │     sale_st           = SOLD
   │     member_buyer_seq  = 구매자 seq
   │     safe_payment_st   = COMPLETED
   │     settlement_st     = READY          ← 판매자 정산 신청 가능 상태로 전환
   │     배송지 정보 저장
   ├── chat_msg INSERT (시스템 메시지: [SAFE_PAYMENT_COMPLETED])
   └── COMMIT

③ 트랜잭션 커밋 후 Redis Pub/Sub으로 채팅 메시지 발행 (afterCommit 이벤트)

[실패 시]
Toss 승인은 성공했지만 DB 처리 실패 → tossApiService.cancelPayment() 호출 → 구매자 자동 환불
```

**보안 검증 (트랜잭션 진입 전)**
- 결제 금액 서버 재계산 (클라이언트 amount 신뢰 안 함)
- IDOR 방지: pendingBuyerSeq ↔ 세션 member_seq 일치 여부 확인
- 판매 상태 재확인 (이미 SOLD인지)

---

### TX-2 · 구매확정

**진입점**: `TradeController` → `tradeService.confirmPurchase()`

```
[하나의 트랜잭션 — @Transactional]

sb_trade_info UPDATE
  confirm_purchase = true

COMMIT
```

구매확정은 판매자가 정산 신청을 할 수 있는 전제 조건이다.
`requestSettlement()` 진입 시 `confirm_purchase = true`인지 검증한다.

---

### TX-3 · 정산 신청

**진입점**: `SettlementController.requestSettlement()` → `SettlementService.requestSettlement()`

```
[하나의 트랜잭션 — @Transactional]

① sb_trade_info SELECT ... FOR UPDATE (row lock 획득)
   → 동시에 같은 거래로 두 번 신청하는 경우 차단

② 검증
   ├── 판매자 본인 여부 (seller_seq = member_seq)
   ├── safe_payment_st = COMPLETED
   ├── confirm_purchase = true
   ├── settlement_st = READY (이미 신청됐거나 완료된 건 차단)
   └── member_bank_account 등록 여부

③ 수수료 계산
   총액    = sale_price + delivery_cost
   수수료  = 총액 × 1% (소수점 내림, BigDecimal.FLOOR)
   정산금액 = 총액 - 수수료

④ settlement INSERT
   settlement_st = REQUESTED

⑤ sb_trade_info UPDATE
   settlement_st = REQUESTED

COMMIT

[FOR UPDATE 역할]
같은 거래에 대해 두 요청이 동시에 들어오면:
- 요청 A: FOR UPDATE 잠금 획득 → settlement INSERT 성공
- 요청 B: FOR UPDATE 대기 → A COMMIT 후 잠금 해제
           → settlement_st = REQUESTED 확인 → "이미 신청된 건" 예외 → 중복 신청 차단
```

---

### TX-4 · 배치 정산 처리 (Spring Batch, chunk=1)

**진입점**: `SettlementScheduler` → `JobLauncher.run(settlementJob)` (매일 새벽 3시)

chunk=1이므로 **정산 1건 = 독립 트랜잭션 1개**다.

```
[Reader — Step 시작 시 1회 실행, 트랜잭션 외부]
findAllRequested() → REQUESTED 상태 전체 로딩 → 메모리 List 보관
→ chunk마다 1건씩 꺼내 줌

[Processor — 트랜잭션 외부]
bank_account_no AES-256 복호화
→ Writer가 복호화된 계좌번호로 로그를 기록하기 위해 이 단계에서 처리

[Writer — 청크 트랜잭션 (chunk=1)]

TX-4 시작
  ① admin_account SELECT ... FOR UPDATE (행 잠금 + 최신 잔액 획득)
     → 동시에 다른 TX가 같은 잔액 행을 수정하지 못하게 차단

  ② 잔액 검증
     balance < settlement_amount → InsufficientBalanceException → TX 롤백 → skip

  ③ settlement UPDATE
     settlement_st = COMPLETED
     WHERE settlement_st = 'REQUESTED'   ← 멱등성 조건
     → 반환값 0이면 이미 처리된 건 → IllegalStateException → Job 전체 실패

  ④ admin_account_log INSERT
     (차감 금액, 차감 후 잔액, 이체 대상 계좌 정보)

  ⑤ sb_trade_info UPDATE
     settlement_st = COMPLETED

  ⑥ admin_account UPDATE
     balance = balance - settlement_amount

TX-4 COMMIT → 행 잠금 해제 → 다음 건 처리 시작
```

**chunk=1이 FOR UPDATE와 맞물리는 이유**

```
chunk=1 → 1건 처리 → COMMIT → 잠금 해제 → 다음 건

chunk=10이었다면:
  10건 처리 내내 admin_account 행 잠금 유지
  → 다른 요청(대시보드 조회 등)이 해당 행에 접근하지 못하는 시간이 10배
  → chunk=1로 잠금 점유 시간 최소화
```

**동시 배치 감지 (멱등성)**

```
Job A, Job B가 동시 실행되는 경우:

Job A: settlement_seq=1 → COMPLETED 갱신 (1행 반영)
Job B: settlement_seq=1 → 갱신 시도 → 0행 반영 (이미 COMPLETED)
       → Writer: updated == 0 → IllegalStateException
       → skip 대상 아님 → Job B 전체 실패
       → 이중 차감 없음
```

---

### TX-4a · 잔액 부족 skip → 상태 기록 + 판매자 알림 (독립 트랜잭션)

**진입점**: `SettlementSkipListener.onSkipInWrite()` (TX-4 롤백 직후 Spring Batch가 호출)

```
TX-4 롤백 (InsufficientBalanceException)
  → TX-4에서 했던 모든 DB 변경 취소
  → 잔액도 차감되지 않음

     ↓ (TX-4 종료 후)

TX-4a 시작 (새 트랜잭션 — SkipListener는 트랜잭션 바깥에서 실행됨)
  ① settlement UPDATE
     settlement_st = INSUFFICIENT_BALANCE

  ② sb_trade_info UPDATE
     settlement_st = INSUFFICIENT_BALANCE

TX-4a COMMIT

  ③ member_info SELECT (memberMapper.findByMemberSeq)
     → 판매자 이메일·닉네임 조회

  ④ settlement SELECT (settlementMapper.findBySettlementSeq)
     → 정산 금액 확인

  ⑤ 판매자 이메일 발송 (mailService.sendInsufficientBalanceEmail)
     → 제목: "[SecondHand Books] 정산 처리가 일시 지연되었습니다"
     → 본문: 정산 예정 금액, 지연 사유, 재처리 안내
     → 발송 실패 시 log.error만 기록하고 예외를 삼킴 (COMMIT된 DB 상태에 무영향)
```

**왜 새 트랜잭션이 생성되는가**

`markAsInsufficient()`는 `@Transactional`(전파: REQUIRED)로 선언되어 있다.
SkipListener는 청크 롤백 이후 Spring Batch가 직접 호출하므로, 이 시점에 활성 트랜잭션이 없다.
REQUIRED는 활성 트랜잭션이 없으면 새로 생성하므로, 결과적으로 REQUIRES_NEW와 동일하게 동작한다.

```
[트랜잭션 타임라인]

TX-4  (청크): ─────────────── ROLLBACK
                                    │
TX-4a (skip): 새로 시작 ─────── COMMIT ──→ ③④⑤ (트랜잭션 바깥 — 이메일 발송)

TX-4가 롤백되어도 TX-4a는 영향받지 않는다.
TX-4a 덕분에 잔액 부족 건은 다음 배치 때 무한 반복 처리되지 않는다.
이메일(③④⑤)은 COMMIT 후 실행되므로, 발송 실패가 DB 상태를 되돌리지 않는다.
```

**이메일 발송 설계 원칙**

```
[DB 변경 → COMMIT → 이메일 발송] 순서를 지키는 이유

이메일을 COMMIT 전에 보내면:
  DB 롤백 상황에서도 메일이 나가는 "거짓 알림" 문제 발생

이메일을 COMMIT 후에 보내면:
  DB 상태 변경이 확정된 뒤에만 판매자가 알림을 받음
  메일 발송 실패 → log.error 기록, DB 상태는 이미 COMMIT되어 정상 유지

[판매자 조회 실패 방어]
memberMapper.findByMemberSeq() 반환값이 null이면 (탈퇴 회원 등)
이메일 발송을 건너뛰고 정상 종료한다.
```

---

### TX-5 · 이체 완료 확인

**진입점**: `AdminController.confirmTransfer()` → `SettlementService.confirmTransfer()`

```
[하나의 트랜잭션 — @Transactional]

① settlement UPDATE
   transfer_confirmed_yn = 1
   WHERE settlement_st = 'COMPLETED'
     AND transfer_confirmed_yn = 0    ← 이미 확인된 건 중복 처리 방지

② admin_account_log INSERT
   amount       = 0     (잔액 변동 없음 — 배치에서 이미 차감 완료)
   description  = "거래#N 이체 완료 확인"

COMMIT → 정산 프로세스 종료
```

---

### TX-충전 · 관리자 잔액 충전 (Toss 결제)

**진입점**: `AdminController.balanceChargeSuccess()` → `SettlementService.chargeAdminBalance()`

관리자가 admin_account.balance가 부족할 때 토스페이로 직접 충전하는 흐름이다.

```
[Toss 승인 — 트랜잭션 외부]
tossApiService.confirmPayment(paymentKey, orderId, amount)
→ DONE이 아니면 트랜잭션 진입하지 않음

[하나의 트랜잭션 — @Transactional]

① admin_account UPDATE
   balance = balance + amount        ← 잔액 증가

② admin_account_log INSERT
   amount      = +amount             (양수 = 충전)
   description = "관리자 잔액 충전 | orderId=..."

COMMIT

[실패 시]
DB 처리 실패 → tossApiService.cancelPayment() 자동 호출 → 관리자 카드 자동 환불
```

**충전 후 처리 흐름**

잔액 충전 완료 → 관리자가 어드민 페이지에서 INSUFFICIENT_BALANCE 건들에 "재처리 설정" 클릭
→ settlement_st = REQUESTED로 복원 → 다음 배치 실행 시 TX-4로 재처리

---

## 4. 잔액 부족 (INSUFFICIENT_BALANCE) 처리

---

### 4-1. 발생 조건

배치(TX-4)가 정산 건을 처리할 때 관리자 계좌 잔액을 실시간으로 확인한다.
이 시점에 **관리자 계좌(admin_account) 잔액 < 정산 요청 금액**이면 잔액 부족으로 처리된다.

```
[TX-4 Writer 내부 — 잔액 확인 시점]

admin_account SELECT ... FOR UPDATE  ← 최신 잔액 획득 + 행 락

balance < settlement_amount
  → InsufficientBalanceException 발생
  → TX-4 롤백 (잔액 차감 없음, settlement 상태 변경 없음)
  → 해당 건 skip → 다음 건 처리 계속
```

잔액이 부족한 건만 건너뛰고, 잔액이 충분한 나머지 건들은 계속 정상 처리된다.
즉, 잔액이 일부 건에만 부족해도 전체 배치가 중단되지 않는다.

---

### 4-2. 전체 비즈니스 플로우 (발생 → 해소)

```
[잔액 부족 발생]

① 새벽 3시 배치 실행
   → 정산 건 처리 중 잔액 부족 감지 (balance < settlement_amount)
   → TX-4 롤백 → 해당 건 skip
   → TX-4a: settlement_st = INSUFFICIENT_BALANCE 기록
   → 판매자에게 이메일 자동 발송
      제목: "[SecondHand Books] 정산 처리가 일시 지연되었습니다"
      내용: 거래번호, 정산 예정 금액, 재처리 안내

[판매자 관점]

② 판매자는 이메일로 정산 지연 사실을 즉시 인지한다.
   → 마이페이지에서도 settlement_st = INSUFFICIENT_BALANCE 상태 확인 가능
   → 별도 조치 불필요 — 관리자가 잔액 충전 후 자동 재처리됨

[관리자 관점]

③ 관리자는 어드민 대시보드에서 INSUFFICIENT_BALANCE 건수를 확인한다.
   → "잔액 충전" 버튼 클릭
   → 충전 금액 선택 후 토스페이로 결제
   → admin_account.balance 증가 (TX-충전)

④ 어드민 > 정산 탭의 잔액 부족 목록에서 "재처리 설정" 클릭
   → settlement_st = REQUESTED 로 복원 (resetToRequested)
   → sb_trade_info.settlement_st = REQUESTED 동기화

[자동 재처리]

⑤ 다음 날 새벽 3시 배치가 REQUESTED 건으로 재처리
   → 잔액 충분하면 → COMPLETED
   → 이체 대기 목록에 등장 → 관리자 수동 이체 → 이체 완료 확인 → 정산 종료
```

**정리: 잔액 부족 → 판매자 이메일 수신 → 관리자 토스 충전 → 재처리 설정 → 다음 배치 자동 처리**

---

### 4-3. 이메일 알림 상세

```
[발송 시점]
TX-4a COMMIT 완료 직후
→ DB 상태 변경이 확정된 뒤에만 이메일이 나간다

[이메일 내용]
제목: [SecondHand Books] 정산 처리가 일시 지연되었습니다
본문:
  - 판매자 닉네임
  - 거래 번호 (#trade_seq)
  - 정산 예정 금액 (settlement_amount, 원 단위 포맷)
  - 현재 상태: 잔액 부족 (INSUFFICIENT_BALANCE)
  - 안내: 관리자가 잔액 충전 후 자동 재처리 예정

[발송 주체]
MailService.sendInsufficientBalanceEmail()
→ 기존 회원가입/비밀번호 재설정 이메일과 동일한 JavaMailSender 인프라 재사용
→ Gmail SMTP (smtp.gmail.com:587, TLS)
```

---

### 4-4. 설계 원칙

```
[원칙 1 — DB 변경 → COMMIT → 이메일 순서 보장]

이메일을 COMMIT 전에 보내면:
  → DB 롤백 상황에서도 메일이 나가는 "거짓 알림" 발생
  → 판매자는 정산이 실패했다고 알림받았지만 실제로는 정상 처리될 수 있음

이메일을 COMMIT 후에 보내면:
  → DB 상태 변경이 확정된 뒤에만 판매자에게 알림이 감
  → 메일 발송 실패 → log.error 기록, DB 상태는 이미 COMMIT되어 정상 유지

[원칙 2 — 이메일 실패 격리]

MailService.sendInsufficientBalanceEmail() 내부에서 try-catch로 예외를 삼킨다.
→ 메일 서버 장애가 있어도 INSUFFICIENT_BALANCE 상태 기록은 정상 완료
→ 이메일 실패 시 log.error만 기록

[원칙 3 — 탈퇴 회원 방어]

memberMapper.findByMemberSeq() 반환값이 null이면 (탈퇴 회원 등)
이메일 발송을 건너뛰고 정상 종료한다.
→ NullPointerException 없이 정산 상태 기록은 항상 완료된다.
```

---

## 5. 관리자 업무 흐름

```
[관리자 어드민 페이지 — 정산 탭]

1. 대시보드 확인
   - 관리자 계좌 현재 잔액 확인
   - 처리 대기 건수 (REQUESTED), 완료 건수 (COMPLETED), 잔액 부족 건수 확인

2. 배치 실행 (자동 — 새벽 3시)
   - 시스템이 REQUESTED 상태인 모든 정산 건을 일괄 처리
   - 관리자가 별도로 버튼을 누를 필요 없음

3. [선택] 잔액 부족 시 — 관리자 잔액 충전
   - 어드민 > 정산 탭의 "잔액 충전" 버튼 클릭
   - 충전 금액 선택 후 토스페이로 결제
   - admin_account.balance 증가 (TX-충전)
   - 잔액 부족 건에 "재처리 설정" 클릭 → 다음 배치에서 자동 재처리

4. 이체 대기 목록 확인 (배치 완료 후)
   - COMPLETED + transfer_confirmed_yn=0 인 건 목록
   - 예금주명, 은행코드, 계좌번호(복호화), 정산금액이 표시됨

5. 직접 이체 (관리자 수동 작업)
   - 목록을 보며 은행 앱 또는 인터넷뱅킹으로 각 판매자 계좌에 직접 입금

6. 이체 완료 확인 클릭 (TX-5)
   - 실제 입금한 건에 대해 "이체 완료" 버튼 클릭
   - transfer_confirmed_yn = 1 → 정산 프로세스 종료
```

---

## 6. 수수료 계산

```
총액 = 판매가 + 배송비
수수료 = 총액 × 1% (소수점 내림 처리)
정산금액 = 총액 - 수수료

예시:
  판매가 10,000원 + 배송비 3,000원 = 13,000원
  수수료 = 13,000 × 0.01 = 130원
  정산금액 = 12,870원
```

소수점 처리는 `BigDecimal.FLOOR`(내림)를 사용해 플랫폼이 손해 보지 않도록 한다.

---

## 7. 계좌번호 보안 (AES-256 암호화)

판매자가 등록한 계좌번호는 DB에 평문으로 저장되지 않는다.

```
[저장 시]
평문 계좌번호 → AES-256-CBC 암호화 (16바이트 랜덤 IV 포함) → Base64 인코딩 → DB 저장

[조회 시 — 관리자 이체 목록 / 배치 Processor]
DB 암호화값 → AES-256-CBC 복호화 → 평문 계좌번호 → 화면 표시 또는 로그 기록
```

- IV(초기화 벡터)를 매번 랜덤 생성해 같은 계좌번호도 암호화할 때마다 다른 값이 나온다
- 저장 형식: `Base64(IV):Base64(암호화값)` — 최대 69자 → `VARCHAR(100)` 컬럼
- 암호화 키는 `application.properties`에서 관리 (운영 환경에서는 환경변수 또는 Vault로 분리)
- 배치 Processor 단계에서 복호화해 Writer의 로그에 평문 계좌번호를 기록한다 (Reader-Writer 관심사 분리)

---

## 관련 파일 위치

```
settlement/
├── SettlementService.java
│     requestSettlement()     ← TX-3: 정산 신청
│     markAsInsufficient()    ← TX-4a: 잔액 부족 상태 기록 + 판매자 이메일 알림
│     confirmTransfer()       ← TX-5: 이체 완료 확인
│     chargeAdminBalance()    ← TX-충전: 관리자 잔액 충전
│     findTransferPending()   ← 이체 대기 목록 조회 (복호화 포함)
├── SettlementMapper.java     ← DB 쿼리 인터페이스
├── SettlementController.java ← 판매자 정산 신청 / 정산 내역 조회 엔드포인트
├── SettlementVO.java         ← 정산 데이터 객체
└── SettlementScheduler.java  ← @Scheduled(cron="0 0 3 * * *") → JobLauncher.run()

batch/
├── BatchConfig.java                ← Job/Step/Reader/Processor/Writer Bean 정의, chunk=1, skip 설정
├── SettlementItemReader.java       ← Step 시작 시 REQUESTED 전체 로딩 (@StepScope)
├── SettlementItemProcessor.java    ← 계좌번호 AES-256 복호화
├── SettlementItemWriter.java       ← TX-4: FOR UPDATE → 4단계 DB 처리
├── SettlementSkipListener.java     ← TX-4a: InsufficientBalanceException skip → markAsInsufficient(member_seller_seq 포함)
└── InsufficientBalanceException.java ← skip 전용 예외 (RuntimeException)

member/
├── MailService.java
│     sendInsufficientBalanceEmail() ← TX-4a 이후: 판매자에게 정산 지연 알림 이메일 발송
└── MemberMapper.java
      findByMemberSeq()              ← 판매자 이메일·닉네임 조회 (TX-4a 이메일 발송용)

admin/
├── AdminController.java
│     balanceChargePage()     ← GET /admin/balance/charge (충전 폼)
│     balanceChargeSuccess()  ← GET /admin/balance/success (Toss 콜백 → TX-충전)
│     balanceChargeFail()     ← GET /admin/balance/fail
│     confirmTransfer()       ← POST /admin/api/settlement/confirm-transfer/{seq}
│     resetSettlement()       ← POST /admin/api/settlement/reset/{seq}
└── balanceCharge.jsp         ← 관리자 잔액 충전 UI (프리셋 버튼 + Toss 위젯)
```
