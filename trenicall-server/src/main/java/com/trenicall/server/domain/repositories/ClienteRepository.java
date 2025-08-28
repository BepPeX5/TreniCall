package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Cliente findByEmail(String email);
}
