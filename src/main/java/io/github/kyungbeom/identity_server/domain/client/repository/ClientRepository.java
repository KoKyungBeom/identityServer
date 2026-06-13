package io.github.kyungbeom.identity_server.domain.client.repository;

import io.github.kyungbeom.identity_server.domain.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByClientName(String clientName);
}
