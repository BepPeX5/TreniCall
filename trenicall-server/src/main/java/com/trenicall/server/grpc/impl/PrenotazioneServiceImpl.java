package com.trenicall.server.grpc.impl;

import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.prenotazione.*;
import com.trenicall.server.grpc.prenotazione.PrenotazioneProto.*;
import com.trenicall.server.grpc.biglietteria.BiglietteriaProto.*;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrenotazioneServiceImpl extends PrenotazioneServiceGrpc.PrenotazioneServiceImplBase {

    private final Map<String, PrenotazioneResponse> prenotazioni = new ConcurrentHashMap<>();

    @Override
    public void creaPrenotazione(CreaPrenotazioneRequest request, StreamObserver<PrenotazioneResponse> responseObserver) {
        String id = UUID.randomUUID().toString();
        PrenotazioneResponse p = PrenotazioneResponse.newBuilder()
                .setId(id)
                .setClienteId(request.getClienteId())
                .setTrenoId("TEMP_TRENO")
                .setBigliettoId(UUID.randomUUID().toString())
                .setDataCreazione(new Date().toString())
                .setScadenza(new Date(System.currentTimeMillis() + request.getMinutiValidita() * 60000L).toString())
                .setAttiva(true)
                .build();
        prenotazioni.put(id, p);
        responseObserver.onNext(p);
        responseObserver.onCompleted();
    }

    @Override
    public void confermaAcquisto(ConfermaAcquistoRequest request, StreamObserver<BigliettoResponse> responseObserver) {
        PrenotazioneResponse p = prenotazioni.get(request.getPrenotazioneId());
        if (p == null) {
            responseObserver.onError(new IllegalArgumentException("Prenotazione non trovata"));
            return;
        }
        BigliettoResponse b = BigliettoResponse.newBuilder()
                .setId(p.getBigliettoId())
                .setClienteId(p.getClienteId())
                .setTipo("REGIONALE")
                .setStato("Pagato")
                .setPartenza("TEMP_PARTENZA")
                .setArrivo("TEMP_ARRIVO")
                .setDataViaggio(new Date().toString())
                .setDistanzaKm(100)
                .setPrezzo(10.0)
                .build();
        responseObserver.onNext(b);
        responseObserver.onCompleted();
    }

    @Override
    public void listaPrenotazioniAttive(ListaPrenotazioniRequest request, StreamObserver<ListaPrenotazioniResponse> responseObserver) {
        List<PrenotazioneResponse> attive = new ArrayList<>();
        for (PrenotazioneResponse p : prenotazioni.values()) {
            if (p.getClienteId().equals(request.getClienteId()) && p.getAttiva()) {
                attive.add(p);
            }
        }
        ListaPrenotazioniResponse resp = ListaPrenotazioniResponse.newBuilder().addAllPrenotazioni(attive).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
