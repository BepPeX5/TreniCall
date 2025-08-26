package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;

import java.util.function.Predicate;

public class ScontoPercentualeStrategy implements PricingStrategy {
    private final double percentuale;
    private final String descrizione;
    private final Predicate<Biglietto> criterio;

    public ScontoPercentualeStrategy(double percentuale, String descrizione, Predicate<Biglietto> criterio) {
        this.percentuale = percentuale;
        this.descrizione = descrizione;
        this.criterio = criterio;
    }

    @Override
    public boolean isApplicable(Biglietto biglietto) {
        return criterio.test(biglietto);
    }

    @Override
    public double calcolaPrezzo(Biglietto biglietto, double prezzoAttuale) {
        return prezzoAttuale * (1 - percentuale);
    }

    @Override
    public String getDescrizione() {
        return descrizione;
    }
}
