package com.trenicall.server.business.patterns.factory.types;

import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import java.util.UUID;

public class BigliettoIntercityFactory {

    public Biglietto creaBiglietto(String partenza, String arrivo, int distanzaKm) {
        String id = "IC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double prezzo = calcolaPrezzo(distanzaKm);

        System.out.println("Creando biglietto InterCity: " + partenza + " -> " + arrivo);
        System.out.println("Distanza: " + distanzaKm + "km, Prezzo: €" + prezzo + " (include supplemento IC)");

        return new Biglietto(
                id,
                new StatoPrenotato(),
                TipoBiglietto.INTERCITY.name(),
                partenza,
                arrivo,
                distanzaKm,
                prezzo
        );
    }

    public Biglietto creaBigliettoConPrezzo(String partenza, String arrivo, Double prezzoFisso) {
        String id = "IC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int distanzaStimata = (int) ((prezzoFisso - 3.50) / TipoBiglietto.INTERCITY.getPrezzoPerKm());

        return new Biglietto(
                id,
                new StatoPrenotato(),
                TipoBiglietto.INTERCITY.name(),
                partenza,
                arrivo,
                distanzaStimata,
                prezzoFisso
        );
    }

    private double calcolaPrezzo(int distanzaKm) {
        double prezzoBase = distanzaKm * TipoBiglietto.INTERCITY.getPrezzoPerKm();
        double supplementoIC = 3.50;
        double prezzoMinimo = 5.00;

        return Math.max(prezzoBase + supplementoIC, prezzoMinimo);
    }
}