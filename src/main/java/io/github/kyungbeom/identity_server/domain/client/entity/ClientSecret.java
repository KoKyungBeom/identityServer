package io.github.kyungbeom.identity_server.domain.client.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_secrets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientSecret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "secret_hash", nullable = false, length = 255)
    private String secretHash;

    @Column(length = 255)
    private String description;

    // null이면 만료 없음 (무중단 회전 시 신규 키는 무제한으로, 구 키는 expired_at 부여)
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ClientSecret create(Client client, String secretHash, String description, LocalDateTime expiredAt) {
        ClientSecret secret = new ClientSecret();
        secret.client = client;
        secret.secretHash = secretHash;
        secret.description = description;
        secret.expiredAt = expiredAt;
        return secret;
    }
}
