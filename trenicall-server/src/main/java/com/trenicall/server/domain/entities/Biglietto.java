package com.trenicall.server.domain.entities;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

import java.time.LocalDateTime;

public class Biglietto {
    private String id;
    private String clienteId;
    private StatoBiglietto stato;
    private TipoBiglietto tipo;
    private String partenza;
    private String arrivo;
    private LocalDateTime dataViaggio;
    private Integer distanzaKm;
    private Double prezzo;

    public Biglietto(String id, String clienteId, StatoBiglietto stato, TipoBiglietto tipo,
                     String partenza, String arrivo, LocalDateTime dataViaggio, Integer distanzaKm) {
        this.id = id;
        this.clienteId = clienteId;
        this.stato = stato;
        this.tipo = tipo;
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.dataViaggio = dataViaggio;
        this.distanzaKm = distanzaKm;
        this.prezzo = distanzaKm * tipo.getPrezzoPerKm();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public StatoBiglietto getStato() {
        return stato;
    }

    public void setStato(StatoBiglietto stato) {
        this.stato = stato;
    }

    public TipoBiglietto getTipo() {
        return tipo;
    }

    public void setTipo(TipoBiglietto tipo) {
        this.tipo = tipo;
    }

    public String getPartenza() {
        return partenza;
    }

    public void setPartenza(String partenza) {
        this.partenza = partenza;
    }

    public String getArrivo() {
        return arrivo;
    }

    public void setArrivo(String arrivo) {
        this.arrivo = arrivo;
    }

    public LocalDateTime getDataViaggio() {
        return dataViaggio;
    }

    public void setDataViaggio(LocalDateTime dataViaggio) {
        this.dataViaggio = dataViaggio;
    }

    public Integer getDistanzaKm() {
        return distanzaKm;
    }

    public void setDistanzaKm(Integer distanzaKm) {
        this.distanzaKm = distanzaKm;
        this.prezzo = distanzaKm * tipo.getPrezzoPerKm();
    }

    public Double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(Double prezzo) {
        this.prezzo = prezzo;
    }

    public void confermaPrenotazione() {
        stato.confermaPrenotazione(this);
    }

    public void utilizzaBiglietto() {
        stato.utilizzaBiglietto(this);
    }

    public void rimborsaBiglietto() {
        stato.rimborsaBiglietto(this);
    }

    public void modificaBiglietto() {
        stato.modificaBiglietto(this);
    }

    @Override
    public String toString() {
        return "Biglietto{" +
                "id='" + id + '\'' +
                ", clienteId='" + clienteId + '\'' +
                ", stato=" + stato.getNomeStato() +
                ", tipo=" + tipo.getDescrizione() +
                ", partenza='" + partenza + '\'' +
                ", arrivo='" + arrivo + '\'' +
                ", dataViaggio=" + dataViaggio +
                ", distanzaKm=" + distanzaKm +
                ", prezzo=" + prezzo +
                '}';
    }
}

