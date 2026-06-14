package io.github.kyungbeom.identity_server.domain.member.dto;

import io.github.kyungbeom.identity_server.domain.member.entity.Member;

public record SignupResponse(
        Long id,
        String email,
        String nickname
) {

    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
