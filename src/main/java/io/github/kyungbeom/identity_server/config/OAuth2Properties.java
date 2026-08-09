package io.github.kyungbeom.identity_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * app.oauth2.* 설정 바인딩.
 * Redis 에 저장되는 두 값(인가 코드 / refresh 토큰)의 유효 기간.
 */
@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(
        Duration authorizationCodeTtl,
        Duration refreshTokenTtl
) {
}
