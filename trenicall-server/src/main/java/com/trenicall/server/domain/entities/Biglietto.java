package com.trenicall.server.domain.entities;

import com.trenicall.server.business.patterns.state.StatoBiglietto;

public class Biglietto {
    private String id;
    private StatoBiglietto stato;
    private String tipo;
    private String partenza, arrivo;
    private Integer distanzaKm;
    private Double prezzo;

    public Biglietto(String id, StatoBiglietto stato, String tipo, String partenza, String arrivo, Integer distanzaKm, Double prezzo) {
        this.id = id;
        this.stato = stato;
        this.tipo = tipo;
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.distanzaKm = distanzaKm;
        this.prezzo = prezzo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StatoBiglietto getStato() {
        return stato;
    }

    public void setStato(StatoBiglietto stato) {
        this.stato = stato;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
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

    public Integer getDistanzaKm() {
        return distanzaKm;
    }

    public void setDistanzaKm(Integer distanzaKm) {
        this.distanzaKm = distanzaKm;
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
                ", stato=" + stato.getNomeStato() +
                ", tipo='" + tipo + '\'' +
                ", partenza='" + partenza + '\'' +
                ", arrivo='" + arrivo + '\'' +
                ", distanzaKm=" + distanzaKm +
                ", prezzo=" + prezzo +
                '}';
    }
}
