package com.trenicall.server.business.patterns.strategy;

import com.trenicall.server.domain.entities.Biglietto;

import java.util.ArrayList;
import java.util.List;

public class PricingContext {
    private final List<PricingStrategy> strategie = new ArrayList<>();
    private final List<String> motivazioni = new ArrayList<>();

    public void aggiungiStrategia(PricingStrategy strategy) {
        strategie.add(strategy);
    }

    public double calcolaPrezzoFinale(Biglietto biglietto) {
        double prezzo = biglietto.getPrezzo();
        motivazioni.clear();

        for (PricingStrategy s : strategie) {
            if (s.isApplicable(biglietto)) {
                double nuovoPrezzo = s.calcolaPrezzo(biglietto, prezzo);
                motivazioni.add(s.getDescrizione() + " (-" + (int) ((1 - nuovoPrezzo / prezzo) * 100) + "%)");
                prezzo = nuovoPrezzo;
            }
        }
        return prezzo;
    }

    public List<String> getMotivazioni() {
        return motivazioni;
    }
}