package com.trenicall.server.business.patterns.observer;

import com.trenicall.server.business.patterns.observer.TrenoEvento;

public interface NotificaObserver {
    void aggiorna(TrenoEvento evento);
    String getCanale();
}
