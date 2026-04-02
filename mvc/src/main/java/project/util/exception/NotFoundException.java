package project.util.exception;

/**
 * 404 Not Found 예외
 * - 요청한 리소스가 존재하지 않을 때 발생
 */
public class NotFoundException extends ClientException {

    public NotFoundException(String message) {
        super(message);
    }
}
