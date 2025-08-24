package com.trenicall.server.business.patterns.strategy;

import com.trenicall.server.domain.entities.Biglietto;

public interface PricingStrategy {

    double calcolaPrezzo(Biglietto biglietto, Object cliente);

    double calcolaPrezzoBase(String tipoBiglietto, int distanzaKm);

    String getNomeStrategy();

    boolean isApplicabile(Object cliente);

    String getDescrizione();

    double getPercentualeSconto();
}