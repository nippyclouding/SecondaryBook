package project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

        /**
         * 통합 보안 설정
         * - CSRF: 전역 활성화 (상태 변경 요청 보호)
         * - WebSocket, Health Check 등 특정 경로만 CSRF 제외
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeRequests()
                                .anyRequest().permitAll()
                                .and()
                                // SockJS iframe 허용 (채팅 WebSocket용)
                                .headers()
                                .frameOptions().sameOrigin()
                                .and()
                                // CSRF: 전역 적용, 특정 경로만 제외
                                .csrf()
                                .ignoringRequestMatchers(
                                        // WebSocket/STOMP 연결 - SockJS fallback transport (CSRF 토큰 전달 어려움)
                                        new AntPathRequestMatcher("/chatEndPoint/**"),
                                        // Health Check
                                        new AntPathRequestMatcher("/health", "GET"),
                                        // 관리자 로그아웃 pending (sendBeacon은 커스텀 헤더 불가)
                                        new AntPathRequestMatcher("/admin/api/logout-pending", "POST")
                                )
                                .and()
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler(csrfAccessDeniedHandler()))
                                .formLogin().disable()
                                .httpBasic().disable()
                                .logout().disable();

                return http.build();
        }

        /**
         * CSRF 검증 실패 등 접근 거부 시 처리
         * - AJAX 요청: 401 + JSON 응답
         * - 일반 요청: /login으로 리다이렉트
         */
        @Bean
        public AccessDeniedHandler csrfAccessDeniedHandler() {
                return (request, response, accessDeniedException) -> {
                        log.warn("Access denied for URI: {}, Exception: {}",
                                        request.getRequestURI(), accessDeniedException.getClass().getSimpleName());

                        // CSRF 토큰 관련 예외인 경우 (세션 만료로 인한 토큰 불일치 가능성)
                        boolean isCsrfError = accessDeniedException instanceof InvalidCsrfTokenException
                                        || accessDeniedException instanceof MissingCsrfTokenException;

                        // AJAX 요청 감지
                        String requestedWith = request.getHeader("X-Requested-With");
                        boolean isAjax = "XMLHttpRequest".equals(requestedWith);
                        String accept = request.getHeader("Accept");
                        boolean isJsonRequest = accept != null && accept.contains("application/json");

                        if (isAjax || isJsonRequest) {
                                // AJAX/JSON 요청: 401 + JSON 응답
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                String message = isCsrfError
                                                ? "세션이 만료되었습니다. 페이지를 새로고침 후 다시 시도해주세요."
                                                : "접근이 거부되었습니다. 다시 로그인해주세요.";
                                response.getWriter().write(
                                                "{\"error\":\"SESSION_EXPIRED\"," +
                                                                "\"message\":\"" + message + "\"," +
                                                                "\"redirectUrl\":\"/login\"}");
                        } else {
                                // 일반 요청: 로그인 페이지로 리다이렉트
                                String redirectUrl = isCsrfError
                                                ? request.getContextPath() + "/login?expired=true"
                                                : request.getContextPath() + "/login?denied=true";
                                response.sendRedirect(redirectUrl);
                        }
                };
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}