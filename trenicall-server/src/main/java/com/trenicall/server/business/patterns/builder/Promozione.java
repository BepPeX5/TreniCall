package com.trenicall.server.business.patterns.builder;

import java.time.LocalDateTime;

public class Promozione {
    private final String nome;
    private final double percentualeSconto;
    private final LocalDateTime inizio;
    private final LocalDateTime fine;
    private final String trattaPartenza;
    private final String trattaArrivo;
    private final boolean soloFedelta;

    private Promozione(Builder builder) {
        this.nome = builder.nome;
        this.percentualeSconto = builder.percentualeSconto;
        this.inizio = builder.inizio;
        this.fine = builder.fine;
        this.trattaPartenza = builder.trattaPartenza;
        this.trattaArrivo = builder.trattaArrivo;
        this.soloFedelta = builder.soloFedelta;
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

    public static class Builder {
        private String nome;
        private double percentualeSconto;
        private LocalDateTime inizio;
        private LocalDateTime fine;
        private String trattaPartenza;
        private String trattaArrivo;
        private boolean soloFedelta;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder percentualeSconto(double percentualeSconto) {
            this.percentualeSconto = percentualeSconto;
            return this;
        }

        public Builder periodo(LocalDateTime inizio, LocalDateTime fine) {
            this.inizio = inizio;
            this.fine = fine;
            return this;
        }

        public Builder tratta(String partenza, String arrivo) {
            this.trattaPartenza = partenza;
            this.trattaArrivo = arrivo;
            return this;
        }

        public Builder soloFedelta(boolean soloFedelta) {
            this.soloFedelta = soloFedelta;
            return this;
        }

        public Promozione build() {
            return new Promozione(this);
        }
    }
}
