package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.Biglietto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BigliettoRepository extends JpaRepository<Biglietto, String> {
    List<Biglietto> findByClienteId(String clienteId);
    List<Biglietto> findByPartenzaAndArrivo(String partenza, String arrivo);
    List<Biglietto> findByPartenzaAndArrivoAndDataViaggio(String partenza, String arrivo, LocalDateTime dataViaggio);
}
