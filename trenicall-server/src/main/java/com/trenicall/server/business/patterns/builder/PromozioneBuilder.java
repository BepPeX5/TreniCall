package com.trenicall.server.business.patterns.builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PromozioneBuilder {
    private String codice;
    private String nome;
    private String descrizione;
    private Double percentualeSconto;
    private Double importoSconto;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private List<String> tipiBigliettoApplicabili;
    private List<String> tratteApplicabili;
    private Boolean soloFedeltaTreno;
    private Integer utilizziMassimi;
    private Double importoMinimoAcquisto;
    private Boolean attiva;

    public PromozioneBuilder() {
        this.tipiBigliettoApplicabili = new ArrayList<>();
        this.tratteApplicabili = new ArrayList<>();
        this.soloFedeltaTreno = false;
        this.attiva = true;
    }

    public static PromozioneBuilder nuovaPromozione() {
        return new PromozioneBuilder();
    }

    public PromozioneBuilder codice(String codice) {
        this.codice = codice;
        return this;
    }

    public PromozioneBuilder nome(String nome) {
        this.nome = nome;
        return this;
    }

    public PromozioneBuilder descrizione(String descrizione) {
        this.descrizione = descrizione;
        return this;
    }

    public PromozioneBuilder scontoPercentuale(Double percentuale) {
        this.percentualeSconto = percentuale;
        this.importoSconto = null;
        return this;
    }

    public PromozioneBuilder scontoImporto(Double importo) {
        this.importoSconto = importo;
        this.percentualeSconto = null;
        return this;
    }

    public PromozioneBuilder validaDal(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
        return this;
    }

    public PromozioneBuilder validaAl(LocalDate dataFine) {
        this.dataFine = dataFine;
        return this;
    }

    public PromozioneBuilder periodoValidita(LocalDate inizio, LocalDate fine) {
        this.dataInizio = inizio;
        this.dataFine = fine;
        return this;
    }

    public PromozioneBuilder applicabileA(String tipoBiglietto) {
        this.tipiBigliettoApplicabili.add(tipoBiglietto);
        return this;
    }

    public PromozioneBuilder applicabileATipi(List<String> tipi) {
        this.tipiBigliettoApplicabili.addAll(tipi);
        return this;
    }

    public PromozioneBuilder perTratta(String partenza, String arrivo) {
        this.tratteApplicabili.add(partenza + "-" + arrivo);
        return this;
    }

    public PromozioneBuilder perTratte(List<String> tratte) {
        this.tratteApplicabili.addAll(tratte);
        return this;
    }

    public PromozioneBuilder soloFedeltaTreno(Boolean solo) {
        this.soloFedeltaTreno = solo;
        return this;
    }

    public PromozioneBuilder utilizziMassimi(Integer utilizzi) {
        this.utilizziMassimi = utilizzi;
        return this;
    }

    public PromozioneBuilder importoMinimo(Double importo) {
        this.importoMinimoAcquisto = importo;
        return this;
    }

    public PromozioneBuilder attiva(Boolean attiva) {
        this.attiva = attiva;
        return this;
    }

    public Promozione build() {
        if (codice == null || nome == null) {
            throw new IllegalStateException("Codice e Nome sono obbligatori");
        }
        if (percentualeSconto == null && importoSconto == null) {
            throw new IllegalStateException("Deve essere specificato almeno un tipo di sconto");
        }
        if (dataInizio == null) {
            this.dataInizio = LocalDate.now();
        }

        return new Promozione(
                codice, nome, descrizione, percentualeSconto, importoSconto,
                dataInizio, dataFine, tipiBigliettoApplicabili, tratteApplicabili,
                soloFedeltaTreno, utilizziMassimi, importoMinimoAcquisto, attiva
        );
    }

    public static class Promozione {
        private final String codice;
        private final String nome;
        private final String descrizione;
        private final Double percentualeSconto;
        private final Double importoSconto;
        private final LocalDate dataInizio;
        private final LocalDate dataFine;
        private final List<String> tipiBigliettoApplicabili;
        private final List<String> tratteApplicabili;
        private final Boolean soloFedeltaTreno;
        private final Integer utilizziMassimi;
        private final Double importoMinimoAcquisto;
        private final Boolean attiva;

        public Promozione(String codice, String nome, String descrizione,
                          Double percentualeSconto, Double importoSconto,
                          LocalDate dataInizio, LocalDate dataFine,
                          List<String> tipiBigliettoApplicabili, List<String> tratteApplicabili,
                          Boolean soloFedeltaTreno, Integer utilizziMassimi,
                          Double importoMinimoAcquisto, Boolean attiva) {
            this.codice = codice;
            this.nome = nome;
            this.descrizione = descrizione;
            this.percentualeSconto = percentualeSconto;
            this.importoSconto = importoSconto;
            this.dataInizio = dataInizio;
            this.dataFine = dataFine;
            this.tipiBigliettoApplicabili = new ArrayList<>(tipiBigliettoApplicabili);
            this.tratteApplicabili = new ArrayList<>(tratteApplicabili);
            this.soloFedeltaTreno = soloFedeltaTreno;
            this.utilizziMassimi = utilizziMassimi;
            this.importoMinimoAcquisto = importoMinimoAcquisto;
            this.attiva = attiva;
        }

        // Getters
        public String getCodice() { return codice; }
        public String getNome() { return nome; }
        public String getDescrizione() { return descrizione; }
        public Double getPercentualeSconto() { return percentualeSconto; }
        public Double getImportoSconto() { return importoSconto; }
        public LocalDate getDataInizio() { return dataInizio; }
        public LocalDate getDataFine() { return dataFine; }
        public List<String> getTipiBigliettoApplicabili() { return new ArrayList<>(tipiBigliettoApplicabili); }
        public List<String> getTratteApplicabili() { return new ArrayList<>(tratteApplicabili); }
        public Boolean getSoloFedeltaTreno() { return soloFedeltaTreno; }
        public Integer getUtilizziMassimi() { return utilizziMassimi; }
        public Double getImportoMinimoAcquisto() { return importoMinimoAcquisto; }
        public Boolean getAttiva() { return attiva; }

        @Override
        public String toString() {
            return "Promozione{" +
                    "codice='" + codice + '\'' +
                    ", nome='" + nome + '\'' +
                    ", percentualeSconto=" + percentualeSconto +
                    ", importoSconto=" + importoSconto +
                    ", dataInizio=" + dataInizio +
                    ", dataFine=" + dataFine +
                    ", soloFedeltaTreno=" + soloFedeltaTreno +
                    '}';
        }
    }
}