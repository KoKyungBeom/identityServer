package io.github.kyungbeom.identity_server.oauth2.jwks.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 공개키를 표준 형식(JWKS)으로 공개한다.
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final RSAKey rsaKey;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        // toPublicJWK(): 개인키 부분을 제거하고 공개키만 남긴다. 절대 개인키가 나가면 안 된다
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
