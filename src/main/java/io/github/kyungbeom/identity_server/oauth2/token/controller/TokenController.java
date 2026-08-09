package io.github.kyungbeom.identity_server.oauth2.token.controller;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.oauth2.token.dto.TokenRequest;
import io.github.kyungbeom.identity_server.oauth2.token.dto.TokenResponse;
import io.github.kyungbeom.identity_server.oauth2.token.service.ClientAuthenticator;
import io.github.kyungbeom.identity_server.oauth2.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2 토큰 엔드포인트.
 * client 앱의 서버가 호출하며, 인가 코드나 refresh 토큰을 실제 토큰으로 바꿔준다.
 */
@RestController
@RequiredArgsConstructor
public class TokenController {

    private final ClientAuthenticator clientAuthenticator;
    private final TokenService tokenService;

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        // 1. 요청을 보낸 client 가 진짜인지 먼저 확인
        Client client = clientAuthenticator.authenticate(authorization, clientId, clientSecret);

        // 2. grant_type 에 맞춰 토큰 발급
        return tokenService.issue(client, new TokenRequest(grantType, code, redirectUri, refreshToken));
    }
}
