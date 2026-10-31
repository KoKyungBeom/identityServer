package io.github.kyungbeom.identity_server.domain.member.entity;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 회원은 프로젝트(client)에 속한다.
 * 프로젝트끼리는 서로 남이라, 같은 이메일이라도 프로젝트가 다르면 다른 사람이다.
 * (그래서 유니크 제약이 email 하나가 아니라 client_id + email 묶음이다.)
 */
@Entity
@Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_members_client_email",
                columnNames = {"client_id", "email"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 회원이 속한 프로젝트
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Member create(Client client, String email, String nickname) {
        Member member = new Member();
        member.client = client;
        member.email = email;
        member.nickname = nickname;
        return member;
    }
}
