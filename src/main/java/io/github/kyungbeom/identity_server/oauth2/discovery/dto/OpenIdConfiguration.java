package io.github.kyungbeom.identity_server.oauth2.discovery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OIDC Discovery 문서
 * client 는 issuer 주소만 알면 이 문서를 읽어 나머지 엔드포인트를 자동으로 알아낸다.
 */
public record OpenIdConfiguration(
        @JsonProperty("issuer") String issuer,
        @JsonProperty("authorization_endpoint") String authorizationEndpoint,
        @JsonProperty("token_endpoint") String tokenEndpoint,
        @JsonProperty("userinfo_endpoint") String userinfoEndpoint,
        @JsonProperty("jwks_uri") String jwksUri,
        @JsonProperty("response_types_supported") List<String> responseTypesSupported,
        @JsonProperty("grant_types_supported") List<String> grantTypesSupported,
        @JsonProperty("subject_types_supported") List<String> subjectTypesSupported,
        @JsonProperty("id_token_signing_alg_values_supported") List<String> idTokenSigningAlgValuesSupported,
        @JsonProperty("scopes_supported") List<String> scopesSupported,
        @JsonProperty("token_endpoint_auth_methods_supported") List<String> tokenEndpointAuthMethodsSupported
) {
}
