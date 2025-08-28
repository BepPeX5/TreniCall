package com.trenicall.server.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifiche")
public class Notifica {

    @Id
    private String id;

    private String clienteId;
    private String canale;
    private String messaggio;
    private LocalDateTime timestamp;
    private boolean letta;

    public Notifica() {}

    public Notifica(String id, String clienteId, String canale, String messaggio) {
        this.id = id;
        this.clienteId = clienteId;
        this.canale = canale;
        this.messaggio = messaggio;
        this.timestamp = LocalDateTime.now();
        this.letta = false;
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getCanale() {
        return canale;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isLetta() {
        return letta;
    }

    public void segnaComeLetta() {
        this.letta = true;
    }

    @Override
    public String toString() {
        return "[" + canale + "] " + messaggio + " (" + timestamp + ")";
    }
}
