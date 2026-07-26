package io.github.kyungbeom.identity_server.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 회원 / 인증
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // OAuth2
    UNSUPPORTED_RESPONSE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 response_type 입니다."),
    INVALID_CLIENT(HttpStatus.BAD_REQUEST, "유효하지 않은 client 입니다."),
    INVALID_REDIRECT_URI(HttpStatus.BAD_REQUEST, "허용되지 않은 redirect_uri 입니다."),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
