package project.util.logInOut;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 로그인 관련 유틸리티 클래스
 * - 로그인 후 원래 페이지로 리다이렉트 기능 지원
 */
public class LoginUtil {

    public static final String REDIRECT_PARAM = "redirect";
    public static final String REDIRECT_SESSION_KEY = "redirectAfterLogin";

    /**
     * 로그인 페이지로 리다이렉트하면서 현재 URL을 redirect 파라미터로 전달
     * (Spring RequestContextHolder를 사용하여 현재 request 자동 획득)
     * @return "redirect:/login?redirect=현재URL" 형태의 문자열
     */
    public static String redirectToLogin() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "redirect:/login";
        }
        String currentUrl = getCurrentUrl(request);
        String encodedUrl = URLEncoder.encode(currentUrl, StandardCharsets.UTF_8);
        return "redirect:/login?" + REDIRECT_PARAM + "=" + encodedUrl;
    }

    /**
     * 로그인 페이지로 리다이렉트하면서 현재 URL을 redirect 파라미터로 전달
     * @param request 현재 요청
     * @return "redirect:/login?redirect=현재URL" 형태의 문자열
     */
    public static String redirectToLogin(HttpServletRequest request) {
        String currentUrl = getCurrentUrl(request);
        String encodedUrl = URLEncoder.encode(currentUrl, StandardCharsets.UTF_8);
        return "redirect:/login?" + REDIRECT_PARAM + "=" + encodedUrl;
    }

    /**
     * Spring RequestContextHolder를 사용하여 현재 HttpServletRequest 획득
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 현재 요청의 URL을 가져옴 (쿼리스트링 포함)
     * @param request 현재 요청
     * @return 현재 URL (컨텍스트 패스 제외)
     */
    public static String getCurrentUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 컨텍스트 패스 제거
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            return uri + "?" + queryString;
        }
        return uri;
    }

    /**
     * 로그인 성공 후 리다이렉트할 URL 결정
     * @param redirect redirect 파라미터 값
     * @param defaultUrl 기본 URL (redirect가 없을 경우)
     * @return 리다이렉트할 URL
     */
    public static String getRedirectUrl(String redirect, String defaultUrl) {
        if (redirect != null && !redirect.isEmpty() && isValidRedirectUrl(redirect)) {
            return redirect;
        }
        return defaultUrl;
    }

    /**
     * redirect URL이 유효한지 검증 (보안)
     * - 외부 URL로의 리다이렉트 방지 (Open Redirect 취약점 방지)
     * @param url 검증할 URL
     * @return 유효 여부
     */
    public static boolean isValidRedirectUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        // 상대 경로만 허용 (/, /path 형태)
        // 절대 URL(http://, https://, //) 차단
        if (url.startsWith("//") || url.contains("://")) {
            return false;
        }
        // 반드시 /로 시작해야 함
        return url.startsWith("/");
    }
}
