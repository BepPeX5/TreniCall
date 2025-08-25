package com.trenicall.server.business.patterns.factory;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;

import java.time.LocalDateTime;

public class BigliettoFactoryImpl implements BigliettoFactory {

    @Override
    public Biglietto creaBiglietto(TipoBiglietto tipo, String partenza, String arrivo,
                                   LocalDateTime dataViaggio, Integer distanzaKm, String clienteId) {
        String idPrefix;
        switch (tipo) {
            case REGIONALE:
                idPrefix = "REG-";
                break;
            case INTERCITY:
                idPrefix = "INT-";
                break;
            case FRECCIA_ROSSA:
                idPrefix = "FR-";
                break;
            default:
                throw new IllegalArgumentException("Tipo biglietto non supportato: " + tipo);
        }

        return new Biglietto(
                idPrefix + System.currentTimeMillis(),
                clienteId,
                new StatoPrenotato(),
                tipo,
                partenza,
                arrivo,
                dataViaggio,
                distanzaKm
        );
    }
}

