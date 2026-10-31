package io.github.kyungbeom.identity_server.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * userinfo 전용 체인. 세션이 아니라 Bearer 액세스 토큰(JWT)으로 인증하므로 따로 분리한다.
     * securityMatcher 로 이 경로에만 적용되고 나머지는 아래 기본 체인이 처리한다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/userinfo")
                .csrf(csrf -> csrf.disable())
                // 토큰만으로 판단하므로 세션을 만들지 않는다
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // JwtDecoder Bean(공개키 검증)을 그대로 사용한다
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * 기본 체인: 세션 기반.
     * 로그인은 formLogin 이 아니라 MemberAuthController 가 직접 처리한다 —
     * 회원 풀이 프로젝트별로 나뉘어 있어 이메일만으로는 사람을 특정할 수 없기 때문.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // TODO: 폼 화면(UI)/OAuth2 흐름 도입 시 CSRF 정책 재검토 (계획서 Step 7)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                        // 로그인 자체는 당연히 미인증 상태로 들어온다
                        .requestMatchers("/auth/login").permitAll()
                        // 공개키·설명서는 항상 열어둔다
                        .requestMatchers("/.well-known/**").permitAll()
                        // 로그인 여부는 컨트롤러가 직접 확인(미로그인 시 로그인 화면으로 보냄)하므로 여기선 막지 않음
                        .requestMatchers("/oauth2/authorize").permitAll()
                        // 사용자 세션이 아니라 client secret 으로 인증하므로(ClientAuthenticator) 여기선 막지 않음
                        .requestMatchers("/oauth2/token").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                )
                // 미인증 상태로 보호 자원 접근 시 302 리다이렉트 대신 401 반환
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, e) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)));

        return http.build();
    }
}
