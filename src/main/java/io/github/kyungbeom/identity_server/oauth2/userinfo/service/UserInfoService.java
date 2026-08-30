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
    public UserInfoResponse getUserInfo(Long memberId, String scope) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

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
