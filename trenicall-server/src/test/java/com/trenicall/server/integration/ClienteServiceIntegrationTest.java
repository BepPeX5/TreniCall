package com.trenicall.server.integration;

import com.trenicall.server.business.services.ClienteService;
import com.trenicall.server.config.TestDataConfiguration;
import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.repositories.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@Transactional
class ClienteServiceIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void testRegistraClienteConPersistenza() {
        Cliente nuovoCliente = new Cliente("C10", "Anna Verde", "anna@test.com", "555123456");

        Cliente salvato = clienteService.registraCliente(nuovoCliente);

        assertEquals("C10", salvato.getId());
        assertEquals("Anna Verde", salvato.getNome());
        assertFalse(salvato.isFedelta());

        Cliente dalDatabase = clienteRepository.findById("C10").orElse(null);
        assertNotNull(dalDatabase);
        assertEquals("anna@test.com", dalDatabase.getEmail());
    }

    @Test
    void testAbilitaFedeltaConUpdate() {
        Cliente cliente = clienteService.getCliente("C1");
        assertFalse(cliente.isFedelta());

        Cliente conFedelta = clienteService.abilitaFedelta("C1");

        assertTrue(conFedelta.isFedelta());

        Cliente verificaDalDB = clienteRepository.findById("C1").orElse(null);
        assertNotNull(verificaDalDB);
        assertTrue(verificaDalDB.isFedelta());
    }

    @Test
    void testGetClienteEsistente() {
        Cliente cliente = clienteService.getCliente("C2");

        assertEquals("C2", cliente.getId());
        assertEquals("Luca Bianchi", cliente.getNome());
        assertEquals("luca@test.com", cliente.getEmail());
        assertTrue(cliente.isFedelta());
    }

    @Test
    void testGetClienteInesistente() {
        assertThrows(IllegalStateException.class, () -> {
            clienteService.getCliente("C999");
        });
    }

    @Test
    void testAbilitaFedeltaClienteInesistente() {
        assertThrows(IllegalStateException.class, () -> {
            clienteService.abilitaFedelta("C999");
        });
    }

    @Test
    void testRegistrazioneMultipliClienti() {
        int countIniziale = (int) clienteRepository.count();

        clienteService.registraCliente(new Cliente("C20", "Test1", "test1@mail.com", "111"));
        clienteService.registraCliente(new Cliente("C21", "Test2", "test2@mail.com", "222"));
        clienteService.registraCliente(new Cliente("C22", "Test3", "test3@mail.com", "333"));

        assertEquals(countIniziale + 3, clienteRepository.count());

        assertNotNull(clienteRepository.findById("C20").orElse(null));
        assertNotNull(clienteRepository.findById("C21").orElse(null));
        assertNotNull(clienteRepository.findById("C22").orElse(null));
    }

    @Test
    void testFindByEmail() {
        Cliente trovato = clienteRepository.findByEmail("mario@test.com");

        assertNotNull(trovato);
        assertEquals("C1", trovato.getId());
        assertEquals("Mario Rossi", trovato.getNome());
    }
}