package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.services.BiglietteriaService;
import com.trenicall.server.business.services.PrenotazioneService;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.prenotazione.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
public class PrenotazioneServiceImpl extends PrenotazioneServiceGrpc.PrenotazioneServiceImplBase {

    private final PrenotazioneService prenotazioneService;
    private final BiglietteriaService biglietteriaService;

    public PrenotazioneServiceImpl(PrenotazioneService prenotazioneService,
                                   BiglietteriaService biglietteriaService) {
        this.prenotazioneService = prenotazioneService;
        this.biglietteriaService = biglietteriaService;
    }

    @Override
    public void creaPrenotazione(CreaPrenotazioneRequest request,
                                 StreamObserver<PrenotazioneResponse> responseObserver) {
        Prenotazione p = prenotazioneService.creaPrenotazione(
                request.getClienteId(),
                TipoBiglietto.valueOf(request.getTipoBiglietto()),
                request.getPartenza(),
                request.getArrivo(),
                LocalDateTime.parse(request.getDataViaggio()),
                request.getDistanzaKm(),
                request.getMinutiValidita()
        );

        PrenotazioneResponse response = PrenotazioneResponse.newBuilder()
                .setId(p.getId())
                .setClienteId(p.getCliente().getId())
                .setBigliettoId(p.getBiglietto().getId())
                .setDataCreazione(p.getDataCreazione().toString())
                .setScadenza(p.getScadenza().toString())
                .setAttiva(p.isAttiva())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void confermaAcquisto(ConfermaAcquistoRequest request,
                                 StreamObserver<BigliettoResponse> responseObserver) {
        Biglietto b = prenotazioneService.confermaAcquisto(request.getPrenotazioneId(), biglietteriaService);

        BigliettoResponse response = BigliettoResponse.newBuilder()
                .setId(b.getId())
                .setClienteId(b.getClienteId())
                .setTipo(b.getTipo().name())
                .setPartenza(b.getPartenza())
                .setArrivo(b.getArrivo())
                .setDataViaggio(b.getDataViaggio().toString())
                .setDistanzaKm(b.getDistanzaKm())
                .setPrezzo(b.getPrezzo())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listaPrenotazioniAttive(ListaPrenotazioniRequest request,
                                        StreamObserver<ListaPrenotazioniResponse> responseObserver) {
        Collection<Prenotazione> attive = prenotazioneService.getPrenotazioniAttive();
        ListaPrenotazioniResponse.Builder builder = ListaPrenotazioniResponse.newBuilder();

        attive.forEach(p -> {
            PrenotazioneResponse response = PrenotazioneResponse.newBuilder()
                    .setId(p.getId())
                    .setClienteId(p.getCliente().getId())
                    .setBigliettoId(p.getBiglietto().getId())
                    .setDataCreazione(p.getDataCreazione().toString())
                    .setScadenza(p.getScadenza().toString())
                    .setAttiva(p.isAttiva())
                    .build();
            builder.addPrenotazioni(response);
        });

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}


