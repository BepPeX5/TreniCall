package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import com.trenicall.server.domain.entities.Notifica;
import com.trenicall.server.domain.repositories.NotificaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificaServiceTest {

    @Mock
    private NotificaRepository notificaRepository;

    @Mock
    private NotificaObserver mockObserver;

    @InjectMocks
    private NotificaService notificaService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegistraObserver() {
        when(mockObserver.getCanale()).thenReturn("EMAIL");

        notificaService.registraObserver(mockObserver);

        TrenoEvento evento = new TrenoEvento("T123", "RITARDO", "Il treno ha 15 minuti di ritardo");
        when(notificaRepository.save(any(Notifica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificaService.inviaNotifica(evento, "C1", "EMAIL");

        verify(mockObserver).aggiorna(evento);
        verify(notificaRepository).save(any(Notifica.class));
    }

    @Test
    void testRimuoviObserver() {
        when(mockObserver.getCanale()).thenReturn("SMS");

        notificaService.registraObserver(mockObserver);

        notificaService.rimuoviObserver(mockObserver);

        TrenoEvento evento = new TrenoEvento("T456", "CANCELLAZIONE", "Il treno è stato cancellato");
        when(notificaRepository.save(any(Notifica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificaService.inviaNotifica(evento, "C2", "SMS");

        verify(mockObserver, never()).aggiorna(evento);
        verify(notificaRepository).save(any(Notifica.class));
    }

    @Test
    void testInviaNotifica() {
        TrenoEvento evento = new TrenoEvento("T789", "BINARIO_CAMBIATO",
                "Il treno parte dal binario 3 invece del binario 1");

        when(notificaRepository.save(any(Notifica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificaService.inviaNotifica(evento, "C3", "PUSH");

        verify(notificaRepository).save(argThat(notifica ->
                "T789".equals(notifica.getId()) &&
                        "C3".equals(notifica.getClienteId()) &&
                        "PUSH".equals(notifica.getCanale()) &&
                        "Il treno parte dal binario 3 invece del binario 1".equals(notifica.getMessaggio()) &&
                        !notifica.isLetta()
        ));
    }

    @Test
    void testInviaNotificaConObserverRegistrati() {
        NotificaObserver observer1 = mock(NotificaObserver.class);
        NotificaObserver observer2 = mock(NotificaObserver.class);

        when(observer1.getCanale()).thenReturn("EMAIL");
        when(observer2.getCanale()).thenReturn("SMS");

        notificaService.registraObserver(observer1);
        notificaService.registraObserver(observer2);

        TrenoEvento evento = new TrenoEvento("T100", "RITARDO", "Ritardo di 30 minuti");
        when(notificaRepository.save(any(Notifica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificaService.inviaNotifica(evento, "C4", "EMAIL");

        verify(observer1).aggiorna(evento);
        verify(observer2).aggiorna(evento);
        verify(notificaRepository).save(any(Notifica.class));
    }

    @Test
    void testGetNotificheCliente() {
        Notifica n1 = new Notifica("N1", "C5", "EMAIL", "Prima notifica");
        Notifica n2 = new Notifica("N2", "C5", "SMS", "Seconda notifica");
        Notifica n3 = new Notifica("N3", "C6", "PUSH", "Notifica altro cliente");

        when(notificaRepository.findByClienteId("C5"))
                .thenReturn(Arrays.asList(n1, n2));

        List<Notifica> notifiche = notificaService.getNotificheCliente("C5");

        assertEquals(2, notifiche.size());
        assertEquals("Prima notifica", notifiche.get(0).getMessaggio());
        assertEquals("Seconda notifica", notifiche.get(1).getMessaggio());
        verify(notificaRepository).findByClienteId("C5");
    }

    @Test
    void testGetNotificheClienteVuoto() {
        when(notificaRepository.findByClienteId("C999"))
                .thenReturn(Arrays.asList());

        List<Notifica> notifiche = notificaService.getNotificheCliente("C999");

        assertTrue(notifiche.isEmpty());
        verify(notificaRepository).findByClienteId("C999");
    }

    @Test
    void testTrenoEventoContenuti() {
        TrenoEvento evento = new TrenoEvento("T555", "ARRIVO", "Il treno è arrivato in stazione");

        assertEquals("T555", evento.getTrenoId());
        assertEquals("ARRIVO", evento.getTipoEvento());
        assertEquals("Il treno è arrivato in stazione", evento.getMessaggio());
        assertEquals("[ARRIVO] Treno T555: Il treno è arrivato in stazione", evento.toString());
    }

    @Test
    void testInviaNotificaMultipleVolte() {
        when(notificaRepository.save(any(Notifica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrenoEvento evento1 = new TrenoEvento("T200", "PARTENZA", "Il treno è in partenza");
        TrenoEvento evento2 = new TrenoEvento("T200", "IN_VIAGGIO", "Il treno è in viaggio");
        TrenoEvento evento3 = new TrenoEvento("T200", "ARRIVO", "Il treno è arrivato");

        notificaService.inviaNotifica(evento1, "C7", "EMAIL");
        notificaService.inviaNotifica(evento2, "C7", "EMAIL");
        notificaService.inviaNotifica(evento3, "C7", "EMAIL");

        verify(notificaRepository, times(3)).save(any(Notifica.class));
    }
}