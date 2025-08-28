package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.grpc.biglietteria.*;
import com.trenicall.server.grpc.biglietteria.BiglietteriaProto.*;
import io.grpc.stub.StreamObserver;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BiglietteriaServiceImpl extends BiglietteriaServiceGrpc.BiglietteriaServiceImplBase {

    private final Map<String, Biglietto> biglietti = new ConcurrentHashMap<>();

    @Override
    public void ricercaBiglietti(RicercaBigliettiRequest request, StreamObserver<RicercaBigliettiResponse> responseObserver) {
        Biglietto b = new Biglietto(
                UUID.randomUUID().toString(),
                "ANONIMO",
                new StatoPrenotato(),
                TipoBiglietto.REGIONALE,
                request.getPartenza(),
                request.getArrivo(),
                LocalDateTime.now(),
                100
        );

        biglietti.put(b.getId(), b);

        BigliettoResponse br = BigliettoResponse.newBuilder()
                .setId(b.getId())
                .setClienteId(b.getClienteId())
                .setTipo(b.getTipo().name())
                .setStato(b.getStato().getNomeStato())
                .setPartenza(b.getPartenza())
                .setArrivo(b.getArrivo())
                .setDataViaggio(b.getDataViaggio().toString())
                .setDistanzaKm(b.getDistanzaKm())
                .setPrezzo(b.getPrezzo())
                .build();

        RicercaBigliettiResponse resp = RicercaBigliettiResponse.newBuilder()
                .addRisultati(br)
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void acquistaBiglietto(AcquistaBigliettoRequest request, StreamObserver<BigliettoResponse> responseObserver) {
        Biglietto b = new Biglietto(
                UUID.randomUUID().toString(),
                request.getClienteId(),
                new StatoPrenotato(),
                TipoBiglietto.fromString(request.getTipoBiglietto()),
                request.getPartenza(),
                request.getArrivo(),
                LocalDateTime.parse(request.getDataViaggio()),
                request.getDistanzaKm()
        );

        biglietti.put(b.getId(), b);

        BigliettoResponse br = BigliettoResponse.newBuilder()
                .setId(b.getId())
                .setClienteId(b.getClienteId())
                .setTipo(b.getTipo().name())
                .setStato(b.getStato().getNomeStato())
                .setPartenza(b.getPartenza())
                .setArrivo(b.getArrivo())
                .setDataViaggio(b.getDataViaggio().toString())
                .setDistanzaKm(b.getDistanzaKm())
                .setPrezzo(b.getPrezzo())
                .build();

        responseObserver.onNext(br);
        responseObserver.onCompleted();
    }

    @Override
    public void modificaBiglietto(ModificaBigliettoRequest request, StreamObserver<BigliettoResponse> responseObserver) {
        Biglietto b = biglietti.get(request.getBigliettoId());
        if (b == null) {
            responseObserver.onError(new IllegalArgumentException("Biglietto non trovato"));
            return;
        }

        b.setDataViaggio(LocalDateTime.parse(request.getNuovaData()));

        BigliettoResponse br = BigliettoResponse.newBuilder()
                .setId(b.getId())
                .setClienteId(b.getClienteId())
                .setTipo(b.getTipo().name())
                .setStato(b.getStato().getNomeStato())
                .setPartenza(b.getPartenza())
                .setArrivo(b.getArrivo())
                .setDataViaggio(b.getDataViaggio().toString())
                .setDistanzaKm(b.getDistanzaKm())
                .setPrezzo(b.getPrezzo())
                .build();

        responseObserver.onNext(br);
        responseObserver.onCompleted();
    }

    @Override
    public void listaBigliettiCliente(ListaBigliettiClienteRequest request, StreamObserver<ListaBigliettiClienteResponse> responseObserver) {
        List<BigliettoResponse> list = new ArrayList<>();
        for (Biglietto b : biglietti.values()) {
            if (b.getClienteId().equals(request.getClienteId())) {
                BigliettoResponse br = BigliettoResponse.newBuilder()
                        .setId(b.getId())
                        .setClienteId(b.getClienteId())
                        .setTipo(b.getTipo().name())
                        .setStato(b.getStato().getNomeStato())
                        .setPartenza(b.getPartenza())
                        .setArrivo(b.getArrivo())
                        .setDataViaggio(b.getDataViaggio().toString())
                        .setDistanzaKm(b.getDistanzaKm())
                        .setPrezzo(b.getPrezzo())
                        .build();
                list.add(br);
            }
        }

        ListaBigliettiClienteResponse resp = ListaBigliettiClienteResponse.newBuilder()
                .addAllBiglietti(list)
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
