package io.github.kyungbeom.identity_server.oauth2.token.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 토큰 응답 (OAuth2 표준 형식).
 * JSON 키가 snake_case 로 정해져 있어 @JsonProperty 로 이름을 맞춘다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        // openid scope 일 때만 채워진다.
        @JsonProperty("id_token") String idToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,      // 액세스 토큰 남은 수명(초)
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("scope") String scope
) {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    public static TokenResponse of(String accessToken, String idToken, long expiresIn,
                                   String refreshToken, String scope) {
        return new TokenResponse(accessToken, idToken, TOKEN_TYPE_BEARER, expiresIn, refreshToken, scope);
    }
}
