package io.github.kyungbeom.identity_server.oauth2.token.service;

import io.github.kyungbeom.identity_server.common.exception.BusinessException;
import io.github.kyungbeom.identity_server.common.exception.ErrorCode;
import io.github.kyungbeom.identity_server.common.util.TokenGenerator;
import io.github.kyungbeom.identity_server.config.JwtProperties;
import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberRepository;
import io.github.kyungbeom.identity_server.oauth2.authorize.model.AuthorizationCode;
import io.github.kyungbeom.identity_server.oauth2.authorize.repository.AuthorizationCodeStore;
import io.github.kyungbeom.identity_server.oauth2.token.dto.TokenRequest;
import io.github.kyungbeom.identity_server.oauth2.token.dto.TokenResponse;
import io.github.kyungbeom.identity_server.oauth2.token.model.RefreshToken;
import io.github.kyungbeom.identity_server.oauth2.token.repository.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 토큰 발급 로직.
 * - authorization_code: 인가 코드를 토큰으로 교환
 * - refresh_token: 기존 refresh 토큰으로 새 토큰 재발급(회전)
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE_OPENID = "openid";

    private final AuthorizationCodeStore authorizationCodeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    public TokenResponse issue(Client client, TokenRequest request) {
        return switch (request.grantType()) {
            case GRANT_TYPE_AUTHORIZATION_CODE -> exchangeAuthorizationCode(client, request);
            case GRANT_TYPE_REFRESH_TOKEN -> refresh(client, request);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_GRANT_TYPE);
        };
    }

    /**
     * 인가 코드 → 토큰.
     * 코드는 Redis 에서 꺼내는 즉시 삭제되므로 한 번만 쓸 수 있다.
     * 발급 당시 기록해둔 client·redirect_uri 와 지금 요청이 같은지 반드시 확인한다.
     */
    private TokenResponse exchangeAuthorizationCode(Client client, TokenRequest request) {
        if (request.code() == null || request.redirectUri() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_REQUEST);
        }
        AuthorizationCode authorizationCode = authorizationCodeStore.consume(request.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_GRANT));

        // 다른 client 가 남의 코드를 가로채 쓰지 못하게 막는다
        if (!authorizationCode.getClientId().equals(client.getClientId())
                || !authorizationCode.getRedirectUri().equals(request.redirectUri())) {
            throw new BusinessException(ErrorCode.INVALID_GRANT);
        }
        return issueTokens(client, authorizationCode.getMemberId(), authorizationCode.getScope());
    }

    /**
     * refresh 토큰 → 새 토큰 쌍(회전).
     * 사용한 토큰은 꺼내는 순간 삭제되므로, 탈취된 옛 토큰을 다시 쓰면 거부된다.
     */
    private TokenResponse refresh(Client client, TokenRequest request) {
        if (request.refreshToken() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_REQUEST);
        }
        RefreshToken refreshToken = refreshTokenStore.consume(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_GRANT));

        if (!refreshToken.getClientId().equals(client.getClientId())) {
            throw new BusinessException(ErrorCode.INVALID_GRANT);
        }
        return issueTokens(client, refreshToken.getMemberId(), refreshToken.getScope());
    }

    /** 액세스 토큰(JWT) + refresh 토큰(랜덤 문자열)을 발급하고, openid scope 면 id_token 도 함께 준다. */
    private TokenResponse issueTokens(Client client, Long memberId, String scope) {
        List<String> scopes = toScopes(scope);
        String accessToken = jwtTokenProvider.issueAccessToken(memberId, client.getClientName(), scopes);
        String idToken = scopes.contains(SCOPE_OPENID) ? issueIdToken(client, memberId) : null;

        String refreshToken = TokenGenerator.generate();
        refreshTokenStore.save(refreshToken,
                RefreshToken.of(memberId, client.getClientId(), scope, Instant.now()));

        return TokenResponse.of(accessToken, idToken, jwtProperties.accessTokenTtl().toSeconds(),
                refreshToken, scope);
    }

    /** id_token 에는 사용자 정보가 들어가므로 회원을 실제로 조회한다. */
    private String issueIdToken(Client client, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return jwtTokenProvider.issueIdToken(
                memberId, client.getClientName(), member.getEmail(), member.getNickname());
    }

    // 공백으로 구분된 scope 문자열을 목록으로
    private List<String> toScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.asList(scope.trim().split("\\s+"));
    }
}
