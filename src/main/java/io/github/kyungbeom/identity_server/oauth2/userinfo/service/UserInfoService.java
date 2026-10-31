package io.github.kyungbeom.identity_server.oauth2.userinfo.service;

import io.github.kyungbeom.identity_server.common.exception.BusinessException;
import io.github.kyungbeom.identity_server.common.exception.ErrorCode;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberRepository;
import io.github.kyungbeom.identity_server.oauth2.userinfo.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 액세스 토큰의 주인 정보를 돌려준다.
 * scope 에 허용된 항목만 담는다 — profile 이 없으면 nickname 도 안 나간다.
 */
@Service
@RequiredArgsConstructor
public class UserInfoService {

    private static final String SCOPE_PROFILE = "profile";
    private static final String SCOPE_EMAIL = "email";

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long memberId, List<String> audience, String scope) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 토큰이 이 회원의 프로젝트에서 발급된 것인지 확인한다.
        // sub(회원 번호)는 프로젝트를 가리지 않고 매겨지므로, 이 확인이 없으면
        // A 프로젝트 토큰으로 B 프로젝트 회원 정보를 꺼낼 수 있게 된다.
        if (audience == null || !audience.contains(member.getClient().getClientName())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_AUDIENCE);
        }

        Set<String> scopes = toScopes(scope);
        return new UserInfoResponse(
                String.valueOf(member.getId()),
                scopes.contains(SCOPE_PROFILE) ? member.getNickname() : null,
                scopes.contains(SCOPE_EMAIL) ? member.getEmail() : null
        );
    }

    private Set<String> toScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(scope.trim().split("\\s+")).collect(Collectors.toSet());
    }
}
