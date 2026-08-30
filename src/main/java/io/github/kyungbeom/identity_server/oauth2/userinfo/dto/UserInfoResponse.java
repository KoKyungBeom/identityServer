package io.github.kyungbeom.identity_server.oauth2.userinfo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * userinfo 응답. sub(회원 식별자)는 항상 나가고, 나머지는 토큰의 scope 에 따라 채워진다.
 * 값이 null 인 항목은 응답에서 빠진다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfoResponse(
        String sub,
        String nickname,
        String email
) {
}
