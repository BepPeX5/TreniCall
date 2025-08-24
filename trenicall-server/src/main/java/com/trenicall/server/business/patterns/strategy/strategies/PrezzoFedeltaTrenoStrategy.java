package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

public class PrezzoFedeltaTrenoStrategy implements PricingStrategy {

    private static final double SCONTO_BRONZE = 0.05;  // 5% sconto
    private static final double SCONTO_SILVER = 0.10;  // 10% sconto
    private static final double SCONTO_GOLD = 0.15;    // 15% sconto
    private static final double SCONTO_PLATINUM = 0.25; // 25% sconto

    @Override
    public double calcolaPrezzo(Biglietto biglietto, Object cliente) {
        double prezzoBase = calcolaPrezzoBase(biglietto.getTipo(), biglietto.getDistanzaKm());

        String livelloFedelta = "SILVER";
        double percentualeSconto = getPercentualeScontoPerLivello(livelloFedelta);
        double sconto = prezzoBase * percentualeSconto;
        double prezzoFinale = prezzoBase - sconto;

        System.out.println("💎 Calcolo prezzo FedeltàTreno " + livelloFedelta + " per " + biglietto.getTipo());
        System.out.println("💰 Prezzo base: €" + prezzoBase);
        System.out.println("🎯 Sconto FedeltàTreno (" + (percentualeSconto * 100) + "%): -€" + sconto);
        System.out.println("✅ Prezzo finale: €" + prezzoFinale);

        return prezzoFinale;
    }

    private double getPercentualeScontoPerLivello(String livello) {
        switch (livello.toUpperCase()) {
            case "BRONZE": return SCONTO_BRONZE;
            case "SILVER": return SCONTO_SILVER;
            case "GOLD": return SCONTO_GOLD;
            case "PLATINUM": return SCONTO_PLATINUM;
            default: return SCONTO_BRONZE;
        }
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
        return "PREZZO_FEDELTA_TRENO";
    }

    @Override
    public boolean isApplicabile(Object cliente) {
        return true;
    }

    @Override
    public String getDescrizione() {
        return "Sconti progressivi in base al livello FedeltàTreno (5%-25%)";
    }

    @Override
    public double getPercentualeSconto() {
        return SCONTO_SILVER * 100;
    }
}