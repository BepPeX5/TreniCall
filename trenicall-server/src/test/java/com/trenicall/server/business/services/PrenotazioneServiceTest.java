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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreaPrenotazione() {
        LocalDateTime dataViaggio = LocalDateTime.now().plusDays(1);

        when(clienteRepository.findById("C1"))
                .thenReturn(Optional.of(new Cliente("C1", "Mario", "mario@test.it", "333")));

        Biglietto stubBig = new Biglietto("B1", "C1",
                new StatoPrenotato(), TipoBiglietto.INTERCITY,
                "Roma", "Napoli", dataViaggio, 200);
        when(factory.creaBiglietto(
                eq(TipoBiglietto.INTERCITY), eq("Roma"), eq("Napoli"),
                eq(dataViaggio), eq(200), eq("C1"))).thenReturn(stubBig);

        when(bigliettoRepository.save(any(Biglietto.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(prenotazioneRepository.save(any(Prenotazione.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Prenotazione p = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.INTERCITY, "Roma", "Napoli",
                dataViaggio, 200, 30
        );

        assertNotNull(p.getId());
        assertTrue(p.isAttiva());
        assertEquals("C1", p.getBiglietto().getClienteId());
        verify(bigliettoRepository).save(stubBig);
        verify(prenotazioneRepository).save(any(Prenotazione.class));
    }

    @Test
    void testVerificaScadenze() {
        LocalDateTime passata = LocalDateTime.now().minusHours(1);
        Biglietto bigliettoScaduto = new Biglietto("B1", "C1",
                null, TipoBiglietto.REGIONALE,
                "Roma", "Milano", passata, 500);

        Prenotazione prenotazioneScaduta = new Prenotazione(
                "P1", null, null, passata.minusMinutes(60), -30, bigliettoScaduto
        );

        when(prenotazioneRepository.findAll())
                .thenReturn(Arrays.asList(prenotazioneScaduta));

        prenotazioneService.verificaScadenze();

        verify(prenotazioneRepository).delete(prenotazioneScaduta);
        verify(bigliettoRepository).delete(bigliettoScaduto);
        assertEquals("SCADUTO", bigliettoScaduto.getStato().getNomeStato());
    }

    @Test
    void testConfermaAcquisto() {
        LocalDateTime data = LocalDateTime.now().plusDays(1);
        Biglietto bPren = new Biglietto("B1", "C1",
                null, TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", data, 500);
        bPren.setTrenoAssociato("FR1234");

        Prenotazione pren = new Prenotazione("P1", null, null,
                LocalDateTime.now(), 60, bPren);

        when(prenotazioneRepository.findById("P1"))
                .thenReturn(Optional.of(pren));

        Treno treno = new Treno("FR1234", "Freccia Rossa 1234", null, 400, "1");
        when(trenoRepository.findById("FR1234"))
                .thenReturn(Optional.of(treno));

        when(bigliettoRepository.save(any(Biglietto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Biglietto res = prenotazioneService.confermaAcquisto("P1");

        assertNotNull(res);
        assertEquals("PAGATO", res.getStato().getNomeStato());
        verify(trenoRepository).save(treno);
        verify(prenotazioneRepository).deleteById("P1");
    }

    @Test
    void testGetPrenotazioniAttive() {
        LocalDateTime d1 = LocalDateTime.now().plusDays(1);
        LocalDateTime d2 = LocalDateTime.now().plusDays(2);

        Biglietto b1 = new Biglietto("B1", "C1", null, TipoBiglietto.REGIONALE,
                "Roma", "Milano", d1, 300);
        Biglietto b2 = new Biglietto("B2", "C2", null, TipoBiglietto.INTERCITY,
                "Milano", "Torino", d2, 150);

        Prenotazione p1 = new Prenotazione("P1", null, null, LocalDateTime.now(), 60, b1);
        Prenotazione p2 = new Prenotazione("P2", null, null, LocalDateTime.now(), 30, b2);

        when(prenotazioneRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        var attive = prenotazioneService.getPrenotazioniAttive();

        assertEquals(2, attive.size());
        verify(prenotazioneRepository).findAll();
    }

    @Test
    void testConfermaAcquistoPrenotazioneNonTrovata() {
        when(prenotazioneRepository.findById("P999"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            prenotazioneService.confermaAcquisto("P999");
        });
    }
}
