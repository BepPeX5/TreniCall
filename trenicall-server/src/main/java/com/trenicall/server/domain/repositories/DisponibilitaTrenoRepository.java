package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.DisponibilitaTreno;
import com.trenicall.server.domain.entities.Treno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DisponibilitaTrenoRepository extends JpaRepository<DisponibilitaTreno, Long> {
    Optional<DisponibilitaTreno> findByTrenoAndDataViaggio(Treno treno, LocalDate dataViaggio);
}
