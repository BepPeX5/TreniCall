package com.trenicall.server.business.patterns.observer;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TrenoEvento {

    private final String numeroTreno;
    private final String tipoEvento;
    private final String messaggio;
    private final LocalDateTime timestampEvento;
    private final String partenza;
    private final String arrivo;
    private final LocalTime orarioPartenzaPrevisto;
    private final LocalTime orarioArrivoPrevisto;
    private final LocalTime orarioPartenzaEffettivo;
    private final LocalTime orarioArrivoEffettivo;
    private final String binarioPartenza;
    private final String binarioArrivo;
    private final String statoTreno;
    private final String motivoRitardo;

    public TrenoEvento(String numeroTreno, String tipoEvento, String messaggio,
                       LocalDateTime timestampEvento, String partenza, String arrivo,
                       LocalTime orarioPartenzaPrevisto, LocalTime orarioArrivoPrevisto,
                       LocalTime orarioPartenzaEffettivo, LocalTime orarioArrivoEffettivo,
                       String binarioPartenza, String binarioArrivo,
                       String statoTreno, String motivoRitardo) {
        this.numeroTreno = numeroTreno;
        this.tipoEvento = tipoEvento;
        this.messaggio = messaggio;
        this.timestampEvento = timestampEvento;
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.orarioPartenzaPrevisto = orarioPartenzaPrevisto;
        this.orarioArrivoPrevisto = orarioArrivoPrevisto;
        this.orarioPartenzaEffettivo = orarioPartenzaEffettivo;
        this.orarioArrivoEffettivo = orarioArrivoEffettivo;
        this.binarioPartenza = binarioPartenza;
        this.binarioArrivo = binarioArrivo;
        this.statoTreno = statoTreno;
        this.motivoRitardo = motivoRitardo;
    }

    public boolean isRitardo() {
        return "RITARDO".equals(tipoEvento);
    }

    public boolean isCancellazione() {
        return "CANCELLAZIONE".equals(tipoEvento);
    }

    public boolean isCambioBinario() {
        return "CAMBIO_BINARIO".equals(tipoEvento);
    }

    public boolean isPartenza() {
        return "PARTENZA".equals(tipoEvento);
    }

    public boolean isArrivo() {
        return "ARRIVO".equals(tipoEvento);
    }

    public int getRitardoMinuti() {
        if (orarioPartenzaEffettivo != null && orarioPartenzaPrevisto != null) {
            return (int) java.time.Duration.between(orarioPartenzaPrevisto, orarioPartenzaEffettivo).toMinutes();
        }
        return 0;
    }


    public String getNumeroTreno() { return numeroTreno; }
    public String getTipoEvento() { return tipoEvento; }
    public String getMessaggio() { return messaggio; }
    public LocalDateTime getTimestampEvento() { return timestampEvento; }
    public String getPartenza() { return partenza; }
    public String getArrivo() { return arrivo; }
    public LocalTime getOrarioPartenzaPrevisto() { return orarioPartenzaPrevisto; }
    public LocalTime getOrarioArrivoPrevisto() { return orarioArrivoPrevisto; }
    public LocalTime getOrarioPartenzaEffettivo() { return orarioPartenzaEffettivo; }
    public LocalTime getOrarioArrivoEffettivo() { return orarioArrivoEffettivo; }
    public String getBinarioPartenza() { return binarioPartenza; }
    public String getBinarioArrivo() { return binarioArrivo; }
    public String getStatoTreno() { return statoTreno; }
    public String getMotivoRitardo() { return motivoRitardo; }

    @Override
    public String toString() {
        return "TrenoEvento{" +
                "treno='" + numeroTreno + '\'' +
                ", evento='" + tipoEvento + '\'' +
                ", timestamp=" + timestampEvento +
                ", messaggio='" + messaggio + '\'' +
                '}';
    }
}