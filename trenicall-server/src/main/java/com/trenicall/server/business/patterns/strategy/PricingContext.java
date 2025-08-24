package com.trenicall.server.business.patterns.strategy;

import com.trenicall.server.business.patterns.strategy.strategies.*;
import com.trenicall.server.domain.entities.Biglietto;
import java.util.HashMap;
import java.util.Map;

public class PricingContext {

    private PricingStrategy strategy;
    private final Map<String, PricingStrategy> strategieDisponibili;

    public PricingContext() {
        this.strategieDisponibili = new HashMap<>();
        inizializzaStrategieDisponibili();
        this.strategy = strategieDisponibili.get("NORMALE");
    }

    public PricingContext(PricingStrategy strategy) {
        this();
        this.strategy = strategy;
    }

    private void inizializzaStrategieDisponibili() {
        strategieDisponibili.put("NORMALE", new PrezzoNormaleStrategy());
        strategieDisponibili.put("STUDENTE", new PrezzoStudenteStrategy());
        strategieDisponibili.put("ANZIANO", new PrezzoAnzianoStrategy());
        strategieDisponibili.put("FEDELTA_TRENO", new PrezzoFedeltaTrenoStrategy());

        strategieDisponibili.put("BLACKFRIDAY", PromozioneStrategy.blackFriday());
        strategieDisponibili.put("AUTUNNO2025", PromozioneStrategy.autunno2025());
    }

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = strategy;
        System.out.println("🔄 Cambiata strategia pricing a: " + strategy.getNomeStrategy());
    }

    public void setStrategy(String nomeStrategy) {
        PricingStrategy nuovaStrategy = strategieDisponibili.get(nomeStrategy.toUpperCase());
        if (nuovaStrategy != null) {
            setStrategy(nuovaStrategy);
        } else {
            throw new IllegalArgumentException("Strategia non trovata: " + nomeStrategy);
        }
    }

    public double calcolaPrezzo(Biglietto biglietto, Object cliente) {
        if (strategy == null) {
            throw new IllegalStateException("Nessuna strategia impostata");
        }

        System.out.println("💡 Utilizzando strategia: " + strategy.getNomeStrategy());
        return strategy.calcolaPrezzo(biglietto, cliente);
    }

    public double calcolaPrezzoConStrategy(Biglietto biglietto, Object cliente, String nomeStrategy) {
        PricingStrategy strategiaTemp = strategieDisponibili.get(nomeStrategy.toUpperCase());
        if (strategiaTemp == null) {
            throw new IllegalArgumentException("Strategia non trovata: " + nomeStrategy);
        }

        System.out.println("💡 Utilizzando strategia temporanea: " + strategiaTemp.getNomeStrategy());
        return strategiaTemp.calcolaPrezzo(biglietto, cliente);
    }

    public PricingStrategy getMigliorStrategia(Object cliente) {

        PricingStrategy migliore = strategieDisponibili.get("NORMALE");
        double scontoMassimo = 0.0;

        for (PricingStrategy strategia : strategieDisponibili.values()) {
            if (strategia.isApplicabile(cliente)) {
                double sconto = strategia.getPercentualeSconto();
                if (sconto > scontoMassimo) {
                    scontoMassimo = sconto;
                    migliore = strategia;
                }
            }
        }

        System.out.println("🎯 Migliore strategia trovata: " + migliore.getNomeStrategy() +
                " (sconto: " + migliore.getPercentualeSconto() + "%)");
        return migliore;
    }

    public void aggiungiPromozione(String codice, String nome, double percentualeSconto) {
        PromozioneStrategy nuovaPromozione = PromozioneStrategy.scontoFisso(codice, nome, percentualeSconto);
        strategieDisponibili.put(codice.toUpperCase(), nuovaPromozione);
        System.out.println("➕ Aggiunta nuova promozione: " + nome + " (" + codice + ")");
    }

    public Map<String, PricingStrategy> getStrategieDisponibili() {
        return new HashMap<>(strategieDisponibili);
    }

    public PricingStrategy getCurrentStrategy() {
        return strategy;
    }

    public String getDescrizioneStrategy() {
        return strategy != null ? strategy.getDescrizione() : "Nessuna strategia impostata";
    }
}
