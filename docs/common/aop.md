# AOP (Aspect-Oriented Programming)

---

## 개념

AOP는 **횡단 관심사(Cross-Cutting Concerns)**를 핵심 비즈니스 로직과 분리하는 프로그래밍 패러다임이다.

횡단 관심사란 여러 클래스/메서드에 공통으로 필요하지만, 비즈니스 로직 자체와는 무관한 코드를 말한다.

```
[AOP 없을 때]

TradeService.upload() {
    StopWatch sw = new StopWatch(); sw.start();       ← 로깅 코드
    try {
        // 실제 비즈니스 로직
    } catch (Exception e) {
        log.error("...");                              ← 로깅 코드
        throw e;
    }
    log.info("... {}ms", sw.getTotalTimeMillis());    ← 로깅 코드
}

SettlementService.requestSettlement() {
    StopWatch sw = new StopWatch(); sw.start();       ← 중복!
    try {
        // 실제 비즈니스 로직
    } catch (Exception e) {
        log.error("...");                              ← 중복!
        throw e;
    }
    log.info("... {}ms", sw.getTotalTimeMillis());    ← 중복!
}
```

모든 Service 메서드에 같은 로깅 코드가 반복된다.
AOP를 사용하면 이 공통 코드를 한 곳에 모아 적용할 수 있다.

```
[AOP 적용 후]

ServiceLoggingAspect.java
  → "Service 클래스의 모든 메서드 실행 전후에 StopWatch로 시간을 재고 로그를 남긴다"

TradeService.upload() {
    // 실제 비즈니스 로직만
}

SettlementService.requestSettlement() {
    // 실제 비즈니스 로직만
}
```

---

## Spring AOP의 동작 방식 — 프록시(Proxy)

Spring AOP는 **프록시 패턴**으로 동작한다.
실제 Bean을 감싼 프록시 객체를 만들어, 메서드 호출 전후에 부가 기능을 끼워 넣는다.

```
[외부에서 TradeService.upload() 호출]

호출자
  │
  ▼
TradeServiceProxy (Spring이 자동 생성)
  │
  ├── Before: AOP Aspect 실행 (StopWatch 시작)
  │
  ├── Target: TradeService.upload() 실제 실행
  │
  └── After: AOP Aspect 실행 (로그 기록)
  │
  ▼
호출자에게 결과 반환
```

**한계**: 같은 클래스 내에서 `this.method()`로 자기 자신을 호출할 때는 프록시를 거치지 않아 AOP가 적용되지 않는다.

---

## 핵심 어노테이션

### @Aspect

이 클래스가 Aspect(부가 기능 모음)임을 선언한다.
Spring이 이 클래스를 AOP 처리 대상으로 인식한다.
`@Component`와 함께 사용해야 Spring Bean으로 등록된다.

```java
@Aspect
@Component
public class ServiceLoggingAspect { ... }
```

---

### Advice 어노테이션 — "언제" 실행하는가

| 어노테이션 | 실행 시점 | 특징 |
|------------|-----------|------|
| `@Before` | 메서드 실행 전 | 예외가 발생해도 실행됨 |
| `@After` | 메서드 실행 후 (성공/실패 무관) | finally와 유사 |
| `@AfterReturning` | 메서드가 정상 반환한 후 | 반환값 접근 가능 |
| `@AfterThrowing` | 메서드가 예외를 던진 후 | 예외 객체 접근 가능 |
| `@Around` | 메서드 실행 전후 전체 제어 | 가장 강력, proceed() 직접 호출 |

```java
// 실행 전
@Before("within(project..*Service)")
public void before(JoinPoint jp) { ... }

// 실행 후 (성공/실패 무관)
@After("within(project..*Service)")
public void after(JoinPoint jp) { ... }

// 정상 반환 후 (반환값 접근)
@AfterReturning(value = "...", returning = "result")
public void afterReturning(JoinPoint jp, Object result) { ... }

// 예외 발생 후
@AfterThrowing(value = "...", throwing = "ex")
public void afterThrowing(JoinPoint jp, Exception ex) { ... }

// 전후 전체 제어
@Around("within(project..*Service)")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    // before
    Object result = pjp.proceed(); // 실제 메서드 실행
    // after
    return result;
}
```

---

### Pointcut 표현식 — "어디에" 적용하는가

Pointcut은 Advice가 적용될 대상 메서드를 선택하는 표현식이다.

#### execution — 메서드 시그니처 기준

```
execution([접근제어자] 반환타입 [패키지.]클래스.메서드(파라미터))
```

```java
// project 패키지 하위 모든 클래스의 모든 메서드
execution(* project..*.*(..))

// TradeService의 모든 public 메서드
execution(public * project.trade.TradeService.*(..))

// 반환타입이 void인 메서드만
execution(void project..*.*(..))
```

#### within — 클래스/패키지 기준

```java
// project 패키지 하위의 이름이 *Service로 끝나는 모든 클래스
within(project..*Service)

// 특정 패키지 내 모든 클래스
within(project.trade.*)
```

#### @annotation — 특정 어노테이션이 붙은 메서드

```java
// @Transactional이 붙은 메서드
@annotation(org.springframework.transaction.annotation.Transactional)
```

---

### ProceedingJoinPoint / JoinPoint

`JoinPoint`는 현재 실행 중인 메서드의 정보에 접근하는 객체다.

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `getSignature()` | `Signature` | 메서드 시그니처 (이름, 클래스 등) |
| `getArgs()` | `Object[]` | 메서드 실제 인자 |
| `getTarget()` | `Object` | 실제 타겟 Bean |

`ProceedingJoinPoint`는 `@Around`에서만 사용하며, `JoinPoint`의 확장이다.

| 메서드 | 설명 |
|--------|------|
| `proceed()` | 실제 메서드를 실행하고 결과를 반환 |
| `proceed(args)` | 인자를 바꿔서 실제 메서드를 실행 |

`@Around`에서 `proceed()`를 호출하지 않으면 실제 메서드가 실행되지 않는다.
이를 활용해 인증 체크, 조건부 실행 등을 구현할 수 있다.

---

## 이 프로젝트의 적용 — ServiceLoggingAspect

```java
@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("within(project..*Service)")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        StopWatch sw = new StopWatch();
        sw.start();

        try {
            Object result = joinPoint.proceed();  // 실제 메서드 실행
            sw.stop();
            log.info("[SERVICE] {}.{}() → {}ms", className, methodName, sw.getTotalTimeMillis());
            return result;

        } catch (Exception e) {
            sw.stop();
            log.error("[SERVICE] {}.{}() 예외 발생 ({}ms) — {}",
                    className, methodName, sw.getTotalTimeMillis(), e.getMessage());
            throw e;  // 예외 재발생 (AOP가 삼키지 않음)
        }
    }
}
```

### 설계 포인트

| 항목 | 내용 |
|------|------|
| Pointcut 대상 | `within(project..*Service)` → project 하위의 이름이 *Service인 클래스 전체 |
| 적용 범위 | TradeService, MemberService, SettlementService, BookClubService 등 모든 Service 계층 |
| 로깅 도구 | Spring의 `StopWatch` — start/stop으로 실행 시간 측정 |
| 정상 실행 | `INFO` 레벨: `[SERVICE] 클래스명.메서드명() → X ms` |
| 예외 발생 | `ERROR` 레벨: `[SERVICE] 클래스명.메서드명() 예외 발생 (Xms) — 예외 메시지` |
| 예외 처리 | `throw e`로 예외를 다시 던짐 → GlobalExceptionHandler가 최종 처리 |

### 로그 출력 예시

```
INFO  [SERVICE] TradeService.upload() → 243ms
INFO  [SERVICE] SettlementService.requestSettlement() → 12ms
ERROR [SERVICE] TradeService.search() 예외 발생 (3ms) — 거래를 찾을 수 없습니다.
```

### 주의 사항

- `@BatchConfig`에서 만드는 `SettlementItemWriter`, `SettlementItemProcessor`, `SettlementItemReader`는 이름이 `*Service`가 아니므로 AOP 대상이 아니다.
- `SettlementService.markAsInsufficient()`는 AOP 대상이므로, SkipListener에서 호출할 때도 로그가 찍힌다.
- 같은 Service 클래스 안에서 `this.otherMethod()` 호출은 프록시를 거치지 않아 AOP 적용 안 됨.

---

## 관련 파일 위치

```
aop/
└── ServiceLoggingAspect.java     ← @Aspect @Component: Service 계층 실행 시간/예외 로깅
```
