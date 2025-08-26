package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import com.trenicall.server.business.patterns.observer.TrenoSubject;

import java.util.ArrayList;
import java.util.List;

public class NotificaService {
    private final TrenoSubject subject = new TrenoSubject();
    private final List<NotificaObserver> observers = new ArrayList<>();

    public void registraObserver(NotificaObserver observer) {
        observers.add(observer);
        subject.attach(observer);
    }

    public void rimuoviObserver(NotificaObserver observer) {
        observers.remove(observer);
        subject.detach(observer);
    }

    public void inviaNotifica(TrenoEvento evento) {
        subject.notifyObservers(evento);
    }

    public List<NotificaObserver> getObservers() {
        return observers;
    }
}
