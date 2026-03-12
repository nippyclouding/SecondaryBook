package project.util.logInOut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.admin.AdminService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutPendingScheduler {

    private final LogoutPendingManager logoutPendingManager;
    private final AdminService adminService;

    // Admin 5초마다 체크 (1초 만료)
    @Scheduled(fixedRate = 5000)
    public void processAdminLogouts() {
        processLogouts(UserType.ADMIN);
    }

    // Member 5초마다 체크 (60초 만료)
    @Scheduled(fixedRate = 5000)
    public void processMemberLogouts() {
        processLogouts(UserType.MEMBER);
    }

    private void processLogouts(UserType userType) {
        List<LogoutPendingManager.PendingInfo> expiredList = logoutPendingManager.getExpiredPendings(userType);

        if (expiredList.isEmpty()) {
            return;
        }

        for (LogoutPendingManager.PendingInfo info : expiredList) {
            try {
                String ip = info.getIpAddress() != null ? info.getIpAddress() : "SESSION_CLOSED";

                // 타입별 로그아웃 로그 기록
                if (userType == UserType.ADMIN) {
                    adminService.recordAdminLogout(info.getUserSeq(),ip);
                } else {
                    adminService.recordMemberLogout(info.getUserSeq(), ip);
                }
                // 강제 로그아웃 대상에 추가
                logoutPendingManager.addForceLogout(userType, info.getUserSeq());

                // pending에서 제거
                logoutPendingManager.removePending(userType, info.getUserSeq());
                log.info("{} 로그아웃 처리 : userSeq={}", userType, info.getUserSeq());
            } catch (Exception e) {
                log.error("{} 로그아웃 처리 실패 : userSeq={}", userType, info.getUserSeq(), e);
                logoutPendingManager.removePending(userType, info.getUserSeq());
            }
        }
    }
}
