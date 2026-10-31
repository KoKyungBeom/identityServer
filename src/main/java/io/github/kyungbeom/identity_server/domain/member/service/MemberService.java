package io.github.kyungbeom.identity_server.domain.member.service;

import io.github.kyungbeom.identity_server.common.exception.BusinessException;
import io.github.kyungbeom.identity_server.common.exception.ErrorCode;
import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.client.repository.ClientRepository;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.entity.MemberProvider;
import io.github.kyungbeom.identity_server.domain.member.entity.ProviderType;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberProviderRepository;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberRepository;
import io.github.kyungbeom.identity_server.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ClientRepository clientRepository;
    private final MemberRepository memberRepository;
    private final MemberProviderRepository memberProviderRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * LOCAL 회원가입: members + member_providers(LOCAL) 동시 생성.
     * provider_user_id 는 LOCAL 의 경우 email 과 동일하게 둔다.
     * 중복 검사는 프로젝트 안에서만 한다 — 다른 프로젝트에 같은 이메일이 있어도 상관없다.
     */
    @Transactional
    public Member signup(String clientName, String email, String rawPassword, String nickname) {
        Client client = findActiveClient(clientName);

        if (memberRepository.existsByClientAndEmail(client, email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberRepository.save(Member.create(client, email, nickname));

        String hashedPassword = passwordEncoder.encode(rawPassword);
        memberProviderRepository.save(MemberProvider.local(member, email, hashedPassword));

        return member;
    }

    /**
     * 로그인 검증. "이 프로젝트의 이 이메일 + 이 비밀번호가 맞는가"만 판단한다.
     * 세션에 저장하는 일은 컨트롤러가 한다(여기는 DB 와 비밀번호만 다룬다).
     *
     * 이메일만으로는 사람을 특정할 수 없어서 clientName 을 함께 받는다.
     * Spring Security 기본 폼 로그인을 쓰지 않는 이유가 이것 —
     * UserDetailsService.loadUserByUsername(email) 에는 프로젝트를 넘길 자리가 없다.
     */
    @Transactional(readOnly = true)
    public CustomUserDetails authenticate(String clientName, String email, String rawPassword) {
        Client client = findActiveClient(clientName);

        MemberProvider provider = memberProviderRepository
                .findByClientAndProviderTypeAndProviderUserId(client, ProviderType.LOCAL, email)
                // 계정이 없는 경우와 비밀번호가 틀린 경우를 같은 응답으로 묶는다
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(rawPassword, provider.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return new CustomUserDetails(provider.getMember().getId(), client.getClientName(), email);
    }

    private Client findActiveClient(String clientName) {
        return clientRepository.findByClientName(clientName)
                .filter(Client::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CLIENT));
    }
}
