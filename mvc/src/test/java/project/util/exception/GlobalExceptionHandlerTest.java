package project.util.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("브라우저 권한 오류는 403 오류 화면으로 응답한다")
    void forbiddenBrowserRequest_returns403View() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/400"));
    }

    @Test
    @DisplayName("JSON 권한 오류는 403 JSON 응답으로 반환한다")
    void forbiddenJsonRequest_returns403Json() throws Exception {
        mockMvc.perform(get("/test/forbidden").header("Accept", "application/json"))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"success\":false,\"code\":\"COMMON_403\",\"message\":\"접근 권한이 없습니다.\"}"));
    }

    @Test
    @DisplayName("잘못된 브라우저 요청은 400 오류 화면으로 응답한다")
    void invalidRequest_returns400View() throws Exception {
        mockMvc.perform(get("/test/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/400"));
    }

    @Test
    @DisplayName("업로드 크기 초과는 400 오류 화면으로 응답한다")
    void maxUploadSizeExceeded_returns400View() throws Exception {
        mockMvc.perform(get("/test/max-upload"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/400"));
    }

    @Test
    @DisplayName("JSON 업로드 크기 초과는 표준 코드와 함께 400으로 응답한다")
    void maxUploadSizeExceededJson_returnsCode() throws Exception {
        mockMvc.perform(get("/test/max-upload").header("Accept", "application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"code\":\"FILE_400\",\"message\":\"업로드 가능한 파일 크기를 초과했습니다. (최대 5MB)\"}"));
    }

    @Test
    @DisplayName("JSON 서버 오류는 표준 코드와 함께 500으로 응답한다")
    void genericJsonRequest_returnsCode() throws Exception {
        mockMvc.perform(get("/test/server").header("Accept", "application/json"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"success\":false,\"code\":\"COMMON_500\",\"message\":\"서버 오류가 발생했습니다.\"}"));
    }

    @Controller
    static class ExceptionController {

        @GetMapping("/test/forbidden")
        public String forbidden() {
            throw new ForbiddenException("접근 권한이 없습니다.");
        }

        @GetMapping("/test/invalid")
        public String invalid() {
            throw new InvalidRequestException("잘못된 요청입니다.");
        }

        @GetMapping("/test/max-upload")
        public String maxUpload() {
            throw new MaxUploadSizeExceededException(5L);
        }

        @GetMapping("/test/server")
        public String server() {
            throw new RuntimeException("unexpected");
        }
    }
}
