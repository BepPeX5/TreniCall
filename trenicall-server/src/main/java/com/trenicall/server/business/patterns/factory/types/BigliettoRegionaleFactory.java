package com.trenicall.server.business.patterns.factory.types;

import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import java.util.UUID;

public class BigliettoRegionaleFactory {

    public Biglietto creaBiglietto(String partenza, String arrivo, int distanzaKm) {
        String id = "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double prezzo = calcolaPrezzo(distanzaKm);

        System.out.println("Creando biglietto Regionale: " + partenza + " -> " + arrivo);
        System.out.println("Distanza: " + distanzaKm + "km, Prezzo: €" + prezzo);

        return new Biglietto(
                id,
                new StatoPrenotato(),
                TipoBiglietto.REGIONALE.name(),
                partenza,
                arrivo,
                distanzaKm,
                prezzo
        );
    }

    public Biglietto creaBigliettoConPrezzo(String partenza, String arrivo, Double prezzoFisso) {
        String id = "REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int distanzaStimata = (int) (prezzoFisso / TipoBiglietto.REGIONALE.getPrezzoPerKm());

        return new Biglietto(
                id,
                new StatoPrenotato(),
                TipoBiglietto.REGIONALE.name(),
                partenza,
                arrivo,
                distanzaStimata,
                prezzoFisso
        );
    }

    private double calcolaPrezzo(int distanzaKm) {
        double prezzoBase = distanzaKm * TipoBiglietto.REGIONALE.getPrezzoPerKm();
        double prezzoMinimo = 2.50;
        return Math.max(prezzoBase, prezzoMinimo);
    }
}