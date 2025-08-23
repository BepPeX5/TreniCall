package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoPagato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        throw new IllegalStateException("Biglietto già pagato e confermato");
    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        System.out.println("Utilizzo biglietto " + biglietto.getId());
        biglietto.setStato(new StatoUtilizzato());
        System.out.println("Biglietto utilizzato con successo");
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        System.out.println("Rimborso biglietto " + biglietto.getId() + " con penale del 20%");
        biglietto.setStato(new StatoRimborsato());
        System.out.println("Rimborso completato");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        System.out.println("Modifica biglietto " + biglietto.getId() + " con penale di €5");
        System.out.println("Modifica applicata");
    }

    @Override
    public void scadenzaBiglietto(Biglietto biglietto) {
        System.out.println("Biglietto " + biglietto.getId() + " scaduto");
        biglietto.setStato(new StatoScaduto());
    }

    @Override
    public String getNomeStato() {
        return "PAGATO";
    }

    @Override
    public boolean isUtilizzabile() {
        return true;
    }

    @Override
    public boolean isModificabile() {
        return true;
    }

    @Override
    public boolean isRimborsabile() {
        return true;
    }

    @Override
    public String toString() {
        return "StatoPagato{" +
                "utilizzabile=" + isUtilizzabile() +
                ", modificabile=" + isModificabile() +
                ", rimborsabile=" + isRimborsabile() +
                '}';
    }
}

