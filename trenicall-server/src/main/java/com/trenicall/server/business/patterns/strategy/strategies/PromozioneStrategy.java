package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import java.time.LocalDate;

public class PromozioneStrategy implements PricingStrategy {

    private final String codicePromozione;
    private final String nomePromozione;
    private final double percentualeSconto;
    private final Double importoSconto;
    private final LocalDate dataInizio;
    private final LocalDate dataFine;

    public PromozioneStrategy(String codicePromozione, String nomePromozione,
                              double percentualeSconto, Double importoSconto,
                              LocalDate dataInizio, LocalDate dataFine) {
        this.codicePromozione = codicePromozione;
        this.nomePromozione = nomePromozione;
        this.percentualeSconto = percentualeSconto;
        this.importoSconto = importoSconto;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    public static PromozioneStrategy blackFriday() {
        return new PromozioneStrategy(
                "BLACKFRIDAY2025",
                "Black Friday",
                0.50,
                null,
                LocalDate.of(2024, 11, 25),
                LocalDate.of(2024, 11, 30)
        );
    }

    public static PromozioneStrategy autunno2025() {
        return new PromozioneStrategy(
                "AUTUNNO2025",
                "Promozione Autunno",
                0.30,
                null,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 8, 31)
        );
    }

    public static PromozioneStrategy scontoFisso(String codice, String nome, double importo) {
        return new PromozioneStrategy(codice, nome, 0.0, importo, LocalDate.now(), LocalDate.now().plusMonths(1));
    }

    @Override
    public double calcolaPrezzo(Biglietto biglietto, Object cliente) {
        if (!isPromozioneValida()) {
            System.out.println("❌ Promozione " + codicePromozione + " scaduta o non ancora attiva");
            return calcolaPrezzoBase(biglietto.getTipo(), biglietto.getDistanzaKm());
        }

        double prezzoBase = calcolaPrezzoBase(biglietto.getTipo(), biglietto.getDistanzaKm());
        double sconto = 0.0;
        double prezzoFinale = prezzoBase;

        if (percentualeSconto > 0) {
            sconto = prezzoBase * percentualeSconto;
            prezzoFinale = prezzoBase - sconto;
        } else if (importoSconto != null && importoSconto > 0) {
            sconto = Math.min(importoSconto, prezzoBase);
            prezzoFinale = prezzoBase - sconto;
        }

        System.out.println("🎉 Applicando promozione: " + nomePromozione + " (" + codicePromozione + ")");
        System.out.println("💰 Prezzo base: €" + prezzoBase);
        System.out.println("🎯 Sconto promozione: -€" + sconto);
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
        return "PROMOZIONE_" + codicePromozione;
    }

    @Override
    public boolean isApplicabile(Object cliente) {
        return isPromozioneValida();
    }

    @Override
    public String getDescrizione() {
        return nomePromozione + " - " + codicePromozione;
    }

    @Override
    public double getPercentualeSconto() {
        return percentualeSconto * 100;
    }

    private boolean isPromozioneValida() {
        LocalDate oggi = LocalDate.now();
        return !oggi.isBefore(dataInizio) && !oggi.isAfter(dataFine);
    }

    public String getCodicePromozione() {
        return codicePromozione;
    }

    public String getNomePromozione() {
        return nomePromozione;
    }
}