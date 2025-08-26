package com.trenicall.server.domain.entities;

import java.time.LocalDateTime;

public class Promozione {
    private final String id;
    private final String nome;
    private final double percentualeSconto;
    private final LocalDateTime inizio;
    private final LocalDateTime fine;
    private final String trattaPartenza;
    private final String trattaArrivo;
    private final boolean soloFedelta;

    public Promozione(String id, String nome, double percentualeSconto, LocalDateTime inizio, LocalDateTime fine,
                      String trattaPartenza, String trattaArrivo, boolean soloFedelta) {
        this.id = id;
        this.nome = nome;
        this.percentualeSconto = percentualeSconto;
        this.inizio = inizio;
        this.fine = fine;
        this.trattaPartenza = trattaPartenza;
        this.trattaArrivo = trattaArrivo;
        this.soloFedelta = soloFedelta;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPercentualeSconto() {
        return percentualeSconto;
    }

    public LocalDateTime getInizio() {
        return inizio;
    }

    public LocalDateTime getFine() {
        return fine;
    }

    public String getTrattaPartenza() {
        return trattaPartenza;
    }

    public String getTrattaArrivo() {
        return trattaArrivo;
    }

    public boolean isSoloFedelta() {
        return soloFedelta;
    }

    public boolean isAttiva(LocalDateTime dataViaggio, boolean clienteFedelta) {
        boolean periodoValido = (dataViaggio.isAfter(inizio) && dataViaggio.isBefore(fine));
        boolean fedeltaValido = !soloFedelta || clienteFedelta;
        return periodoValido && fedeltaValido;
    }
}
