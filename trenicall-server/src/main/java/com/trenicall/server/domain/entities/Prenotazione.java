package com.trenicall.server.domain.entities;

import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazioni")
public class Prenotazione {

    @Id
    private String id;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    private Treno treno;

    private LocalDateTime dataCreazione;
    private LocalDateTime scadenza;

    @OneToOne
    private Biglietto biglietto;

    private boolean attiva;

    public Prenotazione() {}

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
