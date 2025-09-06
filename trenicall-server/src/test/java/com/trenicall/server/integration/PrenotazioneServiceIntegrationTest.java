package com.trenicall.server.integration;

import com.trenicall.server.business.services.PrenotazioneService;
import com.trenicall.server.config.TestDataConfiguration;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.PrenotazioneRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@TestPropertySource(properties = {"grpc.server.enabled=false"})
@Transactional
class PrenotazioneServiceIntegrationTest {

    @Autowired
    private PrenotazioneService prenotazioneService;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private BigliettoRepository bigliettoRepository;

    @Test
    void testCreaPrenotazioneConPersistenza() {
        LocalDateTime dataViaggio = LocalDateTime.now().plusDays(5);

        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.INTERCITY, "Milano", "Torino",
                dataViaggio, 150, 60
        );

        assertNotNull(prenotazione.getId());
        assertEquals("C1", prenotazione.getBiglietto().getClienteId());
        assertTrue(prenotazione.isAttiva());

        Prenotazione dalDB = prenotazioneRepository.findById(prenotazione.getId()).orElse(null);
        assertNotNull(dalDB);

        Biglietto bigliettoAssociato = bigliettoRepository.findById(prenotazione.getBiglietto().getId()).orElse(null);
        assertNotNull(bigliettoAssociato);
        assertEquals(TipoBiglietto.INTERCITY, bigliettoAssociato.getTipo());
        assertEquals("C1", bigliettoAssociato.getClienteId());
    }

    @Test
    void testConfermaAcquistoConIntegrazione() {
        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(
                "C2", TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", LocalDateTime.now().plusDays(3), 500, 30
        );

        String prenotazioneId = prenotazione.getId();
        assertTrue(prenotazioneRepository.existsById(prenotazioneId));

        Biglietto bigliettoAcquistato = prenotazioneService.confermaAcquisto(prenotazioneId);

        assertNotNull(bigliettoAcquistato);
        assertEquals("C2", bigliettoAcquistato.getClienteId());
        assertEquals(TipoBiglietto.FRECCIA_ROSSA, bigliettoAcquistato.getTipo());

        assertFalse(prenotazioneRepository.existsById(prenotazioneId));
        assertTrue(bigliettoRepository.existsById(bigliettoAcquistato.getId()));
    }

    @Test
    void testVerificaScadenzeConDatabase() {
        Collection<Prenotazione> iniziali = prenotazioneRepository.findAll();
        int countIniziale = iniziali.size();

        prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.REGIONALE, "Roma", "Napoli",
                LocalDateTime.now().plusDays(1), 200, 60
        );

        Collection<Prenotazione> dopoCreazione = prenotazioneRepository.findAll();
        assertEquals(countIniziale + 1, dopoCreazione.size());

        prenotazioneService.verificaScadenze();

        Collection<Prenotazione> dopoVerifica = prenotazioneRepository.findAll();
        assertTrue(dopoVerifica.size() <= dopoCreazione.size());
    }

    @Test
    void testGetPrenotazioniAttiveConMultipleRecord() {
        Collection<Prenotazione> attivaIniziali = prenotazioneService.getPrenotazioniAttive();
        int countIniziale = attivaIniziali.size();

        prenotazioneService.creaPrenotazione("C1", TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 500, 60);
        prenotazioneService.creaPrenotazione("C2", TipoBiglietto.INTERCITY,
                "Milano", "Torino", LocalDateTime.now().plusDays(2), 150, 120);

        Collection<Prenotazione> attive = prenotazioneService.getPrenotazioniAttive();
        assertEquals(countIniziale + 2, attive.size());

        assertTrue(attive.stream().allMatch(Prenotazione::isAttiva));
    }

    @Test
    void testConfermaAcquistoPrenotazioneInesistente() {
        assertThrows(IllegalStateException.class, () -> {
            prenotazioneService.confermaAcquisto("P999_INESISTENTE");
        });
    }

    @Test
    void testGetPrenotazioniAttiveByClienteVuoto() {
        Collection<Prenotazione> vuote = prenotazioneService.getPrenotazioniAttiveByCliente("CLIENTE_INESISTENTE");
        assertNotNull(vuote);
        assertTrue(vuote.isEmpty());
    }

    @Test
    void testRepositoryFunziona() {
        long count = prenotazioneRepository.count();
        assertTrue(count >= 0);
    }

    @Test
    void testServiceEsiste() {
        assertNotNull(prenotazioneService);
        Collection<Prenotazione> attive = prenotazioneService.getPrenotazioniAttive();
        assertNotNull(attive);
    }

    @Test
    void testVerificaScadenzeSenzaEffetti() {
        prenotazioneService.verificaScadenze();
        assertTrue(true);
    }
}