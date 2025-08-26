package com.trenicall.server.business.patterns.observer;

public class TrenoEvento {
    private final String trenoId;
    private final String tipoEvento;
    private final String messaggio;

    public TrenoEvento(String trenoId, String tipoEvento, String messaggio) {
        this.trenoId = trenoId;
        this.tipoEvento = tipoEvento;
        this.messaggio = messaggio;
    }

    public String getTrenoId() {
        return trenoId;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getMessaggio() {
        return messaggio;
    }

    @Override
    public String toString() {
        return "[" + tipoEvento + "] Treno " + trenoId + ": " + messaggio;
    }
}
