# 코드 컨벤션

> `trade` 패키지를 기준으로 확립한 프로젝트 전체 코드 컨벤션.
> 모든 패키지는 이 문서를 따른다.

---

## 1. 네이밍 규칙

### 1-1. 클래스명 — PascalCase + 역할 접미사

| 역할 | 접미사 | 예시 |
|------|--------|------|
| 엔티티/값 객체 | `VO` | `TradeVO`, `TradeImageVO` |
| MyBatis 매퍼 | `Mapper` | `TradeMapper`, `BookImgMapper` |
| 서비스 | `Service` | `TradeService` |
| 컨트롤러 | `Controller` | `TradeController` |
| Enum | 접미사 없음 | `SaleStatus`, `BookStatus`, `PaymentType` |

### 1-2. 필드명 — DB 매핑 필드는 snake_case

```java
// DB 컬럼과 1:1 매핑되는 필드 → snake_case
private long trade_seq;
private long member_seller_seq;
private String sale_title;
private LocalDateTime crt_dtm;

// DB와 무관한 임시 필드 → camelCase
private List<MultipartFile> uploadFiles;
private List<String> keepImageUrls;
```

### 1-3. 지역변수 / 파라미터 — camelCase

```java
TradeVO findTrade = tradeMapper.findBySeq(trade_seq);
List<String> imgUrls = imgService.storeFiles(uploadFiles);
MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);
```

### 1-4. 메서드명 — camelCase, 동사로 시작

| 동작 | 네이밍 패턴 | 예시 |
|------|------------|------|
| 단건 조회 | `search`, `findByXxx` | `search(long trade_seq)`, `findBySeq()` |
| 목록 조회 | `searchAllWithXxx`, `selectXxx` | `searchAllWithPaging()` |
| 등록 | `upload`, `save` | `upload(TradeVO tradeVO)` |
| 수정 | `modify`, `update` | `modify(Long trade_seq, TradeVO updateTrade)` |
| 삭제 | `remove`, `delete` | `remove(Long trade_seq)` |
| 검증 | `validateXxx` | `validateSellerOwnership()` |
| 개수 | `countXxx` | `countAll()`, `countLike()` |
| 토글 | `saveXxx` (존재하면 삭제, 없으면 생성) | `saveLike()` |

### 1-5. Enum 값 — UPPER_SNAKE_CASE

```java
public enum SaleStatus {
    SALE,       // 판매 중
    SOLD        // 판매 완료
}

public enum BookStatus {
    NEW,        // 새것
    LIKE_NEW,   // 매우 좋음
    GOOD,       // 좋음
    USED        // 사용감 있음
}
```

### 1-6. 상수 — UPPER_SNAKE_CASE, `Const` 클래스에 집중

```java
Const.SESSION
Const.DATETIME_FORMATTER
```

---

## 2. 패키지 구조

기능 단위로 패키지를 분리하고, 한 패키지 안에 VO / Mapper / Service / Controller를 함께 둔다.

```
project
├── trade
│   ├── TradeVO.java
│   ├── TradeImageVO.java
│   ├── TradeMapper.java
│   ├── TradeService.java
│   ├── TradeController.java
│   └── ENUM
│       ├── SaleStatus.java
│       ├── BookStatus.java
│       └── PaymentType.java
├── member
│   ├── MemberVO.java
│   ├── MemberMapper.java
│   └── ...
├── chat
│   ├── chatroom/
│   ├── message/
│   └── pubsub/
├── config
│   └── redis/
└── util
    ├── exception/
    └── imgUpload/
```

---

## 3. 클래스별 작성 규칙

### 3-1. VO

```java
@Data
public class TradeVO {

    // ── PK ──
    private long trade_seq;

    // ── FK ──
    private long member_seller_seq;
    private long member_buyer_seq;
    private Long pending_buyer_seq;          // nullable FK는 Wrapper 타입

    // ── 비즈니스 필드 (검증 포함) ──
    @NotBlank
    private String sale_title;

    @NotBlank
    @Length(max = 500)
    private String sale_cont;

    @NotNull
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer sale_price;

    // ── Enum 필드 ──
    private BookStatus book_st;
    private SaleStatus sale_st;

    // ── 날짜 (JSON 직렬화 커스텀) ──
    @JsonIgnore
    private LocalDateTime crt_dtm;

    @JsonProperty("crt_dtm")
    public String getCrtDtmFormatted() {
        return crt_dtm != null ? crt_dtm.format(Const.DATETIME_FORMATTER) : null;
    }

    // ── 임시/화면용 필드 (DB 미매핑) ──
    private List<MultipartFile> uploadFiles;
    private List<String> imgUrls;
}
```

**규칙 요약:**
- `@Data` (Lombok)으로 getter/setter/toString 자동 생성
- nullable FK는 `Long` (Wrapper), non-null FK는 `long` (primitive)
- 검증 어노테이션은 필드 위에 직접 선언
- 날짜 포맷은 `@JsonIgnore` + `@JsonProperty` 조합
- 필드 순서: PK → FK → 비즈니스 필드 → Enum → 날짜 → 임시 필드

### 3-2. Mapper (인터페이스)

```java
@Mapper
public interface TradeMapper {

    TradeVO findBySeq(@Param("trade_seq") long trade_seq);

    List<TradeVO> findAllWithPaging(@Param("size") int size,
                                    @Param("offset") int offset,
                                    @Param("searchVO") TradeVO searchVO);

    int save(TradeVO tradeVO);

    int update(TradeVO tradeVO);

    void delete(@Param("trade_seq") long trade_seq);

    int countAll(@Param("searchVO") TradeVO searchVO);
}
```

**규칙 요약:**
- `@Mapper` 어노테이션 사용
- 파라미터가 2개 이상이면 `@Param` 필수
- 반환 타입: 단건 `TradeVO`, 목록 `List<TradeVO>`, 변경 `int`, 없음 `void`
- `@Param` 이름은 XML의 `#{}` 과 정확히 일치

### 3-3. Service

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeMapper tradeMapper;
    private final BookImgMapper bookImgMapper;

    // ── 조회 (readOnly 상속) ──
    @Cacheable(value = "trade", key = "#trade_seq", unless = "#result == null")
    public TradeVO search(long trade_seq) {
        TradeVO findTrade = tradeMapper.findBySeq(trade_seq);
        if (findTrade == null) {
            throw new TradeNotFoundException("Cannot find trade_seq=" + trade_seq);
        }
        return findTrade;
    }

    // ── 쓰기 (readOnly 해제 + 캐시 무효화) ──
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "tradeList", allEntries = true),
        @CacheEvict(value = "tradeCount", allEntries = true)
    })
    public boolean upload(TradeVO tradeVO) { ... }

    // ── 검증 ──
    public void validateSellerOwnership(long trade_seq, long member_seq) {
        TradeVO trade = tradeMapper.findBySeq(trade_seq);
        if (trade == null || trade.getMember_seller_seq() != member_seq) {
            log.warn("권한 없는 접근 시도: trade_seq={}, 요청자={}", trade_seq, member_seq);
            throw new ForbiddenException("권한이 없습니다.");
        }
    }
}
```

**규칙 요약:**
- 클래스 레벨 `@Transactional(readOnly = true)` — 조회는 상속, 쓰기는 `@Transactional`로 오버라이드
- 의존성 주입: `@RequiredArgsConstructor` + `private final`
- 캐시: 조회에 `@Cacheable`, 쓰기에 `@CacheEvict`
- 검증 실패 시 커스텀 예외 throw (`ForbiddenException`, `TradeNotFoundException`)

### 3-4. Controller

```java
@Controller
@Slf4j
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    // ── 페이지 반환 ──
    @GetMapping("/trade/{tradeSeq}")
    public String getSaleDetail(@PathVariable long tradeSeq,
                                Model model,
                                HttpSession session) {
        TradeVO trade = tradeService.search(tradeSeq);
        model.addAttribute("trade", trade);
        return "trade/tradedetail";
    }

    // ── JSON 반환 ──
    @PostMapping("/trade/like")
    @ResponseBody
    public Map<String, Object> tradeLike(@RequestParam long trade_seq,
                                         HttpSession session) {
        // ...
    }

    // ── 폼 전송 + 검증 ──
    @PostMapping("/trade")
    public String uploadTrade(@Valid TradeVO tradeVO,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("Trade validation error: {}", bindingResult.getAllErrors());
            return "error/numberError";
        }
        // ...
        return "redirect:/trade/{tradeSeq}";
    }
}
```

**규칙 요약:**
- 페이지 반환: `String` (뷰 이름)
- JSON 반환: `@ResponseBody` + `Map<String, Object>` 또는 `List<T>`
- 리다이렉트: `"redirect:/경로"`
- 세션 접근: `session.getAttribute(Const.SESSION)`
- 권한 검증은 Service에 위임 (`tradeService.validateXxx()`)

---

## 4. MyBatis Mapper XML

### 4-1. 파일 위치 및 네이밍

```
src/main/resources/project/trade/tradeMapper.xml
src/main/resources/project/trade/bookImgMapper.xml
```

- 경로는 Java 패키지 구조와 동일: `resources/project/trade/`
- 파일명은 Mapper 인터페이스의 camelCase: `TradeMapper.java` → `tradeMapper.xml`

### 4-2. XML 선언부

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="project.trade.TradeMapper">
```

- `namespace`는 Mapper 인터페이스의 FQCN과 정확히 일치

### 4-3. SQL 포맷팅 규칙

**키워드는 대문자, 들여쓰기 4칸:**

```xml
<select id="findBySeq"
        parameterType="long"
        resultType="project.trade.TradeVO">
    SELECT
        sb.trade_seq,
        sb.member_seller_seq,
        sb.sale_title,
        sb.book_st,
        sb.sale_cont,
        sb.sale_price,
        sb.delivery_cost,
        sb.sale_st,
        sb.crt_dtm
    FROM
        sb_trade_info AS sb
    WHERE
        sb.trade_seq = #{trade_seq}
</select>
```

**규칙:**
- `SELECT`, `FROM`, `WHERE`, `JOIN`, `ORDER BY`, `LIMIT` — 줄바꿈 후 대문자
- 컬럼 목록 — 한 줄에 하나씩, 4칸 들여쓰기
- 테이블 별칭 — 짧고 의미 있게: `sb`, `ti`, `m`
- 컬럼 참조 — 항상 별칭 포함: `sb.trade_seq`
- 파라미터 바인딩 — `#{}` 사용 (SQL Injection 방지)

### 4-4. INSERT

```xml
<insert id="save"
        parameterType="project.trade.TradeVO"
        useGeneratedKeys="true"
        keyProperty="trade_seq">
    INSERT INTO sb_trade_info (
        member_seller_seq,
        category_seq,
        sale_title,
        book_st,
        sale_cont,
        sale_price,
        delivery_cost,
        sale_rg
    ) VALUES (
        #{member_seller_seq},
        #{category_seq},
        #{sale_title},
        #{book_st},
        #{sale_cont},
        #{sale_price},
        #{delivery_cost},
        #{sale_rg}
    )
</insert>
```

**규칙:**
- 자동 증가 PK: `useGeneratedKeys="true"` + `keyProperty="trade_seq"`
- 컬럼과 값 — 한 줄에 하나씩, 순서 일치

### 4-5. UPDATE

```xml
<update id="update" parameterType="project.trade.TradeVO">
    UPDATE sb_trade_info
    SET
        sale_title = #{sale_title},
        book_st = #{book_st},
        sale_cont = #{sale_cont},
        sale_price = #{sale_price},
        delivery_cost = #{delivery_cost},
        sale_rg = #{sale_rg},
        upd_dtm = NOW()
    WHERE
        trade_seq = #{trade_seq}
</update>
```

### 4-6. 동적 SQL

**`<if>` — 단일 조건:**

```xml
<if test="searchVO.category_seq != null and searchVO.category_seq != 0">
    AND ti.category_seq = #{searchVO.category_seq}
</if>

<if test="searchVO.search_word != null and searchVO.search_word != ''">
    AND ti.sale_title LIKE CONCAT('%', #{searchVO.search_word}, '%')
</if>
```

**`<choose>` — 다중 분기:**

```xml
<choose>
    <when test="searchVO.sort == 'priceAsc'">
        ORDER BY ti.sale_price ASC
    </when>
    <when test="searchVO.sort == 'likeDesc'">
        ORDER BY wish_count DESC
    </when>
    <otherwise>
        ORDER BY ti.crt_dtm DESC
    </otherwise>
</choose>
```

**`<foreach>` — 반복:**

```xml
<foreach collection="tradeSeqList"
         item="seq"
         open="("
         separator=","
         close=")">
    #{seq}
</foreach>
```

### 4-7. 페이징

```xml
LIMIT #{size}
OFFSET #{offset}
```

- `offset` 계산은 Service에서: `(page - 1) * size`

### 4-8. 비관적 락

```xml
<select id="findSafePaymentForUpdate" ...>
    SELECT
        trade_seq,
        safe_payment_st
    FROM
        sb_trade_info
    WHERE
        trade_seq = #{trade_seq}
    FOR UPDATE
</select>
```

- `FOR UPDATE` 사용 시 XML 주석으로 목적 표기

---

## 5. 어노테이션 순서

### 클래스 레벨

```java
@Service                              // 1. 스프링 역할
@Transactional(readOnly = true)       // 2. 트랜잭션
@RequiredArgsConstructor              // 3. Lombok 생성자
@Slf4j                                // 4. Lombok 로깅
public class TradeService { ... }
```

```java
@Controller                           // 1. 스프링 역할
@Slf4j                                // 2. Lombok 로깅
@RequiredArgsConstructor              // 3. Lombok 생성자
public class TradeController { ... }
```

### 메서드 레벨

```java
@Transactional                        // 1. 트랜잭션
@Caching(evict = { ... })            // 2. 캐시
public boolean upload(...) { ... }
```

```java
@GetMapping("/trade/{tradeSeq}")      // 1. HTTP 매핑
@ResponseBody                         // 2. 응답 방식
public List<BookVO> findBookByTitle(...) { ... }
```

---

## 6. 예외 처리

### 커스텀 예외 사용

| 예외 | 용도 |
|------|------|
| `ForbiddenException` | 권한 없는 접근 |
| `TradeNotFoundException` | 존재하지 않는 리소스 조회 |
| `IllegalStateException` | 비즈니스 로직상 불가능한 상태 전이 |

### 패턴

```java
// Service — 예외 throw
if (trade == null) {
    throw new TradeNotFoundException("Cannot find trade_seq=" + trade_seq);
}

// Controller — try-catch로 에러 페이지 반환
try {
    tradeService.modify(tradeSeq, updateTrade);
    return "redirect:/trade/{tradeSeq}";
} catch (Exception e) {
    return "error/500";
}
```

---

## 7. 로깅

### 설정

```java
@Slf4j   // Lombok — log 필드 자동 생성
```

### 레벨별 사용 기준

| 레벨 | 용도 | 예시 |
|------|------|------|
| `log.info` | 비즈니스 성공/결과 | `log.info("trade save success, isbn: {}", isbn)` |
| `log.warn` | 검증 실패, 권한 위반 | `log.warn("권한 없는 접근 시도: trade_seq={}, 요청자={}", ...)` |
| `log.error` | 예외, 시스템 오류 | `log.error("이미지 삭제 실패: {}", url, e)` |

### 작성 규칙

```java
// 플레이스홀더 사용 (문자열 연결 금지)
log.info("trade_seq={}, sender={}", trade_seq, sender);   // O
log.info("trade_seq=" + trade_seq);                        // X

// 한국어로 간결하게
log.warn("존재하지 않는 거래 접근: trade_seq={}", trade_seq);

// 예외는 마지막 인자로
log.error("처리 실패: {}", url, e);
```

---

## 8. 테스트

### 구조

```java
@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    TradeMapper tradeMapper;

    @InjectMocks
    TradeService tradeService;
}
```

### 테스트 메서드명 — 한국어, 시나리오_기대결과

```java
@Nested
@DisplayName("validateSellerOwnership - 판매자 본인 검증")
class ValidateSellerOwnership {

    @Test
    @DisplayName("본인 거래 - 예외 없음")
    void 본인거래_예외없음() {
        // given
        when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, "NONE"));

        // when & then
        assertThatCode(() -> tradeService.validateSellerOwnership(100L, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 거래 - ForbiddenException")
    void 타인거래_ForbiddenException() {
        when(tradeMapper.findBySeq(100L)).thenReturn(createTrade(1L, null, "NONE"));

        assertThatThrownBy(() -> tradeService.validateSellerOwnership(100L, 999L))
                .isInstanceOf(ForbiddenException.class);
    }
}
```

### 규칙

- given / when / then 주석으로 구분
- `@Nested` + `@DisplayName`으로 그룹핑
- AssertJ 사용: `assertThat`, `assertThatThrownBy`, `assertThatCode`
- Mockito: `when().thenReturn()`, `verify()`

---

## 9. Import 순서

```java
// 1. 서드파티 (Lombok, Jackson, Spring)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 2. 프로젝트 내부
import project.trade.TradeVO;
import project.util.exception.ForbiddenException;

// 3. Java 표준 라이브러리
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
```

---

## 10. 요약 치트시트

```
클래스명       PascalCase + 역할접미사        TradeService, TradeVO
DB 필드        snake_case                      trade_seq, member_seller_seq
지역변수       camelCase                       findTrade, imgUrls
메서드명       camelCase + 동사 시작           search(), validateXxx()
Enum 값        UPPER_SNAKE_CASE                SALE, LIKE_NEW
상수           UPPER_SNAKE_CASE                Const.SESSION
SQL 키워드     UPPERCASE                       SELECT, FROM, WHERE
테스트명       한국어_시나리오_결과             본인거래_예외없음()

트랜잭션       클래스: readOnly=true, 쓰기 메서드: @Transactional
캐시           조회: @Cacheable, 쓰기: @CacheEvict
검증           VO: @NotBlank/@Min 등, Service: validateXxx() → 예외 throw
로깅           info=성공, warn=검증실패, error=예외
```
