package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.ClienteRepository;
import com.trenicall.server.domain.repositories.DisponibilitaTrenoRepository;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiglietteriaServiceTest {

    @Mock
    private BigliettoRepository bigliettoRepository;

    @Mock
    private BigliettoFactory factory;

    @Mock
    private PromozioneService promozioneService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TrenoRepository trenoRepository;

    @Mock
    private DisponibilitaTrenoRepository disponibilitaTrenoRepository;

    private BiglietteriaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BiglietteriaService(factory, bigliettoRepository, clienteRepository, promozioneService, trenoRepository, disponibilitaTrenoRepository);
    }

    @Test
    void testRicercaBiglietti() {
        RicercaBiglietti ricerca = new RicercaBiglietti.Builder()
                .partenza("Roma")
                .arrivo("Milano")
                .dataViaggio(LocalDateTime.now().plusDays(1))
                .build();

        Biglietto mockBiglietto = new Biglietto("1", "C1", null, TipoBiglietto.FRECCIA_ROSSA,
                "Roma", "Milano", ricerca.getDataViaggio(), 500);

        when(bigliettoRepository.findByPartenzaAndArrivoAndDataViaggio(
                ricerca.getPartenza(), ricerca.getArrivo(), ricerca.getDataViaggio()))
                .thenReturn(Collections.singletonList(mockBiglietto));

        List<Biglietto> risultati = service.ricerca(ricerca);

        assertFalse(risultati.isEmpty());
        assertEquals("Roma", risultati.get(0).getPartenza());
        assertEquals("Milano", risultati.get(0).getArrivo());
    }

    @Test
    void testAcquistaBiglietto() {
        LocalDateTime data = LocalDateTime.now().plusDays(1);

        when(bigliettoRepository.save(any(Biglietto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(promozioneService.applicaPromozioni(any(Biglietto.class), anyBoolean()))
                .thenAnswer(invocation -> {
                    Biglietto b = invocation.getArgument(0);
                    return b.getPrezzo() * 0.9; // Simula 10% sconto
                });

        Biglietto b = service.acquista("C1", TipoBiglietto.REGIONALE,
                "Roma", "Napoli", data, 200);

        assertNotNull(b.getId());
        assertEquals("C1", b.getClienteId());
        assertTrue(b.getPrezzo() > 0);
    }

    @Test
    void testModificaBiglietto() {
        LocalDateTime data = LocalDateTime.now().plusDays(1);
        Biglietto b = new Biglietto("1", "C1", null, TipoBiglietto.INTERCITY,
                "Roma", "Firenze", data, 300);

        when(bigliettoRepository.save(any(Biglietto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(promozioneService.applicaPromozioni(any(Biglietto.class), anyBoolean()))
                .thenReturn(100.0);

        LocalDateTime nuovaData = LocalDateTime.now().plusDays(2);
        Biglietto modificato = service.modifica(b, nuovaData);

        assertEquals(nuovaData, modificato.getDataViaggio());
        assertEquals(100.0, modificato.getPrezzo());
    }
}
