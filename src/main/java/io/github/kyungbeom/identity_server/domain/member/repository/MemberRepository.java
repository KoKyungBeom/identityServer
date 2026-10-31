package io.github.kyungbeom.identity_server.domain.member.repository;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import io.github.kyungbeom.identity_server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일만으로는 사람을 특정할 수 없다. 어느 프로젝트의 회원인지까지 함께 봐야 한다.
    Optional<Member> findByClientAndEmail(Client client, String email);

    boolean existsByClientAndEmail(Client client, String email);
}
