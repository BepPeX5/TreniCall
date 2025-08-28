package com.trenicall.server.business.services;

import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente registraCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente abilitaFedelta(String clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente non trovato"));
        cliente.abilitaFedelta();
        return clienteRepository.save(cliente);
    }

    public Cliente getCliente(String clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente non trovato"));
    }
}
