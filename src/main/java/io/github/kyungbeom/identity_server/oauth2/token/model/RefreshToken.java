package io.github.kyungbeom.identity_server.oauth2.token.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * refresh 토큰에 딸린 정보. Redis 에 "refresh_token:{토큰해시}" 키로 저장된다.
 * 재발급 요청이 오면 이 정보로 "누구·어느 client 의 토큰인지" 확인한 뒤 새 토큰을 만든다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    private Long memberId;
    private Integer clientId;   // Client PK
    private String scope;
    private Instant issuedAt;

    private RefreshToken(Long memberId, Integer clientId, String scope, Instant issuedAt) {
        this.memberId = memberId;
        this.clientId = clientId;
        this.scope = scope;
        this.issuedAt = issuedAt;
    }

    public static RefreshToken of(Long memberId, Integer clientId, String scope, Instant issuedAt) {
        return new RefreshToken(memberId, clientId, scope, issuedAt);
    }
}
