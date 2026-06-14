package io.github.kyungbeom.identity_server.domain.member.controller;

import io.github.kyungbeom.identity_server.domain.member.dto.SignupRequest;
import io.github.kyungbeom.identity_server.domain.member.dto.SignupResponse;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberAuthController {

    private final MemberService memberService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        Member member = memberService.signup(request.email(), request.password(), request.nickname());
        return SignupResponse.from(member);
    }
}
