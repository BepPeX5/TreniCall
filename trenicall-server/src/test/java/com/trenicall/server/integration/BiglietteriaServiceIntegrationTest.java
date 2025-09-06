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
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@TestPropertySource(properties = {"grpc.server.enabled=false"})
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
        assertEquals("Roma", biglietto.getPartenza());
        assertEquals("Milano", biglietto.getArrivo());
        assertEquals(500, biglietto.getDistanzaKm());
        assertTrue(biglietto.getPrezzo() > 0);

        Biglietto salvato = bigliettoRepository.findById(biglietto.getId()).orElse(null);
        assertNotNull(salvato);
        assertEquals(biglietto.getClienteId(), salvato.getClienteId());
        assertEquals(biglietto.getTipo(), salvato.getTipo());
    }

    @Test
    void testRicercaBigliettiConDatabase() {
        LocalDateTime dataFissa = LocalDateTime.of(2024, 12, 15, 10, 30);

        biglietteriaService.acquista("C1", TipoBiglietto.INTERCITY,
                "Milano", "Torino", dataFissa, 150);
        biglietteriaService.acquista("C2", TipoBiglietto.INTERCITY,
                "Milano", "Torino", dataFissa, 150);

        RicercaBiglietti ricerca = new RicercaBiglietti.Builder()
                .partenza("Milano")
                .arrivo("Torino")
                .dataViaggio(dataFissa)
                .build();

        List<Biglietto> risultati = biglietteriaService.ricerca(ricerca);

        assertEquals(2, risultati.size());
        assertTrue(risultati.stream().allMatch(b -> b.getTipo() == TipoBiglietto.INTERCITY));
        assertTrue(risultati.stream().allMatch(b -> "Milano".equals(b.getPartenza())));
        assertTrue(risultati.stream().allMatch(b -> "Torino".equals(b.getArrivo())));
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
        assertEquals(originale.getId(), modificato.getId());
        assertEquals(originale.getClienteId(), modificato.getClienteId());

        Biglietto dalDatabase = bigliettoRepository.findById(originale.getId()).orElse(null);
        assertNotNull(dalDatabase);
        assertEquals(nuovaData, dalDatabase.getDataViaggio());
    }

    @Test
    void testGetArchivioBiglietti() {
        long countIniziale = bigliettoRepository.count();

        biglietteriaService.acquista("C1", TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 500);
        biglietteriaService.acquista("C2", TipoBiglietto.INTERCITY,
                "Milano", "Torino", LocalDateTime.now().plusDays(2), 150);

        List<Biglietto> archivio = biglietteriaService.getArchivioBiglietti();
        assertEquals(countIniziale + 2, archivio.size());
    }

    @Test
    void testCalcoloPrezziDiversiTipiBiglietto() {
        Biglietto regionale = biglietteriaService.acquista(
                "C1", TipoBiglietto.REGIONALE, "Roma", "Napoli",
                LocalDateTime.now().plusDays(1), 200
        );

        Biglietto intercity = biglietteriaService.acquista(
                "C1", TipoBiglietto.INTERCITY, "Milano", "Torino",
                LocalDateTime.now().plusDays(1), 150
        );

        Biglietto frecciaRossa = biglietteriaService.acquista(
                "C1", TipoBiglietto.FRECCIA_ROSSA, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 500
        );

        assertTrue(regionale.getPrezzo() > 0);
        assertTrue(intercity.getPrezzo() > 0);
        assertTrue(frecciaRossa.getPrezzo() > 0);

        assertTrue(regionale.getPrezzo() <= intercity.getPrezzo());
        assertTrue(intercity.getPrezzo() <= frecciaRossa.getPrezzo());
    }

    @Test
    void testBigliettiPerCliente() {
        biglietteriaService.acquista("C1", TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 500);
        biglietteriaService.acquista("C1", TipoBiglietto.INTERCITY,
                "Milano", "Torino", LocalDateTime.now().plusDays(2), 150);

        List<Biglietto> bigliettiC1 = bigliettoRepository.findByClienteId("C1");

        assertTrue(bigliettiC1.size() >= 2);
        assertTrue(bigliettiC1.stream().allMatch(b -> "C1".equals(b.getClienteId())));
    }
}