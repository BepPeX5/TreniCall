package com.trenicall.server.business.patterns.factory.types;

import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import java.util.UUID;

public class BigliettoFrecciaRossaFactory {

    public Biglietto creaBiglietto(String partenza, String arrivo, int distanzaKm) {
        String id = "FR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double prezzo = calcolaPrezzo(distanzaKm);

        System.out.println("Creando biglietto Freccia Rossa: " + partenza + " -> " + arrivo);
        System.out.println("Distanza: " + distanzaKm + "km, Prezzo: €" + prezzo + " (alta velocità + servizi premium)");

        return new Biglietto(
                id,
                new StatoPrenotato(),
                TipoBiglietto.FRECCIA_ROSSA.name(),
                partenza,
                arrivo,
                distanzaKm,
                prezzo
        );
    }

    public Biglietto creaBigliettoConPrezzo(String partenza, String arrivo, Double prezzoFisso) {
        String id = "FR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int distanzaStimata = (int) ((prezzoFisso - 13.00) / TipoBiglietto.FRECCIA_ROSSA.getPrezzoPerKm());

        return new Biglietto(
                id,
                new StatoPrenotato(),
                TipoBiglietto.FRECCIA_ROSSA.name(),
                partenza,
                arrivo,
                distanzaStimata,
                prezzoFisso
        );
    }

    private double calcolaPrezzo(int distanzaKm) {
        double prezzoBase = distanzaKm * TipoBiglietto.FRECCIA_ROSSA.getPrezzoPerKm();
        double supplementoAltaVelocita = 8.00;
        double supplementoServizi = 5.00;
        double prezzoMinimo = 15.00;

        return Math.max(prezzoBase + supplementoAltaVelocita + supplementoServizi, prezzoMinimo);
    }
}