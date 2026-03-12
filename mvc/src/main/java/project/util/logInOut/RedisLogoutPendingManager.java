package project.util.logInOut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class RedisLogoutPendingManager implements LogoutPendingManager {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PENDING_PREFIX = "logout:pending:";
    private static final String FORCE_PREFIX = "logout:force:";

    private String pendingKey(UserType userType, Long userSeq) {
        return PENDING_PREFIX + userType.name() + ":" + userSeq;
    }

    private String forceKey(UserType userType, Long userSeq) {
        return FORCE_PREFIX + userType.name() + ":" + userSeq;
    }

    @Override
    public void addPending(UserType userType, Long userSeq, String ipAddress) {
        String key = pendingKey(userType, userSeq);
        PendingInfo info = new PendingInfo(userType, userSeq, System.currentTimeMillis(), ipAddress);
        try {
            String json = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set(key, json, userType.getTimeoutSeconds(), TimeUnit.SECONDS);
            log.info(">>> [PENDING 등록] type={}, userSeq={}, IP={}", userType, userSeq, ipAddress);
        } catch (JsonProcessingException e) {
            log.error("PendingInfo 직렬화 실패: type={}, userSeq={}", userType, userSeq, e);
        }
    }

    @Override
    public void removePending(UserType userType, Long userSeq) {
        String key = pendingKey(userType, userSeq);
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("로그아웃 pending 제거 : type={}, userSeq={}", userType, userSeq);
        }
    }

    @Override
    public boolean isPending(UserType userType, Long userSeq) {
        String key = pendingKey(userType, userSeq);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public List<PendingInfo> getExpiredPendings(UserType userType) {
        // Redis TTL로 만료가 자동 처리되므로 만료된 항목은 이미 삭제됨
        // 빈 리스트 반환 (InMemory와 달리 만료 항목 수동 정리 불필요)
        return new ArrayList<>();
    }

    @Override
    public PendingInfo getPendingInfo(UserType userType, Long userSeq) {
        String key = pendingKey(userType, userSeq);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PendingInfo.class);
        } catch (JsonProcessingException e) {
            log.error("PendingInfo 역직렬화 실패: key={}", key, e);
            return null;
        }
    }

    @Override
    public void addForceLogout(UserType userType, Long userSeq) {
        String key = forceKey(userType, userSeq);
        // 강제 로그아웃 플래그: 24시간 TTL (충분히 긴 시간)
        redisTemplate.opsForValue().set(key, "true", 24, TimeUnit.HOURS);
        log.info(">>> [FORCE_LOGOUT 추가] key={} (이제 이 사용자는 다음 활동 시 튕깁니다)", key);
    }

    @Override
    public boolean isForceLogout(UserType userType, Long userSeq) {
        String key = forceKey(userType, userSeq);
        boolean result = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        log.info("isForceLogout 체크: key={}, result={}", key, result);
        return result;
    }

    @Override
    public void removeForceLogout(UserType userType, Long userSeq) {
        String key = forceKey(userType, userSeq);
        redisTemplate.delete(key);
        log.debug("강제 로그아웃 대상 제거: type={}, userSeq={}", userType, userSeq);
    }
}
