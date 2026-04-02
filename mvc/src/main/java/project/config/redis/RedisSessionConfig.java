package project.config.redis;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Session + Redis 설정
 * - HttpSession을 Redis에 저장하여 다중 서버(Auto Scaling) 환경에서 세션 공유
 * - maxInactiveIntervalInSeconds: 세션 만료 시간 (1800초 = 30분, web.xml의 session-timeout과 동일)
 * - redisNamespace: Redis에 저장되는 세션 키의 네임스페이스
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800, redisNamespace = "secondarybook:session")
public class RedisSessionConfig {
}
