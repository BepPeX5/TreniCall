package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import com.trenicall.server.business.patterns.observer.TrenoSubject;
import com.trenicall.server.domain.entities.Notifica;
import com.trenicall.server.domain.repositories.NotificaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificaService {

    private final TrenoSubject subject = new TrenoSubject();
    private final NotificaRepository notificaRepository;

    public NotificaService(NotificaRepository notificaRepository) {
        this.notificaRepository = notificaRepository;
    }

    public void registraObserver(NotificaObserver observer) {
        subject.attach(observer);
    }

    public void rimuoviObserver(NotificaObserver observer) {
        subject.detach(observer);
    }

    public void inviaNotifica(TrenoEvento evento, String clienteId, String canale) {
        subject.notifyObservers(evento);
        Notifica notifica = new Notifica(
                evento.getTrenoId(), clienteId, canale, evento.getMessaggio()
        );
        notificaRepository.save(notifica);
    }

    public List<Notifica> getNotificheCliente(String clienteId) {
        return notificaRepository.findByClienteId(clienteId);
    }
}
