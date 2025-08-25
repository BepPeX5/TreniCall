package com.trenicall.server.business.patterns.factory;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

import java.time.LocalDateTime;

public interface BigliettoFactory {
    Biglietto creaBiglietto(TipoBiglietto tipo, String partenza, String arrivo,
                            LocalDateTime dataViaggio, Integer distanzaKm, String clienteId);
}
