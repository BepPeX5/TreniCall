package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.Treno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrenoRepository extends JpaRepository<Treno, String> {
    List<Treno> findByTrattaId(String trattaId);
}
