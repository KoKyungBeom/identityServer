package io.github.kyungbeom.identity_server.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.ByteArrayInputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * base64(env var) → PEM → RSA 키페어를 읽어 Nimbus RSAKey Bean 으로 등록한다.
 * <p>
 * 키 로딩 방식(env var/파일)을 바꾸려면 이 클래스만 수정하면 된다 —
 * 토큰 발급/검증 측은 RSAKey Bean 만 주입받으므로 영향받지 않는다.
 * Phase 1 은 단일 활성 키. kid 는 공개키 thumbprint(SHA-256)에서 안정적으로 도출.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class RsaKeyConfig {

    private final JwtProperties properties;

    @Bean
    public RSAKey rsaKey() throws Exception {
        RSAPublicKey publicKey = decodePublicKey(properties.publicKey());
        RSAPrivateKey privateKey = decodePrivateKey(properties.privateKey());

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyIDFromThumbprint()
                .build();
    }

    private RSAPrivateKey decodePrivateKey(String base64Pem) {
        byte[] pem = Base64.getDecoder().decode(base64Pem);
        return RsaKeyConverters.pkcs8().convert(new ByteArrayInputStream(pem));
    }

    private RSAPublicKey decodePublicKey(String base64Pem) {
        byte[] pem = Base64.getDecoder().decode(base64Pem);
        return RsaKeyConverters.x509().convert(new ByteArrayInputStream(pem));
    }
}
