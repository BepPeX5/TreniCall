package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;

import java.time.LocalDateTime;

public class PromozionePeriodoStrategy implements PricingStrategy {
    private final LocalDateTime inizio;
    private final LocalDateTime fine;
    private final double percentuale;
    private final String descrizione;

    public PromozionePeriodoStrategy(LocalDateTime inizio, LocalDateTime fine, double percentuale, String descrizione) {
        this.inizio = inizio;
        this.fine = fine;
        this.percentuale = percentuale;
        this.descrizione = descrizione;
    }

    @Override
    public boolean isApplicable(Biglietto biglietto) {
        return biglietto.getDataViaggio().isAfter(inizio) && biglietto.getDataViaggio().isBefore(fine);
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
