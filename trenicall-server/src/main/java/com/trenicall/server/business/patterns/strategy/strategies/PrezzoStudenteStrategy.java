package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

public class PrezzoStudenteStrategy implements PricingStrategy {

    private static final double SCONTO_STUDENTE = 0.20; // 20% di sconto

    @Override
    public double calcolaPrezzo(Biglietto biglietto, Object cliente) {
        double prezzoBase = calcolaPrezzoBase(biglietto.getTipo(), biglietto.getDistanzaKm());
        double sconto = prezzoBase * SCONTO_STUDENTE;
        double prezzoFinale = prezzoBase - sconto;

        System.out.println("🎓 Calcolo prezzo studente per " + biglietto.getTipo());
        System.out.println("💰 Prezzo base: €" + prezzoBase);
        System.out.println("🎯 Sconto studente (20%): -€" + sconto);
        System.out.println("✅ Prezzo finale: €" + prezzoFinale);

        return prezzoFinale;
    }

    @Override
    public double calcolaPrezzoBase(String tipoBiglietto, int distanzaKm) {
        TipoBiglietto tipo = TipoBiglietto.fromString(tipoBiglietto);

        switch (tipo) {
            case REGIONALE:
                return Math.max(distanzaKm * tipo.getPrezzoPerKm(), 2.50);

            case INTERCITY:
                return Math.max(distanzaKm * tipo.getPrezzoPerKm() + 3.50, 5.00);

            case FRECCIA_ROSSA:
                return Math.max(distanzaKm * tipo.getPrezzoPerKm() + 13.00, 15.00);

            default:
                throw new IllegalArgumentException("Tipo biglietto non supportato: " + tipoBiglietto);
        }
    }

    @Override
    public String getNomeStrategy() {
        return "PREZZO_STUDENTE";
    }

    @Override
    public boolean isApplicabile(Object cliente) {
        return true;
    }

    @Override
    public String getDescrizione() {
        return "Sconto del 20% per studenti con tessera valida";
    }

    @Override
    public double getPercentualeSconto() {
        return SCONTO_STUDENTE * 100;
    }
}