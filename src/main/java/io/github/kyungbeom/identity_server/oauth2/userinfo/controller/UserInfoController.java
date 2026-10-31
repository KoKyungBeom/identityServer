package io.github.kyungbeom.identity_server.oauth2.userinfo.controller;

import io.github.kyungbeom.identity_server.oauth2.userinfo.dto.UserInfoResponse;
import io.github.kyungbeom.identity_server.oauth2.userinfo.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    @GetMapping("/oauth2/userinfo")
    public UserInfoResponse userInfo(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());          // 발급 때 넣은 sub
        // aud 는 이 토큰을 쓸 수 있는 프로젝트. 회원의 소속과 맞는지는 서비스에서 확인한다.
        return userInfoService.getUserInfo(memberId, jwt.getAudience(), jwt.getClaimAsString("scope"));
    }
}
