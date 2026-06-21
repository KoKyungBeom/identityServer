package io.github.kyungbeom.identity_server.token;

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
 * 액세스 토큰(RS256 JWT) 발급 책임.
 * Phase 1 은 access token 만 다룬다. refresh token(opaque, Redis)은 OAuth2 흐름 도입 시 별도 추가.
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

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
