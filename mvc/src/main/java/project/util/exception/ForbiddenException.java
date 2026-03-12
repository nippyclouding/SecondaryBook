package project.util.exception;

/**
 * 권한이 없는 리소스에 접근 시 발생하는 예외
 * - IDOR (Insecure Direct Object Reference) 방지용
 * - 본인 소유가 아닌 거래/게시글 수정/삭제 시도 시 발생
 */
public class ForbiddenException extends ClientException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
