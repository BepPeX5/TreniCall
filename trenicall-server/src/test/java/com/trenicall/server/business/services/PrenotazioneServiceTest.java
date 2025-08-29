package com.trenicall.server.business.services;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.PrenotazioneRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrenotazioneServiceTest {

    @Mock
    private PrenotazioneRepository prenotazioneRepository;

    @Mock
    private BigliettoRepository bigliettoRepository;

    @InjectMocks
    private PrenotazioneService prenotazioneService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreaPrenotazione() {
        LocalDateTime dataViaggio = LocalDateTime.now().plusDays(1);

        when(bigliettoRepository.save(any(Biglietto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(prenotazioneRepository.save(any(Prenotazione.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Prenotazione p = prenotazioneService.creaPrenotazione(
                "C1", TipoBiglietto.INTERCITY, "Roma", "Napoli",
                dataViaggio, 200, 30
        );

        assertNotNull(p.getId());
        assertEquals("C1", p.getBiglietto().getClienteId());
        assertTrue(p.isAttiva());
        assertEquals("PRENOTATO", p.getBiglietto().getStato().getNomeStato());
        verify(bigliettoRepository).save(any(Biglietto.class));
        verify(prenotazioneRepository).save(any(Prenotazione.class));
    }

    @Test
    void testVerificaScadenze() {
        LocalDateTime passata = LocalDateTime.now().minusHours(1);
        Biglietto bigliettoScaduto = new Biglietto("B1", "C1", null, TipoBiglietto.REGIONALE,
                "Roma", "Milano", passata, 500);

        Prenotazione prenotazioneScaduta = new Prenotazione(
                "P1", null, null, passata.minusMinutes(60), -30, bigliettoScaduto
        );

        when(prenotazioneRepository.findAll())
                .thenReturn(Arrays.asList(prenotazioneScaduta));

        prenotazioneService.verificaScadenze();

        verify(prenotazioneRepository).delete(prenotazioneScaduta);
        assertEquals("SCADUTO", bigliettoScaduto.getStato().getNomeStato());
    }

    @Test
    void testConfermaAcquisto() {
        BiglietteriaService mockBiglietteriaService = mock(BiglietteriaService.class);

        Biglietto bigliettoPrenotato = new Biglietto("B1", "C1", null, TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 500);

        Prenotazione prenotazione = new Prenotazione(
                "P1", null, null, LocalDateTime.now(), 60, bigliettoPrenotato
        );

        Biglietto bigliettoAcquistato = new Biglietto("B2", "C1", null, TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 500);

        when(prenotazioneRepository.findById("P1"))
                .thenReturn(Optional.of(prenotazione));
        when(mockBiglietteriaService.acquista(anyString(), any(TipoBiglietto.class),
                anyString(), anyString(), any(LocalDateTime.class), anyInt()))
                .thenReturn(bigliettoAcquistato);

        Biglietto risultato = prenotazioneService.confermaAcquisto("P1", mockBiglietteriaService);

        assertNotNull(risultato);
        assertEquals("B2", risultato.getId());
        verify(prenotazioneRepository).delete(prenotazione);
        verify(mockBiglietteriaService).acquista(
                "C1", TipoBiglietto.FRECCIA_ROSSA, "Roma", "Milano",
                bigliettoPrenotato.getDataViaggio(), 500
        );
    }

    @Test
    void testGetPrenotazioniAttive() {
        Biglietto b1 = new Biglietto("B1", "C1", null, TipoBiglietto.REGIONALE,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 300);
        Biglietto b2 = new Biglietto("B2", "C2", null, TipoBiglietto.INTERCITY,
                "Milano", "Torino", LocalDateTime.now().plusDays(2), 150);

        Prenotazione p1 = new Prenotazione("P1", null, null, LocalDateTime.now(), 60, b1);
        Prenotazione p2 = new Prenotazione("P2", null, null, LocalDateTime.now(), 30, b2);

        when(prenotazioneRepository.findAll())
                .thenReturn(Arrays.asList(p1, p2));

        var attive = prenotazioneService.getPrenotazioniAttive();

        assertEquals(2, attive.size());
        verify(prenotazioneRepository).findAll();
    }

    @Test
    void testConfermaAcquistoPrenotazioneNonTrovata() {
        when(prenotazioneRepository.findById("P999"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            prenotazioneService.confermaAcquisto("P999", mock(BiglietteriaService.class));
        });
    }
}
