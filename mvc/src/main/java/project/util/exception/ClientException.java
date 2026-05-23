package project.util.exception;

/**
 * 4xx 클라이언트 에러 기본 클래스
 * - 잘못된 요청, 권한 없음, 리소스 없음 등
 * - GlobalExceptionHandler에서 error/400.jsp로 이동
 */
public abstract class ClientException extends RuntimeException {

    private final ErrorCode errorCode;

    public ClientException(String message) {
        this(ErrorCode.BAD_REQUEST, message);
    }

    public ClientException(String message, Throwable cause) {
        this(ErrorCode.BAD_REQUEST, message, cause);
    }

    protected ClientException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ClientException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
