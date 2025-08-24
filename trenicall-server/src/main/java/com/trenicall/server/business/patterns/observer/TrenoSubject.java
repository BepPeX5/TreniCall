package com.trenicall.server.business.patterns.observer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TrenoSubject {

    private final String numeroTreno;
    private final String partenza;
    private final String arrivo;
    private LocalTime orarioPartenzaPrevisto;
    private LocalTime orarioArrivoPrevisto;
    private LocalTime orarioPartenzaEffettivo;
    private LocalTime orarioArrivoEffettivo;
    private String binarioPartenza;
    private String binarioArrivo;
    private String statoTreno;
    private String motivoRitardo;
    private final List<NotificaObserver> observers;

    public TrenoSubject(String numeroTreno, String partenza, String arrivo,
                        LocalTime orarioPartenza, LocalTime orarioArrivo) {
        this.numeroTreno = numeroTreno;
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.orarioPartenzaPrevisto = orarioPartenza;
        this.orarioArrivoPrevisto = orarioArrivo;
        this.statoTreno = "IN_ORARIO";
        this.observers = new ArrayList<>();
    }

    public void aggiungiObserver(NotificaObserver observer) {
        observers.add(observer);
        System.out.println("👀 Aggiunto observer: " + observer.getTipoNotifica());
    }

    public void rimuoviObserver(NotificaObserver observer) {
        observers.remove(observer);
        System.out.println("❌ Rimosso observer: " + observer.getTipoNotifica());
    }

    public void notificaObservers(String tipoEvento, String messaggio) {
        System.out.println("📢 Notificando " + observers.size() + " observer per evento: " + tipoEvento);

        TrenoEvento evento = new TrenoEvento(
                numeroTreno, tipoEvento, messaggio, LocalDateTime.now(),
                partenza, arrivo, orarioPartenzaPrevisto, orarioArrivoPrevisto,
                orarioPartenzaEffettivo, orarioArrivoEffettivo,
                binarioPartenza, binarioArrivo, statoTreno, motivoRitardo
        );

        for (NotificaObserver observer : observers) {
            try {
                observer.notifica(evento);
            } catch (Exception e) {
                System.err.println("❌ Errore notificando observer " + observer.getTipoNotifica() + ": " + e.getMessage());
            }
        }
    }

    public void aggiungiRitardo(int minutiRitardo) {
        this.orarioPartenzaEffettivo = orarioPartenzaPrevisto.plusMinutes(minutiRitardo);
        this.orarioArrivoEffettivo = orarioArrivoPrevisto.plusMinutes(minutiRitardo);
        this.statoTreno = "IN_RITARDO";

        String messaggio = "Il treno " + numeroTreno + " ha un ritardo di " + minutiRitardo + " minuti";
        notificaObservers("RITARDO", messaggio);
    }

    public void cambiaBinario(String nuovoBinario) {
        String vecchioBinario = this.binarioPartenza;
        this.binarioPartenza = nuovoBinario;

        String messaggio = "Il treno " + numeroTreno + " è stato spostato al binario " + nuovoBinario;
        if (vecchioBinario != null) {
            messaggio += " (era binario " + vecchioBinario + ")";
        }

        notificaObservers("CAMBIO_BINARIO", messaggio);
    }

    public void cancellaTreno(String motivo) {
        this.statoTreno = "CANCELLATO";
        this.motivoRitardo = motivo;

        String messaggio = "Il treno " + numeroTreno + " è stato cancellato. Motivo: " + motivo;
        notificaObservers("CANCELLAZIONE", messaggio);
    }

    public void trenoInPartenza() {
        this.statoTreno = "IN_VIAGGIO";

        String messaggio = "Il treno " + numeroTreno + " è partito da " + partenza;
        if (binarioPartenza != null) {
            messaggio += " dal binario " + binarioPartenza;
        }

        notificaObservers("PARTENZA", messaggio);
    }

    public void trenoArrivato() {
        this.statoTreno = "ARRIVATO";

        String messaggio = "Il treno " + numeroTreno + " è arrivato a " + arrivo;
        if (binarioArrivo != null) {
            messaggio += " al binario " + binarioArrivo;
        }

        notificaObservers("ARRIVO", messaggio);
    }

    public void aggiornamentoGenerico(String messaggio) {
        notificaObservers("AGGIORNAMENTO", messaggio);
    }

    // Getters
    public String getNumeroTreno() { return numeroTreno; }
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
    public int getNumeroObservers() { return observers.size(); }

    @Override
    public String toString() {
        return "TrenoSubject{" +
                "numeroTreno='" + numeroTreno + '\'' +
                ", tratta='" + partenza + "-" + arrivo + '\'' +
                ", stato='" + statoTreno + '\'' +
                ", observers=" + observers.size() +
                '}';
    }
}