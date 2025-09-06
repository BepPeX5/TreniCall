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
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@TestPropertySource(properties = {"grpc.server.enabled=false"})
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
        assertEquals("anna@test.com", salvato.getEmail());
        assertFalse(salvato.isFedelta());

        Cliente dalDatabase = clienteRepository.findById("C10").orElse(null);
        assertNotNull(dalDatabase);
        assertEquals("Anna Verde", dalDatabase.getNome());
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
    void testFindByEmail() {
        Cliente trovato = clienteRepository.findByEmail("mario@test.com");

        assertNotNull(trovato);
        assertEquals("C1", trovato.getId());
        assertEquals("Mario Rossi", trovato.getNome());
    }

    @Test
    void testRegistrazioneMultipliClienti() {
        long countIniziale = clienteRepository.count();

        clienteService.registraCliente(new Cliente("C20", "Test1", "test1@mail.com", "111"));
        clienteService.registraCliente(new Cliente("C21", "Test2", "test2@mail.com", "222"));

        assertEquals(countIniziale + 2, clienteRepository.count());
    }
}