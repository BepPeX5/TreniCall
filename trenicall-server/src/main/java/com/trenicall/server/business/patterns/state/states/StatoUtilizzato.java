package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoUtilizzato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Biglietto già utilizzato");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Biglietto già utilizzato");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        System.out.println("Rimborso eccezionale biglietto utilizzato " + biglietto.getId());
        System.out.println("Rimborso del 50% per motivi straordinari");
        biglietto.setStato(new StatoRimborsato());
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Impossibile modificare biglietto già utilizzato");
    }

    @Override
    public void scadenzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException("Biglietto utilizzato non può scadere");
    }

    @Override
    public String getNomeStato() {
        return "UTILIZZATO";
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
        return "StatoUtilizzato{utilizzabile=false, modificabile=false, rimborsabile=true}";
    }
}
