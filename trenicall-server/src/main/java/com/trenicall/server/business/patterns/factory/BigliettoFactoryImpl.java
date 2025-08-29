package com.trenicall.server.business.patterns.factory;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;

import java.time.LocalDateTime;

public class BigliettoFactoryImpl implements BigliettoFactory {

    @Override
    public Biglietto creaBiglietto(TipoBiglietto tipo,
                                   String partenza,
                                   String arrivo,
                                   LocalDateTime dataViaggio,
                                   Integer distanzaKm,
                                   String clienteId) {
        String id = tipo.getCodice() + "-" + System.currentTimeMillis();

        return new Biglietto(
                id,
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


