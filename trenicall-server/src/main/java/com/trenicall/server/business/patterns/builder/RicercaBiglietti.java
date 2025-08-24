package com.trenicall.server.business.patterns.builder;

import java.time.LocalDate;
import java.time.LocalTime;

public class RicercaBiglietti {
    private final String partenza;
    private final String arrivo;
    private final LocalDate data;
    private final LocalTime orarioPreferito;
    private final String tipoBiglietto;
    private final String classeServizio;
    private final Boolean includiAnimali;
    private final Boolean includiBicicletta;
    private final Double prezzoMin;
    private final Double prezzoMax;
    private final Boolean soloPostiDisponibili;
    private final Boolean notificheRitardi;
    private final Integer numeroPasseggeri;

    private RicercaBiglietti(Builder builder) {
        this.partenza = builder.partenza;
        this.arrivo = builder.arrivo;
        this.data = builder.data;
        this.orarioPreferito = builder.orarioPreferito;
        this.tipoBiglietto = builder.tipoBiglietto;
        this.classeServizio = builder.classeServizio;
        this.includiAnimali = builder.includiAnimali;
        this.includiBicicletta = builder.includiBicicletta;
        this.prezzoMin = builder.prezzoMin;
        this.prezzoMax = builder.prezzoMax;
        this.soloPostiDisponibili = builder.soloPostiDisponibili;
        this.notificheRitardi = builder.notificheRitardi;
        this.numeroPasseggeri = builder.numeroPasseggeri;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String partenza;
        private String arrivo;
        private LocalDate data;
        private LocalTime orarioPreferito;
        private String tipoBiglietto;
        private String classeServizio = "2°";
        private Boolean includiAnimali = false;
        private Boolean includiBicicletta = false;
        private Double prezzoMin;
        private Double prezzoMax;
        private Boolean soloPostiDisponibili = true;
        private Boolean notificheRitardi = false;
        private Integer numeroPasseggeri = 1;

        public Builder partenza(String partenza) {
            this.partenza = partenza;
            return this;
        }

        public Builder arrivo(String arrivo) {
            this.arrivo = arrivo;
            return this;
        }

        public Builder data(LocalDate data) {
            this.data = data;
            return this;
        }

        public Builder data(String data) {
            this.data = LocalDate.parse(data);
            return this;
        }

        public Builder orarioPreferito(LocalTime orario) {
            this.orarioPreferito = orario;
            return this;
        }

        public Builder orarioPreferito(String orario) {
            this.orarioPreferito = LocalTime.parse(orario);
            return this;
        }

        public Builder tipoBiglietto(String tipoBiglietto) {
            this.tipoBiglietto = tipoBiglietto;
            return this;
        }

        public Builder classeServizio(String classeServizio) {
            this.classeServizio = classeServizio;
            return this;
        }

        public Builder includiAnimali(Boolean includiAnimali) {
            this.includiAnimali = includiAnimali;
            return this;
        }

        public Builder includiBicicletta(Boolean includiBicicletta) {
            this.includiBicicletta = includiBicicletta;
            return this;
        }

        public Builder prezzoMin(Double prezzoMin) {
            this.prezzoMin = prezzoMin;
            return this;
        }

        public Builder prezzoMax(Double prezzoMax) {
            this.prezzoMax = prezzoMax;
            return this;
        }

        public Builder rangePrezzo(Double min, Double max) {
            this.prezzoMin = min;
            this.prezzoMax = max;
            return this;
        }

        public Builder soloPostiDisponibili(Boolean soloPostiDisponibili) {
            this.soloPostiDisponibili = soloPostiDisponibili;
            return this;
        }

        public Builder notificheRitardi(Boolean notificheRitardi) {
            this.notificheRitardi = notificheRitardi;
            return this;
        }

        public Builder numeroPasseggeri(Integer numeroPasseggeri) {
            this.numeroPasseggeri = numeroPasseggeri;
            return this;
        }

        public RicercaBiglietti build() {
            if (partenza == null || arrivo == null) {
                throw new IllegalStateException("Partenza e Arrivo sono obbligatori");
            }
            if (data == null) {
                this.data = LocalDate.now();
            }
            return new RicercaBiglietti(this);
        }
    }

    public String getPartenza() { return partenza; }
    public String getArrivo() { return arrivo; }
    public LocalDate getData() { return data; }
    public LocalTime getOrarioPreferito() { return orarioPreferito; }
    public String getTipoBiglietto() { return tipoBiglietto; }
    public String getClasseServizio() { return classeServizio; }
    public Boolean getIncludiAnimali() { return includiAnimali; }
    public Boolean getIncludiBicicletta() { return includiBicicletta; }
    public Double getPrezzoMin() { return prezzoMin; }
    public Double getPrezzoMax() { return prezzoMax; }
    public Boolean getSoloPostiDisponibili() { return soloPostiDisponibili; }
    public Boolean getNotificheRitardi() { return notificheRitardi; }
    public Integer getNumeroPasseggeri() { return numeroPasseggeri; }

    @Override
    public String toString() {
        return "RicercaBiglietti{" +
                "partenza='" + partenza + '\'' +
                ", arrivo='" + arrivo + '\'' +
                ", data=" + data +
                ", orarioPreferito=" + orarioPreferito +
                ", tipoBiglietto='" + tipoBiglietto + '\'' +
                ", classeServizio='" + classeServizio + '\'' +
                ", numeroPasseggeri=" + numeroPasseggeri +
                ", prezzoMin=" + prezzoMin +
                ", prezzoMax=" + prezzoMax +
                '}';
    }
}
