package project.util.logInOut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import project.admin.AdminVO;
import project.member.MemberVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LogoutPendingController {

    private final LogoutPendingManager logoutPendingManager;

    // admin 로그아웃 pending
    @PostMapping("/admin/api/logout-pending")
    public Map<String, Object> adminLogoutPending(HttpServletRequest request,
                                                  HttpSession sess) {
        return handleLogoutPending(UserType.ADMIN, "adminSess", request, sess);
    }

    // admin 로그아웃 취소
    @PostMapping("/admin/api/cancel-logout")
    public Map<String, Object> adminCancelLogout(HttpSession sess) {
        return handleCancelLogout(UserType.ADMIN, "adminSess", sess);
    }

    @GetMapping("/admin/api/session-check")
    public Map<String, Object> sessionCheck(HttpSession sess) {
        Map<String, Object> result = new HashMap<>();
        Object admin = sess.getAttribute("adminSess");

        result.put("valid", admin != null);
        // 세션 만료 시간 계산 로직 (필요시)
        result.put("remainingSeconds", sess.getMaxInactiveInterval());
        return result;
    }

    // member 로그아웃 pending
    @PostMapping("/api/member/logout-pending")
    public Map<String, Object> memberLogoutPending(HttpServletRequest request,
                                                  HttpSession sess) {
        return handleLogoutPending(UserType.MEMBER, "loginSess", request, sess);
    }

    // member 로그아웃 취소
    @PostMapping("/api/member/cancel-logout")
    public Map<String, Object> memberCancelLogout(HttpSession sess) {
        return handleCancelLogout(UserType.MEMBER, "loginSess", sess);
    }



    // 공통처리 메서드
    private Map<String, Object> handleLogoutPending(UserType userType,
                                                    String sessionKey,
                                                    HttpServletRequest request,
                                                    HttpSession sess) {
        Map<String, Object> result = new HashMap<>();
        Object user = sess.getAttribute(sessionKey);

        if (user != null) {
            Long userSeq = extractUserSeq(userType, user);
            String ip = getClientIP(request);

            logoutPendingManager.addPending(userType, userSeq, ip);
            result.put("success", true);
        } else {
            result.put("success", false);
        }
        return result;
    }

    private Map<String, Object> handleCancelLogout(UserType userType,
                                                   String sessionKey,
                                                   HttpSession sess) {
        Map<String,Object> result = new HashMap<>();

        Object user = sess.getAttribute(sessionKey);

        if (user != null) {
            Long userSeq = extractUserSeq(userType, user);
            logoutPendingManager.removePending(userType, userSeq);
            logoutPendingManager.removeForceLogout(userType, userSeq);
            result.put("success", true);
        } else {
            result.put("success", false);
        }
        return result;
    }
    private Long extractUserSeq(UserType userType, Object user) {
        if (userType == UserType.ADMIN) {
            return ((AdminVO) user).getAdmin_seq();
        } else {
            return ((MemberVO) user).getMember_seq();
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
}
