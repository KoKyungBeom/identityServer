package io.github.kyungbeom.identity_server.domain.client.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    // 콤마(,) 구분 화이트리스트 URL 목록
    @Column(name = "redirect_uris", nullable = false, columnDefinition = "TEXT")
    private String redirectUris;

    // 콤마(,) 구분 권한 범위 (openid, profile, email 등)
    @Column(name = "allowed_scopes", nullable = false, length = 255)
    private String allowedScopes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Client create(String clientName, String redirectUris, String allowedScopes) {
        Client client = new Client();
        client.clientName = clientName;
        client.redirectUris = redirectUris;
        client.allowedScopes = allowedScopes;
        return client;
    }
}
