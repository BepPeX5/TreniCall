package com.trenicall.server.business.services;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Promozione;
import com.trenicall.server.domain.repositories.PromozioneRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromozioneServiceTest {

    @Mock
    private PromozioneRepository promozioneRepository;

    @InjectMocks
    private PromozioneService promozioneService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAggiungiPromozione() {
        Promozione promozione = new Promozione(
                "P1", "Estate 2024", 0.15,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30),
                "Roma", "Milano", false
        );

        when(promozioneRepository.save(promozione)).thenReturn(promozione);

        Promozione salvata = promozioneService.aggiungiPromozione(promozione);

        assertEquals("Estate 2024", salvata.getNome());
        assertEquals(0.15, salvata.getPercentualeSconto());
        assertFalse(salvata.isSoloFedelta());
        verify(promozioneRepository).save(promozione);
    }

    @Test
    void testGetPromozioni() {
        Promozione p1 = new Promozione("P1", "Promo1", 0.10,
                LocalDateTime.now(), LocalDateTime.now().plusDays(10),
                "Roma", "Napoli", false);
        Promozione p2 = new Promozione("P2", "Promo Fedeltà", 0.20,
                LocalDateTime.now(), LocalDateTime.now().plusDays(20),
                "Milano", "Torino", true);

        when(promozioneRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Promozione> promozioni = promozioneService.getPromozioni();

        assertEquals(2, promozioni.size());
        assertEquals("Promo1", promozioni.get(0).getNome());
        assertEquals("Promo Fedeltà", promozioni.get(1).getNome());
        verify(promozioneRepository).findAll();
    }

    @Test
    void testApplicaPromozioni() {
        Biglietto biglietto = new Biglietto("B1", "C1", null, TipoBiglietto.INTERCITY,
                "Roma", "Milano", LocalDateTime.now().plusDays(1), 300);
        biglietto.setPrezzo(36.0);

        double prezzoConPromozioni = promozioneService.applicaPromozioni(biglietto, false);

        assertTrue(prezzoConPromozioni >= 0);
    }

    @Test
    void testApplicaPromozioniFedelta() {
        Biglietto biglietto = new Biglietto("B1", "C1", null, TipoBiglietto.FRECCIA_ROSSA,
                "Milano", "Napoli", LocalDateTime.now().plusDays(1), 400);
        biglietto.setPrezzo(72.0);

        double prezzoConPromozioni = promozioneService.applicaPromozioni(biglietto, true);

        assertTrue(prezzoConPromozioni <= 72.0);
    }
}
