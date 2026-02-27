# 테스트 코드 가이드

## 1. 실무에서 테스트 코드를 짜는 방식

### 테스트 피라미드

```
        /  E2E  \          ← 적게 (느림, 비용 높음)
       /----------\
      / Integration \      ← 적당히
     /----------------\
    /    Unit Test      \  ← 가장 많이 (빠름, 비용 낮음)
```

| 종류 | 대상 | 특징 |
|------|------|------|
| **Unit Test** | Service, 유틸 메서드, Batch 컴포넌트, VO 도메인 로직 | 외부 의존성을 Mockito로 대체. 가장 빠르고 가장 많이 작성 |
| **Integration Test** | Mapper(DB), Controller(HTTP) | 실제 DB(H2)/MockMvc로 실제 쿼리·HTTP 흐름 검증 |
| **E2E Test** | 전체 흐름 | 브라우저 시뮬레이션. 실무에서도 적게 작성 |

### 실무 원칙

1. **Service 계층을 가장 많이 테스트한다** - 비즈니스 로직이 집중된 곳
2. **Mapper(Repository)는 통합 테스트로** - 쿼리가 실제로 동작하는지 확인
3. **Controller는 MockMvc standaloneSetup** - Spring Context 없이 빠르게 요청/응답 검증
4. **모든 메서드를 테스트하지 않는다** - 단순 위임 메서드(getter/setter)는 제외, 분기/검증/계산 로직 위주

### 테스트 이름 작성 규칙

DisplayName에 한글로 명확히 기술한다:

```java
@DisplayName("잔액 부족 → InsufficientBalanceException → 두 테이블 INSUFFICIENT_BALANCE 갱신")
@DisplayName("판매자가 아닌 사용자의 정산 신청 - ForbiddenException")
```

---

## 2. 현재 프로젝트 테스트 환경

### 의존성 (pom.xml)

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| `spring-test` | 5.3.39 | Spring 통합 테스트 / MockMvc |
| `junit-jupiter-api` | 5.13.4 | @Test, @Nested, @DisplayName |
| `mockito-core` | 5.8.0 | @Mock, @InjectMocks, when/verify |
| `mockito-junit-jupiter` | 5.8.0 | @ExtendWith(MockitoExtension.class) |
| `assertj-core` | 3.27.3 | assertThat() 가독성 높은 검증 |
| `hamcrest` | 2.2 | MockMvc model() 검증 |
| `jsonassert` | 1.5.3 | content().json() 검증 |
| `json-path` | 2.9.0 | jsonPath() 응답 필드 검증 |
| `h2` | 2.2.224 | Mapper 통합 테스트용 인메모리 DB |
| `spring-batch-core` | 4.3.9 | 배치 컴포넌트 단위 테스트 |

### 전체 테스트 현황

**총 363개 테스트 — 모두 통과 (BUILD SUCCESS)**

---

## 3. 테스트 파일 목록 및 현황

### Domain / VO (Unit Test)

| 파일 | 테스트 수 | 상태 |
|------|-----------|------|
| `member/MemberVoValidationTest` | 13 | ✅ 완료 |
| `trade/TradeVoTest` | 9 | ✅ 완료 |
| `util/paging/PageResultTest` | 4 | ✅ 완료 |

### Controller 계층 (MockMvc)

| 파일 | 테스트 수 | 상태 |
|------|-----------|------|
| `member/MemberControllerTest` | 15 | ✅ 완료 |
| `trade/TradeControllerTest` | 11 | ✅ 완료 |
| `settlement/SettlementControllerTest` | 8 | ✅ 완료 |

### Mapper / Repository 계층 (H2 통합 테스트)

| 파일 | 테스트 수 | 상태 |
|------|-----------|------|
| `member/MemberMapperTest` | 16 | ✅ 완료 |
| `member/MemberBankAccountMapperTest` | 6 | ✅ 완료 |
| `settlement/SettlementMapperTest` | 17 | ✅ 완료 |

### Service 계층 (Unit Test)

| 파일 | 테스트 수 | 상태 |
|------|-----------|------|
| `trade/TradeServiceTest` | 38 | ✅ 완료 |
| `member/MemberServiceTest` | 28 | ✅ 완료 |
| `bookclub/BookClubServiceTest` | 49 | ✅ 완료 |
| `chat/chatroom/ChatroomServiceTest` | 23 | ✅ 완료 |
| `admin/AdminServiceTest` | 51 | ✅ 완료 |
| `settlement/SettlementServiceTest` | 17 | ✅ 완료 |
| `member/MemberBankAccountServiceTest` | 4 | ✅ 완료 |

### 배치 계층 (Unit Test + 통합 시나리오)

| 파일 | 테스트 수 | 상태 |
|------|-----------|------|
| `batch/SettlementItemWriterTest` | 8 | ✅ 완료 |
| `batch/SettlementItemProcessorTest` | 3 | ✅ 완료 |
| `batch/SettlementSkipListenerTest` | 5 | ✅ 완료 |
| `batch/SettlementConcurrencyTest` | 5 | ✅ 완료 |
| `batch/SettlementBatchIntegrationTest` | 5 | ✅ 완료 |

### 유틸리티 (Unit Test)

| 파일 | 테스트 수 | 상태 |
|------|-----------|------|
| `util/AesEncryptionUtilTest` | 5 | ✅ 완료 |
| `util/imgUpload/FileUploadServiceTest` | 19 | ✅ 완료 |

---

## 4. 테스트 커버 범위 상세

### MemberVoValidationTest (13개)

| 분류 | 수 | 내용 |
|------|----|------|
| SignUpGroup | 8 | loginId blank/짧음/긺, password 짧음/긺, email 형식, nickname blank, 정상 가입 |
| UpdateGroup | 3 | telNo 짧음/숫자아님/null 허용 |
| 그룹 분리 | 2 | UpdateGroup 어노테이션이 SignUpGroup에서 미작동 |

### TradeVoTest (9개)

| 분류 | 수 | 내용 |
|------|----|------|
| checkTradeVO | 7 | 모든 필드 있음/각 필드 null/sale_cont 499자/500자 |
| generateBook | 2 | 정확한 매핑/String null 필드 방어 |

### PageResultTest (4개)
- 생성자 값 저장, 빈 리스트, 0 total, 1페이지

### MemberControllerTest (15개)

| 분류 | 수 | 내용 |
|------|----|------|
| GET /login | 2 | OAuth ID 모델 포함, redirect 파라미터 |
| POST /login | 2 | 성공 redirect / 실패 뷰 |
| GET /signup | 1 | 뷰 반환 |
| AJAX 중복 체크 | 3 | idCheck/emailCheck/nicknmCheck (1/0) |
| GET /api/session-check | 2 | 로그인됨/비로그인 |
| POST /auth/ajax/findId | 2 | 찾음/실패 |
| POST /auth/ajax/resetPassword | 3 | 성공/세션없음/ID불일치 |
| POST /member/delete | 2 | 성공/실패 |

### TradeControllerTest (11개)

| 분류 | 수 | 내용 |
|------|----|------|
| GET /trade/{tradeSeq} | 2 | 비로그인(wished false)/로그인(wished true) |
| POST /trade/like | 2 | 찜 추가/취소 |
| POST /trade/sold | 2 | 성공/실패 |
| POST /trade/confirm/{trade_seq} | 2 | 성공/이미 확정 |
| GET /trade/book | 2 | 결과 있음/빈 배열 |
| 기타 | 1 | bookApiService.searchBooks 호출 검증 |

### SettlementControllerTest (8개)

| 분류 | 수 | 내용 |
|------|----|------|
| POST /settlement/request | 5 | 성공/세션없음/SettlementException/ForbiddenException/false |
| GET /settlement/{trade_seq} | 3 | 세션없음/내역있음/내역없음 |

### MemberMapperTest (16개)

| 분류 | 수 | 내용 |
|------|----|------|
| signUp | 1 | 반환값 1 |
| findByLoginId | 3 | 존재/미존재/탈퇴회원 null |
| 중복 체크 | 6 | id/email/nickNm 각각 존재/미존재 |
| updatePassword | 2 | 성공/없는ID 반환0 |
| updateMember | 1 | 닉네임·전화번호 수정 |
| deleteMember | 1 | soft delete + findByLoginId null |
| findIdByTel | 2 | 찾음/null |
| checkUserByIdAndEmail | 2 | 일치/불일치 |
| loginLogUpdate | 1 | 반환값 1 |

### MemberBankAccountMapperTest (6개)

| 분류 | 수 | 내용 |
|------|----|------|
| findByMemberSeq | 2 | null/등록 후 조회 |
| insert | 2 | 반환값 1 + PK, verified_yn=0 |
| update | 2 | 필드 변경, verified_yn 초기화 |

### SettlementMapperTest (17개)

| 분류 | 수 | 내용 |
|------|----|------|
| insertSettlement | 2 | 반환값 1 + PK, 기본상태 REQUESTED |
| countByStatus | 2 | REQUESTED 2건 ≥2, COMPLETED 0건 |
| updateToCompleted | 2 | REQUESTED→COMPLETED / 이미COMPLETED→0 (멱등성) |
| updateToInsufficient | 2 | REQUESTED→INSUFFICIENT / COMPLETED→0 |
| resetToRequested | 2 | INSUFFICIENT→REQUESTED / REQUESTED→0 |
| confirmTransfer | 2 | 미확인→confirmed=true / 이미확인→0 |
| sumTransferPending | 2 | 2건 합산 / 없으면 0 (COALESCE) |
| getAdminBalance | 1 | 500000 반환 (FOR UPDATE) |
| updateAdminBalance | 1 | 차감 후 재조회 |
| insertAccountLog | 1 | 반환값 1 |
| updateTradeSettlementSt | 1 | sb_trade_info.settlement_st 변경 |

### TradeServiceTest (38개)

| 분류 | 수 | 내용 |
|------|----|------|
| validateSellerOwnership | 3 | 본인/타인/null 거래 |
| validateBuyerOwnership | 4 | 본인/타인/구매자 없음/null 거래 |
| validateCanModify | 5 | NONE/PENDING/COMPLETED/타인/null |
| validateCanDelete | 5 | NONE/PENDING/COMPLETED/타인/null |
| search | 3 | 이미지 있음/없음/거래 없음 |
| upload | 3 | 이미지 포함/미포함/실패 |
| saveLike | 2 | 찜 추가/취소 토글 |
| SafePayment | 5 | 요청/취소/만료시간/null/리셋 |
| confirmPurchase | 2 | 정상 확정/이미 확정 |
| updateToSoldManually | 2 | 성공/실패 |
| searchAllWithPaging | 4 | offset 계산 (1페이지, 3페이지, 음수, 큰값) |
| successPurchase | 1 | 파라미터 전달 검증 |
| autoConfirmExpiredPurchases | 1 | 자동 확정 건수 |

### MemberServiceTest (28개)

| 분류 | 수 | 내용 |
|------|----|------|
| signUp | 2 | BCrypt 암호화 저장, 가입 실패 |
| login | 6 | BCrypt 정상/불일치/없는ID, MD5 마이그레이션/불일치, $2b$ prefix |
| DuplicateCheck | 4 | id/email/nickNm: 중복/사용가능 |
| resetPassword | 4 | 정상 재설정/이전 비밀번호 동일/없는 회원/DB 실패 |
| checkUserByIdAndEmail | 2 | 존재/미존재 |
| findIdByTel | 2 | 단순 위임 |
| processSocialLogin | 4 | 기존 회원/탈퇴 회원/신규 가입/닉네임 중복 난수 |
| deleteMember | 2 | Trade+BookClub+회원 순 탈퇴/모임 없는 회원 |
| updateMember | 2 | 정상 수정/실패 |

### BookClubServiceTest (49개)

| 분류 | 수 | 내용 |
|------|----|------|
| createJoinRequest | 5 | 정상/이미 가입/대기중/null/DB UNIQUE 동시성 |
| approveJoinRequest | 7 | 신규 승인/재가입/정원 초과/이미 JOINED/이미 처리됨/삭제된 모임/null |
| rejectJoinRequest | 3 | 정상 거절/이미 처리됨/null |
| kickMember | 5 | 정상/모임장 강퇴 방지/비가입 멤버/존재하지 않는 멤버/null |
| leaveBookClub | 6 | 일반 탈퇴/모임장 승계/마지막 멤버 종료/이미 탈퇴/존재하지 않는 멤버/null |
| createBookClub | 3 | 배너 포함/기본이미지/이름 중복 |
| 멤버 상태 | 6 | isMemberJoined/isLeader (null 방어 포함) |
| 토글 | 5 | 찜하기/좋아요 추가/취소 + ToggleBoardLike |
| 댓글 | 4 | 수정(성공/빈값)/삭제(성공/null) |
| null 방어 | 5 | memberCount/wishCount/likeCount/recentBoards/boardDetail |

### ChatroomServiceTest (23개)

| 분류 | 수 | 내용 |
|------|----|------|
| searchAll | 2 | 목록 반환/빈 리스트 |
| findOrCreateRoom | 4 | 기존 반환/신규 생성/save 실패/DuplicateKey 동시성 |
| isMemberOfChatroom | 2 | true/false |
| findChatRoomSeqByTradeAndBuyer | 2 | 존재/null |
| isBuyerOfTrade | 2 | true/false |
| findByChatRoomSeq | 1 | 조회 |
| searchAllWithPaging | 1 | 페이징 |
| countAll | 1 | 전체 개수 |
| updateLastMessage | 10 | 일반/null·빈값 무시/[IMAGE]/[SAFE_PAYMENT]/50자 초과 잘림 |

### AdminServiceTest (51개)

| 분류 | 수 | 내용 |
|------|----|------|
| login | 3 | BCrypt 정상/불일치/없는 ID |
| statistics | 3 | countAllMembers/Trades/BookClubs |
| search | 6 | Members/Trades/SafePayList/BookClubs/AdminLogs/UsersLogs |
| handleMemberAction | 4 | BAN/ACTIVE(→JOIN)/DELETE/알 수 없는 액션 |
| handleTradeAction | 4 | DELETE/BAN/SALE/기타 |
| recentLists | 3 | getRecentMembers/Trades/BookClubs |
| loginLogout | 6 | 관리자/회원 로그인·로그아웃 |
| bannerManagement | 3 | 조회/저장/삭제 |
| tempPageManagement | 2 | 저장/조회 |
| noticeManagement | 7 | 저장/검색/단건조회/조회수증가/삭제/수정/활성 |
| deleteBookClub, getChartData | 2 | 개별 |
| countBySearch | 10 | 8개 countBy 메서드 전체 |

### SettlementServiceTest (17개)

| 분류 | 수 | 내용 |
|------|----|------|
| requestSettlement | 6 | 정상 신청/비판매자/안전결제 미완료/구매확정 미완료/이미 신청됨/없는 거래 |
| findTransferPending | 3 | 복호화 후 반환/빈 리스트/총액 |
| confirmTransfer | 2 | 정상/이미 확인됨 |
| markAsInsufficient | 2 | 정상 갱신/다른 seq와 혼동 없음 |

### Batch 테스트

**SettlementItemWriterTest (8개)**

| 분류 | 수 | 내용 |
|------|----|------|
| WriteSuccess | 3 | 계좌 있음/계좌 없음/복수건 순차 |
| WriteInsufficientBalance | 2 | 예외 발생/경계값(잔액==금액) |
| WriteNoAdminAccount | 1 | null 잔액 → IllegalStateException |

**SettlementItemProcessorTest (3개)**
- 계좌번호 복호화/null 통과/동일 인스턴스 반환

**SettlementSkipListenerTest (5개)**
- InsufficientBalanceException → markAsInsufficient 호출
- 다른 예외 → 미호출
- IllegalStateException → 미호출
- onSkipInRead/onSkipInProcess → no-op

**SettlementConcurrencyTest (5개)**
- 두 번째 chunk가 최신 잔액 재조회
- updateToCompleted=0 → IllegalStateException
- 경계값: 잔액==금액 → 성공
- 경계값: 잔액==금액-1 → InsufficientBalanceException
- 멀티스레드 이중 차감 없음 (FOR UPDATE 시뮬레이션)

**SettlementBatchIntegrationTest (5개)**
- 잔액 부족 → 두 테이블 INSUFFICIENT_BALANCE 갱신
- 잔액 부족 → updateToCompleted 미호출
- 정상 처리 → SkipListener 미호출
- 정상 처리 → 계좌 정보 로그 포함
- 동시 배치 감지 → IllegalStateException

### Util 테스트

**AesEncryptionUtilTest (5개)**
- 암호화→복호화 round-trip
- 같은 평문이라도 암호화값이 매번 다름 (랜덤 IV)
- 최대 길이 계좌번호 처리
- 잘못된 형식 → IllegalArgumentException
- 잘못된 키 크기 → IllegalArgumentException

**FileUploadServiceTest (19개)**
- storeFiles: 이미지 포함/미포함
- uploadFile: 8가지 케이스 (성공/빈파일/null/오류 등)
- deleteByUrl: 9가지 케이스 (성공/null/공백/S3 없음 등)

**MemberBankAccountServiceTest (4개)**
- save: 신규 등록 시 암호화/수정 시 암호화
- getByMemberSeq: 복호화 후 반환/계좌 없으면 null

---

## 5. H2 Mapper 통합 테스트 구조

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMapperConfig.class)  // H2 + MyBatis 설정
@Transactional  // 각 테스트 후 자동 롤백
class SomeMapperTest {

    @Autowired
    SomeMapper someMapper;

    @Autowired
    DataSource dataSource;

    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        // 사전 데이터 삽입
        jdbc.execute("INSERT INTO some_table ...");
    }

    @Test
    @DisplayName("설명")
    void test() {
        int result = someMapper.insert(vo);
        assertThat(result).isEqualTo(1);
    }
}
```

**TestMapperConfig** (`src/test/java/project/config/TestMapperConfig.java`):
- H2 인메모리 DB (`MODE=MySQL`로 MySQL 문법 호환)
- `schema-h2.sql` 자동 실행 (테이블 생성)
- MyBatis `SqlSessionFactory` + `classpath:project/**/*Mapper.xml`
- `DataSourceTransactionManager`

---

## 6. Controller 단위 테스트 구조 (MockMvc)

```java
@ExtendWith(MockitoExtension.class)
class SomeControllerTest {

    @Mock SomeService someService;

    @InjectMocks
    SomeController someController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // @Value 필드 주입 (Spring Context 없이)
        ReflectionTestUtils.setField(someController, "someField", "testValue");
        mockMvc = MockMvcBuilders.standaloneSetup(someController).build();
    }

    @Test
    void test() throws Exception {
        when(someService.doSomething(1L)).thenReturn(true);

        mockMvc.perform(post("/some/endpoint").session(loginSession(1L)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    private MockHttpSession loginSession(long memberSeq) {
        MemberVO member = new MemberVO();
        member.setMember_seq(memberSeq);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginSess", member);
        return session;
    }
}
```

---

## 7. Unit Test 기본 구조

```java
@ExtendWith(MockitoExtension.class)  // Spring Context 없이 Mockito만 사용
class SomeServiceTest {

    @Mock
    SomeMapper someMapper;            // 가짜 객체 (DB 호출 안 함)

    @InjectMocks
    SomeService someService;          // Mock들이 주입된 실제 Service

    @Test
    @DisplayName("설명")
    void 메서드명_조건_기대결과() {
        // given - 테스트 데이터 준비
        when(someMapper.findById(1L)).thenReturn(new SomeVO());

        // when - 실행
        SomeVO result = someService.findById(1L);

        // then - 검증
        assertThat(result).isNotNull();
        verify(someMapper).findById(1L);
    }
}
```

### 자주 쓰는 AssertJ 검증

```java
assertThat(result).isNotNull();
assertThat(result).isEqualTo(expected);
assertThat(result).isTrue();
assertThat(list).hasSize(3);
assertThat(list).isEmpty();

// 예외 검증
assertThatThrownBy(() -> service.method())
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("키워드");

// 예외 없음 검증
assertThatCode(() -> service.method())
        .doesNotThrowAnyException();

// 특정 타입 예외 잡기
InsufficientBalanceException ex = catchThrowableOfType(
        () -> writer.write(items), InsufficientBalanceException.class);
assertThat(ex).isNotNull();
```

---

## 8. 테스트 실행 방법

```bash
# 전체 테스트 실행
mvn test

# 특정 클래스만
mvn test -Dtest=SettlementServiceTest

# 특정 중첩 클래스만
mvn test -Dtest="SettlementServiceTest$RequestSettlement"

# Mapper 통합 테스트만
mvn test -Dtest="MemberMapperTest,MemberBankAccountMapperTest,SettlementMapperTest"
```

---

## 9. 핵심 정리

| 항목 | 설명 |
|------|------|
| **테스트 총 수** | 363개 (모두 통과) |
| **테스트 계층** | VO 도메인 + Controller(MockMvc) + Mapper(H2) + Service(Mockito) + Batch |
| **집중 영역** | Service 계층 (비즈니스 로직) + Batch 컴포넌트 + Mapper SQL 검증 |
| **Mock 대상** | Mapper, 외부 서비스(S3, AES), Batch 의존성 |
| **H2 호환** | `MODE=MySQL` + `NON_KEYWORDS=VALUE` + VARCHAR(ENUM 대신) |
| **패턴** | given-when-then |
| **이름 규칙** | @DisplayName에 한글로 명확히 기술 |
| **검증 도구** | AssertJ + Mockito + MockMvc + JdbcTemplate(H2) |
| **우선순위** | 돈/권한 > 배치 정합성 > 비즈니스 분기 > 단순 위임 |
