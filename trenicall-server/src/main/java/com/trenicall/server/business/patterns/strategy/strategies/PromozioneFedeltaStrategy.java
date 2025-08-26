package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;

import java.util.Set;

public class PromozioneFedeltaStrategy implements PricingStrategy {
    private final Set<String> clientiFedelta;
    private final double percentuale;
    private final String descrizione;

    public PromozioneFedeltaStrategy(Set<String> clientiFedelta, double percentuale, String descrizione) {
        this.clientiFedelta = clientiFedelta;
        this.percentuale = percentuale;
        this.descrizione = descrizione;
    }

    @Override
    public boolean isApplicable(Biglietto biglietto) {
        return clientiFedelta.contains(biglietto.getClienteId());
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
