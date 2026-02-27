# STOMP 기반 실시간 채팅 흐름

## 전체 구조 요약

```
클라이언트 (chat.js)
    │
    │  SockJS + STOMP
    │
    ▼
┌──────────────────────────────────────────────────────────┐
│  Spring Server                                           │
│                                                          │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────┐ │
│  │ StompConfig │    │   Simple     │    │   Stomp     │ │
│  │ (설정)      │───→│   Broker     │    │  Controller │ │
│  │             │    │  /chatroom   │    │ @Message    │ │
│  │ endpoint:   │    │              │    │  Mapping    │ │
│  │ /chatEnd    │    └──────────────┘    └─────────────┘ │
│  │  Point      │                                        │
│  └─────────────┘                                        │
│                     ┌──────────────┐    ┌─────────────┐ │
│                     │   Redis      │    │   Redis     │ │
│                     │   Publisher  │───→│  Subscriber │ │
│                     │              │    │  (Listener) │ │
│                     └──────────────┘    └─────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## 1단계: STOMP 설정 (StompConfig.java)

```
StompConfig
├── WebSocket 엔드포인트 등록
│   └── /chatEndPoint (SockJS fallback 지원)
│       └── HttpSessionHandshakeInterceptor → WebSocket 핸드셰이크 시 HttpSession을 STOMP 세션에 복사
│
├── Application Destination Prefix
│   └── /sendMessage  → 클라이언트가 메시지를 보낼 때 이 prefix 사용
│                        서버의 @MessageMapping 메서드로 라우팅됨
│
└── Simple Broker
    └── /chatroom     → 서버가 클라이언트에게 메시지를 보낼 때 이 prefix 사용
                         클라이언트가 subscribe하는 경로
```

### 경로 규칙 정리

```
[클라이언트 → 서버] (SEND)
  /sendMessage/chat/{chat_room_seq}         → StompController.sendMessage()
  /sendMessage/chat/{chat_room_seq}/read    → StompController.handleReadEvent()

[서버 → 클라이언트] (SUBSCRIBE)
  /chatroom/{chat_room_seq}                 → 채팅 메시지, 결제 알림, 에러 메시지
  /chatroom/{chat_room_seq}/read            → 읽음 이벤트
```

---

## 2단계: 클라이언트 연결 및 구독 (chat.js)

```
페이지 로드 (window.onload)
    │
    ├── 1. connect()
    │       │
    │       ├── new SockJS('/chatEndPoint')          ← WebSocket 연결 (SockJS fallback)
    │       ├── stompClient = Stomp.over(socket)     ← STOMP 프로토콜 래핑
    │       └── stompClient.connect()                ← STOMP 핸드셰이크
    │               │
    │               └── 연결 성공 시 subscribeCurrentRoom() 호출
    │
    └── 2. subscribeCurrentRoom()
            │
            ├── stompClient.subscribe('/chatroom/' + chat_room_seq)
            │       → 수신 콜백: showMessage(msg) → 화면에 메시지 렌더링
            │       → 상대방 메시지면 sendReadEvent() 자동 호출
            │
            └── stompClient.subscribe('/chatroom/' + chat_room_seq + '/read')
                    → 수신 콜백: updateReadStatus() → 내 메시지에 ✔ 표시
```

### 채팅방 전환 시

```
채팅방 클릭 (setupChatroomClick)
    │
    ├── chat_room_seq 업데이트
    ├── stompClient.disconnect()     ← 기존 구독 해제
    ├── connect()                    ← 새 채팅방으로 재연결 + 재구독
    └── fetchMessages(chat_room_seq) ← HTTP로 기존 메시지 조회 (GET /chat/messages)
```

---

## 3단계: 메시지 전송 흐름 (전체 다이어그램)

### 일반 채팅 메시지

```
유저A (chat.js)                    서버                              유저B (chat.js)
    │                               │                                   │
    │  stompClient.send(            │                                   │
    │    "/sendMessage/chat/15",    │                                   │
    │    {chat_cont: "안녕",        │                                   │
    │     sender_seq: 1,            │                                   │
    │     trade_seq: 42}            │                                   │
    │  )                            │                                   │
    │──── STOMP SEND ──────────────→│                                   │
    │                               │                                   │
    │               StompController.sendMessage()                       │
    │               @MessageMapping("/chat/{chat_room_seq}")            │
    │                               │                                   │
    │                   ┌───────────┼───────────┐                       │
    │                   │           │           │                       │
    │           1. 검증  │   2. DB   │  3. Redis │                       │
    │           세션확인  │   저장    │  Pub/Sub  │                       │
    │           멤버확인  │          │           │                       │
    │           길이검증  │          │           │                       │
    │                   └───────────┼───────────┘                       │
    │                               │                                   │
    │                  chatMessagePublisher.publishChat()                │
    │                               │                                   │
    │                  redisTemplate.convertAndSend(                     │
    │                    "chat:messages", JSON)                          │
    │                               │                                   │
    │                  ┌────────────┼────────────┐                      │
    │                  ▼            │            ▼                      │
    │            [EC2 #1]          │      [EC2 #2]                     │
    │        ChatMessage           │   ChatMessage                     │
    │        Subscriber            │   Subscriber                      │
    │        .onMessage()          │   .onMessage()                    │
    │              │               │         │                          │
    │    messagingTemplate         │  messagingTemplate                 │
    │    .convertAndSend(          │  .convertAndSend(                  │
    │      "/chatroom/15",         │    "/chatroom/15",                 │
    │      message)                │    message)                        │
    │              │               │         │                          │
    │←── STOMP MESSAGE ────────────│         │── STOMP MESSAGE ────────→│
    │                              │                                    │
    │  showMessage(msg)            │                      showMessage(msg)
    │  화면에 렌더링                │                      화면에 렌더링
    │                              │                      + sendReadEvent() 자동
```

### 읽음 처리

```
유저B                              서버                              유저A
  │                                 │                                  │
  │  (메시지 수신 콜백에서           │                                  │
  │   상대방 메시지 감지)            │                                  │
  │                                 │                                  │
  │  stompClient.send(              │                                  │
  │    "/sendMessage/chat/15/read"  │                                  │
  │  )                              │                                  │
  │──── STOMP SEND ────────────────→│                                  │
  │                                 │                                  │
  │                 StompController.handleReadEvent()                   │
  │                 @MessageMapping("/chat/{chat_room_seq}/read")       │
  │                                 │                                  │
  │                     1. DB: 안읽은 메시지 → read_yn = true          │
  │                     2. chatMessagePublisher.publishRead()           │
  │                                 │                                  │
  │                     Redis PUBLISH ("chat:messages")                 │
  │                                 │                                  │
  │                     ChatMessageSubscriber.onMessage()               │
  │                     type == READ                                    │
  │                                 │                                  │
  │                     messagingTemplate.convertAndSend(               │
  │                       "/chatroom/15/read",                          │
  │                       readerMemberSeq)                              │
  │                                 │                                  │
  │                                 │──── STOMP MESSAGE ──────────────→│
  │                                 │                                  │
  │                                 │                    updateReadStatus()
  │                                 │                    내 메시지에 ✔ 표시
```

### 안전결제 요청

```
판매자 (chat.js)                   서버                              구매자 (chat.js)
  │                                 │                                  │
  │  stompClient.send(              │                                  │
  │    "/sendMessage/chat/15",      │                                  │
  │    {chat_cont:                  │                                  │
  │     "[SAFE_PAYMENT_REQUEST]",   │                                  │
  │     trade_seq: 42}              │                                  │
  │  )                              │                                  │
  │──── STOMP SEND ────────────────→│                                  │
  │                                 │                                  │
  │                 StompController.sendMessage()                       │
  │                                 │                                  │
  │                     chat_cont == "[SAFE_PAYMENT_REQUEST]" 감지     │
  │                     → canUseSafePayment() 검증                     │
  │                       ├── 판매자 본인인지 확인                      │
  │                       ├── sale_st == SALE 확인                     │
  │                       ├── safe_payment_st == NONE 확인             │
  │                       └── tradeService.requestSafePayment()        │
  │                            ├── safe_payment_st → PENDING           │
  │                            ├── pending_buyer_seq 설정              │
  │                            └── expire_dtm = NOW + 5분              │
  │                                 │                                  │
  │                     검증 통과 시:                                   │
  │                     1. DB 저장                                     │
  │                     2. chatMessagePublisher.publishChat()           │
  │                                 │                                  │
  │←── STOMP MESSAGE ───────────────│──── STOMP MESSAGE ──────────────→│
  │                                 │                                  │
  │  안전결제 요청 카드 렌더링       │              안전결제 요청 카드 렌더링
  │                                 │              + "결제하기" 버튼 표시
  │                                 │              + 5분 카운트다운 시작
```

### 결제 완료 알림 (PaymentController → 채팅방)

```
구매자 (브라우저)                   서버                              판매자 (chat.js)
  │                                 │                                  │
  │  Toss 결제 성공                 │                                  │
  │  GET /payments/success          │                                  │
  │────── HTTP ────────────────────→│                                  │
  │                                 │                                  │
  │                 PaymentController.handlePaymentSuccess()            │
  │                                 │                                  │
  │                     1. 금액 검증                                    │
  │                     2. Toss API 결제 승인 (WebClient)               │
  │                     3. sale_st → SOLD, safe_payment_st → COMPLETED │
  │                     4. chatMessagePublisher.publishPayment()        │
  │                                 │                                  │
  │                     Redis PUBLISH ("chat:messages")                 │
  │                                 │                                  │
  │                     ChatMessageSubscriber.onMessage()               │
  │                     type == PAYMENT                                 │
  │                                 │                                  │
  │                     messagingTemplate.convertAndSend(               │
  │                       "/chatroom/15",                               │
  │                       "[SAFE_PAYMENT_COMPLETE]" 메시지)             │
  │                                 │                                  │
  │  success.jsp 렌더링              │──── STOMP MESSAGE ──────────────→│
  │                                 │                                  │
  │                                 │              결제 완료 알림 수신
  │                                 │              채팅방에 완료 메시지 표시
```

---

## 4단계: Redis Pub/Sub — 도입 배경과 원리

### Pub/Sub을 도입하게 된 이유 — 1:1 채팅에서 발생한 문제

처음에는 Redis Pub/Sub 없이 Spring의 SimpleBroker만으로 채팅을 구현했다.
SimpleBroker는 서버 메모리 안에서 동작하는 인메모리 메시지 브로커다.
단일 서버에서는 문제없이 동작했다.

문제는 EC2가 2대 이상이 되는 순간 발생했다.

```
[Pub/Sub 도입 전 — EC2 2대 환경]

유저A (판매자) → EC2 #1에 WebSocket 연결
유저B (구매자) → EC2 #2에 WebSocket 연결
                              (ALB가 각각 다른 서버로 라우팅)

유저A가 메시지 전송
  → EC2 #1이 DB에 저장          ← DB에는 기록됨
  → EC2 #1의 SimpleBroker가 브로드캐스트
  → EC2 #1에 연결된 클라이언트에게만 전달
  → 유저B는 EC2 #2에 있으므로 수신 불가  ✗
```

유저B는 새로고침을 하면 DB에서 메시지를 가져올 수 있지만,
그것은 실시간 채팅이 아니라 게시판 방식이다.

**1:1 채팅에서 두 사람이 서로 다른 서버에 연결되면 실시간 대화가 불가능했다.**
이것이 Redis Pub/Sub을 도입한 직접적인 계기다.

---

### Pub/Sub의 원리

Pub/Sub은 발행(Publish) — 구독(Subscribe) 패턴이다.

```
[개념 구조]

Publisher (발행자)
  → 채널에 메시지를 보냄
  → 누가 받는지 신경 쓰지 않음

채널 (Channel)
  → Redis 서버 안에 존재하는 메시지 통로
  → 이름이 있음 ("chat:messages")

Subscriber (구독자)
  → 채널을 구독하면 발행된 메시지를 즉시 수신
  → 여러 구독자가 같은 채널을 구독할 수 있음
```

```
[Redis Pub/Sub 메시지 흐름]

EC2 #1이 "chat:messages" 채널에 PUBLISH
  ↓
Redis 서버가 이 채널을 구독 중인 모든 인스턴스에게 메시지 전파
  ↓
EC2 #1, EC2 #2 모두 SUBSCRIBE로 수신
  ↓
각 EC2가 자신에게 연결된 WebSocket 클라이언트에게 전달
  → 유저A(EC2 #1에 연결)도 수신
  → 유저B(EC2 #2에 연결)도 수신  ✅
```

핵심은 **Redis가 모든 서버 인스턴스를 연결하는 중간 허브** 역할을 한다는 것이다.
각 서버는 Redis만 바라보면 되고, 서버끼리 직접 통신할 필요가 없다.

---

### DB 저장과 Pub/Sub의 역할 분담

둘이 해결하는 문제가 다르다.

| 역할 | 기술 | 이유 |
|------|------|------|
| 메시지 영속성 | DB (MySQL) | 채팅방에 나중에 들어왔을 때 이전 메시지를 볼 수 있어야 함 |
| 실시간 전달 | Redis Pub/Sub | 현재 연결된 다른 서버의 클라이언트에게 즉시 전달 |

DB에 저장한다고 Pub/Sub이 불필요해지지 않는다.
DB는 "나중에 조회"를 해결하고, Pub/Sub은 "지금 즉시 전달"을 해결한다.

---

### Redis Pub/Sub vs 다른 방식

| 방식 | 설명 | 문제점 |
|------|------|--------|
| **SimpleBroker만 사용** | 서버 메모리 내 브로드캐스트 | 다른 서버 인스턴스에 연결된 클라이언트에게 전달 불가 |
| **Sticky Session** | ALB가 같은 사용자를 항상 같은 서버로 연결 | 특정 서버 과부하, 서버 장애 시 세션 유실 |
| **DB 폴링** | 주기적으로 DB를 확인해 새 메시지 수신 | 폴링 간격만큼 지연, DB 부하 증가 |
| **Redis Pub/Sub** | 채널 기반 실시간 브로드캐스트 | Redis 장애 시 실시간 전달 불가 (DB에는 저장됨) |

Redis Pub/Sub은 구현이 간단하고 지연이 거의 없다.
Redis가 장애가 나도 DB에는 메시지가 저장되어 있어서 새로고침으로 복구 가능하다.

---

## 5단계: Redis Pub/Sub 계층 상세

### 구성 요소 (RedisConfig.java)

```
RedisConfig
├── ChannelTopic: "chat:messages"                ← 모든 채팅 이벤트가 이 채널 하나를 공유
├── MessageListenerAdapter                       ← ChatMessageSubscriber.onMessage()를 호출하도록 연결
└── RedisMessageListenerContainer                ← Redis 연결 + 리스너 + 토픽 바인딩
```

### 메시지 타입별 라우팅 (ChatMessageSubscriber.onMessage)

```
Redis 채널 "chat:messages" 수신
    │
    ├── ChatMessage 역직렬화 (JSON → ChatMessage)
    │
    └── switch (type)
        │
        ├── CHAT / PAYMENT / ERROR
        │   → messagingTemplate.convertAndSend("/chatroom/{roomSeq}", payload)
        │   → 해당 채팅방을 구독 중인 모든 클라이언트에게 전달
        │
        └── READ
            → messagingTemplate.convertAndSend("/chatroom/{roomSeq}/read", readerMemberSeq)
            → 읽음 이벤트 전용 경로로 전달
```

### ChatMessage DTO

```
ChatMessage
├── type: MessageType (CHAT, READ, PAYMENT, ERROR)
├── chatRoomSeq: long
├── payload: Object         ← CHAT/PAYMENT/ERROR 시 사용 (MessageVO 등)
└── readerMemberSeq: Long   ← READ 시 사용 (누가 읽었는지)
```

### Publisher 호출 위치

```
ChatMessagePublisher
├── publishChat(roomSeq, message)      ← StompController.sendMessage()
├── publishRead(roomSeq, memberSeq)    ← StompController.handleReadEvent()
│                                        ChatroomController.getMessages() (HTTP 메시지 조회 시)
├── publishPayment(roomSeq, message)   ← PaymentController.handlePaymentSuccess()
│                                        PaymentController.handlePaymentFail()
└── publishError(roomSeq, message)     ← StompController (안전결제 중복 요청 시)
```

---

## 6단계: 보안 검증 흐름

```
클라이언트 STOMP SEND
    │
    ▼
StompController
    │
    ├── 1. 세션 검증 (validateSessionAndMembership)
    │       ├── SimpMessageHeaderAccessor에서 HttpSession 추출
    │       │   (HttpSessionHandshakeInterceptor가 핸드셰이크 시 복사해둔 세션)
    │       ├── session에서 loginSess(MemberVO) 조회
    │       │   → null이면 거부 (비로그인)
    │       └── chatroomService.isMemberOfChatroom(roomSeq, memberSeq)
    │           → false이면 거부 (채팅방 비참여자)
    │
    ├── 2. 메시지 검증
    │       ├── trade_seq > 0 확인
    │       └── chat_cont 길이 ≤ 1000자 확인
    │
    ├── 3. 안전결제 검증 (chat_cont == "[SAFE_PAYMENT_REQUEST]"인 경우)
    │       ├── 판매자 본인인지 확인
    │       ├── sale_st == SALE 확인
    │       ├── safe_payment_st == NONE 확인
    │       └── requestSafePayment() 성공 여부 (동시성 제어)
    │           → 실패 시 publishError()로 "[SAFE_PAYMENT_IN_PROGRESS]" 전송
    │
    └── 검증 통과 → DB 저장 → Redis Pub/Sub 발행
```

---

## 관련 파일 위치

```
config/
└── StompConfig.java                      ← STOMP 설정 (endpoint, broker, prefix)
└── redis/RedisConfig.java                ← Pub/Sub Bean (ChannelTopic, Listener, Container)

chat/
├── StompController.java                  ← @MessageMapping (메시지 수신 처리)
├── pubsub/
│   ├── ChatMessage.java                  ← Pub/Sub 메시지 DTO (type, payload)
│   ├── ChatMessagePublisher.java         ← Redis PUBLISH (발행)
│   └── ChatMessageSubscriber.java        ← Redis SUBSCRIBE → convertAndSend (수신 → 브로커 전달)
├── chatroom/
│   ├── ChatroomController.java           ← HTTP 엔드포인트 (채팅방 목록, 메시지 조회)
│   └── ChatroomService.java              ← 채팅방 비즈니스 로직
└── message/
    └── MessageService.java               ← 메시지 DB 저장/조회/읽음처리

payment/
└── PaymentController.java                ← 결제 성공/실패 시 publishPayment() 호출

webapp/resources/js/chat/
└── chat.js                               ← 클라이언트 STOMP 연결/구독/전송
```
