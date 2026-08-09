package io.github.kyungbeom.identity_server.oauth2.token.dto;

/**
 * 토큰 요청 파라미터 묶음.
 * grant_type 에 따라 쓰이는 값이 다르다 (코드 교환은 code/redirect_uri, 재발급은 refresh_token).
 */
public record TokenRequest(
        String grantType,
        String code,
        String redirectUri,
        String refreshToken
) {
}
