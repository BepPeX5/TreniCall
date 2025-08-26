package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;

public class PushNotifier implements NotificaObserver {
    private final String deviceId;

    public PushNotifier(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void aggiorna(TrenoEvento evento) {
        System.out.println("Notifica push a device " + deviceId + ": " + evento);
    }

    @Override
    public String getCanale() {
        return "PUSH";
    }
}
