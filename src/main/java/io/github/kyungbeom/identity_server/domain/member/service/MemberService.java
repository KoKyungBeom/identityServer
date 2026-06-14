package io.github.kyungbeom.identity_server.domain.member.service;

import io.github.kyungbeom.identity_server.common.exception.BusinessException;
import io.github.kyungbeom.identity_server.common.exception.ErrorCode;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.entity.MemberProvider;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberProviderRepository;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProviderRepository memberProviderRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * LOCAL 회원가입: members + member_providers(LOCAL) 동시 생성.
     * provider_user_id 는 LOCAL 의 경우 email 과 동일하게 둔다.
     */
    @Transactional
    public Member signup(String email, String rawPassword, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberRepository.save(Member.create(email, nickname));

        String hashedPassword = passwordEncoder.encode(rawPassword);
        memberProviderRepository.save(MemberProvider.local(member, email, hashedPassword));

        return member;
    }
}
