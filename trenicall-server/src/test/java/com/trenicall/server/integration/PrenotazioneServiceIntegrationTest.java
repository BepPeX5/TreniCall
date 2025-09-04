package com.trenicall.server.integration;

import com.trenicall.server.business.services.BiglietteriaService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@Transactional
class PrenotazioneServiceIntegrationTest {

    @Autowired
    private PrenotazioneService prenotazioneService;

    @Autowired
    private BiglietteriaService biglietteriaService;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private BigliettoRepository bigliettoRepository;

    @Test
    void testCreaPrenotazioneConPersistenza() {
        LocalDateTime dataViaggio = LocalDateTime.now().plusDays(5);

        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.INTERCITY, "Roma", "Milano",
                dataViaggio, 500, 60
        );

        assertNotNull(prenotazione.getId());
        assertEquals("C1", prenotazione.getBiglietto().getClienteId());
        assertTrue(prenotazione.isAttiva());

        Prenotazione dalDB = prenotazioneRepository.findById(prenotazione.getId()).orElse(null);
        assertNotNull(dalDB);
        assertTrue(dalDB.isAttiva());

        Biglietto bigliettoAssociato = bigliettoRepository.findById(prenotazione.getBiglietto().getId()).orElse(null);
        assertNotNull(bigliettoAssociato);
        assertEquals(TipoBiglietto.INTERCITY, bigliettoAssociato.getTipo());
        assertEquals("C1", bigliettoAssociato.getClienteId());
    }

    @Test
    void testConfermaAcquistoConIntegrazione() {
        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(
                "C2", TipoBiglietto.FRECCIA_ROSSA, "Milano", "Torino",
                LocalDateTime.now().plusDays(3), 150, 30
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
        LocalDateTime dataPassata = LocalDateTime.now().minusHours(2);

        Prenotazione prenotazioneScaduta = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.REGIONALE, "Roma", "Napoli",
                dataPassata, 200, -120
        );

        String prenotazioneId = prenotazioneScaduta.getId();
        String bigliettoId = prenotazioneScaduta.getBiglietto().getId();

        assertTrue(prenotazioneRepository.existsById(prenotazioneId));

        prenotazioneService.verificaScadenze();

        assertFalse(prenotazioneRepository.existsById(prenotazioneId));

        Biglietto bigliettoDalDB = bigliettoRepository.findById(bigliettoId).orElse(null);
        assertNotNull(bigliettoDalDB);
        assertEquals("SCADUTO", bigliettoDalDB.getStato().getNomeStato());
    }

    @Test
    void testGetPrenotazioniAttiveConMultipleRecord() {
        int countIniziale = prenotazioneService.getPrenotazioniAttive().size();

        prenotazioneService.creaPrenotazione("C1", TipoBiglietto.REGIONALE,
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
            prenotazioneService.confermaAcquisto("P999");
        });
    }

    @Test
    void testCascadeDeletePrenotazione() {
        Prenotazione prenotazione = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.REGIONALE, "Roma", "Napoli",
                LocalDateTime.now().plusDays(1), 200, 45
        );

        String prenotazioneId = prenotazione.getId();
        String bigliettoId = prenotazione.getBiglietto().getId();

        assertTrue(prenotazioneRepository.existsById(prenotazioneId));
        assertTrue(bigliettoRepository.existsById(bigliettoId));

        prenotazioneRepository.deleteById(prenotazioneId);

        assertFalse(prenotazioneRepository.existsById(prenotazioneId));
        assertTrue(bigliettoRepository.existsById(bigliettoId));
    }
}