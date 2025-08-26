package com.trenicall.server.business.services;

import java.util.HashSet;
import java.util.Set;

public class ClienteService {
    private final Set<String> clientiRegistrati = new HashSet<>();
    private final Set<String> clientiFedelta = new HashSet<>();

    public void registraCliente(String clienteId) {
        clientiRegistrati.add(clienteId);
    }

    public boolean isRegistrato(String clienteId) {
        return clientiRegistrati.contains(clienteId);
    }

    public void abilitaFedelta(String clienteId) {
        if (clientiRegistrati.contains(clienteId)) {
            clientiFedelta.add(clienteId);
        } else {
            throw new IllegalStateException("Cliente non registrato");
        }
    }

    public boolean isFedelta(String clienteId) {
        return clientiFedelta.contains(clienteId);
    }

    public Set<String> getClientiRegistrati() {
        return clientiRegistrati;
    }

    public Set<String> getClientiFedelta() {
        return clientiFedelta;
    }
}
