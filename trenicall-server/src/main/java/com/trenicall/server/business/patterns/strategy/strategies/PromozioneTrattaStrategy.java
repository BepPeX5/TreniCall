package com.trenicall.server.business.patterns.strategy.strategies;

import com.trenicall.server.business.patterns.strategy.PricingStrategy;
import com.trenicall.server.domain.entities.Biglietto;

public class PromozioneTrattaStrategy implements PricingStrategy {
    private final String partenza;
    private final String arrivo;
    private final double percentuale;
    private final String descrizione;

    public PromozioneTrattaStrategy(String partenza, String arrivo, double percentuale, String descrizione) {
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.percentuale = percentuale;
        this.descrizione = descrizione;
    }

    @Override
    public boolean isApplicable(Biglietto biglietto) {
        return biglietto.getPartenza().equalsIgnoreCase(partenza) &&
                biglietto.getArrivo().equalsIgnoreCase(arrivo);
    }

    @Override
    public double calcolaPrezzo(Biglietto biglietto, double prezzoAttuale) {
        return prezzoAttuale * (1 - percentuale);
    }

    @Override
    public String getDescrizione() {
        return descrizione;
    }
}
