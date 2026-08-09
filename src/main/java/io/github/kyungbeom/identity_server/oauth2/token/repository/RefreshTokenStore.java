package io.github.kyungbeom.identity_server.oauth2.token.repository;

import io.github.kyungbeom.identity_server.config.OAuth2Properties;
import io.github.kyungbeom.identity_server.oauth2.token.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * refresh 토큰을 Redis 에 저장하고 꺼낸다.
 * <p>
 * 토큰 원본을 그대로 저장하지 않고 SHA-256 해시를 키로 쓴다.
 * Redis 내용이 유출돼도 해시로는 원본 토큰을 되돌릴 수 없어 피해를 줄일 수 있다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh_token:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final OAuth2Properties properties;

    public void save(String rawToken, RefreshToken value) {
        redisTemplate.opsForValue().set(key(rawToken), value, properties.refreshTokenTtl());
    }

    /** 꺼내면서 동시에 지운다 → 한 번 쓴 refresh 토큰은 재사용 불가(회전). */
    public Optional<RefreshToken> consume(String rawToken) {
        Object value = redisTemplate.opsForValue().getAndDelete(key(rawToken));
        return Optional.ofNullable((RefreshToken) value);
    }

    private String key(String rawToken) {
        return KEY_PREFIX + sha256(rawToken);
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
