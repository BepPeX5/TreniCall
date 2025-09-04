package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.strategy.PricingContext;
import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.business.patterns.strategy.strategies.PromozioneFedeltaStrategy;
import com.trenicall.server.business.patterns.strategy.strategies.PromozionePeriodoStrategy;
import com.trenicall.server.business.patterns.strategy.strategies.PromozioneTrattaStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Promozione;
import com.trenicall.server.domain.repositories.PromozioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class PromozioneService {

    private final PromozioneRepository promozioneRepository;

    public PromozioneService(PromozioneRepository promozioneRepository) {
        this.promozioneRepository = promozioneRepository;
    }

    public Promozione aggiungiPromozione(Promozione promozione) {
        return promozioneRepository.save(promozione);
    }

    public List<Promozione> getPromozioni() {
        return promozioneRepository.findAll();
    }


    public List<Promozione> getPromozioniAttive(LocalDateTime dataViaggio, boolean clienteFedelta) {
        List<Promozione> tutte = promozioneRepository.findAll();
        return tutte.stream()
                .filter(p -> p.isAttiva(dataViaggio, clienteFedelta))
                .toList();
    }


    public double applicaPromozioni(Biglietto biglietto, boolean clienteFedelta) {
        PricingContext context = new PricingContext();

        List<Promozione> attive = getPromozioniAttive(biglietto.getDataViaggio(), clienteFedelta);
        for (Promozione p : attive) {
            PricingStrategy strategy;

            if (p.isSoloFedelta()) {
                strategy = new PromozioneFedeltaStrategy(
                        Set.of(biglietto.getClienteId()),
                        p.getPercentualeSconto(),
                        p.getNome()
                );
            } else if (p.getTrattaPartenza() != null && p.getTrattaArrivo() != null) {
                strategy = new PromozioneTrattaStrategy(
                        p.getTrattaPartenza(),
                        p.getTrattaArrivo(),
                        p.getPercentualeSconto(),
                        p.getNome()
                );
            } else {
                strategy = new PromozionePeriodoStrategy(
                        p.getInizio(),
                        p.getFine(),
                        p.getPercentualeSconto(),
                        p.getNome()
                );
            }

            context.aggiungiStrategia(strategy);
        }

        return context.calcolaPrezzoFinale(biglietto);
    }
}
