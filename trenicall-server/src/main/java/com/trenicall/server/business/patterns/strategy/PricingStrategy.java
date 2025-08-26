package com.trenicall.server.business.patterns.strategy;

import com.trenicall.server.domain.entities.Biglietto;

public interface PricingStrategy {
    boolean isApplicable(Biglietto biglietto);
    double calcolaPrezzo(Biglietto biglietto, double prezzoAttuale);
    String getDescrizione();
}
