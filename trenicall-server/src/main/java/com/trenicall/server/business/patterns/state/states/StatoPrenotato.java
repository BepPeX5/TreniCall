package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoPrenotato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        biglietto.setStato(new StatoPagato());
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto prenotato non può essere utilizzato.");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        biglietto.setStato(new StatoScaduto());
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
    }

    @Override
    public String getNomeStato() {
        return "PRENOTATO";
    }
}

