package com.trenicall.server.domain.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treni")
public class Treno {

    @Id
    private String id;

    private String nome;

    @ManyToOne
    private Tratta tratta;

    private int postiTotali;
    private int postiDisponibili;
    private String binarioPartenza;
    private boolean cancellato;
    private int ritardoMinuti;

    @ManyToMany
    private final List<Cliente> passeggeri = new ArrayList<>();

    public Treno() {}

    public Treno(String id, String nome, Tratta tratta, int postiTotali, String binarioPartenza) {
        this.id = id;
        this.nome = nome;
        this.tratta = tratta;
        this.postiTotali = postiTotali;
        this.postiDisponibili = postiTotali;
        this.binarioPartenza = binarioPartenza;
        this.cancellato = false;
        this.ritardoMinuti = 0;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Tratta getTratta() {
        return tratta;
    }

    public int getPostiTotali() {
        return postiTotali;
    }

    public int getPostiDisponibili() {
        return postiDisponibili;
    }

    public boolean haPostiDisponibili(int richiesti) {
        return postiDisponibili >= richiesti;
    }

    public void prenotaPosti(int n) {
        if (postiDisponibili >= n) {
            postiDisponibili -= n;
        } else {
            throw new IllegalStateException("Posti non disponibili");
        }
    }

    public void rilasciaPosti(int n) {
        postiDisponibili = Math.min(postiTotali, postiDisponibili + n);
    }

    public String getBinarioPartenza() {
        return binarioPartenza;
    }

    public void setBinarioPartenza(String binarioPartenza) {
        this.binarioPartenza = binarioPartenza;
    }

    public boolean isCancellato() {
        return cancellato;
    }

    public void cancellaTreno() {
        this.cancellato = true;
    }

    public int getRitardoMinuti() {
        return ritardoMinuti;
    }

    public void setRitardoMinuti(int ritardoMinuti) {
        this.ritardoMinuti = ritardoMinuti;
    }

    public List<Cliente> getPasseggeri() {
        return passeggeri;
    }

    public void aggiungiPasseggero(Cliente cliente) {
        passeggeri.add(cliente);
    }
}
