# 거래 · 채팅 · 안전결제 (Trade · Chat · Payment)

---

## 1. 비즈니스 흐름 — 전체 개요

중고책 거래의 핵심은 "판매자와 구매자가 채팅으로 거래를 협의하고, 안전결제를 통해 대금을 에스크로 방식으로 주고받는 것"이다.

```
[전체 흐름]

① 판매자가 판매글을 등록한다.
   → 카카오 책 검색 API로 도서 정보를 선택, S3에 이미지를 업로드하고 DB에 저장한다.
   → 초기 상태: sale_st = SALE, safe_payment_st = NONE

② 구매자가 판매글 상세에서 "채팅하기"를 클릭한다.
   → 채팅방이 없으면 새로 생성된다 (있으면 기존 채팅방 재사용).
   → 자기 자신과의 채팅은 불가능하다.

③ 판매자와 구매자가 채팅으로 거래를 협의한다.
   → 텍스트, 이미지 모두 전송 가능.
   → 실시간 통신은 WebSocket(STOMP) + Redis Pub/Sub으로 처리.

④ 판매자가 채팅방 내 "안전결제 요청" 버튼을 클릭한다.
   → 현재 채팅 상대방(구매자)이 결제 대상으로 지정된다.
   → 5분 타이머가 시작된다. 구매자는 5분 안에 결제해야 한다.
   → safe_payment_st = PENDING

⑤ 구매자가 "결제하기" 버튼을 눌러 Toss 결제를 진행한다.
   → 결제 성공 시 서버가 Toss API에 승인을 요청한다.
   → 승인 완료 후 DB를 업데이트한다.
   → sale_st = SOLD, safe_payment_st = COMPLETED
   → 결제 금액은 관리자 계좌(에스크로)에 보관된다.

⑥ 구매자가 물건을 받고 "구매확정"을 클릭한다.
   → 또는 결제 완료 후 15일이 경과하면 자동 확정된다.
   → confirm_purchase = true → 판매자가 정산 신청 가능 상태가 된다.

[결제 실패/만료 시]
   → safe_payment_st = NONE으로 복원 → 재요청 가능
```

**정리: 판매글 등록 → 채팅 → 안전결제 요청 → Toss 결제 → SOLD → 구매확정 → 정산 신청 가능**

---

## 2. 상태 흐름도

### sale_st (판매 상태)

```
SALE
(판매 중 — 수정/삭제 가능)
  ↓ 안전결제 완료 (safe_payment_st → COMPLETED)
SOLD
(판매 완료 — 수정/삭제 불가)

  또는 판매자 수동 처리:
  POST /trade/sold → SALE → SOLD (안전결제 없이 직거래 완료 표시)
```

### safe_payment_st (안전결제 상태)

```
NONE
(초기 상태 — 안전결제 요청 가능)
  ↓ 판매자가 [SAFE_PAYMENT_REQUEST] 전송
PENDING
(결제 진행 중 — 5분 타이머)
  ├── ↓ 구매자 결제 성공 + Toss 승인 완료
  │  COMPLETED (결제 완료 — 정산 대기 중)
  │
  └── ↓ 결제 실패 / 타임아웃 / 취소
     NONE (재요청 가능)
```

### confirm_purchase (구매확정)

```
false
(결제 완료 직후 — 정산 신청 불가)
  ↓ 구매자 수동 확정 (POST /trade/confirm/{trade_seq})
  또는
  ↓ 결제 완료 후 15일 경과 (SafePaymentScheduler 자정 실행)
true
(구매확정 완료 — 판매자 정산 신청 가능)
```

---

## 3. 트랜잭션별 상세 흐름

전체 흐름에서 발생하는 트랜잭션을 발생 순서대로 정리한다.

---

### TX-1 · 판매글 등록

**진입점**: `TradeController.uploadTrade()` → `TradeService.upload()`

```
[트랜잭션 진입 전]
① @Valid Bean Validation (제목·내용·가격 등)
② S3 이미지 업로드 (트랜잭션 외부)
   → imgService.storeFiles(uploadFiles)
   → CloudFront URL 목록 반환

[하나의 트랜잭션 — @Transactional]
③ sb_trade_info INSERT (tradeMapper.save)
   → trade_seq 생성됨 (useGeneratedKeys)
   → sale_st = SALE, safe_payment_st = NONE
④ book_image INSERT (imgUrl 개수만큼 반복)
   COMMIT

[캐시 무효화]
@CacheEvict: tradeList(allEntries), tradeCount(allEntries)
```

**이미지 처리 원칙**
- 이미지를 S3에 먼저 업로드한 뒤 트랜잭션에서 URL만 DB에 저장
- 트랜잭션 실패 시 S3 이미지는 남아있지만 DB 롤백으로 참조되지 않음 (허용 가능한 수준)

---

### TX-2 · 판매글 수정

**진입점**: `TradeController.modifyUpload()` → `TradeService.modify()`

```
[트랜잭션 진입 전]
① validateCanModify() — 판매자 본인 여부 + 결제 상태 검증
   safe_payment_st가 PENDING 또는 COMPLETED이면 ForbiddenException
② 유지할 기존 이미지(keepImageUrls) + 새 이미지 S3 업로드

[하나의 트랜잭션 — @Transactional]
③ sb_trade_info UPDATE
④ book_image DELETE (기존 이미지 전부)
⑤ book_image INSERT (유지할 이미지 + 새 이미지)
⑥ 제거된 이미지 URL → afterCommit S3 삭제 예약
   COMMIT

⑦ 커밋 후: 제거된 이미지 S3에서 삭제
   (실패해도 트랜잭션에 영향 없음)

[캐시 무효화]
@CacheEvict: trade(key=trade_seq), tradeList(allEntries), tradeCount(allEntries)
```

**afterCommit S3 삭제 이유**
- 트랜잭션 롤백 시 이미지까지 삭제되면 데이터 정합성 파괴
- COMMIT 확정 후에만 S3에서 삭제 (`TransactionSynchronizationManager.registerSynchronization`)

---

### TX-3 · 판매글 삭제

**진입점**: `TradeController.remove()` → `TradeService.remove()`

```
[트랜잭션 진입 전]
① validateCanDelete() — 판매자 본인 여부 + 결제 상태 검증
   safe_payment_st가 PENDING 또는 COMPLETED이면 ForbiddenException

[하나의 트랜잭션 — @Transactional]
② 이미지 URL 목록 조회 (삭제 대상 파악)
③ book_image DELETE
④ sb_trade_info DELETE (soft delete)
⑤ 이미지 URL들 → afterCommit S3 삭제 예약
   COMMIT

⑥ 커밋 후: S3 이미지 삭제

[캐시 무효화]
@CacheEvict: trade(key=trade_seq), tradeList(allEntries), tradeCount(allEntries)
```

---

### TX-4 · 채팅방 생성 (findOrCreate)

**진입점**: `ChatroomController.chat()` (POST /chatrooms) → `ChatroomService.findOrCreateRoom()`

```
[본인 채팅 방지]
member_seller_seq == member_buyer_seq → 채팅방 목록만 반환

[하나의 트랜잭션 — @Transactional]
① chatroom SELECT (seller + buyer + trade 조건)
   → 기존 채팅방 있으면 → 바로 반환 (재사용)
   → 없으면:
② chatroom INSERT (신규 생성)
③ chatroom SELECT (생성된 seq 조회 후 반환)
   COMMIT

[Race Condition 처리]
동시에 두 사용자가 채팅방을 생성하면 DuplicateKeyException 발생
→ catch(DuplicateKeyException) → chatroom SELECT 재시도
→ 먼저 생성된 채팅방을 반환 (중복 생성 방지)
```

---

### TX-5 · 메시지 전송

**진입점**: STOMP `@MessageMapping("/chat/{chat_room_seq}")` → `StompController.sendMessage()`

```
[트랜잭션 없음 — 복수의 독립적인 DB 작업]

① 검증 (트랜잭션 외부)
   ├── WebSocket 세션에서 로그인 사용자 확인
   ├── 채팅방 멤버 여부 확인 (chatroomMapper.isMemberOfChatroom)
   └── 메시지 길이 ≤ 1000자, trade_seq > 0

② [SAFE_PAYMENT_REQUEST] 메시지인 경우 → TX-6으로 분기

③ DB 저장 (각각 별도 트랜잭션)
   ├── messageService.saveMessage()          — chat_msg INSERT
   └── chatroomService.updateLastMessage()   — chatroom UPDATE (last_msg, last_msg_dtm)

④ Redis Pub/Sub 브로드캐스트
   chatMessagePublisher.publishChat(chatRoomSeq, message)
   → Redis PUBLISH "chat:messages" {type:CHAT, chatRoomSeq, payload}
   → 모든 EC2 인스턴스의 ChatMessageSubscriber가 수신
   → SimpMessagingTemplate.convertAndSend("/chatroom/{seq}", payload)
   → 해당 인스턴스에 연결된 WebSocket 클라이언트에게 전달
```

### TX-5a · 읽음 처리

**진입점**: STOMP `@MessageMapping("/chat/{chat_room_seq}/read")` → `StompController.handleReadEvent()`

```
① 세션 + 채팅방 멤버 검증
② messageService.markAsRead(chat_room_seq, member_seq)
   → chat_msg UPDATE SET read_yn = 1 (해당 채팅방, 상대방이 보낸 메시지)
③ chatMessagePublisher.publishRead(chatRoomSeq, readerMemberSeq)
   → Redis PUBLISH {type:READ, chatRoomSeq, readerMemberSeq}
   → Subscriber: convertAndSend("/chatroom/{seq}/read", readerMemberSeq)
   → 상대방 화면에서 "읽음" 표시 업데이트
```

---

### TX-6 · 안전결제 요청

**진입점**: `StompController.sendMessage()` → `canUseSafePayment()` → `TradeService.requestSafePayment()`

```
[검증 (트랜잭션 외부)]
① 전송자가 해당 거래의 판매자인지 확인
   trade.member_seller_seq == sessionMember.member_seq
② sale_st == SALE 확인 (이미 판매 완료된 거래 차단)
③ safe_payment_st == NONE 확인 (이미 요청 중인 거래 차단)

[하나의 트랜잭션 — @Transactional + @CacheEvict]
④ sb_trade_info UPDATE
   SET safe_payment_st         = 'PENDING'
     , safe_payment_expire_dtm = NOW() + INTERVAL 5 MINUTE
     , pending_buyer_seq       = {현재 채팅 상대 구매자 seq}
   WHERE safe_payment_st = 'NONE'       ← 상태 가드 (동시 요청 차단)

   UPDATE 결과 = 0 → 다른 트랜잭션이 이미 PENDING 처리
   → publishError([SAFE_PAYMENT_IN_PROGRESS]) 전송 후 종료

   UPDATE 결과 > 0 → 요청 승인
   COMMIT

[정상 처리 후]
⑤ [SAFE_PAYMENT_REQUEST] 메시지 DB 저장 + last_msg 업데이트
⑥ Redis Pub/Sub 브로드캐스트
   → 구매자 화면: 5분 카운트다운 + "결제하기" 버튼 활성화
```

**동시성 처리 포인트**
- `WHERE safe_payment_st = 'NONE'` 조건이 낙관적 잠금 역할
- 동시에 두 건의 요청이 들어와도 DB가 하나만 업데이트하고 나머지는 0을 반환

---

### TX-7 · 결제창 진입 검증

**진입점**: `PaymentController.showPayment()` (GET /payments?trade_seq=...)

```
[모두 읽기 전용 — 검증 전용]

① 세션 로그인 확인
② trade 조회 (캐시)
③ PaymentVO 조회 (safe_payment_st, pending_buyer_seq, 남은 초)

검증 항목:
├── 판매자 본인은 접근 불가 (seller_seq == session.member_seq → redirect:/)
├── sale_st ≠ SOLD (이미 판매 완료된 상품)
├── safe_payment_st == PENDING (결제 요청이 없으면 접근 불가)
└── pending_buyer_seq == session.member_seq (IDOR 방지: 지정된 구매자만 접근)

검증 통과 → payform.jsp 반환
  ├── 남은 결제 시간 (remainingSeconds)
  ├── 구매자 배송지 목록
  └── 거래 정보 (sale_price, delivery_cost 등)
```

---

### TX-8 · 안전결제 완료 (Toss 승인 → DB 반영)

**진입점**: `PaymentController.success()` (GET /payments/success) — Toss가 리다이렉트하는 콜백

```
[트랜잭션 진입 전 — 다중 검증]

① 거래 조회 (trade가 없으면 fail redirect)
② 서버 금액 재계산
   serverAmount = trade.sale_price + trade.delivery_cost
   → 클라이언트 amount 파라미터 신뢰 안 함 (조작 방지)
   → serverAmount ≠ amount → cancelSafePayment() + fail redirect
③ 판매 상태 검증
   sale_st == SOLD 또는 safe_payment_st ≠ PENDING → fail redirect
④ Toss API 결제 승인 요청 (트랜잭션 완전 외부)
   tossApiService.confirmPayment(paymentKey, orderId, serverAmount)
   → POST https://api.tosspayments.com/v1/payments/confirm
   → Idempotency-Key: paymentKey (중복 승인 방지)
   → 응답 status ≠ DONE → fail redirect
⑤ 세션 재확인 (Toss 승인 후 세션 만료 체크)
⑥ IDOR 재검증 (Toss 승인 직후 — 가장 마지막 방어선)
   paymentCheck.pending_buyer_seq == session.member_seq 재확인
   → 불일치 시 fail redirect

[하나의 트랜잭션 — @Transactional (completePurchaseAndNotify)]

⑦ tradeMapper.successPurchase()
   UPDATE sb_trade_info
   SET sale_st          = 'SOLD'
     , member_buyer_seq = #{buyerSeq}
     , safe_payment_st  = 'COMPLETED'
     , settlement_st    = 'READY'       ← 정산 신청 가능 상태로 전환
     , post_no / addr_h / addr_d        ← 배송지 저장
   WHERE trade_seq = #{tradeSeq}
     AND safe_payment_st = 'PENDING'    ← 상태 가드 (중복 결제 방지)

   UPDATE 결과 = 0 → InvalidRequestException (이미 처리된 거래)

⑧ settlementMapper.increaseAdminBalance(ADMIN_ACCOUNT_SEQ, amount)
   → 구매자 결제 금액을 관리자 계좌(에스크로)에 보관

⑨ 채팅방 조회 + [SAFE_PAYMENT_COMPLETE] 메시지 저장
   messageService.saveMessage()

⑩ afterCommit 등록
   → COMMIT 확정 후에만 Redis Pub/Sub 발행
   chatMessagePublisher.publishPayment(chatRoomSeq, completeMsg)

COMMIT

[커밋 후]
⑪ Redis Pub/Sub 발행 (afterCommit 실행)
   → 판매자 화면: "결제 완료" 알림 수신
   → 실패해도 트랜잭션에 영향 없음

[DB 처리 실패 시 — catch 블록]
⑫ tossApiService.cancelPayment(paymentKey, "결제 처리 중 서버 오류로 자동 취소")
   → Toss에 취소 요청 → 구매자 자동 환불

[성공 완료]
⑬ PRG (Post-Redirect-Get) 패턴으로 리다이렉트
   FlashAttribute에 payment 정보 담아 redirect:/payments/result?status=success
   → paymentKey 등 민감정보가 URL에 남지 않음
```

**afterCommit 발행 이유**
- DB COMMIT 이전에 Pub/Sub을 발행하면 수신 측이 즉시 DB를 조회할 때 아직 COMMIT 안 된 구 데이터를 읽을 수 있음
- `TransactionSynchronizationManager.registerSynchronization().afterCommit()` 으로 커밋 확정 후에만 발행

**Toss API를 트랜잭션 외부에서 호출하는 이유**
- 외부 HTTP 호출이 길어지면 DB 커넥션을 오래 점유 → HikariCP 풀 고갈 위험
- Toss 승인 실패 시 불필요한 DB 롤백 비용 제거

---

### TX-9 · 결제 실패 / 사용자 취소

**진입점**: `PaymentController.fail()` (GET /payments/fail) — Toss가 리다이렉트하는 실패 콜백

```
[조건부 처리]
trade_seq 파라미터가 있고 세션 로그인 상태일 때:

① isBuyerOfTrade(trade_seq, session.member_seq) — 채팅방 참여자 검증 (IDOR 방지)
   → 구매자가 아닌 경우 redirect:/

② tradeService.cancelSafePayment(trade_seq)
   UPDATE sb_trade_info SET safe_payment_st = 'NONE'
   → PENDING → NONE 복원 (재요청 가능)

③ sendPaymentFailedMessage(trade_seq, member_seq)
   → chat_msg INSERT ([SAFE_PAYMENT_FAILED], sender_seq = 0L)
   → chatMessagePublisher.publishPayment() (즉시 Pub/Sub)

④ fail.jsp 반환 (errorCode, errorMessage 표시)
```

---

### TX-10 · 결제 타임아웃

**진입점 A — 클라이언트**: `POST /payments/timeout`

```
[클라이언트 5분 타이머 만료 또는 페이지 이탈 시 호출]

① 세션 검증 + isBuyerOfTrade 검증 (IDOR 방지)
② tradeService.cancelSafePayment() → NONE 복원
③ sendPaymentFailedMessage() → 채팅방에 [SAFE_PAYMENT_FAILED]
```

**진입점 B — 서버 스케줄러**: `SafePaymentScheduler.cleanupExpiredSafePayments()` (매 60초)

```
[클라이언트 미호출 시 서버 사이드 안전망]

[하나의 트랜잭션 — @Transactional]
tradeMapper.resetExpiredSafePayments()
UPDATE sb_trade_info
SET safe_payment_st         = 'NONE'
  , pending_buyer_seq       = NULL
  , safe_payment_expire_dtm = NULL
WHERE safe_payment_st = 'PENDING'
  AND safe_payment_expire_dtm < NOW()
COMMIT

→ 결과 > 0일 때만 로그 출력
@CacheEvict: trade(allEntries)
```

**두 가지 타임아웃 처리가 모두 필요한 이유**
- 클라이언트가 비정상 종료되거나 네트워크 단절된 경우 서버 스케줄러가 최종 안전망 역할
- 클라이언트는 사용자 경험(즉시 UI 복원)을 위해, 서버는 데이터 정합성을 위해 각각 처리

---

### TX-11 · 구매확정 (수동)

**진입점**: `TradeController.confirmPurchase()` → `TradeService.confirmPurchase()`

```
① validateBuyerOwnership(trade_seq, member_seq)
   → member_buyer_seq == session.member_seq 검증 (IDOR 방지)

[하나의 트랜잭션 — @Transactional]
② tradeMapper.confirmPurchase(trade_seq, member_seq)
   UPDATE sb_trade_info
   SET confirm_purchase = true
   WHERE trade_seq      = #{trade_seq}
     AND member_buyer_seq = #{member_seq}   ← 구매자 조건 재검증
   COMMIT

[캐시 무효화]
@CacheEvict: trade(key=trade_seq)

→ confirm_purchase = true → 판매자 정산 신청 가능
```

---

### TX-12 · 자동 구매확정 (15일 스케줄러)

**진입점**: `SafePaymentScheduler.autoConfirmExpiredPurchases()` (매일 자정)

```
[하나의 트랜잭션 — @Transactional]
tradeMapper.autoConfirmExpiredPurchases()
UPDATE sb_trade_info
SET confirm_purchase = true
WHERE safe_payment_st = 'COMPLETED'
  AND confirm_purchase = false
  AND DATEDIFF(NOW(), safe_payment_cmp_dtm) >= 15
COMMIT

→ 처리 건수 > 0일 때만 로그 출력

[캐시 무효화]
@CacheEvict: trade(allEntries)
```

---

### TX-13 · 채팅 이미지 전송

```
[트랜잭션 없음 — HTTP + STOMP 두 단계 분리]

[1단계: 이미지 업로드 (HTTP)]
POST /chat/image/upload (multipart)
① 세션 검증
② isMemberOfChatroom 검증 (채팅방 참여자인지)
③ imgService.uploadFile(image) → S3 업로드 → CloudFront URL 반환
④ {success: true, imageUrl: "..."} JSON 응답

[2단계: 메시지 전송 (STOMP)]
클라이언트가 imageUrl을 받아 STOMP 메시지로 전송
chat_cont = "[IMAGE]https://cdn.cloudfront.net/images/{uuid}.jpg"

→ StompController가 일반 메시지와 동일하게 처리:
   DB 저장 + updateLastMessage("사진을 보냈습니다.") + Pub/Sub 브로드캐스트
```

---

## 4. Redis Pub/Sub — 멀티 서버 실시간 통신

```
[문제]
ALB가 유저A를 EC2 #1, 유저B를 EC2 #2로 라우팅하면
WebSocket 세션은 각 서버 메모리에 바인딩되어 있으므로
EC2 #1에서 convertAndSend()해도 EC2 #2의 유저B에게 전달되지 않는다.

[해결: Redis Pub/Sub]

유저A → EC2 #1 StompController
           ↓ DB 저장
           ↓ chatMessagePublisher.publish*()
           ↓ redisTemplate.convertAndSend("chat:messages", JSON)
                              ↓
                   Redis가 모든 구독자에게 전파
                              ↓
         EC2 #1 ChatMessageSubscriber.onMessage()
             → SimpMessagingTemplate → EC2 #1의 WebSocket 클라이언트
         EC2 #2 ChatMessageSubscriber.onMessage()
             → SimpMessagingTemplate → EC2 #2의 WebSocket 클라이언트 (유저B 수신 ✅)
```

### 메시지 타입별 처리

| 타입 | publish 메서드 | STOMP 구독 경로 | 사용 시점 |
|------|--------------|---------------|---------|
| `CHAT` | `publishChat()` | `/chatroom/{seq}` | 일반 텍스트/이미지 메시지 |
| `PAYMENT` | `publishPayment()` | `/chatroom/{seq}` | 안전결제 요청/완료/실패 알림 |
| `READ` | `publishRead()` | `/chatroom/{seq}/read` | 읽음 처리 이벤트 |
| `ERROR` | `publishError()` | `/chatroom/{seq}` | 안전결제 진행 중 오류 |

---

## 5. 보안 검증 체크리스트

| 검증 항목 | 검증 위치 | 방법 |
|----------|---------|------|
| **금액 조작 방지** | `PaymentController.success()` | 클라이언트 amount 무시, 서버에서 `sale_price + delivery_cost` 직접 계산 |
| **IDOR — 결제창 접근** | `PaymentController.showPayment()` | `pending_buyer_seq == session.member_seq` |
| **IDOR — 결제 승인** | `PaymentController.success()` | Toss 승인 직후 `pending_buyer_seq` 재검증 |
| **IDOR — 타임아웃** | `PaymentController.timeout()` | `isBuyerOfTrade(trade_seq, member_seq)` |
| **IDOR — 결제 실패** | `PaymentController.fail()` | `isBuyerOfTrade(trade_seq, member_seq)` |
| **IDOR — 구매확정** | `TradeService.validateBuyerOwnership()` | `member_buyer_seq == session.member_seq` |
| **자기 자신 채팅** | `ChatroomController.chat()` | `seller_seq == buyer_seq` 차단 |
| **채팅방 STOMP 접근** | `StompController.validateSessionAndMembership()` | `isMemberOfChatroom(chatRoomSeq, member_seq)` |
| **채팅방 HTTP 접근** | `ChatroomController.getMessages()` | `isMemberOfChatroom` |
| **채팅 이미지 접근** | `ChatroomController.uploadChatImage()` | `isMemberOfChatroom` |
| **안전결제 요청 권한** | `StompController.canUseSafePayment()` | 판매자 본인 + sale_st=SALE + safe_payment_st=NONE |
| **동시 결제 요청 차단** | `TradeService.requestSafePayment()` | `WHERE safe_payment_st = 'NONE'` UPDATE 가드 |
| **중복 결제 처리 차단** | `TradeService.completePurchaseAndNotify()` | `WHERE safe_payment_st = 'PENDING'` UPDATE 가드 |
| **판매글 수정/삭제 잠금** | `TradeService.validateCanModify/Delete()` | safe_payment_st = PENDING/COMPLETED 이면 ForbiddenException |
| **XSS 방지 (배송지)** | `PaymentController.success()` | `HtmlUtils.htmlEscape(addr_h/addr_d/post_no)` |

---

## 6. 캐시 전략

```
캐시 저장소: Redis (RedisCacheConfig)

[캐시 항목]
trade      TTL 10분  — 거래 상세 (key: trade_seq)
tradeList  TTL 5분   — 거래 목록 (key: 페이지+검색조건 조합)
tradeCount TTL 10분  — 거래 총 개수 (key: 검색조건 조합)

[캐시 무효화 (@CacheEvict)]
판매글 등록    → tradeList(allEntries), tradeCount(allEntries)
판매글 수정    → trade(key), tradeList(allEntries), tradeCount(allEntries)
판매글 삭제    → trade(key), tradeList(allEntries), tradeCount(allEntries)
안전결제 요청  → trade(key)
결제 완료      → trade(key)   — completePurchaseAndNotify 포함
구매확정       → trade(key)
만료 리셋 스케줄러 → trade(allEntries)
자동 확정 스케줄러 → trade(allEntries)
```

---

## 7. 스케줄러 요약

| 스케줄러 | 실행 주기 | 동작 | 관련 TX |
|----------|---------|------|---------|
| `SafePaymentScheduler.cleanupExpiredSafePayments()` | 매 60초 | 만료된 PENDING 건을 NONE으로 일괄 리셋 | TX-10B |
| `SafePaymentScheduler.autoConfirmExpiredPurchases()` | 매일 자정 | 결제 완료 후 15일 경과 건 confirm_purchase → true | TX-12 |

---

## 8. 관련 파일 위치

| 역할 | 파일 |
|------|------|
| 판매글 CRUD | `trade/TradeController.java`, `trade/TradeService.java`, `trade/TradeMapper.java` |
| 채팅방 생성/조회 | `chat/chatroom/ChatroomController.java`, `chat/chatroom/ChatroomService.java` |
| STOMP 메시지 처리 | `chat/StompController.java` |
| Redis Pub/Sub 발행 | `chat/pubsub/ChatMessagePublisher.java` |
| Redis Pub/Sub 수신 | `chat/pubsub/ChatMessageSubscriber.java` |
| 안전결제 흐름 | `payment/PaymentController.java` |
| Toss API 호출 | `payment/TossApiService.java` |
| 거래 상태 업데이트 | `trade/TradeService.java` — `requestSafePayment`, `completePurchaseAndNotify`, `cancelSafePayment` |
| 스케줄러 | `payment/SafePaymentScheduler.java` |
| 이미지 업로드 | `util/imgUpload/ImgService.java` (→ S3Service 위임) |
| Mapper XML | `resources/project/trade/tradeMapper.xml`, `resources/project/chat/` |
| JSP 뷰 | `views/trade/`, `views/chat/chatrooms.jsp`, `views/payment/` |
