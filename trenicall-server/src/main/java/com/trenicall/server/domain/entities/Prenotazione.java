package com.trenicall.server.domain.entities;

import com.trenicall.server.business.patterns.state.states.StatoPrenotato;

import java.time.LocalDateTime;

public class Prenotazione {
    private final String id;
    private final Cliente cliente;
    private final Treno treno;
    private final LocalDateTime dataCreazione;
    private final LocalDateTime scadenza;
    private Biglietto biglietto;
    private boolean attiva;

    public Prenotazione(String id, Cliente cliente, Treno treno, LocalDateTime dataCreazione, int minutiValidita, Biglietto biglietto) {
        this.id = id;
        this.cliente = cliente;
        this.treno = treno;
        this.dataCreazione = dataCreazione;
        this.scadenza = dataCreazione.plusMinutes(minutiValidita);
        this.biglietto = biglietto;
        this.biglietto.setStato(new StatoPrenotato());
        this.attiva = true;
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Treno getTreno() {
        return treno;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public LocalDateTime getScadenza() {
        return scadenza;
    }

    public Biglietto getBiglietto() {
        return biglietto;
    }

    public boolean isAttiva() {
        return attiva && LocalDateTime.now().isBefore(scadenza);
    }

    public void scaduta() {
        this.attiva = false;
    }
}
