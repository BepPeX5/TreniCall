package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoScaduto implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("La prenotazione è scaduta.");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("La prenotazione è scaduta.");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("La prenotazione è scaduta.");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("La prenotazione è scaduta.");
    }

    @Override
    public String getNomeStato() {
        return "SCADUTO";
    }
}
