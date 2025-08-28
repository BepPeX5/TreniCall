package com.trenicall.server.domain.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clienti")
public class Cliente {

    @Id
    private String id;

    private String nome;
    private String email;
    private String telefono;
    private boolean fedelta;

    @OneToMany
    private final Set<Biglietto> biglietti = new HashSet<>();

    public Cliente() {}

    public Cliente(String id, String nome, String email, String telefono) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefono = telefono;
        this.fedelta = false;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isFedelta() {
        return fedelta;
    }

    public void abilitaFedelta() {
        this.fedelta = true;
    }

    public Set<Biglietto> getBiglietti() {
        return biglietti;
    }

    public void aggiungiBiglietto(Biglietto biglietto) {
        biglietti.add(biglietto);
    }
}
