package io.github.kyungbeom.identity_server.oauth2.authorize.controller;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.oauth2.authorize.service.AuthorizeService;
import io.github.kyungbeom.identity_server.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 인가 엔드포인트.
 * 로그인한 사용자의 요청을 검증하고, 통과하면 인가 코드를 redirect_uri 로 돌려준다.
 */
@RestController
@RequiredArgsConstructor
public class AuthorizeController {

    private final AuthorizeService authorizeService;

    @GetMapping("/oauth2/authorize")
    public void authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @AuthenticationPrincipal CustomUserDetails principal,
            HttpServletResponse response) throws IOException {

        // 1. 세션 확인 → 로그인 화면으로
        if (principal == null) {
            response.sendRedirect("/auth/login");
            return;
        }

        // 2~4. redirect_uri 를 믿기 전 검증. 실패하면 그 주소로 보내지 않고 에러로 응답
        Client client = authorizeService.validateRequest(responseType, clientId, redirectUri);

        // 5. scope 가 허용 범위를 벗어나면, 표준대로 redirect_uri 에 error 를 붙여 돌려보냄
        if (!authorizeService.isScopeAllowed(client, scope)) {
            response.sendRedirect(buildRedirect(redirectUri, "error", "invalid_scope", state));
            return;
        }

        // 6~7. (처음이면 자동 연결 후) 인가 코드 발급 → redirect_uri 로 전달
        String code = authorizeService.issueAuthorizationCode(
                client, principal.getMemberId(), redirectUri, scope);
        response.sendRedirect(buildRedirect(redirectUri, "code", code, state));
    }

    // redirect_uri 뒤에 ?code=... 또는 ?error=... 를 (있으면 state 도) 안전하게 붙인다
    private String buildRedirect(String redirectUri, String key, String value, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam(key, value);
        if (state != null && !state.isBlank()) {
            builder.queryParam("state", state);
        }
        return builder.build().toUriString();
    }
}
