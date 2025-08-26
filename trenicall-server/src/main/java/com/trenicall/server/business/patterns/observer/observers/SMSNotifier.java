package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;

public class SMSNotifier implements NotificaObserver {
    private final String numeroTelefono;

    public SMSNotifier(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    @Override
    public void aggiorna(TrenoEvento evento) {
        System.out.println("SMS a " + numeroTelefono + ": " + evento);
    }

    @Override
    public String getCanale() {
        return "SMS";
    }
}
