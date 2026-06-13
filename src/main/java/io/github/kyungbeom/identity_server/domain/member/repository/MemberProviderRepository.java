package io.github.kyungbeom.identity_server.domain.member.repository;

import io.github.kyungbeom.identity_server.domain.member.entity.MemberProvider;
import io.github.kyungbeom.identity_server.domain.member.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProviderRepository extends JpaRepository<MemberProvider, Long> {

    Optional<MemberProvider> findByProviderTypeAndProviderUserId(ProviderType providerType, String providerUserId);
}
