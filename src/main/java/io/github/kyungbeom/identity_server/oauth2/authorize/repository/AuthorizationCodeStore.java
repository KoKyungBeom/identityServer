package io.github.kyungbeom.identity_server.oauth2.authorize.repository;

import io.github.kyungbeom.identity_server.config.OAuth2Properties;
import io.github.kyungbeom.identity_server.oauth2.authorize.model.AuthorizationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 인가 코드를 Redis 에 저장하고 꺼낸다. 코드는 5분 뒤 자동으로 사라지고, 한 번 꺼내면 삭제된다(재사용 방지).
 */
@Component
@RequiredArgsConstructor
public class AuthorizationCodeStore {

    private static final String KEY_PREFIX = "auth_code:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final OAuth2Properties properties;

    public void save(String code, AuthorizationCode value) {
        // 정해진 시간(기본 5분) 뒤 자동 삭제되도록 TTL 과 함께 저장
        redisTemplate.opsForValue().set(KEY_PREFIX + code, value, properties.authorizationCodeTtl());
    }

    /** 코드를 꺼내면서 동시에 지운다 → 한 번만 쓸 수 있음(재사용 방지). */
    public Optional<AuthorizationCode> consume(String code) {
        Object value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + code);
        return Optional.ofNullable((AuthorizationCode) value);
    }
}
