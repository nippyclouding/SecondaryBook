package project.util.exception;

/**
 * 400 Bad Request 예외
 * - 잘못된 요청 파라미터, 비즈니스 규칙 위반 등
 * - IllegalArgumentException/IllegalStateException 대체
 */
public class InvalidRequestException extends ClientException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
