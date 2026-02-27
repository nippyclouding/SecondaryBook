# SecondaryBook - 중고책 거래 및 독서 모임 플랫폼

## 프로젝트 개요
중고책 판매/구매와 독서 모임을 결합한 온라인 플랫폼입니다.
안전결제, 실시간 채팅, 독서 모임 커뮤니티 기능을 제공합니다.

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
- **5분 타임아웃** - 미결제 시 자동 취소
- **15일 자동 확정** - 구매 후 15일 경과 시 자동 구매 확정
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
- **정산 신청** - 구매확정 완료된 거래에서 판매자가 마이페이지에서 직접 신청 (계좌 미등록 시 차단)
- **수수료 1%** - (판매가 + 배송비) × 1% 수수료 차감, BigDecimal FLOOR 처리
- **Spring Batch 자동 처리** - 매일 새벽 3시 REQUESTED 건 일괄 처리 (chunk=1, SELECT FOR UPDATE)
- **잔액 부족 처리** - 관리자 잔액 부족 시 해당 건만 INSUFFICIENT_BALANCE 처리 (SkipListener), 나머지 건 계속 처리
- **관리자 잔액 충전** - 어드민 페이지에서 Toss 결제로 관리자 운영 잔액 직접 충전
- **이체 완료 확인** - 배치 처리 후 관리자가 은행 앱으로 수동 이체 → "이체 완료" 클릭으로 처리 종료
- **재처리** - 잔액 부족 건을 REQUESTED 상태로 되돌려 다음 배치에서 재시도

### 8. 관리자
- **대시보드** - 회원 수, 거래 수, 모임 수 통계
- **회원/상품/모임 관리** - 목록, 검색, 상세 조회, 회원 제재(BAN)
- **안전결제 내역** - 전체 결제 내역 조회
- **정산 관리** - 이체 대기 목록 (계좌번호 복호화 표시), 잔액 부족 목록, 관리자 잔액 현황
- **관리자 잔액 충전** - Toss 결제 연동 (충전 → 승인 → DB 반영, 실패 시 자동 환불)
- **공지사항/배너 관리** - CRUD

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Spring MVC 5.3.39, MyBatis 3.5.19 |
| **Batch** | Spring Batch 4.3.9 |
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
| **Web Server** | Tomcat 9 (Cargo Plugin) |

---

## 기술적 의사결정

핵심 기술을 선택하거나 특정 방식을 구현할 때 내린 의사결정과 그 이유를 정리합니다.

### Spring MVC (Spring Boot 미사용)

Spring Boot는 자동 설정(Auto Configuration)으로 빠르게 시작할 수 있지만, 무엇이 어떻게 등록되는지 파악하기 어렵다. 이 프로젝트에서는 Spring Boot를 사용하지 않고 `web.xml`, `root-context.xml`, `MvcConfig.java`를 직접 작성해 **필터 체인 순서, 컨텍스트 계층 구조, 빈 등록 과정**을 명시적으로 제어했다. 자동 설정의 편의보다 내부 동작을 이해하고 직접 구성하는 경험을 우선했다.

### MyBatis (JPA 미사용)

중고책 거래 플랫폼 특성상 다음과 같은 쿼리가 많다.
- 다중 테이블 JOIN (거래 + 이미지 + 카테고리 + 회원)
- 동적 검색 조건 (카테고리, 키워드, 정렬, 판매 상태 필터)
- `SELECT ... FOR UPDATE` (안전결제 동시성 제어, 정산 배치)

JPA로 이런 쿼리를 작성하면 JPQL 또는 Native Query로 우회해야 하고, 실행되는 SQL을 직접 제어하기 어렵다. MyBatis XML 매퍼를 사용하면 SQL을 직접 작성하므로 쿼리 최적화와 실행 계획을 명확히 파악할 수 있다.

### Spring Batch — chunk=1 선택 이유

정산 배치는 건당 `SELECT balance FOR UPDATE` → 잔액 차감 → `INSERT 로그` → 상태 변경 순으로 처리된다.

`chunk > 1`로 설정하면 여러 건이 하나의 트랜잭션에 묶이므로:
- `FOR UPDATE` 락이 chunk 전체 처리 시간 동안 유지되어 다른 트랜잭션을 오래 대기시킨다.
- 한 건 실패 시 chunk 전체가 롤백된다.

`chunk=1`을 선택한 이유:
- 정산 1건 = 독립 트랜잭션. 잔액 부족으로 한 건을 skip해도 나머지 건은 정상 처리된다.
- `FOR UPDATE` 락이 1건 처리 후 즉시 해제되어 병목을 최소화한다.
- 잔액 변경이 매 커밋마다 반영되어 다음 건이 항상 최신 잔액을 기준으로 처리된다.

### 안전결제 동시성 — `UPDATE WHERE safe_payment_st = 'NONE'`

별도 `SELECT FOR UPDATE` 없이 `UPDATE ... WHERE safe_payment_st = 'NONE'` 한 쿼리로 동시 요청 문제를 방어한다. MySQL InnoDB는 `UPDATE`가 해당 행에 암묵적 배타 락을 걸어 직렬로 처리하므로, 두 요청이 동시에 들어와도 한 건만 1 row affected가 된다. 애플리케이션 레벨 락 없이 DB 고유 특성으로 동시성을 처리한 구조다.

### 트랜잭션 커밋 후 외부 작업 (`TransactionSynchronizationManager`)

S3 이미지 삭제와 Redis Pub/Sub 메시지 발행은 트랜잭션 롤백으로 되돌릴 수 없는 외부 시스템 작업이다. 트랜잭션 내에서 바로 실행하면 이후 DB 처리가 실패해 롤백되더라도 외부 작업은 이미 완료된 상태로 남아 불일치가 생긴다.

```
// 잘못된 방식: 트랜잭션 내에서 바로 발행하면 DB 롤백 후에도 메시지는 이미 전송됨
messageService.save(msg);
chatMessagePublisher.publish(msg);  // DB 롤백 시 메시지는 취소 불가

// 올바른 방식: 커밋이 확정된 직후에만 실행
TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override
    public void afterCommit() {
        chatMessagePublisher.publish(msg);  // DB 커밋 확정 후 발행
    }
});
```

`afterCommit()`을 사용하면 DB가 정상 커밋된 경우에만 외부 작업을 실행해 일관성을 보장한다.

### AES-256-CBC — IV를 매 암호화마다 새로 생성

계좌번호를 DB에 암호화 저장할 때 고정 IV를 사용하면 같은 계좌번호가 항상 같은 암호문이 된다. 이 경우 공격자가 암호문만 비교해 동일 계좌 여부를 파악할 수 있다(패턴 노출). `SecureRandom`으로 매 암호화마다 16바이트 IV를 새로 생성하면 같은 계좌번호도 매번 다른 암호문이 생성된다. IV는 비밀이 아니므로 `Base64(IV):Base64(암호문)` 형식으로 함께 저장하고 복호화 시 분리해 사용한다.

### Redis를 세션·캐시·Pub/Sub 세 가지 용도로 통합 사용

별도 인프라를 두지 않고 단일 Redis 인스턴스로 세 가지 역할을 처리한다.
- **세션**: Spring Session으로 Auto Scaling 환경의 세션 공유
- **캐시**: `@Cacheable`로 자주 조회되는 거래/독서모임 데이터 캐싱
- **Pub/Sub**: 채팅 메시지를 다중 서버 전체에 브로드캐스트

세 용도가 서로 다른 키 패턴(`secondarybook:session:*`, `trade::*`, `chat:messages`)을 사용하므로 충돌이 없다. 별도 MQ 서버(RabbitMQ 등) 없이 Redis 하나로 해결해 인프라 복잡도를 낮췄다.

### S3 파일 삭제 보안 검증 4단계

파일 삭제 API(`deleteByUrl`)는 URL을 받아 S3 객체를 삭제한다. 검증 없이 구현하면 임의 URL을 전달해 다른 버킷이나 경로의 파일을 삭제하는 공격이 가능하다. 4단계 검증으로 방어한다.

| 단계 | 검증 내용 | 방어 대상 |
|------|----------|----------|
| 1 | host가 지정 버킷/CloudFront 도메인인지 확인 | 외부 URL 주입 |
| 2 | path가 비어있지 않은지 확인 | 버킷 루트 접근 |
| 3 | key가 `images/` prefix로 시작하는지 확인 | 허용 경로 외 삭제 시도 |
| 4 | `..` 포함 여부 확인 | Path Traversal 공격 |

---

## AWS 인프라

### 아키텍처
```
사용자 → Route 53 → ALB → EC2 (Auto Scaling)
                         ↓
                    RDS (MySQL)
                         ↓
                    ElastiCache (Redis)

이미지 → S3 → CloudFront (CDN)
```

### 구성 요소

| 서비스 | 스펙 | 용도 |
|--------|------|------|
| **EC2** | t3.small (2GB RAM) | 웹 서버 |
| **ALB** | Application Load Balancer | 로드 밸런싱 |
| **Auto Scaling** | 최소 1대, 최대 8대 | 자동 확장 |
| **RDS** | MySQL 8.0 | 데이터베이스 |
| **ElastiCache** | Redis | 캐시 + 메시지 브로커 |
| **S3** | Standard | 이미지 저장 |
| **CloudFront** | CDN | 이미지 캐싱 |

### Auto Scaling 정책

| 항목 | 설정 |
|------|------|
| 트리거 | CPU 사용률 **50%** 초과 |
| 최소 인스턴스 | 1대 |
| 최대 인스턴스 | 8대 |
| 쿨다운 | 60초 |

---

## 프로젝트 구조
```
secondaryBook/
├── src/main/java/project/
│   ├── config/                    # Spring 설정
│   │   ├── AppConfig.java         # RestTemplate, ObjectMapper Bean
│   │   ├── MvcConfig.java         # View Resolver, 정적 리소스
│   │   ├── SecurityConfig.java    # Spring Security (CSRF)
│   │   ├── StompConfig.java       # WebSocket STOMP 설정
│   │   ├── S3Config.java          # AWS S3 Client
│   │   ├── SchedulerConfig.java   # @EnableScheduling
│   │   ├── WebClientConfig.java   # OAuth, Toss API WebClient
│   │   ├── InterceptorConfig.java
│   │   └── redis/                 # Redis 설정
│   │       ├── RedisConfig.java
│   │       └── RedisCacheConfig.java
│   │
│   ├── member/                    # 회원 관리
│   │   ├── MemberController.java  # OAuth, 회원가입/로그인
│   │   ├── MemberService.java
│   │   ├── MemberMapper.java
│   │   ├── MemberVO.java
│   │   ├── MailService.java       # Gmail SMTP
│   │   ├── MypageController.java  # 마이페이지
│   │   ├── MemberBankAccountMapper.java  # 정산 계좌 Mapper
│   │   ├── MemberBankAccountService.java # 정산 계좌 (AES 암/복호화)
│   │   ├── MemberBankAccountVO.java
│   │   └── ENUM/
│   │       └── MemberStatus.java
│   │
│   ├── trade/                     # 중고책 거래
│   │   ├── TradeController.java   # 판매글 CRUD
│   │   ├── TradeService.java
│   │   ├── TradeMapper.java
│   │   ├── TradeVO.java
│   │   ├── TradeImageVO.java
│   │   ├── BookImgMapper.java
│   │   └── ENUM/
│   │       ├── SaleStatus.java    # SALE, SOLD
│   │       ├── BookStatus.java
│   │       └── PaymentType.java
│   │
│   ├── payment/                   # 안전 결제
│   │   ├── PaymentController.java
│   │   ├── PaymentService.java
│   │   ├── TossApiService.java    # Toss Payments API
│   │   ├── TossPaymentResponse.java
│   │   ├── PaymentVO.java
│   │   └── SafePaymentScheduler.java  # 타임아웃/자동확정
│   │
│   ├── settlement/                # 정산
│   │   ├── SettlementController.java  # 정산 신청 + 관리자 API
│   │   ├── SettlementService.java
│   │   ├── SettlementMapper.java
│   │   ├── SettlementScheduler.java   # Spring Batch 실행 트리거 (매일 03:00)
│   │   └── SettlementVO.java
│   │
│   ├── batch/                     # Spring Batch 정산 배치
│   │   ├── BatchConfig.java           # Job / Step 설정
│   │   ├── SettlementItemReader.java  # REQUESTED 건 조회 (FOR UPDATE)
│   │   ├── SettlementItemProcessor.java # 유효성 검증
│   │   ├── SettlementItemWriter.java  # 잔액 차감 + 상태 COMPLETED 변경
│   │   ├── SettlementSkipListener.java # 잔액 부족 → INSUFFICIENT_BALANCE 처리
│   │   └── InsufficientBalanceException.java
│   │
│   ├── chat/                      # 실시간 채팅
│   │   ├── StompController.java   # WebSocket 메시지 핸들러
│   │   ├── chatroom/
│   │   │   ├── ChatroomController.java
│   │   │   ├── ChatroomService.java
│   │   │   ├── ChatroomMapper.java
│   │   │   └── ChatroomVO.java
│   │   └── message/
│   │       ├── MessageService.java
│   │       ├── MessageMapper.java
│   │       └── MessageVO.java
│   │
│   ├── bookclub/                  # 독서 모임
│   │   ├── BookClubController.java
│   │   ├── BookClubManageController.java
│   │   ├── BookClubService.java
│   │   ├── BookClubMapper.java
│   │   ├── dto/
│   │   │   ├── BookClubJoinRequestDTO.java
│   │   │   ├── BookClubPageResponseDTO.java
│   │   │   ├── BookClubManageMemberDTO.java
│   │   │   ├── BookClubManageViewDTO.java
│   │   │   └── BookClubUpdateSettingsDTO.java
│   │   ├── vo/
│   │   │   ├── BookClubVO.java
│   │   │   ├── BookClubBoardVO.java
│   │   │   ├── BookClubMemberVO.java
│   │   │   ├── BookClubRequestVO.java
│   │   │   └── BookClubWishVO.java
│   │   └── ENUM/
│   │       ├── JoinStatus.java    # WAIT, JOINED, REJECTED
│   │       ├── JoinRequestResult.java
│   │       └── RequestStatus.java
│   │
│   ├── admin/                     # 관리자
│   │   ├── AdminController.java
│   │   ├── AdminService.java
│   │   ├── AdminMapper.java
│   │   ├── AdminVO.java
│   │   ├── BannerVO.java
│   │   ├── LoginInfoVO.java
│   │   ├── NoticeController.java
│   │   ├── NoticeVO.java
│   │   └── TempPageVO.java
│   │
│   ├── address/                   # 배송 주소 관리
│   │   ├── AddressController.java
│   │   ├── AddressService.java
│   │   ├── AddressMapper.java
│   │   └── AddressVO.java
│   │
│   └── util/                      # 유틸리티
│       ├── S3Service.java         # S3 파일 업로드
│       ├── AesEncryptionUtil.java # AES-256-CBC 계좌번호 암호화
│       ├── HomeController.java
│       ├── HealthController.java  # 헬스체크
│       ├── Const.java
│       ├── LoginUtil.java
│       ├── book/
│       │   ├── BookApiService.java  # 카카오 책 검색 API
│       │   └── BookVO.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── ClientException.java          # 4xx 기본 클래스 (abstract)
│       │   ├── ServerException.java          # 5xx 기본 클래스
│       │   ├── NotFoundException.java        # 404
│       │   ├── ForbiddenException.java       # 403
│       │   ├── InvalidRequestException.java  # 400
│       │   ├── trade/
│       │   │   └── TradeNotFoundException.java
│       │   ├── bookclub/
│       │   │   ├── BookClubNotFoundException.java
│       │   │   └── BookClubInvalidRequestException.java
│       │   ├── file/
│       │   │   └── FileUploadException.java
│       │   └── settlement/
│       │       └── SettlementException.java
│       ├── imgUpload/
│       │   ├── FileStore.java
│       │   └── UploadFile.java
│       ├── interceptor/           # 인터셉터
│       │   ├── AdminAuthInterceptor.java
│       │   ├── LoginRequiredInterceptor.java
│       │   ├── MemberActivityInterceptor.java
│       │   └── UnreadInterceptor.java
│       ├── logout/                # 로그아웃 관리
│       │   ├── LogoutPendingController.java
│       │   ├── LogoutPendingManager.java
│       │   ├── InMemoryLogoutPendingManager.java
│       │   ├── LogoutPendingScheduler.java
│       │   └── UserType.java
│       └── paging/
│           ├── PageResult.java
│           └── SearchVO.java
│
├── src/main/resources/
│   ├── project/                   # MyBatis Mapper XML
│   │   ├── trade/
│   │   ├── chat/
│   │   ├── bookclub/
│   │   ├── member/                # MemberMapper.xml, MemberBankAccountMapper.xml
│   │   ├── settlement/            # settlementMapper.xml
│   │   ├── admin/
│   │   └── address/
│   ├── application.properties     # 환경 설정
│   ├── log4jdbc.log4j2.properties
│   └── logback.xml                # 로깅 설정
│
├── src/main/webapp/WEB-INF/
│   ├── web.xml
│   ├── spring/
│   │   ├── root-context.xml
│   │   └── appServlet/
│   │       └── servlet-context.xml
│   └── views/                     # JSP
│       ├── member/
│       │   ├── mypage.jsp
│       │   ├── signup.jsp
│       │   ├── login.jsp
│       │   ├── findAccount.jsp
│       │   └── tabs/              # 마이페이지 탭
│       │       ├── profile.jsp
│       │       ├── sales.jsp
│       │       ├── purchases.jsp
│       │       ├── wishlist.jsp
│       │       ├── addresses.jsp
│       │       ├── groups.jsp
│       │       └── bankaccount.jsp  # 정산 계좌 관리
│       ├── trade/
│       │   ├── tradelist.jsp
│       │   ├── tradedetail.jsp
│       │   ├── tradeform.jsp
│       │   ├── tradeupdate.jsp
│       │   └── include/
│       │       └── holo_card_modal.jsp
│       ├── chat/
│       │   └── chatrooms.jsp
│       ├── bookclub/
│       │   ├── bookclubs.jsp
│       │   ├── bookclub_list.jsp
│       │   ├── bookclub_create.jsp
│       │   ├── bookclub_detail.jsp
│       │   ├── bookclub_detail_home.jsp
│       │   ├── bookclub_detail_board.jsp
│       │   ├── bookclub_manage.jsp
│       │   ├── bookclub_posts.jsp
│       │   ├── bookclub_posts_edit.jsp
│       │   ├── bookclub_post_detail.jsp
│       │   ├── bookclub_post_forbidden.jsp
│       │   ├── bookclub_board_forbidden.jsp
│       │   └── bookclub_closed_fragment.jsp
│       ├── payment/
│       │   ├── payform.jsp
│       │   ├── success.jsp
│       │   └── fail.jsp
│       ├── admin/
│       │   ├── adminLogin.jsp
│       │   ├── adminAccess.jsp    # 관리자 접근 제한 안내
│       │   ├── balanceCharge.jsp  # 관리자 잔액 충전 (Toss 결제)
│       │   ├── dashboard.jsp
│       │   ├── exDashboard.jsp
│       │   └── tabs/
│       │       ├── dashboardContent.jsp
│       │       ├── usersContent.jsp
│       │       ├── usersLogContent.jsp
│       │       ├── adminLogContent.jsp
│       │       ├── booksContent.jsp
│       │       ├── groupsContent.jsp
│       │       ├── safePayList.jsp
│       │       ├── settlementContent.jsp  # 정산 관리 탭
│       │       ├── bannerContent.jsp
│       │       ├── noticeContent.jsp
│       │       ├── noticeView.jsp
│       │       ├── noticeWriteForm.jsp
│       │       └── noticeEditForm.jsp
│       ├── common/
│       │   ├── header.jsp
│       │   ├── footer.jsp
│       │   ├── home.jsp
│       │   ├── return.jsp
│       │   └── tempPage.jsp
│       ├── error/
│       │   ├── 400.jsp
│       │   └── 500.jsp
│       ├── presentation/
│       │   └── intro.jsp
│       └── userNotice/
│           ├── userNoticeList.jsp
│           └── noticeDetail.jsp
│
├── k6/                            # 부하 테스트 스크립트
├── scripts/                       # 배포 스크립트
├── appspec.yml                    # AWS CodeDeploy
└── pom.xml
```

---

## Redis 활용

### 아키텍처 개요

```
                        ┌─────────────────────┐
                        │   ALB (로드밸런서)     │
                        └──────┬──────┬───────┘
                               │      │
                    ┌──────────┘      └──────────┐
                    ▼                             ▼
             ┌─────────────┐              ┌─────────────┐
             │   EC2 #1    │              │   EC2 #2    │
             │  (Tomcat)   │              │  (Tomcat)   │
             └──────┬──────┘              └──────┬──────┘
                    │                             │
                    └──────────┐  ┌───────────────┘
                               ▼  ▼
                     ┌───────────────────┐
                     │  Redis(ElastiCache)│
                     └───────────────────┘
```

Auto Scaling 환경에서 여러 EC2 인스턴스가 동시에 운영되면, 각 인스턴스는 독립된 프로세스이므로 서버 메모리를 공유할 수 없다. Redis는 이 인스턴스들 사이에서 **공유 저장소**이자 **메시지 브로커** 역할을 한다.

### 용도 요약

| 용도 | 한 줄 요약 |
|------|-----------|
| **Pub/Sub** | 채팅 메시지를 모든 서버 인스턴스의 WebSocket 클라이언트에게 실시간 전달 |
| **Session** | 어떤 EC2로 요청이 라우팅되든 동일한 로그인 세션 유지 |
| **Cache** | 자주 조회되는 목록/상세 데이터를 캐싱하여 DB 부하 감소 |
| **인증코드** | TTL 기반 자동 만료로 이메일 인증코드의 임시 저장 |

### 1. Pub/Sub — 트러블 슈팅: 멀티 서버 환경에서 채팅 메시지 수신 불가

#### 문제 상황

k6 부하 테스트 중 Auto Scaling이 작동하여 EC2가 1대에서 2대로 증가했을 때, 1:1 채팅에서 **상대방의 메시지가 실시간으로 수신되지 않는 현상**이 발생했다.

- 판매자(유저A)가 메시지를 전송하면 구매자(유저B)에게 도착하지 않음
- 새로고침하면 메시지가 보임 (DB에는 정상 저장됨)
- EC2 1대일 때는 정상 동작, **2대 이상에서만 발생**

#### 원인 분석

WebSocket(STOMP) 세션은 **서버 메모리에 바인딩**된다. ALB가 유저A를 EC2 #1로, 유저B를 EC2 #2로 라우팅하면, 두 사람의 WebSocket 세션은 서로 다른 서버에 존재한다.

기존 코드에서 `SimpMessagingTemplate.convertAndSend()`는 **해당 서버의 WebSocket 세션에만** 메시지를 전달하므로, EC2 #1에서 발송한 메시지는 EC2 #2에 연결된 유저B에게 도달할 수 없었다.

```
[문제 발생 구조]
유저A (EC2 #1에 연결) → 메시지 전송 → EC2 #1이 convertAndSend()
                                        → EC2 #1의 WebSocket 클라이언트에만 전달
                                        → EC2 #2의 유저B에게는 전달 불가 ❌
```

이 문제는 채팅 메시지뿐 아니라, WebSocket을 통해 전달되는 **읽음 처리, 안전결제 요청/완료/실패 알림** 등 모든 실시간 이벤트에 동일하게 영향을 미쳤다.

#### 해결: Redis Pub/Sub 도입

`SimpMessagingTemplate.convertAndSend()`를 직접 호출하는 대신, **Redis Pub/Sub 채널에 메시지를 발행**하도록 변경했다. 모든 EC2 인스턴스가 동일한 Redis 채널을 구독하고 있으므로, 어떤 서버에서 발행하든 전체 인스턴스가 수신하여 자기에게 연결된 클라이언트에게 전달한다.

```
[해결 후 구조]
유저A → EC2 #1 → DB 저장 → Redis PUBLISH ("chat:messages")
                                    ↓
                            Redis가 모든 구독자에게 전파
                                    ↓
                    EC2 #1 SUBSCRIBE → 자기 WebSocket 클라이언트에게 전달
                    EC2 #2 SUBSCRIBE → 자기 WebSocket 클라이언트에게 전달
                                        → 유저B 실시간 수신 ✅
```

#### 적용 범위

| Publisher 메서드 | 호출 위치 | 실시간 전달 대상 |
|-----------------|----------|----------------|
| `publishChat()` | `StompController` | 채팅 메시지 |
| `publishRead()` | `StompController`, `ChatroomController` | 읽음 처리 이벤트 |
| `publishPayment()` | `PaymentController` | 안전결제 요청/완료/실패 알림 |
| `publishError()` | `StompController` | 에러 메시지 |

#### 왜 채팅만 해당되는가

일반 HTTP 요청(거래 목록 조회, 독서모임, 마이페이지 등)은 어떤 EC2로 라우팅되든 DB나 Redis Cache(공유 저장소)에서 데이터를 읽기 때문에 멀티 서버 문제가 발생하지 않는다. **서버 로컬 메모리에 바인딩된 커넥션은 WebSocket뿐**이므로, Pub/Sub이 필요한 기능은 채팅(및 채팅 내 결제 알림)으로 한정된다.

- DB 저장 = **영속성** (메시지를 잃지 않기 위함)
- Pub/Sub = **실시간 전달** (다른 서버의 WebSocket 클라이언트에게 즉시 전달)
- 이 둘은 서로 다른 문제를 해결하므로 DB에 저장한다고 Pub/Sub이 불필요해지지 않는다

### 2. Session — 분산 세션 관리

Spring의 `HttpSession`은 기본적으로 서버 메모리(Tomcat)에 저장된다. EC2 #1에서 로그인 후 다음 요청이 EC2 #2로 라우팅되면 세션이 없어 로그아웃 상태로 처리된다.

```java
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 1800,  // 30분
    redisNamespace = "secondarybook:session"
)
```

모든 EC2 인스턴스가 Redis에서 세션을 읽고 쓰므로, 어떤 인스턴스로 라우팅되든 동일한 세션을 사용한다. 기존 `HttpSession` 기반 코드를 거의 수정하지 않고 분산 환경을 지원할 수 있다.

### 3. Cache — 조회 성능 최적화

자주 조회되지만 변경은 드문 데이터를 캐싱하여 DB 부하를 감소시킨다.

| 캐시 이름 | TTL | 대상 | 사용 위치 |
|-----------|-----|------|----------|
| `tradeList` | 5분 | 중고거래 목록 | `TradeService` |
| `trade` | 10분 | 중고거래 상세 | `TradeService` |
| `tradeCount` | 10분 | 중고거래 개수 | `TradeService` |
| `bookClubList` | 10분 | 독서모임 목록 | `BookClubService` |
| `bookClub` | 10분 | 독서모임 상세 | `BookClubService` |

```
1차 요청: DB 조회 → Redis에 저장 → 응답 (느림)
2차 요청: Redis에서 조회 → 응답 (빠름)
데이터 변경: @CacheEvict로 캐시 삭제 → 다음 조회 시 DB에서 다시 가져옴
```

중고거래 특성상 가격 변경, 판매 완료 등이 빈번하므로 5~10분의 짧은 TTL + 변경 시 즉시 무효화로 정합성과 성능의 균형을 맞췄다.

### 4. 이메일 인증코드 — 임시 데이터 저장

회원가입 시 이메일 인증코드(6자리)를 발급하고 3분 후 자동 만료시킨다.

```java
// 인증코드 저장 (3분 TTL)
redisTemplate.opsForValue().set("AuthCode:" + email, checkNum, 180, TimeUnit.SECONDS);

// 인증코드 검증
String storedCode = redisTemplate.opsForValue().get("AuthCode:" + email);
```

Redis의 TTL 기능으로 만료 로직을 별도 구현할 필요 없이 자동 삭제된다. 영속성이 필요 없는 임시 데이터이므로 DB보다 Redis가 적합하다.

---

## S3 & CloudFront (이미지 최적화)

### 아키텍처
```
이미지 업로드 → S3 저장 → CloudFront URL 반환 → 사용자에게 CDN으로 제공
```

### 구성

| 항목 | 값 |
|------|-----|
| **S3 버킷** | secondarybooksimages |
| **CloudFront 도메인** | d3p8m254izebr5.cloudfront.net |
| **이미지 경로** | images/{UUID}.{확장자} |

### 최적화 효과

| 항목 | S3 직접 | CloudFront |
|------|---------|------------|
| **응답 시간** | ~300ms | ~50ms |
| **캐싱** | 없음 | 엣지 로케이션 캐싱 |
| **비용** | 요청당 과금 | 캐시 히트 시 무료 |

### 적용 범위
- 중고책 판매 이미지
- 독서모임 배너/게시글 이미지
- 채팅 이미지

---

## 외부 API

| API | 용도 | 상태 |
|-----|------|------|
| **Toss Payments** | 결제 | 연동 완료 |
| **카카오 책 검색** | 도서 정보 조회 | 연동 완료 |
| **카카오 OAuth** | 소셜 로그인 | 연동 완료 |
| **네이버 OAuth** | 소셜 로그인 | 연동 완료 |
| **Gmail SMTP** | 이메일 발송 | 연동 완료 |
| **AWS S3** | 이미지 저장 | 연동 완료 |
| **AWS CloudFront** | 이미지 CDN | 연동 완료 |

### WebClient (외부 API 호출 클라이언트)

이 프로젝트는 **Spring MVC** 기반이며, 외부 API 호출 시 HTTP 클라이언트로 **WebClient**를 사용한다. WebFlux의 리액티브 스택으로 동작하는 것이 아니라, `.block()`을 통해 **동기 방식**으로 응답을 기다린다. 즉, `RestTemplate`의 대안으로 WebClient를 선택한 구조이다.

#### WebClient Bean 목록 (`WebClientConfig.java`)

| Bean 이름 | Base URL | 용도 |
|-----------|----------|------|
| `tossPaymentWebClient` | Toss API | 결제 승인 요청 (Basic Auth) |
| `kakaoAuthWebClient` | `kauth.kakao.com` | 카카오 OAuth 토큰 발급 |
| `kakaoApiWebClient` | `kapi.kakao.com` | 카카오 사용자 정보 조회 |
| `naverAuthWebClient` | `nid.naver.com` | 네이버 OAuth 토큰 발급 |
| `naverApiWebClient` | `openapi.naver.com` | 네이버 사용자 정보 조회 |
| `kakaoBookWebClient` | 카카오 도서 검색 API | 책 검색 |

각 Bean은 `ConnectionProvider`로 커넥션 풀(50~100개)을 설정하고, `ReactorClientHttpConnector`를 통해 Netty 기반 HTTP 통신을 수행한다.

#### 사용 위치

| 파일 | 주입 Bean | 호출 방식 | 설명 |
|------|-----------|-----------|------|
| `TossApiService.java` | `tossPaymentWebClient` | `.bodyToMono(TossPaymentResponse.class).block()` | 토스 결제 승인 API 호출, Idempotency-Key로 이중결제 방지 |
| `BookApiService.java` | `kakaoBookWebClient` | `.bodyToMono(String.class).block()` | 카카오 도서 검색, JSON 파싱 후 `BookVO` 리스트 반환 |
| `MemberController.java` | `kakaoAuthWebClient`, `kakaoApiWebClient`, `naverAuthWebClient`, `naverApiWebClient` | `.block()` | 소셜 로그인 (토큰 발급 → 사용자 정보 조회) |

#### 호출 흐름

```
Service/Controller → WebClient Bean → .get()/.post() → .retrieve() → .bodyToMono(T.class) → .block()
                                                                                                  ↑
                                                                                           동기 대기 (결과 반환까지 블로킹)
```

> **참고:** `Mono`, `Flux` 등 리액티브 타입을 컨트롤러 반환값으로 사용하지 않으며, WebFlux 서버로 동작하지 않는다. WebClient는 순수하게 외부 API 호출용 HTTP 클라이언트로만 사용된다.

---

## 예외 계층화 (Exception Hierarchy)

### 설계 원칙
- HTTP 상태 코드 기준으로 중간 추상 클래스를 두어 계층화
- `GlobalExceptionHandler`에서 `ClientException`(4xx)과 `ServerException`(5xx) 두 개로 단순 처리
- 에러 페이지는 `400.jsp`와 `500.jsp` 두 개만 사용, `errorMessage` 동적 표시
- 기능별 하위 패키지로 구체 예외 분리

### 계층 구조

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

### GlobalExceptionHandler 처리 흐름

| 핸들러 | 대상 | 응답 |
|--------|------|------|
| `ClientException` | 모든 4xx 예외 | AJAX: JSON 400 / 일반: error/400.jsp |
| `ServerException` | 모든 5xx 예외 | error/500.jsp |
| `MaxUploadSizeExceededException` | 파일 크기 초과 | error/400.jsp |
| `Exception` | 미처리 예외 | error/500.jsp |

### 새 예외 추가 가이드

1. `ClientException` 또는 `ServerException` 하위 클래스로 생성
2. 기능별 하위 패키지에 배치 (예: `exception/payment/PaymentException.java`)
3. `GlobalExceptionHandler` 수정 불필요 (상위 클래스에서 자동 처리)

---

## 입력값 검증 (Spring Bean Validation)

사용자 입력이 바인딩되는 모든 VO / DTO에 Spring Bean Validation(`javax.validation`) 애노테이션을 적용했습니다.

| 대상 | 주요 검증 항목 |
|------|----------------|
| `MemberVO` | 아이디·비밀번호·이메일·닉네임 (가입/수정 그룹 분리) |
| `AdminVO` | 관리자 아이디·비밀번호 길이 |
| `AddressVO` | 우편번호 형식(5자리 숫자), 주소 필수 입력 |
| `BookClubVO` | 모임 이름·소개·지역 필수, 정원 범위(2~100) |
| `BookClubUpdateSettingsDTO` | 모임 이름·소개 필수 및 최대 길이 |
| `NoticeVO` | 제목·내용 필수 및 최대 길이 |
| `TradeVO` | (기존 적용) 제목·내용·가격 등 필수 및 범위 |

- **`@Valid` + `BindingResult`**: VO/DTO를 `@ModelAttribute`·`@RequestBody`로 바인딩하는 엔드포인트
- **`@Validated` (클래스 레벨) + `ConstraintViolationException` 핸들러**: `@RequestParam`을 직접 사용하는 엔드포인트
- **그룹 검증**: `SignUpGroup` / `UpdateGroup` 인터페이스로 가입·수정 시 서로 다른 규칙 적용

---

## AOP (Spring AOP)

서비스 계층 전체에 실행 로깅을 적용했습니다.

- `ServiceLoggingAspect` — `*Service` 클래스의 모든 public 메서드에 `@Around`로 실행시간·예외를 자동 기록

---

## Flow 1: 회원가입

### 전체 흐름
```
사용자                        서버                          외부
  │                           │                            │
  ├─ GET /signup ────────────→│                            │
  │←── signup.jsp 폼 반환 ────│                            │
  │                           │                            │
  ├─ 아이디 입력 후 중복확인 ─→│                            │
  │  GET /auth/ajax/idCheck   │─ DB 조회 ─→ COUNT(*) ──→  │
  │←── 0(사용가능) / 1(중복) ──│                            │
  │                           │                            │
  ├─ 이메일 입력 후 중복확인 ─→│                            │
  │  GET /auth/ajax/emailCheck│─ DB 조회 ─→ COUNT(*) ──→  │
  │←── 0(사용가능) / 1(중복) ──│                            │
  │                           │                            │
  ├─ 인증번호 발송 클릭 ──────→│                            │
  │  GET /auth/ajax/sendEmail │─ 6자리 코드 생성 ──→       │
  │                           │─ Redis 저장 (TTL 3분) ──→  │
  │                           │─ HTML 이메일 발송 ────────→│ Gmail SMTP
  │←── "success" ──────────────│                            │
  │                           │                            │
  ├─ 인증번호 입력 후 확인 ───→│                            │
  │  GET /auth/ajax/           │─ Redis에서 코드 조회 ──→   │
  │      checkEmailCode       │─ 코드 비교 ──→             │
  │←── true(일치) / false ─────│─ 일치 시 Redis 삭제 ──→   │
  │                           │                            │
  ├─ 닉네임 입력 후 중복확인 ─→│                            │
  │  GET /auth/ajax/           │─ DB 조회 ─→ COUNT(*) ──→  │
  │      nicknmCheck          │                            │
  │←── 0(사용가능) / 1(중복) ──│                            │
  │                           │                            │
  ├─ 폼 제출 ─────────────────→│                            │
  │  POST /auth/signup        │─ BCrypt 비밀번호 암호화 ─→ │
  │                           │─ DB INSERT ──→ MEMBER_INFO │
  │←── 성공 → /login 리다이렉트│                            │
```

### 프론트엔드 검증 (validateForm)

| 항목 | 조건 | 에러 메시지 |
|------|------|------------|
| 아이디 | 4자 이상, 중복확인 완료 | "아이디 중복 확인을 해주세요." |
| 이메일 | RFC 5322 형식, 중복확인 + 인증 완료 | "이메일 인증을 완료해주세요." |
| 비밀번호 | 8자 이상 | "비밀번호는 8자 이상이어야 합니다." |
| 비밀번호 확인 | 비밀번호와 일치 | "비밀번호가 일치하지 않습니다." |
| 닉네임 | 2자 이상, 중복확인 완료 | "닉네임은 2글자 이상이어야 합니다." |
| 휴대폰 | XXX-XXXX-XXXX 형식 | "휴대폰 번호 형식이 올바르지 않습니다." |
| 약관 동의 | 3개 필수 약관 모두 체크 | "필수 약관에 모두 동의해주세요." |

### 이메일 인증 상세
```
인증 코드: 6자리 랜덤 (111111 ~ 999999)
저장소: Redis (Key: "AuthCode:{email}", TTL: 180초)
이메일: HTML 템플릿 (JavaMailSender, Gmail SMTP)
검증 후: Redis에서 코드 삭제 (1회성)
```

### 비밀번호 처리
```
평문 → BCryptPasswordEncoder.encode() → "$2a$10$..." (DB 저장)
로그인 시 → BCryptPasswordEncoder.matches(입력값, DB값)
기존 MD5 회원 → BCrypt 마이그레이션 (loginByMd5 fallback)
```

---

## Flow 2: 판매글 CRUD → 1:1 채팅 → 안전결제 → 구매확정

### 전체 흐름 요약
```
판매자: 판매글 등록 → 구매자: 채팅 문의 → 판매자: 안전결제 요청 (5분 타이머)
    → 구매자: Toss 결제 → 판매 상태 SOLD → 배송 → 구매자: 구매 확정 (또는 15일 자동 확정)
```

### Phase 1: 판매글 CRUD

```
[등록]
판매자 ─ GET /trade ──→ tradeform.jsp (카카오 책 검색 API로 도서 선택)
       ─ POST /trade ──→ S3 이미지 업로드 → DB INSERT → 캐시 evict
       ←── /trade/{tradeSeq} 리다이렉트

[조회]
사용자 ─ GET /trade/{tradeSeq} ──→ TradeService.search() (캐시)
       ←── tradedetail.jsp (판매 정보 + 이미지 + 찜/채팅 버튼)

[수정]
판매자 ─ GET /trade/modify/{tradeSeq} ──→ 소유자 검증 + 결제상태 검증
       ─ POST /trade/modify/{tradeSeq} ──→ S3 이미지 교체 → DB UPDATE → 캐시 evict
       ※ safe_payment_st가 PENDING/COMPLETED이면 수정 불가 (ForbiddenException)

[삭제]
판매자 ─ POST /trade/delete/{tradeSeq} ──→ 소유자 + 결제상태 검증 → soft delete
       ※ safe_payment_st가 PENDING/COMPLETED이면 삭제 불가

[수동 판매완료]
판매자 ─ POST /trade/sold ──→ 소유자 검증 → sale_st = SOLD (안전결제 없이)
```

### Phase 2: 1:1 채팅

```
구매자                          서버                         판매자
  │                             │                            │
  ├─ "채팅하기" 클릭 ──────────→│                            │
  │  POST /chatrooms           │─ 자기 자신 채팅 방지 ──→   │
  │  (trade_seq, seller_seq)   │─ findOrCreateRoom() ──→    │
  │                             │  ├ 기존 채팅방 있으면 반환  │
  │                             │  └ 없으면 생성 (Race       │
  │                             │    Condition: Duplicate     │
  │                             │    KeyException 처리)       │
  │←── chatrooms.jsp ───────────│                            │
  │                             │                            │
  ├─ WebSocket 연결 ───────────→│ /chatEndPoint (SockJS)     │
  │  STOMP subscribe:          │                            │
  │  /chatroom/{room_id}      │                            │
  │                             │                            │
  ├─ 메시지 입력 ──────────────→│                            │
  │  /sendMessage/chat/{id}    │─ 세션 검증 ──→             │
  │                             │─ 채팅방 멤버 검증 ──→      │
  │                             │─ 메시지 길이 검증(≤1000) ──→│
  │                             │─ DB 저장 ──→               │
  │                             │─ 채팅방 미리보기 갱신 ──→   │
  │                             │─ STOMP broadcast ─────────→│ 실시간 수신
  │                             │                            │
  ├─ 읽음 이벤트 ──────────────→│                            │
  │  /sendMessage/chat/{id}/   │─ DB 읽음 처리 ──→          │
  │  read                      │─ broadcast ───────────────→│ "읽음" 표시
  │                             │                            │
  ├─ 이미지 전송 ──────────────→│                            │
  │  POST /chat/image/upload   │─ S3 업로드 ──→             │
  │←── imageUrl ────────────────│                            │
  │  [IMAGE]{url} 메시지 전송 ─→│─ broadcast ───────────────→│
```

### Phase 3: 안전결제 요청 → 결제

```
판매자                          서버                         구매자
  │                             │                            │
  ├─ "안전결제 요청" 클릭 ─────→│                            │
  │  [SAFE_PAYMENT_REQUEST]    │                            │
  │                             │─ canUseSafePayment() 검증  │
  │                             │  ├ 판매자 본인인지 확인     │
  │                             │  ├ sale_st = SALE 확인      │
  │                             │  └ safe_payment_st = NONE   │
  │                             │                            │
  │                             │─ requestSafePayment()      │
  │                             │  ├ safe_payment_st → PENDING│
  │                             │  ├ pending_buyer_seq 설정   │
  │                             │  └ expire_dtm = NOW + 5분   │
  │                             │                            │
  │                             │─ broadcast ───────────────→│ "안전결제 요청"
  │                             │                            │  5분 카운트다운 시작
  │                             │                            │
  │                             │                            ├─ "결제하기" 클릭
  │                             │←── GET /payments ──────────│
  │                             │─ 검증: PENDING, 구매자 일치,│
  │                             │  sale_st ≠ SOLD            │
  │                             │──→ payform.jsp ───────────→│
  │                             │                            │
  │                             │                            ├─ Toss 결제 진행
  │                             │                            │  (카드/간편결제)
  │                             │                            │
  │                             │←── /payments/success ──────│
  │                             │  ?paymentKey=...            │
  │                             │  &orderId=...               │
  │                             │  &amount=...                │
  │                             │                            │
  │                             │─ 금액 검증 (DB vs 요청)     │
  │                             │─ TossAPI 결제 승인 요청     │
  │                             │  POST /v1/payments/confirm  │
  │                             │─ pending_buyer_seq 검증     │
  │                             │─ 배송지 처리                │
  │                             │                            │
  │                             │─ successPurchase()          │
  │                             │  ├ sale_st → SOLD           │
  │                             │  ├ member_buyer_seq 설정    │
  │                             │  ├ 배송지 저장              │
  │                             │  └ confirm_purchase = false  │
  │                             │                            │
  │  [SAFE_PAYMENT_COMPLETE]  ←│─ 채팅방 알림 broadcast ───→│ success.jsp
  │  실시간 알림 수신           │                            │
```

### Phase 4: 결제 실패/타임아웃 처리

```
[결제 실패]
구매자 결제 취소 or 오류 ──→ GET /payments/fail
  → tradeService.cancelSafePayment() (PENDING → NONE)
  → 채팅방에 [SAFE_PAYMENT_FAILED] 메시지 전송
  → 재요청 가능 (safe_payment_st가 NONE으로 복구)

[5분 타임아웃 - 클라이언트]
카운트다운 0 도달 ──→ POST /payments/timeout
  → cancelSafePayment() → PENDING → NONE
  → 채팅방에 실패 메시지 전송

[5분 타임아웃 - 서버 스케줄러]
SafePaymentScheduler (매 60초 실행)
  → safe_payment_expire_dtm < NOW인 PENDING 건 조회
  → safe_payment_st → NONE으로 일괄 리셋
```

### Phase 5: 구매 확정

```
[수동 확정]
구매자 ─ POST /trade/confirm/{trade_seq} ──→ 구매자 검증
       ──→ confirm_purchase = true
       ←── {success: true}

[자동 확정]
SafePaymentScheduler (매일 자정 실행)
  → 결제 완료 후 15일 경과 + confirm_purchase = false 건 조회
  → confirm_purchase = true로 일괄 업데이트
```

### Phase 6: 정산 (Settlement)

```
[판매자 정산 신청]
판매자 ─ 마이페이지 > 판매 내역 > "정산 신청" 클릭
       → SettlementService.requestSettlement()
       → sb_trade_info SELECT ... FOR UPDATE (동시 신청 차단)
       → 검증: 판매자 본인 / safe_payment_st=COMPLETED / confirm_purchase=true
               / settlement_st=READY / 계좌 등록 여부
       → 수수료 계산: (sale_price + delivery_cost) × 1% (BigDecimal FLOOR)
       → settlement INSERT + sb_trade_info.settlement_st → REQUESTED

[자동 배치 처리 - 매일 새벽 3시]
SettlementScheduler (@Scheduled cron = "0 0 3 * * *")
  → JobLauncher.run(settlementJob, params)

Spring Batch Job (chunk=1, faultTolerant):
  Reader   : REQUESTED 상태 건 조회
  Processor: 유효성 검증
  Writer   : admin_account SELECT FOR UPDATE → 잔액 확인
             → 잔액 충분: 잔액 차감 + settlement_st → COMPLETED + 감사 로그
             → 잔액 부족: InsufficientBalanceException throw
  Skip     : InsufficientBalanceException → SkipListener
             → settlement_st → INSUFFICIENT_BALANCE
             → sb_trade_info.settlement_st → INSUFFICIENT_BALANCE

[관리자 - 이체 완료 처리]
배치 완료 후 어드민 페이지 > 정산 관리
  → "이체 대기" 목록 확인 (계좌번호 AES 복호화하여 표시)
  → 관리자가 은행 앱으로 직접 입금
  → "이체 완료" 버튼 클릭 → transfer_confirmed_yn = 1

[관리자 잔액 충전]
관리자 ─ 어드민 > "잔액 충전" 클릭 → balanceCharge.jsp
       → 금액 선택 → Toss 결제 (TossPayments.requestPayment)
       → GET /admin/balance/success?paymentKey=...&amount=...
       → tossApiService.confirmPayment() → DONE 확인
       → settlementService.chargeAdminBalance() (admin_account.balance 증가 + 감사 로그)
       → DB 실패 시: tossApiService.cancelPayment() → 자동 환불

[잔액 부족 재처리]
관리자 ─ "재처리 설정" 클릭
       → settlement_st: INSUFFICIENT_BALANCE → REQUESTED
       → 다음 새벽 3시 배치에서 재시도
```

### 거래 상태 전이도

```
                    ┌──────────────────────────────────────────────┐
                    │                                              │
  [판매글 등록]     │      [안전결제 요청]        [결제 실패/만료] │
  sale_st: SALE ────┼──→ safe_payment_st: ──→ safe_payment_st:    │
  safe_payment_st:  │    NONE → PENDING        PENDING → NONE ────┘
  NONE              │         │                   (재요청 가능)
                    │         │
  ✓ 수정 가능       │         │ [결제 성공]
  ✓ 삭제 가능       │         ↓
                    │    sale_st: SOLD
                    │    safe_payment_st: COMPLETED
                    │    confirm_purchase: false
                    │         │
                    │    ✗ 수정 불가
                    │    ✗ 삭제 불가
                    │         │
                    │         │ [구매 확정] (수동 or 15일 자동)
                    │         ↓
                    │    confirm_purchase: true
                    │    → 거래 완료
                    │
  [수동 판매완료]   │
  POST /trade/sold ─┘
  sale_st: SOLD (안전결제 없이)
```

---

## Flow 3: 독서모임 생성/가입 및 내부 운영

### 모임 생성 흐름

```
사용자                          서버                         S3
  │                             │                            │
  ├─ GET /bookclubs/create ───→│                            │
  │←── bookclub_create.jsp ─────│                            │
  │                             │                            │
  ├─ 폼 작성 후 제출 ─────────→│                            │
  │  POST /bookclubs           │                            │
  │  (모임명, 설명, 지역,      │─ 로그인 검증 ──→           │
  │   모집인원, 일정,          │─ 모임명 중복 검증 ──→      │
  │   배너 이미지)             │─ 배너 이미지 업로드 ──────→│ S3 저장
  │                             │←── CloudFront URL ─────────│
  │                             │                            │
  │                             │─ DB INSERT: book_club      │
  │                             │─ 생성자를 모임장으로 등록   │
  │                             │  book_club_member           │
  │                             │  (leader_yn=true, JOINED)   │
  │                             │─ 캐시 evict ──→             │
  │                             │                            │
  │←── /bookclubs/{id} 리다이렉트│                            │
```

### 모임 가입 흐름

```
일반 회원                       서버                         모임장
  │                             │                            │
  ├─ 모임 목록에서 모임 클릭 ──→│                            │
  │  GET /bookclubs/{id}       │─ 모임 정보 + CTA 상태 조회 │
  │←── bookclub_detail.jsp ─────│                            │
  │  (CTA 버튼: "가입 신청")   │                            │
  │                             │                            │
  ├─ "가입 신청" 클릭 ─────────→│                            │
  │  POST /bookclubs/{id}/join │                            │
  │  (가입 사유 메시지)        │─ 이미 가입? → ALREADY_JOINED│
  │                             │─ 대기중? → ALREADY_REQUESTED│
  │                             │─ DB INSERT: book_club_request│
  │                             │  (request_st = WAIT)        │
  │←── SUCCESS ─────────────────│                            │
  │  (CTA → "승인 대기중")     │                            │
  │                             │                            │
  │                             │     [모임장이 관리 페이지]  │
  │                             │←── GET /bookclubs/{id}/     │
  │                             │    manage                   │
  │                             │──→ bookclub_manage.jsp ────→│
  │                             │    (가입 요청 목록 표시)    │
  │                             │                            │
  │                             │                            ├─ "승인" 클릭
  │                             │←── POST .../approve ───────│
  │                             │─ book_club FOR UPDATE 락   │
  │                             │─ 정원 초과 검증 ──→        │
  │                             │─ book_club_member INSERT    │
  │                             │  (join_st = JOINED)         │
  │                             │─ request_st → APPROVED      │
  │                             │──→ {success, memberCount} ─→│
  │                             │                            │
  │  (CTA → "탈퇴하기")       │          OR                 │
  │  게시판 접근 가능           │                            │
  │                             │                            ├─ "거절" 클릭
  │                             │←── POST .../reject ────────│
  │                             │─ request_st → REJECTED      │
  │                             │──→ {success} ──────────────→│
```

### CTA(Call-To-Action) 버튼 상태

| 사용자 상태 | CTA 버튼 | 게시판 접근 |
|-------------|----------|------------|
| 비로그인 / 비회원 | "가입 신청" | 불가 |
| WAIT (대기중) | "승인 대기중" (비활성) | 불가 |
| REJECTED (거절됨) | "재신청" | 불가 |
| JOINED (가입됨) | "탈퇴하기" | 가능 |
| 모임장 | "관리" | 가능 |

### 모임 내부 운영: 게시판

```
[글 작성]
회원 ─ GET /bookclubs/{id}/posts ──→ 권한 검증 (JOINED/모임장)
     ─ POST /bookclubs/{id}/posts ──→ S3 이미지 업로드 (선택)
       → DB INSERT: book_club_board (parent_seq = NULL)
       → 리다이렉트: 게시글 상세

[글 조회]
회원 ─ GET /bookclubs/{id}/posts/{postId} ──→ 권한 검증
     ←── bookclub_post_detail.jsp (본문 + 댓글 + 좋아요)

[글 수정] ※ 작성자만 가능
작성자 ─ GET .../posts/{postId}/edit ──→ 작성자 검증
       ─ POST .../posts/{postId}/edit ──→ S3 이미지 교체 → DB UPDATE

[글 삭제] ※ 작성자 또는 모임장
작성자/모임장 ─ POST .../posts/{postId}/delete ──→ soft delete
              → S3 이미지 삭제 (AfterCommit)

[댓글 작성]
회원 ─ POST .../posts/{postId}/comments ──→ 권한 검증
     → DB INSERT: book_club_board (parent_seq = postId)

[댓글 수정] ※ 작성자만
작성자 ─ POST .../comments/{commentId}/edit ──→ 작성자 검증

[댓글 삭제] ※ 작성자 또는 모임장
작성자/모임장 ─ POST .../comments/{commentId}/delete ──→ soft delete

[좋아요 토글] (게시글/댓글 공통)
회원 ─ POST /bookclubs/{id}/boards/{boardId}/like ──→ AJAX
     → 해당 모임의 게시글인지 IDOR 검증
     → book_club_board_like INSERT or DELETE (토글)
     ←── {liked: true/false, likeCount: N}
```

### 모임장 관리 기능

```
[멤버 강퇴]
모임장 ─ POST .../manage/members/{memberSeq}/kick
       → 모임장 본인 강퇴 방지
       → join_st → KICKED
       ←── {memberCount: N}

[모임 설정 수정]
모임장 ─ POST .../manage/settings
       → 모임명 중복 검증 (자기 자신 제외)
       → 배너 이미지 교체 시 S3 업로드
       → DB UPDATE: book_club
       → 이전 배너 S3 삭제 (AfterCommit)
       → 캐시 evict

[가입 승인/거절]
모임장 ─ POST .../requests/{reqId}/approve ──→ FOR UPDATE 락
       → 정원 초과 검증 → book_club_member INSERT/UPDATE
       OR
모임장 ─ POST .../requests/{reqId}/reject
       → request_st → REJECTED
```

### 모임 탈퇴 및 리더 승계

```
[일반 회원 탈퇴]
회원 ─ POST /bookclubs/{id}/leave
     → join_st → LEFT

[모임장 탈퇴 - 다른 멤버 있을 때]
모임장 ─ POST /bookclubs/{id}/leave
       → book_club FOR UPDATE 락 (동시성 제어)
       → 가장 오래된 JOINED 멤버 조회
       → 해당 멤버에게 leader_yn = true 위임
       → book_club.leader_seq 업데이트
       → 기존 모임장 join_st → LEFT

[모임장 탈퇴 - 마지막 멤버일 때]
모임장 ─ POST /bookclubs/{id}/leave
       → 다른 JOINED 멤버 없음
       → 모임 soft delete (book_club_deleted_dt = NOW)
       → 모든 게시글 이미지 + 배너 이미지 S3 삭제 예약 (AfterCommit)
```

### 독서모임 상태 전이도

```
[가입 요청 상태]
  WAIT ──→ APPROVED (승인) ──→ JOINED (멤버 활동)
    │                            │
    └──→ REJECTED (거절)         ├──→ LEFT (자발적 탈퇴)
         │                      └──→ KICKED (강퇴)
         └──→ 재신청 가능

[모임 생명주기]
  생성 (모임장 = 생성자)
    │
    ├──→ 운영 중 (멤버 가입/탈퇴, 게시판 활동)
    │
    ├──→ 모임장 탈퇴 + 다른 멤버 있음 → 리더 자동 승계
    │
    └──→ 모임장 탈퇴 + 마지막 멤버 → 모임 종료 (soft delete)
```

---

## 부하 테스트 (k6)

AWS CloudWatch를 통해 실시간 모니터링하며 테스트를 진행했습니다.

### 시나리오 1: Load Test (예상 트래픽)

약 30명의 인원이 30분간 접속하는 상황 시뮬레이션.

```javascript
export const options = {
    stages: [
        { duration: '2m', target: 30 },
        { duration: '26m', target: 30 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.05'],
    },
};
```

**결과:**
| 지표 | 기준 | 결과 | 판정 |
|------|------|------|------|
| p(95) 응답시간 | 1초 미만 | **59.7ms** | PASS |
| 에러율 | 5% 미만 | **0.00%** | PASS |

```
http_req_duration: avg=26.73ms, med=21.52ms, p(95)=59.7ms
http_req_failed: 0.00%
http_reqs: 13,695건 (7.6/s)
```

### 시나리오 2: Stress Test (한계 테스트)

500 VU까지 점진적으로 부하 증가.

```javascript
export const options = {
    stages: [
        { duration: '1m', target: 30 },
        { duration: '3m', target: 30 },
        { duration: '1m', target: 100 },
        { duration: '5m', target: 100 },
        { duration: '1m', target: 200 },
        { duration: '5m', target: 200 },
        { duration: '1m', target: 350 },
        { duration: '5m', target: 350 },
        { duration: '1m', target: 500 },
        { duration: '5m', target: 500 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.10'],
    },
};
```

**결과 (t3.small x 2대, Auto Scaling):**
| 지표 | 기준 | 결과 | 판정 |
|------|------|------|------|
| p(95) 응답시간 | 3초 미만 | **1.85초** | PASS |
| 에러율 | 10% 미만 | **0.00%** | PASS |

```
http_req_duration: avg=452.92ms, med=149.01ms, p(90)=1.06s, p(95)=1.85s
http_req_failed: 0.00%
http_reqs: 458,004건 (254/s)
data_received: 19 GB
```

**Auto Scaling 동작:**
- 테스트 중 CPU 50% 초과 → 1대 → 2대 스케일 아웃
- 테스트 종료 후 → 2대 → 1대 스케일 인

---

## 로컬 개발 환경 설정

### 필수 요구사항
- **Java**: JDK 17
- **Maven**: 3.6.0 이상
- **Redis**: 로컬 또는 원격 Redis 서버
- **Database**: MySQL 8.0+

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd secondaryBook
```

### 2. 설정 파일 수정
`src/main/resources/application.properties` 파일에서 환경에 맞게 설정:

```properties
# Database (MySQL)
db.url=jdbc:log4jdbc:mysql://your-host:3306/your-db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
db.username=your-username
db.password=your-password

# Redis
redis.host=localhost
redis.port=6379

# 이미지 저장 경로
file.dir=/your/image/path

# Toss Payments
api.toss.secret-key=your-secret-key
api.toss.client-key=your-client-key

# AES-256-CBC 계좌번호 암호화 키 (32바이트를 Base64 인코딩)
aes.secret-key=your-base64-encoded-32-byte-key

# Kakao OAuth
api.kakao.client.id=your-client-id
api.kakao.client_secret=your-secret
api.kakao.redirect.uri=http://localhost:8080/auth/kakao/callback

# Naver OAuth
api.naver.client.id=your-client-id
api.naver.client.secret=your-secret
api.naver.redirect.uri=http://localhost:8080/auth/naver/callback

# Kakao Book API
api.kakao.rest-api-key=your-api-key

# Gmail SMTP
mail.username=your-email@gmail.com
mail.password=your-app-password
```

### 3. 빌드 및 실행

**macOS/Linux:**
```bash
./mvnw clean package
./mvnw cargo:run
```

**Windows:**
```cmd
mvnw.cmd clean package
mvnw.cmd cargo:run
```

### 4. 접속
- http://localhost:8080/
