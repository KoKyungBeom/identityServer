package io.github.kyungbeom.identity_server.oauth2.discovery.controller;

import io.github.kyungbeom.identity_server.config.JwtProperties;
import io.github.kyungbeom.identity_server.oauth2.discovery.dto.OpenIdConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * OIDC Discovery 엔드포인트.
 * 우리가 실제로 지원하는 것만 적어야 한다 — client 가 이 목록을 믿고 요청을 만들기 때문.
 */
@RestController
@RequiredArgsConstructor
public class DiscoveryController {

    private final JwtProperties jwtProperties;

    @GetMapping("/.well-known/openid-configuration")
    public OpenIdConfiguration openIdConfiguration() {
        String issuer = jwtProperties.issuer();

        return new OpenIdConfiguration(
                issuer,
                issuer + "/oauth2/authorize",
                issuer + "/oauth2/token",
                issuer + "/oauth2/userinfo",
                issuer + "/.well-known/jwks.json",
                List.of("code"),                                  // 인가 코드 방식만 지원
                List.of("authorization_code", "refresh_token"),
                List.of("public"),                                // sub 를 모든 client 에 동일하게 발급
                List.of("RS256"),
                List.of("openid", "profile", "email"),
                List.of("client_secret_basic", "client_secret_post")   // Basic 헤더 / 폼 파라미터
        );
    }
}
