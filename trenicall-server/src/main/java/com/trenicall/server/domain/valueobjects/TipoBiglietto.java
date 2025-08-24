package com.trenicall.server.domain.valueobjects;

public enum TipoBiglietto {
    REGIONALE("REG", "Regionale", 0.08, "Treno regionale con fermate in tutte le stazioni"),
    INTERCITY("IC", "InterCity", 0.12, "Treno intercity con fermate limitate"),
    FRECCIA_ROSSA("FR", "Freccia Rossa", 0.18, "Treno ad alta velocità con servizi premium");

    private final String codice;
    private final String descrizione;
    private final double prezzoPerKm;
    private final String caratteristiche;

    TipoBiglietto(String codice, String descrizione, double prezzoPerKm, String caratteristiche) {
        this.codice = codice;
        this.descrizione = descrizione;
        this.prezzoPerKm = prezzoPerKm;
        this.caratteristiche = caratteristiche;
    }

    public String getCodice() {
        return codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public double getPrezzoPerKm() {
        return prezzoPerKm;
    }

    public String getCaratteristiche() {
        return caratteristiche;
    }

    public static TipoBiglietto fromString(String tipo) {
        for (TipoBiglietto t : values()) {
            if (t.name().equalsIgnoreCase(tipo) || t.getCodice().equalsIgnoreCase(tipo)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo biglietto non supportato: " + tipo);
    }
}