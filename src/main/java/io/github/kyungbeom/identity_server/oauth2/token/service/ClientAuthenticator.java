package io.github.kyungbeom.identity_server.oauth2.token.service;

import io.github.kyungbeom.identity_server.common.exception.BusinessException;
import io.github.kyungbeom.identity_server.common.exception.ErrorCode;
import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.client.entity.ClientSecret;
import io.github.kyungbeom.identity_server.domain.client.repository.ClientRepository;
import io.github.kyungbeom.identity_server.domain.client.repository.ClientSecretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 토큰 요청을 보낸 client 가 진짜인지 확인한다.
 * <p>
 * /oauth2/authorize 는 브라우저(사용자)가 호출하지만, /oauth2/token 은 client 앱의 서버가 호출한다.
 * 그래서 사용자 로그인과 별개로 "이 요청이 정말 그 client 가 보낸 것인지"를 secret 으로 확인해야 한다.
 * secret 은 회원 비밀번호처럼 DB 에 해시로만 저장돼 있어 매칭으로 검사한다.
 */
@Component
@RequiredArgsConstructor
public class ClientAuthenticator {

    private static final String BASIC_PREFIX = "Basic ";

    private final ClientRepository clientRepository;
    private final ClientSecretRepository clientSecretRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 표준이 허용하는 두 가지 방식 모두 지원한다.
     * 1) Authorization: Basic base64(client_id:client_secret)  ← 권장
     * 2) 폼 파라미터 client_id / client_secret
     */
    @Transactional(readOnly = true)
    public Client authenticate(String authorizationHeader, String clientIdParam, String clientSecretParam) {
        String[] credentials = extractCredentials(authorizationHeader, clientIdParam, clientSecretParam);
        String clientId = credentials[0];
        String clientSecret = credentials[1];

        Client client = clientRepository.findByClientName(clientId)
                .filter(Client::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CLIENT_CREDENTIALS));

        // 활성 secret 이 여러 개일 수 있어(무중단 교체) 그 중 하나라도 맞으면 통과
        boolean matched = clientSecretRepository
                .findActiveByClientId(client.getClientId(), LocalDateTime.now())
                .stream()
                .anyMatch(secret -> matches(clientSecret, secret));
        if (!matched) {
            throw new BusinessException(ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }
        return client;
    }

    private boolean matches(String rawSecret, ClientSecret storedSecret) {
        return passwordEncoder.matches(rawSecret, storedSecret.getSecretHash());
    }

    /** Basic 헤더가 있으면 우선 사용하고, 없으면 폼 파라미터를 쓴다. 둘 다 없으면 인증 실패. */
    private String[] extractCredentials(String authorizationHeader, String clientIdParam, String clientSecretParam) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BASIC_PREFIX)) {
            return decodeBasic(authorizationHeader.substring(BASIC_PREFIX.length()));
        }
        if (clientIdParam == null || clientSecretParam == null) {
            throw new BusinessException(ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }
        return new String[]{clientIdParam, clientSecretParam};
    }

    private String[] decodeBasic(String encoded) {
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }
        // "client_id:client_secret" — secret 에 콜론이 있을 수 있으므로 첫 콜론만 기준으로 자른다
        int separator = decoded.indexOf(':');
        if (separator < 0) {
            throw new BusinessException(ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }
        return new String[]{decoded.substring(0, separator), decoded.substring(separator + 1)};
    }
}
