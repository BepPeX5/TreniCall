package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoPagato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è già pagato.");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        biglietto.setStato(new StatoUtilizzato());
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        biglietto.setStato(new StatoRimborsato());
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
    }

    @Override
    public String getNomeStato() {
        return "PAGATO";
    }
}


