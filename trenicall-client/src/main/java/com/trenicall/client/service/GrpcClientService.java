package com.trenicall.client.service;

import com.google.protobuf.Empty;
import com.trenicall.server.grpc.biglietteria.*;
import com.trenicall.server.grpc.cliente.*;
import com.trenicall.server.grpc.prenotazione.*;
import com.trenicall.server.grpc.notifica.*;
import com.trenicall.server.grpc.promozione.ListaPromozioniResponse;
import com.trenicall.server.grpc.promozione.PromozioneResponse;
import com.trenicall.server.grpc.promozione.PromozioneServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcClientService {

    private final ManagedChannel channel;
    private final BiglietteriaServiceGrpc.BiglietteriaServiceBlockingStub biglietteriaStub;
    private final ClienteServiceGrpc.ClienteServiceBlockingStub clienteStub;
    private final PrenotazioneServiceGrpc.PrenotazioneServiceBlockingStub prenotazioneStub;
    private final NotificaServiceGrpc.NotificaServiceBlockingStub notificaBlockingStub;
    private final NotificaServiceGrpc.NotificaServiceStub notificaAsyncStub;
    private final PromozioneServiceGrpc.PromozioneServiceBlockingStub promozioneStub;

    public GrpcClientService(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.biglietteriaStub = BiglietteriaServiceGrpc.newBlockingStub(channel);
        this.clienteStub = ClienteServiceGrpc.newBlockingStub(channel);
        this.prenotazioneStub = PrenotazioneServiceGrpc.newBlockingStub(channel);
        this.notificaBlockingStub = NotificaServiceGrpc.newBlockingStub(channel);
        this.notificaAsyncStub = NotificaServiceGrpc.newStub(channel);
        this.promozioneStub = PromozioneServiceGrpc.newBlockingStub(channel);
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

    public BigliettoResponse modificaBiglietto(String bigliettoId, String nuovaData) {
        ModificaBigliettoRequest request = ModificaBigliettoRequest.newBuilder()
                .setBigliettoId(bigliettoId)
                .setNuovaData(nuovaData)
                .build();

        return biglietteriaStub.modificaBiglietto(request);
    }

    public List<PrenotazioneResponse> listaPrenotazioniCliente(String clienteId) {
        ListaPrenotazioniRequest request = ListaPrenotazioniRequest.newBuilder()
                .setClienteId(clienteId)
                .build();

        return prenotazioneStub.listaPrenotazioniAttive(request).getPrenotazioniList();
    }

    public BigliettoResponse confermaAcquistoPrenotazione(String prenotazioneId) {
        ConfermaAcquistoRequest request = ConfermaAcquistoRequest.newBuilder()
                .setPrenotazioneId(prenotazioneId)
                .build();

        return prenotazioneStub.confermaAcquisto(request);
    }

    public void annullaPrenotazione(String prenotazioneId) {
        AnnullaPrenotazioneRequest request = AnnullaPrenotazioneRequest.newBuilder()
                .setPrenotazioneId(prenotazioneId)
                .build();

        prenotazioneStub.annullaPrenotazione(request);
    }

    public List<TrainInfo> listaTreniAttivi() {
        ListaTreniResponse res = notificaBlockingStub.listaTreniAttivi(Empty.getDefaultInstance());
        return new ArrayList<>(res.getTreniList());
    }

    public void seguiTreno(String clienteId, String trenoCodice, StreamObserver<NotificaResponse> observer) {
        SeguiTrenoRequest req = SeguiTrenoRequest.newBuilder()
                .setClienteId(clienteId == null ? "" : clienteId)
                .setTrenoId(trenoCodice)
                .build();
        notificaAsyncStub.seguiTreno(req, observer);
    }

    public List<PromozioneResponse> listaPromozioniAttive() {
        ListaPromozioniResponse response = promozioneStub.listaPromozioniAttive(Empty.getDefaultInstance());
        return new ArrayList<>(response.getPromozioniList());
    }

    public boolean logoutNotifiche(String clienteId) {
        LogoutRequest request = LogoutRequest.newBuilder()
                .setClienteId(clienteId)
                .build();
        LogoutResponse response = notificaBlockingStub.logoutNotifiche(request);
        return response.getSuccess();
    }


    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}