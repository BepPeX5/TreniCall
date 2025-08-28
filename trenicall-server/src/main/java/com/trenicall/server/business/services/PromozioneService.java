package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.strategy.PricingContext;
import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Promozione;
import com.trenicall.server.domain.repositories.PromozioneRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PromozioneService {

    private final PromozioneRepository promozioneRepository;
    private final PricingContext pricingContext = new PricingContext();

    public PromozioneService(PromozioneRepository promozioneRepository) {
        this.promozioneRepository = promozioneRepository;
    }

    public Promozione aggiungiPromozione(Promozione promozione, PricingStrategy strategy) {
        pricingContext.aggiungiStrategia(strategy);
        return promozioneRepository.save(promozione);
    }

    public List<Promozione> getPromozioni() {
        return promozioneRepository.findAll();
    }

    public double applicaPromozioni(Biglietto biglietto) {
        return pricingContext.calcolaPrezzoFinale(biglietto);
    }
}

