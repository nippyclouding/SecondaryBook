package project.util.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import project.admin.AdminVO;
import project.util.logInOut.LogoutPendingManager;
import project.util.logInOut.UserType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAuthInterceptor implements HandlerInterceptor {
    private final LogoutPendingManager logoutPendingManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        log.info("=== AdminAuthInterceptor 실행: uri={} ===", uri);
        // 세션 확인
        HttpSession sess = request.getSession(false);

        if (sess == null) {
            return handleUnauthorized(request, response);
        }

        AdminVO admin = (AdminVO) sess.getAttribute("adminSess");

        if (admin == null) {
            return handleUnauthorized(request, response);
        }

        // ★ 강제 로그아웃 체크 ★
        if (logoutPendingManager.isForceLogout(UserType.ADMIN, admin.getAdmin_seq())) {
            // 세션 무효화
            request.getSession().invalidate();
            // 강제 로그아웃 대상에서 제거
            logoutPendingManager.removeForceLogout(UserType.ADMIN, admin.getAdmin_seq());

            response.sendRedirect("/admin/login");
            return false;
        } else {
            log.debug("Not in force logout list. Seq: {}", admin.getAdmin_seq());
        }
        // pending 상태 제거 (활동 감지)
        // logout-pending API는 제외
        if (!uri.contains("/admin/api/")) {
            logoutPendingManager.removePending(UserType.ADMIN, admin.getAdmin_seq());
        }

        return true;
    }

    private boolean handleUnauthorized(HttpServletRequest request,
                                       HttpServletResponse response)
            throws Exception {

        // AJAX 요청인지 확인
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        if (isAjax) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"로그인이 필요합니다.\"}");
        } else {
            response.sendRedirect("/admin/login");
        }

        return false;
    }
}
