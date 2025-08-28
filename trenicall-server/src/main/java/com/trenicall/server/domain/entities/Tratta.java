package com.trenicall.server.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tratte")
public class Tratta {

    @Id
    private String id;

    private String stazionePartenza;
    private String stazioneArrivo;
    private int distanzaKm;

    public Tratta() {}

    public Tratta(String id, String stazionePartenza, String stazioneArrivo, int distanzaKm) {
        this.id = id;
        this.stazionePartenza = stazionePartenza;
        this.stazioneArrivo = stazioneArrivo;
        this.distanzaKm = distanzaKm;
    }

    public String getId() {
        return id;
    }

    public String getStazionePartenza() {
        return stazionePartenza;
    }

    public String getStazioneArrivo() {
        return stazioneArrivo;
    }

    public int getDistanzaKm() {
        return distanzaKm;
    }

    @Override
    public String toString() {
        return stazionePartenza + " → " + stazioneArrivo + " (" + distanzaKm + " km)";
    }
}
