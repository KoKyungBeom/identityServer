package io.github.kyungbeom.identity_server.common.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 추측할 수 없는 랜덤 문자열 생성기 (인가 코드 / refresh 토큰용).
 * URL 에 그대로 담을 수 있는 형태로 만든다.
 */
public final class TokenGenerator {

    private static final int BYTE_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
