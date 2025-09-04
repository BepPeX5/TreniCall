package com.trenicall.server.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "disponibilita_treno")
public class DisponibilitaTreno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Treno treno;

    private LocalDate dataViaggio;
    private int postiDisponibili;

    public DisponibilitaTreno() {}

    public DisponibilitaTreno(Treno treno, LocalDate dataViaggio, int postiTotali) {
        this.treno = treno;
        this.dataViaggio = dataViaggio;
        this.postiDisponibili = postiTotali;
    }

    public Long getId() { return id; }
    public Treno getTreno() { return treno; }
    public LocalDate getDataViaggio() { return dataViaggio; }
    public int getPostiDisponibili() { return postiDisponibili; }

    public boolean haPostiDisponibili(int richiesti) {
        return postiDisponibili >= richiesti;
    }

    public void prenotaPosti(int n) {
        if (postiDisponibili < n) {
            throw new IllegalStateException("Posti insufficienti per la data " + dataViaggio);
        }
        postiDisponibili -= n;
    }

    public void rilasciaPosti(int n) {
        postiDisponibili += n;
    }
}
