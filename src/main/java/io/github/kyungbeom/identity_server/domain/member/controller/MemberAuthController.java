package io.github.kyungbeom.identity_server.domain.member.controller;

import io.github.kyungbeom.identity_server.domain.member.dto.SignupRequest;
import io.github.kyungbeom.identity_server.domain.member.dto.SignupResponse;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.service.MemberService;
import io.github.kyungbeom.identity_server.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberAuthController {

    private final MemberService memberService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        Member member = memberService.signup(
                request.clientId(), request.email(), request.password(), request.nickname());
        return SignupResponse.from(member);
    }

    /**
     * 로그인. Spring Security 의 기본 폼 로그인(formLogin) 대신 직접 처리한다.
     * 기본 폼 로그인은 UserDetailsService.loadUserByUsername(email) 로 사람을 찾는데,
     * 여기엔 "어느 프로젝트" 를 넘길 자리가 없다. 프로젝트마다 회원 풀이 따로라
     * 같은 이메일이 여러 개 존재할 수 있으므로 client_id 를 함께 받아 직접 조회한다.
     * client_id 는 /oauth2/authorize 요청에 있던 값이 로그인 화면을 거쳐 전달된 것이다.
     */
    @PostMapping("/login")
    public void login(
            @RequestParam("client_id") String clientId,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {

        CustomUserDetails principal = memberService.authenticate(clientId, email, password);

        // 로그인 전에 쓰던 세션 ID 를 새로 바꾼다(세션 고정 공격 방지).
        // changeSessionId() 는 세션이 있어야 호출할 수 있어서 먼저 만들어 둔다.
        request.getSession(true);
        request.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        // Spring Security 6 부터는 이 저장을 직접 해줘야 세션에 남는다
        securityContextRepository.saveContext(context, request, response);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
