package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

public class PrezzoNormaleStrategy implements PricingStrategy {

    @Override
    public double calcolaPrezzo(Biglietto biglietto, Object cliente) {
        double prezzoBase = calcolaPrezzoBase(biglietto.getTipo(), biglietto.getDistanzaKm());
        System.out.println("🎫 Calcolo prezzo normale per " + biglietto.getTipo());
        System.out.println("💰 Prezzo finale: €" + prezzoBase);
        return prezzoBase;
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
        return "PREZZO_NORMALE";
    }

    @Override
    public boolean isApplicabile(Object cliente) {
        return true;
    }

    @Override
    public String getDescrizione() {
        return "Prezzo standard senza sconti";
    }

    @Override
    public double getPercentualeSconto() {
        return 0.0;
    }
}