package io.github.kyungbeom.identity_server.oauth2.token.service;

import io.github.kyungbeom.identity_server.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * RS256 JWT 발급 책임.
 * - access token: API 호출용. "무엇을 할 수 있는지"(scope)를 담는다
 * - id token: OIDC 용. "사용자가 누구인지"를 담아 client 앱이 직접 읽는다
 * refresh token 은 JWT 가 아닌 랜덤 문자열이라 여기서 다루지 않는다.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    /**
     * @param memberId 토큰 주체(sub)
     * @param clientId 대상 클라이언트(aud)
     * @param scopes   공백 구분 scope 클레임으로 직렬화
     */
    public String issueAccessToken(Long memberId, String clientId, Collection<String> scopes) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenTtl()))
                .subject(String.valueOf(memberId))
                .audience(List.of(clientId))
                .claim("scope", String.join(" ", scopes))
                .build();

        return encode(claims);
    }

    /**
     * id_token(OIDC): scope 에 openid 가 있을 때만 발급된다.
     * access token 과 달리 client 앱이 직접 열어 "누가 로그인했는지" 확인하는 용도라 사용자 정보를 담는다.
     */
    public String issueIdToken(Long memberId, String clientId, String email, String nickname) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(properties.idTokenTtl()))
                .subject(String.valueOf(memberId))
                .audience(List.of(clientId))
                .claim("email", email)
                .claim("nickname", nickname)
                .build();

        return encode(claims);
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
