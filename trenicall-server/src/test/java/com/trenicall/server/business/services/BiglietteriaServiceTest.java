package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiglietteriaServiceTest {

    private final BiglietteriaService service = new BiglietteriaService();

    @Test
    void testRicercaBiglietti() {
        RicercaBiglietti ricerca = new RicercaBiglietti.Builder()
                .partenza("Roma")
                .arrivo("Milano")
                .dataViaggio(LocalDateTime.now().plusDays(1))
                .build();

        List<Biglietto> risultati = service.ricerca(ricerca);

        assertFalse(risultati.isEmpty());
        assertEquals("Roma", risultati.get(0).getPartenza());
        assertEquals("Milano", risultati.get(0).getArrivo());
    }

    @Test
    void testAcquistaBiglietto() {
        Biglietto b = service.acquista("C1", TipoBiglietto.REGIONALE,
                "Roma", "Napoli", LocalDateTime.now().plusDays(1), 200);

        assertNotNull(b.getId());
        assertEquals("C1", b.getClienteId());
        assertTrue(b.getPrezzo() > 0);
    }

    @Test
    void testModificaBiglietto() {
        Biglietto b = service.acquista("C1", TipoBiglietto.INTERCITY,
                "Roma", "Firenze", LocalDateTime.now().plusDays(1), 300);

        LocalDateTime nuovaData = LocalDateTime.now().plusDays(2);
        service.modifica(b, nuovaData);

        assertEquals(nuovaData, b.getDataViaggio());
        assertTrue(b.getPrezzo() > 0);
    }
}
