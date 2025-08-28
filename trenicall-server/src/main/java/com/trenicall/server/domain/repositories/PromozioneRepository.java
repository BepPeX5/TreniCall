package com.trenicall.server.domain.repositories;

import com.trenicall.server.domain.entities.Promozione;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PromozioneRepository extends JpaRepository<Promozione, String> {
    List<Promozione> findBySoloFedeltaTrue();
}
