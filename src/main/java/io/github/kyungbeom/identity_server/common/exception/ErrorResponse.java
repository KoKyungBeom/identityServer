package io.github.kyungbeom.identity_server.common.exception;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorDetail> errors
) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldErrorDetail> errors) {
        return new ErrorResponse(errorCode.name(), message, errors);
    }

    public record FieldErrorDetail(String field, String reason) {
    }
}
