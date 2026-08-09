package io.github.kyungbeom.identity_server.config;

import io.github.kyungbeom.identity_server.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // TODO: 폼 화면(UI)/OAuth2 흐름 도입 시 CSRF 정책 재검토 (계획서 Step 7)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        // 로그인 여부는 컨트롤러가 직접 확인(미로그인 시 로그인 화면으로 보냄)하므로 여기선 막지 않음
                        .requestMatchers("/oauth2/authorize").permitAll()
                        // 사용자 세션이 아니라 client secret 으로 인증하므로(ClientAuthenticator) 여기선 막지 않음
                        .requestMatchers("/oauth2/token").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // API 스타일: 리다이렉트 대신 상태코드만 반환
                        .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                        .failureHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                )
                // 미인증 상태로 보호 자원 접근 시 302 리다이렉트 대신 401 반환
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, e) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .userDetailsService(userDetailsService);

        return http.build();
    }
}
