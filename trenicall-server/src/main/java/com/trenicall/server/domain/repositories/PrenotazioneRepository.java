package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.Prenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrenotazioneRepository extends JpaRepository<Prenotazione, String> {
    List<Prenotazione> findByClienteId(String clienteId);
    List<Prenotazione> findByAttivaTrue();
}
