package project.util.logInOut;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public interface LogoutPendingManager {
    // 로그아웃 대기 상태 추가
    void addPending(UserType userType, Long userSeq, String ipAddress);

    // 로그아웃 대기 상태 제거
    void removePending(UserType userType, Long userSeq);

    // 대기 중인지 확인
    boolean isPending(UserType userType, Long userSeq);

    // 특정 타입의 만료된 pending 목록 조회
    List<PendingInfo> getExpiredPendings(UserType userType);

    // pending 정보 조회
    PendingInfo getPendingInfo(UserType userType, Long userSeq);

    // 강제 로그아웃 대상 추가
    void addForceLogout(UserType userType, Long userSeq);

    // 강제 로그아웃 대상인지 확인
    boolean isForceLogout(UserType userType, Long userSeq);

    // 강제 로그아웃 대상에서 제거
    void removeForceLogout(UserType userType, Long userSeq);

    // pending 정보 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class PendingInfo {
        private UserType userType;
        private Long userSeq;
        private Long timestamp;
        private String ipAddress;

        // 만료 여부 확인
        public boolean isExpired() {
            long elapsed = System.currentTimeMillis() - timestamp;
            return elapsed > (userType.getTimeoutSeconds() * 1000L);
        }
    }
}
