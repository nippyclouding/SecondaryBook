# SecondaryBook - 중고책 거래 및 독서 모임 플랫폼

## 프로젝트 개요


중고 시장의 특성 상 판매자의 악의성 사기 문제가 많이 발생합니다.

우리 플랫폼은 중고책 시장의 특징인 사기 문제를 "안전 결제" 개념을 도입하여 해결합니다.

<img width="942" height="494" alt="무제 2" src="https://github.com/user-attachments/assets/2323d2c0-7950-4b70-be8a-c8bff5d2b11e" />

당근, 중고나라의 특징 : 온라인 중고 거래 시 사기 문제 발생 가능 & 중고품 상태 불확실

YES24, 알라딘의 특징 : 높은 가격과 매입 제한


<img width="953" height="457" alt="무제" src="https://github.com/user-attachments/assets/cebd5d23-0b16-41e3-95da-b42713ba6b9e" />

등록의 정확성 : Kakao 도서 API를 이용하여 구매자는 높은 정확도의 정보로 책에 대한 데이터를 확인할 수 있고, 판매자는 등록 시 한 번의 클릭으로 정확한 데이터를 불러올 수 있습니다.

거래의 안정성 : 중고 거래 특징 상 사기 문제를 "안전 결제" 개념으로 풀어내었습니다.

독서의 연결성 : 우리 플랫폼을 이용하는 사람들은 독서를 즐길 것으로 예상되기에 "독서 모임" 커뮤니티를 기획 및 개발하였습니다.


---

### 모듈 구성

| 모듈 | 디렉토리 | 역할 | 패키징 | 배포 서버 |
|------|---------|------|--------|----------|
| **웹 서버** | `mvc/` | 사용자 요청 처리 (API, 화면, 채팅) | WAR | AWS EC2 × N대 (Auto Scaling) |
| **배치 서버** | `batch/` | 정산 자동화, 스케줄러 | JAR | AWS EC2 전용 1대 |


---

## 주요 기능

### 1. 회원 관리
- **소셜 로그인** - 카카오, 네이버 OAuth 2.0
- **회원가입/로그인** - 일반 이메일 로그인
- **비밀번호 찾기** - Gmail SMTP 이메일 인증
- **회원 정보 수정/탈퇴 - 회원 탈퇴 시 해당 회원이 작성한 게시글, 참여 모임 먼저 삭제 후 탈퇴**

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
- **정산 계좌 관리** - 판매 대금 수령 계좌 등록/수정

### 7. 정산
- **정산 신청** - 구매확정이 완료된 거래에 한해 판매자가 마이페이지에서 직접 신청
- **수수료 1%** - (판매가 + 배송비) × 1% 수수료 차감 후 판매자에게 지급
- **Spring Batch 자동 처리** - 매일 새벽 3시 정산 신청된(REQUESTED) 건을 일괄 자동 처리
- **잔액 부족 처리** - 관리자 잔액 부족 시 해당 건만 INSUFFICIENT_BALANCE로 skip 처리하고 나머지 건은 계속 진행, 판매자에게 이메일 알림 발송
- **관리자 잔액 충전** - 관리자 페이지에서 Toss 결제로 운영 잔액 직접 충전
- **수동 이체 + 확인** - 배치 처리(COMPLETED) 후 관리자가 은행 앱으로 직접 이체 → "이체 완료 확인" 클릭으로 최종 처리 종료
- **재처리** - 잔액 부족 건을 관리자가 충전 후 REQUESTED 상태로 되돌려 다음 배치에서 자동 재시도

### 8. 관리자
- **대시보드** - 회원 수, 거래 수, 모임 수 통계
- **회원/상품/모임 관리** - 목록, 검색, 상세 조회, 회원 제재
- **안전결제 내역** - 전체 결제 내역 조회
- **정산 관리** - 이체 대기 목록 (계좌번호 복호화 표시), 잔액 부족 목록, 관리자 잔액 현황
- **관리자 잔액 충전** - Toss 결제 연동
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
| **Framework** | Spring Boot 3.3.4, Spring Batch 5 |
| **ORM** | MyBatis 3.0.3 (mybatis-spring-boot-starter) |
| **Reader** | MyBatisCursorItemReader (DB 커서 스트리밍) |
| **Database** | 동일 RDS 서버, 별도 DB (`secondHandBook_batch`) |
| **암호화** | AES-256-CBC (계좌번호 복호화) |
| **Email** | Gmail SMTP (JavaMailSender, jakarta.mail) |
| **Build Tool** | Maven |
| **Java Version** | Java 17 |
| **Packaging** | JAR (내장 서버 없음, 배치 전용) |

---

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


## 정산 플로우

> 이 플랫폼의 정산은 **구매자 결제 → 구매 확정 → 판매자 신청 → 배치 자동 처리 → 관리자 수동 이체** 순으로 진행됩니다.
> 배치가 실제 은행 이체를 직접 수행하지는 않으며, DB 상태 처리 + 잔액 차감까지만 담당하고 이체는 관리자가 수동으로 진행합니다.




---

### ERD

옅은 갈색 : 관리자 관련 테이블

노란색 : 회원 관련 테이블

초록색 : 독서 모임 관련 테이블

자주색 : 정산 테이블

짙은 갈색 : 거래 관련 테이블

보라색 : 1:1 채팅 테이블

검정색 : 배치 테이블

<img width="4540" height="3722" alt="secondarybook_erd" src="https://github.com/user-attachments/assets/540f46ad-a383-4c65-90fa-411dedc1f396" />

---

## 기술적 의사결정

### Spring MVC (웹 서버)

Spring Boot는 자동 설정(Auto Configuration)으로 빠르게 시작할 수 있지만, 무엇이 어떻게 등록되는지 파악하기 어렵다. 이 프로젝트에서는 Spring Boot를 사용하지 않고 `web.xml`, `root-context.xml`, `MvcConfig.java`를 직접 작성해 **필터 체인 순서, 컨텍스트 계층 구조, 빈 등록 과정**을 명시적으로 제어했다.

### Spring Boot (배치 서버)

배치 서버는 Spring Boot 3.3.4를 사용한다. Spring Batch 5.x는 Spring Boot 3.x 생태계를 전제로 설계되었고, 별도 Tomcat이 필요 없는 JAR 실행 방식이 배치 전용 서버에 더 적합하다.



### MyBatis

중고책 거래 플랫폼 특성상 다음과 같은 쿼리가 많다.
- 다중 테이블 JOIN (거래 + 이미지 + 카테고리 + 회원)
- 동적 검색 조건 (카테고리, 키워드, 정렬, 판매 상태 필터)
- `SELECT ... FOR UPDATE` (안전결제 동시성 제어, 정산 배치)

JPA로 이런 쿼리를 작성하면 JPQL 또는 Native Query로 우회해야 하고, 실행되는 SQL을 직접 제어하기 어렵다.

MyBatis XML 매퍼를 사용하면 SQL을 직접 작성하므로 쿼리 최적화와 실행 계획을 명확히 파악할 수 있다.

배치 서버도 동일한 이유로 MyBatis를 선택했다.



---

### 시스템 아키텍쳐

<img width="841" height="820" alt="무제 (1)" src="https://github.com/user-attachments/assets/6812296a-5a1f-42a6-9e99-2df6aa511fb8" />

---

### Redis
세션·캐시·Pub/Sub 기능 수행

단일 Redis 인스턴스(ElastiCache)로 세 가지 역할을 처리한다.

| 용도 | 한 줄 요약 |

| **Session** | 어떤 EC2로 요청이 라우팅되든 동일한 로그인 세션 유지

| **Cache** | 자주 조회되는 목록/상세 데이터를 캐싱하여 DB 부하 감소

| **Pub/Sub** | 채팅 메시지를 다중 서버 전체에 브로드캐스트

---


### 트러블슈팅과 Redis Pub/Sub 도입
문제 상황 : 멀티 서버 환경에서 채팅 메시지 수신 불가

#### 문제 상황

해당 프로젝트는 AWS 아키텍쳐에서 ELB의 Auto Scaling을 통해 요청이 많을 경우 같은 스펙의 EC2가 자동으로 복제된다.

개발 완료 후 테스트 기간에 부하 테스트를 하는 도중 요청이 많아 Auto Scaling이 동작하여 EC2가 3대가 되었을 때 1:1 채팅에서 **상대방의 메시지가 실시간으로 수신되지 않는 현상**이 발생했다.


- 판매자(유저A)가 메시지를 전송하면 구매자(유저B)에게 실시간으로 도착하지 않음

- 새로고침하면 다시 DB에서 HTTP 요청으로 메시지를 조회해서 새로운 메시지를 가져온다.

- EC2 1대일 때는 정상 동작, **2대 이상에서만 발생**

#### 원인

WebSocket(STOMP) 세션은 **서버 메모리에 바인딩**된다.

ALB가 유저A를 EC2 #1로, 유저B를 EC2 #2로 라우팅하면, `SimpMessagingTemplate.convertAndSend()`는 **해당 서버의 WebSocket 세션에만** 메시지를 전달한다.


#### 해결: Redis Pub/Sub 도입

모든 EC2 인스턴스가 동일한 Redis 채널(`chat:messages`)을 구독하도록 변경했다. 어떤 서버에서 발행하든 전체 인스턴스가 수신하여 자기에게 연결된 클라이언트에게 전달한다.

```
유저A → EC2 #1 → DB 저장 → Redis PUBLISH ("chat:messages")
                                    ↓
                    EC2 #1 SUBSCRIBE → EC2 #1의 WebSocket 클라이언트에게 전달
                    EC2 #2 SUBSCRIBE → 유저B 실시간 수신 
```

---

### Cache

캐시 이름, TTL

| `tradeList` , 5분 | 메인 화면 (중고 거래 리스트) 캐싱, 사용자가 중고 거래 글을 새로 등록하거나 수정, 삭제할 경우 CacheEvict

| `trade` , 10분 | 중고거래 상세 페이지 - 사용자가 직접 작성한 중고 거래 글을 수정, 삭제할 경우 CacheEvict

| `tradeCount` , 10분 | 중고거래 개수 - 페이징 처리 전용

| `bookClubList` , 10분 | 독서모임 목록 (독서모임 리스트) 캐싱, 독서모임 이용자가 독서 모임 글을 새로 등록하거나 수정, 삭제할 경우 CacheEvict

| `bookClub` | 10분 | 독서모임 상세 페이지 - 사용자가 직접 작성한 독서모임 글을 수정, 삭제할 경우 CacheEvict

캐싱 도입 - DB 부하 분산 절감

<img width="954" height="513" alt="ㅁㄴㅇㄹㅁㄴㅇㄹ" src="https://github.com/user-attachments/assets/9e9098e9-5807-48c0-898c-de0ac7c7952c" />

---

## 외부 API

| **Toss Payments** | 결제 승인/취소 |
| **카카오 책 검색** | 도서 정보 조회 |
| **카카오 OAuth** | 소셜 로그인 |
| **네이버 OAuth** | 소셜 로그인 |
| **Gmail SMTP** | 이메일 발송 (회원가입 인증, 정산 알림) |


| `tossPaymentWebClient` : Toss API | 결제 승인 (Basic Auth)
| `kakaoAuthWebClient` : `kauth.kakao.com` | 카카오 OAuth 토큰
| `kakaoApiWebClient` : `kapi.kakao.com` | 카카오 사용자 정보
| `naverAuthWebClient` : `nid.naver.com` | 네이버 OAuth 토큰
| `naverApiWebClient` : `openapi.naver.com` | 네이버 사용자 정보
| `kakaoBookWebClient` : 카카오 도서 검색 API | 책 검색

---

## 예외 계층화

```
InvalidRequestException (400)
- SettlementException
- FileUploadException
- BookClubInvalidRequestException
ForbiddenException (403)
NotFoundException (404)
- TradeNotFoundException
- BookClubNotFoundException
ClientException (4xx - 400.jsp)
ServerException (5xx — 500.jsp)
```

`GlobalExceptionHandler`에서 `ClientException`(4xx)과 `ServerException`(5xx) 두 개로 단순 처리하고, 에러 페이지는 `400.jsp`와 `500.jsp` 두 개만 사용한다.

---

## 이미지 저장 - 파일 업로드 & S3, CloudFront

이미지 : ImgService 인터페이스
- 구현체 S3Service, FileUploadService
- S3Service implements ImgService : AWS S3에 업로드할 때 사용
- FileUploadService implements ImgService : 로컬 개발 시 사용, application.properties에서 file.dir=mvc/img


---
## Video
https://youtu.be/bUNw9EWqAn8?si=i-8td0puum18mk9-
