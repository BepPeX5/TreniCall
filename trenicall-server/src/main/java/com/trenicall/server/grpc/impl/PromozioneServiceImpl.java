package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.services.PromozioneService;
import com.trenicall.server.domain.entities.Promozione;
import com.trenicall.server.grpc.promozione.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PromozioneServiceImpl extends PromozioneServiceGrpc.PromozioneServiceImplBase {

    private final PromozioneService promozioneService;

    public PromozioneServiceImpl(PromozioneService promozioneService) {
        this.promozioneService = promozioneService;
    }

    @Override
    public void creaPromozione(CreaPromozioneRequest request,
                               StreamObserver<PromozioneResponse> responseObserver) {
        Promozione promozione = new Promozione(
                UUID.randomUUID().toString(),
                request.getNome(),
                request.getPercentualeSconto(),
                LocalDateTime.parse(request.getInizio()),
                LocalDateTime.parse(request.getFine()),
                request.getTrattaPartenza(),
                request.getTrattaArrivo(),
                request.getSoloFedelta()
        );
        promozioneService.aggiungiPromozione(promozione, null);

        PromozioneResponse response = PromozioneResponse.newBuilder()
                .setId(promozione.getId())
                .setNome(promozione.getNome())
                .setPercentualeSconto(promozione.getPercentualeSconto())
                .setInizio(promozione.getInizio().toString())
                .setFine(promozione.getFine().toString())
                .setTrattaPartenza(promozione.getTrattaPartenza())
                .setTrattaArrivo(promozione.getTrattaArrivo())
                .setSoloFedelta(promozione.isSoloFedelta())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listaPromozioni(ListaPromozioniRequest request,
                                StreamObserver<ListaPromozioniResponse> responseObserver) {
        List<Promozione> promozioni = promozioneService.getPromozioni();
        ListaPromozioniResponse.Builder builder = ListaPromozioniResponse.newBuilder();
        promozioni.forEach(p -> builder.addPromozioni(
                PromozioneResponse.newBuilder()
                        .setId(p.getId())
                        .setNome(p.getNome())
                        .setPercentualeSconto(p.getPercentualeSconto())
                        .setInizio(p.getInizio().toString())
                        .setFine(p.getFine().toString())
                        .setTrattaPartenza(p.getTrattaPartenza())
                        .setTrattaArrivo(p.getTrattaArrivo())
                        .setSoloFedelta(p.isSoloFedelta())
                        .build()
        ));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}

