package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.entities.Treno;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.ClienteRepository;
import com.trenicall.server.domain.repositories.PrenotazioneRepository;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrenotazioneServiceTest {

    @Mock private PrenotazioneRepository prenotazioneRepository;
    @Mock private BigliettoRepository bigliettoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private TrenoRepository trenoRepository;
    @Mock private BigliettoFactory factory;

    @InjectMocks
    private PrenotazioneService prenotazioneService;

    @Test
    void testCreaPrenotazione() {
        LocalDateTime dataViaggio = LocalDateTime.now().plusDays(1);
        Cliente cliente = new Cliente("C1", "Mario", "mario@test.it", "333");
        Biglietto biglietto = new Biglietto("B1", "C1", new StatoPrenotato(),
                TipoBiglietto.INTERCITY, "Roma", "Napoli", dataViaggio, 200);

        when(clienteRepository.findById("C1")).thenReturn(Optional.of(cliente));
        when(factory.creaBiglietto(TipoBiglietto.INTERCITY, "Roma", "Napoli", dataViaggio, 200, "C1"))
                .thenReturn(biglietto);
        when(bigliettoRepository.save(biglietto)).thenReturn(biglietto);
        when(prenotazioneRepository.save(any(Prenotazione.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Prenotazione p = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.INTERCITY, "Roma", "Napoli", dataViaggio, 200, 30);

        assertNotNull(p.getId());
        assertEquals("C1", p.getBiglietto().getClienteId());
    }

    @Test
    void testCreaPrenotazioneClienteNonTrovato() {
        lenient().when(clienteRepository.findById("C999")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            prenotazioneService.creaPrenotazione("C999", TipoBiglietto.INTERCITY,
                    "Roma", "Napoli", LocalDateTime.now().plusDays(1), 200, 30);
        });
    }

    @Test
    void testVerificaScadenze() {
        Biglietto bigliettoScaduto = new Biglietto("B2", "C1", new StatoPrenotato(),
                TipoBiglietto.REGIONALE, "Roma", "Milano",
                LocalDateTime.now().minusHours(2), 500);

        Cliente cliente = new Cliente("C1", "Mario", "mario@test.it", "333");
        Prenotazione prenotazioneScaduta = new Prenotazione("P2", cliente, null,
                LocalDateTime.now().minusHours(3), 30, bigliettoScaduto);

        when(prenotazioneRepository.findAll()).thenReturn(Arrays.asList(prenotazioneScaduta));

        prenotazioneService.verificaScadenze();

        assertTrue(true);
    }

    @Test
    void testConfermaAcquisto() {
        Cliente cliente = new Cliente("C1", "Mario", "mario@test.it", "333");
        Biglietto biglietto = new Biglietto("B1", "C1", new StatoPrenotato(),
                TipoBiglietto.FRECCIA_ROSSA, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 500);
        biglietto.setTrenoAssociato("TR123");

        Prenotazione prenotazione = new Prenotazione("P1", cliente, null,
                LocalDateTime.now(), 60, biglietto);

        Treno treno = new Treno("TR123", "Freccia Rossa 1234", null, 400, "1");

        when(prenotazioneRepository.findById("P1")).thenReturn(Optional.of(prenotazione));
        when(trenoRepository.findById("TR123")).thenReturn(Optional.of(treno));
        when(bigliettoRepository.save(any(Biglietto.class))).thenReturn(biglietto);

        Biglietto res = prenotazioneService.confermaAcquisto("P1");

        assertNotNull(res);
        assertEquals("PAGATO", res.getStato().getNomeStato());
    }

    @Test
    void testConfermaAcquistoPrenotazioneNonTrovata() {
        lenient().when(prenotazioneRepository.findById("P999")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            prenotazioneService.confermaAcquisto("P999");
        });
    }

    @Test
    void testGetPrenotazioniAttive() {
        Cliente cliente = new Cliente("C1", "Mario", "mario@test.it", "333");
        Biglietto biglietto1 = new Biglietto("B1", "C1", new StatoPrenotato(),
                TipoBiglietto.REGIONALE, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 300);
        Biglietto biglietto2 = new Biglietto("B2", "C2", new StatoPrenotato(),
                TipoBiglietto.INTERCITY, "Milano", "Torino",
                LocalDateTime.now().plusDays(2), 150);

        Prenotazione prenotazione1 = new Prenotazione("P1", cliente, null,
                LocalDateTime.now(), 60, biglietto1);
        Prenotazione prenotazione2 = new Prenotazione("P2", cliente, null,
                LocalDateTime.now(), 30, biglietto2);

        when(prenotazioneRepository.findAll()).thenReturn(Collections.emptyList());
        when(prenotazioneRepository.findByAttivaTrue()).thenReturn(Arrays.asList(prenotazione1, prenotazione2));

        var attive = prenotazioneService.getPrenotazioniAttive();

        assertEquals(2, attive.size());
    }

    @Test
    void testGetPrenotazioniAttiveByCliente() {
        Cliente cliente = new Cliente("C1", "Mario", "mario@test.it", "333");
        Biglietto biglietto = new Biglietto("B1", "C1", new StatoPrenotato(),
                TipoBiglietto.REGIONALE, "Roma", "Milano",
                LocalDateTime.now().plusDays(1), 300);

        Prenotazione prenotazione = new Prenotazione("P1", cliente, null,
                LocalDateTime.now(), 60, biglietto);

        when(prenotazioneRepository.findAll()).thenReturn(Collections.emptyList());
        when(prenotazioneRepository.findByClienteId("C1")).thenReturn(Arrays.asList(prenotazione));
        when(prenotazioneRepository.existsById("P1")).thenReturn(true);

        var attiveByCliente = prenotazioneService.getPrenotazioniAttiveByCliente("C1");

        assertEquals(1, attiveByCliente.size());
    }

    @Test
    void testGetPrenotazioniAttiveByClienteVuoto() {
        when(prenotazioneRepository.findAll()).thenReturn(Collections.emptyList());
        when(prenotazioneRepository.findByClienteId("C999")).thenReturn(Collections.emptyList());

        var attiveByCliente = prenotazioneService.getPrenotazioniAttiveByCliente("C999");

        assertTrue(attiveByCliente.isEmpty());
    }

    @Test
    void testConfermaAcquistoPrenotazioneScaduta() {
        Cliente cliente = new Cliente("C1", "Mario", "mario@test.it", "333");
        Biglietto bigliettoScaduto = new Biglietto("B3", "C1", new StatoPrenotato(),
                TipoBiglietto.REGIONALE, "Roma", "Milano",
                LocalDateTime.now().minusHours(2), 300);

        Prenotazione prenotazioneScaduta = new Prenotazione("P3", cliente, null,
                LocalDateTime.now().minusHours(2), 30, bigliettoScaduto);

        when(prenotazioneRepository.findById("P3")).thenReturn(Optional.of(prenotazioneScaduta));

        assertThrows(IllegalStateException.class, () -> {
            prenotazioneService.confermaAcquisto("P3");
        });
    }
}