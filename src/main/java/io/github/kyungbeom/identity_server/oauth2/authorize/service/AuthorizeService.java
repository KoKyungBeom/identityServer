package io.github.kyungbeom.identity_server.oauth2.authorize.service;

import io.github.kyungbeom.identity_server.common.exception.BusinessException;
import io.github.kyungbeom.identity_server.common.exception.ErrorCode;
import io.github.kyungbeom.identity_server.common.util.TokenGenerator;
import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.client.repository.ClientRepository;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.entity.MemberClient;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberClientRepository;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberRepository;
import io.github.kyungbeom.identity_server.oauth2.authorize.model.AuthorizationCode;
import io.github.kyungbeom.identity_server.oauth2.authorize.repository.AuthorizationCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorizeService {

    private static final String RESPONSE_TYPE_CODE = "code";

    private final ClientRepository clientRepository;
    private final MemberRepository memberRepository;
    private final MemberClientRepository memberClientRepository;
    private final AuthorizationCodeStore authorizationCodeStore;

    /**
     * 코드 발급 전 기본 검증: response_type / client / redirect_uri 를 확인한다.
     * 여기서 실패하면 redirect_uri 를 아직 믿을 수 없으므로, 그 주소로 보내지 않고 에러로 응답한다.
     * (요청의 client_id 값은 clientName 으로 찾는다.)
     */
    @Transactional(readOnly = true)
    public Client validateRequest(String responseType, String clientId, String redirectUri) {
        if (!RESPONSE_TYPE_CODE.equals(responseType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_RESPONSE_TYPE);
        }
        Client client = clientRepository.findByClientName(clientId)
                .filter(Client::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CLIENT));
        if (!isRegisteredRedirectUri(client, redirectUri)) {
            throw new BusinessException(ErrorCode.INVALID_REDIRECT_URI);
        }
        return client;
    }

    /** 요청한 scope 가 전부 이 client 에게 허용된 범위 안인지 확인한다. scope 를 안 보냈으면 통과. */
    public boolean isScopeAllowed(Client client, String scope) {
        if (scope == null || scope.isBlank()) {
            return true;
        }
        Set<String> allowed = toSet(client.getAllowedScopes());
        return Arrays.stream(scope.trim().split("\\s+")).allMatch(allowed::contains);
    }

    /**
     * 이 회원이 이 client 를 처음 쓰는 거면 자동으로 연결해준다(member_clients). ← 프로젝트 핵심(격리)
     * 그 뒤 인가 코드를 하나 만들어 Redis 에 저장하고 코드를 돌려준다.
     */
    @Transactional
    public String issueAuthorizationCode(Client client, Long memberId, String redirectUri, String scope) {
        // DB 조회 없이 id 만 담은 참조 (연결에만 쓰므로 실제 조회 불필요)
        Member member = memberRepository.getReferenceById(memberId);
        if (!memberClientRepository.existsByMemberAndClient(member, client)) {   // 이미 연결돼 있나?
            memberClientRepository.save(MemberClient.bind(member, client));       // 없으면 새로 연결(자동 가입)
        }
        String code = TokenGenerator.generate();
        authorizationCodeStore.save(code,
                AuthorizationCode.of(memberId, client.getClientId(), redirectUri, scope));
        return code;
    }

    private boolean isRegisteredRedirectUri(Client client, String redirectUri) {
        return toSet(client.getRedirectUris()).contains(redirectUri);
    }

    // 콤마로 저장된 문자열("a,b,c")을 비교하기 쉽게 Set 으로 바꾼다
    private Set<String> toSet(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

}
