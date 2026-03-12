# SecondaryBook - 중고책 거래 및 독서 모임 플랫폼

## 프로젝트 개요

중고책 판매/구매와 독서 모임을 결합한 온라인 플랫폼입니다.
안전결제, 실시간 채팅, 독서 모임 커뮤니티, 정산 자동화 기능을 제공합니다.

### 모듈 구성

| 모듈 | 디렉토리 | 역할 | 패키징 | 배포 서버 |
|------|---------|------|--------|----------|
| **웹 서버** | `mvc/` | 사용자 요청 처리 (API, 화면, 채팅) | WAR | AWS EC2 × N대 (Auto Scaling) |
| **배치 서버** | `batch/` | 정산 자동화, 스케줄러 | JAR | AWS EC2 전용 1대 |

> **왜 분리했나?**
> 웹 서버는 ALB + Auto Scaling으로 여러 대가 동시에 뜨므로, 배치 Job이 중복 실행될 수 있습니다.
> 배치는 전용 EC2 1대에서만 실행해 이 문제를 구조적으로 차단합니다.

---

## 주요 기능

### 1. 회원 관리
- **소셜 로그인** - 카카오, 네이버 OAuth 2.0 (WebClient 사용)
- **회원가입/로그인** - 일반 이메일 로그인
- **비밀번호 찾기** - Gmail SMTP 이메일 인증
- **회원 정보 수정/탈퇴**

### 2. 중고책 거래
- **책 검색** - 카카오 책 검색 API 연동
- **판매글 CRUD** - 등록, 조회, 수정, 삭제
- **이미지 업로드** - S3 + CloudFront 다중 업로드
- **찜하기** - 관심 상품 저장
- **카테고리별 필터링**
- **판매 상태 관리** - 판매중 / 판매완료

### 3. 안전 결제
- **Toss Payments** 연동 (카드, 간편결제)
- **5분 타임아웃** - 미결제 시 자동 취소 (배치 서버 스케줄러)
- **15일 자동 확정** - 구매 후 15일 경과 시 자동 구매 확정 (배치 서버 스케줄러)
- **수정/삭제 제한** - 결제 진행 중(PENDING) 또는 완료(COMPLETED) 시 판매글 수정/삭제 불가

### 4. 실시간 채팅
- **1:1 채팅** - 구매자-판매자 간 실시간 대화
- **WebSocket + STOMP** 기반 실시간 통신
- **Redis Pub/Sub** - 메시지 브로커로 다중 서버 지원
- **읽음/안읽음 표시**
- **이미지 전송** - S3 업로드 후 채팅 내 이미지 공유
- **안전결제 요청** - 채팅 내에서 결제 요청 발송

### 5. 독서 모임
- **모임 개설** - 모임명, 설명, 모집 인원, 배너 이미지, 정기 모임 장소
- **가입 신청** - 가입 사유 작성 후 모임장 승인 대기
- **모임 게시판** - 글 작성, 이미지 첨부, 댓글, 좋아요
- **멤버 관리** - 모임장이 멤버 강퇴, 권한 위임 (자동 승계)
- **찜하기** - 관심 모임 저장
- **모임 종료** - 모임장이 모임 종료 시 게시글/이미지 일괄 삭제

### 6. 마이페이지
- **판매 내역** - 판매중/판매완료 필터링, 정산 상태 확인 및 신청 버튼
- **구매 내역** - 안전결제 구매 목록, 구매 확정 버튼
- **찜 목록** - 찜한 상품/모임
- **배송 주소 관리** - 배송지 등록/수정/삭제
- **나의 모임** - 가입한 독서모임 목록
- **정산 계좌 관리** - 판매 대금 수령 계좌 등록/수정 (계좌번호 AES-256-CBC 암호화 저장)

### 7. 정산
- **정산 신청** - 구매확정이 완료된 거래에 한해 판매자가 마이페이지에서 직접 신청 (계좌 미등록 시 신청 차단)
- **수수료 1%** - (판매가 + 배송비) × 1% 수수료 차감 후 판매자에게 지급, BigDecimal FLOOR 처리
- **Spring Batch 자동 처리** - 매일 새벽 3시 정산 신청된(REQUESTED) 건을 일괄 자동 처리 (chunk=1로 건별 독립 트랜잭션)
- **잔액 부족 처리** - 관리자 잔액 부족 시 해당 건만 INSUFFICIENT_BALANCE로 skip 처리하고 나머지 건은 계속 진행, 판매자에게 이메일 알림 발송
- **관리자 잔액 충전** - 관리자 페이지에서 Toss 결제로 운영 잔액 직접 충전 (DB 처리 실패 시 자동 환불)
- **수동 이체 + 확인** - 배치 처리(COMPLETED) 후 관리자가 은행 앱으로 직접 이체 → "이체 완료 확인" 클릭으로 최종 처리 종료
- **재처리** - 잔액 부족 건을 관리자가 충전 후 REQUESTED 상태로 되돌려 다음 배치에서 자동 재시도

### 8. 관리자
- **대시보드** - 회원 수, 거래 수, 모임 수 통계
- **회원/상품/모임 관리** - 목록, 검색, 상세 조회, 회원 제재(BAN)
- **안전결제 내역** - 전체 결제 내역 조회
- **정산 관리** - 이체 대기 목록 (계좌번호 복호화 표시), 잔액 부족 목록, 관리자 잔액 현황
- **관리자 잔액 충전** - Toss 결제 연동 (충전 → 승인 → DB 반영, 실패 시 자동 환불)
- **공지사항/배너 관리** - CRUD

---

## 기술 스택

### 웹 서버 (mvc/)

| 구분 | 기술 |
|------|------|
| **Backend** | Spring MVC 5.3.39, MyBatis 3.5.19 |
| **Security** | Spring Security 5.7.12 (CSRF), 커스텀 인터셉터 |
| **Validation** | Spring Bean Validation (Hibernate Validator 6.2.5) |
| **암호화** | AES-256-CBC (계좌번호 암호화) |
| **Database** | MySQL (AWS RDS) |
| **Connection Pool** | HikariCP 4.0.3 |
| **Cache/Broker** | Redis (Lettuce) - ElastiCache |
| **Real-time** | WebSocket, STOMP, Redis Pub/Sub |
| **Storage** | AWS S3 + CloudFront CDN |
| **Payment** | Toss Payments API |
| **OAuth** | 카카오, 네이버 |
| **External API** | 카카오 책 검색 API |
| **Email** | Gmail SMTP (JavaMailSender) |
| **Build Tool** | Maven 3.6+ |
| **Java Version** | Java 17 |
| **Web Server** | Tomcat 9 (Cargo Plugin), WAR 패키징 |

### 배치 서버 (batch/)

| 구분 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.3.4, Spring Batch 5.x |
| **ORM** | MyBatis 3.0.3 (mybatis-spring-boot-starter) |
| **Reader** | MyBatisCursorItemReader (DB 커서 스트리밍) |
| **Database** | MySQL 같은 RDS 서버, 별도 DB (`secondHandBook_batch`) |
| **암호화** | AES-256-CBC (계좌번호 복호화) |
| **Email** | Gmail SMTP (JavaMailSender, jakarta.mail) |
| **Build Tool** | Maven |
| **Java Version** | Java 17 |
| **Packaging** | JAR (내장 서버 없음, 배치 전용) |

---

## 정산 플로우

> 이 플랫폼의 정산은 **구매자 결제 → 구매 확정 → 판매자 신청 → 배치 자동 처리 → 관리자 수동 이체** 순으로 진행됩니다.
> 배치가 실제 은행 이체를 직접 수행하지는 않으며, DB 상태 처리 + 잔액 차감까지만 담당하고 이체는 관리자가 수동으로 진행합니다.

### 전체 비즈니스 플로우

```
[1단계 - 구매자]
  구매자가 Toss 안전결제로 결제 완료 (safe_payment_st = COMPLETED)
  └─ 거래 완료 후 마이페이지에서 "구매 확정" 버튼 클릭 (confirm_purchase = true)
       (※ 15일 경과 시 자동 구매 확정 — 배치 서버 스케줄러)

[2단계 - 판매자]
  판매자가 마이페이지 > 판매 내역에서 "정산 신청" 버튼 클릭
  └─ 웹 서버(mvc) 검증
       ├─ 본인(판매자) 여부 확인
       ├─ 안전결제 완료(COMPLETED) 여부 확인
       ├─ 구매 확정 여부 확인
       ├─ 이미 신청된 건인지 확인 (settlement_st = READY 인지)
       └─ 정산 계좌 등록 여부 확인 (미등록 시 차단)
  └─ 검증 통과 시
       ├─ 수수료 계산: (판매가 + 배송비) × 1% → FLOOR 처리
       ├─ settlement 테이블에 정산 내역 INSERT
       └─ trade.settlement_st → REQUESTED (배치 처리 대기)

[3단계 - Spring Batch, 매일 새벽 3시 자동 실행 — 배치 서버(batch)]
  REQUESTED 상태인 정산 건 전체 조회
  └─ 각 건을 1건씩 독립 트랜잭션으로 처리
       ├─ [잔액 부족] → 해당 건 INSUFFICIENT_BALANCE 처리 후 skip → 판매자 이메일 알림 → 다음 건 계속 처리
       └─ [잔액 충분] → settlement_st = COMPLETED, 관리자 잔액 차감, 이체 로그 기록

[4단계 - 관리자]
  관리자 페이지 > 정산 관리 > 이체 대기 목록 확인
  └─ 계좌번호(복호화), 예금주, 은행 정보 확인
  └─ 은행 앱에서 직접 이체
  └─ "이체 완료 확인" 버튼 클릭 → transfer_confirmed_yn = 1 → 처리 완전 종료
```

### 정산 상태 흐름

```
READY
  └─ 판매자 정산 신청
       └─ REQUESTED  ──────────────────────────────────────────────────┐
            └─ 새벽 3시 배치                                               │
                 ├─ 잔액 충분  → COMPLETED → (관리자 수동 이체 → 이체 완료 확인)  │
                 └─ 잔액 부족  → INSUFFICIENT_BALANCE → 관리자 충전 후 재처리 ──┘
```

| 상태 | 의미 |
|------|------|
| `READY` | 구매 확정 완료, 정산 신청 전 상태 |
| `REQUESTED` | 판매자 정산 신청 완료, 배치 처리 대기 중 |
| `COMPLETED` | 배치 처리 완료, 관리자 수동 이체 대기 중 |
| `INSUFFICIENT_BALANCE` | 관리자 잔액 부족으로 배치에서 처리 보류됨 |

### Spring Batch 내부 파이프라인 (batch/ 서버)

`@Scheduled`는 새벽 3시에 배치를 **깨우는 알람 시계** 역할만 하고, 실제 처리는 **Spring Batch**가 담당합니다.

```
@Scheduled(cron = "0 0 3 * * *")   ← 트리거: 매일 새벽 3시에 배치 실행 명령
  └─ SettlementScheduler
       └─ JobLauncher.run(settlementJob)
            └─ processSettlementStep (chunk = 1, 1건 = 1 트랜잭션)
                 │
                 ├─ [Reader]      MyBatisCursorItemReader로 DB 커서를 열어 REQUESTED 건을 1건씩 스트리밍
                 │                 → 전체를 메모리에 올리지 않아 대용량 처리에 적합
                 │
                 ├─ [Processor]   암호화된 계좌번호를 AES-256-CBC로 복호화
                 │                 → Writer가 평문 계좌번호를 사용할 수 있도록 준비
                 │
                 └─ [Writer]      1건씩 독립 트랜잭션으로 아래 순서 처리
                       ①  SELECT balance FROM admin_account FOR UPDATE
                           └─ 관리자 계좌에 행 락 획득 (동시 처리 중복 차단)
                       ②  잔액 < 정산금액?
                           └─ YES → InsufficientBalanceException 발생
                                    → Spring Batch가 해당 건만 롤백 후 skip
                                    → SkipListener: INSUFFICIENT_BALANCE 상태 기록 + 판매자 이메일 발송
                                    → 다음 건으로 계속 진행
                       ③  UPDATE settlement SET settlement_st = 'COMPLETED'
                           WHERE settlement_st = 'REQUESTED'   ← 이미 처리된 건이면 0 row → 예외 발생 (멱등성)
                       ④  INSERT admin_account_log (계좌번호 마스킹 처리 후 이체 내역 기록)
                       ⑤  UPDATE trade.settlement_st = 'COMPLETED'
                       ⑥  UPDATE admin_account.balance = balance - 정산금액
```

**@Scheduled만으로는 부족한 이유**

| 상황 | @Scheduled 단독 구현 시 문제 | Spring Batch 사용 시 |
|------|------------------------------|----------------------|
| 10건 중 3번째 건 실패 | 1~2번째 커밋된 건도 전체 롤백 위험 | chunk=1로 1~2번째는 이미 커밋, 3번째만 롤백 후 skip |
| 관리자 잔액 부족 건 발생 | try-catch 직접 구현, 상태 관리 복잡 | `skip(InsufficientBalanceException)` + `SkipListener`로 자동 처리 |
| 같은 건이 두 번 처리되는 경우 | 수동으로 중복 체크 로직 구현 필요 | `WHERE settlement_st='REQUESTED'` 조건으로 멱등성 자동 보장 |
| 서버 2대 이상(Auto Scaling) 환경 | 두 서버 모두 새벽 3시에 실행되면 이중 처리 | 배치 서버를 전용 EC2 1대로 분리해 구조적으로 차단 |
| 배치 실행 이력 확인 | 별도 로깅 직접 구현 필요 | `BATCH_JOB_EXECUTION` 테이블에 처리 건수, 상태, 시간 자동 기록 |

### 관리자 잔액 충전 플로우 (잔액 부족 발생 시)

```
관리자 페이지 (/admin/balance/charge)
  └─ 현재 잔액 확인 후 Toss 결제 창 열기
       └─ 결제 완료 → /admin/balance/success 콜백
            ├─ TossApiService.confirmPayment()로 토스 결제 최종 승인
            ├─ [성공] admin_account.balance 증가 + 감사 로그(admin_account_log) 기록
            └─ [DB 처리 실패] TossApiService.cancelPayment()로 자동 환불 처리

충전 완료 후
  └─ 관리자가 INSUFFICIENT_BALANCE 목록에서 "재처리" 클릭
       └─ settlement_st → REQUESTED 복원
            └─ 다음 날 새벽 3시 배치에서 자동 재시도
```

---

## AWS 인프라

### 아키텍처

```
사용자 → Route 53 → ALB → EC2 웹 서버 (Auto Scaling, N대)
                              ↓
                         RDS (MySQL)  ←──── EC2 배치 서버 (전용 1대)
                              ↓
                         ElastiCache (Redis)

이미지 → S3 → CloudFront (CDN)
```

### 구성 요소

| 서비스 | 스펙 | 용도 |
|--------|------|------|
| **EC2 (웹)** | t3.small (2GB RAM) | Spring MVC 웹 서버 (WAR) |
| **EC2 (배치)** | t3.small | Spring Boot 배치 서버 (JAR) |
| **ALB** | Application Load Balancer | 웹 서버 로드 밸런싱 |
| **Auto Scaling** | 최소 1대, 최대 8대 | 웹 서버 자동 확장 |
| **RDS** | MySQL 8.0 | 메인 DB (`secondHandBook`) + 배치 메타 DB (`secondHandBook_batch`) |
| **ElastiCache** | Redis | 세션 + 캐시 + 메시지 브로커 |
| **S3** | Standard | 이미지 저장 |
| **CloudFront** | CDN | 이미지 캐싱 |

> **DB 분리 이유:** Spring Batch 메타데이터 테이블(`BATCH_JOB_EXECUTION` 등)은 같은 RDS 서버 내 별도 DB(`secondHandBook_batch`)에 생성합니다. 메인 서비스 DB와 스키마를 격리해 배치 메타 테이블이 비즈니스 테이블과 섞이지 않습니다.

### Auto Scaling 정책 (웹 서버)

| 항목 | 설정 |
|------|------|
| 트리거 | CPU 사용률 **50%** 초과 |
| 최소 인스턴스 | 1대 |
| 최대 인스턴스 | 8대 |
| 쿨다운 | 60초 |

---

## 프로젝트 구조

```
SecondaryBook/
├── mvc/                               # 웹 서버 (Spring MVC, WAR)
│   └── src/main/java/project/
│       ├── config/                    # Spring 설정
│       │   ├── AppConfig.java
│       │   ├── MvcConfig.java
│       │   ├── SecurityConfig.java
│       │   ├── StompConfig.java
│       │   ├── S3Config.java
│       │   ├── SchedulerConfig.java   # @EnableScheduling (LogoutPendingScheduler용)
│       │   ├── WebClientConfig.java
│       │   ├── InterceptorConfig.java
│       │   └── redis/
│       │       ├── RedisConfig.java
│       │       └── RedisCacheConfig.java
│       │
│       ├── member/                    # 회원 관리
│       │   ├── MemberController.java
│       │   ├── MemberService.java
│       │   ├── MemberMapper.java
│       │   ├── MemberVO.java
│       │   ├── MailService.java       # Gmail SMTP (회원가입 인증)
│       │   ├── MypageController.java
│       │   ├── MemberBankAccountMapper.java
│       │   ├── MemberBankAccountService.java  # AES 암/복호화
│       │   ├── MemberBankAccountVO.java
│       │   └── ENUM/MemberStatus.java
│       │
│       ├── trade/                     # 중고책 거래
│       │   ├── TradeController.java
│       │   ├── TradeService.java
│       │   ├── TradeMapper.java
│       │   ├── TradeVO.java
│       │   ├── TradeImageVO.java
│       │   ├── BookImgMapper.java
│       │   └── ENUM/
│       │       ├── SaleStatus.java
│       │       ├── BookStatus.java
│       │       └── PaymentType.java
│       │
│       ├── payment/                   # 안전 결제
│       │   ├── PaymentController.java
│       │   ├── PaymentService.java
│       │   ├── TossApiService.java
│       │   ├── TossPaymentResponse.java
│       │   └── PaymentVO.java
│       │
│       ├── settlement/                # 정산 신청 (웹 서버 담당)
│       │   ├── SettlementController.java
│       │   ├── SettlementService.java
│       │   ├── SettlementMapper.java
│       │   └── SettlementVO.java
│       │
│       ├── chat/                      # 실시간 채팅
│       │   ├── StompController.java
│       │   ├── chatroom/
│       │   └── message/
│       │
│       ├── bookclub/                  # 독서 모임
│       │
│       ├── admin/                     # 관리자
│       │
│       ├── address/                   # 배송 주소 관리
│       │
│       └── util/                      # 유틸리티
│           ├── S3Service.java
│           ├── AesEncryptionUtil.java
│           ├── logout/
│           │   └── LogoutPendingScheduler.java  # 로그아웃 처리 스케줄러 (웹 서버 담당)
│           └── ...
│
├── batch/                             # 배치 서버 (Spring Boot 3, JAR)
│   └── src/main/java/spring/batch/
│       ├── BatchApplication.java
│       ├── config/
│       │   └── SchedulerConfig.java   # @EnableScheduling
│       │
│       ├── job/settlement/            # Spring Batch Job
│       │   ├── BatchConfig.java           # Job / Step / Reader 설정
│       │   ├── SettlementItemProcessor.java  # 계좌번호 복호화
│       │   ├── SettlementItemWriter.java     # 잔액 차감 + 상태 변경
│       │   ├── SettlementSkipListener.java   # 잔액 부족 → 이메일 알림
│       │   └── InsufficientBalanceException.java
│       │
│       ├── scheduler/
│       │   ├── SettlementScheduler.java    # 매일 03:00 정산 배치 실행
│       │   └── SafePaymentScheduler.java   # 안전결제 타임아웃/자동확정
│       │
│       ├── settlement/
│       │   ├── BatchSettlementService.java  # markAsInsufficient() 전담
│       │   ├── SettlementMapper.java
│       │   ├── SettlementVO.java
│       │   └── SettlementStatus.java
│       │
│       ├── member/
│       │   ├── MemberMapper.java       # findByMemberSeq() 만 포함
│       │   ├── MemberVO.java
│       │   └── BatchMailService.java   # 잔액부족 알림 이메일 발송
│       │
│       ├── trade/
│       │   └── TradeMapper.java        # 안전결제 타임아웃/자동확정 쿼리
│       │
│       └── util/
│           └── AesEncryptionUtil.java
│
│   └── src/main/resources/
│       ├── application.properties
│       └── mappers/
│           ├── settlement/settlementMapper.xml
│           ├── member/memberMapper.xml
│           └── trade/tradeMapper.xml
│
└── README.md
```

---

## 기술적 의사결정

### Spring MVC (Spring Boot 미사용 — 웹 서버)

Spring Boot는 자동 설정(Auto Configuration)으로 빠르게 시작할 수 있지만, 무엇이 어떻게 등록되는지 파악하기 어렵다. 이 프로젝트에서는 Spring Boot를 사용하지 않고 `web.xml`, `root-context.xml`, `MvcConfig.java`를 직접 작성해 **필터 체인 순서, 컨텍스트 계층 구조, 빈 등록 과정**을 명시적으로 제어했다.

### Spring Boot (배치 서버)

배치 서버는 Spring Boot 3.3.4를 사용한다. Spring Batch 5.x는 Spring Boot 3.x 생태계를 전제로 설계되었고, 별도 Tomcat이 필요 없는 JAR 실행 방식이 배치 전용 서버에 더 적합하다.

### MyBatis (JPA 미사용)

중고책 거래 플랫폼 특성상 다음과 같은 쿼리가 많다.
- 다중 테이블 JOIN (거래 + 이미지 + 카테고리 + 회원)
- 동적 검색 조건 (카테고리, 키워드, 정렬, 판매 상태 필터)
- `SELECT ... FOR UPDATE` (안전결제 동시성 제어, 정산 배치)

JPA로 이런 쿼리를 작성하면 JPQL 또는 Native Query로 우회해야 하고, 실행되는 SQL을 직접 제어하기 어렵다. MyBatis XML 매퍼를 사용하면 SQL을 직접 작성하므로 쿼리 최적화와 실행 계획을 명확히 파악할 수 있다. 배치 서버도 동일한 이유로 MyBatis를 선택했다.

### MyBatisCursorItemReader (대용량 배치 처리)

기존 ListItemReader는 REQUESTED 상태 전체를 메모리에 올린 뒤 처리한다. 정산 건수가 많아지면 OutOfMemoryError 위험이 있다.
`MyBatisCursorItemReader`는 DB 커서를 열어 1건씩 스트리밍하므로 메모리 사용량이 일정하게 유지된다.

```
ListItemReader:            [건1, 건2, ..., 건N] → 전부 메모리에 올림
MyBatisCursorItemReader:   DB 커서 열기 → 건1 → 건2 → ... → 건N (1건씩 스트리밍)
```

### chunk=1 선택 이유

정산 배치는 건당 `SELECT balance FOR UPDATE` → 잔액 차감 → `INSERT 로그` → 상태 변경 순으로 처리된다.

`chunk > 1`로 설정하면 여러 건이 하나의 트랜잭션에 묶이므로:
- `FOR UPDATE` 락이 chunk 전체 처리 시간 동안 유지되어 다른 트랜잭션을 오래 대기시킨다.
- 한 건 실패 시 chunk 전체가 롤백된다.

`chunk=1`을 선택한 이유:
- 정산 1건 = 독립 트랜잭션. 잔액 부족으로 한 건을 skip해도 나머지 건은 정상 처리된다.
- `FOR UPDATE` 락이 1건 처리 후 즉시 해제되어 병목을 최소화한다.
- 잔액 변경이 매 커밋마다 반영되어 다음 건이 항상 최신 잔액을 기준으로 처리된다.

### 안전결제 동시성 — `UPDATE WHERE safe_payment_st = 'NONE'`

별도 `SELECT FOR UPDATE` 없이 `UPDATE ... WHERE safe_payment_st = 'NONE'` 한 쿼리로 동시 요청 문제를 방어한다. MySQL InnoDB는 `UPDATE`가 해당 행에 암묵적 배타 락을 걸어 직렬로 처리하므로, 두 요청이 동시에 들어와도 한 건만 1 row affected가 된다.

### 트랜잭션 커밋 후 외부 작업 (`TransactionSynchronizationManager`)

S3 이미지 삭제와 Redis Pub/Sub 메시지 발행은 트랜잭션 롤백으로 되돌릴 수 없는 외부 시스템 작업이다. `afterCommit()`을 사용하면 DB가 정상 커밋된 경우에만 외부 작업을 실행해 일관성을 보장한다.

```java
// DB 롤백 시 메시지는 취소 불가 → 커밋 확정 후에 발행해야 함
TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override
    public void afterCommit() {
        chatMessagePublisher.publish(msg);  // DB 커밋 확정 후 발행
    }
});
```

### AES-256-CBC — IV를 매 암호화마다 새로 생성

계좌번호를 DB에 암호화 저장할 때 고정 IV를 사용하면 같은 계좌번호가 항상 같은 암호문이 된다(패턴 노출). `SecureRandom`으로 매 암호화마다 16바이트 IV를 새로 생성하면 같은 계좌번호도 매번 다른 암호문이 생성된다. IV는 비밀이 아니므로 `Base64(IV):Base64(암호문)` 형식으로 함께 저장하고 복호화 시 분리해 사용한다.

### Redis를 세션·캐시·Pub/Sub 세 가지 용도로 통합 사용

단일 Redis 인스턴스(ElastiCache)로 세 가지 역할을 처리한다.

| 용도 | 한 줄 요약 |
|------|-----------|
| **Session** | 어떤 EC2로 요청이 라우팅되든 동일한 로그인 세션 유지 |
| **Cache** | 자주 조회되는 목록/상세 데이터를 캐싱하여 DB 부하 감소 |
| **Pub/Sub** | 채팅 메시지를 다중 서버 전체에 브로드캐스트 |
| **인증코드** | TTL 기반 자동 만료로 이메일 인증코드 임시 저장 |

---

## Redis 활용

### Pub/Sub — 트러블슈팅: 멀티 서버 환경에서 채팅 메시지 수신 불가

#### 문제 상황

k6 부하 테스트 중 Auto Scaling이 작동하여 EC2가 1대에서 2대로 증가했을 때, 1:1 채팅에서 **상대방의 메시지가 실시간으로 수신되지 않는 현상**이 발생했다.

- 판매자(유저A)가 메시지를 전송하면 구매자(유저B)에게 도착하지 않음
- 새로고침하면 메시지가 보임 (DB에는 정상 저장됨)
- EC2 1대일 때는 정상 동작, **2대 이상에서만 발생**

#### 원인

WebSocket(STOMP) 세션은 **서버 메모리에 바인딩**된다. ALB가 유저A를 EC2 #1로, 유저B를 EC2 #2로 라우팅하면, `SimpMessagingTemplate.convertAndSend()`는 **해당 서버의 WebSocket 세션에만** 메시지를 전달한다.

#### 해결: Redis Pub/Sub 도입

모든 EC2 인스턴스가 동일한 Redis 채널(`chat:messages`)을 구독하도록 변경했다. 어떤 서버에서 발행하든 전체 인스턴스가 수신하여 자기에게 연결된 클라이언트에게 전달한다.

```
유저A → EC2 #1 → DB 저장 → Redis PUBLISH ("chat:messages")
                                    ↓
                    EC2 #1 SUBSCRIBE → EC2 #1의 WebSocket 클라이언트에게 전달
                    EC2 #2 SUBSCRIBE → 유저B 실시간 수신 ✅
```

| Publisher 메서드 | 실시간 전달 대상 |
|-----------------|----------------|
| `publishChat()` | 채팅 메시지 |
| `publishRead()` | 읽음 처리 이벤트 |
| `publishPayment()` | 안전결제 요청/완료/실패 알림 |
| `publishError()` | 에러 메시지 |

### Cache

| 캐시 이름 | TTL | 대상 |
|-----------|-----|------|
| `tradeList` | 5분 | 중고거래 목록 |
| `trade` | 10분 | 중고거래 상세 |
| `tradeCount` | 10분 | 중고거래 개수 |
| `bookClubList` | 10분 | 독서모임 목록 |
| `bookClub` | 10분 | 독서모임 상세 |

---

## 외부 API

| API | 용도 |
|-----|------|
| **Toss Payments** | 결제 승인/취소 |
| **카카오 책 검색** | 도서 정보 조회 |
| **카카오 OAuth** | 소셜 로그인 |
| **네이버 OAuth** | 소셜 로그인 |
| **Gmail SMTP** | 이메일 발송 (회원가입 인증, 정산 알림) |
| **AWS S3** | 이미지 저장 |
| **AWS CloudFront** | 이미지 CDN |

### WebClient (외부 API 호출)

Spring MVC 기반이지만 외부 API 호출 시 **WebClient**를 사용한다. `.block()`으로 동기 대기하므로 `RestTemplate`의 대안으로 사용하는 구조다. WebFlux 서버로 동작하지 않는다.

| Bean | Base URL | 용도 |
|------|----------|------|
| `tossPaymentWebClient` | Toss API | 결제 승인 (Basic Auth) |
| `kakaoAuthWebClient` | `kauth.kakao.com` | 카카오 OAuth 토큰 |
| `kakaoApiWebClient` | `kapi.kakao.com` | 카카오 사용자 정보 |
| `naverAuthWebClient` | `nid.naver.com` | 네이버 OAuth 토큰 |
| `naverApiWebClient` | `openapi.naver.com` | 네이버 사용자 정보 |
| `kakaoBookWebClient` | 카카오 도서 검색 API | 책 검색 |

---

## 예외 계층화

```
RuntimeException
├── ClientException (4xx — 400.jsp)
│   ├── NotFoundException (404)
│   │   ├── trade/TradeNotFoundException
│   │   └── bookclub/BookClubNotFoundException
│   ├── ForbiddenException (403)
│   └── InvalidRequestException (400)
│       ├── bookclub/BookClubInvalidRequestException
│       ├── file/FileUploadException
│       └── settlement/SettlementException
└── ServerException (5xx — 500.jsp)
```

`GlobalExceptionHandler`에서 `ClientException`(4xx)과 `ServerException`(5xx) 두 개로 단순 처리하고, 에러 페이지는 `400.jsp`와 `500.jsp` 두 개만 사용한다.

---

## S3 & CloudFront

| 항목 | 값 |
|------|-----|
| **S3 버킷** | secondarybooksimages |
| **CloudFront 도메인** | d3p8m254izebr5.cloudfront.net |
| **이미지 경로** | images/{UUID}.{확장자} |

파일 삭제 API는 4단계 검증으로 임의 URL 주입 및 Path Traversal 공격을 방어한다.

| 단계 | 검증 내용 |
|------|----------|
| 1 | host가 지정 버킷/CloudFront 도메인인지 확인 |
| 2 | path가 비어있지 않은지 확인 |
| 3 | key가 `images/` prefix로 시작하는지 확인 |
| 4 | `..` 포함 여부 확인 |
