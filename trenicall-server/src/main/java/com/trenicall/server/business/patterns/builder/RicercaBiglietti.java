package com.trenicall.server.business.patterns.builder;

import java.time.LocalDateTime;

public class RicercaBiglietti {
    private final String partenza;
    private final String arrivo;
    private final LocalDateTime dataViaggio;
    private final String classeServizio;
    private final boolean soloAltaVelocita;
    private final boolean includiPromozioni;

    private RicercaBiglietti(Builder builder) {
        this.partenza = builder.partenza;
        this.arrivo = builder.arrivo;
        this.dataViaggio = builder.dataViaggio;
        this.classeServizio = builder.classeServizio;
        this.soloAltaVelocita = builder.soloAltaVelocita;
        this.includiPromozioni = builder.includiPromozioni;
    }

    public String getPartenza() {
        return partenza;
    }

    public String getArrivo() {
        return arrivo;
    }

    public LocalDateTime getDataViaggio() {
        return dataViaggio;
    }

    public String getClasseServizio() {
        return classeServizio;
    }

    public boolean isSoloAltaVelocita() {
        return soloAltaVelocita;
    }

    public boolean isIncludiPromozioni() {
        return includiPromozioni;
    }

    public static class Builder {
        private String partenza;
        private String arrivo;
        private LocalDateTime dataViaggio;
        private String classeServizio;
        private boolean soloAltaVelocita;
        private boolean includiPromozioni;

        public Builder partenza(String partenza) {
            this.partenza = partenza;
            return this;
        }

        public Builder arrivo(String arrivo) {
            this.arrivo = arrivo;
            return this;
        }

        public Builder dataViaggio(LocalDateTime dataViaggio) {
            this.dataViaggio = dataViaggio;
            return this;
        }

        public Builder classeServizio(String classeServizio) {
            this.classeServizio = classeServizio;
            return this;
        }

        public Builder soloAltaVelocita(boolean soloAltaVelocita) {
            this.soloAltaVelocita = soloAltaVelocita;
            return this;
        }

        public Builder includiPromozioni(boolean includiPromozioni) {
            this.includiPromozioni = includiPromozioni;
            return this;
        }

        public RicercaBiglietti build() {
            return new RicercaBiglietti(this);
        }
    }
}
