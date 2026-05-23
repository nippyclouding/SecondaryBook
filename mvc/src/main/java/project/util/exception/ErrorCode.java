package project.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST("COMMON_400", HttpStatus.BAD_REQUEST),
    FORBIDDEN("COMMON_403", HttpStatus.FORBIDDEN),
    NOT_FOUND("COMMON_404", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("COMMON_500", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("FILE_400", HttpStatus.BAD_REQUEST);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }
}
