package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoRimborsato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è rimborsato.");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è rimborsato.");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è già rimborsato.");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è rimborsato.");
    }

    @Override
    public String getNomeStato() {
        return "RIMBORSATO";
    }
}

