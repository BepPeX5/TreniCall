package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificaRepository extends JpaRepository<Notifica, String> {
    List<Notifica> findByClienteId(String clienteId);
    List<Notifica> findByLettaFalse();
}
