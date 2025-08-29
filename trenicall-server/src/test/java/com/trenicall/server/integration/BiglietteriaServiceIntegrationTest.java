package com.trenicall.server.integration;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.services.BiglietteriaService;
import com.trenicall.server.config.TestDataConfiguration;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@Transactional
class BiglietteriaServiceIntegrationTest {

    @Autowired
    private BiglietteriaService biglietteriaService;

    @Autowired
    private BigliettoRepository bigliettoRepository;

    @Test
    void testAcquistaBigliettoConPersistenza() {
        LocalDateTime dataViaggio = LocalDateTime.now().plusDays(7);

        Biglietto biglietto = biglietteriaService.acquista(
                "C1", TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", dataViaggio, 500
        );

        assertNotNull(biglietto.getId());
        assertEquals("C1", biglietto.getClienteId());
        assertEquals(TipoBiglietto.FRECCIA_ROSSA, biglietto.getTipo());
        assertEquals(90.0, biglietto.getPrezzo());

        Biglietto salvato = bigliettoRepository.findById(biglietto.getId()).orElse(null);
        assertNotNull(salvato);
        assertEquals(biglietto.getClienteId(), salvato.getClienteId());
    }

    @Test
    void testRicercaBigliettiConDatabase() {
        LocalDateTime dataFissa = LocalDateTime.of(2024, 12, 15, 10, 30);

        biglietteriaService.acquista("C1", TipoBiglietto.INTERCITY,
                "Milano", "Torino", dataFissa, 150);
        biglietteriaService.acquista("C2", TipoBiglietto.REGIONALE,
                "Milano", "Torino", dataFissa, 150);

        RicercaBiglietti ricerca = new RicercaBiglietti.Builder()
                .partenza("Milano")
                .arrivo("Torino")
                .dataViaggio(dataFissa)
                .build();

        List<Biglietto> risultati = biglietteriaService.ricerca(ricerca);

        assertEquals(2, risultati.size());
        assertTrue(risultati.stream().anyMatch(b -> b.getTipo() == TipoBiglietto.INTERCITY));
        assertTrue(risultati.stream().anyMatch(b -> b.getTipo() == TipoBiglietto.REGIONALE));
    }

    @Test
    void testModificaBigliettoConPersistenza() {
        Biglietto originale = biglietteriaService.acquista(
                "C2", TipoBiglietto.REGIONALE,
                "Roma", "Napoli", LocalDateTime.now().plusDays(5), 200
        );

        LocalDateTime nuovaData = LocalDateTime.now().plusDays(10);
        Biglietto modificato = biglietteriaService.modifica(originale, nuovaData);

        assertEquals(nuovaData, modificato.getDataViaggio());

        Biglietto dalDatabase = bigliettoRepository.findById(originale.getId()).orElse(null);
        assertNotNull(dalDatabase);
        assertEquals(nuovaData, dalDatabase.getDataViaggio());
    }

    @Test
    void testGetArchivioBiglietti() {
        int countIniziale = biglietteriaService.getArchivioBiglietti().size();

        biglietteriaService.acquista("C1", TipoBiglietto.INTERCITY,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 500);
        biglietteriaService.acquista("C2", TipoBiglietto.REGIONALE,
                "Milano", "Torino", LocalDateTime.now().plusDays(2), 150);

        List<Biglietto> archivio = biglietteriaService.getArchivioBiglietti();
        assertEquals(countIniziale + 2, archivio.size());
    }

    @Test
    void testCalcoloPrezziDiversiTipiBiglietto() {
        Biglietto regionale = biglietteriaService.acquista(
                "C1", TipoBiglietto.REGIONALE, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 500
        );

        Biglietto intercity = biglietteriaService.acquista(
                "C1", TipoBiglietto.INTERCITY, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 500
        );

        Biglietto frecciaRossa = biglietteriaService.acquista(
                "C1", TipoBiglietto.FRECCIA_ROSSA, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 500
        );

        assertEquals(40.0, regionale.getPrezzo());
        assertEquals(60.0, intercity.getPrezzo());
        assertEquals(90.0, frecciaRossa.getPrezzo());

        assertTrue(regionale.getPrezzo() < intercity.getPrezzo());
        assertTrue(intercity.getPrezzo() < frecciaRossa.getPrezzo());
    }
}