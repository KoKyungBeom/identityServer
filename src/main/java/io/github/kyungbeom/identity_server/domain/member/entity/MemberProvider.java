package io.github.kyungbeom.identity_server.domain.member.entity;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 로그인 수단(LOCAL / 소셜).
 * client_id 는 member 를 타고 가면 알 수 있는 값이지만 일부러 중복해서 들고 있다.
 * 그래야 "같은 프로젝트 안에서 같은 계정 중복 가입"을 DB 유니크 제약으로 막을 수 있다.
 * (Keycloak 도 realm_id 를 이런 식으로 중복 보관한다.)
 */
@Entity
@Table(
        name = "member_providers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_providers_client_type_user",
                columnNames = {"client_id", "provider_type", "provider_user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    private ProviderType providerType;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    // LOCAL 가입에만 사용 (BCrypt 해시). 소셜은 null.
    @Column(length = 255)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static MemberProvider local(Member member, String email, String hashedPassword) {
        MemberProvider provider = new MemberProvider();
        provider.member = member;
        // 로그인 수단의 소속은 언제나 회원의 소속과 같다
        provider.client = member.getClient();
        provider.providerType = ProviderType.LOCAL;
        provider.providerUserId = email;
        provider.password = hashedPassword;
        return provider;
    }
}
