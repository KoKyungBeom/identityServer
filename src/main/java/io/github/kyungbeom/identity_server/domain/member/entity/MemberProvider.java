package io.github.kyungbeom.identity_server.domain.member.entity;

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

@Entity
@Table(
        name = "member_providers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_providers_type_user",
                columnNames = {"provider_type", "provider_user_id"}
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
        provider.providerType = ProviderType.LOCAL;
        provider.providerUserId = email;
        provider.password = hashedPassword;
        return provider;
    }
}
