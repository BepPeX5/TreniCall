package com.trenicall.server.domain.entities;

public class Tratta {
    private final String id;
    private final String stazionePartenza;
    private final String stazioneArrivo;
    private final int distanzaKm;

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
