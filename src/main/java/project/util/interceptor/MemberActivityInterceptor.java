package project.util.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import project.util.logInOut.LogoutPendingManager;
import project.util.logInOut.UserType;
import project.member.MemberVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberActivityInterceptor implements HandlerInterceptor {

    private final LogoutPendingManager logoutPendingManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();

        //logout-pending API는 제외
        if (uri.contains("logout-pending")) {
            return true;
        }

        HttpSession sess = request.getSession(false);

        if (sess != null) {
            MemberVO memberVO = (MemberVO) sess.getAttribute("loginSess");

            if (memberVO != null) {
                // ★ 강제 로그아웃 체크 ★
                if (logoutPendingManager.isForceLogout(UserType.MEMBER, memberVO.getMember_seq())) {

                    request.getSession().invalidate();
                    // 강제 로그아웃 대상에서 제거
                    logoutPendingManager.removeForceLogout(UserType.MEMBER, memberVO.getMember_seq());

                    log.info("Member 강제 로그아웃 실행: memberSeq={}", memberVO.getMember_seq());

                    // AJAX 요청 감지
                    return handleForceLogout(request, response);
                }
                // pending 상태 제거 (활동 감지)
                logoutPendingManager.removePending(UserType.MEMBER, memberVO.getMember_seq());

            }
        }
        return true;
    }

    /**
     * 강제 로그아웃 처리
     * - AJAX 요청: 401 + JSON 응답
     * - 일반 요청: /login으로 리다이렉트
     */
    private boolean handleForceLogout(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        // Content-Type으로도 AJAX 판단 (fetch API 등)
        String contentType = request.getContentType();
        String accept = request.getHeader("Accept");
        boolean isJsonRequest = (contentType != null && contentType.contains("application/json"))
                || (accept != null && accept.contains("application/json"));

        if (isAjax || isJsonRequest) {
            // AJAX/JSON 요청: 401 + JSON 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"SESSION_EXPIRED\"," +
                    "\"message\":\"세션이 만료되었습니다. 다시 로그인해주세요.\"," +
                    "\"redirectUrl\":\"/login\"}"
            );
        } else {
            // 일반 요청: 로그인 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/login?expired=true");
        }
        return false;
    }
}

