package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoUtilizzato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è già utilizzato.");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Il biglietto è già utilizzato.");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Non è possibile rimborsare un biglietto utilizzato.");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Non è possibile modificare un biglietto utilizzato.");
    }

    @Override
    public String getNomeStato() {
        return "UTILIZZATO";
    }
}
