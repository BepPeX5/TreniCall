package com.trenicall.server.e2e;

import com.trenicall.server.config.GrpcServerTestConfiguration;
import com.trenicall.server.config.TestDataConfiguration;
import com.trenicall.server.grpc.biglietteria.*;
import com.trenicall.server.grpc.cliente.ClienteServiceGrpc;
import com.trenicall.server.grpc.cliente.RegistraClienteRequest;
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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestDataConfiguration.class, GrpcServerTestConfiguration.class})
class BiglietteriaGrpcEndToEndTest {

    @Autowired
    private Server grpcTestServer;

    private ManagedChannel channel;
    private BiglietteriaServiceGrpc.BiglietteriaServiceBlockingStub biglietteriaStub;

    @BeforeEach
    void setUp() throws InterruptedException {
        Thread.sleep(2000);
        channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        biglietteriaStub = BiglietteriaServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void testAcquistaBigliettoGrpcCompleto() {
        AcquistaBigliettoRequest request = AcquistaBigliettoRequest.newBuilder()
                .setClienteId("C1")
                .setTipoBiglietto("FRECCIA_ROSSA")
                .setPartenza("Roma")
                .setArrivo("Milano")
                .setDataViaggio(LocalDateTime.now().plusDays(1).toString())
                .setDistanzaKm(500)
                .build();

        BigliettoResponse response = biglietteriaStub.acquistaBiglietto(request);

        assertNotNull(response.getId());
        assertEquals("C1", response.getClienteId());
        assertEquals("FRECCIA_ROSSA", response.getTipo());
        assertEquals("Roma", response.getPartenza());
        assertEquals("Milano", response.getArrivo());
        assertEquals(500, response.getDistanzaKm());
        assertTrue(response.getPrezzo() > 0);
    }

    @Test
    void testRicercaBigliettiGrpcCompleto() {
        LocalDateTime dataViaggio = LocalDateTime.of(2024, 12, 20, 14, 30);

        AcquistaBigliettoRequest acquisto1 = AcquistaBigliettoRequest.newBuilder()
                .setClienteId("C1")
                .setTipoBiglietto("INTERCITY")
                .setPartenza("Milano")
                .setArrivo("Torino")
                .setDataViaggio(dataViaggio.toString())
                .setDistanzaKm(150)
                .build();
        biglietteriaStub.acquistaBiglietto(acquisto1);

        AcquistaBigliettoRequest acquisto2 = AcquistaBigliettoRequest.newBuilder()
                .setClienteId("C2")
                .setTipoBiglietto("INTERCITY")
                .setPartenza("Milano")
                .setArrivo("Torino")
                .setDataViaggio(dataViaggio.toString())
                .setDistanzaKm(150)
                .build();
        biglietteriaStub.acquistaBiglietto(acquisto2);

        RicercaBigliettiRequest ricerca = RicercaBigliettiRequest.newBuilder()
                .setPartenza("Milano")
                .setArrivo("Torino")
                .setDataViaggio(dataViaggio.toString())
                .setClasseServizio("STANDARD")
                .setSoloAltaVelocita(false)
                .setIncludiPromozioni(true)
                .build();

        RicercaBigliettiResponse response = biglietteriaStub.ricercaBiglietti(ricerca);

        assertEquals(2, response.getRisultatiCount());
        assertTrue(response.getRisultatiList().stream()
                .allMatch(b -> "INTERCITY".equals(b.getTipo())));
    }

    @Test
    void testModificaBigliettoGrpcCompleto() {
        LocalDateTime dataOriginale = LocalDateTime.of(2024, 12, 25, 15, 30);
        LocalDateTime nuovaData = LocalDateTime.of(2024, 12, 30, 10, 15);

        AcquistaBigliettoRequest acquistoRequest = AcquistaBigliettoRequest.newBuilder()
                .setClienteId("C1")
                .setTipoBiglietto("REGIONALE")
                .setPartenza("Roma")
                .setArrivo("Napoli")
                .setDataViaggio(dataOriginale.toString())
                .setDistanzaKm(200)
                .build();

        BigliettoResponse biglietto = biglietteriaStub.acquistaBiglietto(acquistoRequest);

        ModificaBigliettoRequest modificaRequest = ModificaBigliettoRequest.newBuilder()
                .setBigliettoId(biglietto.getId())
                .setNuovaData(nuovaData.toString())
                .build();

        BigliettoResponse modificato = biglietteriaStub.modificaBiglietto(modificaRequest);

        assertEquals(biglietto.getId(), modificato.getId());
        assertEquals(nuovaData.toString(), modificato.getDataViaggio());
        assertEquals(biglietto.getClienteId(), modificato.getClienteId());
        assertEquals(biglietto.getTipo(), modificato.getTipo());
    }

    @Test
    void testListaBigliettiClienteGrpcCompleto() {
        String clienteTest = "C1";

        ListaBigliettiClienteRequest checkIniziale = ListaBigliettiClienteRequest.newBuilder()
                .setClienteId(clienteTest)
                .build();
        ListaBigliettiClienteResponse iniziale = biglietteriaStub.listaBigliettiCliente(checkIniziale);
        int countIniziale = iniziale.getBigliettiCount();

        biglietteriaStub.acquistaBiglietto(AcquistaBigliettoRequest.newBuilder()
                .setClienteId(clienteTest)
                .setTipoBiglietto("FRECCIA_ROSSA")
                .setPartenza("Roma")
                .setArrivo("Milano")
                .setDataViaggio(LocalDateTime.now().plusDays(1).toString())
                .setDistanzaKm(500)
                .build());

        biglietteriaStub.acquistaBiglietto(AcquistaBigliettoRequest.newBuilder()
                .setClienteId(clienteTest)
                .setTipoBiglietto("INTERCITY")
                .setPartenza("Milano")
                .setArrivo("Torino")
                .setDataViaggio(LocalDateTime.now().plusDays(3).toString())
                .setDistanzaKm(150)
                .build());

        ListaBigliettiClienteResponse response = biglietteriaStub.listaBigliettiCliente(checkIniziale);

        assertEquals(countIniziale + 2, response.getBigliettiCount());
        assertTrue(response.getBigliettiList().stream()
                .allMatch(b -> clienteTest.equals(b.getClienteId())));
    }

    @Test
    void testCalcoloPrezziDiversiTipiBigliettoGrpc() {
        String dataViaggio = LocalDateTime.now().plusDays(1).toString();

        BigliettoResponse regionale = biglietteriaStub.acquistaBiglietto(
                AcquistaBigliettoRequest.newBuilder()
                        .setClienteId("C1")
                        .setTipoBiglietto("REGIONALE")
                        .setPartenza("Roma")
                        .setArrivo("Napoli")
                        .setDataViaggio(dataViaggio)
                        .setDistanzaKm(200)
                        .build()
        );

        BigliettoResponse intercity = biglietteriaStub.acquistaBiglietto(
                AcquistaBigliettoRequest.newBuilder()
                        .setClienteId("C1")
                        .setTipoBiglietto("INTERCITY")
                        .setPartenza("Milano")
                        .setArrivo("Torino")
                        .setDataViaggio(dataViaggio)
                        .setDistanzaKm(150)
                        .build()
        );

        BigliettoResponse frecciaRossa = biglietteriaStub.acquistaBiglietto(
                AcquistaBigliettoRequest.newBuilder()
                        .setClienteId("C1")
                        .setTipoBiglietto("FRECCIA_ROSSA")
                        .setPartenza("Roma")
                        .setArrivo("Milano")
                        .setDataViaggio(dataViaggio)
                        .setDistanzaKm(500)
                        .build()
        );

        assertTrue(regionale.getPrezzo() > 0);
        assertTrue(intercity.getPrezzo() > 0);
        assertTrue(frecciaRossa.getPrezzo() > 0);

        assertTrue(regionale.getPrezzo() <= intercity.getPrezzo());
        assertTrue(intercity.getPrezzo() <= frecciaRossa.getPrezzo());
    }
}