package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoRimborsato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Biglietto già rimborsato");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile utilizzare biglietto rimborsato");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        System.out.println("Biglietto " + biglietto.getId() + " già rimborsato");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile modificare biglietto rimborsato");
    }

    @Override
    public void scadenzaBiglietto(Biglietto biglietto) {
        System.out.println("Biglietto rimborsato non può scadere");
    }

    @Override
    public String getNomeStato() {
        return "RIMBORSATO";
    }

    @Override
    public boolean isUtilizzabile() {
        return false;
    }

    @Override
    public boolean isModificabile() {
        return false;
    }

    @Override
    public boolean isRimborsabile() {
        return false;
    }

    @Override
    public String toString() {
        return "StatoRimborsato{utilizzabile=false, modificabile=false, rimborsabile=false}";
    }
}
