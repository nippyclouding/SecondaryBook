package project.util.logInOut;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InMemoryLogoutPendingManager implements LogoutPendingManager {

    // key : "ADMIN:123" 또는 "MEMBER:456" 형태
    private final ConcurrentHashMap<String, PendingInfo> pendingMap = new ConcurrentHashMap<>();

    // 강제 로그아웃 대상
    private final Set<String> forceLogoutSet = ConcurrentHashMap.newKeySet();

    private String makeKey(UserType userType, Long userSeq) {
        return userType.name() + ":" + userSeq;
    }

    @Override
    public void addPending(UserType userType, Long userSeq, String ipAddress) {
        String key = makeKey(userType, userSeq);
        PendingInfo info = new PendingInfo(userType, userSeq, System.currentTimeMillis(), ipAddress);
        pendingMap.put(key, info);
        log.info(">>> [PENDING 등록] type={}, userSeq={}, IP={}", userType, userSeq, ipAddress);
    }

    @Override
    public void removePending(UserType userType, Long userSeq) {
        String key = makeKey(userType, userSeq);
        PendingInfo removed = pendingMap.remove(key);
        if (removed != null) {
            log.debug("로그아웃 pending 제거 : type={}, userSeq={}", userType, userSeq);
        }
    }

    @Override
    public boolean isPending(UserType userType, Long userSeq) {
        String key = makeKey(userType, userSeq);
        return pendingMap.containsKey(key);
    }

    @Override
    public List<PendingInfo> getExpiredPendings(UserType userType) {
        List<PendingInfo> expiredList = new ArrayList<>();

        for (Map.Entry<String, PendingInfo> entry : pendingMap.entrySet()) {
            PendingInfo info = entry.getValue();

            // 해당 타입이고 만료된 경우
            if (info.getUserType() == userType && info.isExpired()) {
                expiredList.add(info);
            }
        }
        return expiredList;
    }

    @Override
    public PendingInfo getPendingInfo(UserType userType, Long userSeq) {
        String key = makeKey(userType, userSeq);
        return pendingMap.get(key);
    }

    @Override
    public void addForceLogout(UserType userType, Long userSeq) {
        String key = makeKey(userType, userSeq);
        forceLogoutSet.add(key);
        log.info(">>> [FORCE_LOGOUT 추가] key={} (이제 이 사용자는 다음 활동 시 튕깁니다)", key);
    }

    @Override
    public boolean isForceLogout(UserType userType, Long userSeq) {
        String key = makeKey(userType, userSeq);
        boolean result =  forceLogoutSet.contains(key);

        log.info("isForceLogout 체크: key={}, result={}, forceLogoutSet={}",
                key, result, forceLogoutSet);
        return result;

    }

    @Override
    public void removeForceLogout(UserType userType, Long userSeq) {
        String key = makeKey(userType, userSeq);
        forceLogoutSet.remove(key);
        log.debug("강제 로그아웃 대상 제거: type={}, userSeq={}", userType, userSeq);
    }
}
