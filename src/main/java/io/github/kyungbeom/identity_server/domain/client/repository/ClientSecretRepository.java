package io.github.kyungbeom.identity_server.domain.client.repository;

import io.github.kyungbeom.identity_server.domain.client.entity.ClientSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientSecretRepository extends JpaRepository<ClientSecret, Integer> {

    // expired_at NULL(무제한) 또는 미래값이면 활성 시크릿. client_secret 인증 시 사용
    @Query("""
            SELECT cs FROM ClientSecret cs
            WHERE cs.client.clientId = :clientId
              AND (cs.expiredAt IS NULL OR cs.expiredAt > :now)
            """)
    List<ClientSecret> findActiveByClientId(@Param("clientId") Integer clientId,
                                            @Param("now") LocalDateTime now);
}
