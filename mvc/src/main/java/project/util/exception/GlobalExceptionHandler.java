package project.util.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import project.util.exception.ForbiddenException;
import project.util.exception.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public Object handleForbiddenException(ForbiddenException e, HttpServletRequest request, Model model) {
        log.warn("접근 거부: uri={}, message={}", request.getRequestURI(), e.getMessage());

        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith) ||
                         (accept != null && accept.contains("application/json"));

        if (isAjax) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFoundException(NotFoundException e, HttpServletRequest request, Model model) {
        log.warn("리소스 없음: uri={}, message={}", request.getRequestURI(), e.getMessage());

        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith) ||
                         (accept != null && accept.contains("application/json"));

        if (isAjax) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(ClientException.class)
    public Object handleClientException(ClientException e, HttpServletRequest request, Model model) {
        log.warn("클라이언트 에러: uri={}, message={}", request.getRequestURI(), e.getMessage());

        // AJAX 요청인 경우 JSON 응답
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith) ||
                         (accept != null && accept.contains("application/json"));

        if (isAjax) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleServerException(ServerException e, Model model) {
        log.error("서버 에러: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", e.getMessage());
        return "error/500";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException ex, Model model) {
        model.addAttribute("errorMessage", "업로드 가능한 파일 크기를 초과했습니다. (최대 5MB)");
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, Model model) {
        log.error("서버 오류 발생: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "error/500";
    }
}
