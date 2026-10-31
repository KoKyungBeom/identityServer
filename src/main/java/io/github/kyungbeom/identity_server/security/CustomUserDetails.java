package io.github.kyungbeom.identity_server.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 세션에 저장되는 인증 주체.
 * memberId 는 /oauth2/authorize 에서 인가 코드를 만들 때,
 * clientName 은 "이 세션이 그 프로젝트의 로그인이 맞는지" 확인할 때 쓴다.
 * (A 에 로그인한 세션으로 B 의 authorize 를 타는 걸 막기 위함)
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String clientName;
    private final String email;

    public CustomUserDetails(Long memberId, String clientName, String email) {
        this.memberId = memberId;
        this.clientName = clientName;
        this.email = email;
    }

    /**
     * 비밀번호 비교는 로그인할 때 MemberService 가 이미 끝낸다.
     * 세션에 해시를 들고 다닐 이유가 없어 null 을 돌려준다.
     */
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
