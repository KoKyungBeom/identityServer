package io.github.kyungbeom.identity_server.domain.member.repository;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import io.github.kyungbeom.identity_server.domain.member.entity.MemberClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberClientRepository extends JpaRepository<MemberClient, Long> {

    boolean existsByMemberAndClient(Member member, Client client);

    Optional<MemberClient> findByMemberAndClient(Member member, Client client);
}
