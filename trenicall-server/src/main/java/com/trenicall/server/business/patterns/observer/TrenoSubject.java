package com.trenicall.server.business.patterns.observer;

import java.util.ArrayList;
import java.util.List;

public class TrenoSubject {
    private final List<NotificaObserver> observers = new ArrayList<>();

    public void attach(NotificaObserver observer) {
        observers.add(observer);
    }

    public void detach(NotificaObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(TrenoEvento evento) {
        for (NotificaObserver obs : observers) {
            obs.aggiorna(evento);
        }
    }
}
