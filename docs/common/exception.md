# 예외 계층 구조

## 설계 원칙

- HTTP 상태 코드 기준으로 중간 추상 클래스를 두어 계층화
- `GlobalExceptionHandler`에서 `ClientException`(4xx)과 `ServerException`(5xx) 두 개로 단순 처리
- 에러 페이지는 `400.jsp`와 `500.jsp` 두 개만 사용, `errorMessage` 동적 표시
- 기능별 하위 패키지로 구체 예외 분리

---

## 계층 구조

### 웹 예외 계층 (GlobalExceptionHandler 처리 대상)

```
RuntimeException
├── ClientException (4xx → 400.jsp)             [abstract]
│   ├── NotFoundException (404)
│   │   ├── trade/TradeNotFoundException
│   │   └── bookclub/BookClubNotFoundException
│   ├── ForbiddenException (403)
│   └── InvalidRequestException (400)
│       ├── bookclub/BookClubInvalidRequestException
│       ├── file/FileUploadException
│       └── settlement/SettlementException
└── ServerException (5xx → 500.jsp)
```

### 배치 전용 예외 (Spring Batch skip/fail 신호, GlobalExceptionHandler 처리 안 함)

```
RuntimeException
├── InsufficientBalanceException        ← Spring Batch skip 대상 (잔액 부족)
└── IllegalStateException (JDK 기본)   ← Spring Batch Job 실패 신호 (동시 배치 감지)
```

배치 예외는 HTTP 요청이 아니므로 GlobalExceptionHandler를 거치지 않는다.
Spring Batch가 직접 처리한다:
- `InsufficientBalanceException` → skip → `SkipListener.onSkipInWrite()` 호출
- `IllegalStateException` → Job 전체 실패 (skip 대상 아님)

---

## 패키지 구조

```
project.util.exception/
├── ClientException.java              (abstract, 4xx)
├── ServerException.java              (5xx)
├── NotFoundException.java            (404, ClientException 하위)
├── ForbiddenException.java           (403, ClientException 하위)
├── InvalidRequestException.java      (400, ClientException 하위)
├── GlobalExceptionHandler.java       (@ControllerAdvice)
├── trade/
│   └── TradeNotFoundException.java
├── bookclub/
│   ├── BookClubNotFoundException.java
│   └── BookClubInvalidRequestException.java
├── file/
│   └── FileUploadException.java
└── settlement/
    └── SettlementException.java

project.batch/
├── InsufficientBalanceException.java (배치 전용, RuntimeException 직접 상속)
```

---

## GlobalExceptionHandler 처리 흐름

| 핸들러 | 대상 | 응답 방식 |
|--------|------|-----------|
| `ClientException` | 모든 4xx 예외 (ForbiddenException 포함) | AJAX: `{success:false, message}` JSON / 일반: error/400.jsp |
| `ServerException` | 5xx 예외 | error/500.jsp |
| `MaxUploadSizeExceededException` | 파일 크기 초과 (Spring 제공) | error/400.jsp + "최대 5MB" 메시지 |
| `Exception` | 위에서 처리되지 않은 모든 예외 | error/500.jsp + "서버 오류" 메시지 |

### AJAX 판별 로직

```java
boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
              || (accept != null && accept.contains("application/json"));
```

AJAX 요청이면 JSON으로, 일반 요청이면 JSP 뷰로 응답한다.

---

## AdminController 로컬 핸들러

`AdminController`는 `@Validated`로 파라미터 유효성 검증을 사용하며,
`ConstraintViolationException` 을 클래스 내부 `@ExceptionHandler`로 직접 처리한다.

```java
// AdminController 내부
@ExceptionHandler(ConstraintViolationException.class)
@ResponseBody
public Map<String, Object> handleConstraintViolation(ConstraintViolationException e) {
    String message = e.getConstraintViolations()...findFirst()...;
    result.put("success", false);
    result.put("message", message);
    return result;
}
```

이 예외는 GlobalExceptionHandler까지 전파되지 않는다.
다른 컨트롤러에서 `@Validated`를 사용하면 `ConstraintViolationException`은 `Exception` 핸들러가 잡아 500으로 처리된다.

---

## 새 예외 추가 가이드

1. `ClientException` 또는 `ServerException` 하위 클래스로 생성
2. 기능별 하위 패키지에 배치 (예: `exception/payment/PaymentException.java`)
3. `GlobalExceptionHandler` 수정 불필요 (상위 클래스에서 자동 처리)

```java
// 예시: 결제 예외 추가
package project.util.exception.payment;

import project.util.exception.InvalidRequestException;

public class PaymentException extends InvalidRequestException {
    public PaymentException(String message) {
        super(message);
    }
}
// → 자동으로 GlobalExceptionHandler.handleClientException()에서 처리됨
```

---

## 예외 처리 흐름 전체 다이어그램

```
요청
  │
  ▼
Controller
  │
  ├── ClientException 발생
  │     → GlobalExceptionHandler.handleClientException()
  │         → AJAX: ResponseEntity(400, {success:false, message})
  │         → 일반: error/400.jsp
  │
  ├── ServerException 발생
  │     → GlobalExceptionHandler.handleServerException()
  │         → error/500.jsp
  │
  ├── MaxUploadSizeExceededException 발생
  │     → GlobalExceptionHandler.handleMaxSizeException()
  │         → error/400.jsp + "최대 5MB" 메시지
  │
  └── 기타 Exception 발생
        → GlobalExceptionHandler.handleGenericException()
            → error/500.jsp + "서버 오류" 메시지
            → log.error로 스택트레이스 기록

[배치 영역 - GlobalExceptionHandler와 무관]
Spring Batch Step
  │
  ├── InsufficientBalanceException → skip → SkipListener.onSkipInWrite()
  └── IllegalStateException        → Job 실패 (스케줄러 로그에 기록)
```
