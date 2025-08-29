package com.trenicall.server.e2e;

import com.trenicall.server.config.GrpcServerTestConfiguration;
import com.trenicall.server.config.TestDataConfiguration;
import com.trenicall.server.grpc.cliente.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestDataConfiguration.class, GrpcServerTestConfiguration.class})
class ClienteGrpcEndToEndTest {

    @Autowired
    private Server grpcTestServer;

    private ManagedChannel channel;
    private ClienteServiceGrpc.ClienteServiceBlockingStub clienteStub;

    @BeforeEach
    void setUp() throws InterruptedException {
        Thread.sleep(2000);
        channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        clienteStub = ClienteServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void testRegistraClienteGrpcCompleto() {
        RegistraClienteRequest request = RegistraClienteRequest.newBuilder()
                .setId("C100")
                .setNome("Test Cliente")
                .setEmail("test@cliente.com")
                .setTelefono("123456789")
                .build();

        ClienteResponse response = clienteStub.registraCliente(request);

        assertEquals("C100", response.getId());
        assertEquals("Test Cliente", response.getNome());
        assertEquals("test@cliente.com", response.getEmail());
        assertEquals("123456789", response.getTelefono());
        assertFalse(response.getFedelta());
    }

    @Test
    void testAbilitaFedeltaGrpcCompleto() {
        RegistraClienteRequest registrazione = RegistraClienteRequest.newBuilder()
                .setId("C200")
                .setNome("Cliente Fedelta")
                .setEmail("fedelta@test.com")
                .setTelefono("987654321")
                .build();
        clienteStub.registraCliente(registrazione);

        AbilitaFedeltaRequest fedeltaRequest = AbilitaFedeltaRequest.newBuilder()
                .setClienteId("C200")
                .build();

        ClienteResponse response = clienteStub.abilitaFedelta(fedeltaRequest);

        assertEquals("C200", response.getId());
        assertTrue(response.getFedelta());
    }

    @Test
    void testDettagliClienteGrpcCompleto() {
        DettagliClienteRequest request = DettagliClienteRequest.newBuilder()
                .setClienteId("C1")
                .build();

        ClienteResponse response = clienteStub.dettagliCliente(request);

        assertEquals("C1", response.getId());
        assertEquals("Mario Rossi", response.getNome());
        assertEquals("mario@test.com", response.getEmail());
        assertEquals("123456789", response.getTelefono());
        assertFalse(response.getFedelta());
    }

    @Test
    void testDettagliClienteFedeltaGrpcCompleto() {
        DettagliClienteRequest request = DettagliClienteRequest.newBuilder()
                .setClienteId("C2")
                .build();

        ClienteResponse response = clienteStub.dettagliCliente(request);

        assertEquals("C2", response.getId());
        assertEquals("Luca Bianchi", response.getNome());
        assertEquals("luca@test.com", response.getEmail());
        assertTrue(response.getFedelta());
    }

    @Test
    void testFlussoCompletoRegistrazioneFedeltaGrpc() {
        String clienteId = "C300";

        RegistraClienteRequest registrazione = RegistraClienteRequest.newBuilder()
                .setId(clienteId)
                .setNome("Test Flusso")
                .setEmail("flusso@test.com")
                .setTelefono("555123456")
                .build();

        ClienteResponse nuovoCliente = clienteStub.registraCliente(registrazione);
        assertFalse(nuovoCliente.getFedelta());

        AbilitaFedeltaRequest abilitaFedelta = AbilitaFedeltaRequest.newBuilder()
                .setClienteId(clienteId)
                .build();

        ClienteResponse conFedelta = clienteStub.abilitaFedelta(abilitaFedelta);
        assertTrue(conFedelta.getFedelta());

        DettagliClienteRequest dettagli = DettagliClienteRequest.newBuilder()
                .setClienteId(clienteId)
                .build();

        ClienteResponse finale = clienteStub.dettagliCliente(dettagli);
        assertEquals("Test Flusso", finale.getNome());
        assertEquals("flusso@test.com", finale.getEmail());
        assertTrue(finale.getFedelta());
    }
}