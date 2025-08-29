package com.trenicall.server.business.services;

import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegistraCliente() {
        Cliente c = new Cliente("C1", "Mario", "mario@example.com", "12345");
        when(clienteRepository.save(c)).thenReturn(c);

        Cliente salvato = clienteService.registraCliente(c);

        assertEquals("Mario", salvato.getNome());
        verify(clienteRepository, times(1)).save(c);
    }

    @Test
    void testAbilitaFedelta() {
        Cliente c = new Cliente("C1", "Luca", "luca@example.com", "67890");
        when(clienteRepository.findById("C1")).thenReturn(Optional.of(c));
        when(clienteRepository.save(c)).thenReturn(c);

        Cliente fedelta = clienteService.abilitaFedelta("C1");

        assertTrue(fedelta.isFedelta());
        verify(clienteRepository).save(c);
    }

    @Test
    void testGetCliente() {
        Cliente c = new Cliente("C1", "Anna", "anna@example.com", "11111");
        when(clienteRepository.findById("C1")).thenReturn(Optional.of(c));

        Cliente trovato = clienteService.getCliente("C1");

        assertEquals("Anna", trovato.getNome());
    }
}
