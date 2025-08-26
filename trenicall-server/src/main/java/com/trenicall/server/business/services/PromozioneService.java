package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.strategy.PricingContext;
import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Promozione;

import java.util.ArrayList;
import java.util.List;

public class PromozioneService {
    private final List<Promozione> promozioni = new ArrayList<>();
    private final PricingContext pricingContext = new PricingContext();

    public void aggiungiPromozione(Promozione promozione, PricingStrategy strategy) {
        promozioni.add(promozione);
        pricingContext.aggiungiStrategia(strategy);
    }

    public List<Promozione> getPromozioni() {
        return promozioni;
    }

    public double applicaPromozioni(Biglietto biglietto) {
        return pricingContext.calcolaPrezzoFinale(biglietto);
    }
}
