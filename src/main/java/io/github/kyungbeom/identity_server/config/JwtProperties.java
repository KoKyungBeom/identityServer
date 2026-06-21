package io.github.kyungbeom.identity_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * app.jwt.* 설정 바인딩.
 * privateKey/publicKey 는 PEM 을 base64 로 인코딩한 한 줄 문자열(env var 주입).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        String privateKey,
        String publicKey,
        Duration accessTokenTtl
) {
}
