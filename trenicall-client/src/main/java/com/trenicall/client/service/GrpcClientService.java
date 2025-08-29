package com.trenicall.client.service;

import com.trenicall.server.grpc.biglietteria.*;
import com.trenicall.server.grpc.cliente.*;
import com.trenicall.server.grpc.prenotazione.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcClientService {

    private final ManagedChannel channel;
    private final BiglietteriaServiceGrpc.BiglietteriaServiceBlockingStub biglietteriaStub;
    private final ClienteServiceGrpc.ClienteServiceBlockingStub clienteStub;
    private final PrenotazioneServiceGrpc.PrenotazioneServiceBlockingStub prenotazioneStub;

    public GrpcClientService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.biglietteriaStub = BiglietteriaServiceGrpc.newBlockingStub(channel);
        this.clienteStub = ClienteServiceGrpc.newBlockingStub(channel);
        this.prenotazioneStub = PrenotazioneServiceGrpc.newBlockingStub(channel);
    }

    public RicercaBigliettiResponse ricercaBiglietti(String partenza, String arrivo, String dataViaggio) {
        RicercaBigliettiRequest request = RicercaBigliettiRequest.newBuilder()
                .setPartenza(partenza)
                .setArrivo(arrivo)
                .setDataViaggio(dataViaggio)
                .setClasseServizio("STANDARD")
                .setSoloAltaVelocita(false)
                .setIncludiPromozioni(true)
                .build();

        return biglietteriaStub.ricercaBiglietti(request);
    }

    public BigliettoResponse acquistaBiglietto(String clienteId, String tipoBiglietto,
                                               String partenza, String arrivo, String dataViaggio, int distanzaKm) {
        AcquistaBigliettoRequest request = AcquistaBigliettoRequest.newBuilder()
                .setClienteId(clienteId)
                .setTipoBiglietto(tipoBiglietto)
                .setPartenza(partenza)
                .setArrivo(arrivo)
                .setDataViaggio(dataViaggio)
                .setDistanzaKm(distanzaKm)
                .build();

        return biglietteriaStub.acquistaBiglietto(request);
    }

    public List<BigliettoResponse> listaBigliettiCliente(String clienteId) {
        ListaBigliettiClienteRequest request = ListaBigliettiClienteRequest.newBuilder()
                .setClienteId(clienteId)
                .build();

        return biglietteriaStub.listaBigliettiCliente(request).getBigliettiList();
    }

    public ClienteResponse registraCliente(String id, String nome, String email, String telefono) {
        RegistraClienteRequest request = RegistraClienteRequest.newBuilder()
                .setId(id)
                .setNome(nome)
                .setEmail(email)
                .setTelefono(telefono)
                .build();

        return clienteStub.registraCliente(request);
    }

    public ClienteResponse dettagliCliente(String clienteId) {
        DettagliClienteRequest request = DettagliClienteRequest.newBuilder()
                .setClienteId(clienteId)
                .build();

        return clienteStub.dettagliCliente(request);
    }

    public PrenotazioneResponse creaPrenotazione(String clienteId, String tipoBiglietto,
                                                 String partenza, String arrivo, String dataViaggio, int distanzaKm, int minutiValidita) {
        CreaPrenotazioneRequest request = CreaPrenotazioneRequest.newBuilder()
                .setClienteId(clienteId)
                .setTipoBiglietto(tipoBiglietto)
                .setPartenza(partenza)
                .setArrivo(arrivo)
                .setDataViaggio(dataViaggio)
                .setDistanzaKm(distanzaKm)
                .setMinutiValidita(minutiValidita)
                .build();

        return prenotazioneStub.creaPrenotazione(request);
    }

    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}