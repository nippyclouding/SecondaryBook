# 모바일 앱 개발 계획 (Android)

---

## 1. 구조 개요 — 서버 vs 앱

**Kotlin으로 서버를 새로 만드는 게 아니다.**
기존 Spring(Java) 백엔드가 서버 역할을 그대로 유지하고,
Kotlin으로는 사용자 폰에서 실행되는 Android 앱(클라이언트)을 만든다.

```
[지금 이미 있는 것 — 서버]

Spring (Java) 백엔드
  ├── MySQL, Redis, S3, Toss API, WebSocket 등
  └── https://www.shinhan6th.com 에서 실행 중
       ↑ 이 서버는 건드리지 않는다 (최소 수정)

[새로 만드는 것 — 앱 (클라이언트)]

Android 앱 (Kotlin)
  ├── 사용자 폰에서 실행된다
  ├── 서버에 HTTP 요청을 보내고 JSON 응답을 받는다
  └── 받은 데이터를 Jetpack Compose로 화면에 그린다
```

| | 역할 | 언어 | 실행 위치 |
|---|---|---|---|
| **기존 Spring** | 서버 — 데이터 처리, DB, 비즈니스 로직 | Java | 클라우드 서버 |
| **Android 앱** | 클라이언트 — 화면 표시, 사용자 입력 처리 | Kotlin | 사용자 폰 |

---

## 2. 앱 구현 범위

앱은 **사용자(판매자/구매자)가 사용하는 기능 전체**를 구현한다.
관리자 전용 기능은 앱에서 만들지 않는다.

```
[앱에서 구현하는 것]               [앱에서 만들지 않는 것]
────────────────────────────       ──────────────────────────
로그인 / 회원가입 / OAuth           관리자 대시보드
도서 거래 목록 / 상세 / 등록        관리자 정산 승인 / 재처리
찜하기                              관리자 잔액 충전
실시간 채팅                         관리자 회원 관리
안전결제 (Toss Payments)            관리자 강제 로그아웃
구매확정
정산 신청 / 내역 조회
독서 모임 (목록, 상세, 가입)
마이페이지 (프로필, 계좌 관리)
```

로그인/회원가입만 짜는 게 아니라, 사용자가 쓰는 기능 전부가 구현 대상이다.

---

## 3. 기술 스택

```
[Android 앱 — 새로 만드는 것]

언어         : Kotlin
화면(UI)     : Jetpack Compose  ← 화면을 Kotlin 코드로 선언적으로 작성
HTTP 통신    : Retrofit2 + OkHttp  ← 서버 API 호출
WebSocket    : Krossbow (STOMP over WebSocket, Kotlin Coroutine 지원)
이미지 로딩  : Coil (S3/CloudFront URL을 그대로 받아서 표시)
OAuth 로그인 : Kakao Android SDK / Naver Login SDK for Android
결제         : Toss Payments Android SDK
의존성 주입  : Hilt
네비게이션   : Navigation Compose
빌드 툴      : Gradle (Kotlin DSL)

[기존 Spring 백엔드 — 유지]

언어         : Java 17
프레임워크   : Spring MVC 5.3
인증         : Redis 세션 (Cookie) + OAuth
실시간       : WebSocket / STOMP + Redis Pub/Sub
결제         : Toss Payments API
DB           : MySQL + MyBatis
파일 저장    : AWS S3 + CloudFront
```

---

## 4. 화면 구성 — Jetpack Compose

기존 웹(JSP)과 앱(Compose)의 화면 구성 방식이 다르다.

```
[기존 JSP — 서버가 HTML을 만들어서 브라우저에 전달]

서버 → HTML 생성 → 브라우저가 표시

[Jetpack Compose — 서버는 JSON만 주고, 앱이 직접 화면을 그림]

서버 → JSON 데이터 → 앱이 Compose 코드로 화면 구성
```

Compose에서는 화면을 `@Composable` 함수로 작성한다.

```kotlin
// 거래 목록 화면
@Composable
fun TradeListScreen(viewModel: TradeViewModel = hiltViewModel()) {
    val trades by viewModel.trades.collectAsState()  // 서버 데이터 구독

    LazyColumn {                    // 스크롤 가능한 목록
        items(trades) { trade ->
            TradeItem(trade)        // 각 거래 카드
        }
    }
}

// 거래 카드 하나
@Composable
fun TradeItem(trade: TradeVO) {
    Row {
        AsyncImage(trade.imageUrl)  // S3 이미지를 URL로 바로 로딩 (Coil)
        Column {
            Text(trade.sale_title)
            Text("${trade.sale_price}원")
        }
    }
}
```

---

## 5. 데이터 흐름

화면 터치부터 서버 응답이 화면에 반영되기까지의 전체 흐름이다.

```
[사용자가 화면 터치]
        ↓
Composable (화면) — Jetpack Compose로 작성
        ↓
ViewModel — 비즈니스 로직, 상태 관리
        ↓
Repository
        ↓
Retrofit2 — HTTP 요청 (JSON)
        ↓
  ─── 인터넷 ───
        ↓
Spring 백엔드 (기존 Java 서버)
        ↓
MySQL / Redis / S3 등
        ↑
  JSON 응답 반환
        ↑
  ─── 인터넷 ───
        ↑
Retrofit2 — 응답 파싱
        ↑
ViewModel — 상태 업데이트
        ↑
Composable — 화면 자동 갱신
```

ViewModel의 상태가 바뀌면 Compose가 해당 부분만 자동으로 다시 그린다.
직접 View를 찾아서 업데이트하는 기존 방식(`findViewById`)과 다르다.

---

## 6. Android Studio 도입 적절성

### 결론: 적절하다

| 검토 항목 | 현재 기술 | Android 대응 | 호환성 |
|-----------|-----------|--------------|--------|
| 백엔드 언어 | Java 17 | Kotlin (JVM 동일) | ✅ 문법 유사, 상호운용 가능 |
| HTTP 통신 | Spring MVC REST | Retrofit2 + OkHttp | ✅ 동일 JSON 응답 재사용 |
| 실시간 채팅 | WebSocket + STOMP | Krossbow | ✅ 프로토콜 동일 |
| OAuth 로그인 | Kakao / Naver | 공식 Android SDK | ✅ 별도 서버 구축 불필요 |
| 결제 | Toss Payments API | Toss Payments Android SDK | ✅ 별도 서버 구축 불필요 |
| 이미지 | AWS S3 + CloudFront | Coil (CDN URL 그대로 사용) | ✅ 추가 작업 없음 |
| 인증 | Redis 세션 (Cookie) | OkHttp CookieJar로 쿠키 유지 | ✅ 기존 세션 재사용 가능 |
| 뷰 | JSP (서버 렌더링) | Jetpack Compose (앱 렌더링) | ✅ JSP는 웹 전용, 앱은 별도 UI |
| CSRF | Spring Security 전역 활성화 | 모바일 API 경로 예외 처리 | ⚠️ 백엔드 소폭 수정 필요 |

### 기존 백엔드를 그대로 활용할 수 있는 이유

```
① 대부분의 Controller가 이미 @ResponseBody + JSON 응답을 지원한다.
   → 새로운 API 서버를 따로 만들 필요 없이 기존 엔드포인트를 앱에서 호출 가능.

② WebSocket/STOMP 인프라가 이미 구축되어 있다.
   → 채팅 기능을 Android에서 동일한 프로토콜로 연결 가능.

③ Redis 세션이 분산 저장소에 있다.
   → 웹과 앱이 동일한 세션을 공유할 수 있다.

④ Kakao/Naver OAuth, Toss Payments 모두 Android 공식 SDK가 있다.
   → 별도 결제/로그인 서버 구축 불필요.
```

---

## 7. 백엔드 수정 계획

앱을 위해 기존 백엔드에서 수정이 필요한 부분은 딱 2가지다.

---

### STEP B-1 · CSRF 예외 경로 추가

**파일**: `SecurityConfig.java`

앱은 브라우저처럼 CSRF 토큰을 자동으로 관리하지 않는다.
앱 전용 API 경로를 CSRF 예외로 등록해야 한다.

```
현재 CSRF 예외 경로:
  /chatEndPoint/**
  /health
  /admin/api/logout-pending

추가할 경로:
  /api/mobile/**    ← 앱 전용 API prefix
```

---

### STEP B-2 · 모바일 로그인 엔드포인트 추가

**파일**: `MemberController.java`

현재 `/login` (POST)는 로그인 성공 시 페이지 redirect를 반환한다.
앱은 redirect를 따라가지 않으므로 JSON 응답을 반환하는 별도 엔드포인트가 필요하다.

```
POST /api/mobile/login
  요청: { login_id, member_pwd }
  응답: { member_seq, member_nicknm, member_email }  + JSESSIONID 쿠키 발급
  실패: HTTP 401 + { message: "아이디 또는 비밀번호가 틀렸습니다." }
```

세션은 기존 Redis 세션을 그대로 사용한다.
앱의 OkHttp CookieJar가 JSESSIONID 쿠키를 저장하므로
이후 모든 API 호출에 인증이 자동으로 유지된다.

나머지 엔드포인트(거래, 채팅, 결제, 정산, 독서 모임)는 수정 없이 재사용 가능하다.

---

## 8. 앱 구현 계획

---

### PHASE 1 · 프로젝트 기본 구조 세팅

```
[Android Studio에서 신규 프로젝트 생성]

언어   : Kotlin
최소 API : 26 (Android 8.0)  ← 한국 시장 기준
UI 템플릿 : Empty Compose Activity

[폴더 구조]
app/
├── data/
│   ├── api/         ← Retrofit 인터페이스 (백엔드 API 호출 정의)
│   ├── model/       ← 데이터 클래스 (서버 VO와 대응)
│   └── repository/  ← API 호출 + 결과 가공
│
├── ui/
│   ├── auth/        ← 로그인, 회원가입, OAuth
│   ├── trade/       ← 도서 거래 목록, 상세, 등록
│   ├── chat/        ← 채팅방 목록, 채팅방 상세
│   ├── bookclub/    ← 독서 모임
│   ├── payment/     ← 결제
│   ├── mypage/      ← 마이페이지, 정산
│   └── common/      ← 공통 컴포넌트 (로딩, 에러, 이미지)
│
├── di/              ← Hilt 의존성 주입 모듈
├── util/            ← 날짜/금액 포맷 등 유틸
└── MainActivity.kt  ← Navigation Host (화면 전환 관리)
```

---

### PHASE 2 · 네트워크 레이어 구축

```
[Retrofit2 + OkHttp 설정]

Base URL   : https://www.shinhan6th.com       (운영)
           : http://10.0.2.2:8080             (에뮬레이터에서 로컬 서버 접근 시)
CookieJar  : PersistentCookieJar              (JSESSIONID 영구 유지)
Timeout    : Connect 10s / Read 30s / Write 30s
로깅       : HttpLoggingInterceptor            (DEBUG 빌드에서만)

[데이터 클래스 — 기존 VO와 대응]

data class TradeVO(
    val trade_seq: Long,
    val sale_title: String,
    val sale_price: Int,
    ...
)
→ 백엔드 JSON 키(snake_case)를 그대로 사용하거나 @SerializedName으로 매핑

[API 인터페이스 예시]

interface TradeApi {
    @GET("/trade/{trade_seq}")
    suspend fun getTradeDetail(@Path("trade_seq") tradeSeq: Long): TradeVO

    @GET("/trade")
    suspend fun searchTrades(@QueryMap params: Map<String, String>): PageResult<TradeVO>

    @Multipart
    @POST("/trade")
    suspend fun createTrade(@PartMap parts: Map<String, RequestBody>): Response<Void>
}
```

---

### PHASE 3 · 인증 구현

```
[일반 로그인]

① 앱 → POST /api/mobile/login { login_id, member_pwd }
② 서버 → 세션 발급 + JSON 응답 { member_seq, member_nicknm }
③ OkHttp CookieJar → JSESSIONID 쿠키 로컬 저장
④ 이후 모든 API → 쿠키 자동 첨부

[Kakao 로그인]

① Kakao Android SDK → 카카오 인증 → access_token 획득
② 앱 → 서버에 access_token 전달 (엔드포인트 추가 필요)
③ 서버 → 카카오 사용자 정보 조회 → 세션 발급 + JSON 응답

[Naver 로그인] — Kakao와 동일한 패턴

[자동 로그인]

앱 시작 시:
  GET /api/session-check → 200이면 메인 화면, 401이면 로그인 화면
  (기존 엔드포인트 재사용, 수정 불필요)
```

---

### PHASE 4 · 핵심 기능 구현

#### 4-1. 도서 거래

```
[화면]
- 거래 목록 (검색, 필터, 무한 스크롤)
- 거래 상세 (이미지 슬라이더, 찜하기, 채팅 시작, 결제)
- 거래 등록 (갤러리 사진 첨부, Kakao 도서 검색)
- 거래 수정 / 삭제

[기존 API 재사용]
GET  /trade/{trade_seq}          ← 상세 조회
POST /trade                      ← 등록
POST /trade/modify/{trade_seq}   ← 수정
POST /trade/like                 ← 찜하기
POST /trade/confirm/{trade_seq}  ← 구매 확정
```

#### 4-2. 실시간 채팅

```
[WebSocket 연결]

라이브러리 : Krossbow (STOMP over WebSocket, Kotlin Coroutine 지원)
엔드포인트 : wss://www.shinhan6th.com/chatEndPoint
구독       : /chatroom/{chat_room_seq}
발행       : /sendMessage/chat/{chat_room_seq}

[구현 흐름]
① 채팅방 입장 → STOMP Connect (JSESSIONID 쿠키 포함)
② /chatroom/{id} 구독
③ 메시지 입력 → /sendMessage/chat/{id} 발행
④ 구독 채널로 메시지 수신 → Compose UI 자동 갱신

[시스템 메시지 처리]
[SAFE_PAYMENT_REQUEST], [SAFE_PAYMENT_COMPLETED] 등
→ 일반 텍스트 말풍선과 구분되는 별도 카드 UI로 표시
```

#### 4-3. 결제 (Toss Payments)

```
[Toss Payments Android SDK]

① 결제 정보 준비 (금액, 주문 ID, 상품명)
② Toss SDK 호출 → SDK가 결제 위젯 화면을 띄워줌
③ 결제 성공 → paymentKey, orderId, amount 획득
④ 앱 → 서버 POST /payments/success?paymentKey=...&orderId=...&amount=...
⑤ 서버 → Toss API 검증 → DB 갱신 → JSON 응답 반환

기존 PaymentController를 JSON 응답만 추가하면 재사용 가능.
```

#### 4-4. 정산

```
[화면]
- 마이페이지 > 정산 내역 조회
- 정산 신청 버튼 (구매확정 완료 거래에서만 활성화)
- 정산 계좌 등록 / 수정

[기존 API 재사용]
POST /settlement/request/{trade_seq}  ← 정산 신청
GET  /settlement/{trade_seq}          ← 정산 내역 조회
```

#### 4-5. 독서 모임

```
[화면]
- 모임 목록 (검색, 카테고리 필터)
- 모임 상세 (멤버 목록, 게시글)
- 모임 생성 / 가입 신청

[기존 API 재사용]
GET  /bookclubs           ← 목록
GET  /bookclubs/{id}      ← 상세
POST /bookclubs/create    ← 생성
POST /bookclubs/{id}/join ← 가입 신청
```

---

### PHASE 5 · 알림 (FCM)

```
[Firebase Cloud Messaging 연동]

현재 백엔드에는 푸시 알림 인프라가 없다.
이메일 알림(잔액 부족)은 구현되어 있으나 앱 푸시는 별도 작업이 필요하다.

[추가 필요 작업]
① Firebase 프로젝트 생성 + google-services.json 앱에 추가
② 백엔드에 firebase-admin SDK 의존성 추가
③ FCM 토큰 등록 엔드포인트 추가 (앱 로그인 시 서버에 전달)
   POST /api/mobile/fcm-token { token }
④ 푸시 발송 시점 (백엔드에서 처리):
   - 채팅 메시지 수신 시
   - 구매자가 안전결제 완료 시 (판매자에게)
   - 구매확정 시 (판매자에게)
   - 정산 지연(INSUFFICIENT_BALANCE) 시
     → 현재 이메일로만 발송 중, FCM 추가 후 이메일 + 앱 푸시 이중 발송 가능
```

---

## 9. 구현 우선순위 로드맵

```
PHASE 1 (기반 세팅)
  ├── Android 프로젝트 생성 + 폴더 구조
  ├── Retrofit2 + OkHttp + CookieJar 설정
  └── 백엔드 CSRF 예외 + 모바일 로그인 엔드포인트 추가

PHASE 2 (인증)
  ├── 일반 로그인 / 로그아웃
  ├── Kakao OAuth 로그인
  ├── Naver OAuth 로그인
  └── 자동 로그인 (JSESSIONID 쿠키 유지)

PHASE 3 (핵심 기능)
  ├── 도서 거래 목록 / 상세 / 등록
  ├── 찜하기
  └── 실시간 채팅 (WebSocket/STOMP)

PHASE 4 (결제 + 정산)
  ├── Toss Payments 안전결제
  ├── 구매확정
  └── 정산 신청 / 조회

PHASE 5 (부가 기능)
  ├── 독서 모임
  ├── 마이페이지 (프로필, 계좌 관리)
  └── FCM 푸시 알림

PHASE 6 (완성도)
  ├── 이미지 업로드 최적화 (압축 후 S3)
  ├── 다크모드
  └── 오프라인 캐싱 (Room DB)
```

---

## 10. 관련 파일 위치

```
[백엔드 — 수정 대상 (2개만)]
config/SecurityConfig.java      ← CSRF 예외 경로 추가 (/api/mobile/**)
member/MemberController.java    ← POST /api/mobile/login 엔드포인트 추가

[백엔드 — 재사용 (수정 불필요)]
trade/TradeController.java           ← 거래 조회/등록/수정/삭제/찜하기
payment/PaymentController.java       ← Toss 결제 처리
settlement/SettlementController.java ← 정산 신청/조회
chat/StompController.java            ← WebSocket 메시지 처리
bookclub/BookClubController.java     ← 독서 모임

[앱 — 신규 생성 (별도 레포지토리 권장)]
app/src/main/java/com/secondhandbooks/
  ├── data/api/        ← Retrofit 인터페이스
  ├── data/model/      ← 데이터 클래스 (서버 VO 대응)
  ├── data/repository/ ← Repository
  ├── ui/              ← Jetpack Compose 화면
  └── di/              ← Hilt 모듈
app/build.gradle.kts   ← 의존성 선언
```

---

## 11. 앱 의존성 목록 (build.gradle.kts)

```kotlin
dependencies {
    // HTTP 통신
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 실시간 채팅 (STOMP over WebSocket)
    implementation("org.hildan.krossbow:krossbow-stomp-core:7.1.0")
    implementation("org.hildan.krossbow:krossbow-websocket-okhttp:7.1.0")

    // 이미지 로딩 (S3/CloudFront URL 그대로 사용)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // OAuth 로그인
    implementation("com.kakao.sdk:v2-user:2.20.6")
    implementation("com.navercorp.nid:oauth:5.10.0")

    // 결제
    implementation("com.tosspayments.paymentsdk:payment-sdk:1.8.3")

    // 의존성 주입
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // FCM 푸시 알림
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Jetpack
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
}
```
