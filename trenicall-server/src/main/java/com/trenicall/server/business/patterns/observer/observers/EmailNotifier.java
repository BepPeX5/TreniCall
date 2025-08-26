package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;

public class EmailNotifier implements NotificaObserver {
    private final String email;

    public EmailNotifier(String email) {
        this.email = email;
    }

    @Override
    public void aggiorna(TrenoEvento evento) {
        System.out.println("Email a " + email + ": " + evento);
    }

    @Override
    public String getCanale() {
        return "EMAIL";
    }
}
