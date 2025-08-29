package com.trenicall.server.e2e;

import com.trenicall.server.config.GrpcServerTestConfiguration;
import com.trenicall.server.config.TestDataConfiguration;
import com.trenicall.server.grpc.biglietteria.*;
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
                .setTipoBiglietto("INTERCITY")
                .setPartenza("Roma")
                .setArrivo("Milano")
                .setDataViaggio(LocalDateTime.now().plusDays(1).toString())
                .setDistanzaKm(500)
                .build();

        BigliettoResponse response = biglietteriaStub.acquistaBiglietto(request);

        assertNotNull(response.getId());
        assertEquals("C1", response.getClienteId());
        assertEquals("INTERCITY", response.getTipo());
        assertEquals("Roma", response.getPartenza());
        assertEquals("Milano", response.getArrivo());
        assertEquals(500, response.getDistanzaKm());
        assertEquals(60.0, response.getPrezzo(), 0.01);
    }

    @Test
    void testRicercaBigliettiGrpcCompleto() {
        LocalDateTime dataViaggio = LocalDateTime.of(2024, 12, 20, 14, 30);

        AcquistaBigliettoRequest acquisto1 = AcquistaBigliettoRequest.newBuilder()
                .setClienteId("C1")
                .setTipoBiglietto("REGIONALE")
                .setPartenza("Milano")
                .setArrivo("Torino")
                .setDataViaggio(dataViaggio.toString())
                .setDistanzaKm(150)
                .build();
        biglietteriaStub.acquistaBiglietto(acquisto1);

        AcquistaBigliettoRequest acquisto2 = AcquistaBigliettoRequest.newBuilder()
                .setClienteId("C2")
                .setTipoBiglietto("FRECCIA_ROSSA")
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
                .anyMatch(b -> "REGIONALE".equals(b.getTipo())));
        assertTrue(response.getRisultatiList().stream()
                .anyMatch(b -> "FRECCIA_ROSSA".equals(b.getTipo())));
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
        String clienteTest = "C999";

        biglietteriaStub.acquistaBiglietto(AcquistaBigliettoRequest.newBuilder()
                .setClienteId(clienteTest)
                .setTipoBiglietto("INTERCITY")
                .setPartenza("Roma")
                .setArrivo("Milano")
                .setDataViaggio(LocalDateTime.now().plusDays(1).toString())
                .setDistanzaKm(500)
                .build());

        biglietteriaStub.acquistaBiglietto(AcquistaBigliettoRequest.newBuilder()
                .setClienteId(clienteTest)
                .setTipoBiglietto("REGIONALE")
                .setPartenza("Milano")
                .setArrivo("Torino")
                .setDataViaggio(LocalDateTime.now().plusDays(3).toString())
                .setDistanzaKm(150)
                .build());

        ListaBigliettiClienteRequest listaRequest = ListaBigliettiClienteRequest.newBuilder()
                .setClienteId(clienteTest)
                .build();

        ListaBigliettiClienteResponse response = biglietteriaStub.listaBigliettiCliente(listaRequest);

        assertEquals(2, response.getBigliettiCount());
        assertTrue(response.getBigliettiList().stream()
                .allMatch(b -> clienteTest.equals(b.getClienteId())));
    }

    @Test
    void testCalcoloPrezziDiversiTipiBigliettoGrpc() {
        String dataViaggio = LocalDateTime.now().plusDays(1).toString();
        int distanza = 300;

        BigliettoResponse regionale = biglietteriaStub.acquistaBiglietto(
                AcquistaBigliettoRequest.newBuilder()
                        .setClienteId("C1")
                        .setTipoBiglietto("REGIONALE")
                        .setPartenza("Test1")
                        .setArrivo("Test2")
                        .setDataViaggio(dataViaggio)
                        .setDistanzaKm(distanza)
                        .build()
        );

        BigliettoResponse intercity = biglietteriaStub.acquistaBiglietto(
                AcquistaBigliettoRequest.newBuilder()
                        .setClienteId("C1")
                        .setTipoBiglietto("INTERCITY")
                        .setPartenza("Test1")
                        .setArrivo("Test2")
                        .setDataViaggio(dataViaggio)
                        .setDistanzaKm(distanza)
                        .build()
        );

        BigliettoResponse frecciaRossa = biglietteriaStub.acquistaBiglietto(
                AcquistaBigliettoRequest.newBuilder()
                        .setClienteId("C1")
                        .setTipoBiglietto("FRECCIA_ROSSA")
                        .setPartenza("Test1")
                        .setArrivo("Test2")
                        .setDataViaggio(dataViaggio)
                        .setDistanzaKm(distanza)
                        .build()
        );

        assertEquals(24.0, regionale.getPrezzo(), 0.01);
        assertEquals(36.0, intercity.getPrezzo(), 0.01);
        assertEquals(54.0, frecciaRossa.getPrezzo(), 0.01);

        assertTrue(regionale.getPrezzo() < intercity.getPrezzo());
        assertTrue(intercity.getPrezzo() < frecciaRossa.getPrezzo());
    }
}