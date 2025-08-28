package com.trenicall.server.grpc.impl;

import com.trenicall.server.grpc.promozione.*;
import com.trenicall.server.grpc.promozione.PromozioneProto.*;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PromozioneServiceImpl extends PromozioneServiceGrpc.PromozioneServiceImplBase {

    private final Map<String, PromozioneResponse> promozioni = new ConcurrentHashMap<>();

    @Override
    public void creaPromozione(CreaPromozioneRequest request, StreamObserver<PromozioneResponse> responseObserver) {
        String id = UUID.randomUUID().toString();
        PromozioneResponse p = PromozioneResponse.newBuilder()
                .setId(id)
                .setNome(request.getNome())
                .setPercentualeSconto(request.getPercentualeSconto())
                .setInizio(request.getInizio())
                .setFine(request.getFine())
                .setTrattaPartenza(request.getTrattaPartenza())
                .setTrattaArrivo(request.getTrattaArrivo())
                .setSoloFedelta(request.getSoloFedelta())
                .build();
        promozioni.put(id, p);
        responseObserver.onNext(p);
        responseObserver.onCompleted();
    }

    @Override
    public void listaPromozioni(ListaPromozioniRequest request, StreamObserver<ListaPromozioniResponse> responseObserver) {
        ListaPromozioniResponse resp = ListaPromozioniResponse.newBuilder()
                .addAllPromozioni(promozioni.values())
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
