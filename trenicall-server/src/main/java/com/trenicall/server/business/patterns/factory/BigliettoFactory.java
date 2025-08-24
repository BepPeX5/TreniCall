package com.trenicall.server.business.patterns.factory;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import java.util.List;

public interface BigliettoFactory {

    Biglietto creaBiglietto(String tipo, String partenza, String arrivo, int distanzaKm);

    Biglietto creaBigliettoConPrezzo(String tipo, String partenza, String arrivo, Double prezzoFisso);

    Biglietto creaBiglietto(TipoBiglietto tipo, String partenza, String arrivo, int distanzaKm);

    List<String> getTipiSupportati();

    boolean supportaTipo(String tipo);

    double calcolaPrezzo(String tipo, int distanzaKm);
}
