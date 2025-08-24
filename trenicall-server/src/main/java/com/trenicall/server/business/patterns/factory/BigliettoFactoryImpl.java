package com.trenicall.server.business.patterns.factory;

import com.trenicall.server.business.patterns.factory.types.BigliettoFrecciaRossaFactory;
import com.trenicall.server.business.patterns.factory.types.BigliettoIntercityFactory;
import com.trenicall.server.business.patterns.factory.types.BigliettoRegionaleFactory;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigliettoFactoryImpl implements BigliettoFactory {

    private final Map<TipoBiglietto, Object> factories;

    public BigliettoFactoryImpl() {
        factories = new HashMap<>();
        factories.put(TipoBiglietto.REGIONALE, new BigliettoRegionaleFactory());
        factories.put(TipoBiglietto.INTERCITY, new BigliettoIntercityFactory());
        factories.put(TipoBiglietto.FRECCIA_ROSSA, new BigliettoFrecciaRossaFactory());
    }

    @Override
    public Biglietto creaBiglietto(String tipo, String partenza, String arrivo, int distanzaKm) {
        TipoBiglietto tipoBiglietto = TipoBiglietto.fromString(tipo);
        return creaBiglietto(tipoBiglietto, partenza, arrivo, distanzaKm);
    }

    @Override
    public Biglietto creaBiglietto(TipoBiglietto tipo, String partenza, String arrivo, int distanzaKm) {
        System.out.println("Creazione biglietto tipo " + tipo.getDescrizione());

        switch (tipo) {
            case REGIONALE:
                BigliettoRegionaleFactory regionaleFactory = (BigliettoRegionaleFactory) factories.get(tipo);
                return regionaleFactory.creaBiglietto(partenza, arrivo, distanzaKm);

            case INTERCITY:
                BigliettoIntercityFactory intercityFactory = (BigliettoIntercityFactory) factories.get(tipo);
                return intercityFactory.creaBiglietto(partenza, arrivo, distanzaKm);

            case FRECCIA_ROSSA:
                BigliettoFrecciaRossaFactory frecciaFactory = (BigliettoFrecciaRossaFactory) factories.get(tipo);
                return frecciaFactory.creaBiglietto(partenza, arrivo, distanzaKm);

            default:
                throw new IllegalArgumentException("Tipo biglietto non supportato: " + tipo);
        }
    }

    @Override
    public Biglietto creaBigliettoConPrezzo(String tipo, String partenza, String arrivo, Double prezzoFisso) {
        TipoBiglietto tipoBiglietto = TipoBiglietto.fromString(tipo);

        switch (tipoBiglietto) {
            case REGIONALE:
                BigliettoRegionaleFactory regionaleFactory = (BigliettoRegionaleFactory) factories.get(tipoBiglietto);
                return regionaleFactory.creaBigliettoConPrezzo(partenza, arrivo, prezzoFisso);

            case INTERCITY:
                BigliettoIntercityFactory intercityFactory = (BigliettoIntercityFactory) factories.get(tipoBiglietto);
                return intercityFactory.creaBigliettoConPrezzo(partenza, arrivo, prezzoFisso);

            case FRECCIA_ROSSA:
                BigliettoFrecciaRossaFactory frecciaFactory = (BigliettoFrecciaRossaFactory) factories.get(tipoBiglietto);
                return frecciaFactory.creaBigliettoConPrezzo(partenza, arrivo, prezzoFisso);

            default:
                throw new IllegalArgumentException("Tipo biglietto non supportato: " + tipoBiglietto);
        }
    }

    @Override
    public List<String> getTipiSupportati() {
        return Arrays.asList(
                TipoBiglietto.REGIONALE.name(),
                TipoBiglietto.INTERCITY.name(),
                TipoBiglietto.FRECCIA_ROSSA.name()
        );
    }

    @Override
    public boolean supportaTipo(String tipo) {
        try {
            TipoBiglietto.fromString(tipo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public double calcolaPrezzo(String tipo, int distanzaKm) {
        TipoBiglietto tipoBiglietto = TipoBiglietto.fromString(tipo);

        switch (tipoBiglietto) {
            case REGIONALE:
                return Math.max(distanzaKm * tipoBiglietto.getPrezzoPerKm(), 2.50);

            case INTERCITY:
                return Math.max(distanzaKm * tipoBiglietto.getPrezzoPerKm() + 3.50, 5.00);

            case FRECCIA_ROSSA:
                return Math.max(distanzaKm * tipoBiglietto.getPrezzoPerKm() + 13.00, 15.00);

            default:
                throw new IllegalArgumentException("Tipo biglietto non supportato: " + tipo);
        }
    }
}