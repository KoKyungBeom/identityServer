package io.github.kyungbeom.identity_server.oauth2.authorize;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인가 코드에 딸린 정보. Redis 에 "auth_code:{코드}" 키로 5분간 저장된다.
 * 나중에 /oauth2/token 에서 코드를 받을 때, "누구·어느 client·어느 redirect_uri 용이었는지" 대조하는 데 쓴다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthorizationCode {

    private Long memberId;
    private Integer clientId;   // Client PK
    private String redirectUri;
    private String scope;

    private AuthorizationCode(Long memberId, Integer clientId, String redirectUri, String scope) {
        this.memberId = memberId;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    public static AuthorizationCode of(Long memberId, Integer clientId, String redirectUri, String scope) {
        return new AuthorizationCode(memberId, clientId, redirectUri, scope);
    }
}
