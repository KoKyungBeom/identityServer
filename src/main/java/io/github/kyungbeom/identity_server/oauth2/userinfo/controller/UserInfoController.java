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
        return userInfoService.getUserInfo(memberId, jwt.getClaimAsString("scope"));
    }
}
