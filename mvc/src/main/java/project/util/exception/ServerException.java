package project.util.exception;

/**
 * 5xx 서버 에러 기본 클래스
 * - 내부 서버 오류, 외부 서비스 장애 등
 * - GlobalExceptionHandler에서 error/500.jsp로 이동
 */
public class ServerException extends RuntimeException {

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
