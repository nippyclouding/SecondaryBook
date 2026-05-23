package project.util.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class) // 403
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Object handleForbiddenException(ForbiddenException e, HttpServletRequest request, Model model) {
        log.warn("접근 거부: uri={}, message={}", request.getRequestURI(), e.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
        }

        model.addAttribute("errorCode", e.getErrorCode().getCode());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(NotFoundException.class) // 404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNotFoundException(NotFoundException e, HttpServletRequest request, Model model) {
        log.warn("리소스 없음: uri={}, message={}", request.getRequestURI(), e.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
        }

        model.addAttribute("errorCode", e.getErrorCode().getCode());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(ClientException.class) // 4xx
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleClientException(ClientException e, HttpServletRequest request, Model model) {
        log.warn("클라이언트 에러: uri={}, message={}", request.getRequestURI(), e.getMessage());

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
        }

        model.addAttribute("errorCode", e.getErrorCode().getCode());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(ServerException.class) // 5xx
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleServerException(ServerException e, HttpServletRequest request, Model model) {
        log.error("서버 에러: {}", e.getMessage(), e);
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
        }
        model.addAttribute("errorCode", e.getErrorCode().getCode());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/500";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request, Model model) {
        String message = "업로드 가능한 파일 크기를 초과했습니다. (최대 5MB)";
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEEDED, message));
        }
        model.addAttribute("errorCode", ErrorCode.FILE_SIZE_EXCEEDED.getCode());
        model.addAttribute("errorMessage", message);
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleGenericException(Exception e, HttpServletRequest request, Model model) {
        log.error("서버 오류 발생: {}", e.getMessage(), e);
        String message = "서버 오류가 발생했습니다.";
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, message));
        }
        model.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        model.addAttribute("errorMessage", message);
        return "error/500";
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equals(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }
}
