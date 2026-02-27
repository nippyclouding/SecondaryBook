package project.util.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import project.member.MemberVO;
import project.util.Const;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 로그인 필수 경로에 대한 인터셉터
 * - 세션에 로그인 정보가 없으면 로그인 페이지로 리다이렉트
 * - AJAX 요청인 경우 401 JSON 응답 반환
 */
@Component
@Slf4j
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        HttpSession session = request.getSession(false);
        MemberVO member = (session != null)
                ? (MemberVO) session.getAttribute(Const.SESSION)
                : null;

        if (member == null) {
            log.debug("로그인 필요: uri={}, method={}", uri, method);
            return handleUnauthorized(request, response);
        }

        return true;
    }

    private boolean handleUnauthorized(HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        // AJAX 요청 감지
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        String accept = request.getHeader("Accept");
        boolean isJsonRequest = accept != null && accept.contains("application/json");

        if (isAjax || isJsonRequest) {
            // AJAX/JSON 요청: 401 + JSON 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"로그인이 필요합니다.\"," +
                    "\"redirectUrl\":\"/login\"}");
        } else {
            // 일반 요청: 로그인 페이지로 리다이렉트
            String returnUrl = request.getRequestURI();
            String queryString = request.getQueryString();
            if (queryString != null) {
                returnUrl += "?" + queryString;
            }
            response.sendRedirect("/login?redirect=" + java.net.URLEncoder.encode(returnUrl, "UTF-8"));
        }

        return false;
    }
}
