package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoScaduto implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile confermare biglietto scaduto");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile utilizzare biglietto scaduto");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile rimborsare un biglietto scaduto");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile modificare biglietto scaduto");
    }

    @Override
    public void scadenzaBiglietto(Biglietto biglietto) {
        System.out.println("Biglietto già scaduto");
    }

    @Override
    public String getNomeStato() {
        return "SCADUTO";
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
        return true;
    }

    @Override
    public String toString() {
        return "StatoScaduto{utilizzabile=false, modificabile=false, rimborsabile=true}";
    }
}