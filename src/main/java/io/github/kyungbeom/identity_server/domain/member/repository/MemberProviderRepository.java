package io.github.kyungbeom.identity_server.domain.member.repository;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.member.entity.MemberProvider;
import io.github.kyungbeom.identity_server.domain.member.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProviderRepository extends JpaRepository<MemberProvider, Long> {

    // 로그인 수단도 프로젝트 안에서만 유효하다
    Optional<MemberProvider> findByClientAndProviderTypeAndProviderUserId(
            Client client, ProviderType providerType, String providerUserId);
}
