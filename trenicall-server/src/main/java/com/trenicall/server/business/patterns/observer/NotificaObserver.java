package com.trenicall.server.business.patterns.observer;

public interface NotificaObserver {

    void notifica(TrenoEvento evento);

    String getTipoNotifica();

    boolean isAttivo();

    void attiva();

    void disattiva();
}
