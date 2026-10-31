package io.github.kyungbeom.identity_server.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        // 어느 프로젝트의 회원으로 가입하는지. 이게 없으면 같은 이메일을 어디에 만들지 알 수 없다.
        @JsonProperty("client_id")
        @NotBlank(message = "client_id 는 필수입니다.")
        String clientId,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자를 초과할 수 없습니다.")
        String nickname
) {
}
