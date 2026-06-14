package io.github.kyungbeom.identity_server.security;

import io.github.kyungbeom.identity_server.domain.member.entity.MemberProvider;
import io.github.kyungbeom.identity_server.domain.member.entity.ProviderType;
import io.github.kyungbeom.identity_server.domain.member.repository.MemberProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 폼 로그인 시 LOCAL provider 를 email 로 조회해 인증한다.
 * (소셜 로그인은 별도 흐름이므로 여기서 다루지 않는다.)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberProviderRepository memberProviderRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberProvider provider = memberProviderRepository
                .findByProviderTypeAndProviderUserId(ProviderType.LOCAL, email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다: " + email));

        return new CustomUserDetails(
                provider.getMember().getId(),
                provider.getProviderUserId(),
                provider.getPassword()
        );
    }
}
